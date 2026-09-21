package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wasama.hustlehub.data.local.*
import kotlinx.coroutines.launch

@Composable
fun ProjectsKanbanDragDropScreen(nav: NavController) {
    val context = LocalContext.current
    val db = remember { HustleHubDatabase.get(context) }
    val scope = rememberCoroutineScope()

    var projects by remember { mutableStateOf(
        listOf(
            KanbanProject("1","Logo Redesign","Acme", ProjectStatus.PROPOSED, 5000.0, "USD 275 -> R5000"),
            KanbanProject("2","Website Build","TechStart", ProjectStatus.IN_PROGRESS, 12000.0, "USD 650 -> R12000"),
            KanbanProject("3","Pitch Deck","Acme", ProjectStatus.DONE, 3000.0, "R3000"),
            KanbanProject("4","Social Kit","TechStart", ProjectStatus.PROPOSED, 1500.0, "R1500")
        )
    ) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProject by remember { mutableStateOf<KanbanProject?>(null) }
    var draggedId by remember { mutableStateOf<String?>(null) }

    val columns = listOf(ProjectStatus.PROPOSED, ProjectStatus.IN_PROGRESS, ProjectStatus.DONE, ProjectStatus.INVOICED)

    Scaffold(
        topBar = { TopAppBar(title={Text("Kanban Board - Long press to drag")}) },
        floatingActionButton = { FloatingActionButton(onClick={ editingProject=null; showAddDialog=true }){ Icon(Icons.Default.Add, null) } }
    ) { pad ->
        LazyRow(Modifier.padding(pad).padding(8.dp).fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(columns) { col ->
                var isDragTarget by remember { mutableStateOf(false) }
                Column(
                    Modifier.width(300.dp).fillMaxHeight()
                        .background(if(isDragTarget) Color(0xFFCCF3E8) else Color(0xFFF1F5F3), shape = MaterialTheme.shapes.medium)
                        .border(if(isDragTarget) 2.dp else 0.dp, Color(0xFF2DD4BF), MaterialTheme.shapes.medium)
                        .padding(8.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(col.name.replace("_"," "), style = MaterialTheme.typography.titleSmall)
                        Badge { Text("${projects.count{it.status==col}}") }
                    }
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                        items(projects.filter{it.status==col}, key={it.id}) { proj ->
                            Card(
                                Modifier.fillMaxWidth()
                                    .pointerInput(proj.id) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = { draggedId = proj.id },
                                            onDragEnd = { draggedId = null; isDragTarget = false },
                                            onDrag = { _, _ -> }
                                        )
                                    }
                                    .clickable { editingProject = proj; showAddDialog = true },
                                elevation = CardDefaults.cardElevation(defaultElevation = if(draggedId==proj.id) 8.dp else 2.dp)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(proj.title, style=MaterialTheme.typography.titleMedium)
                                    Text(proj.client, style=MaterialTheme.typography.bodySmall)
                                    Text(proj.usdConverted, color=MaterialTheme.colorScheme.secondary, style=MaterialTheme.typography.labelSmall)
                                    LinearProgressIndicator(progress={0.6f}, modifier=Modifier.fillMaxWidth().padding(top=6.dp))
                                    Row(Modifier.padding(top=6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        // Move buttons for quick move + drag target drop
                                        ProjectStatus.values().forEach { target ->
                                            if(target != proj.status && target != ProjectStatus.PAID) {
                                                AssistChip(onClick={
                                                    projects = projects.map{ if(it.id==proj.id) it.copy(status=target) else it }
                                                    scope.launch{ db.projectDao().updateStatus(proj.id, target) }
                                                }, label={Text(target.name.take(4))})
                                            }
                                        }
                                    }
                                    Text("Tap to edit • Long press to drag", style=MaterialTheme.typography.labelSmall, color=Color.Gray)
                                }
                            }
                        }
                        item {
                            // Drop zone
                            if(draggedId != null) {
                                Box(Modifier.fillMaxWidth().height(60.dp)
                                    .background(Color(0xFF2DD4BF).copy(alpha=0.2f), MaterialTheme.shapes.small)
                                    .clickable {
                                        val dId = draggedId
                                        if(dId != null) {
                                            projects = projects.map{ if(it.id==dId) it.copy(status=col) else it }
                                            scope.launch{ db.projectDao().updateStatus(dId, col) }
                                            draggedId = null
                                        }
                                    },
                                ) { Text("Drop here to move to ${col.name}", modifier=Modifier.padding(16.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }

    if(showAddDialog) {
        var title by remember(editingProject) { mutableStateOf(editingProject?.title ?: "") }
        var budget by remember(editingProject) { mutableStateOf(editingProject?.budgetZAR?.toString() ?: "") }
        var client by remember(editingProject) { mutableStateOf(editingProject?.client ?: "") }

        AlertDialog(
            onDismissRequest={ showAddDialog=false },
            title={ Text(if(editingProject==null) "New Project" else "Edit Project") },
            text={
                Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value=title, onValueChange={title=it}, label={Text("Title*")})
                    OutlinedTextField(value=client, onValueChange={client=it}, label={Text("Client")})
                    OutlinedTextField(value=budget, onValueChange={budget=it}, label={Text("Budget ZAR")})
                    Text("USD->ZAR conversion via ExchangeRate-API will show automatically: e.g. USD 275 = R${(budget.toDoubleOrNull() ?: 0.0)}", style=MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton={
                Button(onClick={
                    if(title.isNotBlank()) {
                        if(editingProject==null) {
                            val newP = KanbanProject(id=System.currentTimeMillis().toString(), title=title, client=client.ifBlank{"Acme"}, status=ProjectStatus.PROPOSED, budgetZAR=budget.toDoubleOrNull()?:0.0, usdConverted="USD ${(budget.toDoubleOrNull()?:0.0)/18.5} -> R$budget")
                            projects = projects + newP
                        } else {
                            projects = projects.map{ if(it.id==editingProject!!.id) it.copy(title=title, client=client, budgetZAR=budget.toDoubleOrNull()?:it.budgetZAR) else it }
                        }
                        showAddDialog=false
                    }
                }){ Text("Save") }
            },
            dismissButton={
                if(editingProject!=null) {
                    TextButton(onClick={
                        projects = projects.filter{ it.id != editingProject!!.id }
                        showAddDialog=false
                    }){ Text("Delete", color=Color.Red) }
                }
            }
        )
    }
}
