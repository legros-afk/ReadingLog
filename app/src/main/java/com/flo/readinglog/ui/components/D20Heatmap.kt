package com.flo.readinglog.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Composable
fun D20Heatmap(
    data: Map<LocalDate, Int>,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val startDate = today
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .minusWeeks(4)
    val allDates = (0 until 35).map { startDate.plusDays(it.toLong()) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    val weeks = 5
    val daysPerWeek = 7
    val cellSizeDp = 20.dp
    val gapDp = 3.dp
    val canvasWidthDp = cellSizeDp * weeks + gapDp * (weeks - 1)
    val canvasHeightDp = cellSizeDp * daysPerWeek + gapDp * (daysPerWeek - 1)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier
                .width(16.dp)
                .height(canvasHeightDp),
            verticalArrangement = Arrangement.spacedBy(gapDp),
        ) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { label ->
                Box(
                    modifier = Modifier.size(16.dp, cellSizeDp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        fontSize = 8.sp,
                        lineHeight = 8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.width(4.dp))

        Canvas(modifier = Modifier.size(canvasWidthDp, canvasHeightDp)) {
            val cellSize = cellSizeDp.toPx()
            val gap = gapDp.toPx()
            val strokeWidth = 1.5.dp.toPx()

            allDates.forEachIndexed { index, date ->
                val weekCol = index / daysPerWeek
                val dayRow = index % daysPerWeek

                val left = weekCol * (cellSize + gap)
                val top = dayRow * (cellSize + gap)
                val right = left + cellSize
                val bottom = top + cellSize
                val cx = (left + right) / 2f
                val cy = (top + bottom) / 2f

                val pages = data[date] ?: 0
                val isFuture = date.isAfter(today)

                val fillColor = when {
                    isFuture -> surfaceVariantColor.copy(alpha = 0.15f)
                    pages == 0 -> surfaceVariantColor.copy(alpha = 0.5f)
                    pages in 1..10 -> primaryColor.copy(alpha = 0.35f)
                    pages in 11..25 -> primaryColor.copy(alpha = 0.65f)
                    else -> primaryColor
                }

                val diamond = Path().apply {
                    moveTo(cx, top)
                    lineTo(right, cy)
                    lineTo(cx, bottom)
                    lineTo(left, cy)
                    close()
                }

                drawPath(diamond, fillColor)

                if (date == today) {
                    drawPath(diamond, primaryColor, style = Stroke(width = strokeWidth))
                }
            }
        }
    }
}
