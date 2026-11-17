package com.sailinghawklabs.speakfreely.presentation.screen.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Formats a Double rating to exactly one decimal place.
 * Example: 8.432 -> "8.4"
 */
fun Double?.toFormattedRating(): String {
    val value = this ?: return "N/A"
    return if (value == 0.0) "N/A" else String.format(Locale.getDefault(), "%.1f", value)
}

/**
 * Formats a Double rating to a rounded Int.
 * Example: 8.432 -> "8"
 */
fun Double?.toRoundedInt(): String {
    val value = this ?: return "N/A"
    return if (value == 0.0) "N/A" else value.roundToInt().toString()
}

/**
 * Formats large numbers with commas for better readability.
 * Example: 1250000 -> "1,250,000"
 */
fun Int?.toFormattedCount(): String {
    return String.format(Locale.getDefault(), "%,d", this ?: 0).trim()
}

/**
 * Formats an integer number as USD currency.
 * Example: 1250000 -> "$1,250,000"
 */
fun Long?.toFormattedUsd(): String {
    val value = this ?: return "N/A"
    return "$" + String.format(Locale.getDefault(), "%,d", value)
}

/**
 * Formats a number as USD currency, with cents.
 * Example: 1250000.1223 -> "$1,250,000.12"
 */
fun Double?.toFormattedUsd(): String {
    val value = this ?: return "N/A"

    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(value) // "$1,234.56"

}

/**
 * Formats minutes into a readable "Xh Ym" string.
 * Example: 125 -> "2h 5m", 45 -> "45m"
 */
fun Int?.toFormattedHoursMinutes(): String {
    val totalMinutes = this ?: return "N/A"
    if (totalMinutes <= 0) return "N/A"

    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return buildString {
        if (hours > 0) append("${hours}h ")
        if (minutes > 0) append("${minutes}m")
    }.trim()
}
