package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saving_targets")
data class SavingTarget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val sourceAccountId: String, // Linked wallet/account
    val colorHex: String = "#FF9800"
)
