import sys

with open('app/src/main/java/com/example/data/repository/FinanceRepository.kt', 'r') as f:
    content = f.read()

target = """    suspend fun addInstallment(installment: Installment) {
        installmentDao.insertInstallment(installment)
    }"""

replacement = """    suspend fun addInstallment(installment: Installment) {
        installmentDao.insertInstallment(installment)
        
        // Find category to know if it's income or expense
        val category = categoryDao.getAllCategories().first().find { it.id == installment.categoryId }
        val type = category?.type ?: "PENGELUARAN"
        
        // Deduct/Add from account for the initial creation
        val account = accountDao.getAccountById(installment.paymentAccountId) ?: return
        
        if (type == "PEMASUKAN") {
            // Piutang: kita meminjamkan uang, saldo kita berkurang
            accountDao.insertAccount(account.copy(balance = account.balance - installment.totalAmount))
            
            // Catat sebagai pengeluaran awal (karena uang keluar dari dompet)
            val transaction = Transaction(
                amount = installment.totalAmount,
                type = "PENGELUARAN",
                categoryId = installment.categoryId,
                subCategoryId = installment.subCategoryId,
                accountId = installment.paymentAccountId,
                notes = "Pemberian Piutang: ${installment.name}",
                date = System.currentTimeMillis()
            )
            transactionDao.insertTransaction(transaction)
        } else {
            // Hutang / Cicilan pinjaman: kita menerima uang, saldo bertambah
            accountDao.insertAccount(account.copy(balance = account.balance + installment.totalAmount))
            
            // Catat sebagai pemasukan awal (karena uang masuk ke dompet)
            val transaction = Transaction(
                amount = installment.totalAmount,
                type = "PEMASUKAN",
                categoryId = installment.categoryId,
                subCategoryId = installment.subCategoryId,
                accountId = installment.paymentAccountId,
                notes = "Penerimaan Pinjaman: ${installment.name}",
                date = System.currentTimeMillis()
            )
            transactionDao.insertTransaction(transaction)
        }
    }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/data/repository/FinanceRepository.kt', 'w') as f:
    f.write(content)

