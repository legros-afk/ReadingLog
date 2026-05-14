package com.flo.readinglog.ui.screens.entry

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.flo.readinglog.domain.model.Book
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    initialBookId: Long?,
    onNavigateUp: () -> Unit,
    viewModel: AddEntryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(initialBookId) {
        if (initialBookId != null) viewModel.loadBook(initialBookId)
    }

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) onNavigateUp()
    }

    uiState.errorMessage?.let { msg ->
        LaunchedEffect(msg) {
            snackbarHostState.showSnackbar(msg)
            viewModel.onErrorShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚔️  The Encounter") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            // Flavour header when no book selected yet
            if (uiState.selectedBook == null && uiState.bookSearchQuery.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("🎲 A new adventure awaits!", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Choose your tome and record your progress, brave adventurer.",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }
            }

            // Tome picker
            item {
                Text("Choose your Tome", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                uiState.selectedBook?.let { book ->
                    SelectedBookCard(book = book, onClear = { viewModel.onBookSearchQueryChange("") })
                } ?: run {
                    OutlinedTextField(
                        value = uiState.bookSearchQuery,
                        onValueChange = viewModel::onBookSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search for a tome...") },
                        singleLine = true,
                        trailingIcon = if (uiState.isSearching) {
                            { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) }
                        } else null,
                    )
                }
            }

            // Search results
            if (uiState.bookSearchResults.isNotEmpty()) {
                items(uiState.bookSearchResults) { book ->
                    BookSearchResultItem(book = book, onClick = { viewModel.onBookSelected(book) })
                    HorizontalDivider()
                }
            }

            // Page range
            item {
                Text("Pages Conquered", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.pageFrom,
                        onValueChange = viewModel::onPageFromChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("Starting page") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.pageTo,
                        onValueChange = viewModel::onPageToChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("Last page reached") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
                // Live page count
                val from = uiState.pageFrom.toIntOrNull()
                val to = uiState.pageTo.toIntOrNull()
                if (from != null && to != null && to >= from) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "⚔️  ${to - from + 1} pages conquered!",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }

            // Date
            item {
                Text("Date of Battle", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = uiState.date.format(dateFormatter),
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.onShowDatePicker(true) },
                    label = { Text("When did this encounter happen?") },
                    readOnly = true,
                    enabled = false,
                    trailingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                )
            }

            // Battle notes
            item {
                Text("Battle Notes", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = uiState.impressions,
                    onValueChange = viewModel::onImpressionsChange,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    label = { Text("What happened in this encounter? (optional)") },
                    maxLines = 8,
                )
            }

            // Save button
            item {
                Button(
                    onClick = viewModel::save,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving,
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("✅  Complete Encounter!")
                    }
                }
            }
        }
    }

    if (uiState.showDatePicker) {
        EntryDatePickerDialog(
            initialDate = uiState.date,
            onDateSelected = viewModel::onDateChange,
            onDismiss = { viewModel.onShowDatePicker(false) },
        )
    }
}

@Composable
private fun SelectedBookCard(book: Book, onClear: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            book.coverUrl?.let {
                AsyncImage(
                    model = it, contentDescription = null,
                    modifier = Modifier.size(56.dp, 80.dp),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("📖 Chosen Tome", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(2.dp))
                Text(book.title, style = MaterialTheme.typography.titleSmall)
                Text(book.authors.joinToString(", "), style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onClear) { Text("Change") }
        }
    }
}

@Composable
private fun BookSearchResultItem(book: Book, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        book.coverUrl?.let {
            AsyncImage(
                model = it, contentDescription = null,
                modifier = Modifier.size(40.dp, 56.dp),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(book.title, style = MaterialTheme.typography.bodyMedium)
            Text(
                book.authors.joinToString(", "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochDay() * 86_400_000L,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { millis ->
                    onDateSelected(LocalDate.ofEpochDay(millis / 86_400_000L))
                }
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DatePicker(state = state)
    }
}
