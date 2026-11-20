package com.sailinghawklabs.speakfreely.di

import com.sailinghawklabs.speakfreely.data.repository.SttRepositoryImpl
import com.sailinghawklabs.speakfreely.data.repository.TtsRepositoryImpl
import com.sailinghawklabs.speakfreely.data.repository.UserPreferencesRepositoryImpl
import com.sailinghawklabs.speakfreely.domain.repository.SttRepository
import com.sailinghawklabs.speakfreely.domain.repository.TtsRepository
import com.sailinghawklabs.speakfreely.domain.repository.UserPreferencesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryDiModule = module {
    single<UserPreferencesRepository> {
        UserPreferencesRepositoryImpl(
            dataStore = get()
        )
    }

    single<TtsRepository> {
        TtsRepositoryImpl(
            context = androidContext()
        )
    }

    single<SttRepository> {
        SttRepositoryImpl(context = androidContext())
    }
}
