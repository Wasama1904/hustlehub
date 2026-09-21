package com.wasama.hustlehub.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class BadgeItem(val id: String, val name: String, val desc: String, val icon: ImageVector, val xpReq: Int, val unlocked: Boolean, val rarity: String)

@Composable
fun BadgesScreenCool(nav: NavController) {
    var xp by remember { mutableStateOf(450) }
    val infinite = rememberInfiniteTransition()
    val scale by infinite.animateFloat(initialValue=1f, targetValue=1.15f, animationSpec=infiniteRepeatable(tween(800), RepeatMode.Reverse))

    val badges = listOf(
        BadgeItem("first_client","First Client","Add your first client", Icons.Default.PersonAdd, 0, true, "Common"),
        BadgeItem("first_1000","First R1k","Earn first R1000", Icons.Default.Payments, 100, true, "Common"),
        BadgeItem("5_projects","5 Projects","Complete 5 projects", Icons.Default.Work, 300, false, "Rare"),
        BadgeItem("on_time","On-Time Finisher","Deliver on time 3x", Icons.Default.Timer, 200, xp>=200, "Rare"),
        BadgeItem("night_owl","Night Owl","Work after 10pm", Icons.Default.DarkMode, 150, false, "Epic"),
        BadgeItem("streak_7","Week Warrior","7 day streak", Icons.Default.LocalFireDepartment, 500, false, "Epic"),
        BadgeItem("agency_boss","Agency Boss","Reach Level 10", Icons.Default.EmojiEvents, 2700, false, "Legendary")
    )

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // Header with gradient
        Card(Modifier.fillMaxWidth(), shape=MaterialTheme.shapes.large) {
            Box(Modifier.background(Brush.linearGradient(listOf(Color(0xFF0F2623), Color(0xFF2DD4BF)))).padding(20.dp)) {
                Column {
                    Text("Your Hustle Growth", color=Color.White, style=MaterialTheme.typography.headlineSmall)
                    Text("Level ${(xp/300)+1} • ${xp} XP • ${badges.count{it.unlocked}}/7 Badges", color=Color.White.copy(alpha=0.9f))
                    LinearProgressIndicator(progress={ (xp%300)/300f }, modifier=Modifier.fillMaxWidth().padding(top=12.dp), color=Color.White, trackColor=Color.White.copy(alpha=0.3f))
                    Row(Modifier.padding(top=12.dp), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        Button(onClick={ xp+=100 }, colors=ButtonDefaults.buttonColors(containerColor=Color.White, contentColor=Color(0xFF0F2623))) { Text("+100 XP") }
                        OutlinedButton(onClick={ xp=0 }, colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White)) { Text("Reset") }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Badges", style=MaterialTheme.typography.titleLarge)

        LazyVerticalGrid(columns=GridCells.Fixed(2), verticalArrangement=Arrangement.spacedBy(12.dp), horizontalArrangement=Arrangement.spacedBy(12.dp), modifier=Modifier.padding(top=8.dp)) {
            items(badges) { badge ->
                Card(
                    Modifier.fillMaxWidth(),
                    colors=CardDefaults.cardColors(containerColor= if(badge.unlocked) Color.White else Color(0xFFF1F5F5)),
                    elevation=CardDefaults.cardElevation(defaultElevation= if(badge.unlocked) 4.dp else 0.dp)
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment=Alignment.CenterHorizontally) {
                        Box(Modifier.size(56.dp).background(
                            when(badge.rarity){
                                "Common"->Color(0xFFE0E0E0)
                                "Rare"->Color(0xFFB2EBF2)
                                "Epic"->Color(0xFFE1BEE7)
                                else->Color(0xFFFFE082)
                            }, shape=MaterialTheme.shapes.extraLarge),
                            contentAlignment=Alignment.Center
                        ) {
                            Icon(badge.icon, null, modifier=Modifier.size(32.dp).then(if(badge.unlocked) Modifier.scale(scale) else Modifier), tint=if(badge.unlocked) Color(0xFF0F2623) else Color.Gray)
                        }
                        Text(badge.name, style=MaterialTheme.typography.titleSmall, modifier=Modifier.padding(top=8.dp))
                        Text(badge.desc, style=MaterialTheme.typography.bodySmall, color=Color.Gray)
                        Spacer(Modifier.height(6.dp))
                        if(badge.unlocked) {
                            Badge(containerColor=Color(0xFF2DD4BF)) { Text("UNLOCKED +${badge.xpReq} XP", color=Color.White) }
                        } else {
                            Badge(containerColor=Color(0xFFE0E0E0)) { Text("${badge.rarity} • ${badge.xpReq} XP") }
                        }
                    }
                }
            }
        }
    }
}
