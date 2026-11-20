package com.sailinghawklabs.speakfreely.di

import com.sailinghawklabs.speakfreely.presentation.screen.splash.AppSplashViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelDiModule = module {
    viewModel {
        AppSplashViewModel(
            repository = get(),
            ttsRepository = get(),
            sttRepository = get()
        )
    }
}
