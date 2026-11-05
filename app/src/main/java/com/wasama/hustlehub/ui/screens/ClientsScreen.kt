package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(nav: NavController) {
    val context = LocalContext.current
    val db = remember { HustleHubDatabase.get(context) }
    var clients by remember { mutableStateOf<List<com.wasama.hustlehub.data.local.ClientEntity>>(emptyList()) }
    var showAdd by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.clientDao().getAllClients().collect { list ->
            clients = list
        }
    }

    if (clients.isEmpty()) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Clients", fontWeight = FontWeight.Bold) }, actions = { IconButton(onClick = { nav.navigate("settings") }) { Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White) } }) },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAdd = true }, containerColor = Color(0xFF2DD4BF)) {
                    Icon(Icons.Default.Add, null, tint = Color.Black)
                }
            }
        ) { pad ->
            Box(Modifier.padding(pad).fillMaxSize()) {
                ProductionEmptyState(
                    icon = Icons.Default.People,
                    title = "No clients yet",
                    subtitle = "Your first client is your first hustle. Add them to start tracking projects and invoices.",
                    ctaText = "Add Your First Client"
                ) { showAdd = true }
            }
        }
        if (showAdd) { AddClientDialogProduction(onDismiss = { showAdd = false }, db = db) }
        return
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Clients (${clients.size})", fontWeight = FontWeight.Bold) }, actions = { IconButton(onClick = { nav.navigate("settings") }) { Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White) } }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }, containerColor = Color(0xFF2DD4BF)) {
                Icon(Icons.Default.Add, null, tint = Color.Black)
            }
        }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(clients, key = { it.clientId }) { client ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(Color(0xFF1E1E1E))) {
                    Column(Modifier.padding(16.dp)) {
                        Text(client.name, color = Color.White, fontWeight = FontWeight.Bold)
                        if (!client.company.isNullOrBlank()) Text(client.company, color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                        if (client.email.isNotBlank()) Text(client.email, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!client.phone.isNullOrBlank()) {
                                FilledTonalButton(onClick = {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${client.phone}"))
                                    context.startActivity(intent)
                                }) {
                                    Icon(Icons.Default.Call, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Call")
                                }
                            } else {
                                OutlinedButton(onClick = { /* edit */ }) { Text("Add Phone") }
                            }
                        }
                    }
                }
            }
        }
        if (showAdd) { AddClientDialogProduction(onDismiss = { showAdd = false }, db = db) }
    }
}

@Composable
fun AddClientDialogProduction(onDismiss: () -> Unit, db: HustleHubDatabase) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Client") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name*") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("All fields start empty", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    val client = com.wasama.hustlehub.data.local.ClientEntity(name = name, phone = phone.ifBlank { null }, email = email, company = company.ifBlank { null })
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        try { db.clientDao().insert(client) } catch (_: Exception) {}
                    }
                    onDismiss()
                }
            ) { Text("Add Client") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
