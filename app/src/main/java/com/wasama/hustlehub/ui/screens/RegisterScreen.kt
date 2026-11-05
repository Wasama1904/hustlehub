package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.wasama.hustlehub.data.local.HustleHubDatabase
import com.wasama.hustlehub.data.local.UserStatsEntity
import com.wasama.hustlehub.util.SecurePrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(onRegistered: () -> Unit, onNavigateToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Create Account", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
        Text("Join HustleHub - Free forever", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(24.dp))

        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = password, onValueChange = { password = it }, label = { Text("Password (min 6)") }, singleLine = true,
                    visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = { IconButton(onClick = { showPass = !showPass }) { Icon(if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

                Button(onClick = {
                    if (email.isBlank() || password.length < 6) { error = "Password must be >=6"; return@Button }
                    loading = true
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.trim(), password)
                        .addOnSuccessListener { result ->
                            val prefs = SecurePrefs.getEncrypted(context)
                            val uid = result.user?.uid ?: ""
                            prefs.edit().putString("uid", uid).putString("hourlyRate", "250").putString("email", email).apply()
                            
                            val db = HustleHubDatabase.get(context)
                            scope.launch(Dispatchers.IO) {
                                try {
                                    db.clearAllTables()
                                    db.userStatsDao().insert(UserStatsEntity(uid = uid, totalXp = 0, level = 1, freezeTokens = 1))
                                    com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid).set(
                                        mapOf("email" to email, "createdAt" to System.currentTimeMillis(), "hourlyRate" to 250, "currency" to "ZAR")
                                    )
                                } catch (_: Exception) {}
                            }
                            loading = false
                            onRegistered()
                        }
                        .addOnFailureListener { e -> loading = false; error = e.message }
                }, modifier = Modifier.fillMaxWidth().height(48.dp), enabled = !loading) {
                    if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Register")
                }
                TextButton(onClick = onNavigateToLogin, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Already have account? Sign In") }
            }
        }
    }
}
