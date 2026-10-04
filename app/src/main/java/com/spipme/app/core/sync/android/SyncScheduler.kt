package com.spipme.app.core.sync.android

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.spipme.app.core.sync.RaisonArret
import com.spipme.app.core.sync.SyncEngine
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    private val contrainte = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    /**
     * À appeler dès qu'une opération est enfilée, au retour du réseau et après reconnexion.
     * APPEND_OR_REPLACE : un envoi déclenché pendant qu'un autre tourne s'exécute APRÈS lui
     * (KEEP perdrait le déclencheur ; REPLACE interromprait l'envoi en cours).
     */
    fun declencher() {
        val requete = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(contrainte)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(NOM_UNIQUE, ExistingWorkPolicy.APPEND_OR_REPLACE, requete)
    }

    /** Filet de sécurité : reprise même si aucun déclencheur n'a eu lieu (app fermée, téléphone redémarré). */
    fun planifierPeriodique() {
        val requete = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(contrainte)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(NOM_PERIODIQUE, ExistingPeriodicWorkPolicy.KEEP, requete)
    }

    private companion object {
        const val NOM_UNIQUE = "spi-pme-sync"
        const val NOM_PERIODIQUE = "spi-pme-sync-periodique"
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SyncEntryPoint {
    fun moteur(): SyncEngine
    fun activite(): SyncActivite
}

/**
 * Worker sans @HiltWorker (évite la dépendance hilt-work et la modification du manifeste) : le moteur
 * est récupéré par EntryPoint. La file étant persistante, toute interruption est reprise telle quelle.
 */
class SyncWorker(contexte: Context, parametres: WorkerParameters) : CoroutineWorker(contexte, parametres) {
    override suspend fun doWork(): Result {
        val points = EntryPointAccessors.fromApplication(applicationContext, SyncEntryPoint::class.java)
        points.activite().definirEnCours(true)
        return try {
            val bilan = points.moteur().synchroniser()
            when {
                bilan.arretPour == RaisonArret.RESEAU -> Result.retry()
                // Session expirée : inutile de boucler ; la synchronisation repart à la reconnexion.
                bilan.arretPour == RaisonArret.SESSION_EXPIREE -> Result.success()
                bilan.reportees > 0 -> Result.retry()
                else -> Result.success()
            }
        } catch (e: Exception) {
            if (runAttemptCount >= MAX_REPRISES) Result.failure() else Result.retry()
        } finally {
            points.activite().definirEnCours(false)
        }
    }

    private companion object { const val MAX_REPRISES = 8 }
}
