package com.wasama.hustlehub.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wasama.hustlehub.data.local.ProjectStatus
import java.util.UUID

data class ProjectFull(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val client: String,
    val description: String = "",
    var status: ProjectStatus = ProjectStatus.PROPOSED,
    val budgetZAR: Double = 5000.0,
    val usdConverted: String = "USD 275 -> R5000",
    val tasks: List<String> = emptyList(),
    val deadline: String = "2d left"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsKanbanScreen(nav: NavController) {
    var projects by remember { mutableStateOf(listOf(
        ProjectFull(title="Logo Redesign", client="Acme", description="Redesign logo for Acme's new brand identity including color palette and typography.", status=ProjectStatus.PROPOSED, tasks=listOf("Moodboard","Draft 1","Client review","Final export","Brand guide")),
        ProjectFull(title="Social Kit", client="TechStart", description="Create social media kit with 20 templates for Instagram and LinkedIn.", status=ProjectStatus.PROPOSED, tasks=listOf("Research","Templates","Review")),
        ProjectFull(title="Website Build", client="TechStart", description="Build responsive landing page with CMS.", status=ProjectStatus.IN_PROGRESS, budgetZAR=12000.0, usdConverted="USD 650 -> R12000")
    )) }

    var showAdd by remember { mutableStateOf(false) }

    // Add Project Dialog with description + 10 tasks
    if (showAdd) {
        var title by remember { mutableStateOf("") }
        var client by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var tasksText by remember { mutableStateOf("") } // comma or newline separated

        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add Project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title *") }, singleLine = true)
                    OutlinedTextField(value = client, onValueChange = { client = it }, label = { Text("Client") }, singleLine = true)
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, minLines = 3, maxLines = 5, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = tasksText, onValueChange = { tasksText = it }, label = { Text("Tasks (max 10, one per line)") }, placeholder = { Text("e.g.\nDesign draft\nReview\nExport") }, minLines = 4, maxLines = 10, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    val taskList = tasksText.lines().map { it.trim() }.filter { it.isNotBlank() }.take(10)
                    if (title.isBlank()) return@Button
                    projects = projects + ProjectFull(title=title, client=client, description=description, tasks=taskList)
                    showAdd = false
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }

    val columns = listOf(ProjectStatus.PROPOSED, ProjectStatus.IN_PROGRESS, ProjectStatus.DONE, ProjectStatus.INVOICED, ProjectStatus.PAID)

    Scaffold(floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }, containerColor = MaterialTheme.colorScheme.secondaryContainer) { Icon(Icons.Default.Add, null) } }) { pad ->
        LazyRow(Modifier.fillMaxSize().padding(pad).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(columns) { col ->
                Column(Modifier.width(320.dp).fillMaxHeight().background(Color(0xFF101918), shape = RoundedCornerShape(16.dp)).padding(12.dp)) {
                    Text(col.name.replace("_"," "), style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.padding(8.dp))
                    Divider(color = Color(0xFF2A3C38))
                    Spacer(Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                        items(projects.filter { it.status == col }, key = { it.id }) { proj ->
                            Card(
                                onClick = { nav.navigate("projectDetail/${proj.id}") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2E2A))
                            ) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(proj.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                                    Text("Client: ${proj.client}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA8C5BE))
                                    Text("Budget: R${proj.budgetZAR} (${proj.usdConverted})", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5EE9D1))
                                    Text("Deadline: ${proj.deadline}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF7ED8C6))

                                    if (proj.description.isNotBlank()) {
                                        Text(proj.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFFD1E8E2), maxLines = 3)
                                    }

                                    if (proj.tasks.isNotEmpty()) {
                                        Text("Tasks: ${proj.tasks.size}/10", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                        proj.tasks.take(3).forEach { t -> Text("• $t", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA8C5BE)) }
                                        if (proj.tasks.size > 3) Text("+${proj.tasks.size-3} more - tap to view", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5EE9D1))
                                    }

                                    Spacer(Modifier.height(8.dp))
                                    // Status move row - spaced properly now
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        listOf("PROPOSED","IN_P","DONE").forEach { label ->
                                            val target = when(label) {
                                                "PROPOSED" -> ProjectStatus.PROPOSED
                                                "IN_P" -> ProjectStatus.IN_PROGRESS
                                                else -> ProjectStatus.DONE
                                            }
                                            FilterChip(
                                                selected = proj.status == target,
                                                onClick = { projects = projects.map { if(it.id==proj.id) it.copy(status=target) else it } },
                                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                                modifier = Modifier.padding(end=4.dp)
                                            )
                                        }
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        FilterChip(selected = proj.status==ProjectStatus.INVOICED, onClick = { projects = projects.map { if(it.id==proj.id) it.copy(status=ProjectStatus.INVOICED) else it } }, label = { Text("INVOICED") })
                                        FilterChip(selected = proj.status==ProjectStatus.PAID, onClick = { projects = projects.map { if(it.id==proj.id) it.copy(status=ProjectStatus.PAID) else it } }, label = { Text("PAID") })
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
