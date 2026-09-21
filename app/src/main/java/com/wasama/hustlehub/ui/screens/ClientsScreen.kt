package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ClientsScreen(nav: NavController) {
    var clients by remember { mutableStateOf(listOf(Pair("Acme Design", "R250/hr - Trust 85%"), Pair("TechStart", "R400/hr - Trust 92%"))) }
    Scaffold(floatingActionButton = { FloatingActionButton(onClick = { clients = clients + ("New Client ${clients.size}" to "R250/hr") }) { Icon(Icons.Default.Add, null) } }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(clients) { (name, meta) ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text(name, style=MaterialTheme.typography.titleMedium); Text(meta, style=MaterialTheme.typography.bodySmall) }
                        Row { IconButton(onClick={}){ Icon(Icons.Default.Call, null) }; IconButton(onClick={}){ Icon(Icons.Default.Email, null) } }
                    }
                }
            }
        }
    }
}
