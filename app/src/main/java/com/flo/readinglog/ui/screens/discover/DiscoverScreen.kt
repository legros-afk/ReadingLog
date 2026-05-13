package com.flo.readinglog.ui.screens.discover

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun DiscoverScreen() {
    Scaffold(topBar = { TopAppBar(title = { Text("Discover") }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Book recommendations will appear here", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
