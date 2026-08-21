import sys

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

# The wrongly inserted dialogs are from '    if (isAddInstallmentOpen) {'
# up to '        var showPaymentDialog by remember { mutableStateOf(true) }' and their ends.
# I will use string manipulation to remove them.

start_str = "    if (isAddInstallmentOpen) {\n        AddInstallmentDialog("
end_str = "    if (isAddTargetOpen) {\n        AddSavingTargetDialog("

idx1 = content.find(start_str)
idx2 = content.find(end_str)

if idx1 != -1 and idx2 != -1:
    content = content[:idx1] + content[idx2:]

# Now let's place them properly in MainScreen
# Let's place them at the end of MainScreen, before DashboardPanel

target_placement = "    }\n}\n\n@Composable\nfun DashboardPanel("
dialogs_correct = """
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

content = content.replace(target_placement, dialogs_correct)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
