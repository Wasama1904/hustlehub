package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable fun ProjectDetailFixed(id: String, nav: NavController) {
    // Reuse previous but with back nav that doesn't lose dashboard
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Project Detail $id", style=MaterialTheme.typography.headlineSmall)
        Text("This screen now has back button to Board, not stuck. Use top app bar back.")
        Button(onClick={ nav.popBackStack() }){ Text("Back to Board") }
        Button(onClick={ nav.navigate("timer/$id") }){ Text("Start Timer") }
        Button(onClick={ nav.navigate("invoice/$id") }){ Text("Invoice") }
    }
}
@Composable fun TimerScreenFixed(id: String, nav: NavController) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Timer for $id")
        Button(onClick={ nav.popBackStack() }){ Text("Back") }
    }
}
@Composable fun InvoiceScreenFixed(id: String, nav: NavController) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Invoice $id")
        Button(onClick={ nav.popBackStack() }){ Text("Back") }
    }
}
@Composable fun SettingsScreenFixed(nav: NavController) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", style=MaterialTheme.typography.headlineSmall)
        Button(onClick={ nav.popBackStack() }){ Text("Back to Dashboard") }
    }
}
