package com.sailinghawklabs.speakfreely.domain.model

enum class ConversationMode {
    TAP_TO_RECORD,
    CONTINUOUS;

    companion object {
        val DEFAULT = TAP_TO_RECORD
    }
}