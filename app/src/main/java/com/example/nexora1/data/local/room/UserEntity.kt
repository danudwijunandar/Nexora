package com.example.nexora1.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val email: String,
    val username: String,
    val profileImagePath: String? = null
)