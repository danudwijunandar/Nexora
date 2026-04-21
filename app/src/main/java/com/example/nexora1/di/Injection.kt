package com.example.nexora1.di

import android.content.Context
import com.example.nexora1.data.local.room.NexoraDatabase
import com.example.nexora1.data.remote.retrofit.ApiConfig
import com.example.nexora1.data.repository.NexoraRepository

object Injection {
    fun provideRepository(context: Context): NexoraRepository {
        val database = NexoraDatabase.getInstance(context)
        val apiService = ApiConfig.getApiService()
        return NexoraRepository(apiService, database.activityDao(), database.financeDao(), database.userDao())
    }
}
