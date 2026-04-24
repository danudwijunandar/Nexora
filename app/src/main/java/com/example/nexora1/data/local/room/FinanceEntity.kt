package com.example.nexora1.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "finance")
data class FinanceEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val id: Int? = null,
    val userId: Int,
    val type: String,
    val category: String,
    val amount: String,
    val date: String,
    val note: String,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Boolean = true
)