package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_transactions")
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "PEMASUKAN", "PENGELUARAN"
    val amount: Long,
    val accountId: String,
    val categoryId: String,
    val subCategoryId: String? = null,
    val frequency: String = "MONTHLY", // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    val dayOfMonth: Int = 1, // keeping for backward compat or use nextDate
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val nextDate: Long = System.currentTimeMillis(),
    val lastAppliedTimestamp: Long = 0L, 
    val notes: String = "",
    val autoCreate: Boolean = true, // true = auto, false = require confirmation
    val isActive: Boolean = true,
    val isPaused: Boolean = false // backward compat
)
