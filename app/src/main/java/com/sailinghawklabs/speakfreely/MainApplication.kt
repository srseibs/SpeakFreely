package com.sailinghawklabs.speakfreely

import android.app.Application
import com.sailinghawklabs.speakfreely.di.appDiModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(appDiModule)
        }
    }
}
