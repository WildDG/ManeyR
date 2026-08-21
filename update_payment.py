import sys

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

target = """    activeInstallmentForPayment?.let { installment ->
        val cat = categories.find { it.id == installment.categoryId }
        val isPiutang = cat?.type == "PEMASUKAN"
        var showPaymentDialog by remember { mutableStateOf(true) }
        if (showPaymentDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showPaymentDialog = false
                    activeInstallmentForPayment = null
                },
                title = { Text(if (isPiutang) "Terima Pembayaran Piutang" else "Bayar Cicilan") },
                text = { Text(if (isPiutang) "Konfirmasi penerimaan uang sebesar Rp${com.example.ui.util.FormatUtils.formatRupiah(installment.installmentAmount)} dari '${installment.name}'?" else "Konfirmasi pembayaran cicilan sebesar Rp${com.example.ui.util.FormatUtils.formatRupiah(installment.installmentAmount)} untuk '${installment.name}'?") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.payInstallment(installment.id, installment.paymentAccountId, installment.installmentAmount)
                        showPaymentDialog = false
                        activeInstallmentForPayment = null
                    }) {
                        Text("Konfirmasi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showPaymentDialog = false
                        activeInstallmentForPayment = null
                    }) {
                        Text("Batal")
                    }
                }
            )
        }
    }"""

replacement = """    activeInstallmentForPayment?.let { installment ->
        val cat = categories.find { it.id == installment.categoryId }
        val isPiutang = cat?.type == "PEMASUKAN"
        var showPaymentDialog by remember { mutableStateOf(true) }
        var selectedPaymentAccountId by remember { mutableStateOf(installment.paymentAccountId) }
        val selectedAccount = accounts.find { it.id == selectedPaymentAccountId }
        val isBalanceSufficient = isPiutang || (selectedAccount != null && selectedAccount.balance >= installment.installmentAmount)
        
        if (showPaymentDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showPaymentDialog = false
                    activeInstallmentForPayment = null
                },
                title = { Text(if (isPiutang) "Terima Pembayaran Piutang" else "Bayar Cicilan") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(if (isPiutang) "Konfirmasi penerimaan uang sebesar Rp${com.example.ui.util.FormatUtils.formatRupiah(installment.installmentAmount)} dari '${installment.name}'?" else "Konfirmasi pembayaran cicilan sebesar Rp${com.example.ui.util.FormatUtils.formatRupiah(installment.installmentAmount)} untuk '${installment.name}'?")
                        
                        var accExpanded by remember { mutableStateOf(false) }
                        @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
                        androidx.compose.material3.ExposedDropdownMenuBox(
                            expanded = accExpanded,
                            onExpandedChange = { accExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedAccount?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(if (isPiutang) "Masuk ke Akun/Dompet" else "Bayar dari Akun/Dompet") },
                                trailingIcon = { androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = accExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            androidx.compose.material3.ExposedDropdownMenu(
                                expanded = accExpanded,
                                onDismissRequest = { accExpanded = false }
                            ) {
                                accounts.forEach { acc ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(acc.name + " (Rp" + com.example.ui.util.FormatUtils.formatRupiah(acc.balance) + ")") },
                                        onClick = {
                                            selectedPaymentAccountId = acc.id
                                            accExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        if (!isBalanceSufficient && !isPiutang) {
                            Text(
                                "Saldo tidak cukup!",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.payInstallment(installment.id, selectedPaymentAccountId, installment.installmentAmount)
                            showPaymentDialog = false
                            activeInstallmentForPayment = null
                        },
                        enabled = isBalanceSufficient
                    ) {
                        Text("Konfirmasi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showPaymentDialog = false
                        activeInstallmentForPayment = null
                    }) {
                        Text("Batal")
                    }
                }
            )
        }
    }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
        f.write(content)
    print("MainScreen payment updated")
else:
    print("MainScreen payment target not found!")
    # Let's write the target found inside the file for debug
    import re
    m = re.search(r'activeInstallmentForPayment.*?AlertDialog\(.*?\)\s*\}\s*\}', content, re.DOTALL)
    if m:
        print(m.group(0))

