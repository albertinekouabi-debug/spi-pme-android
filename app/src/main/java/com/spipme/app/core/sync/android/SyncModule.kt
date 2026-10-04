package com.spipme.app.core.sync.android

import com.spipme.app.core.sync.BackoffPolicy
import com.spipme.app.core.sync.SyncEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.random.Random

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {
    @Provides
    @Singleton
    fun fournirMoteur(
        file: RoomFileOperations,
        passerelle: PasserelleOkHttp,
        ids: RoomCorrespondances,
    ): SyncEngine = SyncEngine(
        file = file,
        passerelle = passerelle,
        ids = ids,
        // Gigue jusqu'à +20 % : évite que tous les appareils réessaient au même instant après une panne.
        backoff = BackoffPolicy(gigueMs = { plafonne -> Random.nextLong(plafonne / 5 + 1) }),
        maintenantMs = System::currentTimeMillis,
    )
}
