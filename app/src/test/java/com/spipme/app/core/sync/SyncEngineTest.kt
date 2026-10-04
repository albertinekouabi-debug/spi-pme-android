package com.spipme.app.core.sync

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class FileMemoire : FileOperations {
    val lignes = linkedMapOf<Long, OperationEnAttente>()
    private var seq = 0L
    fun ajouter(op: OperationEnAttente): OperationEnAttente { val o = op.copy(id = ++seq); lignes[o.id] = o; return o }
    override suspend fun operationsAEnvoyer() =
        lignes.values.filter { it.statut == StatutOperation.EN_ATTENTE }.sortedWith(compareBy({ it.creeLe }, { it.id }))
    override suspend fun mettreAJour(operation: OperationEnAttente) { lignes[operation.id] = operation }
    override suspend fun supprimer(id: Long) { lignes.remove(id) }
    override suspend fun recupererOrphelines() {
        lignes.replaceAll { _, o -> if (o.statut == StatutOperation.EN_COURS) o.copy(statut = StatutOperation.EN_ATTENTE) else o }
    }
}

private class IdsMemoire : CorrespondancesIds {
    val table = mutableMapOf<String, String>()
    override suspend fun resoudre(referenceLocale: String) = table[referenceLocale]
    override suspend fun enregistrer(referenceLocale: String, idServeur: String) { table[referenceLocale] = idServeur }
}

/** Serveur simulé : applique chaque clé d'idempotence UNE fois, et sait « perdre » la réponse. */
private class ServeurSimule : PasserelleDistante {
    val vues = mutableListOf<OperationEnAttente>()
    val effets = mutableMapOf<String, String>()      // clé -> id créé
    var scenario: (OperationEnAttente, Int) -> ResultatEnvoi? = { _, _ -> null }
    private val appels = mutableMapOf<String, Int>()
    private var prochainId = 100
    override suspend fun envoyer(operation: OperationEnAttente): ResultatEnvoi {
        vues += operation
        val n = (appels[operation.idempotencyKey] ?: 0) + 1
        appels[operation.idempotencyKey] = n
        scenario(operation, n)?.let { return it }
        val id = effets.getOrPut(operation.idempotencyKey) { (prochainId++).toString() }
        return ResultatEnvoi.Succes(id)
    }
}

class SyncEngineTest {
    private var horloge = 1_000_000L
    private val file = FileMemoire()
    private val ids = IdsMemoire()
    private val serveur = ServeurSimule()
    private val moteur = SyncEngine(file, serveur, ids, BackoffPolicy(30_000, 3_600_000), maxTentatives = 4, maintenantMs = { horloge })
    private var compteur = 0
    private fun cle() = "cle-${++compteur}"
    private fun annuler(facture: String = "12") = file.ajouter(OperationsMetier.annulerFacture(facture, "Erreur", horloge + compteur, ::cle))

    private fun <T> run(bloc: suspend () -> T): T = runBlocking { bloc() }

    @Test fun `succes supprime l'operation et envoie la cle d'idempotence`() = run {
        val op = annuler()
        val bilan = moteur.synchroniser()
        assertEquals(1, bilan.envoyees); assertTrue(file.lignes.isEmpty())
        assertEquals(op.idempotencyKey, serveur.vues.single().idempotencyKey)
    }

    @Test fun `la meme cle est reutilisee a chaque tentative et le serveur n'applique qu'une fois`() = run {
        val op = annuler()
        // la requête ARRIVE au serveur mais la réponse se perd (coupure) : 2 fois, puis succès
        serveur.scenario = { o, n -> if (n <= 2) { serveur.effets.getOrPut(o.idempotencyKey) { "777" }; ResultatEnvoi.Reseau("réponse perdue") } else null }
        repeat(3) { moteur.synchroniser(); horloge += 10 * 60_000 }
        assertEquals(setOf(op.idempotencyKey), serveur.vues.map { it.idempotencyKey }.toSet())
        assertEquals(3, serveur.vues.size); assertEquals(1, serveur.effets.size)
        assertTrue(file.lignes.isEmpty())
    }

    @Test fun `erreur reseau arrete la passe sans compter d'echec et respecte le delai`() = run {
        annuler("1"); annuler("2")
        serveur.scenario = { _, _ -> ResultatEnvoi.Reseau("hors ligne") }
        val b1 = moteur.synchroniser()
        assertEquals(RaisonArret.RESEAU, b1.arretPour); assertEquals(1, serveur.vues.size)  // la 2e n'est pas tentée
        assertEquals(0, file.lignes.values.first().tentatives)
        serveur.vues.clear()
        moteur.synchroniser()                       // trop tôt : l'op n°1 est différée et ne repart pas avant son délai
        assertTrue(serveur.vues.none { it.entityId == "1" })
        horloge += 60_000; serveur.scenario = { _, _ -> null }
        assertEquals(2, moteur.synchroniser().envoyees)
    }

