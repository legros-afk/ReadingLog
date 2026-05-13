package com.flo.readinglog.ui.screens.entry

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryScreen(
    entryId: Long,
    onNavigateUp: () -> Unit,
    viewModel: EditEntryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMM yyyy") }

    LaunchedEffect(entryId) { viewModel.load(entryId) }
    LaunchedEffect(uiState.savedSuccessfully) { if (uiState.savedSuccessfully) onNavigateUp() }
    LaunchedEffect(uiState.deletedSuccessfully) { if (uiState.deletedSuccessfully) onNavigateUp() }

    uiState.errorMessage?.let { msg ->
        LaunchedEffect(msg) {
            snackbarHostState.showSnackbar(msg)
            viewModel.onErrorShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Entry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onShowDeleteConfirm(true) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    uiState.entry?.book?.let { book ->
                        Text("Book: ${book.title}", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = uiState.pageFrom,
                            onValueChange = viewModel::onPageFromChange,
                            modifier = Modifier.weight(1f),
                            label = { Text("Page from") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = uiState.pageTo,
                            onValueChange = viewModel::onPageToChange,
                            modifier = Modifier.weight(1f),
                            label = { Text("Page to") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = uiState.date.format(dateFormatter),
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.onShowDatePicker(true) },
                        label = { Text("Date") },
                        readOnly = true,
                        enabled = false,
                        trailingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                    )
                }
                item {
                    OutlinedTextField(
                        value = uiState.impressions,
                        onValueChange = viewModel::onImpressionsChange,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                        label = { Text("Impressions (optional)") },
                        maxLines = 8,
                    )
                }
                item {
                    Button(
                        onClick = viewModel::save,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving,
                    ) {
                        if (uiState.isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        else Text("Save Changes")
                    }
                }
            }
        }
    }

    if (uiState.showDatePicker) {
        EntryDatePickerDialog(
            initialDate = uiState.date,
            onDateSelected = viewModel::onDateChange,
            onDismiss = { viewModel.onShowDatePicker(false) }
        )
    }

    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onShowDeleteConfirm(false) },
            title = { Text("Delete entry?") },
            text = { Text("This cannot be undone.") },
            confirmButton = { TextButton(onClick = viewModel::delete) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { viewModel.onShowDeleteConfirm(false) }) { Text("Cancel") } }
        )
    }
}
