package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wasama.hustlehub.data.local.*
import com.google.gson.Gson
import kotlinx.coroutines.launch

@Composable
fun ProjectDetailScreen(projectId: String, nav: NavController) {
    val context = LocalContext.current
    val db = remember { HustleHubDatabase.get(context) }
    val scope = rememberCoroutineScope()
    var project by remember { mutableStateOf<ProjectEntity?>(null) }
    var description by remember { mutableStateOf("") }
    var tasks by remember { mutableStateOf(listOf<TaskItem>()) }
    var showAddTask by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }

    LaunchedEffect(projectId) {
        val p = db.projectDao().getById(projectId)
        if (p != null) {
            project = p
            description = p.description
            tasks = p.getTasksList()
        }
    }

    // Add task dialog with max 10 enforcement
    if (showAddTask) {
        AlertDialog(
            onDismissRequest = { showAddTask = false },
            title = { Text("Add Task (${tasks.size}/10)") },
            text = { OutlinedTextField(value = newTaskTitle, onValueChange = { newTaskTitle = it }, label = { Text("Task title") }, singleLine = true) },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank() && tasks.size < 10) {
                            val updated = tasks + TaskItem(title = newTaskTitle)
                            tasks = updated
                            scope.launch { db.projectDao().updateTasks(projectId, Gson().toJson(updated)) }
                            newTaskTitle = ""; showAddTask = false
                        }
                    },
                    enabled = tasks.size < 10 && newTaskTitle.isNotBlank()
                ) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddTask = false }) { Text("Cancel") } }
        )
    }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text(project?.title ?: "Project Detail", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Description", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Project description") },
                        minLines = 3, maxLines = 6, modifier = Modifier.fillMaxWidth().padding(top=8.dp)
                    )
                    Button(onClick = { scope.launch { db.projectDao().updateDescription(projectId, description) } }, modifier = Modifier.padding(top=8.dp)) { Text("Save Description") }
                    Spacer(Modifier.height(8.dp))
                    Text("Budget: USD -> R${project?.convertedBudgetZAR ?: project?.budgetAmount} via ExchangeRate-API", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tasks (${tasks.size}/10)", style = MaterialTheme.typography.titleMedium)
                FilledTonalButton(onClick = { if (tasks.size < 10) showAddTask = true }, enabled = tasks.size < 10) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Add Task")
                }
            }
            if (tasks.size >= 10) Text("Max 10 tasks reached - delete one to add more", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
        }

        itemsIndexed(tasks) { idx, task ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(Modifier.weight(1f)) {
                        Checkbox(checked = task.isCompleted, onCheckedChange = { checked ->
                            val updated = tasks.toMutableList().also { it[idx] = task.copy(isCompleted = checked) }
                            tasks = updated
                            scope.launch { db.projectDao().updateTasks(projectId, Gson().toJson(updated)) }
                            // Award XP: 10 per task, 60 if on-time (you can check deadline here)
                        })
                        Text(task.title, modifier = Modifier.padding(start=8.dp, top=12.dp))
                    }
                    if (task.isCompleted) Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) { Text("+10 XP") }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Button(onClick = { nav.navigate("timer/$projectId") }, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Start Timer - Foreground Service") }
            Button(onClick = { nav.navigate("invoice/$projectId") }, modifier = Modifier.fillMaxWidth().padding(top=8.dp)) { Text("Generate Invoice PDF") }
            OutlinedButton(onClick = { nav.popBackStack() }, modifier = Modifier.fillMaxWidth().padding(top=8.dp)) { Text("Back to Kanban") }
        }
    }
}
