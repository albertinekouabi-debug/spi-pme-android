package com.spipme.app.core.sync

/**
 * Lecture minimale des payloads que NOUS avons construits (OperationsMetier.json/jsonObjet) : sert à
 * afficher une saisie en attente (« Riz — 50 ») sans dépendre d'un parseur JSON complet. Ne prétend pas
 * lire du JSON arbitraire : uniquement des champs de premier niveau, texte ou nombre.
 */
object PayloadJson {
    fun champ(json: String?, cle: String): String? {
        if (json.isNullOrBlank()) return null
        val k = Regex.escape(cle)
        Regex("\"$k\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").find(json)?.let { return desechapper(it.groupValues[1]) }
        Regex("\"$k\"\\s*:\\s*(-?[0-9]+(?:\\.[0-9]+)?|true|false)").find(json)?.let { return it.groupValues[1] }
        return null
    }

    private fun desechapper(texte: String): String = buildString {
        var i = 0
        while (i < texte.length) {
            val c = texte[i]
            if (c != '\\' || i + 1 >= texte.length) { append(c); i++; continue }
            when (val n = texte[i + 1]) {
                'n' -> { append('\n'); i += 2 }
                't' -> { append('\t'); i += 2 }
                'r' -> { append('\r'); i += 2 }
                'u' -> {
                    val hex = texte.substring(i + 2, minOf(i + 6, texte.length))
                    val code = hex.toIntOrNull(16)
                    if (hex.length == 4 && code != null) { append(code.toChar()); i += 6 } else { append(n); i += 2 }
                }
                else -> { append(n); i += 2 }
            }
        }
    }
}

/** Texte lisible d'une opération en attente (liste « à traiter » et superpositions sur les écrans). */
object LibelleOperation {
    fun titre(type: String): String = when (type) {
        "ANNULER_FACTURE" -> "Annulation de facture"
        "EMETTRE_AVOIR" -> "Avoir"
        "CONTRE_PASSER_TRANSACTION" -> "Contre-écriture"
        "CREER_TRANSACTION" -> "Nouvelle transaction"
        "CREER_RESSOURCE" -> "Nouvelle ressource"
        "MODIFIER_RESSOURCE" -> "Modification de ressource"
        "CREER_ENTITE" -> "Nouvelle entité"
        "CREER_TACHE" -> "Nouvelle tâche"
        else -> type
    }

    /** Ligne de détail issue du payload (jamais d'invention : champ absent → chaîne vide). */
    fun detail(type: String, payloadJson: String?): String = when (type) {
        "CREER_RESSOURCE" -> listOfNotNull(PayloadJson.champ(payloadJson, "nom"),
            PayloadJson.champ(payloadJson, "niveau_actuel")?.let { "niveau $it" }).joinToString(" — ")
        "MODIFIER_RESSOURCE" -> PayloadJson.champ(payloadJson, "nom").orEmpty()
        "CREER_TRANSACTION" -> listOfNotNull(PayloadJson.champ(payloadJson, "type")?.replace('_', ' '),
            (PayloadJson.champ(payloadJson, "montant") ?: PayloadJson.champ(payloadJson, "quantite"))).joinToString(" ")
        "CREER_ENTITE" -> PayloadJson.champ(payloadJson, "nom").orEmpty()
        "CREER_TACHE" -> PayloadJson.champ(payloadJson, "titre").orEmpty()
        "ANNULER_FACTURE", "EMETTRE_AVOIR", "CONTRE_PASSER_TRANSACTION" -> PayloadJson.champ(payloadJson, "motif").orEmpty()
        else -> ""
    }
}
