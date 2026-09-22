package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showAddTask by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }

    LaunchedEffect(projectId) {
        try {
            if(projectId == "new" || projectId.isBlank()) {
                error = "Invalid project ID"; loading = false; return@LaunchedEffect
            }
            val p = db.projectDao().getById(projectId)
            if(p == null) {
                error = "Project not found"; loading = false; return@LaunchedEffect
            }
            project = p
            description = p.description
            tasks = p.getTasksListSafe()
            loading = false
        } catch(e: Exception) {
            error = "Error loading: ${e.message}"; loading = false
        }
    }

    if(showAddTask) {
        AlertDialog(
            onDismissRequest={showAddTask=false},
            title={Text("Add Task (${tasks.size}/10)")},
            text={ OutlinedTextField(value=newTaskTitle, onValueChange={newTaskTitle=it}, label={Text("Task title")}, singleLine=true) },
            confirmButton={
                Button(
                    onClick={
                        if(newTaskTitle.isNotBlank() && tasks.size<10) {
                            val updated = tasks + TaskItem(newTaskTitle)
                            tasks = updated
                            scope.launch{ try{ db.projectDao().updateTasks(projectId, Gson().toJson(updated)) }catch(_:Exception){} }
                            newTaskTitle=""; showAddTask=false
                        }
                    },
                    enabled=tasks.size<10 && newTaskTitle.isNotBlank()
                ){ Text("Add") }
            },
            dismissButton={ TextButton(onClick={showAddTask=false}){ Text("Cancel") } }
        )
    }

    if(loading) {
        Box(Modifier.fillMaxSize(), contentAlignment= Alignment.Center){ CircularProgressIndicator() }
        return
    }

    error?.let { msg ->
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text(msg, color=MaterialTheme.colorScheme.error)
            Button(onClick={ nav.popBackStack() }){ Text("Back to Board") }
        }
        return
    }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item {
            Text(project?.title ?: "Project", style=MaterialTheme.typography.headlineSmall)
            Card(shape=RoundedCornerShape(12.dp), modifier=Modifier.padding(top=12.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Description", style=MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value=description, onValueChange={description=it}, label={Text("Description")}, minLines=3, maxLines=6, modifier=Modifier.fillMaxWidth().padding(top=8.dp))
                    Button(onClick={ scope.launch{ try{ db.projectDao().updateDescription(projectId, description) }catch(_:Exception){} } }, modifier=Modifier.padding(top=8.dp)){ Text("Save Description") }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
                Text("Tasks (${tasks.size}/10)", style=MaterialTheme.typography.titleMedium)
                FilledTonalButton(onClick={ if(tasks.size<10) showAddTask=true }, enabled=tasks.size<10){
                    Icon(Icons.Default.Add, null, modifier=Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Add")
                }
            }
            if(tasks.size>=10) Text("Max 10 reached", color=MaterialTheme.colorScheme.error, style=MaterialTheme.typography.labelSmall)
        }
        itemsIndexed(tasks) { idx, task ->
            Card(Modifier.fillMaxWidth(), shape=RoundedCornerShape(10.dp)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement=Arrangement.SpaceBetween) {
                    Row(Modifier.weight(1f)) {
                        Checkbox(checked=task.isCompleted, onCheckedChange={ checked ->
                            val updated = tasks.toMutableList().also{ it[idx]=task.copy(isCompleted=checked) }
                            tasks = updated
                            scope.launch{ try{ db.projectDao().updateTasks(projectId, Gson().toJson(updated)) }catch(_:Exception){} }
                        })
                        Text(task.title, modifier=Modifier.padding(start=8.dp, top=12.dp))
                    }
                    if(task.isCompleted) Badge{ Text("+10 XP") }
                }
            }
        }
        item {
            Button(onClick={ nav.navigate("timer/$projectId") }, modifier=Modifier.fillMaxWidth().height(48.dp)){ Text("Start Timer") }
            Button(onClick={ nav.navigate("invoice/$projectId") }, modifier=Modifier.fillMaxWidth().padding(top=8.dp)){ Text("Generate Invoice") }
            OutlinedButton(onClick={ nav.popBackStack() }, modifier=Modifier.fillMaxWidth().padding(top=8.dp)){ Text("Back to Kanban") }
        }
    }
}
