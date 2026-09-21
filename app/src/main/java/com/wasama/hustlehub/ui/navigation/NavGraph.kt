package com.wasama.hustlehub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wasama.hustlehub.ui.screens.*

@Composable
fun HustleNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "login") {
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
