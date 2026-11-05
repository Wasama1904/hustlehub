package com.wasama.hustlehub.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ProductionEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    ctaText: String,
    onCtaClick: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(64.dp), tint = Color(0xFF2A2A2A))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onCtaClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4BF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Add, null, tint = Color.Black)
            Spacer(Modifier.width(8.dp))
            Text(ctaText, color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
