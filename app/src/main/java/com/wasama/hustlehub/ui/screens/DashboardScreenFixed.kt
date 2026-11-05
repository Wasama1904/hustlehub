package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wasama.hustlehub.data.local.HustleHubDatabase
import com.wasama.hustlehub.ui.components.ProductionEmptyState
import kotlinx.coroutines.flow.combine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreenFixed(nav: NavController) {
    val context = LocalContext.current
    val db = remember { HustleHubDatabase.get(context) }
    var totalUnpaid by remember { mutableStateOf(0.0) }
    var overdueCount by remember { mutableStateOf(0) }
    var activeCount by remember { mutableStateOf(0) }
    var hasAnyData by remember { mutableStateOf(false) }
    var xp by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        try {
            combine(
                db.invoiceDao().getInvoices("current_user"),
                db.projectDao().getProjects("current_user"),
                db.userStatsDao().getStats("current_user")
            ) { invoices, projects, stats ->
                val now = System.currentTimeMillis()
                val unpaid = invoices.filter { !it.isPaid }
                val overdueInvoices = unpaid.filter { it.dueDate < now }
                val overdueProjects = projects.filter { it.deadlineTimestamp < now && it.status.name != "PAID" && it.status.name != "INVOICED" }
                
                Triple(
                    Triple(unpaid.sumOf { it.amount }, overdueInvoices.size + overdueProjects.size, projects.count { it.status.name == "IN_PROGRESS" || it.status.name == "PROPOSED" }),
                    invoices.isNotEmpty() || projects.isNotEmpty(),
                    stats?.totalXp ?: 0
                )
            }.collect { (counts, hasData, xpVal) ->
                totalUnpaid = counts.first
                overdueCount = counts.second
                activeCount = counts.third
                hasAnyData = hasData
                xp = xpVal
            }
        } catch (e: Exception) {
            hasAnyData = false
            xp = 0
        }
    }

    if (!hasAnyData && totalUnpaid == 0.0 && overdueCount == 0 && activeCount == 0 && xp == 0) {
        ProductionEmptyState(
            icon = Icons.Default.RocketLaunch,
            title = "Welcome to HustleHub",
            subtitle = "Your freelance business starts here. Add your first client, create a project, and start tracking. All stats start at zero.",
            ctaText = "Add Your First Client"
        ) { nav.navigate("clients") }
        return
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("HustleHub", fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0A0A))) },
        containerColor = Color(0xFF0A0A0A),
        floatingActionButton = {
            if (hasAnyData) {
                FloatingActionButton(onClick = { nav.navigate("timer/new") }, containerColor = Color(0xFF2DD4BF)) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                }
            }
        }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color(0xFF1E1E1E))) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Level ${(xp/300)+1}", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    when((xp/300)+1) { 1 -> "Newbie Hustler" 2 -> "Rising Freelancer" 3 -> "Pro Freelancer" else -> "Agency Boss" },
                                    color = Color(0xFF2DD4BF), style = MaterialTheme.typography.labelMedium
                                )
                            }
                            Badge(containerColor = Color(0xFF2DD4BF)) { Text("${xp} XP", color = Color.Black) }
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { (xp % 300)/300f }, modifier = Modifier.fillMaxWidth().height(6.dp), color = Color(0xFF2DD4BF), trackColor = Color(0xFF2A2A2A))
                        Text("${xp % 300} / 300 XP to next level", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(Color(0xFF1E1E1E))) {
                        Column(Modifier.padding(12.dp)) {
                            Icon(Icons.Default.Payments, null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(20.dp))
                            Text("Unpaid", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                            Text("R${totalUnpaid.toInt()}", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(if(totalUnpaid==0.0) "All caught up" else "from invoices", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Card(Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(if(overdueCount>0) Color(0xFF3A1A1A) else Color(0xFF1E1E1E))) {
                        Column(Modifier.padding(12.dp)) {
                            Icon(Icons.Default.Warning, null, tint = if(overdueCount>0) Color(0xFFEF4444) else Color.Gray, modifier = Modifier.size(20.dp))
                            Text("Overdue", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                            Text("$overdueCount", color = if(overdueCount>0) Color(0xFFEF4444) else Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(if(overdueCount>0) "Past due" else "On track", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Card(Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(Color(0xFF1E1E1E))) {
                        Column(Modifier.padding(12.dp)) {
                            Icon(Icons.Default.Work, null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(20.dp))
                            Text("Active", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                            Text("$activeCount", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("In progress", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            item {
                if (totalUnpaid == 0.0 && overdueCount == 0 && activeCount == 0) {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(Color(0xFF1E1E1E))) {
                        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, null, Modifier.size(32.dp), tint = Color(0xFF2DD4BF))
                            Spacer(Modifier.height(8.dp))
                            Text("All clear! No overdue, no unpaid.", color = Color.White)
                            Text("Add a project to get started.", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
