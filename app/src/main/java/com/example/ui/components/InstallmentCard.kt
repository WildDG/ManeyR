package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.data.model.Installment
import com.example.ui.util.FormatUtils

@Composable
fun InstallmentCard(
    installment: Installment,
    isPiutang: Boolean = false,
    onPay: () -> Unit,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val progress = if (installment.totalInstallments > 0) {
        installment.paidCount.toFloat() / installment.totalInstallments.toFloat()
    } else {
        0f
    }
    
    val indonLocale = java.util.Locale("id", "ID")
    val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", indonLocale)
    val nextDueDateStr = dateFormat.format(java.util.Date(installment.nextDueDate))

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = installment.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(androidx.compose.material.icons.Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(androidx.compose.material.icons.Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (installment.status == "ACTIVE") MaterialTheme.colorScheme.primaryContainer else Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = if (installment.status == "ACTIVE") "Berjalan" else "Lunas",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (installment.status == "ACTIVE") MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF2E7D32)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total: ${FormatUtils.formatRupiah(installment.totalAmount)}", style = MaterialTheme.typography.bodySmall)
                Text("Sisa: ${FormatUtils.formatRupiah(installment.remainingAmount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (isPiutang) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                trackColor = if (isPiutang) Color(0xFFC8E6C9) else MaterialTheme.colorScheme.primaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            if (isPiutang) {
                Text(
                    text = "${FormatUtils.formatRupiah(installment.totalAmount - installment.remainingAmount)} / ${FormatUtils.formatRupiah(installment.totalAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Cicilan ${installment.paidCount} dari ${installment.totalInstallments}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Bayaran ${FormatUtils.formatRupiah(installment.totalAmount - installment.remainingAmount)} / ${FormatUtils.formatRupiah(installment.totalAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (installment.status == "ACTIVE") {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(if (isPiutang) "Penerimaan Berikutnya" else "Tagihan Berikutnya", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FormatUtils.formatRupiah(installment.installmentAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (isPiutang) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error)
                        Text("Jatuh tempo: $nextDueDateStr", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = onPay,
                        shape = RoundedCornerShape(12.dp),
                        colors = if (isPiutang) ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)) else ButtonDefaults.buttonColors()
                    ) {
                        Text(if (isPiutang) "Terima Bayaran" else "Bayar Sekarang")
                    }
                }
            }
        }
    }
}
