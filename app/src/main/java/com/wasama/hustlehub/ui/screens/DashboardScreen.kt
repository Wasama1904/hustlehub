package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(nav: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("HustleHub Dashboard") }, actions = { IconButton(onClick = { nav.navigate("settings") }) { Icon(Icons.Default.Settings, null) } }) },
        floatingActionButton = { FloatingActionButton(onClick = { nav.navigate("timer/new") }) { Text("+ Timer") } }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Level 3 - Newbie Hustler", style = MaterialTheme.typography.titleMedium)
                        LinearProgressIndicator(progress = { 0.5f }, modifier = Modifier.fillMaxWidth().padding(top=8.dp))
                        Text("450 / 900 XP | Streak 5 days 🔥 Freeze:1", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(Modifier.weight(1f)) { Column(Modifier.padding(12.dp)) { Text("Unpaid"); Text("R5400", style=MaterialTheme.typography.headlineSmall) } }
                    Card(Modifier.weight(1f)) { Column(Modifier.padding(12.dp)) { Text("Overdue"); Text("2", style=MaterialTheme.typography.headlineSmall) } }
                    Card(Modifier.weight(1f)) { Column(Modifier.padding(12.dp)) { Text("Active"); Text("3", style=MaterialTheme.typography.headlineSmall) } }
                }
            }
            item { Text("Recent Time Entries") }
            item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Text("Logo Redesign - 1h 23m - R350"); Text("Website Build - 2h - R500") } } }
        }
    }
}
