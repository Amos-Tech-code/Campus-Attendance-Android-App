package com.amos_tech_code.smartattend.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amos_tech_code.smartattend.domain.response.UniversitySuggestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SuggestionDropdown(
    suggestions: List<T>,
    onSuggestionSelected: (T) -> Unit,
    suggestionContent: @Composable (T) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            suggestions.forEach { suggestion ->
                SuggestionItem(
                    suggestion = suggestion,
                    onSuggestionSelected = onSuggestionSelected,
                    suggestionContent = suggestionContent
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SuggestionItem(
    suggestion: T,
    onSuggestionSelected: (T) -> Unit,
    suggestionContent: @Composable (T) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSuggestionSelected(suggestion) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RectangleShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Leading icon (optional - can be customized)
            Icon(
                imageVector = Icons.Default.CheckCircleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                suggestionContent(suggestion)
            }

            // Optional trailing indicator
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Select",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Enhanced version with search highlighting
@Composable
fun <T> HighlightedSuggestionDropdown(
    suggestions: List<T>,
    query: String,
    getSuggestionText: (T) -> String,
    onSuggestionSelected: (T) -> Unit,
    suggestionContent: @Composable (T, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            suggestions.forEach { suggestion ->
                HighlightedSuggestionItem(
                    suggestion = suggestion,
                    query = query,
                    getSuggestionText = getSuggestionText,
                    onSuggestionSelected = onSuggestionSelected,
                    suggestionContent = suggestionContent
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> HighlightedSuggestionItem(
    suggestion: T,
    query: String,
    getSuggestionText: (T) -> String,
    onSuggestionSelected: (T) -> Unit,
    suggestionContent: @Composable (T, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSuggestionSelected(suggestion) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RectangleShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                suggestionContent(suggestion, query)
            }
        }
    }
}

// Helper for highlighting text
@Composable
fun HighlightedText(
    text: String,
    query: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    highlightStyle: TextStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
) {
    val regex = Regex("(${Regex.escape(query)})", RegexOption.IGNORE_CASE)
    val parts = regex.split(text)
    val matches = regex.findAll(text).toList()

    if (query.isBlank() || matches.isEmpty()) {
        Text(text = text, style = style, modifier = modifier)
    } else {
        Text(
            modifier = modifier,
            text = buildAnnotatedString {
                var currentIndex = 0
                parts.forEachIndexed { index, part ->
                    if (part.isNotEmpty()) {
                        pushStyle(style.toSpanStyle())
                        append(part)
                        pop()
                    }

                    if (index < matches.size) {
                        pushStyle(highlightStyle.toSpanStyle())
                        append(matches[index].value)
                        pop()
                    }
                }
            }
        )
    }
}

// Usage example for your specific case:
@Composable
fun UniversitySuggestionDropdown(
    suggestions: List<UniversitySuggestion>,
    query: String,
    onSuggestionSelected: (UniversitySuggestion) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    HighlightedSuggestionDropdown(
        suggestions = suggestions,
        query = query,
        getSuggestionText = { it.name },
        onSuggestionSelected = onSuggestionSelected,
        suggestionContent = { suggestion, highlightQuery ->
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                HighlightedText(
                    text = suggestion.name,
                    query = highlightQuery,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = when (suggestion.matchType) {
                        "exact" -> "Exact match"
                        "prefix" -> "Starts with"
                        "partial" -> "Contains"
                        "similar" -> "Similar"
                        else -> "Match"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
        },
        onDismiss = onDismiss,
        modifier = modifier
    )
}
