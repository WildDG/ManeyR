package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey val id: String,
    val name: String,
    val balance: Long, // current balance
    val iconName: String, // name of material icon
    val colorHex: String = "#6200EE",
    val type: String = "Other", // "Cash", "Bank", "E-Wallet", "Saving", "Other"
    val openingBalance: Long = 0L,
    val displayOrder: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
