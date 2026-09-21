package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wasama.hustlehub.data.local.HustleHubDatabase
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreenFixed(nav: NavController) {
    val context = LocalContext.current
    val db = remember { HustleHubDatabase.get(context) }
    val scope = rememberCoroutineScope()
    var totalUnpaid by remember { mutableStateOf(5400.0) }
    var xp by remember { mutableStateOf(450) }
    var streak by remember { mutableStateOf(5) }

    // Load from Room in real app: collectAsState for projects/invoices
    Scaffold(
        topBar = { TopAppBar(title = { Text("HustleHub") }, actions = { IconButton(onClick={ nav.navigate("settings") }){ Icon(Icons.Default.Settings, null) } }) },
        floatingActionButton = { FloatingActionButton(onClick={ nav.navigate("timer/new") }){ Icon(Icons.Default.PlayArrow, null) } }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                // XP Card - functional
                Card(Modifier.fillMaxWidth().clickable{ nav.navigate("stats") }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Level ${ (xp/300)+1 } - ${levelTitle(xp)}", style = MaterialTheme.typography.titleMedium)
                            Badge { Text("${xp} XP") }
                        }
                        LinearProgressIndicator(progress = { (xp % 300)/300f }, modifier = Modifier.fillMaxWidth().padding(vertical=8.dp))
                        Text("${xp % 300} / 300 XP to next level | Streak $streak days 🔥 | Freeze:1 token", style = MaterialTheme.typography.bodySmall)
                        Button(onClick = { xp += 10 }, modifier = Modifier.padding(top=8.dp)) { Text("+10 XP Demo (Task Done)") }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Unpaid", "R${totalUnpaid.toInt()}", Icons.Default.Payments, Modifier.weight(1f)) { nav.navigate("invoice/list") }
                    StatCard("Overdue", "2", Icons.Default.Warning, Modifier.weight(1f)) { }
                    StatCard("Active", "3", Icons.Default.Work, Modifier.weight(1f)) { nav.navigate("projects") }
                }
            }
            item {
                Text("Quick Actions", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { nav.navigate("clients") }, label = { Text("Add Client") }, leadingIcon = { Icon(Icons.Default.PersonAdd, null) })
                    AssistChip(onClick = { nav.navigate("projects") }, label = { Text("New Project") }, leadingIcon = { Icon(Icons.Default.Add, null) })
                    AssistChip(onClick = { nav.navigate("timer/new") }, label = { Text("Start Timer") }, leadingIcon = { Icon(Icons.Default.Timer, null) })
                }
            }
            item {
                Text("Recent Activity", style = MaterialTheme.typography.titleMedium)
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActivityRow("Logo Redesign", "1h 23m tracked", "R350")
                        ActivityRow("Website Build", "2h tracked + 50 XP on-time bonus", "R500")
                        ActivityRow("Invoice Paid", "+100 XP", "R1250")
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier.clickable(onClick=onClick)) {
        Column(Modifier.padding(12.dp)) {
            Icon(icon, null, modifier = Modifier.size(20.dp))
            Text(title, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}
@Composable
fun ActivityRow(title: String, sub: String, amount: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column { Text(title, style=MaterialTheme.typography.bodyMedium); Text(sub, style=MaterialTheme.typography.bodySmall) }
        Text(amount, style=MaterialTheme.typography.labelMedium)
    }
}
fun levelTitle(xp: Int): String {
    val lvl = (xp/300)+1
    return when {
        lvl < 2 -> "Newbie Hustler"
        lvl < 5 -> "Pro Freelancer"
        lvl < 10 -> "Agency Boss"
        else -> "Legend"
    }
}
