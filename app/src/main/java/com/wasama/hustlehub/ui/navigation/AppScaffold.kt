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
        NavHost(navController = nav, startDestination = "login", modifier = Modifier.padding(pad)) {
            composable("login") { LoginScreen(onLogin = { nav.navigate("dashboard") { popUpTo("login") { inclusive = true } } }, onNavigateToRegister = { nav.navigate("register") }) }
            composable("register") { RegisterScreen(onRegistered = { nav.navigate("dashboard") { popUpTo("register") { inclusive = true } } }, onNavigateToLogin = { nav.popBackStack() }) }
            composable("dashboard") { DashboardScreenFixed(nav) }
            composable("clients") { ClientsScreen(nav) }
            composable("projects") { ProjectsKanbanScreen(nav) }
            composable("stats") { BadgesScreenCool(nav) }
            composable("projectDetail/{id}") { ProjectDetailScreen(it.arguments?.getString("id") ?: "", nav) }
            composable("timer/{id}") { TimerScreenFixed(it.arguments?.getString("id") ?: "", nav) }
            composable("invoice/{id}") { InvoiceScreenFixed(it.arguments?.getString("id") ?: "", nav) }
            composable("settings") { SettingsScreen(nav) }
        }
    }
}
