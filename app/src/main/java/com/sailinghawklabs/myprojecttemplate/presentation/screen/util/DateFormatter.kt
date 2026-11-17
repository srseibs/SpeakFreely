package com.sailinghawklabs.myprojecttemplate.presentation.screen.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Formats a LocalDate into a user-friendly string based on the device's locale.
 * Example: Aug 14, 2026
 */
fun LocalDate?.toFormattedDate(
    style: FormatStyle = FormatStyle.MEDIUM,
    placeholder: String = "TBA"
): String {
    return this?.format(DateTimeFormatter.ofLocalizedDate(style)) ?: placeholder
}

/**
 * Calculates the number of years between this date and [until].
 * Useful for displaying a person's age.
 * Example: "45"
 */
fun LocalDate?.toAgeInYears(until: LocalDate = LocalDate.now()): String {
    val startDate = this ?: return ""
    return Period.between(startDate, until).years.toString()
}

/**
 * Calculates the number of years between this death date and the [birthDate].
 * Example: "82"
 */
fun LocalDate?.ageAtDeathInYears(birthDate: LocalDate?): String {
    val death = this ?: return ""
    val birth = birthDate ?: return ""
    return Period.between(birth, death).years.toString()
}

/**
 * Formats a LocalDateTime into a user-friendly string based on the device's locale.
 * Example: Aug 14, 2026, 5:27 PM
 */
fun LocalDateTime?.toFormattedDateTime(
    dateStyle: FormatStyle = FormatStyle.MEDIUM,
    timeStyle: FormatStyle = FormatStyle.SHORT,
    placeholder: String = "TBA"
): String {
    return this?.format(DateTimeFormatter.ofLocalizedDateTime(dateStyle, timeStyle)) ?: placeholder
}

/**
 * Extracts just the year from a LocalDate. Useful for movie headings.
 * Example: 2026
 */
fun LocalDate?.toYearString(): String {
    return this?.year?.toString() ?: "n/a"
}
