package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SettingsScreen(nav: NavController) {
    var rate by remember { mutableStateOf("250") }
    var currency by remember { mutableStateOf("ZAR") }
    var dark by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("Hourly Rate R/hr") }, modifier = Modifier.fillMaxWidth())
        Row { Text("Default Currency: "); DropdownMenuBox(currency) { currency = it } }
        Row { Text("Dark Theme"); Switch(checked = dark, onCheckedChange = { dark = it }) }
        Text("Offline-first: Room is source of truth, WorkManager syncs to Firestore when online. Critical for load-shedding.", style = MaterialTheme.typography.bodySmall)
        Text("Security: Token stored in EncryptedSharedPreferences, API keys in local.properties not Git", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { nav.navigate("login") { popUpTo(0) } }, modifier = Modifier.fillMaxWidth()) { Text("Logout") }
    }
}

@Composable
fun DropdownMenuBox(current: String, onSelect: (String)->Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) { Text(current) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("ZAR") }, onClick = { onSelect("ZAR"); expanded = false })
            DropdownMenuItem(text = { Text("USD") }, onClick = { onSelect("USD"); expanded = false })
        }
    }
}
