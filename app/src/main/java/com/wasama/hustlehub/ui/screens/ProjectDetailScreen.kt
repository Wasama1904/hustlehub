package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ProjectDetailScreen(projectId: String, nav: NavController) {
    var tasks by remember { mutableStateOf(listOf("Design draft" to false, "Client review" to false, "Final export" to true)) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Project Detail - $projectId", style = MaterialTheme.typography.headlineSmall)
        Text("Budget: USD 275 -> R5000 (Rate: R18.50/USD via ExchangeRate-API)", color = MaterialTheme.colorScheme.secondary)
        Text("Deadline: 2 days left", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        Text("Tasks", style = MaterialTheme.typography.titleMedium)
        tasks.forEachIndexed { idx, (title, done) ->
            Row(Modifier.fillMaxWidth()) {
                Checkbox(checked = done, onCheckedChange = { checked ->
                    tasks = tasks.toMutableList().also { it[idx] = title to checked }
                    // Award 10 XP, 60 if on-time
                })
                Text(title, modifier = Modifier.padding(top=12.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Tracked: 5h x R250 = R1250 billable", style = MaterialTheme.typography.titleMedium)
        Button(onClick = { nav.navigate("timer/$projectId") }, modifier = Modifier.fillMaxWidth()) { Text("Start Timer - Foreground Service") }
        Button(onClick = { nav.navigate("invoice/$projectId") }, modifier = Modifier.fillMaxWidth().padding(top=8.dp)) { Text("Generate Invoice PDF") }
    }
}
