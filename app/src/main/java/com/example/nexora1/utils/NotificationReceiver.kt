package com.example.nexora1.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.nexora1.MainActivity
import com.example.nexora1.R
import com.example.nexora1.data.local.prefs.SessionManager
import com.example.nexora1.data.local.room.NexoraDatabase
import com.example.nexora1.data.local.room.NotificationEntity
import com.example.nexora1.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.getToken() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.getApiService().getActivities("Bearer $token")
                if (response.isSuccessful) {
                    val activities = response.body()?.data ?: emptyList()
                    val unfinishedActivity = activities.find { it.status != "selesai" && it.status != "3" }
                    
                    if (unfinishedActivity != null) {
                        val activityName = unfinishedActivity.title
                        val message = "Kamu belum menyelesaikan aktivitas $activityName. Yuk selesaikan!"

                        val database = NexoraDatabase.getInstance(context)
                        database.notificationDao().insertNotification(
                            NotificationEntity(
                                title = "Pengingat Aktivitas",
                                message = message
                            )
                        )
                        
                        showNotification(context, message)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showNotification(context: Context, message: String) {
        val channelId = "activity_reminder_channel"
        val notificationId = System.currentTimeMillis().toInt()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Activity Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle("Pengingat Aktivitas")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()

        notificationManager.notify(notificationId, notification)
    }
}