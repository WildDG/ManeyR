import sys

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

state_target = """    var isUpcomingExpanded by remember { mutableStateOf(false) }"""
state_replacement = """    var isUpcomingExpanded by remember { mutableStateOf(false) }
    var isInstallmentsExpanded by rememberSaveable { mutableStateOf(true) }"""

if state_target in content:
    content = content.replace(state_target, state_replacement)
else:
    print("State target not found")

section_target = """        // Piutang & Cicilan Section
        val activeInstallments = installments.filter { it.status == "ACTIVE" }
        if (activeInstallments.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Piutang & Cicilan Aktif 💳",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            items(activeInstallments, key = { "inst_${it.id}" }) { installment ->
                val cat = categories.find { it.id == installment.categoryId }
                val isPiutang = cat?.type == "PEMASUKAN"
                
                com.example.ui.components.InstallmentCard(
                    installment = installment,
                    isPiutang = isPiutang,
                    onPay = { onPayInstallment(installment) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }"""

section_replacement = """        // Piutang & Cicilan Section
        val activeInstallments = installments.filter { it.status == "ACTIVE" }
        if (activeInstallments.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 4.dp)
                        .clickable { isInstallmentsExpanded = !isInstallmentsExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Piutang & Cicilan Aktif 💳",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = if (isInstallmentsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand/Collapse",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (isInstallmentsExpanded) {
                items(activeInstallments, key = { "inst_${it.id}" }) { installment ->
                    val cat = categories.find { it.id == installment.categoryId }
                    val isPiutang = cat?.type == "PEMASUKAN"
                    
                    com.example.ui.components.InstallmentCard(
                        installment = installment,
                        isPiutang = isPiutang,
                        onPay = { onPayInstallment(installment) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }"""

if section_target in content:
    content = content.replace(section_target, section_replacement)
    with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
        f.write(content)
    print("Hide/Show updated")
else:
    print("Hide/Show target not found!")

