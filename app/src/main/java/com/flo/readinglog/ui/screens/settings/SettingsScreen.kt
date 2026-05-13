package com.flo.readinglog.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flo.readinglog.ui.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Settings coming soon", style = MaterialTheme.typography.bodyLarge)
                OutlinedButton(onClick = { /* sign out handled in step 7 */ }) {
                    Text("Sign out (coming in step 7)")
                }
            }
        }
    }
}
