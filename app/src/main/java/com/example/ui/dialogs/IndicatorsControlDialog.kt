package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ActiveIndicatorsConfig
import com.example.ui.theme.EmeraldLight

@Composable
fun IndicatorsControlDialog(
    currentConfig: ActiveIndicatorsConfig,
    onDismiss: () -> Unit,
    onApplyConfig: (ActiveIndicatorsConfig) -> Unit
) {
    var config by remember { mutableStateOf(currentConfig) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_indicators_control")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
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
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Technical Indicators",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_indicators_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Configure Robinhood & TradingView technical analysis overlays and oscillators.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Presets Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                // Robinhood Day Trader preset
                                config = ActiveIndicatorsConfig(
                                    showVwap = true,
                                    showBollinger = true,
                                    showEma = true,
                                    showSma = false,
                                    showParabolicSar = false,
                                    showVolume = true,
                                    showRsi = true,
                                    showMacd = true,
                                    showStochastic = false
                                )
                            }
                    ) {
                        Text(
                            text = "⚡ Robinhood Pro",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                // TradingView Classic Swing preset
                                config = ActiveIndicatorsConfig(
                                    showVwap = false,
                                    showBollinger = true,
                                    showEma = false,
                                    showSma = true,
                                    showParabolicSar = true,
                                    showVolume = true,
                                    showRsi = true,
                                    showMacd = true,
                                    showStochastic = false
                                )
                            }
                    ) {
                        Text(
                            text = "📈 TradingView",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // MAIN CHART OVERLAYS SECTION
                    item {
                        Text(
                            text = "CHART OVERLAYS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    item {
                        IndicatorToggleRow(
                            title = "VWAP (Volume Weighted Average Price)",
                            subtitle = "Day trader intraday institutional benchmark",
                            color = Color(0xFFEC4899),
                            checked = config.showVwap,
                            onCheckedChange = { config = config.copy(showVwap = it) }
                        )
                    }

                    item {
                        IndicatorToggleRow(
                            title = "Bollinger Bands (20, 2)",
                            subtitle = "Volatility envelope & mean reversion bands",
                            color = Color(0xFF38BDF8),
                            checked = config.showBollinger,
                            onCheckedChange = { config = config.copy(showBollinger = it) }
                        )
                    }

                    item {
                        IndicatorToggleRow(
                            title = "EMA 9 & EMA 21 Ribbon",
                            subtitle = "Fast momentum exponential trend ribbons",
                            color = Color(0xFF10B981),
                            checked = config.showEma,
                            onCheckedChange = { config = config.copy(showEma = it) }
                        )
                    }

                    item {
                        IndicatorToggleRow(
                            title = "SMA 20 & SMA 50",
                            subtitle = "Simple moving averages for trend support",
                            color = Color(0xFFF59E0B),
                            checked = config.showSma,
                            onCheckedChange = { config = config.copy(showSma = it) }
                        )
                    }

                    item {
                        IndicatorToggleRow(
                            title = "Parabolic SAR (Stop & Reverse)",
                            subtitle = "Trailing stop and directional price dots",
                            color = Color(0xFFC084FC),
                            checked = config.showParabolicSar,
                            onCheckedChange = { config = config.copy(showParabolicSar = it) }
                        )
                    }

                    // SUB-CHART OSCILLATORS SECTION
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "SUB-CHART OSCILLATORS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    item {
                        IndicatorToggleRow(
                            title = "RSI (Relative Strength Index 14)",
                            subtitle = "70 Overbought & 30 Oversold oscillator pane",
                            color = Color(0xFFA855F7),
                            checked = config.showRsi,
                            onCheckedChange = { config = config.copy(showRsi = it) }
                        )
                    }

                    item {
                        IndicatorToggleRow(
                            title = "MACD (12, 26, 9)",
                            subtitle = "Convergence Divergence line, signal & histogram",
                            color = Color(0xFF06B6D4),
                            checked = config.showMacd,
                            onCheckedChange = { config = config.copy(showMacd = it) }
                        )
                    }

                    item {
                        IndicatorToggleRow(
                            title = "Trading Volume & Vol-SMA",
                            subtitle = "Liquidity bars & volume moving average",
                            color = Color(0xFF94A3B8),
                            checked = config.showVolume,
                            onCheckedChange = { config = config.copy(showVolume = it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onApplyConfig(config)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_apply_indicators")
                    ) {
                        Text("Apply Indicators", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun IndicatorToggleRow(
    title: String,
    subtitle: String,
    color: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = color,
                    modifier = Modifier.size(12.dp)
                ) {}

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag("switch_${title.take(6).lowercase()}")
            )
        }
    }
}
