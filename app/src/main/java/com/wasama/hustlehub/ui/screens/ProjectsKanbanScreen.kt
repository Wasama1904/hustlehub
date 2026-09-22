package com.wasama.hustlehub.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.wasama.hustlehub.data.local.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsKanbanScreen(nav: NavController) {
    val context = LocalContext.current
    val db = remember { HustleHubDatabase.get(context) }
    val scope = rememberCoroutineScope()
    var projects by remember { mutableStateOf<List<ProjectEntity>>(emptyList()) }
    var showAdd by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            db.projectDao().getProjects("current_user").collect { list ->
                projects = list
                if (list.isEmpty()) {
                    val now = System.currentTimeMillis()
                    val demo = listOf(
                        ProjectEntity(title="Logo Redesign", description="Redesign logo", deadlineTimestamp=now+2*24*60*60*1000L, budgetAmount=5000.0),
                        ProjectEntity(title="Website Build", description="Landing page", deadlineTimestamp=now-1*24*60*60*1000L, budgetAmount=12000.0, status=ProjectStatus.IN_PROGRESS),
                        ProjectEntity(title="Social Kit", description="20 templates", deadlineTimestamp=now+5*24*60*60*1000L, budgetAmount=1500.0)
                    )
                    demo.forEach { db.projectDao().insert(it) }
                }
            }
        } catch (e: Exception) {}
    }

    if (showAdd) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var tasksText by remember { mutableStateOf("") }
        var deadline by remember { mutableStateOf(System.currentTimeMillis() + 2*24*60*60*1000L) }
        var showDatePicker by remember { mutableStateOf(false) }

        if (showDatePicker) {
            val cal = Calendar.getInstance().apply { timeInMillis = deadline }
            DatePickerDialog(context, { _, y, m, d ->
                val c = Calendar.getInstance().apply { set(y,m,d) }
                deadline = c.timeInMillis
                showDatePicker = false
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            LaunchedEffect(Unit) { showDatePicker = false }
        }

        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add Project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value=title, onValueChange={title=it}, label={Text("Title*")}, singleLine=true, modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(value=desc, onValueChange={desc=it}, label={Text("Description")}, minLines=2, modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(value=tasksText, onValueChange={tasksText=it}, label={Text("Tasks (max 10, one per line)")}, minLines=3, modifier=Modifier.fillMaxWidth())
                    Button(onClick={ showDatePicker = true }, modifier=Modifier.fillMaxWidth()) {
                        val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        Text("Deadline: ${fmt.format(Date(deadline))} (${daysLeftText(deadline)})")
                    }
                }
            },
            confirmButton = {
                Button(onClick={
                    if(title.isBlank()) return@Button
                    val taskList = tasksText.lines().filter{it.isNotBlank()}.take(10).map{ TaskItem(it) }
                    val json = Gson().toJson(taskList)
                    val newProj = ProjectEntity(title=title, description=desc, deadlineTimestamp=deadline, tasksJson=json)
                    scope.launch { 
                        try { db.projectDao().insert(newProj) } catch(_:Exception){}
                    }
                    showAdd = false
                }){ Text("Add") }
            },
            dismissButton = { TextButton(onClick={showAdd=false}){ Text("Cancel") } }
        )
    }

    val allStatuses = listOf(ProjectStatus.PROPOSED, ProjectStatus.IN_PROGRESS, ProjectStatus.DONE, ProjectStatus.INVOICED, ProjectStatus.PAID)

    Scaffold(floatingActionButton={ FloatingActionButton(onClick={showAdd=true}, containerColor=Color(0xFF2DD4BF)){ Icon(Icons.Default.Add, null) } }) { pad ->
        LazyRow(Modifier.fillMaxSize().padding(pad).padding(12.dp), horizontalArrangement=Arrangement.spacedBy(16.dp)) {
            items(allStatuses) { col ->
                Column(Modifier.width(340.dp).fillMaxHeight().background(Color(0xFF101918), shape=RoundedCornerShape(16.dp)).padding(12.dp)) {
                    val count = projects.count{it.status==col}
                    val overdueInCol = projects.count{it.status==col && it.isOverdue()}
                    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
                        Text("${col.name.replace("_"," ")} • $count", color=Color.White, style=MaterialTheme.typography.titleMedium)
                        if(overdueInCol>0) Badge(containerColor=Color(0xFFEF4444)){ Text("$overdueInCol overdue", color=Color.White, style=MaterialTheme.typography.labelSmall) }
                    }
                    HorizontalDivider(color=Color(0xFF2A3C38), modifier=Modifier.padding(vertical=8.dp))
                    LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp), modifier=Modifier.weight(1f)) {
                        items(projects.filter{it.status==col}, key={it.projectId}) { proj ->
                            Card(
                                onClick={ nav.navigate("projectDetail/${proj.projectId}") },
                                modifier = Modifier.fillMaxWidth(),
                                shape=RoundedCornerShape(12.dp),
                                colors=CardDefaults.cardColors(containerColor=Color(0xFF1C2E2A))
                            ) {
                                Column(Modifier.padding(14.dp), verticalArrangement=Arrangement.spacedBy(6.dp)) {
                                    Text(proj.title, color=Color.White, style=MaterialTheme.typography.titleMedium)
                                    if(proj.description.isNotBlank()) Text(proj.description, color=Color(0xFFD1E8E2), style=MaterialTheme.typography.bodySmall, maxLines=2)
                                    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                                    val overdue = proj.isOverdue()
                                    Text("Deadline: ${sdf.format(Date(proj.deadlineTimestamp))} • ${daysLeftText(proj.deadlineTimestamp)}", color= if(overdue) Color(0xFFEF4444) else Color(0xFF7ED8C6), style=MaterialTheme.typography.labelSmall)
                                    Text("Budget: R${proj.budgetAmount}", color=Color(0xFF5EE9D1), style=MaterialTheme.typography.labelSmall)
                                    val taskCount = try{ proj.getTasksListSafe().size }catch(_:Exception){0}
                                    if(taskCount>0) Text("Tasks: $taskCount/10", color=Color.White, style=MaterialTheme.typography.labelSmall)

                                    // MOVE buttons - wrapped properly
                                    Spacer(Modifier.height(8.dp))
                                    Text("Move to:", color=Color.Gray, style=MaterialTheme.typography.labelSmall)
                                    Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
                                        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                                            MoveChip("PROPOSED", proj, allStatuses[0], db, scope)
                                            MoveChip("IN PROG", proj, allStatuses[1], db, scope)
                                            MoveChip("DONE", proj, allStatuses[2], db, scope)
                                        }
                                        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                                            MoveChip("INVOICED", proj, allStatuses[3], db, scope, highlight=true)
                                            MoveChip("PAID", proj, allStatuses[4], db, scope, highlight=true)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MoveChip(label: String, proj: ProjectEntity, target: ProjectStatus, db: HustleHubDatabase, scope: CoroutineScope, highlight: Boolean = false) {
    val isSelected = proj.status == target
    FilterChip(
        selected = isSelected,
        onClick = {
            scope.launch {
                try { db.projectDao().updateStatus(proj.projectId, target) } catch(_:Exception){}
            }
        },
        label = { Text(label, style=MaterialTheme.typography.labelSmall) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = if(highlight) Color(0xFF2DD4BF) else Color(0xFF3A4A45),
            selectedLabelColor = if(highlight) Color.Black else Color.White
        ),
        modifier = Modifier.height(32.dp)
    )
}

fun daysLeftText(ts: Long): String {
    val diff = (ts - System.currentTimeMillis()) / (24*60*60*1000L)
    return when {
        diff < 0 -> "Overdue by ${-diff}d"
        diff == 0L -> "Due today"
        diff == 1L -> "1d left"
        else -> "${diff}d left"
    }
}
