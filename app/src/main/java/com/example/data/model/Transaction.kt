package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Long,
    val type: String, // "PEMASUKAN", "PENGELUARAN", "TRANSFER", "ADJUSTMENT"
    val categoryId: String,
    val subCategoryId: String? = null,
    val accountId: String, // source account
    val destAccountId: String? = null, // destination account for TRANSFER
    val date: Long, // timestamp
    val notes: String = "",
    val time: Long = 0L, // time of day in millis since midnight, or just timestamp
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val transferGroupId: String? = null
)
