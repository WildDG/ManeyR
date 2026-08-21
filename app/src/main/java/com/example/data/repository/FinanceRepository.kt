package com.example.data.repository

import com.example.data.dao.AccountDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.SavingTargetDao
import com.example.data.dao.RecurringTransactionDao
import com.example.data.dao.SubCategoryDao
import com.example.data.dao.TagDao
import com.example.data.dao.TransactionTagDao
import com.example.data.model.Installment
import com.example.data.dao.InstallmentDao
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.SavingTarget
import com.example.data.model.Transaction
import com.example.data.model.RecurringTransaction
import com.example.data.model.SubCategory
import com.example.data.model.Tag
import com.example.data.model.TransactionTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class FinanceRepository(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val subCategoryDao: SubCategoryDao,
    private val transactionDao: TransactionDao,
    private val savingTargetDao: SavingTargetDao,
    private val recurringTransactionDao: RecurringTransactionDao,
    private val tagDao: TagDao,
    private val transactionTagDao: TransactionTagDao,
    private val installmentDao: InstallmentDao
) {
    val accounts: Flow<List<Account>> = accountDao.getAllAccounts()
    val categories: Flow<List<Category>> = categoryDao.getAllCategories()
    val subCategories: Flow<List<SubCategory>> = subCategoryDao.getAllSubCategories()
    val transactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val savingTargets: Flow<List<SavingTarget>> = savingTargetDao.getAllSavingTargets()
    val recurringTransactions: Flow<List<RecurringTransaction>> = recurringTransactionDao.getAllRecurringTransactions()
    val installments: Flow<List<Installment>> = installmentDao.getAllInstallments()
    val tags: Flow<List<Tag>> = tagDao.getAllTags()
    suspend fun getTagIdsForTransactionSync(txId: Int): List<String> = transactionTagDao.getTagIdsForTransactionSync(txId)

    fun getTagIdsForTransaction(transactionId: Int): Flow<List<String>> = transactionTagDao.getTagIdsForTransaction(transactionId)

    suspend fun initializeDefaults() {
        // Seed default categories if empty
        val currentCategories = categories.first()
        if (currentCategories.isEmpty()) {
            val defaultCategories = listOf(
                Category("makanan", "Makanan", "Fastfood", "PENGELUARAN", "#FF5722"),
                Category("transportasi", "Transportasi", "DirectionsCar", "PENGELUARAN", "#2196F3"),
                Category("belanja", "Belanja", "ShoppingCart", "PENGELUARAN", "#E91E63"),
                Category("kesehatan", "Kesehatan", "MedicalServices", "PENGELUARAN", "#4CAF50"),
                Category("hiburan", "Hiburan", "SportsEsports", "PENGELUARAN", "#9C27B0"),
                Category("tagihan", "Tagihan & Pulsa", "Receipt", "PENGELUARAN", "#FFC107"),
                Category("tabungan", "Tabungan", "Star", "PENGELUARAN", "#FF9800"),
                Category("gaji", "Gaji", "MonetizationOn", "PEMASUKAN", "#4CAF50"),
                Category("bonus", "Bonus", "Redeem", "PEMASUKAN", "#8BC34A"),
                Category("investasi", "Investasi", "TrendingUp", "PEMASUKAN", "#009688"),
                Category("pemasukan_lain", "Lain-lain", "AttachMoney", "PEMASUKAN", "#03A9F4")
            )
            categoryDao.insertCategories(defaultCategories)
        }

        // Seed default accounts
        val currentAccounts = accountDao.getAllAccounts().first()
        if (currentAccounts.isEmpty()) {
            val defaultAccounts = listOf(
                Account("bca", "BCA", 0L, "AccountBalance", "#1976D2"),
                Account("ovo", "OVO", 0L, "AccountBalanceWallet", "#673AB7"),
                Account("tunai", "Tunai Dompet", 0L, "Payments", "#4CAF50")
            )
            defaultAccounts.forEach { accountDao.insertAccount(it) }
        }
    }

    suspend fun updateTransaction(oldTransaction: Transaction, newTransaction: Transaction, tagIds: List<String> = emptyList()) {
        // Reverse old balance
        when (oldTransaction.type) {
            "PEMASUKAN" -> {
                accountDao.getAccountById(oldTransaction.accountId)?.let { acct ->
                    accountDao.insertAccount(acct.copy(balance = acct.balance - oldTransaction.amount))
                }
            }
            "PENGELUARAN" -> {
                accountDao.getAccountById(oldTransaction.accountId)?.let { acct ->
                    accountDao.insertAccount(acct.copy(balance = acct.balance + oldTransaction.amount))
                }
            }
            "TRANSFER" -> {
                accountDao.getAccountById(oldTransaction.accountId)?.let { src ->
                    accountDao.insertAccount(src.copy(balance = src.balance + oldTransaction.amount))
                }
                oldTransaction.destAccountId?.let { destId ->
                    accountDao.getAccountById(destId)?.let { dest ->
                        accountDao.insertAccount(dest.copy(balance = dest.balance - oldTransaction.amount))
                    }
                }
            }
        }

        // Apply new transaction
        transactionDao.updateTransaction(newTransaction)
        transactionTagDao.deleteTagsForTransaction(newTransaction.id)
        if (tagIds.isNotEmpty()) {
            val txTags = tagIds.map { TransactionTag(newTransaction.id, it) }
            transactionTagDao.insertTransactionTags(txTags)
        }

        // Adjust new balance
        when (newTransaction.type) {
            "PEMASUKAN" -> {
                accountDao.getAccountById(newTransaction.accountId)?.let { acct ->
                    accountDao.insertAccount(acct.copy(balance = acct.balance + newTransaction.amount))
                }
            }
            "PENGELUARAN" -> {
                accountDao.getAccountById(newTransaction.accountId)?.let { acct ->
                    accountDao.insertAccount(acct.copy(balance = acct.balance - newTransaction.amount))
                }
            }
            "TRANSFER" -> {
                accountDao.getAccountById(newTransaction.accountId)?.let { src ->
                    accountDao.insertAccount(src.copy(balance = src.balance - newTransaction.amount))
                }
                newTransaction.destAccountId?.let { destId ->
                    accountDao.getAccountById(destId)?.let { dest ->
                        accountDao.insertAccount(dest.copy(balance = dest.balance + newTransaction.amount))
                    }
                }
            }
        }
    }

    suspend fun addTransaction(transaction: Transaction, tagIds: List<String> = emptyList()) {
        val id = transactionDao.insertTransaction(transaction)
        if (tagIds.isNotEmpty()) {
            val txTags = tagIds.map { TransactionTag(id.toInt(), it) }
            transactionTagDao.insertTransactionTags(txTags)
        }

        // Adjust balance
        when (transaction.type) {
            "PEMASUKAN" -> {
                accountDao.getAccountById(transaction.accountId)?.let { acct ->
                    accountDao.insertAccount(acct.copy(balance = acct.balance + transaction.amount))
                }
            }
            "PENGELUARAN" -> {
                accountDao.getAccountById(transaction.accountId)?.let { acct ->
                    accountDao.insertAccount(acct.copy(balance = acct.balance - transaction.amount))
                }
            }
            "TRANSFER" -> {
                // Debit from source
                accountDao.getAccountById(transaction.accountId)?.let { src ->
                    accountDao.insertAccount(src.copy(balance = src.balance - transaction.amount))
                }
                // Credit to destination
                transaction.destAccountId?.let { destId ->
                    accountDao.getAccountById(destId)?.let { dest ->
                        accountDao.insertAccount(dest.copy(balance = dest.balance + transaction.amount))
                    }
                }
            }
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)

        // Reverse balance adjustment
        when (transaction.type) {
            "PEMASUKAN" -> {
                accountDao.getAccountById(transaction.accountId)?.let { acct ->
                    accountDao.insertAccount(acct.copy(balance = acct.balance - transaction.amount))
                }
            }
            "PENGELUARAN" -> {
                accountDao.getAccountById(transaction.accountId)?.let { acct ->
                    accountDao.insertAccount(acct.copy(balance = acct.balance + transaction.amount))
                }
                
                // If it was a savings target deposit, reverse the progress too!
                if (transaction.notes.startsWith("Tabungan: ")) {
                    val targetName = transaction.notes.substringAfter("Tabungan: ")
                    val targets = savingTargetDao.getAllSavingTargets().first()
                    targets.find { it.name == targetName }?.let { target ->
                        savingTargetDao.insertSavingTarget(target.copy(currentAmount = (target.currentAmount - transaction.amount).coerceAtLeast(0L)))
                    }
                }
            }
            "TRANSFER" -> {
                // Return money to source
                accountDao.getAccountById(transaction.accountId)?.let { src ->
                    accountDao.insertAccount(src.copy(balance = src.balance + transaction.amount))
                }
                // Claw back money from destination
                transaction.destAccountId?.let { destId ->
                    accountDao.getAccountById(destId)?.let { dest ->
                        accountDao.insertAccount(dest.copy(balance = dest.balance - transaction.amount))
                    }
                }
            }
        }
    }

    suspend fun addAccount(account: Account) {
        accountDao.insertAccount(account)
    }

    suspend fun addCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    suspend fun addSubCategory(subCategory: SubCategory) {
        subCategoryDao.insertSubCategory(subCategory)
    }

    suspend fun deleteAccount(account: Account) {
        accountDao.deleteAccount(account)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    suspend fun deleteSubCategory(subCategory: SubCategory) {
        subCategoryDao.deleteSubCategory(subCategory)
    }

    suspend fun addSavingTarget(savingTarget: SavingTarget) {
        savingTargetDao.insertSavingTarget(savingTarget)
    }

    suspend fun deleteSavingTarget(savingTarget: SavingTarget) {
        savingTargetDao.deleteSavingTarget(savingTarget)
    }

    suspend fun saveToTarget(targetId: Int, sourceAccountId: String, amount: Long) {
        val target = savingTargetDao.getSavingTargetById(targetId) ?: return
        val sourceAccount = accountDao.getAccountById(sourceAccountId) ?: return

        // Update target currentAmount
        savingTargetDao.insertSavingTarget(target.copy(currentAmount = target.currentAmount + amount))

        // Deduct from account balance
        accountDao.insertAccount(sourceAccount.copy(balance = sourceAccount.balance - amount))

        // Add transaction entry
        val transaction = Transaction(
            amount = amount,
            type = "PENGELUARAN",
            categoryId = "tabungan",
            accountId = sourceAccountId,
            notes = "Tabungan: ${target.name}",
            date = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(transaction)
    }

    suspend fun addRecurringTransaction(recurring: RecurringTransaction) {
        recurringTransactionDao.insertRecurringTransaction(recurring)
    }

    suspend fun updateRecurringTransaction(recurring: RecurringTransaction) {
        recurringTransactionDao.updateRecurringTransaction(recurring)
    }

    suspend fun deleteRecurringTransaction(recurring: RecurringTransaction) {
        recurringTransactionDao.deleteRecurringTransaction(recurring)
    }

    suspend fun checkAndApplyRecurringTransactions() {
        val recurrings = recurringTransactionDao.getAllRecurringTransactions().first()
        val today = Calendar.getInstance()
        val currentDay = today.get(Calendar.DAY_OF_MONTH)
        val currentYear = today.get(Calendar.YEAR)
        val currentMonth = today.get(Calendar.MONTH)

        for (rec in recurrings) {
            if (rec.isPaused) continue

            val lastApplied = Calendar.getInstance()
            val hasBeenAppliedThisMonth = if (rec.lastAppliedTimestamp > 0L) {
                lastApplied.timeInMillis = rec.lastAppliedTimestamp
                lastApplied.get(Calendar.YEAR) == currentYear && lastApplied.get(Calendar.MONTH) == currentMonth
            } else {
                false
            }

            if (!hasBeenAppliedThisMonth && currentDay >= rec.dayOfMonth) {
                val notesWithTag = if (rec.notes.isEmpty()) "Transaksi Rutin: ${rec.name}" else "Transaksi Rutin: ${rec.name} (${rec.notes})"
                val txn = Transaction(
                    amount = rec.amount,
                    type = rec.type,
                    categoryId = rec.categoryId,
                    accountId = rec.accountId,
                    date = System.currentTimeMillis(),
                    notes = notesWithTag
                )
                addTransaction(txn)
                recurringTransactionDao.insertRecurringTransaction(
                    rec.copy(lastAppliedTimestamp = System.currentTimeMillis())
                )
            }
        }
    }

    suspend fun restoreDefaultAccounts() {
        val currentAccounts = accountDao.getAllAccounts().first()
        val defaultAccounts = listOf(
            Account("bca", "BCA", 0L, "AccountBalance", "#1976D2"),
            Account("ovo", "OVO", 0L, "AccountBalanceWallet", "#673AB7"),
            Account("tunai", "Tunai Dompet", 0L, "Payments", "#4CAF50")
        )
        for (da in defaultAccounts) {
            if (currentAccounts.none { it.id == da.id }) {
                accountDao.insertAccount(da)
            }
        }
    }

    suspend fun clearAllData() {
        transactionDao.deleteAllTransactions()
        savingTargetDao.deleteAllSavingTargets()
        accountDao.deleteAllAccounts()
        categoryDao.deleteAllCategories()
        recurringTransactionDao.deleteAllRecurringTransactions()
        initializeDefaults()
    }


    suspend fun addInstallment(installment: Installment) {
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
        val category = categoryDao.getAllCategories().first().find { it.id == installment.categoryId }
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

    suspend fun addTag(tag: Tag) {
        tagDao.insertTag(tag)
    }
    suspend fun updateTag(tag: Tag) {
        tagDao.updateTag(tag)
    }
    suspend fun deleteTag(tag: Tag) {
        tagDao.deleteTag(tag)
    }
}
