package com.flo.readinglog.ui.screens.books

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun BooksScreen(onBookClick: (Long) -> Unit, onAddBook: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Books") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBook) {
                Icon(Icons.Default.Add, contentDescription = "Add book")
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Book catalogue will appear here", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
