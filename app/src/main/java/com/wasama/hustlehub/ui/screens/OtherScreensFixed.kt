package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wasama.hustlehub.data.local.*
import com.wasama.hustlehub.util.InvoicePdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable fun ProjectDetailFixed(id: String, nav: NavController) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreenFixed(projectId: String, nav: NavController) {
    val context = LocalContext.current
    val db = remember { HustleHubDatabase.get(context) }
    var project by remember { mutableStateOf<ProjectEntity?>(null) }
    var client by remember { mutableStateOf<ClientEntity?>(null) }
    var totalMinutes by remember { mutableStateOf(0) }
    var generatedFile by remember { mutableStateOf<File?>(null) }
    var isPaid by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(projectId) {
        val proj = db.projectDao().getById(projectId)
        project = proj
        if (proj != null && proj.clientId.isNotBlank()) {
            client = db.clientDao().getById(proj.clientId)
        }
        val mins = db.timeDao().totalMinutes(projectId) ?: 0
        totalMinutes = mins
    }

    val hours = totalMinutes / 60.0
    val rate = client?.ratePerHour ?: 250.0
    val amount = if (project?.budgetAmount != null && project?.budgetAmount!! > 0.0) project?.budgetAmount!! else (hours * rate).coerceAtLeast(250.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice for Project", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0A0A))
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (project == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2DD4BF))
                }
            } else {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("HustleHub Professional Invoice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF2DD4BF))
                        HorizontalDivider(color = Color(0xFF2A2A2A))
                        Text("Project: ${project?.title}", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Client: ${client?.name ?: "General Client"} (${client?.company ?: "Independent"})", color = Color.Gray)
                        Text("Tracked Time: $totalMinutes mins (%.1f hrs)".format(hours), color = Color.Gray)
                        Text("Hourly Rate: R$rate/hr", color = Color.Gray)
                        HorizontalDivider(color = Color(0xFF2A2A2A))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Amount:", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("R${amount.toInt()}", color = Color(0xFF2DD4BF), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }

                successMessage?.let {
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2623))
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2DD4BF))
                            Spacer(Modifier.width(8.dp))
                            Text(it, color = Color.White)
                        }
                    }
                }

                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val c = client ?: ClientEntity(name = "Client", email = "", company = "")
                                val p = project!!
                                val file = InvoicePdf.generate(context, c, p, amount, hours)
                                generatedFile = file
                                
                                val invoice = InvoiceEntity(
                                    projectId = p.projectId,
                                    clientId = c.clientId,
                                    amount = amount,
                                    currency = "ZAR",
                                    isPaid = isPaid,
                                    pdfFilePath = file.absolutePath
                                )
                                db.invoiceDao().insert(invoice)
                                db.projectDao().updateStatus(p.projectId, ProjectStatus.INVOICED)

                                scope.launch(Dispatchers.Main) {
                                    successMessage = "Invoice PDF successfully generated and saved!"
                                }
                            } catch (e: Exception) {
                                scope.launch(Dispatchers.Main) {
                                    successMessage = "Error generating PDF: ${e.message}"
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4BF))
                ) {
                    Icon(Icons.Default.PictureAsPdf, null, tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("Generate & Save Invoice PDF", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                if (generatedFile != null) {
                    OutlinedButton(
                        onClick = {
                            val file = generatedFile!!
                            val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                successMessage = "No PDF viewer found to open file."
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Visibility, null)
                        Spacer(Modifier.width(8.dp))
                        Text("View Generated PDF")
                    }
                }

                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            try {
                                db.projectDao().updateStatus(projectId, ProjectStatus.PAID)
                                db.userStatsDao().onProjectPaid("current_user")
                                scope.launch(Dispatchers.Main) {
                                    isPaid = true
                                    successMessage = "Project marked as PAID! +100 XP earned!"
                                }
                            } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Mark Invoice as Paid (+100 XP)")
                }
            }
        }
    }
}
