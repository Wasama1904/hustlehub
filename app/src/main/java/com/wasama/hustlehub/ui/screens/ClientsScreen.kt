package com.wasama.hustlehub.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.UUID

data class ClientUi(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val company: String = "",
    val email: String = "",
    val phone: String = "", // NEW: phone number field
    val rate: Double = 250.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(nav: NavController) {
    val context = LocalContext.current
    var clients by remember { mutableStateOf(listOf(
        ClientUi(name="Acme Design", company="Acme", email="acme@design.com", phone="0821234567", rate=250.0),
        ClientUi(name="TechStart", company="TechStart", email="hello@techstart.co.za", phone="", rate=400.0)
    )) }
    var showAdd by remember { mutableStateOf(false) }

    // Add Client Dialog with phone
    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var company by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var rate by remember { mutableStateOf("250") }

        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add Client") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name *") }, singleLine = true)
                    OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company") }, singleLine = true)
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true, placeholder = { Text("e.g. 0821234567") })
                    OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("Rate R/hr") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isBlank()) { Toast.makeText(context, "Name required", Toast.LENGTH_SHORT).show(); return@Button }
                    clients = clients + ClientUi(name=name, company=company, email=email, phone=phone, rate=rate.toDoubleOrNull()?:250.0)
                    showAdd = false
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Clients") }) },
        floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, null) } }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(clients, key = { it.id }) { client ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(client.name, style = MaterialTheme.typography.titleMedium)
                        Text("${client.company} • R${client.rate}/hr", style = MaterialTheme.typography.bodySmall)
                        if (client.email.isNotBlank()) Text(client.email, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        if (client.phone.isNotBlank()) Text("📞 ${client.phone}", style = MaterialTheme.typography.labelSmall) else Text("No phone added", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)

                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // CALL - now functional
                            FilledTonalButton(onClick = {
                                if (client.phone.isBlank()) {
                                    Toast.makeText(context, "No contact number added", Toast.LENGTH_SHORT).show()
                                } else {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${client.phone}"))
                                    context.startActivity(intent)
                                }
                            }) { Icon(Icons.Default.Call, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Call") }

                            // EDIT - placeholder, you can open edit dialog
                            OutlinedButton(onClick = { /* TODO: edit dialog similar to add */ }) { Icon(Icons.Default.Edit, null, modifier=Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Edit") }

                            // DELETE
                            OutlinedButton(onClick = { clients = clients.filter { it.id != client.id } }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                                Icon(Icons.Default.Delete, null, modifier=Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
