package com.example.nexora1.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val id: Int? = null, // Remote ID
    val userId: Int,
    val title: String,
    val description: String,
    val status: String,
    val categories: String,
    val moodRating: String? = null,
    val reminderTime: String? = null, // HH:mm
    val reminderDate: String? = null, // yyyy-MM-dd
    val isSynced: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)