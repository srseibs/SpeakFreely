package com.sailinghawklabs.speakfreely.presentation.screen.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Encapsulates UI-bound text content to safely pass string resources and dynamic text from
 * ViewModels or State without requiring an Android [Context] reference in the business logic.
 *
 * Provides two variants:
 * - [DynamicString]: Wraps raw, runtime-generated strings (e.g., transcripts, AI translations).
 * - [StringResource]: Wraps localized Android string resource IDs (`R.string.*`) with optional format arguments.
 *
 * Use [asString] within Composables or pass a [Context] to resolve the evaluated string.
 */
sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    class StringResource(
        @param:StringRes val resId: Int,
        vararg val args: Any,
    ) : UiText

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }
}
