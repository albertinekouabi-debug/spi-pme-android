package com.spipme.app.core.sync.android

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.spipme.app.core.security.TokenManager
import com.spipme.app.core.sync.EtatSynchronisation
import com.spipme.app.core.sync.OperationEnAttente
import com.spipme.app.core.sync.StatutOperation
import com.spipme.app.data.local.dao.OperationDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.filter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

/** Indique au bandeau qu'une passe de synchronisation est en cours (renseigné par le worker). */
@Singleton
class SyncActivite @Inject constructor() {
    private val _enCours = MutableStateFlow(false)
    val enCours: StateFlow<Boolean> = _enCours.asStateFlow()
    fun definirEnCours(valeur: Boolean) { _enCours.value = valeur }
}

/** État réseau réel (réseau validé avec accès Internet), pas seulement « connecté à un Wi‑Fi ». */
@Singleton
class ConnectivityMonitor @Inject constructor(@ApplicationContext private val context: Context) {
    val enLigne: Flow<Boolean> = callbackFlow {
        val gestionnaire = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        fun actuel(): Boolean = gestionnaire.getNetworkCapabilities(gestionnaire.activeNetwork)?.let {
            it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } ?: false

        val rappel = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(actuel()) }
            override fun onLost(network: Network) { trySend(actuel()) }
            override fun onCapabilitiesChanged(network: Network, capacites: NetworkCapabilities) { trySend(actuel()) }
        }
        trySend(actuel())
        gestionnaire.registerNetworkCallback(
            NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(), rappel,
        )
        awaitClose { gestionnaire.unregisterNetworkCallback(rappel) }
    }.distinctUntilChanged()
}

/**
 * Point d'entrée unique de l'offline-first pour l'UI : enfile les écritures, expose l'état de
 * synchronisation, déclenche l'envoi au retour du réseau / à la reconnexion, et porte les actions
 * de résolution (relancer, abandonner, garder ma version).
 */
@Singleton
class SynchronisationRepository @Inject constructor(
    private val dao: OperationDao,
    private val scheduler: SyncScheduler,
    private val connectivite: ConnectivityMonitor,
    private val activite: SyncActivite,
    private val tokenManager: TokenManager,
    private val json: Json,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val etat: Flow<EtatSynchronisation> = combine(
        connectivite.enLigne, dao.compterEnAttente(), dao.compterConflits(), dao.compterEchecs(), activite.enCours,
    ) { enLigne, enAttente, conflits, echecs, enCours ->
        EtatSynchronisation(enLigne, enAttente, conflits, echecs, enCours)
    }

    val aTraiter: Flow<List<OperationEnAttente>> = dao.aTraiter().map { liste -> liste.map { it.versDomaine() } }

    fun entitesEnAttente(entityType: String): Flow<List<String>> = dao.entitesEnAttente(entityType)

    fun operationsEnAttente(vararg types: String): Flow<List<OperationEnAttente>> =
        dao.enAttentePourTypes(types.toList()).map { liste -> liste.map { it.versDomaine() } }

    /** À appeler une fois (Application.onCreate) : filet périodique + déclencheurs automatiques. */
    fun demarrer() {
        scheduler.planifierPeriodique()
        scheduler.declencher()  // reprise au lancement : file persistante après fermeture/redémarrage
        connectivite.enLigne.filter { it }.onEach { scheduler.declencher() }.launchIn(scope)
        // Reconnexion après session expirée : la file mise en pause repart.
        tokenManager.estConnecteFlow.distinctUntilChanged().filter { it }.onEach { scheduler.declencher() }.launchIn(scope)
    }

    /** Enfile une écriture. Une clé d'idempotence déjà présente (double appui) est ignorée sans erreur. */
    suspend fun enfiler(operation: OperationEnAttente) {
        try {
            dao.inserer(operation.versEntite())
        } catch (e: SQLiteConstraintException) {
            return  // déjà enfilée : la saisie n'est jamais dupliquée
        }
        scheduler.declencher()
    }

    suspend fun relancer(id: Long) { dao.relancer(id); scheduler.declencher() }

    suspend fun abandonner(id: Long) {
        val op = dao.parId(id) ?: return
        // On n'abandonne que ce qui attend une décision : jamais une opération encore à envoyer.
        if (op.statut == StatutOperation.CONFLIT.name || op.statut == StatutOperation.ECHEC_DEFINITIF.name) dao.supprimer(id)
    }

    /**
     * « Garder ma version » : on rejoue la modification locale en s'appuyant sur la version serveur
     * connue (If-Match mis à jour). Le serveur re‑contrôle : si elle a encore changé, nouveau conflit.
     * Refusé pour toute opération sans version de base (écritures financières append-only).
     */
    suspend fun garderMaVersion(id: Long): Boolean {
        val op = dao.parId(id) ?: return false
        if (op.statut != StatutOperation.CONFLIT.name || op.versionBase == null) return false
        val versionServeur = op.conflitJson?.let {
            runCatching { json.parseToJsonElement(it).jsonObject["version_serveur"]?.jsonPrimitive?.intOrNull }.getOrNull()
        } ?: return false
        dao.mettreAJour(op.copy(
            statut = StatutOperation.EN_ATTENTE.name, versionBase = versionServeur, tentatives = 0,
            prochaineTentativeLe = 0, messageErreur = null, conflitJson = null,
        ))
        scheduler.declencher()
        return true
    }
}
