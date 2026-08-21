import re

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

# We need to remove `if (availableSubCategories.isNotEmpty()) {`
# and its corresponding closing brace.
search_str = """                                // SubCategory Selection
                                val availableSubCategories = subCategories.filter { !it.isArchived && it.categoryId == selectedCategoryId }
                                if (availableSubCategories.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))"""

replace_str = """                                // SubCategory Selection
                                val availableSubCategories = subCategories.filter { !it.isArchived && it.categoryId == selectedCategoryId }
                                
                                Spacer(modifier = Modifier.height(12.dp))"""

content = content.replace(search_str, replace_str)

# Now we need to find the closing brace of `if (availableSubCategories.isNotEmpty()) {`
# It's right before `// Label Transaksi` or similar, or `if (tags.isNotEmpty()) {`
# Let's find `if (tags.isNotEmpty()) {` and the lines before it.

search_str_2 = """                                }
                            }
                        }
                    }

                    if (tags.isNotEmpty()) {"""

replace_str_2 = """                            }
                        }
                    }

                    if (tags.isNotEmpty()) {"""

content = content.replace(search_str_2, replace_str_2)

# Also let's update the text "None" to "Tidak ada sub kategori" when selecting
content = content.replace('text = { Text("None") }', 'text = { Text("Tidak ada (Pilih jika tidak perlu)") }')
content = content.replace('text = selectedSubCategoryObj?.name ?: "None"', 'text = selectedSubCategoryObj?.name ?: "Pilih Sub Kategori..."')


with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
