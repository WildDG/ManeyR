import sys

with open('app/src/main/java/com/example/ui/screens/AddInstallmentDialog.kt', 'r') as f:
    content = f.read()

target1 = """fun AddInstallmentDialog(
    accounts: List<Account>,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Installment) -> Unit
) {"""

replacement1 = """fun AddInstallmentDialog(
    accounts: List<Account>,
    categories: List<Category>,
    installmentToEdit: Installment? = null,
    onDismiss: () -> Unit,
    onSave: (Installment) -> Unit
) {"""
content = content.replace(target1, replacement1)

target2 = """    var name by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var tenor by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var dueDateDay by remember { mutableStateOf("1") }
    var installmentType by remember { mutableStateOf("CICILAN") } // CICILAN or PIUTANG"""

replacement2 = """    var name by remember { mutableStateOf(installmentToEdit?.name ?: "") }
    var totalAmount by remember { mutableStateOf(installmentToEdit?.totalAmount?.toString() ?: "") }
    var tenor by remember { mutableStateOf(installmentToEdit?.totalInstallments?.toString() ?: "") }
    var selectedCategoryId by remember { mutableStateOf(installmentToEdit?.categoryId ?: categories.firstOrNull()?.id ?: "") }
    var selectedAccountId by remember { mutableStateOf(installmentToEdit?.paymentAccountId ?: accounts.firstOrNull()?.id ?: "") }
    
    val initialType = remember {
        if (installmentToEdit != null) {
            val cat = categories.find { it.id == installmentToEdit.categoryId }
            if (cat?.type == "PEMASUKAN") "PIUTANG" else "CICILAN"
        } else "CICILAN"
    }
    var installmentType by remember { mutableStateOf(initialType) }"""
content = content.replace(target2, replacement2)

target3 = """                val datePickerState = androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())"""
replacement3 = """                val datePickerState = androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = installmentToEdit?.firstDueDate ?: System.currentTimeMillis())"""
content = content.replace(target3, replacement3)

target4 = """                            val installment = Installment(
                                name = name,
                                totalAmount = total,
                                installmentAmount = installmentAmt,
                                totalInstallments = months,
                                remainingCount = months,
                                remainingAmount = total,
                                paymentAccountId = selectedAccountId,
                                firstDueDate = selectedDateMillis,
                                nextDueDate = selectedDateMillis,
                                categoryId = selectedCategoryId,
                                status = "ACTIVE"
                            )"""

replacement4 = """                            val installment = installmentToEdit?.copy(
                                name = name,
                                totalAmount = total,
                                totalInstallments = months,
                                paymentAccountId = selectedAccountId,
                                categoryId = selectedCategoryId,
                                firstDueDate = selectedDateMillis
                            ) ?: Installment(
                                name = name,
                                totalAmount = total,
                                installmentAmount = installmentAmt,
                                totalInstallments = months,
                                remainingCount = months,
                                remainingAmount = total,
                                paymentAccountId = selectedAccountId,
                                firstDueDate = selectedDateMillis,
                                nextDueDate = selectedDateMillis,
                                categoryId = selectedCategoryId,
                                status = "ACTIVE"
                            )"""
content = content.replace(target4, replacement4)

with open('app/src/main/java/com/example/ui/screens/AddInstallmentDialog.kt', 'w') as f:
    f.write(content)
