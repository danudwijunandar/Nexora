package com.example.nexora1.data.local.room

import androidx.room.*

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val email: String,
    val username: String,
    val profileImagePath: String? = null
)