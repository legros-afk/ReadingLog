package com.flo.readinglog.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.flo.readinglog.ui.screens.today.ReadingEntryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: Long,
    onNavigateUp: () -> Unit,
    onEditEntry: (Long) -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(bookId) { viewModel.load(bookId) }

    uiState.errorMessage?.let { msg ->
        LaunchedEffect(msg) { snackbarHostState.showSnackbar(msg); viewModel.onErrorShown() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.book?.title ?: "Book Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleFavourite) {
                        Icon(
                            if (uiState.book?.isFavourite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favourite",
                            tint = if (uiState.book?.isFavourite == true) MaterialTheme.colorScheme.tertiary else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = viewModel::toggleWantToRead) {
                        Icon(
                            if (uiState.book?.isWantToRead == true) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Want to read",
                            tint = if (uiState.book?.isWantToRead == true) MaterialTheme.colorScheme.secondary else LocalContentColor.current
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val book = uiState.book
            if (book == null) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Book not found")
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cover + metadata header
                    item {
                        Row(verticalAlignment = Alignment.Top) {
                            book.coverUrl?.let { url ->
                                AsyncImage(
                                    model = url, contentDescription = null,
                                    modifier = Modifier.size(96.dp, 136.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(16.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(book.title, style = MaterialTheme.typography.titleMedium)
                                Text(book.authors.joinToString(", "), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                book.publishedDate?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                book.pageCount?.let { Text("$it pages", style = MaterialTheme.typography.bodySmall) }
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (book.isFavourite) AssistChip(onClick = {}, label = { Text("Favourite") })
                                    if (book.isWantToRead) AssistChip(onClick = {}, label = { Text("Want to read") })
                                }
                            }
                        }
                    }
                    // Description
                    book.description?.let { desc ->
                        item {
                            Text("About", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            Text(desc, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    // Stats
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${uiState.totalPagesRead}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                                    Text("pages read", style = MaterialTheme.typography.bodySmall)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${uiState.entries.size}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                                    Text("sessions", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    // Reading entries
                    if (uiState.entries.isNotEmpty()) {
                        item { Text("Reading sessions", style = MaterialTheme.typography.titleSmall) }
                        items(uiState.entries, key = { it.id }) { entry ->
                            ReadingEntryCard(entry = entry, onEdit = { onEditEntry(entry.id) })
                        }
                    }
                }
            }
        }
    }
}
