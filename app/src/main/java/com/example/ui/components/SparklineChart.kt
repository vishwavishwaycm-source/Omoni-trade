package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SparklineChart(
    dataPoints: List<Float>,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
    width: Dp = 80.dp,
    height: Dp = 36.dp
) {
    if (dataPoints.size < 2) return

    val strokeColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
    val gradientColor = if (isPositive) Color(0x3310B981) else Color(0x33EF4444)

    Canvas(
        modifier = modifier
            .width(width)
            .height(height)
    ) {
        val minVal = dataPoints.minOrNull() ?: 0f
        val maxVal = dataPoints.maxOrNull() ?: 1f
        val range = if (maxVal - minVal == 0f) 1f else maxVal - minVal

        val w = size.width
        val h = size.height

        val stepX = w / (dataPoints.size - 1)

        val path = Path()
        val fillPath = Path()

        dataPoints.forEachIndexed { index, value ->
            val x = index * stepX
            val normalizedY = 1f - ((value - minVal) / range)
            val y = normalizedY * (h - 8f) + 4f

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (index == dataPoints.size - 1) {
                fillPath.lineTo(x, h)
                fillPath.close()
            }
        }

        // Draw gradient fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(gradientColor, Color.Transparent),
                startY = 0f,
                endY = h
            )
        )

        // Draw stroke line
        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = 2.5f.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
