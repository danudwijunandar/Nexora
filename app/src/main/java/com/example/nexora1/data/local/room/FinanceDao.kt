package com.example.nexora1.data.local.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    @Query("SELECT * FROM finance ORDER BY date DESC")
    fun getAllFinance(): Flow<List<FinanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinance(finance: FinanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(financeList: List<FinanceEntity>)

    @Query("SELECT * FROM finance WHERE isSynced = 0")
    suspend fun getUnsyncedFinance(): List<FinanceEntity>

    @Query("UPDATE finance SET isSynced = 1, id = :remoteId WHERE localId = :localId")
    suspend fun updateSyncStatus(localId: Int, remoteId: Int)

    @Query("DELETE FROM finance")
    suspend fun deleteAll()
}