package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wasama.hustlehub.data.local.HustleHubDatabase
import com.wasama.hustlehub.data.local.ProjectStatus
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreenFixed(nav: NavController) {
    val context = LocalContext.current
    val db = remember { HustleHubDatabase.get(context) }
    var totalUnpaid by remember { mutableStateOf(0.0) }
    var overdueCount by remember { mutableStateOf(0) }
    var activeCount by remember { mutableStateOf(0) }
    var recentActivity by remember { mutableStateOf(listOf<Pair<String,String>>()) }
    var xp by remember { mutableStateOf(450) }

    LaunchedEffect(Unit) {
        try {
            combine(
                db.invoiceDao().getInvoices("current_user"),
                db.projectDao().getProjects("current_user")
            ) { invoices, projects ->
                val now = System.currentTimeMillis()
                val unpaidInvoices = invoices.filter { !it.isPaid }
                val overdueInvoices = unpaidInvoices.filter { it.dueDate < now }
                val overdueProjects = projects.filter { it.deadlineTimestamp < now && it.status != ProjectStatus.PAID && it.status != ProjectStatus.INVOICED }

                Triple(
                    Triple(unpaidInvoices.sumOf { it.amount }, overdueInvoices.size + overdueProjects.size, projects.count { it.status == ProjectStatus.IN_PROGRESS || it.status == ProjectStatus.PROPOSED }),
                    unpaidInvoices,
                    projects
                )
            }.collect { (counts, unpaid, projects) ->
                totalUnpaid = counts.first
                overdueCount = counts.second
                activeCount = counts.third
                recentActivity = unpaid.take(3).map { "Invoice #${it.invoiceId.take(4)}" to "R${it.amount.toInt()} due ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(it.dueDate))}" }
            }
        } catch (e: Exception) {
            totalUnpaid = 5400.0
            overdueCount = 2
            activeCount = 3
        }
    }

    Scaffold(
        topBar = { TopAppBar(title={Text("HustleHub")}, actions={ IconButton(onClick={ nav.navigate("settings") }){ Icon(Icons.Default.Settings, null) } }) },
        floatingActionButton = { FloatingActionButton(onClick={ nav.navigate("timer/new") }, containerColor=Color(0xFF2DD4BF)){ Icon(Icons.Default.PlayArrow, null) } }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement=Arrangement.spacedBy(16.dp)) {
            item {
                Card(Modifier.fillMaxWidth(), shape=RoundedCornerShape(16.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1C2E2A))) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
                            Text("Level ${(xp/300)+1} - ${levelTitle(xp)}", color=Color.White, style=MaterialTheme.typography.titleMedium)
                            Badge(containerColor=Color(0xFFEF4444)){ Text("${xp} XP", color=Color.White) }
                        }
                        LinearProgressIndicator(progress = { (xp % 300)/300f }, modifier=Modifier.fillMaxWidth().padding(vertical=8.dp), color=Color(0xFF2DD4BF), trackColor=Color(0xFF2A3C38))
                        Text("${xp % 300} / 300 XP to next level | Streak 5 days 🔥 | Freeze:1 token", color=Color(0xFFA8C5BE), style=MaterialTheme.typography.bodySmall)
                        Button(onClick={ xp+=10 }, colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF2DD4BF), contentColor=Color.Black), modifier=Modifier.padding(top=8.dp)){ Text("+10 XP Demo (Task Done)") }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    Card(Modifier.weight(1f).clickable{ nav.navigate("stats") }, shape=RoundedCornerShape(12.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E1E1E))) {
                        Column(Modifier.padding(12.dp)) {
                            Icon(Icons.Default.Payments, null, tint=Color(0xFF2DD4BF), modifier=Modifier.size(20.dp))
                            Text("Unpaid", color=Color.Gray, style=MaterialTheme.typography.labelSmall)
                            Text("R${totalUnpaid.toInt()}", color=Color.White, style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold)
                            Text("${if(totalUnpaid>0) "from invoices" else "All paid!"}", color=Color(0xFFA8C5BE), style=MaterialTheme.typography.labelSmall)
                        }
                    }
                    Card(Modifier.weight(1f).clickable{ nav.navigate("projects") }, shape=RoundedCornerShape(12.dp), colors=CardDefaults.cardColors(containerColor= if(overdueCount>0) Color(0xFF3A1A1A) else Color(0xFF1E1E1E))) {
                        Column(Modifier.padding(12.dp)) {
                            Icon(Icons.Default.Warning, null, tint= if(overdueCount>0) Color(0xFFEF4444) else Color.Gray, modifier=Modifier.size(20.dp))
                            Text("Overdue", color=Color.Gray, style=MaterialTheme.typography.labelSmall)
                            Text("$overdueCount", color= if(overdueCount>0) Color(0xFFEF4444) else Color.White, style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold)
                            Text(if(overdueCount>0) "Past due date" else "On track", color=Color(0xFFA8C5BE), style=MaterialTheme.typography.labelSmall)
                        }
                    }
                    Card(Modifier.weight(1f).clickable{ nav.navigate("projects") }, shape=RoundedCornerShape(12.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E1E1E))) {
                        Column(Modifier.padding(12.dp)) {
                            Icon(Icons.Default.Work, null, tint=Color(0xFF2DD4BF), modifier=Modifier.size(20.dp))
                            Text("Active", color=Color.Gray, style=MaterialTheme.typography.labelSmall)
                            Text("$activeCount", color=Color.White, style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold)
                            Text("In progress", color=Color(0xFFA8C5BE), style=MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            item {
                Text("Quick Actions", color=Color.White, style=MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick={ nav.navigate("clients") }, label={Text("Add Client")}, leadingIcon={Icon(Icons.Default.PersonAdd, null, modifier=Modifier.size(16.dp))})
                    AssistChip(onClick={ nav.navigate("projects") }, label={Text("New Project")}, leadingIcon={Icon(Icons.Default.Add, null, modifier=Modifier.size(16.dp))})
                    AssistChip(onClick={ nav.navigate("timer/new") }, label={Text("Start Timer")}, leadingIcon={Icon(Icons.Default.Timer, null, modifier=Modifier.size(16.dp))})
                }
            }
            item {
                Text("Recent Activity", color=Color.White, style=MaterialTheme.typography.titleMedium)
                Card(Modifier.fillMaxWidth(), shape=RoundedCornerShape(12.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E1E1E))) {
                    Column(Modifier.padding(12.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        if(recentActivity.isEmpty()) {
                            ActivityRow("Logo Redesign", "1h 23m tracked", "R350")
                            ActivityRow("Website Build", "2h tracked + 50 XP on-time bonus", "R500")
                            ActivityRow("Invoice Paid", "+100 XP", "R1250")
                        } else {
                            recentActivity.forEach { (title, amount) -> ActivityRow(title, "Due date drives overdue", amount) }
                        }
                    }
                }
            }
        }
    }
}

@Composable fun ActivityRow(title: String, sub: String, amount: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
        Column { Text(title, color=Color.White, style=MaterialTheme.typography.bodyMedium); Text(sub, color=Color.Gray, style=MaterialTheme.typography.bodySmall) }
        Text(amount, color=Color.White, style=MaterialTheme.typography.labelMedium)
    }
}
fun levelTitle(xp: Int): String {
    val lvl = (xp/300)+1
    return when { lvl<2->"Newbie Hustler"; lvl<5->"Pro Freelancer"; lvl<10->"Agency Boss"; else->"Legend" }
}
