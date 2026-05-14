package com.flo.readinglog.ui.screens.digests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flo.readinglog.domain.model.Digest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigestsScreen(
    viewModel: DigestsViewModel = hiltViewModel(),
) {
    val digests by viewModel.digests.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Digests") }) },
    ) { padding ->
        if (digests.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("📬", fontSize = 64.sp)
                    Text("No digests yet", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Your weekly reading summary arrives every Friday at 18:00",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(digests, key = { it.id }) { digest ->
                    DigestCard(digest)
                }
            }
        }
    }
}

@Composable
private fun DigestCard(digest: Digest) {
    val weekFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
    val sentFormatter = DateTimeFormatter.ofPattern("d MMM HH:mm")
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Week of ${digest.weekStart.format(weekFormatter)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    Instant.ofEpochMilli(digest.sentAt).atZone(ZoneId.systemDefault()).format(sentFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                digest.message,
                style = MaterialTheme.typography.bodySmall,
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                overflow = if (expanded) TextOverflow.Visible else TextOverflow.Ellipsis,
            )
            if (!expanded) {
                TextButton(
                    onClick = { expanded = true },
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text("Read more", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
