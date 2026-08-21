with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    """                                    if (selectedType == "TRANSFER") selectedDestAccountId else null,
                                    selectedDate,
                                    finalNotes,
                                    transferFeeVal
                                )""",
    """                                    if (selectedType == "TRANSFER") selectedDestAccountId else null,
                                    selectedDate,
                                    finalNotes,
                                    transferFeeVal,
                                    selectedTagIds.toList()
                                )"""
)

# And insert state for tags
content = content.replace(
    "var transferFeeText by remember { mutableStateOf(\"\") }",
    "var transferFeeText by remember { mutableStateOf(\"\") }\n    var selectedTagIds by remember { mutableStateOf<Set<String>>(emptySet()) }\n    LaunchedEffect(txToEdit) {\n        if (txToEdit != null) {\n            val txTags = getTagsForTx(txToEdit.id)\n            selectedTagIds = txTags.toSet()\n        }\n    }"
)

# Render tags UI in the form, let's put it before Note field
content = content.replace(
    """                        OutlinedTextField(
                            value = notesText,
                            onValueChange = { notesText = it },
                            label = { Text("Catatan (Opsional)") },""",
    """                        if (tags.isNotEmpty()) {
                            Text("Tags (Opsional)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            androidx.compose.foundation.layout.FlowRow(
                                modifier = Modifier.fillMaxWidth(),
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
                        
                        OutlinedTextField(
                            value = notesText,
                            onValueChange = { notesText = it },
                            label = { Text("Catatan (Opsional)") },"""
)

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
