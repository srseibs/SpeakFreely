package com.sailinghawklabs.speakfreely.domain.model

import java.util.Locale

data class Language(
    val bcp47Code: String,      // e.g. "es-MX"
    val displayName: String,    // e.g. "Spanish"
    val localizedName: String,  // e.g. "Español (México)"
    val flagEmoji: String = ""  // e.g. "🇲🇽"
) {
    companion object {
        val DEFAULT_OWNER_LANGUAGE = fromBcp47Code("en-US")
            ?: error("Invalid default BCP-47 language tag: en-US")

        val DEFAULT_TARGET_LANGUAGE = fromBcp47Code("es-MX")
            ?: error("Invalid default BCP-47 language tag: es-MX")

        fun fromBcp47Code(bcp47Code: String?): Language? {
            if (bcp47Code.isNullOrBlank()) return null
            val locale = Locale.forLanguageTag(bcp47Code)

            val displayName =
                locale.displayLanguage.replaceFirstChar { it.uppercase() }.ifBlank { bcp47Code }
            val localizedName = locale.getDisplayName(locale).replaceFirstChar { it.uppercase() }
                .ifBlank { displayName }
            val flagEmoji = countryCodeToFlagEmoji(locale.country)

            return Language(
                bcp47Code = bcp47Code,
                displayName = displayName,
                localizedName = localizedName,
                flagEmoji = flagEmoji
            )
        }

        private fun countryCodeToFlagEmoji(countryCode: String): String {
            if (countryCode.length != 2) return ""
            val first = Character.codePointAt(countryCode.uppercase(), 0) - 0x41 + 0x1F1E6
            val second = Character.codePointAt(countryCode.uppercase(), 1) - 0x41 + 0x1F1E6
            return String(Character.toChars(first)) + String(Character.toChars(second))
        }
    }
}