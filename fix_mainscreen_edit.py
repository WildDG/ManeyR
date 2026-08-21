import sys

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

target1 = """    var isAddInstallmentOpen by remember { mutableStateOf(false) }
    var activeInstallmentForPayment by remember { mutableStateOf<com.example.data.model.Installment?>(null) }"""

replacement1 = """    var isAddInstallmentOpen by remember { mutableStateOf(false) }
    var activeInstallmentForPayment by remember { mutableStateOf<com.example.data.model.Installment?>(null) }
    var installmentToEdit by remember { mutableStateOf<com.example.data.model.Installment?>(null) }"""
content = content.replace(target1, replacement1)

target2 = """    if (isAddInstallmentOpen) {
        AddInstallmentDialog(
            accounts = accounts,
            categories = categories,
            onDismiss = { isAddInstallmentOpen = false },
            onSave = { 
                viewModel.addInstallment(it)
                isAddInstallmentOpen = false
            }
        )
    }"""

replacement2 = """    if (isAddInstallmentOpen) {
        AddInstallmentDialog(
            accounts = accounts,
            categories = categories,
            installmentToEdit = installmentToEdit,
            onDismiss = { 
                isAddInstallmentOpen = false 
                installmentToEdit = null
            },
            onSave = { 
                if (installmentToEdit != null) {
                    viewModel.updateInstallment(it)
                } else {
                    viewModel.addInstallment(it)
                }
                isAddInstallmentOpen = false
                installmentToEdit = null
            }
        )
    }"""
content = content.replace(target2, replacement2)

target3 = """                    com.example.ui.components.InstallmentCard(
                        installment = installment,
                        isPiutang = isPiutang,
                        onPay = { onPayInstallment(installment) },
                        onDelete = { onDeleteInstallment(installment) }
                    )"""

replacement3 = """                    com.example.ui.components.InstallmentCard(
                        installment = installment,
                        isPiutang = isPiutang,
                        onPay = { onPayInstallment(installment) },
                        onDelete = { onDeleteInstallment(installment) },
                        onEdit = { 
                            installmentToEdit = installment
                            isAddInstallmentOpen = true 
                        }
                    )"""
content = content.replace(target3, replacement3)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
