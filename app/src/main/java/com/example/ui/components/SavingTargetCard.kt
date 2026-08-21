package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.Account
import com.example.data.model.SavingTarget
import com.example.ui.util.FormatUtils

@Composable
fun SavingTargetCard(
    target: SavingTarget,
    sourceAccount: Account?,
    onSave: (Long) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(target.name, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val progress = if (target.targetAmount > 0L) (target.currentAmount.toFloat() / target.targetAmount.toFloat()).coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Current: ${FormatUtils.formatRupiah(target.currentAmount)}", style = MaterialTheme.typography.bodySmall)
                Text("Target: ${FormatUtils.formatRupiah(target.targetAmount)}", style = MaterialTheme.typography.bodySmall)
            }
            sourceAccount?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Linked: ${it.name}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onSave(100000L) }, // Default fast deposit
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Quick Save")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Quick Deposit Rp 100K")
            }
        }
    }
}
