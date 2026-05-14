package com.flo.readinglog.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.flo.readinglog.domain.usecase.SynopsisUseCase
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
                title = { Text("📜 Quest Brief") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleFavourite) {
                        Icon(
                            if (uiState.book?.isFavourite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Legendary",
                            tint = if (uiState.book?.isFavourite == true) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    IconButton(onClick = viewModel::toggleWantToRead) {
                        Icon(
                            if (uiState.book?.isWantToRead == true) Icons.Default.Flag else Icons.Default.OutlinedFlag,
                            contentDescription = "Accept Quest",
                            tint = if (uiState.book?.isWantToRead == true) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val book = uiState.book
            if (book == null) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Quest not found")
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Cover + metadata
                    item {
                        Row(verticalAlignment = Alignment.Top) {
                            book.coverUrl?.let { url ->
                                AsyncImage(
                                    model = url, contentDescription = null,
                                    modifier = Modifier.size(96.dp, 136.dp),
                                    contentScale = ContentScale.Crop,
                                )
                                Spacer(Modifier.width(16.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(book.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    book.authors.joinToString(", "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                book.publishedDate?.let {
                                    Text("📅 $it", style = MaterialTheme.typography.bodySmall)
                                }
                                book.pageCount?.let {
                                    Text("📄 $it pages", style = MaterialTheme.typography.bodySmall)
                                }
                                if (book.categories.isNotEmpty()) {
                                    Text(
                                        "🏷️ ${book.categories.take(2).joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (book.isFavourite) {
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("⭐ Legendary") },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                            ),
                                        )
                                    }
                                    if (book.isWantToRead) {
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("🗺️ Quest Accepted") },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Synopsis — sanitized quest brief
                    book.description?.let { rawDesc ->
                        val synopsis = SynopsisUseCase.sanitize(rawDesc)
                        if (synopsis.isNotBlank()) {
                            item {
                                Text(
                                    "⚔️  The Adventure",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.height(6.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(
                                        synopsis,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontStyle = FontStyle.Italic,
                                        modifier = Modifier.padding(12.dp),
                                    )
                                }
                            }
                        }
                    }

                    // Combat record
                    item {
                        Text(
                            "🛡️  Combat Record",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "${uiState.totalPagesRead}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Text("pages conquered", style = MaterialTheme.typography.bodySmall)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "${uiState.entries.size}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                    Text("encounters", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    // Past encounters
                    if (uiState.entries.isNotEmpty()) {
                        item {
                            Text(
                                "📖  Past Encounters",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        items(uiState.entries, key = { it.id }) { entry ->
                            ReadingEntryCard(entry = entry, onEdit = { onEditEntry(entry.id) })
                        }
                    }
                }
            }
        }
    }
}
