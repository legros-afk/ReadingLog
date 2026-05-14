package com.flo.readinglog.ui.screens.discover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.flo.readinglog.data.remote.model.RecommendedBook

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(viewModel: DiscoverViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedRec by remember { mutableStateOf<RecommendedBook?>(null) }

    uiState.errorMessage?.let { msg ->
        LaunchedEffect(msg) { snackbarHostState.showSnackbar(msg); viewModel.onErrorShown() }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Discover") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    Text(if (uiState.isLoading) "Finding books for you…" else "✨  Get Recommendations")
                }
            }

            if (uiState.noFavouritesWarning) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("No favourites yet ⭐", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Mark some books as favourites first so Claude can pick books you'll love!",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            TextButton(onClick = viewModel::dismissNoFavouritesWarning) { Text("Got it") }
                        }
                    }
                }
            }

            if (uiState.recommendations.isEmpty() && !uiState.isLoading && !uiState.noFavouritesWarning) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("🔭", fontSize = 64.sp)
                            Text(
                                "Tap the button to find your next great read!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            items(uiState.recommendations) { rec ->
                RecommendationCard(
                    rec = rec,
                    isAdding = uiState.addingBookTitle == rec.title,
                    onClick = { selectedRec = rec },
                    onAddToList = { viewModel.addToMyList(rec) },
                )
            }
        }
    }

    selectedRec?.let { rec ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selectedRec = null },
            sheetState = sheetState,
        ) {
            RecommendationDetailSheet(
                rec = rec,
                isAdding = uiState.addingBookTitle == rec.title,
                onAddToList = { viewModel.addToMyList(rec); selectedRec = null },
            )
        }
    }
}

@Composable
private fun RecommendationCard(
    rec: RecommendedBook,
    isAdding: Boolean,
    onClick: () -> Unit,
    onAddToList: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            if (rec.coverUrl != null) {
                AsyncImage(
                    model = rec.coverUrl,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp, 80.dp),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(12.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp, 80.dp)
                        .padding(end = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("📖", fontSize = 28.sp)
                }
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(rec.title, style = MaterialTheme.typography.titleSmall)
                Text(rec.author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (rec.year != null) {
                    Text(rec.year, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (rec.genres.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        rec.genres.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(rec.whyRecommended, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun RecommendationDetailSheet(
    rec: RecommendedBook,
    isAdding: Boolean,
    onAddToList: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (rec.coverUrl != null) {
            AsyncImage(
                model = rec.coverUrl,
                contentDescription = null,
                modifier = Modifier.size(120.dp, 172.dp),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                Modifier.size(120.dp, 172.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("📖", fontSize = 64.sp)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(rec.title, style = MaterialTheme.typography.titleLarge)
        Text(rec.author, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))

        if (rec.genres.isNotEmpty() || rec.year != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                rec.year?.let {
                    SuggestionChip(onClick = {}, label = { Text(it) })
                }
                rec.genres.forEach { genre ->
                    SuggestionChip(
                        onClick = {},
                        label = { Text(genre) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        if (rec.synopsis != null) {
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            Text(
                rec.synopsis,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
            )
            Spacer(Modifier.height(12.dp))
        }

        HorizontalDivider()
        Spacer(Modifier.height(8.dp))
        Text(
            "Why you'll love it",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
        Text(rec.whyRecommended, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onAddToList,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isAdding,
        ) {
            if (isAdding) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(8.dp))
            }
            Text(if (isAdding) "Adding…" else "Add to my list")
        }
    }
}
