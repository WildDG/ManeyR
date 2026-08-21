with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

replacement = """                    if (tags.isNotEmpty()) {
                        item {
                            Text("Label Transaksi", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                            androidx.compose.foundation.layout.FlowRow(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tags.forEach { tag ->
                                    val isSelected = selectedTagIds.contains(tag.id)
                                    val tagColor = runCatching { Color(android.graphics.Color.parseColor(tag.colorHex)) }.getOrDefault(Color.Gray)
                                    androidx.compose.material3.FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedTagIds = if (isSelected) selectedTagIds - tag.id else selectedTagIds + tag.id
                                        },
                                        label = { Text(tag.name) },
                                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = tagColor.copy(alpha = 0.2f),
                                            selectedLabelColor = tagColor,
                                            iconColor = tagColor
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Description Note Input text block"""

content = content.replace("                    // Description Note Input text block", replacement)

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)

