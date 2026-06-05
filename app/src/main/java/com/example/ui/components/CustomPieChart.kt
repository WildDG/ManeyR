package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CategoryShare
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CustomPieChart(
    shares: List<CategoryShare>,
    modifier: Modifier = Modifier,
    statType: String = "PENGELUARAN"
) {
    val localeID = Locale("id", "ID")
    val rupiahFormatter = NumberFormat.getCurrencyInstance(localeID).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    if (shares.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            val emptyMsg = when (statType) {
                "PEMASUKAN" -> "Belum ada transaksi pemasukan di periode ini."
                "GABUNGAN" -> "Belum ada transaksi keuangan di periode ini."
                else -> "Belum ada transaksi pengeluaran di periode ini."
            }
            Text(
                text = emptyMsg,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    val totalExpense = shares.sumOf { it.amount }

    // Entrance Animation
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(shares) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    val defaultColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        val onSurfaceArgb = MaterialTheme.colorScheme.onSurface.toArgb()

        // Top side: Enlarged Donut Chart
        Box(
            modifier = Modifier
                .fillMaxWidth(1f)
                .aspectRatio(1.1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val sizeMin = size.minDimension
                val strokeWidth = sizeMin * 0.16f // Thicker but proportioned
                val radius = (sizeMin - strokeWidth) / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                var startAngle = -90f
                val showGaps = shares.size > 1
                val gapAngle = if (showGaps) 2.5f else 0f

                shares.forEach { share ->
                    val totalSweep = (share.percentage.toFloat() / 100f) * 360f * animationProgress.value
                    val sweepAngle = if (showGaps && totalSweep > gapAngle) totalSweep - gapAngle else totalSweep
                    val color = runCatching {
                        Color(android.graphics.Color.parseColor(share.category.colorHex))
                    }.getOrDefault(defaultColor)

                    if (sweepAngle > 0f) {
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )
                        
                        // Draw Percentage Text and Callout if slice is big enough
                        if (share.percentage >= 4.0 && animationProgress.value > 0.8f) {
                            val textAngle = startAngle + (sweepAngle / 2f)
                            val radians = Math.toRadians(textAngle.toDouble())
                            
                            val x = center.x + (radius * cos(radians)).toFloat()
                            val y = center.y + (radius * sin(radians)).toFloat()
                            
                            drawContext.canvas.nativeCanvas.drawText(
                                String.format(Locale.US, "%.1f%%", share.percentage),
                                x,
                                y + (sizeMin * 0.02f), // slight vertical adjust
                                android.graphics.Paint().apply {
                                    this.color = android.graphics.Color.WHITE
                                    this.textSize = sizeMin * 0.035f
                                    this.textAlign = android.graphics.Paint.Align.CENTER
                                    this.typeface = android.graphics.Typeface.DEFAULT_BOLD
                                    this.setShadowLayer(4f, 0f, 2f, android.graphics.Color.parseColor("#80000000"))
                                }
                            )

                            // Draw Callout Line and Label outside
                            val outerRadius = radius + (strokeWidth / 2f)
                            val lineLength = sizeMin * 0.04f
                            
                            val startX = center.x + (outerRadius * cos(radians)).toFloat()
                            val startY = center.y + (outerRadius * sin(radians)).toFloat()
                            
                            val endX = center.x + ((outerRadius + lineLength) * cos(radians)).toFloat()
                            val endY = center.y + ((outerRadius + lineLength) * sin(radians)).toFloat()
                            
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.8f),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 3f,
                                cap = StrokeCap.Round
                            )
                            
                            val isRight = cos(radians) >= 0
                            val isBottom = sin(radians) >= 0
                            
                            val textOffset = sizeMin * 0.015f
                            val textX = endX + if (isRight) textOffset else -textOffset
                            val textY = endY + if (isBottom) textOffset * 1.5f else -textOffset * 0.5f
                            
                            val labelPaint = android.graphics.Paint().apply {
                                this.color = onSurfaceArgb
                                this.textSize = sizeMin * 0.035f
                                this.textAlign = if (isRight) android.graphics.Paint.Align.LEFT else android.graphics.Paint.Align.RIGHT
                                this.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                            }
                            
                            val labelText = if (share.category.name.length > 12) share.category.name.take(10) + ".." else share.category.name
                            drawContext.canvas.nativeCanvas.drawText(
                                labelText,
                                textX,
                                textY,
                                labelPaint
                            )
                        }
                    }
                    startAngle += totalSweep
                }
            }
        }

        // Total Text Below Chart
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when (statType) {
                    "PEMASUKAN" -> "Total Pemasukan"
                    "GABUNGAN" -> "Total Keuangan"
                    else -> "Total Pengeluaran"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = rupiahFormatter.format(totalExpense),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = when (statType) {
                    "PEMASUKAN" -> Color(0xFF2E7D32)
                    "GABUNGAN" -> MaterialTheme.colorScheme.primary
                    else -> Color(0xFFC62828)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
