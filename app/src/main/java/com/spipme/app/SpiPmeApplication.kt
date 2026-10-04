package com.spipme.app

import android.app.Application
import com.spipme.app.core.sync.android.SynchronisationRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SpiPmeApplication : Application() {
    @Inject lateinit var synchronisation: SynchronisationRepository

    override fun onCreate() {
        super.onCreate()
        synchronisation.demarrer()  // reprise de la file persistante + déclencheurs réseau/session
    }
}
