package com.example.nexora1.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.nexora1.data.Result
import com.example.nexora1.data.remote.response.ActivityData
import com.example.nexora1.data.repository.NexoraRepository
import kotlinx.coroutines.launch

class ActivityViewModel(private val repository: NexoraRepository) : ViewModel() {

    private val _addActivityResult = MutableLiveData<Result<Boolean>>()
    val addActivityResult: LiveData<Result<Boolean>> = _addActivityResult

    private val _updateStatusResult = MutableLiveData<Result<String>>()
    val updateStatusResult: LiveData<Result<String>> = _updateStatusResult

    fun getActivities(): LiveData<List<ActivityData>> {
        return repository.getLocalActivities().asLiveData()
    }

    fun syncActivities(token: String) {
        viewModelScope.launch {
            repository.syncActivities(token)
        }
    }

    fun addActivity(token: String, title: String, description: String, category: String, date: String? = null) {
        _addActivityResult.value = Result.Loading
        viewModelScope.launch {
            try {
                val success = repository.addActivity(token, title, description, category, date)
                if (success) {
                    _addActivityResult.value = Result.Success(true)
                } else {
                    _addActivityResult.value = Result.Error("Gagal menambahkan aktivitas")
                }
            } catch (e: Exception) {
                _addActivityResult.value = Result.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun updateActivityStatus(token: String, id: Int, title: String, status: String) {
        _updateStatusResult.value = Result.Loading
        viewModelScope.launch {
            try {
                val success = repository.updateActivityStatus(token, id, title, status)
                if (success) {
                    _updateStatusResult.value = Result.Success(title)
                } else {
                    _updateStatusResult.value = Result.Error("Gagal memperbarui status")
                }
            } catch (e: Exception) {
                _updateStatusResult.value = Result.Error(e.message ?: "Gagal memperbarui status")
            }
        }
    }
}
