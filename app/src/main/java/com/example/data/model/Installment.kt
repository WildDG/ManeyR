package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installments")
data class Installment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val totalAmount: Long,
    val installmentAmount: Long,
    val totalInstallments: Int,
    val paidCount: Int = 0,
    val remainingCount: Int,
    val remainingAmount: Long,
    val paymentAccountId: String,
    val firstDueDate: Long,
    val nextDueDate: Long,
    val categoryId: String,
    val subCategoryId: String? = null,
    val interest: Long = 0L,
    val status: String = "ACTIVE" // ACTIVE, COMPLETED
)
