package com.example.nexora1.data.local.room

import android.content.Context
import androidx.room.*

@Database(entities = [ActivityEntity::class, UserEntity::class, FinanceEntity::class, NotificationEntity::class], version = 8, exportSchema = false)
abstract class NexoraDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun userDao(): UserDao
    abstract fun financeDao(): FinanceDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: NexoraDatabase? = null

        fun getInstance(context: Context): NexoraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NexoraDatabase::class.java,
                    "nexora_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
        fun getDatabase(context: Context): NexoraDatabase = getInstance(context)
    }
}
