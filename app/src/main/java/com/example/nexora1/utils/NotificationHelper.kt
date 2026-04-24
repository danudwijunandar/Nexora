package com.example.nexora1.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.nexora1.MainActivity
import com.example.nexora1.R
import com.example.nexora1.data.local.room.NexoraDatabase
import com.example.nexora1.data.local.room.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object NotificationHelper {
    private const val CHANNEL_ID = "reminder_channel"
    private const val CHANNEL_NAME = "Activity Reminder"

    fun showNotification(context: Context, title: String, message: String, notificationId: Int? = null) {
        val database = NexoraDatabase.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            database.notificationDao().insertNotification(
                NotificationEntity(title = title, message = message)
            )
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val id = notificationId ?: System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()

        notificationManager.notify(id, notification)
    }

    fun scheduleActivityReminder(context: Context, activityId: Int, title: String, delayMillis: Long) {
        if (delayMillis <= 0) return

        val data = Data.Builder()
            .putString("title", "Pengingat Aktivitas")
            .putString("message", "Waktunya mengerjakan: $title")
            .putInt("id", activityId)
            .build()

        val reminderRequest = OneTimeWorkRequestBuilder<AppWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("activity_$activityId")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "activity_$activityId",
            ExistingWorkPolicy.REPLACE,
            reminderRequest
        )
    }
}

class AppWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Aktivitas Nexora"
        val message = inputData.getString("message") ?: "Waktunya mengerjakan aktivitasmu!"
        val id = inputData.getInt("id", System.currentTimeMillis().toInt())

        NotificationHelper.showNotification(applicationContext, title, message, id)
        return Result.success()
    }
}
