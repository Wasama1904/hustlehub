package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wasama.hustlehub.data.local.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreenFixed(nav: NavController) {
    val context = LocalContext.current
    val db = remember { HustleHubDatabase.get(context) }
    val scope = rememberCoroutineScope()
    var clients by remember { mutableStateOf(listOf<ClientEntity>()) }
    var showDialog by remember { mutableStateOf(false) }
    var editClient by remember { mutableStateOf<ClientEntity?>(null) }

    // Load clients
    LaunchedEffect(Unit) {
        db.clientDao().getClients("current_user").collect { clients = it }
        // Fallback demo data if empty
        if(clients.isEmpty()) {
            clients = listOf(
                ClientEntity(userId="current_user", name="Acme Design", email="hello@acme.com", company="Acme", ratePerHour=250.0, trustScore=85),
                ClientEntity(userId="current_user", name="TechStart", email="founder@techstart.io", company="TechStart", ratePerHour=400.0, trustScore=92)
            )
        }
    }

    Scaffold(
        topBar = { TopAppBar(title={ Text("Clients (${clients.size})") }, actions={ IconButton(onClick={ nav.navigate("settings") }){ Icon(Icons.Default.Settings, null) } }) },
        floatingActionButton = { FloatingActionButton(onClick={ editClient=null; showDialog=true }){ Icon(Icons.Default.Add, null) } }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(clients, key={ it.clientId }) { client ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(client.name, style=MaterialTheme.typography.titleMedium)
                            Text("${client.company ?: ""} • R${client.ratePerHour}/hr • Trust ${client.trustScore}%", style=MaterialTheme.typography.bodySmall)
                            Text(client.email, style=MaterialTheme.typography.labelSmall)
                        }
                        Row {
                            IconButton(onClick={ /* call intent */ }){ Icon(Icons.Default.Call, null) }
                            IconButton(onClick={ editClient=client; showDialog=true }){ Icon(Icons.Default.Edit, null) }
                            IconButton(onClick={ scope.launch{ db.clientDao().delete(client); clients = clients.filter { it.clientId != client.clientId } } }){ Icon(Icons.Default.Delete, null) }
                        }
                    }
                }
            }
        }
    }

    if(showDialog) {
        var name by remember(editClient) { mutableStateOf(editClient?.name ?: "") }
        var email by remember(editClient) { mutableStateOf(editClient?.email ?: "") }
        var company by remember(editClient) { mutableStateOf(editClient?.company ?: "") }
        var rate by remember(editClient) { mutableStateOf((editClient?.ratePerHour ?: 250.0).toString()) }

        AlertDialog(
            onDismissRequest = { showDialog=false },
            title = { Text(if(editClient==null) "Add Client" else "Edit Client") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value=name, onValueChange={name=it}, label={Text("Name*")})
                    OutlinedTextField(value=email, onValueChange={email=it}, label={Text("Email*")})
                    OutlinedTextField(value=company, onValueChange={company=it}, label={Text("Company")})
                    OutlinedTextField(value=rate, onValueChange={rate=it}, label={Text("Rate R/hr")})
                }
            },
            confirmButton = {
                Button(onClick={
                    if(name.length>=2 && email.contains("@")) {
                        val newClient = (editClient ?: ClientEntity(userId="current_user", name=name, email=email)).copy(
                            name=name, email=email, company=company.ifBlank{null}, ratePerHour=rate.toDoubleOrNull()?:250.0
                        )
                        scope.launch { db.clientDao().insert(newClient) }
                        clients = if(editClient==null) clients + newClient else clients.map { if(it.clientId==newClient.clientId) newClient else it }
                        showDialog=false
                    }
                }){ Text("Save") }
            },
            dismissButton = { TextButton(onClick={showDialog=false}){ Text("Cancel") } }
        )
    }
}