    @Test fun `rester hors ligne longtemps ne detruit jamais une saisie`() = run {
        annuler(); serveur.scenario = { _, _ -> ResultatEnvoi.Reseau("hors ligne") }
        repeat(50) { moteur.synchroniser(); horloge += 3_600_000 }
        val op = file.lignes.values.single()
        assertEquals(StatutOperation.EN_ATTENTE, op.statut); assertEquals(0, op.tentatives)
    }

    @Test fun `erreurs serveur - backoff exponentiel puis abandon au plafond`() = run {
        annuler(); serveur.scenario = { _, _ -> ResultatEnvoi.ServeurTemporaire(503, "indisponible") }
        val delais = mutableListOf<Long>()
        repeat(3) {
            moteur.synchroniser()
            val op = file.lignes.values.single()
            delais += op.prochaineTentativeLe - horloge
            horloge = op.prochaineTentativeLe
        }
        assertEquals(listOf(30_000L, 60_000L, 120_000L), delais)
        moteur.synchroniser()  // 4e tentative = plafond
        val op = file.lignes.values.single()
        assertEquals(StatutOperation.ECHEC_DEFINITIF, op.statut); assertEquals(4, op.tentatives)
        serveur.vues.clear(); horloge += 10_000_000; moteur.synchroniser()
        assertTrue("plus jamais renvoyée", serveur.vues.isEmpty())  // pas de boucle infinie
    }

    @Test fun `backoff plafonne sans depassement`() {
        val b = BackoffPolicy(30_000, 3_600_000)
        assertEquals(3_600_000L, b.delaiMs(40)); assertEquals(3_600_000L, b.delaiMs(8))
        assertEquals(30_000L, b.delaiMs(1))
        assertEquals(3_600_000L + 500, BackoffPolicy(30_000, 3_600_000) { 500L }.delaiMs(99))
    }

    @Test fun `echec definitif bloque les operations de la meme entite mais pas les autres`() = run {
        annuler("1"); file.ajouter(OperationsMetier.emettreAvoir("1", "m", null, horloge + 50, ::cle)); annuler("2")
        serveur.scenario = { o, _ -> if (o.type == "ANNULER_FACTURE" && o.entityId == "1") ResultatEnvoi.Definitive(409, "déjà annulée") else null }
        val bilan = moteur.synchroniser()
        assertEquals(1, bilan.echecsDefinitifs); assertEquals(1, bilan.bloquees); assertEquals(1, bilan.envoyees)
        assertTrue(serveur.vues.none { it.type == "EMETTRE_AVOIR" })
        assertTrue(file.lignes.values.any { it.statut == StatutOperation.ECHEC_DEFINITIF && it.messageErreur!!.startsWith("409") })
    }

    @Test fun `conflit 412 conserve les donnees serveur et n'est pas rejoue`() = run {
        val op = file.ajouter(OperationsMetier.modifier("MODIFIER_RESSOURCE", "ressource", "5", "resources/5/", "{\"nom\":\"A\"}", 3, horloge, ::cle))
        serveur.scenario = { _, _ -> ResultatEnvoi.Conflit(4, "{\"nom\":\"Serveur\"}") }
        val bilan = moteur.synchroniser()
        assertEquals(1, bilan.conflits)
        val stocke = file.lignes[op.id]!!
        assertEquals(StatutOperation.CONFLIT, stocke.statut); assertEquals("{\"nom\":\"Serveur\"}", stocke.conflitJson)
        assertEquals(3, stocke.versionBase)  // la modification locale reste intacte
        serveur.vues.clear(); moteur.synchroniser(); assertTrue(serveur.vues.isEmpty())
    }

    @Test fun `session expiree met en pause sans perdre ni compter, puis reprend`() = run {
        annuler("1"); annuler("2")
        serveur.scenario = { _, _ -> ResultatEnvoi.AuthExpiree }
        assertEquals(RaisonArret.SESSION_EXPIREE, moteur.synchroniser().arretPour)
        assertTrue(file.lignes.values.all { it.statut == StatutOperation.EN_ATTENTE && it.tentatives == 0 })
        serveur.scenario = { _, _ -> null }
        assertEquals(2, moteur.synchroniser().envoyees)
    }

