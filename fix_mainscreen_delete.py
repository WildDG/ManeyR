import sys

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

target1 = """                        onPayInstallment = { activeInstallmentForPayment = it },
                        onTransfer = { srcId, destId, amt, fee ->"""

replacement1 = """                        onPayInstallment = { activeInstallmentForPayment = it },
                        onDeleteInstallment = { viewModel.deleteInstallment(it) },
                        onTransfer = { srcId, destId, amt, fee ->"""
content = content.replace(target1, replacement1)

target2 = """    onAdjustAsset: (String, Long, Boolean) -> Unit,
    onPayInstallment: (com.example.data.model.Installment) -> Unit = {},
    initialIntentAction: String? = null"""

replacement2 = """    onAdjustAsset: (String, Long, Boolean) -> Unit,
    onPayInstallment: (com.example.data.model.Installment) -> Unit = {},
    onDeleteInstallment: (com.example.data.model.Installment) -> Unit = {},
    initialIntentAction: String? = null"""
content = content.replace(target2, replacement2)

target3 = """                    com.example.ui.components.InstallmentCard(
                        installment = installment,
                        isPiutang = isPiutang,
                        onPay = { onPayInstallment(installment) }
                    )"""

replacement3 = """                    com.example.ui.components.InstallmentCard(
                        installment = installment,
                        isPiutang = isPiutang,
                        onPay = { onPayInstallment(installment) },
                        onDelete = { onDeleteInstallment(installment) }
                    )"""
content = content.replace(target3, replacement3)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
