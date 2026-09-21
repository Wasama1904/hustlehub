package com.wasama.hustlehub.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wasama.hustlehub.ui.screens.*

@Composable
fun HustleNav() {
    val nav = rememberNavController()
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topLevelDestinations = listOf("dashboard", "clients", "projects", "stats")
    val showBottomBar = currentRoute in topLevelDestinations

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "dashboard",
                        onClick = { nav.navigate("dashboard") { launchSingleTop = true } },
                        label = { Text("Dash") },
                        icon = { Icon(Icons.Default.Home, null) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "clients",
                        onClick = { nav.navigate("clients") { launchSingleTop = true } },
                        label = { Text("Clients") },
                        icon = { Icon(Icons.Default.Person, null) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "projects",
                        onClick = { nav.navigate("projects") { launchSingleTop = true } },
                        label = { Text("Projects") },
                        icon = { Icon(Icons.Default.List, null) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "stats",
                        onClick = { nav.navigate("stats") { launchSingleTop = true } },
                        label = { Text("Stats") },
                        icon = { Icon(Icons.Default.Star, null) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") { LoginScreen(onLogin = { nav.navigate("dashboard") { popUpTo("login"){ inclusive=true } } }) }
            composable("dashboard") { DashboardScreen(nav) }
            composable("clients") { ClientsScreen(nav) }
            composable("projects") { ProjectsKanbanScreen(nav) }
            composable("projectDetail/{id}") { backStack -> ProjectDetailScreen(backStack.arguments?.getString("id") ?: "", nav) }
            composable("timer/{id}") { backStack -> TimerScreen(backStack.arguments?.getString("id") ?: "", nav) }
            composable("invoice/{id}") { backStack -> InvoiceScreen(backStack.arguments?.getString("id") ?: "", nav) }
            composable("settings") { SettingsScreen(nav) }
            composable("stats") { StatsScreen(nav) }
        }
    }
}
