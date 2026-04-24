package com.example.nexora1.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nexora1.data.local.room.ActivityEntity
import com.example.nexora1.data.local.room.NexoraDatabase
import java.text.SimpleDateFormat
import java.util.*

class DailyActivityWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val database = NexoraDatabase.getDatabase(applicationContext)
        
        val now = Calendar.getInstance()
        val todayDayOfWeek = now.get(Calendar.DAY_OF_WEEK)

        return try {
            Log.d("DailyActivityWorker", "Running at 5 AM. Checking for recurring tasks for day: $todayDayOfWeek")
            Result.success()
        } catch (e: Exception) {
            Log.e("DailyActivityWorker", "Error: ${e.message}")
            Result.failure()
        }
    }
}
