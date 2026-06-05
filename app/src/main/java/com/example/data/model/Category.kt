package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String, // e.g. "Fastfood"
    val type: String, // "PEMASUKAN" or "PENGELUARAN" or "TRANSFER"
    val colorHex: String,
    val budgetLimit: Double = 0.0
)
