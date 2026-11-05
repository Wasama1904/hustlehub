package com.wasama.hustlehub.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wasama.hustlehub.data.local.HustleHubDatabase
import com.wasama.hustlehub.data.local.ProjectStatus
import com.wasama.hustlehub.data.local.UserStatsEntity
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.combine

data class BadgeProduction(
    val id: String,
    val name: String,
    val description: String,
    val xpReward: Int,
    val rarity: String,
    val isUnlocked: Boolean,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun BadgesScreenCool(nav: NavController) {
    val context = LocalContext.current
    val db = remember { HustleHubDatabase.get(context) }
    var stats by remember { mutableStateOf(UserStatsEntity("current_user")) }
    var clientsCount by remember { mutableStateOf(0) }
    var projectsCompleted by remember { mutableStateOf(0) }
    var tasksCompleted by remember { mutableStateOf(0) }
    var totalEarned by remember { mutableStateOf(0) }
    var onTimeCount by remember { mutableStateOf(0) }
    var currentStreak by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        try {
            combine(
                db.userStatsDao().getStats("current_user"),
                db.clientDao().getAllClients(),
                db.projectDao().getProjects("current_user"),
                db.invoiceDao().getInvoices("current_user")
            ) { userStats, clients, projects, invoices ->
                val s = userStats ?: UserStatsEntity("current_user")
                val cCount = clients.size
                val pCompleted = projects.count { it.status == ProjectStatus.PAID }
                val tCompleted = projects.sumOf { p -> try { p.getTasksListSafe().count { it.isCompleted } } catch(_:Exception){0} }
                val earned = invoices.filter { it.isPaid }.sumOf { it.amount }.toInt()
                val onTime = projects.count { it.status == ProjectStatus.PAID }
                val streak = s.currentStreak
                
                Triple(s, Triple(cCount, pCompleted, tCompleted), Pair(earned, Pair(onTime, streak)))
            }.collect { (s, counts, rest) ->
                stats = s
                clientsCount = counts.first
                projectsCompleted = counts.second
                tasksCompleted = counts.third
                totalEarned = rest.first
                onTimeCount = rest.second.first
                currentStreak = rest.second.second
            }
        } catch (_: Exception) {}
    }

    GrowthScreenProduction(
        nav = nav,
        stats = stats,
        clientsCount = clientsCount,
        projectsCompleted = projectsCompleted,
        tasksCompleted = tasksCompleted,
        totalEarned = totalEarned,
        onTimeCount = onTimeCount,
        currentStreak = currentStreak
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthScreenProduction(
    nav: NavController,
    stats: UserStatsEntity,
    clientsCount: Int,
    projectsCompleted: Int,
    tasksCompleted: Int,
    totalEarned: Int,
    onTimeCount: Int,
    currentStreak: Int
) {
    val badges = remember(clientsCount, projectsCompleted, tasksCompleted, totalEarned, currentStreak, onTimeCount) {
        listOf(
            BadgeProduction("first_client", "First Client", "Add your first client", 50, "Common", clientsCount >= 1, Icons.Default.PersonAdd),
            BadgeProduction("first_1k", "First R1k", "Earn your first R1,000", 100, "Rare", totalEarned >= 1000, Icons.Default.Payments),
            BadgeProduction("five_projects", "5 Projects", "Complete 5 projects", 200, "Rare", projectsCompleted >= 5, Icons.Default.TaskAlt),
            BadgeProduction("on_time", "On-Time Finisher", "Deliver a project on time", 150, "Epic", onTimeCount >= 1, Icons.Default.Timer),
            BadgeProduction("night_owl", "Night Owl", "Work after 10pm", 75, "Common", false, Icons.Default.NightsStay),
            BadgeProduction("week_warrior", "Week Warrior", "Maintain 7-day streak", 300, "Epic", currentStreak >= 7, Icons.Default.LocalFireDepartment),
            BadgeProduction("agency_boss", "Agency Boss", "Manage 10 clients", 500, "Legendary", clientsCount >= 10, Icons.Default.Business)
        )
    }

    val unlockedCount = badges.count { it.isUnlocked }
    val totalXp = stats.totalXp
    val level = (totalXp / 300) + 1
    val xpForNextLevel = level * 300
    val xpInCurrentLevel = totalXp % 300
    val progress = xpInCurrentLevel / 300f

    val infiniteScale by rememberInfiniteTransition(label = "badge").animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse),
        label = "scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Growth", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0A0A))
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad).background(Color(0xFF0A0A0A)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (totalXp == 0 && projectsCompleted == 0 && tasksCompleted == 0) {
                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier.size(96.dp).clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Color(0xFF2DD4BF), Color(0xFF14B8A6)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.RocketLaunch, null, Modifier.size(48.dp), tint = Color.White)
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Your hustle starts now", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Complete tasks, finish projects on time, and get paid to earn XP. Every action moves you from Newbie to Legend.",
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { nav.navigate("projects") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4BF)),
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, null, tint = Color.Black); Spacer(Modifier.width(8.dp))
                                Text("Start Your First Project", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("Level 1 • 0 XP • ${unlockedCount}/7 badges", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        Modifier.fillMaxWidth().background(
                            Brush.linearGradient(listOf(Color(0xFF0F2623), Color(0xFF1E1E1E)))
                        ).padding(20.dp)
                    ) {
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Level $level", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        when(level) {
                                            1 -> "Newbie Hustler"
                                            2 -> "Rising Freelancer"
                                            3 -> "Pro Freelancer"
                                            4 -> "Agency Builder"
                                            5 -> "Agency Boss"
                                            else -> "Legend"
                                        },
                                        color = Color(0xFF2DD4BF),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                Box(
                                    Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF2DD4BF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("$level", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("$xpInCurrentLevel / 300 XP", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                                Text("${xpForNextLevel - totalXp} XP to Level ${level+1}", color = Color(0xFF2DD4BF), style = MaterialTheme.typography.labelMedium)
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF2DD4BF),
                                trackColor = Color(0xFF2A2A2A)
                            )
                        }
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCardProduction("Projects", "$projectsCompleted", Icons.Default.Folder, Modifier.weight(1f))
                    StatCardProduction("Tasks", "$tasksCompleted", Icons.Default.CheckCircle, Modifier.weight(1f))
                    StatCardProduction("Earned", "R$totalEarned", Icons.Default.AttachMoney, Modifier.weight(1f))
                }
            }

            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(Color(0xFF1E1E1E))) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, null, tint = if(currentStreak>0) Color(0xFFFF6B35) else Color.Gray, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("$currentStreak day streak", color = Color.White, fontWeight = FontWeight.Bold)
                                Text(if(currentStreak==0) "Complete a task daily to build streak" else "Keep going!", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        if(stats.freezeTokens > 0) {
                            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(Color(0xFF2A2A2A))) {
                                Row(Modifier.padding(horizontal=8.dp, vertical=4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AcUnit, null, Modifier.size(14.dp), tint = Color(0xFF2DD4BF))
                                    Spacer(Modifier.width(4.dp))
                                    Text("${stats.freezeTokens} freeze", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Badges", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("$unlockedCount/7 unlocked", color = Color(0xFF2DD4BF), style = MaterialTheme.typography.labelMedium)
                }
            }

            items(badges.chunked(2)) { rowBadges ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowBadges.forEach { badge ->
                        BadgeCardProduction(badge, infiniteScale, Modifier.weight(1f))
                    }
                    if(rowBadges.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "HustleHub • Earn XP: 10 per task, 60 on-time bonus, 100 invoice paid • Level = XP/300 + 1",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StatCardProduction(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(Color(0xFF1E1E1E))) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(title, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun BadgeCardProduction(badge: BadgeProduction, scale: Float, modifier: Modifier) {
    val rarityColor = when(badge.rarity) {
        "Common" -> Color(0xFF9CA3AF)
        "Rare" -> Color(0xFF3B82F6)
        "Epic" -> Color(0xFF8B5CF6)
        "Legendary" -> Color(0xFFF59E0B)
        else -> Color.Gray
    }
    Card(
        modifier.then(if(badge.isUnlocked) Modifier.scale(scale) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if(badge.isUnlocked) Color(0xFF1E1E1E) else Color(0xFF121212))
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(48.dp).clip(CircleShape)
                    .background(if(badge.isUnlocked) rarityColor.copy(alpha=0.2f) else Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(badge.icon, null, Modifier.size(24.dp), tint = if(badge.isUnlocked) rarityColor else Color.Gray)
            }
            Spacer(Modifier.height(8.dp))
            Text(badge.name, color = if(badge.isUnlocked) Color.White else Color.Gray, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
            Text(badge.description, color = Color.Gray, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, maxLines = 2)
            Spacer(Modifier.height(6.dp))
            Card(shape = RoundedCornerShape(6.dp), colors = CardDefaults.cardColors(if(badge.isUnlocked) rarityColor.copy(0.2f) else Color(0xFF2A2A2A))) {
                Text(badge.rarity, color = if(badge.isUnlocked) rarityColor else Color.Gray, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal=6.dp, vertical=2.dp))
            }
            if(badge.isUnlocked) {
                Spacer(Modifier.height(4.dp))
                Text("+${badge.xpReward} XP", color = Color(0xFF2DD4BF), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            } else {
                Spacer(Modifier.height(4.dp))
                Icon(Icons.Default.Lock, null, Modifier.size(12.dp), tint = Color.Gray)
            }
        }
    }
}
