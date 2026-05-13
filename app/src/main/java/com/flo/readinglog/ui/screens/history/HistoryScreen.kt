package com.flo.readinglog.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun HistoryScreen(onEditEntry: (Long) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("History") }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Reading history will appear here", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
