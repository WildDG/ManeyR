package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String,
    val iconName: String? = null,
    val orderIndex: Int = 0,
    val isArchived: Boolean = false
)
