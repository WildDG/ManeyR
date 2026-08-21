import sys

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

# 1. Add onPayInstallment to DashboardPanel signature
target_sig = """    onAdjustAsset: (String, Long, Boolean) -> Unit,
    initialIntentAction: String? = null
) {"""

replacement_sig = """    onAdjustAsset: (String, Long, Boolean) -> Unit,
    onPayInstallment: (com.example.data.model.Installment) -> Unit = {},
    initialIntentAction: String? = null
) {"""
content = content.replace(target_sig, replacement_sig)

# 2. Add onPayInstallment to DashboardPanel invocation
target_call = """                        monthOffset = currentMonthOffset,
                        recurringTransactions = recurringTransactions,
                        onTransfer = { srcId, destId, amt, fee ->"""

replacement_call = """                        monthOffset = currentMonthOffset,
                        recurringTransactions = recurringTransactions,
                        onPayInstallment = { activeInstallmentForPayment = it },
                        onTransfer = { srcId, destId, amt, fee ->"""
content = content.replace(target_call, replacement_call)

# 3. Add Piutang & Cicilan Section before Riwayat Transaksi
target_history = """        // Transactions History Header
        item {
            Text(
                text = "Riwayat Transaksi","""

replacement_history = """        // Piutang & Cicilan Section
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
        }

        // Transactions History Header
        item {
            Text(
                text = "Riwayat Transaksi","""
content = content.replace(target_history, replacement_history)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)

