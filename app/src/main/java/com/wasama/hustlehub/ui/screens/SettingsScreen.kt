package com.wasama.hustlehub.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.wasama.hustlehub.util.SecurePrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavController) {
    val context = LocalContext.current
    val prefs = remember { SecurePrefs.getEncrypted(context) }
    val auth = FirebaseAuth.getInstance()

    var hourlyRate by remember { mutableStateOf(prefs.getString("hourlyRate", "250") ?: "250") }
    var currency by remember { mutableStateOf(prefs.getString("currency", "ZAR") ?: "ZAR") }
    var defaultDeadlineDays by remember { mutableStateOf(prefs.getInt("defaultDeadline", 7)) }
    var notificationsEnabled by remember { mutableStateOf(prefs.getBoolean("notifications", true)) }
    var offlineSync by remember { mutableStateOf(prefs.getBoolean("offlineSync", true)) }
    var autoConvert by remember { mutableStateOf(prefs.getBoolean("autoConvert", true)) }
    var currentLang by remember { mutableStateOf(prefs.getString("appLang", "en") ?: "en") }
    var profileUrl by remember { mutableStateOf(prefs.getString("profileUrl", "") ?: "") }
    var isUploading by remember { mutableStateOf(false) }

    var showRateDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            isUploading = true
            val uid = auth.currentUser?.uid ?: prefs.getString("uid", "") ?: "temp"
            val ref = FirebaseStorage.getInstance().reference.child("users/$uid/profile.jpg")
            ref.putFile(uri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    profileUrl = downloadUri.toString()
                    prefs.edit().putString("profileUrl", profileUrl).apply()
                    FirebaseFirestore.getInstance().collection("users").document(uid).update("profileUrl", profileUrl)
                    isUploading = false
                }
            }.addOnFailureListener { isUploading = false }
        }
    }

    if (showRateDialog) {
        var tempRate by remember { mutableStateOf(hourlyRate) }
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = { Text("Set Hourly Rate") },
            text = {
                Column {
                    OutlinedTextField(value = tempRate, onValueChange = { tempRate = it }, label = { Text("R/hr") }, singleLine = true)
                    Text("Used to calculate billable: hours * rate = invoice", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = {
                    hourlyRate = tempRate
                    prefs.edit().putString("hourlyRate", tempRate).apply()
                    showRateDialog = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showRateDialog = false }) { Text("Cancel") } }
        )
    }

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Default Currency") },
            text = { Text("Choose currency for budgets. Live conversion via ExchangeRate-API.") },
            confirmButton = {
                Column {
                    Button(onClick = { currency = "ZAR"; prefs.edit().putString("currency", "ZAR").apply(); showCurrencyDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("ZAR - Rand") }
                    Button(onClick = { currency = "USD"; prefs.edit().putString("currency", "USD").apply(); showCurrencyDialog = false }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("USD - Dollar") }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profile & Settings", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } })
        },
        containerColor = Color(0xFF0A0A0A)
    ) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color(0xFF1E1E1E))) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box {
                            if (profileUrl.isNotEmpty()) {
                                AsyncImage(model = profileUrl, contentDescription = "Profile", modifier = Modifier.size(80.dp).clip(CircleShape))
                            } else {
                                val email = auth.currentUser?.email ?: prefs.getString("email", "") ?: "U"
                                Box(Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF2DD4BF)), contentAlignment = Alignment.Center) {
                                    Text(email.firstOrNull()?.uppercase() ?: "U", color = Color.Black, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (isUploading) CircularProgressIndicator(Modifier.align(Alignment.Center).size(40.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(auth.currentUser?.email ?: prefs.getString("email", "") ?: "New Hustler", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Level 1 • Newbie Hustler • 0 XP", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { launcher.launch("image/*") }, colors = ButtonDefaults.buttonColors(Color(0xFF2DD4BF)), modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Upload, null, tint = Color.Black); Spacer(Modifier.width(8.dp))
                            Text(if (profileUrl.isEmpty()) "Upload Profile Picture" else "Change Photo", color = Color.Black)
                        }
                        Text("Cloud Synced Profile Picture", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }

            item { SectionHeader("Account") }
            item {
                SettingsCard {
                    SettingsItem(icon = Icons.Default.Payments, title = "Hourly Rate", subtitle = "R$hourlyRate/hr - used for billable calc", onClick = { showRateDialog = true }, trailing = { Text("R$hourlyRate", color = Color(0xFF2DD4BF)) })
                    HorizontalDivider(color = Color(0xFF2A2A2A))
                    SettingsItem(icon = Icons.Default.CurrencyExchange, title = "Default Currency", subtitle = "$currency - ExchangeRate-API live", onClick = { showCurrencyDialog = true }, trailing = { Text(currency, color = Color(0xFF2DD4BF)) })
                }
            }

            item { SectionHeader("Project Defaults") }
            item {
                SettingsCard {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Default deadline", color = Color.White)
                        Slider(value = defaultDeadlineDays.toFloat(), onValueChange = { defaultDeadlineDays = it.toInt(); prefs.edit().putInt("defaultDeadline", it.toInt()).apply() }, valueRange = 1f..30f, modifier = Modifier.width(120.dp))
                        Text("${defaultDeadlineDays}d", color = Color.White)
                    }
                    HorizontalDivider(color = Color(0xFF2A2A2A))
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("Auto-convert USD→ZAR", color = Color.White); Text("Show USD 275 -> R5000", style = MaterialTheme.typography.labelSmall, color = Color.Gray) }
                        Switch(checked = autoConvert, onCheckedChange = { autoConvert = it; prefs.edit().putBoolean("autoConvert", it).apply() })
                    }
                }
            }

            item { SectionHeader("Language") }
            item {
                SettingsCard {
                    SettingsItem(icon = Icons.Default.Language, title = "App Language", subtitle = when(currentLang) { "af" -> "Afrikaans"; "zu" -> "isiZulu"; else -> "English" }, onClick = {})
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("en" to "EN", "af" to "AF", "zu" to "ZU").forEach { (code, label) ->
                            FilterChip(selected = currentLang == code, onClick = { currentLang = code; prefs.edit().putString("appLang", code).apply(); val l = LocaleListCompat.forLanguageTags(code); AppCompatDelegate.setApplicationLocales(l) }, label = { Text(label) })
                        }
                    }
                    Text("Includes South African languages: AF + ZU", color = Color.Gray, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(16.dp))
                }
            }

            item { SectionHeader("Notifications & Offline") }
            item {
                SettingsCard {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) { Text("Deadline Reminders", color = Color.White); Text("Notify <24h + XP reminder", style = MaterialTheme.typography.labelSmall, color = Color.Gray) }
                        Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it; prefs.edit().putBoolean("notifications", it).apply() })
                    }
                    HorizontalDivider(color = Color(0xFF2A2A2A))
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) { Text("Offline-first Sync", color = Color.White); Text("Room truth + WorkManager sync", style = MaterialTheme.typography.labelSmall, color = Color.Gray) }
                        Switch(checked = offlineSync, onCheckedChange = { offlineSync = it; prefs.edit().putBoolean("offlineSync", it).apply() })
                    }
                    HorizontalDivider(color = Color(0xFF2A2A2A))
                    Button(onClick = {
                        val nm = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                        val channelId = "hustlehub_deadlines"
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            nm.createNotificationChannel(android.app.NotificationChannel(channelId, "Deadlines", android.app.NotificationManager.IMPORTANCE_HIGH))
                        }
                        val notif = androidx.core.app.NotificationCompat.Builder(context, channelId)
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentTitle("⚠️ Deadline Alert!")
                            .setContentText("Project due tomorrow! +50 XP if on time")
                            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH).build()
                        nm.notify(1, notif)
                    }, modifier = Modifier.fillMaxWidth().padding(16.dp), colors = ButtonDefaults.buttonColors(Color(0xFF2A2A2A))) { Text("Test Push Notification", color = Color.White) }
                }
            }

            item { SectionHeader("Security & Data") }
            item {
                SettingsCard {
                    SettingsItem(icon = Icons.Default.Security, title = "Encrypted Storage", subtitle = "Token in EncryptedSharedPreferences, secure keys", onClick = {})
                    HorizontalDivider(color = Color(0xFF2A2A2A))
                    SettingsItem(icon = Icons.Default.Storage, title = "Clear Local Data", subtitle = "Wipe local database and preferences", onClick = { context.deleteDatabase("hustlehub.db"); prefs.edit().clear().apply() })
                    HorizontalDivider(color = Color(0xFF2A2A2A))
                    SettingsItem(icon = Icons.Default.Info, title = "About", subtitle = "HustleHub v1.0 - Freelance Business Manager", onClick = {})
                }
            }

            item {
                Button(onClick = { FirebaseAuth.getInstance().signOut(); prefs.edit().clear().apply(); nav.navigate("login") { popUpTo(0) { inclusive = true } } }, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) { Icon(Icons.Default.Logout, null); Spacer(Modifier.width(8.dp)); Text("Logout") }
                Spacer(Modifier.height(16.dp))
                Text("HustleHub v1.0 - Hustle Smart, Grow Fast", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) { Text(title, style = MaterialTheme.typography.titleSmall, color = Color(0xFF2DD4BF), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }

@Composable
fun SettingsCard(content: @Composable (ColumnScope.() -> Unit)) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(Color(0xFF1E1E1E)), content = content) }

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, trailing: @Composable (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(title, color = Color.White, style = MaterialTheme.typography.bodyMedium); Text(subtitle, color = Color.Gray, style = MaterialTheme.typography.labelSmall) }
        }
        trailing?.invoke() ?: Icon(Icons.Default.ChevronRight, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
    }
}
