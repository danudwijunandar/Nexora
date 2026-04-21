package com.example.nexora1.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nexora1.data.local.prefs.SessionManager
import com.example.nexora1.data.local.room.NexoraDatabase
import com.example.nexora1.data.remote.retrofit.ApiConfig
import com.example.nexora1.data.remote.response.CreateFinanceResponse

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val database = NexoraDatabase.getInstance(applicationContext)
        val sessionManager = SessionManager(applicationContext)
        val token = sessionManager.getToken() ?: return Result.failure()
        val apiService = ApiConfig.getApiService()

        try {
            // Sync Finance yang belum tersinkron
            val unsyncedFinance = database.financeDao().getUnsyncedFinance()
            unsyncedFinance.forEach { finance ->
                val response = apiService.createFinance(
                    "Bearer $token",
                    finance.type,
                    finance.category,
                    finance.amount,
                    finance.date,
                    finance.note
                )
                
                if (response.isSuccessful) {
                    val body = response.body()
                    val remoteId = body?.data?.id
                    if (remoteId != null) {
                        database.financeDao().updateSyncStatus(finance.localId, remoteId)
                    }
                }
            }
            
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}