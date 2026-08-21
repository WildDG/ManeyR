import sys

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

# Add states in MainScreen
state_insert = """
    var isAddInstallmentOpen by remember { mutableStateOf(false) }
    var activeInstallmentForPayment by remember { mutableStateOf<com.example.data.model.Installment?>(null) }
"""

content = content.replace("    var isAddTargetOpen by remember { mutableStateOf(false) }", "    var isAddTargetOpen by remember { mutableStateOf(false) }\n" + state_insert)

# Add callbacks to SavingGoalsPanel
saving_call = """                    SavingGoalsPanel(
                        installments = installments,
                        savingTargets = savingTargets,
                        accounts = accounts,
                        onAddGoal = { isAddTargetOpen = true },
                        onEditGoal = { /* TODO */ },
                        onDeleteGoal = { viewModel.deleteSavingTarget(it) },
                        onSaveToTarget = { targetId, acctId, amt -> viewModel.saveToTarget(targetId, acctId, amt) }
                    )"""

saving_call_new = """                    SavingGoalsPanel(
                        installments = installments,
                        savingTargets = savingTargets,
                        accounts = accounts,
                        onAddGoal = { isAddTargetOpen = true },
                        onEditGoal = { /* TODO */ },
                        onDeleteGoal = { viewModel.deleteSavingTarget(it) },
                        onSaveToTarget = { targetId, acctId, amt -> viewModel.saveToTarget(targetId, acctId, amt) },
                        onAddInstallment = { isAddInstallmentOpen = true },
                        onPayInstallment = { activeInstallmentForPayment = it }
                    )"""

content = content.replace(saving_call, saving_call_new)

# Add Dialogs at the end
dialogs_insert = """    if (isAddInstallmentOpen) {
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
"""

content = content.replace("    if (isAddTargetOpen) {", dialogs_insert + "\n    if (isAddTargetOpen) {")

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
