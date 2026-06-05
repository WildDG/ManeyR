package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: String, // "PEMASUKAN", "PENGELUARAN", "TRANSFER"
    val categoryId: String,
    val accountId: String, // source account
    val destAccountId: String? = null, // destination account for TRANSFER
    val date: Long, // timestamp
    val notes: String = ""
)
