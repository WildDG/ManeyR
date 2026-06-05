package com.example.data.repository

import com.example.data.dao.AccountDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.SavingTargetDao
import com.example.data.dao.RecurringTransactionDao
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.SavingTarget
import com.example.data.model.Transaction
import com.example.data.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class FinanceRepository(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val savingTargetDao: SavingTargetDao,
    private val recurringTransactionDao: RecurringTransactionDao
) {
    val accounts: Flow<List<Account>> = accountDao.getAllAccounts()
    val categories: Flow<List<Category>> = categoryDao.getAllCategories()
    val transactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val savingTargets: Flow<List<SavingTarget>> = savingTargetDao.getAllSavingTargets()
    val recurringTransactions: Flow<List<RecurringTransaction>> = recurringTransactionDao.getAllRecurringTransactions()

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
                Account("bca", "BCA", 0.0, "AccountBalance", "#1976D2"),
                Account("ovo", "OVO", 0.0, "AccountBalanceWallet", "#673AB7"),
                Account("tunai", "Tunai Dompet", 0.0, "Payments", "#4CAF50")
            )
            defaultAccounts.forEach { accountDao.insertAccount(it) }
        }
    }

    suspend fun updateTransaction(oldTransaction: Transaction, newTransaction: Transaction) {
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

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)

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
                        savingTargetDao.insertSavingTarget(target.copy(currentAmount = (target.currentAmount - transaction.amount).coerceAtLeast(0.0)))
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

    suspend fun deleteAccount(account: Account) {
        accountDao.deleteAccount(account)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    suspend fun addSavingTarget(savingTarget: SavingTarget) {
        savingTargetDao.insertSavingTarget(savingTarget)
    }

    suspend fun deleteSavingTarget(savingTarget: SavingTarget) {
        savingTargetDao.deleteSavingTarget(savingTarget)
    }

    suspend fun saveToTarget(targetId: Int, sourceAccountId: String, amount: Double) {
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
            Account("bca", "BCA", 0.0, "AccountBalance", "#1976D2"),
            Account("ovo", "OVO", 0.0, "AccountBalanceWallet", "#673AB7"),
            Account("tunai", "Tunai Dompet", 0.0, "Payments", "#4CAF50")
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
}
