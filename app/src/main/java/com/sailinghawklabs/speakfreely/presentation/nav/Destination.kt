package com.sailinghawklabs.speakfreely.presentation.nav

import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {

    @Serializable
    data object Splash : Destination


    @Serializable
    data object Main : Destination

    @Serializable
    data object Settings : Destination
}



