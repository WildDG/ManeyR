import sys

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

# The dialogs to move
dialogs_code = """    if (isAddInstallmentOpen) {
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

# 1. Remove from where they are currently (before "if (isAddTargetOpen) {") inside DashboardPanel
content = content.replace(dialogs_code + "    if (isAddTargetOpen) {", "    if (isAddTargetOpen) {")

# 2. Add them to MainScreen just before the end of the Scaffold content (line 614 approx)
# We can search for the end of transactionToDelete dialog
anchor = """                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

@Composable
fun DashboardPanel("""

new_end = """                    shape = RoundedCornerShape(20.dp)
                )
            }
            
""" + dialogs_code + """        }
    }
}

@Composable
fun DashboardPanel("""

content = content.replace(anchor, new_end)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
