package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun StatsScreen(nav: NavController) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Stats & Badges", style = MaterialTheme.typography.headlineSmall) }
        item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Earnings by Client - Pie Chart"); Text("Acme: R5400, TechStart: R12000") } } }
        item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Monthly Bar Chart"); Text("Jan R2000 | Feb R5000 | Mar R12000") } } }
        item {
            Text("Badges")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("First Client", "First R1000", "5 Projects", "On-Time", "Night Owl").forEach { badge ->
                    AssistChip(onClick = {}, label = { Text(badge) })
                }
            }
        }
    }
}
