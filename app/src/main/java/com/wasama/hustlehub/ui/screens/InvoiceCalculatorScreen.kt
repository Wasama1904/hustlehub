package com.wasama.hustlehub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wasama.hustlehub.util.SecurePrefs
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCalculatorScreen(nav: NavController) {
    val context = LocalContext.current
    val prefs = remember { SecurePrefs.getEncrypted(context) }
    
    var hoursStr by remember { mutableStateOf("10") }
    var rateStr by remember { mutableStateOf(prefs.getString("hourlyRate", "250") ?: "250") }
    var currency by remember { mutableStateOf(prefs.getString("currency", "ZAR") ?: "ZAR") }
    var taxRateStr by remember { mutableStateOf("15") }
    var expensesStr by remember { mutableStateOf("0") }

    val hours = hoursStr.toDoubleOrNull() ?: 0.0
    val rate = rateStr.toDoubleOrNull() ?: 0.0
    val taxRate = taxRateStr.toDoubleOrNull() ?: 0.0
    val expenses = expensesStr.toDoubleOrNull() ?: 0.0

    val subtotal = hours * rate
    val taxAmount = subtotal * (taxRate / 100.0)
    val totalAmount = subtotal + taxAmount + expenses

    val currencySymbol = when(currency) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "AUD" -> "A$"
        else -> "R"
    }

    var showCurrencyDialog by remember { mutableStateOf(false) }

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ZAR" to "ZAR - South African Rand (R)",
                           "USD" to "USD - US Dollar ($)",
                           "EUR" to "EUR - Euro (€)",
                           "GBP" to "GBP - British Pound (£)",
                           "AUD" to "AUD - Australian Dollar (A$)").forEach { (code, label) ->
                        Button(
                            onClick = {
                                currency = code
                                prefs.edit().putString("currency", code).apply()
                                showCurrencyDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = if(currency == code) Color(0xFF2DD4BF) else Color(0xFF1E1E1E))
                        ) {
                            Text(label, color = if(currency == code) Color.Black else Color.White)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0A0A))
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Calculate Billable & Invoice Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2DD4BF))
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = hoursStr,
                                onValueChange = { hoursStr = it },
                                label = { Text("Hours Worked") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = rateStr,
                                onValueChange = { rateStr = it },
                                label = { Text("Hourly Rate ($currencySymbol)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = taxRateStr,
                                onValueChange = { taxRateStr = it },
                                label = { Text("Tax / VAT (%)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = expensesStr,
                                onValueChange = { expensesStr = it },
                                label = { Text("Extra Expenses ($currencySymbol)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        OutlinedButton(
                            onClick = { showCurrencyDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CurrencyExchange, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Currency: $currency ($currencySymbol)")
                        }
                    }
                }
            }

            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF102622))
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Calculation Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF2DD4BF))
                        HorizontalDivider(color = Color(0xFF2A3C38))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal ($hours hrs @ $currencySymbol$rate/hr):", color = Color.Gray)
                            Text("$currencySymbol%.2f".format(subtotal), color = Color.White)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tax / VAT ($taxRate%):", color = Color.Gray)
                            Text("$currencySymbol%.2f".format(taxAmount), color = Color.White)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Additional Expenses:", color = Color.Gray)
                            Text("$currencySymbol%.2f".format(expenses), color = Color.White)
                        }
                        HorizontalDivider(color = Color(0xFF2A3C38))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Total Invoice Amount:", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("$currencySymbol%.2f".format(totalAmount), color = Color(0xFF2DD4BF), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        prefs.edit().putString("hourlyRate", rateStr).putString("currency", currency).apply()
                        nav.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4BF))
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Rate & Currency to Preferences", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
