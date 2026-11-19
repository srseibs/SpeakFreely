package com.sailinghawklabs.speakfreely.di

import com.sailinghawklabs.speakfreely.data.repository.UserPreferencesRepositoryImpl
import com.sailinghawklabs.speakfreely.domain.repository.UserPreferencesRepository
import org.koin.dsl.module

val repositoryDiModule = module {
    single<UserPreferencesRepository> {
        UserPreferencesRepositoryImpl(
            dataStore = get()
        )
    }
}
