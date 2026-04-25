package com.example.nexora1.data.repository

import com.example.nexora1.data.Result
import com.example.nexora1.data.local.room.*
import com.example.nexora1.data.remote.response.*
import com.example.nexora1.data.remote.retrofit.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class NexoraRepository(
    private val apiService: ApiService,
    private val activityDao: ActivityDao,
    private val financeDao: FinanceDao,
    private val userDao: UserDao
) {
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    suspend fun login(email: String, pass: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(email, pass)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = parseErrorMessage(errorBody, "Login Gagal")
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Terjadi kesalahan koneksi")
        }
    }

    suspend fun register(username: String, email: String, pass: String, confirm: String): Result<RegisterResponse> {
        return try {
            val response = apiService.register(username, email, pass, confirm)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = parseErrorMessage(errorBody, "Registrasi Gagal")
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Terjadi kesalahan koneksi")
        }
    }

    private fun parseErrorMessage(errorBody: String?, defaultMsg: String): String {
        return try {
            val errorJson = JSONObject(errorBody ?: "{}")
            // Coba ambil dari array errors[0].message jika ada
            val errorsArray = errorJson.optJSONArray("errors")
            if (errorsArray != null && errorsArray.length() > 0) {
                errorsArray.getJSONObject(0).optString("message", defaultMsg)
            } else {
                // Fallback ke field message utama
                errorJson.optString("message", defaultMsg)
            }
        } catch (e: Exception) {
            defaultMsg
        }
    }

    fun getUserProfile(email: String): Flow<UserEntity?> = userDao.getUserProfile(email)

    suspend fun updateUser(token: String, username: String, email: String): Boolean {
        return try {
            val response = apiService.updateUser("Bearer $token", username, email)
            if (response.isSuccessful) {
                val currentLocal = userDao.getUserProfileOnce(email)
                userDao.insertOrUpdate(UserEntity(
                    email = email,
                    username = username,
                    profileImagePath = currentLocal?.profileImagePath
                ))
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updatePassword(token: String, oldPass: String, newPass: String, confirm: String): Boolean {
        return try {
            val response = apiService.updatePassword("Bearer $token", oldPass, newPass, confirm)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveProfileImage(email: String, username: String, path: String) {
        userDao.insertOrUpdate(UserEntity(email = email, username = username, profileImagePath = path))
    }

    fun getLocalActivities(): Flow<List<ActivityData>> {
        return activityDao.getAllActivities().map { entities ->
            entities.map { entity ->
                ActivityData(
                    id = entity.id ?: 0,
                    userId = entity.userId,
                    title = entity.title,
                    description = entity.description,
                    status = entity.status,
                    categories = entity.categories,
                    moodRating = entity.moodRating,
                    date = entity.userSelectedDate,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            }
        }
    }

    suspend fun addActivityWithReturn(token: String, title: String, description: String, category: String, date: String? = null): ActivityData? {
        val inputDate = date ?: sdf.format(Date())
        return try {
            val response = apiService.createActivity("Bearer $token", title, description, category, inputDate)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data
                val entity = ActivityEntity(
                    id = data.id,
                    userId = data.userId,
                    title = data.title,
                    description = data.description,
                    status = data.status,
                    categories = data.categories,
                    moodRating = data.moodRating,
                    userSelectedDate = inputDate,
                    createdAt = data.createdAt,
                    updatedAt = data.updatedAt,
                    isSynced = true
                )
                activityDao.insertActivity(entity)
                data.copy(date = inputDate)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateActivityStatus(token: String, id: Int, title: String, status: String): Boolean {
        return try {
            val response = apiService.updateActivity("Bearer $token", id, title, status)
            if (response.isSuccessful) {
                syncActivities(token)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteActivity(token: String, id: Int): Boolean {
        return try {
            val response = apiService.deleteActivity("Bearer $token", id)
            if (response.isSuccessful) {
                syncActivities(token)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun syncActivities(token: String) {
        try {
            val response = apiService.getActivities("Bearer $token")
            if (response.isSuccessful) {
                val activities = response.body()?.data ?: emptyList()
                val currentLocal = activityDao.getAllActivitiesOnce() 
                val entities = activities.map { data ->
                    val localMatch = currentLocal.find { it.id == data.id }
                    ActivityEntity(
                        id = data.id,
                        userId = data.userId,
                        title = data.title,
                        description = data.description,
                        status = data.status,
                        categories = data.categories,
                        moodRating = data.moodRating,
                        userSelectedDate = localMatch?.userSelectedDate,
                        createdAt = data.createdAt,
                        updatedAt = data.updatedAt,
                        isSynced = true
                    )
                }
                activityDao.deleteAll()
                activityDao.insertActivities(entities)
            }
        } catch (e: Exception) {}
    }

    fun getLocalFinance(): Flow<List<FinanceEntity>> = financeDao.getAllFinance()

    suspend fun addFinance(token: String, type: String, category: String, amount: String, date: String, note: String): Boolean {
        val now = sdf.format(Date())
        val entity = FinanceEntity(
            userId = 0,
            type = type,
            category = category,
            amount = amount,
            date = date,
            note = note,
            createdAt = now,
            updatedAt = now,
            isSynced = false
        )
        financeDao.insertFinance(entity)

        return try {
            val response = apiService.createFinance("Bearer $token", type, category, amount, date, note)
            if (response.isSuccessful) {
                syncFinance(token)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateFinance(token: String, id: Int, type: String, category: String, amount: String, date: String): Boolean {
        return try {
            val response = apiService.updateFinance("Bearer $token", id, type, category, amount, date)
            if (response.isSuccessful) {
                syncFinance(token)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteFinance(token: String, id: Int): Boolean {
        return try {
            val response = apiService.deleteFinance("Bearer $token", id)
            if (response.isSuccessful) {
                syncFinance(token)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun syncFinance(token: String) {
        try {
            val response = apiService.getFinance("Bearer $token")
            if (response.isSuccessful) {
                val dataList = response.body()?.data ?: emptyList()
                val entities = dataList.map { data ->
                    FinanceEntity(
                        id = data.id,
                        userId = data.userId,
                        type = data.type,
                        category = data.category,
                        amount = data.amount,
                        date = data.date,
                        note = data.note,
                        createdAt = data.createdAt,
                        updatedAt = data.updatedAt,
                        isSynced = true
                    )
                }
                financeDao.deleteAll()
                financeDao.insertAll(entities)
            }
        } catch (e: Exception) {}
    }
}
