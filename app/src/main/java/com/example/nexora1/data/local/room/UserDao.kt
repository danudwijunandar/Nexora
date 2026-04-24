package com.example.nexora1.data.local.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE email = :email")
    fun getUserProfile(email: String): Flow<UserEntity?>

    @Query("SELECT * FROM user_profile WHERE email = :email")
    suspend fun getUserProfileOnce(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserEntity)
}