import sys

with open('app/src/main/java/com/example/ui/screens/AddInstallmentDialog.kt', 'r') as f:
    content = f.read()

target = """    var dueDateDay by remember { mutableStateOf("1") }"""

replacement = """    var dueDateDay by remember { mutableStateOf("1") }
    var installmentType by remember { mutableStateOf("CICILAN") } // CICILAN or PIUTANG"""

content = content.replace(target, replacement)

target2 = """                Text(
                    text = "Tambah Cicilan / Piutang",
                    style = MaterialTheme.typography.titleLarge
                )"""

replacement2 = """                Text(
                    text = "Tambah Baru",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { installmentType = "CICILAN" }) {
                        RadioButton(selected = installmentType == "CICILAN", onClick = { installmentType = "CICILAN" })
                        Text("Hutang / Cicilan")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { installmentType = "PIUTANG" }) {
                        RadioButton(selected = installmentType == "PIUTANG", onClick = { installmentType = "PIUTANG" })
                        Text("Piutang (Pinjamkan)")
                    }
                }"""
content = content.replace(target2, replacement2)

target3 = """                // Category Dropdown
                var catExpanded by remember { mutableStateOf(false) }
                val selectedCat = categories.find { it.id == selectedCategoryId }
                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCat?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.name} (${if(cat.type == "PEMASUKAN") "Piutang/Uang Masuk" else "Hutang/Cicilan"})") },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }"""

replacement3 = """                // Category Dropdown
                val filteredCategories = categories.filter { if (installmentType == "PIUTANG") it.type == "PEMASUKAN" else it.type != "PEMASUKAN" }
                if (filteredCategories.none { it.id == selectedCategoryId }) {
                    selectedCategoryId = filteredCategories.firstOrNull()?.id ?: ""
                }
                
                var catExpanded by remember { mutableStateOf(false) }
                val selectedCat = filteredCategories.find { it.id == selectedCategoryId }
                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCat?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }
                    ) {
                        filteredCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }"""

content = content.replace(target3, replacement3)

with open('app/src/main/java/com/example/ui/screens/AddInstallmentDialog.kt', 'w') as f:
    f.write(content)