    @Test fun `reprise apres arret brutal d'une operation restee EN_COURS avec la meme cle`() = run {
        val op = annuler()
        file.mettreAJour(op.copy(statut = StatutOperation.EN_COURS))   // process tué en plein envoi
        assertEquals(1, moteur.synchroniser().envoyees)
        assertEquals(op.idempotencyKey, serveur.vues.single().idempotencyKey)
    }

    @Test fun `ordre FIFO respecte`() = run {
        listOf("a", "b", "c").forEachIndexed { i, f -> file.ajouter(OperationsMetier.annulerFacture(f, "m", horloge + i, ::cle)) }
        moteur.synchroniser()
        assertEquals(listOf("a", "b", "c"), serveur.vues.map { it.entityId })
    }

    @Test fun `creation hors ligne puis operation dependante - id local remplace par id serveur`() = run {
        val ref = "${PREFIXE_ID_LOCAL}abc-123"
        file.ajouter(OperationsMetier.creer("CREER_RESSOURCE", "ressource", "resources/", "{\"nom\":\"Riz\"}", ref, horloge, ::cle))
        file.ajouter(OperationsMetier.modifier("MODIFIER_RESSOURCE", "ressource", ref, "resources/$ref/", "{\"nom\":\"Riz 2\"}", 1, horloge + 1, ::cle))
        assertEquals(2, moteur.synchroniser().envoyees)
        val creation = serveur.vues[0]; val modif = serveur.vues[1]
        assertEquals("100", ids.table[ref])
        assertEquals("resources/100/", modif.chemin); assertEquals("100", modif.entityId)
        assertFalse(modif.chemin.contains(PREFIXE_ID_LOCAL))
        assertEquals("resources/", creation.chemin)  // la création garde son propre chemin
    }

    @Test fun `operation dependante non envoyee si la creation a echoue`() = run {
        val ref = "${PREFIXE_ID_LOCAL}zzz"
        file.ajouter(OperationsMetier.creer("CREER_RESSOURCE", "ressource", "resources/", "{}", ref, horloge, ::cle))
        file.ajouter(OperationsMetier.modifier("MODIFIER_RESSOURCE", "ressource", ref, "resources/$ref/", "{}", 1, horloge + 1, ::cle))
        serveur.scenario = { o, _ -> if (o.type == "CREER_RESSOURCE") ResultatEnvoi.Definitive(400, "invalide") else null }
        val bilan = moteur.synchroniser()
        assertEquals(1, bilan.echecsDefinitifs); assertEquals(1, bilan.bloquees)
        assertEquals(1, serveur.vues.size)
    }

    @Test fun `reference locale referencee dans le payload d'une AUTRE entite est aussi resolue`() = run {
        val ref = "${PREFIXE_ID_LOCAL}res-1"
        file.ajouter(OperationsMetier.creer("CREER_RESSOURCE", "ressource", "resources/", "{}", ref, horloge, ::cle))
        file.ajouter(OperationsMetier.creer("CREER_TRANSACTION", "transaction", "transactions/",
            "{\"ressource\":\"$ref\",\"quantite\":\"-2\"}", "${PREFIXE_ID_LOCAL}tx-1", horloge + 1, ::cle))
        moteur.synchroniser()
        assertTrue(serveur.vues[1].payloadJson!!.contains("\"ressource\":\"100\""))
    }

    @Test fun `exception inattendue du transport est traitee comme transitoire sans perdre l'operation`() = run {
        annuler(); serveur.scenario = { _, _ -> throw IllegalStateException("bug") }
        moteur.synchroniser()
        val op = file.lignes.values.single()
        assertEquals(StatutOperation.EN_ATTENTE, op.statut); assertEquals(1, op.tentatives)
    }

    @Test fun `annulation de coroutine propage et rend l'operation a la file`() {
        annuler(); serveur.scenario = { _, _ -> throw CancellationException("arrêt du worker") }
        var propagee = false
        try { runBlocking { moteur.synchroniser() } } catch (e: CancellationException) { propagee = true }
        assertTrue(propagee); assertEquals(StatutOperation.EN_ATTENTE, file.lignes.values.single().statut)
    }

    @Test fun `json echappe les caracteres speciaux d'un motif`() {
        val j = OperationsMetier.json("motif" to "Client \"X\" \\ ligne1\nligne2\t\u0001")
        assertEquals("{\"motif\":\"Client \\\"X\\\" \\\\ ligne1\\nligne2\\t\\u0001\"}", j)
    }

