import re

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

# 1. Update signature of AddTransactionDialog
old_sig = """    onSave: (Long, String, String, String?, String, String?, Long, String, Long, List<String>) -> Unit,
    onAddCategoryDirectly: (Category) -> Unit
) {"""
new_sig = """    onSave: (Long, String, String, String?, String, String?, Long, String, Long, List<String>) -> Unit,
    onAddCategoryDirectly: (Category) -> Unit,
    onAddSubCatDirectly: (String, String) -> Unit = { _, _ -> }
) {"""
content = content.replace(old_sig, new_sig)

# 2. Add input state in Subcategory section
# Find: `var subCategoryExpanded by remember { mutableStateOf(false) }`
old_subcat_state = "                                    var subCategoryTriggerWidth by remember { mutableStateOf(0) }"
new_subcat_state = old_subcat_state + "\n                                    var newSubCatName by remember { mutableStateOf(\"\") }"
content = content.replace(old_subcat_state, new_subcat_state)

# 3. Add the input row inside the DropdownMenu for subcategories
old_subcat_menu = """                                            availableSubCategories.forEach { subCat ->
                                                androidx.compose.material3.DropdownMenuItem(
                                                    text = { Text(subCat.name) },
                                                    onClick = {
                                                        selectedSubCategoryId = subCat.id
                                                        subCategoryExpanded = false
                                                    }
                                                )
                                            }
                                        }"""

new_subcat_menu = """                                            availableSubCategories.forEach { subCat ->
                                                androidx.compose.material3.DropdownMenuItem(
                                                    text = { Text(subCat.name) },
                                                    onClick = {
                                                        selectedSubCategoryId = subCat.id
                                                        subCategoryExpanded = false
                                                    }
                                                )
                                            }
                                            androidx.compose.material3.DropdownMenuItem(
                                                text = { 
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                                                        OutlinedTextField(
                                                            value = newSubCatName,
                                                            onValueChange = { newSubCatName = it },
                                                            modifier = Modifier.weight(1f).height(48.dp),
                                                            placeholder = { Text("Tambah Baru", fontSize = 12.sp) },
                                                            singleLine = true,
                                                            textStyle = MaterialTheme.typography.bodySmall
                                                        )
                                                        IconButton(onClick = { 
                                                            if (newSubCatName.isNotBlank()) {
                                                                onAddSubCatDirectly(newSubCatName, selectedCategoryId)
                                                                newSubCatName = ""
                                                            }
                                                        }, modifier = Modifier.size(32.dp)) {
                                                            Icon(Icons.Default.Add, contentDescription = "Tambah", tint = MaterialTheme.colorScheme.primary)
                                                        }
                                                    }
                                                },
                                                onClick = {}
                                            )
                                        }"""
content = content.replace(old_subcat_menu, new_subcat_menu)

# 4. Pass the callback from MainScreen
old_call = """                    onAddCategoryDirectly = { newCat ->
                        viewModel.updateCustomCategory(newCat)
                    }
                )"""
new_call = """                    onAddCategoryDirectly = { newCat ->
                        viewModel.updateCustomCategory(newCat)
                    },
                    onAddSubCatDirectly = { name, catId ->
                        viewModel.addCustomSubCategory(name, catId)
                    }
                )"""
content = content.replace(old_call, new_call)

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
