package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveIndicatorsConfig
import com.example.data.model.CandleDataPoint
import com.example.data.model.ChartStyle
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.ExpenseRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun TradingViewChart(
    candles: List<CandleDataPoint>,
    currencySymbol: String,
    chartStyle: ChartStyle,
    indicators: ActiveIndicatorsConfig,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) return

    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    val displayCandle = hoveredIndex?.let { candles.getOrNull(it) } ?: candles.lastOrNull()

    // Dynamic height calculation based on enabled sub-charts (RSI, MACD)
    val rsiHeightDp = if (indicators.showRsi) 70.dp else 0.dp
    val macdHeightDp = if (indicators.showMacd) 70.dp else 0.dp
    val mainChartHeightDp = 240.dp
    val totalChartHeightDp = mainChartHeightDp + rsiHeightDp + macdHeightDp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A))
            .padding(12.dp)
            .testTag("trading_view_chart")
    ) {
        // TOP STATUS BAR (Robinhood & TradingView crosshair readouts)
        displayCandle?.let { c ->
            val isBullish = c.close >= c.open
            val candleColor = if (isBullish) EmeraldLight else ExpenseRed
            val pctChange = if (c.open > 0) ((c.close - c.open) / c.open) * 100.0 else 0.0

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%,.2f", c.close)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = candleColor
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = candleColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${if (pctChange >= 0) "+" else ""}${String.format(Locale.US, "%.2f", pctChange)}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = candleColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(c.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }

                // Sub-readout: OHLC
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("O: ${String.format(Locale.US, "%,.1f", c.open)}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    Text("H: ${String.format(Locale.US, "%,.1f", c.high)}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    Text("L: ${String.format(Locale.US, "%,.1f", c.low)}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    Text("C: ${String.format(Locale.US, "%,.1f", c.close)}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                }

                // Active Indicators live values ribbon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (indicators.showVwap && c.vwap != null) {
                        Text("VWAP: ${String.format(Locale.US, "%,.1f", c.vwap)}", color = Color(0xFFEC4899), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    if (indicators.showBollinger && c.bollingerUpper != null && c.bollingerLower != null) {
                        Text("BB: [${String.format(Locale.US, "%,.0f", c.bollingerLower)} - ${String.format(Locale.US, "%,.0f", c.bollingerUpper)}]", color = Color(0xFF38BDF8), fontSize = 9.sp)
                    }
                    if (indicators.showEma && c.ema9 != null && c.ema21 != null) {
                        Text("EMA(9): ${String.format(Locale.US, "%,.1f", c.ema9)}", color = Color(0xFF10B981), fontSize = 9.sp)
                        Text("EMA(21): ${String.format(Locale.US, "%,.1f", c.ema21)}", color = Color(0xFFF97316), fontSize = 9.sp)
                    }
                    if (indicators.showRsi && c.rsi != null) {
                        Text("RSI(14): ${String.format(Locale.US, "%.1f", c.rsi)}", color = Color(0xFFA855F7), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    if (indicators.showMacd && c.macdLine != null) {
                        Text("MACD: ${String.format(Locale.US, "%.2f", c.macdLine)}", color = Color(0xFF06B6D4), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // CANVAS CHART (Main Chart + Sub-panes)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalChartHeightDp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            val x = change.position.x
                            val width = size.width
                            val stepX = width / candles.size
                            val index = (x / stepX).toInt().coerceIn(0, candles.size - 1)
                            hoveredIndex = index
                            change.consume()
                        },
                        onDragEnd = { hoveredIndex = null },
                        onDragCancel = { hoveredIndex = null }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            val width = size.width
                            val stepX = width / candles.size
                            val index = (offset.x / stepX).toInt().coerceIn(0, candles.size - 1)
                            hoveredIndex = index
                            tryAwaitRelease()
                            hoveredIndex = null
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val totalH = size.height

                // Pane layout boundaries
                var subPanesTotalHeight = 0f
                if (indicators.showRsi) subPanesTotalHeight += 160f
                if (indicators.showMacd) subPanesTotalHeight += 160f

                val mainChartHeight = totalH - subPanesTotalHeight
                val volumeMaxHeight = if (indicators.showVolume) 45f else 0f

                // Find min and max price across candles + Bollinger Bands + VWAP
                var minPrice = candles.minOf { it.low }
                var maxPrice = candles.maxOf { it.high }
                if (indicators.showBollinger) {
                    val validLower = candles.mapNotNull { it.bollingerLower }
                    val validUpper = candles.mapNotNull { it.bollingerUpper }
                    if (validLower.isNotEmpty()) minPrice = min(minPrice, validLower.minOrNull() ?: minPrice)
                    if (validUpper.isNotEmpty()) maxPrice = max(maxPrice, validUpper.maxOrNull() ?: maxPrice)
                }

                minPrice *= 0.998
                maxPrice *= 1.002
                val priceRange = if (maxPrice - minPrice == 0.0) 1.0 else maxPrice - minPrice

                val maxVolume = candles.maxOfOrNull { it.volume } ?: 1.0

                val candleCount = candles.size
                val stepX = w / candleCount
                val candleWidth = max(2.5f, stepX * 0.72f)

                fun priceToY(price: Double): Float {
                    val normalized = 1.0 - ((price - minPrice) / priceRange)
                    val usableHeight = mainChartHeight - volumeMaxHeight - 20f
                    return (normalized * usableHeight + 10f).toFloat()
                }

                // Gridlines on main chart
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                val gridLevels = 3
                for (i in 0..gridLevels) {
                    val y = (mainChartHeight / gridLevels) * i
                    drawLine(
                        color = Color(0x18334155),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f,
                        pathEffect = dashEffect
                    )
                }

                // 1. BOLLINGER BANDS CLOUD (TradingView / Robinhood feature)
                if (indicators.showBollinger) {
                    val bbUpperPath = Path()
                    val bbLowerPath = Path()
                    val bbCloudPath = Path()

                    var hasStarted = false
                    val validCandles = candles.filter { it.bollingerUpper != null && it.bollingerLower != null }

                    if (validCandles.isNotEmpty()) {
                        candles.forEachIndexed { i, c ->
                            if (c.bollingerUpper != null && c.bollingerLower != null) {
                                val x = (i * stepX) + (stepX / 2f)
                                val upperY = priceToY(c.bollingerUpper)
                                val lowerY = priceToY(c.bollingerLower)

                                if (!hasStarted) {
                                    bbUpperPath.moveTo(x, upperY)
                                    bbLowerPath.moveTo(x, lowerY)
                                    bbCloudPath.moveTo(x, upperY)
                                    hasStarted = true
                                } else {
                                    bbUpperPath.lineTo(x, upperY)
                                    bbLowerPath.lineTo(x, lowerY)
                                    bbCloudPath.lineTo(x, upperY)
                                }
                            }
                        }

                        // Close cloud polygon
                        for (i in candles.indices.reversed()) {
                            val c = candles[i]
                            if (c.bollingerLower != null) {
                                val x = (i * stepX) + (stepX / 2f)
                                bbCloudPath.lineTo(x, priceToY(c.bollingerLower))
                            }
                        }
                        bbCloudPath.close()

                        // Draw Shaded Cloud
                        drawPath(
                            path = bbCloudPath,
                            color = Color(0x1638BDF8)
                        )

                        // Draw Upper Band
                        drawPath(
                            path = bbUpperPath,
                            color = Color(0xFF38BDF8),
                            style = Stroke(width = 1.2f.dp.toPx())
                        )

                        // Draw Lower Band
                        drawPath(
                            path = bbLowerPath,
                            color = Color(0xFF38BDF8),
                            style = Stroke(width = 1.2f.dp.toPx())
                        )
                    }
                }

                // 2. VOLUME BARS (With optional Volume SMA)
                if (indicators.showVolume && maxVolume > 0) {
                    candles.forEachIndexed { i, c ->
                        val x = (i * stepX) + (stepX / 2f)
                        val volHeight = ((c.volume / maxVolume) * volumeMaxHeight).toFloat()
                        val volY = mainChartHeight - volHeight
                        val isBullish = c.close >= c.open
                        val volColor = if (isBullish) Color(0x3810B981) else Color(0x38EF4444)

                        drawRect(
                            color = volColor,
                            topLeft = Offset(x - (candleWidth / 2f), volY),
                            size = Size(candleWidth, volHeight)
                        )
                    }
                }

                // 3. CANDLESTICK / LINE / AREA
                when (chartStyle) {
                    ChartStyle.CANDLESTICK -> {
                        candles.forEachIndexed { i, c ->
                            val x = (i * stepX) + (stepX / 2f)
                            val isBullish = c.close >= c.open
                            val color = if (isBullish) Color(0xFF10B981) else Color(0xFFEF4444)

                            val highY = priceToY(c.high)
                            val lowY = priceToY(c.low)
                            val openY = priceToY(c.open)
                            val closeY = priceToY(c.close)

                            // Wick
                            drawLine(
                                color = color,
                                start = Offset(x, highY),
                                end = Offset(x, lowY),
                                strokeWidth = 1.8f,
                                cap = StrokeCap.Round
                            )

                            // Body
                            val bodyTop = min(openY, closeY)
                            val bodyHeight = max(2.5f, kotlin.math.abs(closeY - openY))
                            drawRect(
                                color = color,
                                topLeft = Offset(x - (candleWidth / 2f), bodyTop),
                                size = Size(candleWidth, bodyHeight)
                            )
                        }
                    }

                    ChartStyle.LINE, ChartStyle.AREA, ChartStyle.LIVE_WEB -> {
                        val linePath = Path()
                        val areaPath = Path()
                        candles.forEachIndexed { i, c ->
                            val x = (i * stepX) + (stepX / 2f)
                            val y = priceToY(c.close)
                            if (i == 0) {
                                linePath.moveTo(x, y)
                                areaPath.moveTo(x, mainChartHeight)
                                areaPath.lineTo(x, y)
                            } else {
                                linePath.lineTo(x, y)
                                areaPath.lineTo(x, y)
                            }
                            if (i == candles.size - 1) {
                                areaPath.lineTo(x, mainChartHeight)
                                areaPath.close()
                            }
                        }
                        val isOverallBullish = (candles.lastOrNull()?.close ?: 0.0) >= (candles.firstOrNull()?.open ?: 0.0)
                        val mainColor = if (isOverallBullish) Color(0xFF10B981) else Color(0xFFEF4444)

                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(mainColor.copy(alpha = 0.30f), Color.Transparent),
                                startY = 0f,
                                endY = mainChartHeight
                            )
                        )
                        drawPath(
                            path = linePath,
                            color = mainColor,
                            style = Stroke(width = 2.4f.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // 4. VWAP (Robinhood day-trading staple)
                if (indicators.showVwap) {
                    val vwapPath = Path()
                    candles.forEachIndexed { i, c ->
                        c.vwap?.let { v ->
                            val x = (i * stepX) + (stepX / 2f)
                            val y = priceToY(v)
                            if (i == 0) vwapPath.moveTo(x, y) else vwapPath.lineTo(x, y)
                        }
                    }
                    drawPath(
                        path = vwapPath,
                        color = Color(0xFFEC4899),
                        style = Stroke(width = 2f.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // 5. EMA 9 & EMA 21 (Robinhood moving average ribbons)
                if (indicators.showEma) {
                    val ema9Path = Path()
                    val ema21Path = Path()
                    candles.forEachIndexed { i, c ->
                        val x = (i * stepX) + (stepX / 2f)
                        c.ema9?.let { if (i == 0) ema9Path.moveTo(x, priceToY(it)) else ema9Path.lineTo(x, priceToY(it)) }
                        c.ema21?.let { if (i == 0) ema21Path.moveTo(x, priceToY(it)) else ema21Path.lineTo(x, priceToY(it)) }
                    }
                    drawPath(path = ema9Path, color = Color(0xFF10B981), style = Stroke(width = 1.6f.dp.toPx()))
                    drawPath(path = ema21Path, color = Color(0xFFF97316), style = Stroke(width = 1.6f.dp.toPx()))
                }

                // 6. SMA 20 & SMA 50
                if (indicators.showSma) {
                    val sma20Path = Path()
                    val sma50Path = Path()
                    var s20Started = false
                    var s50Started = false
                    candles.forEachIndexed { i, c ->
                        val x = (i * stepX) + (stepX / 2f)
                        c.sma20?.let {
                            if (!s20Started) { sma20Path.moveTo(x, priceToY(it)); s20Started = true }
                            else sma20Path.lineTo(x, priceToY(it))
                        }
                        c.sma50?.let {
                            if (!s50Started) { sma50Path.moveTo(x, priceToY(it)); s50Started = true }
                            else sma50Path.lineTo(x, priceToY(it))
                        }
                    }
                    if (s20Started) drawPath(path = sma20Path, color = Color(0xFFF59E0B), style = Stroke(width = 1.6f.dp.toPx()))
                    if (s50Started) drawPath(path = sma50Path, color = Color(0xFF06B6D4), style = Stroke(width = 1.6f.dp.toPx()))
                }

                // 7. PARABOLIC SAR DOTS
                if (indicators.showParabolicSar) {
                    candles.forEachIndexed { i, c ->
                        c.sar?.let { s ->
                            val x = (i * stepX) + (stepX / 2f)
                            val y = priceToY(s)
                            drawCircle(
                                color = Color(0xFFC084FC),
                                radius = 2f.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                }

                // SUB-PANE SEPARATOR & DRAWING
                var currentPaneY = mainChartHeight

                // 8. RSI SUB-PANE (TradingView & Robinhood 14-period RSI)
                if (indicators.showRsi) {
                    val paneH = 160f
                    // Separator line
                    drawLine(
                        color = Color(0x33475569),
                        start = Offset(0f, currentPaneY),
                        end = Offset(w, currentPaneY),
                        strokeWidth = 1f
                    )

                    // RSI Overbought (70) and Oversold (30) levels
                    val rsi70Y = currentPaneY + (paneH * 0.30f)
                    val rsi30Y = currentPaneY + (paneH * 0.70f)

                    // Shaded 30-70 band
                    drawRect(
                        color = Color(0x15A855F7),
                        topLeft = Offset(0f, rsi70Y),
                        size = Size(w, rsi30Y - rsi70Y)
                    )

                    drawLine(
                        color = Color(0x55EF4444),
                        start = Offset(0f, rsi70Y),
                        end = Offset(w, rsi70Y),
                        strokeWidth = 1f,
                        pathEffect = dashEffect
                    )
                    drawLine(
                        color = Color(0x5510B981),
                        start = Offset(0f, rsi30Y),
                        end = Offset(w, rsi30Y),
                        strokeWidth = 1f,
                        pathEffect = dashEffect
                    )

                    // Draw RSI Line
                    val rsiPath = Path()
                    candles.forEachIndexed { i, c ->
                        c.rsi?.let { r ->
                            val x = (i * stepX) + (stepX / 2f)
                            val normY = 1.0 - (r / 100.0)
                            val y = (currentPaneY + (normY * (paneH - 16f)) + 8f).toFloat()
                            if (i == 0) rsiPath.moveTo(x, y) else rsiPath.lineTo(x, y)
                        }
                    }
                    drawPath(
                        path = rsiPath,
                        color = Color(0xFFA855F7),
                        style = Stroke(width = 1.8f.dp.toPx(), cap = StrokeCap.Round)
                    )

                    currentPaneY += paneH
                }

                // 9. MACD SUB-PANE (12, 26, 9)
                if (indicators.showMacd) {
                    val paneH = 160f
                    // Separator line
                    drawLine(
                        color = Color(0x33475569),
                        start = Offset(0f, currentPaneY),
                        end = Offset(w, currentPaneY),
                        strokeWidth = 1f
                    )

                    val zeroY = currentPaneY + (paneH / 2f)
                    drawLine(
                        color = Color(0x4464748B),
                        start = Offset(0f, zeroY),
                        end = Offset(w, zeroY),
                        strokeWidth = 1f
                    )

                    val maxHist = candles.mapNotNull { it.macdHist }.map { kotlin.math.abs(it) }.maxOrNull() ?: 1.0
                    val histScale = if (maxHist > 0) (paneH * 0.40f) / maxHist else 1.0

                    // Draw MACD Histogram
                    candles.forEachIndexed { i, c ->
                        c.macdHist?.let { h ->
                            val x = (i * stepX) + (stepX / 2f)
                            val barH = (h * histScale).toFloat()
                            val isPos = h >= 0
                            val color = if (isPos) Color(0x8810B981) else Color(0x88EF4444)
                            val topY = if (isPos) zeroY - barH else zeroY
                            val height = max(2f, kotlin.math.abs(barH))

                            drawRect(
                                color = color,
                                topLeft = Offset(x - (candleWidth / 2f), topY),
                                size = Size(candleWidth, height)
                            )
                        }
                    }

                    // Draw MACD Line & Signal Line
                    val macdPath = Path()
                    val signalPath = Path()
                    candles.forEachIndexed { i, c ->
                        val x = (i * stepX) + (stepX / 2f)
                        c.macdLine?.let { m ->
                            val y = (zeroY - (m * histScale * 0.8f)).toFloat().coerceIn(currentPaneY + 4f, currentPaneY + paneH - 4f)
                            if (i == 0) macdPath.moveTo(x, y) else macdPath.lineTo(x, y)
                        }
                        c.macdSignalLine?.let { s ->
                            val y = (zeroY - (s * histScale * 0.8f)).toFloat().coerceIn(currentPaneY + 4f, currentPaneY + paneH - 4f)
                            if (i == 0) signalPath.moveTo(x, y) else signalPath.lineTo(x, y)
                        }
                    }
                    drawPath(path = macdPath, color = Color(0xFF06B6D4), style = Stroke(width = 1.6f.dp.toPx()))
                    drawPath(path = signalPath, color = Color(0xFFF59E0B), style = Stroke(width = 1.6f.dp.toPx()))

                    currentPaneY += paneH
                }

                // 10. SYNCHRONIZED CROSSHAIR ACROSS ALL PANES
                hoveredIndex?.let { idx ->
                    val c = candles[idx]
                    val crossX = (idx * stepX) + (stepX / 2f)
                    val crossY = priceToY(c.close)

                    // Vertical crosshair across whole canvas height
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = Offset(crossX, 0f),
                        end = Offset(crossX, totalH),
                        strokeWidth = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    // Horizontal price line on main chart
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = Offset(0f, crossY),
                        end = Offset(w, crossY),
                        strokeWidth = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    drawCircle(
                        color = Color.White,
                        radius = 4f.dp.toPx(),
                        center = Offset(crossX, crossY)
                    )
                }
            }
        }
    }
}
