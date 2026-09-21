package com.wasama.hustlehub.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.wasama.hustlehub.ui.screens.*

data class BottomItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun AppScaffold() {
    val nav = rememberNavController()
    val items = listOf(
        BottomItem("dashboard", "Hustle", Icons.Default.Dashboard),
        BottomItem("clients", "Clients", Icons.Default.People),
        BottomItem("projects", "Board", Icons.Default.ViewKanban),
        BottomItem("stats", "Growth", Icons.Default.EmojiEvents)
    )
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // Hide bottom bar on detail/timer/invoice/login
    val showBottomBar = currentRoute in listOf("dashboard","clients","projects","stats")

    Scaffold(
        bottomBar = {
            if(showBottomBar) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                nav.navigate(item.route) {
                                    popUpTo("dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { pad ->
        NavHost(navController = nav, startDestination = "dashboard", modifier = Modifier.padding(pad)) {
            composable("dashboard") { DashboardScreenFixed(nav) }
            composable("clients") { ClientsScreenFixed(nav) }
            composable("projects") { ProjectsKanbanDragDropScreen(nav) }
            composable("stats") { BadgesScreenCool(nav) }
            composable("projectDetail/{id}") { ProjectDetailFixed(it.arguments?.getString("id") ?: "", nav) }
            composable("timer/{id}") { TimerScreenFixed(it.arguments?.getString("id") ?: "", nav) }
            composable("invoice/{id}") { InvoiceScreenFixed(it.arguments?.getString("id") ?: "", nav) }
            composable("settings") { SettingsScreenFixed(nav) }
        }
    }
}
