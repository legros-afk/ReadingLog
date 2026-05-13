package com.flo.readinglog.ui.screens.discover

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(viewModel: DiscoverViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.errorMessage?.let { msg ->
        LaunchedEffect(msg) { snackbarHostState.showSnackbar(msg); viewModel.onErrorShown() }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Discover") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(
                    onClick = viewModel::getRecommendations,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (uiState.isLoading) "Getting recommendations…" else "Get recommendations")
                }
            }

            if (uiState.noFavouritesWarning) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("No favourites yet", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Star some books in your library first so Claude can tailor recommendations for you.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(onClick = viewModel::dismissNoFavouritesWarning) { Text("Got it") }
                        }
                    }
                }
            }

            if (uiState.recommendations.isEmpty() && !uiState.isLoading && !uiState.noFavouritesWarning) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                        Text("Tap the button above to get book recommendations", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            items(uiState.recommendations) { rec ->
                RecommendationCard(
                    rec = rec,
                    isAdding = uiState.addingBookTitle == rec.title,
                    onAddToList = { viewModel.addToMyList(rec) }
                )
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    rec: com.flo.readinglog.data.remote.model.RecommendedBook,
    isAdding: Boolean,
    onAddToList: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(rec.title, style = MaterialTheme.typography.titleSmall)
            Text(rec.author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text(rec.whyRecommended, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onAddToList,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAdding,
            ) {
                if (isAdding) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (isAdding) "Adding…" else "Add to my list")
            }
        }
    }
}
