import sys

with open('app/src/main/java/com/example/data/repository/FinanceRepository.kt', 'r') as f:
    content = f.read()

# Add installment imports
import_insert = "import com.example.data.dao.TagDao\nimport com.example.data.dao.TransactionTagDao\nimport com.example.data.model.Installment\nimport com.example.data.dao.InstallmentDao"
content = content.replace("import com.example.data.dao.TagDao\nimport com.example.data.dao.TransactionTagDao", import_insert)

# Add dao properties
dao_insert = "    private val tagDao: TagDao,\n    private val transactionTagDao: TransactionTagDao,\n    private val installmentDao: InstallmentDao"
content = content.replace("    private val tagDao: TagDao,\n    private val transactionTagDao: TransactionTagDao", dao_insert)

# Add flow
flow_insert = "    val recurringTransactions: Flow<List<RecurringTransaction>> = recurringTransactionDao.getAllRecurringTransactions()\n    val installments: Flow<List<Installment>> = installmentDao.getAllInstallments()"
content = content.replace("    val recurringTransactions: Flow<List<RecurringTransaction>> = recurringTransactionDao.getAllRecurringTransactions()", flow_insert)

# Add CRUD
crud_insert = """
    suspend fun addInstallment(installment: Installment) {
        installmentDao.insertInstallment(installment)
    }
    
    suspend fun updateInstallment(installment: Installment) {
        installmentDao.updateInstallment(installment)
    }
    
    suspend fun deleteInstallment(installment: Installment) {
        installmentDao.deleteInstallment(installment)
    }
    
    suspend fun payInstallment(installmentId: Int, accountId: String, amount: Long) {
        // Implement logic to pay installment
        val installments = installmentDao.getAllInstallments().first()
        val installment = installments.find { it.id == installmentId } ?: return
        
        // Deduct/Add from account
        val account = accountDao.getAccountById(accountId) ?: return
        
        // Find category to know if it's income or expense
        val category = categoryDao.getCategoryById(installment.categoryId)
        val type = category?.type ?: "PENGELUARAN"
        
        if (type == "PEMASUKAN") {
            accountDao.insertAccount(account.copy(balance = account.balance + amount))
        } else {
            accountDao.insertAccount(account.copy(balance = account.balance - amount))
        }
        
        // Add transaction
        val transaction = Transaction(
            amount = amount,
            type = type,
            categoryId = installment.categoryId,
            subCategoryId = installment.subCategoryId,
            accountId = accountId,
            notes = "Pembayaran: ${installment.name}",
            date = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(transaction)
        
        // Update installment
        val updatedInstallment = installment.copy(
            paidCount = installment.paidCount + 1,
            remainingCount = (installment.remainingCount - 1).coerceAtLeast(0),
            remainingAmount = (installment.remainingAmount - amount).coerceAtLeast(0L),
            status = if (installment.remainingAmount - amount <= 0) "COMPLETED" else "ACTIVE",
            nextDueDate = calculateNextDueDate(installment.nextDueDate) // simple +1 month
        )
        installmentDao.updateInstallment(updatedInstallment)
    }
    
    private fun calculateNextDueDate(currentDueDate: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = currentDueDate
        cal.add(java.util.Calendar.MONTH, 1)
        return cal.timeInMillis
    }
"""
content = content.replace("    suspend fun addTag(tag: Tag) {", crud_insert + "\n    suspend fun addTag(tag: Tag) {")

with open('app/src/main/java/com/example/data/repository/FinanceRepository.kt', 'w') as f:
    f.write(content)
