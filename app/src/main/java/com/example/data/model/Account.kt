package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey val id: String,
    val name: String,
    val balance: Double, // current balance
    val iconName: String, // name of material icon
    val colorHex: String = "#6200EE"
)
