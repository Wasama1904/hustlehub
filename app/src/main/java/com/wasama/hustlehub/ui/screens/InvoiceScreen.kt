package com.wasama.hustlehub.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.wasama.hustlehub.data.local.ClientEntity
import com.wasama.hustlehub.data.local.ProjectEntity
import com.wasama.hustlehub.util.InvoicePdf

@Composable
fun InvoiceScreen(projectId: String, nav: NavController) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Invoice Preview", style = MaterialTheme.typography.headlineSmall)
        Card(Modifier.fillMaxWidth().padding(top=12.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Client: Acme Design")
                Text("Project: Logo Redesign")
                Text("Time: 5h x R250")
                Divider(Modifier.padding(vertical=8.dp))
                Text("Total: R1250 ZAR", style = MaterialTheme.typography.titleLarge)
            }
        }
        Button(onClick = {
            // Generate PDF
            val client = ClientEntity(userId="u", clientId="c", name="Acme", email="a@b.com")
            val proj = ProjectEntity(userId="u", clientId="c", projectId=projectId, title="Logo Redesign", budgetAmount=5000.0, deadlineTimestamp=System.currentTimeMillis()+86400000*2)
            val file = InvoicePdf.generate(context, client, proj, 1250.0, 5.0)
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, context.packageName+".provider", file))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(share, "Share via WhatsApp"))
        }, modifier = Modifier.fillMaxWidth().padding(top=16.dp)) { Text("Generate & Share PDF") }
        Button(onClick = {
            // Mark paid + 100 XP
        }, modifier = Modifier.fillMaxWidth().padding(top=8.dp)) { Text("Mark as Paid +100 XP") }
    }
}
