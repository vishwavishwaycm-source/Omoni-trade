package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.ExpenseRed
import java.util.Locale
import kotlin.math.abs

@Composable
fun TradingCalculatorDialog(
    symbol: String,
    currentPrice: Double,
    currencySymbol: String,
    onDismiss: () -> Unit
) {
    var entryPriceStr by remember { mutableStateOf(String.format(Locale.US, "%.2f", currentPrice)) }
    var stopLossStr by remember { mutableStateOf(String.format(Locale.US, "%.2f", currentPrice * 0.96)) }
    var targetProfitStr by remember { mutableStateOf(String.format(Locale.US, "%.2f", currentPrice * 1.08)) }
    var capitalStr by remember { mutableStateOf("1000.00") }

    val entry = entryPriceStr.toDoubleOrNull() ?: currentPrice
    val stopLoss = stopLossStr.toDoubleOrNull() ?: (currentPrice * 0.96)
    val target = targetProfitStr.toDoubleOrNull() ?: (currentPrice * 1.08)
    val capital = capitalStr.toDoubleOrNull() ?: 1000.0

    val riskPerUnit = abs(entry - stopLoss)
    val rewardPerUnit = abs(target - entry)
    val riskRewardRatio = if (riskPerUnit > 0) rewardPerUnit / riskPerUnit else 0.0

    val units = if (entry > 0) capital / entry else 0.0
    val potentialProfit = units * rewardPerUnit
    val potentialLoss = units * riskPerUnit
    val profitPercent = if (entry > 0) ((target - entry) / entry) * 100.0 else 0.0
    val lossPercent = if (entry > 0) ((entry - stopLoss) / entry) * 100.0 else 0.0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .testTag("dialog_trading_calculator")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Trading Risk / Reward",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_calculator_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Calculate position size & risk-reward ratio for $symbol",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Calculated Results Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Risk / Reward Ratio", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = "1 : ${String.format(Locale.US, "%.2f", riskRewardRatio)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (riskRewardRatio >= 2.0) EmeraldLight else MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Potential Gain", style = MaterialTheme.typography.labelSmall, color = EmeraldLight)
                                Text(
                                    "+$currencySymbol${String.format(Locale.US, "%.2f", potentialProfit)} (+${String.format(Locale.US, "%.1f", profitPercent)}%)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldLight
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Max Risk / Loss", style = MaterialTheme.typography.labelSmall, color = ExpenseRed)
                                Text(
                                    "-$currencySymbol${String.format(Locale.US, "%.2f", potentialLoss)} (-${String.format(Locale.US, "%.1f", lossPercent)}%)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ExpenseRed
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Inputs
                OutlinedTextField(
                    value = capitalStr,
                    onValueChange = { capitalStr = it },
                    label = { Text("Capital / Position Size") },
                    prefix = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = entryPriceStr,
                        onValueChange = { entryPriceStr = it },
                        label = { Text("Entry Price") },
                        prefix = { Text(currencySymbol) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = stopLossStr,
                        onValueChange = { stopLossStr = it },
                        label = { Text("Stop Loss") },
                        prefix = { Text(currencySymbol) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetProfitStr,
                    onValueChange = { targetProfitStr = it },
                    label = { Text("Target Profit Price") },
                    prefix = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_close_calculator")
                ) {
                    Text("Done")
                }
            }
        }
    }
}
