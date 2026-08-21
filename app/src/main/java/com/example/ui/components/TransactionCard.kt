package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.ui.util.FormatUtils

@Composable
fun TransactionCard(
    transaction: Transaction,
    category: Category?,
    sourceAccount: Account?,
    destAccount: Account?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isPemasukan = transaction.type == "PEMASUKAN"
    val isTransfer = transaction.type == "TRANSFER"
    val amountColor = if (isPemasukan) Color(0xFF2E7D32) else if (isTransfer) Color(0xFF1976D2) else Color(0xFFC62828)
    val amountPrefix = if (isPemasukan) "+ " else if (isTransfer) "➔ " else "- "

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Category Icon placeholder
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        category?.name?.take(1) ?: "?",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.notes.ifBlank { category?.name ?: "Tanpa Keterangan" },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = if (isTransfer) "${sourceAccount?.name} ➔ ${destAccount?.name}" else sourceAccount?.name ?: "Unknown Account",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$amountPrefix${FormatUtils.formatRupiah(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = amountColor
                )
            }
        }
    }
}
