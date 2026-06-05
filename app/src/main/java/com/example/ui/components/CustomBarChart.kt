package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CategoryShare
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CustomBarChart(
    shares: List<CategoryShare>,
    modifier: Modifier = Modifier,
    statType: String = "PENGELUARAN",
    globalBudgetLimit: Double = 0.0
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

    val totalAmount = shares.sumOf { it.amount }
    val maxShareAmount = shares.maxOfOrNull { it.amount } ?: 1.0

    // Entrance Animation
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(shares) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Total Text Above Chart
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
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = rupiahFormatter.format(totalAmount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = when (statType) {
                    "PEMASUKAN" -> Color(0xFF2E7D32)
                    "GABUNGAN" -> MaterialTheme.colorScheme.primary
                    else -> {
                        if (globalBudgetLimit > 0) {
                            if (totalAmount >= globalBudgetLimit) Color(0xFFC62828)
                            else if (totalAmount >= 0.8 * globalBudgetLimit) Color(0xFFFFB300)
                            else Color(0xFF2E7D32)
                        } else {
                            Color(0xFFC62828)
                        }
                    }
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Bar Chart representation
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            shares.forEach { share ->
                val color = runCatching {
                    Color(android.graphics.Color.parseColor(share.category.colorHex))
                }.getOrDefault(MaterialTheme.colorScheme.primary)

                val fraction = (share.amount / maxShareAmount).toFloat()
                val animatedFraction = fraction * animationProgress.value

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = share.category.name,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f%%", share.percentage),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = animatedFraction.coerceIn(0.01f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(7.dp))
                                .background(color)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = rupiahFormatter.format(share.amount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
