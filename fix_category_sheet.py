import re

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

# Inside CategorySheet, we need to handle editing subcategories.
# Find the Row that renders each subcategory.
sub_cat_row = """                                Text(sub.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                IconButton(onClick = { onDeleteSubCat(sub) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }"""

# We'll replace it with a state-based editable row.
# But CategorySheet is a @Composable, so we can use a remember state for editing.
# Wait, let's just make a separate composable for the row or use state in the loop.
# Using state in a loop inside LazyColumn/Column is fine if we use `var isEditing by remember { mutableStateOf(false) }` etc.
# Actually, since it's a simple `mySubCats.forEach { sub -> Row { ... } }`, we can do:

new_sub_cat_row = """                                var isEditing by remember(sub.id) { mutableStateOf(false) }
                                var editName by remember(sub.id) { mutableStateOf(sub.name) }
                                
                                if (isEditing) {
                                    OutlinedTextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                    IconButton(onClick = { 
                                        if (editName.isNotBlank() && editName != sub.name) {
                                            onUpdateSubCat(sub.copy(name = editName))
                                        }
                                        isEditing = false 
                                    }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Check, contentDescription = "Simpan", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                } else {
                                    Text(sub.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    Row {
                                        IconButton(onClick = { isEditing = true }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(onClick = { onDeleteSubCat(sub) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }"""

content = content.replace(sub_cat_row, new_sub_cat_row)

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
