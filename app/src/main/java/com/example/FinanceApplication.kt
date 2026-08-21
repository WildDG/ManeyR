package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.repository.FinanceRepository
import com.example.data.repository.PreferencesManager

class FinanceApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val preferencesManager by lazy { PreferencesManager(this) }
    val repository by lazy {
        FinanceRepository(
            database.accountDao(),
            database.categoryDao(),
            database.subCategoryDao(),
            database.transactionDao(),
            database.savingTargetDao(),
            database.recurringTransactionDao(),
            database.tagDao(),
            database.transactionTagDao(),
            database.installmentDao()
        )
    }
}
