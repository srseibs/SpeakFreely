package com.sailinghawklabs.speakfreely.di

import org.koin.dsl.module

val appDiModule = module {
    includes(
        dataStoreDiModule,
        repositoryDiModule,
        viewModelDiModule
    )
}
