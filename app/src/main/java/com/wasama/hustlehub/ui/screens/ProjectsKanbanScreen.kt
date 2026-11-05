package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wasama.hustlehub.data.local.*
import com.wasama.hustlehub.ui.components.ProductionEmptyState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsKanbanScreen(nav: NavController) {
    val context = LocalContext.current
    val db = remember { HustleHubDatabase.get(context) }
    var projects by remember { mutableStateOf<List<ProjectEntity>>(emptyList()) }
    var showAdd by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.projectDao().getProjects("current_user").collect { list ->
            projects = list
        }
    }

    if (projects.isEmpty()) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { showAdd = true }, containerColor = Color(0xFF2DD4BF)) {
                    Icon(Icons.Default.Add, null, tint = Color.Black)
                }
            }
        ) { pad ->
            Box(Modifier.padding(pad).fillMaxSize()) {
                ProductionEmptyState(
                    icon = Icons.Default.Folder,
                    title = "No projects yet",
                    subtitle = "Create your first project with description, deadline, and up to 10 tasks. All boards start empty.",
                    ctaText = "Create First Project"
                ) { showAdd = true }
            }
        }
        if (showAdd) { AddProjectDialogProduction(onDismiss = { showAdd = false }, db = db) }
        return
    }

    val allStatuses = listOf(ProjectStatus.PROPOSED, ProjectStatus.IN_PROGRESS, ProjectStatus.DONE, ProjectStatus.INVOICED, ProjectStatus.PAID)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Board • ${projects.size} projects", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }, containerColor = Color(0xFF2DD4BF)) {
                Icon(Icons.Default.Add, null, tint = Color.Black)
            }
        }
    ) { pad ->
        LazyRow(Modifier.fillMaxSize().padding(pad).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(allStatuses) { col ->
                Column(Modifier.width(340.dp).fillMaxHeight().background(Color(0xFF101918), shape = RoundedCornerShape(16.dp)).padding(12.dp)) {
                    val count = projects.count { it.status == col }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${col.name.replace("_", " ")} • $count", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (projects.count { it.status == col && it.isOverdue() } > 0) {
                            Badge(containerColor = Color(0xFFEF4444)) { Text("${projects.count { it.status == col && it.isOverdue() }} overdue", color = Color.White, style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                    HorizontalDivider(color = Color(0xFF2A3C38), modifier = Modifier.padding(vertical=8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                        items(projects.filter { it.status == col }, key = { it.projectId }) { proj ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { nav.navigate("projectDetail/${proj.projectId}") },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2E2A))
                            ) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(proj.title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    if (proj.description.isNotBlank()) Text(proj.description, color = Color(0xFFD1E8E2), style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                    val overdue = proj.isOverdue()
                                    Text(
                                        "Deadline: ${java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(java.util.Date(proj.deadlineTimestamp))} • ${daysLeftText(proj.deadlineTimestamp)}",
                                        color = if (overdue) Color(0xFFEF4444) else Color(0xFF7ED8C6),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    val taskCount = try { proj.getTasksListSafe().size } catch (_: Exception) { 0 }
                                    if (taskCount > 0) Text("Tasks: $taskCount/10", color = Color.White, style = MaterialTheme.typography.labelSmall)

                                    Spacer(Modifier.height(8.dp))
                                    Text("Move to:", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            MoveChipProduction("PROPOSED", proj, allStatuses[0], db)
                                            MoveChipProduction("IN PROG", proj, allStatuses[1], db)
                                            MoveChipProduction("DONE", proj, allStatuses[2], db)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            MoveChipProduction("INVOICED", proj, allStatuses[3], db, highlight = true)
                                            MoveChipProduction("PAID", proj, allStatuses[4], db, highlight = true)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showAdd) { AddProjectDialogProduction(onDismiss = { showAdd = false }, db = db) }
    }
}

@Composable
fun AddProjectDialogProduction(onDismiss: () -> Unit, db: HustleHubDatabase) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var tasksText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Project") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title*") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description (optional)") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = tasksText, onValueChange = { tasksText = it }, label = { Text("Tasks (optional, max 10, one per line)") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                Text("All fields start empty - no test data", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isBlank()) return@Button
                val taskList = tasksText.lines().filter { it.isNotBlank() }.take(10).map { TaskItem(it) }
                val json = com.google.gson.Gson().toJson(taskList)
                val newProj = ProjectEntity(title = title, description = desc, tasksJson = json, budgetAmount = 0.0)
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try { db.projectDao().insert(newProj) } catch (_: Exception) {}
                }
                onDismiss()
            }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun MoveChipProduction(label: String, proj: ProjectEntity, target: ProjectStatus, db: HustleHubDatabase, highlight: Boolean = false) {
    FilterChip(
        selected = proj.status == target,
        onClick = { kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch { try { db.projectDao().updateStatus(proj.projectId, target) } catch (_: Exception) {} } },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = if (highlight) Color(0xFF2DD4BF) else Color(0xFF3A4A45),
            selectedLabelColor = if (highlight) Color.Black else Color.White
        ),
        modifier = Modifier.height(32.dp)
    )
}

fun daysLeftText(ts: Long): String {
    val diff = (ts - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)
    return when {
        diff < 0 -> "Overdue by ${-diff}d"
        diff == 0L -> "Due today"
        diff == 1L -> "1d left"
        else -> "${diff}d left"
    }
}
