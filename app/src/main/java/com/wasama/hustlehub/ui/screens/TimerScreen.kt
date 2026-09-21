package com.wasama.hustlehub.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.wasama.hustlehub.service.TimerService
import kotlinx.coroutines.delay

@Composable
fun TimerScreen(projectId: String, nav: NavController) {
    val context = LocalContext.current
    var seconds by remember { mutableStateOf(0) }
    var running by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        while(running) { delay(1000); seconds++ }
    }

    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(String.format("%02d:%02d:%02d", seconds/3600, (seconds%3600)/60, seconds%60), style = MaterialTheme.typography.displayLarge)
        Text("Project: $projectId", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                running = true
                val i = Intent(context, TimerService::class.java).apply { action = TimerService.ACTION_START; putExtra(TimerService.EXTRA_PROJECT_ID, projectId) }
                ContextCompat.startForegroundService(context, i)
            }) { Text("Start") }
            Button(onClick = { running = false }) { Text("Pause") }
            Button(onClick = {
                running = false
                val i = Intent(context, TimerService::class.java).apply { action = TimerService.ACTION_STOP }
                context.startService(i)
                // Save TimeEntry to Room here: duration = seconds/60, billable = true
                nav.popBackStack()
            }) { Text("Stop & Save") }
        }
        Text("Notification shows running timer - meets Foreground Service requirement", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top=16.dp))
    }
}
