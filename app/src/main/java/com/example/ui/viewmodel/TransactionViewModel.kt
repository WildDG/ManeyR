package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.SubCategory
import com.example.data.model.Installment
import com.example.data.model.SavingAllocation
import com.example.data.model.SavingTarget
import com.example.data.model.Transaction
import com.example.data.model.Tag
import com.example.data.model.RecurringTransaction
import com.example.data.repository.FinanceRepository
import com.example.data.repository.PreferencesManager
import com.example.data.repository.ThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TransactionViewModel(
    private val repository: FinanceRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _currentMonthOffset = MutableStateFlow(0)
    val currentMonthOffset: StateFlow<Int> = _currentMonthOffset.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.initializeDefaults()
                repository.checkAndApplyRecurringTransactions()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val accounts: StateFlow<List<Account>> = repository.accounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringTransactions: StateFlow<List<RecurringTransaction>> = repository.recurringTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subCategories: StateFlow<List<SubCategory>> = repository.subCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val installments: StateFlow<List<Installment>> = repository.installments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val savingTargets: StateFlow<List<SavingTarget>> = repository.savingTargets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tags: StateFlow<List<Tag>> = repository.tags
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getTagIdsForTransactionSync(transactionId: Int): List<String> = repository.getTagIdsForTransactionSync(transactionId)

    val savings: StateFlow<List<SavingTarget>> = repository.savingTargets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val themeMode: StateFlow<ThemeMode> = preferencesManager.themeMode
    val targetAppColorHex: StateFlow<String> = preferencesManager.targetAppColorHex
    val globalBudgetLimit: StateFlow<Long> = preferencesManager.globalBudgetLimit

    // Keep backwards compatibility for MainScreen if it uses isDarkModeEnabled still
    val isDarkModeEnabled: StateFlow<Boolean?> = themeMode.map { mode ->
        when (mode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> null
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setThemeMode(mode: ThemeMode) {
        preferencesManager.setThemeMode(mode)
    }
    
    fun setAppColorHex(hex: String) {
        preferencesManager.setAppColorHex(hex)
    }

    fun setGlobalBudgetLimit(limit: Long) {
        preferencesManager.setGlobalBudgetLimit(limit)
    }

    fun toggleDarkMode(currentSystemDark: Boolean) {
        val current = isDarkModeEnabled.value ?: currentSystemDark
        setThemeMode(if (current) ThemeMode.LIGHT else ThemeMode.DARK)
    }

    // Readable label (e.g., "Juni 2026")
    val selectedMonthLabel: StateFlow<String> = _currentMonthOffset
        .map { offset -> getMonthYearLabel(offset) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // Filtered transaction list for the active month
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        repository.transactions,
        _currentMonthOffset
    ) { allTransactions, offset ->
        val (start, end) = getStartAndEndEpochs(offset)
        allTransactions.filter { it.date in start..end }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary calculations
    val totalBalance: StateFlow<Long> = repository.accounts
        .map { list -> list.sumOf { it.balance } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val monthlyIncome: StateFlow<Long> = filteredTransactions
        .map { list -> list.filter { it.type == "PEMASUKAN" }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val monthlyExpense: StateFlow<Long> = filteredTransactions
        .map { list -> list.filter { it.type == "PENGELUARAN" }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // Expense items broken down by category for chart
    val categoryBreakdown: StateFlow<List<CategoryShare>> = combine(
        filteredTransactions,
        categories,
        subCategories
    ) { filteredTx, cats, subCats ->
        val expenseTx = filteredTx.filter { it.type == "PENGELUARAN" }
        val totalExp = expenseTx.sumOf { it.amount }
        if (totalExp == 0L) emptyList()
        else {
            val grouped = expenseTx.groupBy { it.categoryId }
            grouped.map { (catId, txs) ->
                val cat = cats.find { it.id == catId } ?: Category(catId, catId, "QuestionMark", "PENGELUARAN", "#9E9E9E")
                val amount = txs.sumOf { it.amount }
                
                val subCatGrouped = txs.groupBy { it.subCategoryId }
                val subCatShares = subCatGrouped.map { (subCatId, subTxs) ->
                    val subCat = subCats.find { it.id == subCatId }
                    SubCategoryShare(
                        subCategory = subCat,
                        amount = subTxs.sumOf { it.amount }
                    )
                }.sortedByDescending { it.amount }
                
                CategoryShare(
                    category = cat,
                    amount = amount,
                    percentage = (amount.toDouble() / totalExp.toDouble()) * 100.0,
                    subCategoryShares = subCatShares
                )
            }.sortedByDescending { it.amount }
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun nextMonth() {
        _currentMonthOffset.value += 1
    }

    fun previousMonth() {
        _currentMonthOffset.value -= 1
    }

    fun resetMonth() {
        _currentMonthOffset.value = 0
    }

    fun addTransaction(
        amount: Long,
        type: String,
        categoryId: String,
        subCategoryId: String? = null,
        accountId: String,
        destAccountId: String? = null,
        date: Long = System.currentTimeMillis(),
        notes: String = "",
        tagIds: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    subCategoryId = subCategoryId,
                    accountId = accountId,
                    destAccountId = destAccountId,
                    date = date,
                    notes = notes
                )
                repository.addTransaction(transaction, tagIds)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun quickAddTransaction(amount: Long, type: String) {
        viewModelScope.launch {
            try {
                val existingAccounts = repository.accounts.first()
                val targetAcctId = if (existingAccounts.isEmpty()) {
                    val fallbackAccStr = "bca"
                    val fallbackAcc = Account(
                        id = fallbackAccStr,
                        name = "BCA",
                        balance = 0L,
                        iconName = "AccountBalance",
                        colorHex = "#2196F3"
                    )
                    repository.addAccount(fallbackAcc)
                    fallbackAccStr
                } else {
                    existingAccounts.first().id
                }
                
                val catId = if (type == "PEMASUKAN") {
                    "gaji"
                } else {
                    "makanan"
                }
                
                val tx = Transaction(
                    amount = amount,
                    type = type,
                    categoryId = catId,
                    accountId = targetAcctId,
                    destAccountId = null,
                    date = java.lang.System.currentTimeMillis(),
                    notes = "Quick Add"
                )
                repository.addTransaction(tx)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addAdjustAssetTransaction(srcId: String, amt: Long, isPemasukan: Boolean) {
        viewModelScope.launch {
            try {
                val type = if (isPemasukan) "PEMASUKAN" else "PENGELUARAN"
                val allCats = repository.categories.first()
                var targetCat = allCats.firstOrNull { it.name.trim().lowercase() == "edit saldo" && it.type == type }
                if (targetCat == null) {
                    targetCat = allCats.firstOrNull { it.name.contains("Edit Saldo", ignoreCase = true) && it.type == type }
                }

                val catId = if (targetCat != null) {
                    targetCat.id
                } else {
                    val id = "edit_saldo_${System.currentTimeMillis() % 1000}_$type"
                    val newCategory = Category(
                        id = id,
                        name = "Edit Saldo",
                        iconName = "Build",
                        type = type,
                        colorHex = "#607D8B",
                        budgetLimit = 0L,
                        parentId = null
                    )
                    repository.addCategory(newCategory)
                    id
                }

                val tx = Transaction(
                    amount = amt,
                    type = type,
                    categoryId = catId,
                    accountId = srcId,
                    destAccountId = null,
                    date = java.lang.System.currentTimeMillis(),
                    notes = "Penyesuaian Asset"
                )
                repository.addTransaction(tx)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateTransaction(oldTx: Transaction, amount: Long, type: String, categoryId: String, subCategoryId: String?, accountId: String, destAccountId: String?, date: Long, notes: String, tagIds: List<String> = emptyList()) {
        viewModelScope.launch {
            try {
                val updatedTx = oldTx.copy(
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    subCategoryId = subCategoryId,
                    accountId = accountId,
                    destAccountId = destAccountId,
                    date = date,
                    notes = notes
                )
                repository.updateTransaction(oldTx, updatedTx, tagIds)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addCustomAccount(name: String, initialBalance: Long) {
        viewModelScope.launch {
            try {
                val id = name.lowercase().replace(" ", "_") + "_${System.currentTimeMillis() % 1000}"
                val newAccount = Account(
                    id = id,
                    name = name,
                    balance = initialBalance,
                    iconName = "AccountBalanceWallet",
                    colorHex = getRandomColorHex()
                )
                repository.addAccount(newAccount)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteCustomAccount(account: Account) {
        viewModelScope.launch {
            try {
                repository.deleteAccount(account)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addCustomCategory(name: String, type: String, iconName: String, budgetLimit: Long = 0L, parentId: String? = null) {
        viewModelScope.launch {
            try {
                val id = name.lowercase().replace(" ", "_") + "_${System.currentTimeMillis() % 1000}"
                val newCategory = Category(
                    id = id,
                    name = name,
                    iconName = iconName,
                    type = type,
                    colorHex = getRandomColorHex(),
                    budgetLimit = budgetLimit,
                    parentId = parentId
                )
                repository.addCategory(newCategory)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteCustomCategory(category: Category) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(category)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateCustomAccount(account: Account) {
        viewModelScope.launch {
            try {
                val existingAccounts = repository.accounts.first()
                val oldAccount = existingAccounts.find { it.id == account.id }
                if (oldAccount != null && oldAccount.balance != account.balance) {
                    val diff = account.balance - oldAccount.balance
                    val type = if (diff > 0) "PEMASUKAN" else "PENGELUARAN"
                    
                    val categories = repository.categories.first()
                    var editCat = categories.find { it.name == "Penyesuaian Saldo" && it.type == type }
                    if (editCat == null) {
                        editCat = Category(
                            id = "penyesuaian_saldo_${type.lowercase()}_${java.lang.System.currentTimeMillis() % 1000}",
                            name = "Penyesuaian Saldo",
                            iconName = "SwapHoriz",
                            type = type,
                            colorHex = "#9E9E9E",
                            budgetLimit = 0L
                        )
                        repository.addCategory(editCat)
                    }
                    
                    val tx = Transaction(
                        amount = kotlin.math.abs(diff),
                        type = type,
                        categoryId = editCat.id,
                        accountId = account.id,
                        destAccountId = null,
                        date = java.lang.System.currentTimeMillis(),
                        notes = "Penyesuaian sistem Saldo Dompet"
                    )
                    repository.addTransaction(tx)
                }
                repository.addAccount(account)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateCustomCategory(category: Category) {
        viewModelScope.launch {
            try {
                repository.addCategory(category)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addCustomSubCategory(name: String, categoryId: String) {
        viewModelScope.launch {
            try {
                val subCat = SubCategory(
                    id = UUID.randomUUID().toString(),
                    categoryId = categoryId,
                    name = name,
                    orderIndex = 0
                )
                repository.addSubCategory(subCat)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteCustomSubCategory(subCategory: SubCategory) {
        viewModelScope.launch {
            try {
                repository.deleteSubCategory(subCategory)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateCustomSubCategory(subCategory: SubCategory) {
        viewModelScope.launch {
            try {
                repository.addSubCategory(subCategory)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getStartAndEndEpochs(offset: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, offset)

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis

        return Pair(start, end)
    }

    private fun getMonthYearLabel(offset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, offset)
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        return monthFormat.format(calendar.time)
    }

    private fun getRandomColorHex(): String {
        val colors = listOf("#FF5722", "#E91E63", "#9C27B0", "#3F51B5", "#2196F3", "#00BCD4", "#4CAF50", "#8BC34A", "#FFC107", "#009688")
        return colors.random()
    }

    fun addSavingTarget(name: String, targetAmount: Long, sourceAccountId: String) {
        viewModelScope.launch {
            try {
                val target = SavingTarget(
                    name = name,
                    targetAmount = targetAmount,
                    currentAmount = 0L,
                    sourceAccountId = sourceAccountId,
                    colorHex = getRandomColorHex()
                )
                repository.addSavingTarget(target)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteSavingTarget(savingTarget: SavingTarget) {
        viewModelScope.launch {
            try {
                repository.deleteSavingTarget(savingTarget)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveToTarget(targetId: Int, sourceAccountId: String, amount: Long) {
        viewModelScope.launch {
            try {
                repository.saveToTarget(targetId, sourceAccountId, amount)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addRecurringTransaction(
        name: String,
        amount: Long,
        type: String,
        categoryId: String,
        accountId: String,
        dayOfMonth: Int,
        notes: String = ""
    ) {
        viewModelScope.launch {
            try {
                val r = RecurringTransaction(
                    name = name,
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    accountId = accountId,
                    dayOfMonth = dayOfMonth,
                    notes = notes
                )
                repository.addRecurringTransaction(r)
                repository.checkAndApplyRecurringTransactions()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction) {
        viewModelScope.launch {
            try {
                repository.deleteRecurringTransaction(recurringTransaction)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) {
        viewModelScope.launch {
            try {
                repository.updateRecurringTransaction(recurringTransaction)
                repository.checkAndApplyRecurringTransactions()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun restoreDefaultAccounts() {
        viewModelScope.launch {
            try {
                repository.restoreDefaultAccounts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                repository.clearAllData()
                _currentMonthOffset.value = 0 // Reset month view to today after clearing
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun importCsvTransactions(csvText: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val lines = csvText.lines().filter { it.trim().isNotBlank() }
                if (lines.isEmpty()) {
                    onComplete(false, "Data kosong")
                    return@launch
                }
                
                // Parse lines skipping header if present
                val startIdx = if (lines.first().contains("Tanggal", ignoreCase = true) || lines.first().contains("Aset", ignoreCase = true)) 1 else 0
                val parsedTxList = mutableListOf<Triple<Transaction, String, String?>>() // Transaction, accountName, destAccountName
                
                // Track created accounts & categories
                val existingAccts = repository.accounts.first().toMutableList()
                val existingCats = repository.categories.first().toMutableList()
                
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.US)
                val sdfShort = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.US)
                val sdfWithDash = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                
                for (i in startIdx until lines.size) {
                    val row = lines[i]
                    val parts = mutableListOf<String>()
                    var inQuotes = false
                    val currentField = java.lang.StringBuilder()
                    var cIdx = 0
                    while (cIdx < row.length) {
                        val c = row[cIdx]
                        if (c == '"') {
                            inQuotes = !inQuotes
                        } else if (c == ',' && !inQuotes) {
                            parts.add(currentField.toString().trim())
                            currentField.setLength(0)
                        } else {
                            currentField.append(c)
                        }
                        cIdx++
                    }
                    parts.add(currentField.toString().trim())
                    
                    if (parts.size < 7) continue // invalid row
                    
                    val rawDate = parts[0]
                    val accountName = parts[1]
                    val catName = parts[2]
                    val subCat = parts[3]
                    val rawNotes = parts[4]
                    val rawAmount1 = parts[5]
                    val typeStr = parts[6]
                    val rawAmount2 = parts.getOrNull(8) ?: ""
                    
                    val amount = rawAmount2.replace("\"", "").replace(",", "").toLongOrNull()
                        ?: rawAmount1.replace("\"", "").replace(",", "").toLongOrNull()
                        ?: 0L
                    
                    val cleanDate = rawDate.replace("\uFEFF", "").replace("\"", "").trim()
                    
                    var dateMs = System.currentTimeMillis()
                    val numericVal = cleanDate.toLongOrNull()
                    if (numericVal != null) {
                        dateMs = if (cleanDate.length == 10) numericVal * 1000L else numericVal
                    } else {
                        val formats = listOf(
                            "M/d/yyyy HH:mm:ss",
                            "M/d/yyyy HH:mm",
                            "M/d/yyyy",
                            "MM/dd/yyyy HH:mm:ss",
                            "MM/dd/yyyy HH:mm",
                            "MM/dd/yyyy",
                            "dd/MM/yyyy HH:mm:ss",
                            "dd/MM/yyyy HH:mm",
                            "dd/MM/yyyy",
                            "yyyy-MM-dd HH:mm:ss",
                            "yyyy-MM-dd HH:mm",
                            "yyyy-MM-dd",
                            "dd-MM-yyyy HH:mm:ss",
                            "dd-MM-yyyy HH:mm",
                            "dd-MM-yyyy",
                            "d/M/yyyy H:m:s",
                            "d/M/yyyy H:m",
                            "d/M/yyyy",
                            "d-M-yyyy H:m:s",
                            "d-M-yyyy",
                            "yyyy-MM-dd'T'HH:mm:ss'Z'",
                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                            "yyyy-MM-dd'T'HH:mm:ss",
                            "yyyy-MM-dd'T'HH:mm:ss.SSS"
                        )
                        var parsed = false
                        for (fmt in formats) {
                            try {
                                val sdfParser = java.text.SimpleDateFormat(fmt, java.util.Locale.US)
                                val d = sdfParser.parse(cleanDate)
                                if (d != null) {
                                    dateMs = d.time
                                    parsed = true
                                    break
                                }
                            } catch (e: Exception) {
                                // Try next format
                            }
                        }
                        
                        if (!parsed) {
                            for (fmt in formats) {
                                try {
                                    val sdfParser = java.text.SimpleDateFormat(fmt, java.util.Locale.getDefault())
                                    val d = sdfParser.parse(cleanDate)
                                    if (d != null) {
                                        dateMs = d.time
                                        parsed = true
                                        break
                                    }
                                } catch (e: Exception) {
                                    // Try next format
                                }
                            }
                        }
                        
                        if (!parsed) {
                            try {
                                val dateRegex = """(\d{1,4})[/\-.](\d{1,2})[/\-.](\d{1,4})""".toRegex()
                                val timeRegex = """(\d{1,2})[:.](\d{1,2})(?:[:.](\d{1,2}))?""".toRegex()
                                
                                val dateMatch = dateRegex.find(cleanDate)
                                if (dateMatch != null) {
                                    val (g1, g2, g3) = dateMatch.destructured
                                    var year = 2026
                                    var month = 6
                                    var day = 4
                                    
                                    if (g1.length == 4) {
                                        year = g1.toInt()
                                        month = g2.toInt() - 1
                                        day = g3.toInt()
                                    } else if (g3.length == 4) {
                                        // CSV is Month/Day/Year
                                        month = g1.toInt() - 1
                                        day = g2.toInt()
                                        year = g3.toInt()
                                    } else {
                                        // CSV is Month/Day/Year
                                        month = g1.toInt() - 1
                                        day = g2.toInt()
                                        year = 2000 + g3.toInt()
                                    }
                                    
                                    var hour = 12
                                    var minute = 0
                                    var second = 0
                                    
                                    val timeMatch = timeRegex.find(cleanDate)
                                    if (timeMatch != null) {
                                        hour = timeMatch.groupValues[1].toInt()
                                        minute = timeMatch.groupValues[2].toInt()
                                        second = timeMatch.groupValues.getOrNull(3)?.toIntOrNull() ?: 0
                                    }
                                    
                                    val cal = java.util.Calendar.getInstance()
                                    cal.set(year, month, day, hour, minute, second)
                                    dateMs = cal.timeInMillis
                                }
                            } catch (e: Exception) {
                                // Keep System.currentTimeMillis()
                            }
                        }
                    }
                    
                    // Slugify names for IDs
                    val acctId = accountName.lowercase().filter { it.isLetterOrDigit() }.ifEmpty { "aset" }
                    val catId = catName.lowercase().filter { it.isLetterOrDigit() }.ifEmpty { "makanan" }
                    val isTransfer = typeStr.contains("Transfer", ignoreCase = true)
                    
                    val txType = when {
                        isTransfer -> "TRANSFER"
                        typeStr.contains("Pendapatan", ignoreCase = true) -> "PEMASUKAN"
                        else -> "PENGELUARAN"
                    }
                    
                    // For transfer: catName is the destination account!
                    val finalCatId = if (isTransfer) "transfer" else catId
                    val notesText = if (subCat.isNotEmpty()) "[$subCat] $rawNotes" else rawNotes
                    
                    // Auto create accounts and categories if they are missing
                    var acct = existingAccts.find { it.id == acctId }
                    if (acct == null) {
                        val colorHex = when(acctId) {
                            "bri" -> "#0072C6"
                            "bca" -> "#2196F3"
                            "dana" -> "#03A9F4"
                            "ovo" -> "#9C27B0"
                            "pegangan", "tunai" -> "#4CAF50"
                            "blu" -> "#00BCD4"
                            else -> listOf("#4CAF50", "#2196F3", "#9C27B0", "#03A9F4", "#E91E63", "#FF9800", "#FFC107", "#00BCD4").shuffled().first()
                        }
                        acct = Account(
                            id = acctId,
                            name = accountName,
                            balance = 0L,
                            iconName = if (acctId == "bca" || acctId == "bri") "AccountBalance" else "AccountBalanceWallet",
                            colorHex = colorHex
                        )
                        repository.addAccount(acct)
                        existingAccts.add(acct)
                    }
                    
                    var destAcctId: String? = null
                    if (isTransfer) {
                        val destAcctIdRaw = catName.lowercase().filter { it.isLetterOrDigit() }.ifEmpty { "pegangan" }
                        destAcctId = destAcctIdRaw
                        var destAcct = existingAccts.find { it.id == destAcctIdRaw }
                        if (destAcct == null) {
                            destAcct = Account(
                                id = destAcctIdRaw,
                                name = catName,
                                balance = 0L,
                                iconName = "AccountBalanceWallet",
                                colorHex = listOf("#E91E63", "#FF9800", "#FFC107", "#00BCD4", "#4CAF50").shuffled().first()
                            )
                            repository.addAccount(destAcct)
                            existingAccts.add(destAcct)
                        }
                    } else {
                        // Create Category if it does not exist
                        var cat = existingCats.find { it.id == catId }
                        if (cat == null) {
                            val iconName = when {
                                catId.contains("food") || catId.contains("makanan") || catId.contains("eat") -> "Fastfood"
                                catId.contains("transport") || catId.contains("bensin") -> "DirectionsCar"
                                catId.contains("house") || catId.contains("ครัว") || catId.contains("persiapan") -> "ShoppingCart"
                                catId.contains("health") || catId.contains("sehat") -> "MedicalServices"
                                catId.contains("bonus") -> "Redeem"
                                catId.contains("allowance") -> "AttachMoney"
                                else -> "Category"
                            }
                            cat = Category(
                                id = catId,
                                name = catName,
                                iconName = iconName,
                                type = txType,
                                colorHex = listOf("#FF5722", "#E91E63", "#9C27B0", "#FFC107", "#FF9800", "#03A9F4", "#00BCD4").shuffled().first()
                            )
                            repository.addCategory(cat)
                            existingCats.add(cat)
                        }
                    }
                    
                    val transaction = Transaction(
                        amount = amount,
                        type = txType,
                        categoryId = finalCatId,
                        accountId = acctId,
                        destAccountId = destAcctId,
                        date = dateMs,
                        notes = notesText
                    )
                    parsedTxList.add(Triple(transaction, accountName, destAcctId))
                }
                
                // Sort transactions chronologically (lowest/oldest date first)
                parsedTxList.sortBy { it.first.date }
                
                for (item in parsedTxList) {
                    repository.addTransaction(item.first)
                }
                
                onComplete(true, "Berhasil mengimpor ${parsedTxList.size} transaksi.")
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false, "Format file tidak sesuai. Error: ${e.localizedMessage}")
            }
        }
    }

    fun exportCsvTransactions(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val txs = repository.transactions.first().sortedBy { it.date }
                val accts = repository.accounts.first()
                val cats = repository.categories.first()
                
                val builder = StringBuilder()
                builder.append("Tanggal,Aset,Kategori,Sub-kategori,Catatan,Jumlah,Tipe\n")
                
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.US)
                
                for (tx in txs) {
                    val dateStr = sdf.format(java.util.Date(tx.date))
                    val acctName = accts.find { it.id == tx.accountId }?.name ?: tx.accountId
                    val isTransfer = tx.type == "TRANSFER"
                    val catName = if (isTransfer) {
                        accts.find { it.id == tx.destAccountId }?.name ?: tx.destAccountId ?: ""
                    } else {
                        cats.find { it.id == tx.categoryId }?.name ?: tx.categoryId
                    }
                    
                    var notes = tx.notes
                    var subCat = ""
                    if (notes.startsWith("[") && notes.contains("]")) {
                        val endIdx = notes.indexOf("]")
                        if (endIdx > 1) {
                            subCat = notes.substring(1, endIdx)
                            notes = notes.substring(endIdx + 1).trim()
                        }
                    }
                    
                    val cleanAcctName = if (acctName.contains(",") || acctName.contains("\"")) "\"${acctName.replace("\"", "\"\"")}\"" else acctName
                    val cleanCatName = if (catName.contains(",") || catName.contains("\"")) "\"${catName.replace("\"", "\"\"")}\"" else catName
                    val cleanSubCat = if (subCat.contains(",") || subCat.contains("\"")) "\"${subCat.replace("\"", "\"\"")}\"" else subCat
                    val cleanNotes = if (notes.contains(",") || notes.contains("\"")) "\"${notes.replace("\"", "\"\"")}\"" else notes
                    
                    val typeStr = when (tx.type) {
                        "TRANSFER" -> "Transfer"
                        "PEMASUKAN" -> "Pendapatan"
                        else -> "Pengeluaran"
                    }
                    
                    val formattedAmount = tx.amount.toLong()
                    builder.append("$dateStr,$cleanAcctName,$cleanCatName,$cleanSubCat,$cleanNotes,$formattedAmount,$typeStr\n")
                }
                onComplete(builder.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete("")
            }
        }
    }

    fun addCustomTag(name: String, colorHex: String) {
        viewModelScope.launch {
            try {
                val tagsList = repository.tags.first()
                val orderIndex = tagsList.maxOfOrNull { it.orderIndex }?.plus(1) ?: 0
                val tag = Tag(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    colorHex = colorHex,
                    orderIndex = orderIndex
                )
                repository.addTag(tag)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateCustomTag(tag: Tag, newName: String, newColorHex: String) {
        viewModelScope.launch {
            try {
                repository.updateTag(tag.copy(name = newName, colorHex = newColorHex))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteCustomTag(tag: Tag) {
        viewModelScope.launch {
            try {
                repository.deleteTag(tag)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addInstallment(installment: Installment) {
        viewModelScope.launch {
            repository.addInstallment(installment)
        }
    }
    
    fun updateInstallment(installment: Installment) {
        viewModelScope.launch {
            repository.updateInstallment(installment)
        }
    }
    
    fun deleteInstallment(installment: Installment) {
        viewModelScope.launch {
            repository.deleteInstallment(installment)
        }
    }
    
    fun payInstallment(installmentId: Int, accountId: String, amount: Long) {
        viewModelScope.launch {
            repository.payInstallment(installmentId, accountId, amount)
        }
    }
}



data class SubCategoryShare(
    val subCategory: SubCategory?, // null means "No Sub Category"
    val amount: Long
)

data class CategoryShare(
    val category: Category,
    val amount: Long,
    val percentage: Double,
    val subCategoryShares: List<SubCategoryShare> = emptyList()
)

class ViewModelFactory(
    private val repository: FinanceRepository,
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository, preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }



}
