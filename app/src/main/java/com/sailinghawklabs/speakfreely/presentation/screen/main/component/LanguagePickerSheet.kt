package com.sailinghawklabs.speakfreely.presentation.screen.main.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sailinghawklabs.speakfreely.domain.model.Language
import com.sailinghawklabs.speakfreely.domain.model.SupportedLanguages
import com.sailinghawklabs.speakfreely.presentation.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePickerSheet(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Select Language",
    showAutoDetectOption: Boolean = false,
    isAutoDetectEnabled: Boolean = false,
    onAutoDetectToggled: ((Boolean) -> Unit)? = null,
    sheetState: SheetState = rememberModalBottomSheetState()
) {


    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        LanguagePickerSheetContent(
            title = title,
            selectedLanguage = selectedLanguage,
            onLanguageSelected = onLanguageSelected,
            onDismissRequest = onDismissRequest,
            showAutoDetectOption = showAutoDetectOption,
            isAutoDetectEnabled = isAutoDetectEnabled,
            onAutoDetectToggled = onAutoDetectToggled
        )
    }
}

@Composable
fun LanguagePickerSheetContent(
    title: String,
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    showAutoDetectOption: Boolean = false,
    isAutoDetectEnabled: Boolean = false,
    onAutoDetectToggled: ((Boolean) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredLanguages = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            SupportedLanguages.ALL
        } else {
            SupportedLanguages.ALL.filter { lang ->
                lang.displayName.contains(searchQuery, ignoreCase = true) ||
                lang.localizedName.contains(searchQuery, ignoreCase = true) ||
                lang.bcp47Code.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        if (showAutoDetectOption && onAutoDetectToggled != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ Auto-Detect Language",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = isAutoDetectEnabled,
                        onCheckedChange = onAutoDetectToggled
                    )
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search languages...") },
            singleLine = true
        )

        Text(
            text = "Popular Languages",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SupportedLanguages.POPULAR.take(6).forEach { language ->
                FilterChip(
                    selected = language.bcp47Code == selectedLanguage.bcp47Code,
                    onClick = {
                        onLanguageSelected(language)
                        onDismissRequest()
                    },
                    label = {
                        Text(
                            text = "${language.displayName} ${language.flagEmoji}"
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,        // Dark Mahogany
                        labelColor = MaterialTheme.colorScheme.onSurface,                  // High-Contrast Cream
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, // Copper Container
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer  // Bright Copper Text
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredLanguages) { language ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onLanguageSelected(language)
                            onDismissRequest()
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: 2-Line Language & Country Name
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = language.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (language.bcp47Code == selectedLanguage.bcp47Code)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = language.localizedName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right: Clean Flag Emoji
                    Text(
                        text = language.flagEmoji,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    }
}


@Preview
@Composable
private fun LanguagePickerSheetPrev() {
    AppTheme {

        var isAutoDetectEnabled by remember { mutableStateOf(false) }
        var selectedLanguage by remember { mutableStateOf(Language.DEFAULT_TARGET_LANGUAGE) }

        Surface {
            LanguagePickerSheetContent(
                title = "Select Language",
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { selectedLanguage = it },
                onDismissRequest = {},
                showAutoDetectOption = true,
                isAutoDetectEnabled = isAutoDetectEnabled,
                onAutoDetectToggled = { isAutoDetectEnabled = !isAutoDetectEnabled }
            )
        }
    }
}
