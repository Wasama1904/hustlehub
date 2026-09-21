package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wasama.hustlehub.data.local.ProjectStatus

data class KanbanProject(val id: String, val title: String, val client: String, var status: ProjectStatus, val budgetZAR: Double, val usdConverted: String)

@Composable
fun ProjectsKanbanScreen(nav: NavController) {
    var projects by remember { mutableStateOf(listOf(
        KanbanProject("1","Logo Redesign","Acme", ProjectStatus.PROPOSED, 5000.0, "USD 275 -> R5000"),
        KanbanProject("2","Website Build","TechStart", ProjectStatus.IN_PROGRESS, 12000.0, "USD 650 -> R12000"),
        KanbanProject("3","Invoice Paid","Acme", ProjectStatus.PAID, 3000.0, "R3000")
    )) }

    val columns = listOf(ProjectStatus.PROPOSED, ProjectStatus.IN_PROGRESS, ProjectStatus.DONE, ProjectStatus.INVOICED, ProjectStatus.PAID)

    LazyRow(Modifier.fillMaxSize().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(columns) { col ->
            Column(Modifier.width(300.dp).fillMaxHeight().background(Color(0xFFF1F5F3), shape = MaterialTheme.shapes.medium).padding(8.dp)) {
                Text(col.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(projects.filter { it.status == col }) { proj ->
                        Card(
                            onClick = { nav.navigate("projectDetail/${proj.id}") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(proj.title, style = MaterialTheme.typography.titleMedium)
                                Text(proj.client, style = MaterialTheme.typography.bodySmall)
                                Text(proj.usdConverted, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall)
                                LinearProgressIndicator(progress = { 0.6f }, modifier = Modifier.fillMaxWidth().padding(top=6.dp))
                                Text("2d left - +10 XP", style = MaterialTheme.typography.labelSmall)
                                Row(Modifier.padding(top=6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(onClick = { projects = projects.map { if(it.id==proj.id) it.copy(status=nextStatus(it.status)) else it } }, contentPadding = PaddingValues(horizontal=8.dp)) { Text("Move ->", style=MaterialTheme.typography.labelSmall) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun nextStatus(s: ProjectStatus): ProjectStatus = when(s){
    ProjectStatus.PROPOSED -> ProjectStatus.IN_PROGRESS
    ProjectStatus.IN_PROGRESS -> ProjectStatus.DONE
    ProjectStatus.DONE -> ProjectStatus.INVOICED
    ProjectStatus.INVOICED -> ProjectStatus.PAID
    ProjectStatus.PAID -> ProjectStatus.PAID
}
