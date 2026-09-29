package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TechnicalAnalysisData
import com.example.data.model.TechnicalSignal
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.ExpenseRed
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TechnicalGauge(
    analysis: TechnicalAnalysisData,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TRADINGVIEW TECHNICAL RATING",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Speedometer Arc
            Box(
                modifier = Modifier
                    .size(width = 220.dp, height = 115.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Canvas(modifier = Modifier.size(220.dp, 115.dp)) {
                    val w = size.width
                    val h = size.height * 2f // Full circle height

                    val strokeWidth = 16f

                    // Draw 5 Arc Segments: Strong Sell, Sell, Neutral, Buy, Strong Buy
                    // Arc goes from 180 to 0 degrees (top half circle)
                    val arcColors = listOf(
                        Color(0xFFDC2626), // Strong Sell
                        Color(0xFFEF4444), // Sell
                        Color(0xFF94A3B8), // Neutral
                        Color(0xFF10B981), // Buy
                        Color(0xFF059669)  // Strong Buy
                    )

                    val sweepAnglePerSegment = 35f
                    val gap = 1f

                    arcColors.forEachIndexed { i, c ->
                        val startAngle = 180f + (i * 36f)
                        drawArc(
                            color = c,
                            startAngle = startAngle,
                            sweepAngle = 34f,
                            useCenter = false,
                            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                            size = Size(w - strokeWidth, h - strokeWidth),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Needle Needle calculation
                    // Angle mapping: Strong Sell = 195°, Sell = 230°, Neutral = 270°, Buy = 310°, Strong Buy = 345°
                    val targetAngle = when (analysis.overallSignal) {
                        TechnicalSignal.STRONG_SELL -> 198.0
                        TechnicalSignal.SELL -> 232.0
                        TechnicalSignal.NEUTRAL -> 270.0
                        TechnicalSignal.BUY -> 308.0
                        TechnicalSignal.STRONG_BUY -> 342.0
                    }

                    val rad = Math.toRadians(targetAngle)
                    val centerX = w / 2f
                    val centerY = size.height
                    val needleLength = (w / 2f) - 24f

                    val endX = (centerX + needleLength * cos(rad)).toFloat()
                    val endY = (centerY + needleLength * sin(rad)).toFloat()

                    // Draw needle line
                    drawLine(
                        color = Color.White,
                        start = Offset(centerX, centerY),
                        end = Offset(endX, endY),
                        strokeWidth = 3.5f,
                        cap = StrokeCap.Round
                    )

                    // Draw pivot center circle
                    drawCircle(
                        color = Color.White,
                        radius = 7f,
                        center = Offset(centerX, centerY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Overall Status Pill
            val signalColor = Color(analysis.overallSignal.colorHex)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = signalColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = analysis.overallSignal.label,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = signalColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Buy / Neutral / Sell count metrics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sell", style = MaterialTheme.typography.labelSmall, color = ExpenseRed)
                    Text(
                        "${analysis.sellCount}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ExpenseRed
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Neutral", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    Text(
                        "${analysis.neutralCount}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF94A3B8)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Buy", style = MaterialTheme.typography.labelSmall, color = EmeraldLight)
                    Text(
                        "${analysis.buyCount}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldLight
                    )
                }
            }
        }
    }
}