    @Test fun `avoir partiel porte le montant et annulation n'en porte pas`() {
        assertEquals("{\"motif\":\"m\",\"montant\":\"300.00\"}", OperationsMetier.emettreAvoir("1", "m", "300.00", 0, ::cle).payloadJson)
        assertEquals("{\"motif\":\"m\"}", OperationsMetier.emettreAvoir("1", "m", null, 0, ::cle).payloadJson)
        assertNull(OperationsMetier.annulerFacture("1", "m", 0, ::cle).versionBase)
    }
}

class EtatSynchronisationTest {
    @Test fun `tout synchronise n'affiche rien`() {
        assertEquals(ResumeSynchronisation.A_JOUR, EtatSynchronisation().resume); assertNull(EtatSynchronisation().message)
    }
    @Test fun `hors ligne avec operations en attente`() {
        val e = EtatSynchronisation(enLigne = false, enAttente = 3)
        assertEquals(ResumeSynchronisation.HORS_LIGNE, e.resume); assertEquals("Hors ligne — 3 modifications en attente", e.message)
    }
    @Test fun `un conflit ou un echec prime sur hors ligne`() {
        assertEquals(ResumeSynchronisation.CONFLITS, EtatSynchronisation(enLigne = false, conflits = 1, echecs = 2).resume)
        assertEquals(ResumeSynchronisation.ECHECS, EtatSynchronisation(enLigne = false, echecs = 1).resume)
        assertEquals("1 conflit à résoudre", EtatSynchronisation(conflits = 1).message)
    }
    @Test fun `en cours prime sur en attente mais pas sur hors ligne`() {
        assertEquals(ResumeSynchronisation.EN_COURS, EtatSynchronisation(enAttente = 2, enCours = true).resume)
        assertEquals(ResumeSynchronisation.HORS_LIGNE, EtatSynchronisation(enLigne = false, enAttente = 2, enCours = true).resume)
    }
    @Test fun `pluriels corrects`() {
        assertEquals("1 modification en attente de synchronisation", EtatSynchronisation(enAttente = 1).message)
        assertEquals("2 modifications en attente de synchronisation", EtatSynchronisation(enAttente = 2).message)
    }
}

class PayloadEtLibellesTest {
    @Test fun `jsonObjet type les entiers, omet les nuls et echappe les textes`() {
        val j = OperationsMetier.jsonObjet("nom" to "Riz \"extra\"", "secteur" to 3, "unite" to null, "actif" to true, "niveau_actuel" to "50.00")
        assertEquals("{\"nom\":\"Riz \\\"extra\\\"\",\"secteur\":3,\"actif\":true,\"niveau_actuel\":\"50.00\"}", j)
    }
    @Test fun `champ relit texte, nombre et caracteres echappes`() {
        val j = OperationsMetier.jsonObjet("nom" to "A \"B\"\nC", "secteur" to 12, "niveau_actuel" to "7.5")
        assertEquals("A \"B\"\nC", PayloadJson.champ(j, "nom"))
        assertEquals("12", PayloadJson.champ(j, "secteur"))
        assertEquals("7.5", PayloadJson.champ(j, "niveau_actuel"))
    }
    @Test fun `champ absent ou json vide donne null`() {
        assertNull(PayloadJson.champ(null, "x")); assertNull(PayloadJson.champ("", "x")); assertNull(PayloadJson.champ("{\"a\":\"b\"}", "x"))
    }
    @Test fun `champ ne confond pas une cle avec un suffixe`() {
        assertNull(PayloadJson.champ("{\"surnom\":\"Z\"}", "nom"))
    }
    @Test fun `unicode echappe est relu`() {
        assertEquals("\u0001", PayloadJson.champ(OperationsMetier.json("m" to "\u0001"), "m"))
    }
    @Test fun `libelles lisibles pour les saisies hors ligne`() {
        val ressource = OperationsMetier.jsonObjet("nom" to "Riz", "niveau_actuel" to "50", "secteur" to 1)
        assertEquals("Nouvelle ressource", LibelleOperation.titre("CREER_RESSOURCE"))
        assertEquals("Riz — niveau 50", LibelleOperation.detail("CREER_RESSOURCE", ressource))
        val tx = OperationsMetier.jsonObjet("type" to "mouvement_stock", "quantite" to "-5")
        assertEquals("mouvement stock -5", LibelleOperation.detail("CREER_TRANSACTION", tx))
        assertEquals("Erreur", LibelleOperation.detail("ANNULER_FACTURE", OperationsMetier.json("motif" to "Erreur")))
        assertEquals("", LibelleOperation.detail("INCONNU", ressource))
        assertEquals("INCONNU", LibelleOperation.titre("INCONNU"))
    }
}
