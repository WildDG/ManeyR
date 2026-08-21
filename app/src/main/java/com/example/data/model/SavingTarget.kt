package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saving_targets")
data class SavingTarget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Long,
    val currentAmount: Long = 0L,
    val sourceAccountId: String, // Linked wallet/account (could be null for manual?)
    val colorHex: String = "#FF9800",
    val trackingMode: String = "MANUAL", // "DEDICATED", "AUTOMATIC", "MANUAL"
    val priority: Int = 0,
    val startDate: Long = System.currentTimeMillis(),
    val targetDate: Long? = null,
    val iconName: String = "Savings",
    val note: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
