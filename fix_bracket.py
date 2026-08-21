import sys

with open('app/src/main/java/com/example/ui/components/InstallmentCard.kt', 'r') as f:
    content = f.read()

target = """                Row(verticalAlignment = Alignment.CenterVertically) {
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
            }"""

replacement = """                Row(verticalAlignment = Alignment.CenterVertically) {
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
            }"""
content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/components/InstallmentCard.kt', 'w') as f:
    f.write(content)
