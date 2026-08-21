import sys

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

target = """    activeInstallmentForPayment?.let { installment ->
        val cat = categories.find { it.id == installment.categoryId }
        val isPiutang = cat?.type == "PEMASUKAN"
        var showPaymentDialog by remember { mutableStateOf(true) }
        var selectedPaymentAccountId by remember { mutableStateOf(installment.paymentAccountId) }
        val selectedAccount = accounts.find { it.id == selectedPaymentAccountId }
        val isBalanceSufficient = isPiutang || (selectedAccount != null && selectedAccount.balance >= installment.installmentAmount)"""

replacement = """    activeInstallmentForPayment?.let { installment ->
        val cat = categories.find { it.id == installment.categoryId }
        val isPiutang = cat?.type == "PEMASUKAN"
        var showPaymentDialog by remember { mutableStateOf(true) }
        var selectedPaymentAccountId by remember { mutableStateOf(installment.paymentAccountId) }
        val selectedAccount = accounts.find { it.id == selectedPaymentAccountId }
        var paymentAmountText by remember { mutableStateOf(installment.installmentAmount.toString()) }
        val parsedAmount = paymentAmountText.toLongOrNull() ?: 0L
        val isBalanceSufficient = isPiutang || (selectedAccount != null && selectedAccount.balance >= parsedAmount)"""

content = content.replace(target, replacement)

target2 = """                        Text(if (isPiutang) "Konfirmasi penerimaan uang sebesar Rp${com.example.ui.util.FormatUtils.formatRupiah(installment.installmentAmount)} dari '${installment.name}'?" else "Konfirmasi pembayaran cicilan sebesar Rp${com.example.ui.util.FormatUtils.formatRupiah(installment.installmentAmount)} untuk '${installment.name}'?")"""

replacement2 = """                        Text(if (isPiutang) "Masukkan nominal penerimaan uang dari '${installment.name}':" else "Konfirmasi pembayaran cicilan untuk '${installment.name}':")
                        
                        OutlinedTextField(
                            value = paymentAmountText,
                            onValueChange = { paymentAmountText = it.filter { char -> char.isDigit() } },
                            label = { Text("Nominal (Rp)") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )"""

content = content.replace(target2, replacement2)

target3 = """                        onClick = {
                            viewModel.payInstallment(installment.id, selectedPaymentAccountId, installment.installmentAmount)"""

replacement3 = """                        onClick = {
                            viewModel.payInstallment(installment.id, selectedPaymentAccountId, parsedAmount)"""

content = content.replace(target3, replacement3)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
