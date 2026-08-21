package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saving_allocations")
data class SavingAllocation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,
    val accountId: String,
    val amount: Long,
    val sourceTransactionId: Int? = null,
    val transferGroupId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
