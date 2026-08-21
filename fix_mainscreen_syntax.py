import sys

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

# First, remove the dialogs that were put outside MainScreen
dialogs_outside = """
    if (isAddInstallmentOpen) {
        AddInstallmentDialog(
            accounts = accounts,
            categories = categories,
            onDismiss = { isAddInstallmentOpen = false },
            onSave = { 
                viewModel.addInstallment(it)
                isAddInstallmentOpen = false
            }
        )
    }
    
    activeInstallmentForPayment?.let { installment ->
        var showPaymentDialog by remember { mutableStateOf(true) }
        if (showPaymentDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showPaymentDialog = false
                    activeInstallmentForPayment = null
                },
                title = { Text("Bayar Cicilan / Piutang") },
                text = { Text("Konfirmasi pembayaran sebesar Rp${com.example.ui.util.FormatUtils.formatRupiah(installment.installmentAmount)} untuk '${installment.name}'?") },
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
    }
}

@Composable
fun DashboardPanel("""

if dialogs_outside in content:
    content = content.replace(dialogs_outside, "    }\n}\n\n@Composable\nfun DashboardPanel(")

# Then, place the dialogs back right before if (isAddTargetOpen) {
dialogs_inside = """    if (isAddInstallmentOpen) {
        AddInstallmentDialog(
            accounts = accounts,
            categories = categories,
            onDismiss = { isAddInstallmentOpen = false },
            onSave = { 
                viewModel.addInstallment(it)
                isAddInstallmentOpen = false
            }
        )
    }
    
    activeInstallmentForPayment?.let { installment ->
        var showPaymentDialog by remember { mutableStateOf(true) }
        if (showPaymentDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showPaymentDialog = false
                    activeInstallmentForPayment = null
                },
                title = { Text("Bayar Cicilan / Piutang") },
                text = { Text("Konfirmasi pembayaran sebesar Rp${com.example.ui.util.FormatUtils.formatRupiah(installment.installmentAmount)} untuk '${installment.name}'?") },
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
    }

    if (isAddTargetOpen) {"""

if "    if (isAddTargetOpen) {" in content and "AddInstallmentDialog(" not in content:
    content = content.replace("    if (isAddTargetOpen) {", dialogs_inside)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
