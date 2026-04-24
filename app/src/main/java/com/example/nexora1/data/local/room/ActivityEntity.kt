package com.example.nexora1.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val id: Int? = null,
    val userId: Int,
    val title: String,
    val description: String,
    val status: String,
    val categories: String,
    val moodRating: String? = null,
    val reminderTime: String? = null,
    val reminderDate: String? = null,
    val userSelectedDate: String? = null, // Tambahkan ini untuk menyimpan waktu inputan user secara lokal
    val isSynced: Boolean = true,
    val createdAt: String,
    val updatedAt: String,
    val repeatDays: String? = null
)