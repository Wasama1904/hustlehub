package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen() {
    // Placeholder UI matching mockup
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Level 3 - Newbie Hustler", style = MaterialTheme.typography.titleMedium)
                    LinearProgressIndicator(progress = { 0.5f }, modifier = Modifier.fillMaxWidth().padding(top=8.dp))
                    Text("450 / 900 XP | Streak 5 days \uD83D\uDD25", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(Modifier.weight(1f)) { Column(Modifier.padding(12.dp)) { Text("Total Unpaid"); Text("R5400", style=MaterialTheme.typography.headlineSmall) } }
                Card(Modifier.weight(1f)) { Column(Modifier.padding(12.dp)) { Text("Active Projects"); Text("3") } }
            }
        }
    }
}