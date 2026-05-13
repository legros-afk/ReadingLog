package com.flo.readinglog.ui.screens.digests

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun DigestsScreen() {
    Scaffold(topBar = { TopAppBar(title = { Text("Digests") }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Weekly digests will appear here", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
