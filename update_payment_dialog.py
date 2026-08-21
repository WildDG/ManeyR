import sys

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

target = """    activeInstallmentForPayment?.let { installment ->
        var showPaymentDialog by remember { mutableStateOf(true) }
        if (showPaymentDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showPaymentDialog = false
                    activeInstallmentForPayment = null
                },
                title = { Text("Bayar Cicilan / Piutang") },
                text = { Text("Konfirmasi pembayaran sebesar Rp${com.example.ui.util.FormatUtils.formatRupiah(installment.installmentAmount)} untuk '${installment.name}'?") },
                confirmButton = {"""

replacement = """    activeInstallmentForPayment?.let { installment ->
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
                confirmButton = {"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)

