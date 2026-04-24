package com.example.nexora1.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Pengingat Aktivitas"
        val message = inputData.getString("message") ?: "Waktunya melakukan kegiatanmu!"
        val notificationId = inputData.getInt("id", System.currentTimeMillis().toInt())
        
        NotificationHelper.showNotification(applicationContext, title, message, notificationId)
        
        return Result.success()
    }
}