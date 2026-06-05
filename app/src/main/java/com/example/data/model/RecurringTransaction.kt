package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_transactions")
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val type: String, // "PEMASUKAN", "PENGELUARAN"
    val categoryId: String,
    val accountId: String,
    val dayOfMonth: Int, // 1 s/d 31
    val lastAppliedTimestamp: Long = 0L, // Waktu terakhir kali transaksi otomatis dimasukkan
    val notes: String = "",
    val isPaused: Boolean = false
)
