package com.sailinghawklabs.speakfreely.presentation.nav

import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {

    @Serializable
    data object Startup : Destination


    @Serializable
    data object ListScreen : Destination

    @Serializable
    data class DetailsScreen(val id: Long) : Destination
}



