import sys

with open('app/src/main/java/com/example/ui/components/InstallmentCard.kt', 'r') as f:
    content = f.read()

target = """                Text(
                    text = installment.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface("""

replacement = """                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = installment.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(androidx.compose.material.icons.Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface("""

content = content.replace(target, replacement)

target2 = """    onPay: () -> Unit,"""
replacement2 = """    onPay: () -> Unit,
    onDelete: () -> Unit = {},"""
content = content.replace(target2, replacement2)

target3 = """import androidx.compose.ui.unit.sp"""
replacement3 = """import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton"""
content = content.replace(target3, replacement3)

with open('app/src/main/java/com/example/ui/components/InstallmentCard.kt', 'w') as f:
    f.write(content)

