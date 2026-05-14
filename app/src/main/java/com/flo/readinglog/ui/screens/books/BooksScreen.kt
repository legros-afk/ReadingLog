package com.flo.readinglog.ui.screens.books

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.flo.readinglog.domain.model.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(
    onBookClick: (Long) -> Unit,
    viewModel: BooksViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.errorMessage?.let { msg ->
        LaunchedEffect(msg) { snackbarHostState.showSnackbar(msg); viewModel.onErrorShown() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Books") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onShowAddBookDialog(true) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Book") },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = uiState.filter.ordinal,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                BookFilter.values().forEach { filter ->
                    Tab(
                        selected = uiState.filter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        text = { Text(filter.label, style = MaterialTheme.typography.labelLarge) },
                    )
                }
            }
            if (uiState.books.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(uiState.filter.emptyEmoji, fontSize = 64.sp)
                        Text(uiState.filter.emptyMessage, style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.books, key = { it.id }) { book ->
                        BookListItem(book = book, onClick = { onBookClick(book.id) })
                    }
                }
            }
        }
    }

    if (uiState.showAddBookDialog) {
        AddBookDialog(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            searchResults = uiState.searchResults,
            isSearching = uiState.isSearching,
            isAdding = uiState.isAddingBook,
            onAddBook = viewModel::addBookToCatalogue,
            onDismiss = { viewModel.onShowAddBookDialog(false) },
        )
    }
}

private val BookFilter.label get() = when (this) {
    BookFilter.ALL -> "All"
    BookFilter.FAVOURITES -> "Favourites"
    BookFilter.WANT_TO_READ -> "Want to Read"
}
private val BookFilter.emptyEmoji get() = when (this) {
    BookFilter.ALL -> "📖"
    BookFilter.FAVOURITES -> "⭐"
    BookFilter.WANT_TO_READ -> "🔖"
}
private val BookFilter.emptyMessage get() = when (this) {
    BookFilter.ALL -> "No books yet — tap Add Book!"
    BookFilter.FAVOURITES -> "No favourites yet"
    BookFilter.WANT_TO_READ -> "Nothing on your wishlist yet"
}

@Composable
private fun BookListItem(book: Book, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            book.coverUrl?.let { url ->
                AsyncImage(
                    model = url, contentDescription = null,
                    modifier = Modifier.size(48.dp, 68.dp),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(book.title, style = MaterialTheme.typography.titleSmall)
                Text(
                    book.authors.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (book.isFavourite) Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                if (book.isWantToRead) Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBookDialog(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<Book>,
    isSearching: Boolean,
    isAdding: Boolean,
    onAddBook: (Book) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a book") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("Search title or author") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = if (isSearching) {
                        { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) }
                    } else null,
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(searchResults) { book ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onAddBook(book) }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            book.coverUrl?.let { url ->
                                AsyncImage(model = url, contentDescription = null, modifier = Modifier.size(36.dp, 52.dp), contentScale = ContentScale.Crop)
                                Spacer(Modifier.width(10.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(book.title, style = MaterialTheme.typography.bodyMedium)
                                Text(book.authors.joinToString(", "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (isAdding) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
