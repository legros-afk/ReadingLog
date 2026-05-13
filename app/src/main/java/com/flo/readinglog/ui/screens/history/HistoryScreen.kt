package com.flo.readinglog.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flo.readinglog.domain.model.ReadingEntry
import com.flo.readinglog.ui.screens.today.ReadingEntryCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onEditEntry: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy") }

    Scaffold(topBar = { TopAppBar(title = { Text("History") }) }) { padding ->
        if (uiState.grouped.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No reading entries yet", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.grouped.forEach { (date, entries) ->
                    item(key = date.toEpochDay()) {
                        DateHeader(date = date, formatter = dateFormatter, totalPages = entries.sumOf { it.pagesRead })
                    }
                    items(entries, key = { it.id }) { entry ->
                        ReadingEntryCard(entry = entry, onEdit = { onEditEntry(entry.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DateHeader(date: LocalDate, formatter: DateTimeFormatter, totalPages: Int) {
    Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
        Text(date.format(formatter), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Text("$totalPages pages", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider(Modifier.padding(top = 4.dp))
    }
}
