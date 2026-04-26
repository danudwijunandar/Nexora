package com.example.nexora1.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.nexora1.data.Result
import com.example.nexora1.data.remote.response.ActivityData
import com.example.nexora1.data.repository.NexoraRepository
import com.example.nexora1.utils.Event
import kotlinx.coroutines.launch

class ActivityViewModel(private val repository: NexoraRepository) : ViewModel() {

    private val _addActivityResult = MutableLiveData<Event<Result<ActivityData>>>()
    val addActivityResult: LiveData<Event<Result<ActivityData>>> = _addActivityResult

    private val _updateStatusResult = MutableLiveData<Event<Result<String>>>()
    val updateStatusResult: LiveData<Event<Result<String>>> = _updateStatusResult

    private val _deleteActivityResult = MutableLiveData<Event<Result<Boolean>>>()
    val deleteActivityResult: LiveData<Event<Result<Boolean>>> = _deleteActivityResult

    private val _syncResult = MutableLiveData<Result<Unit>>()
    val syncResult: LiveData<Result<Unit>> = _syncResult

    fun getActivities(): LiveData<List<ActivityData>> {
        return repository.getLocalActivities().asLiveData()
    }

    fun syncActivities(token: String) {
        _syncResult.value = Result.Loading
        viewModelScope.launch {
            try {
                repository.syncActivities(token)
                _syncResult.value = Result.Success(Unit)
            } catch (e: Exception) {
                _syncResult.value = Result.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun addActivity(token: String, title: String, description: String, category: String, date: String? = null) {
        _addActivityResult.value = Event(Result.Loading)
        viewModelScope.launch {
            try {
                val activity = repository.addActivityWithReturn(token, title, description, category, date)
                if (activity != null) {
                    _addActivityResult.value = Event(Result.Success(activity))
                } else {
                    _addActivityResult.value = Event(Result.Error("Gagal menambahkan aktivitas"))
                }
            } catch (e: Exception) {
                _addActivityResult.value = Event(Result.Error(e.message ?: "Terjadi kesalahan"))
            }
        }
    }

    fun updateActivityStatus(token: String, id: Int, title: String, status: String) {
        _updateStatusResult.value = Event(Result.Loading)
        viewModelScope.launch {
            try {
                val success = repository.updateActivityStatus(token, id, title, status)
                if (success) {
                    _updateStatusResult.value = Event(Result.Success(title))
                } else {
                    _updateStatusResult.value = Event(Result.Error("Gagal memperbarui status"))
                }
            } catch (e: Exception) {
                _updateStatusResult.value = Event(Result.Error(e.message ?: "Gagal memperbarui status"))
            }
        }
    }

    fun deleteActivity(token: String, id: Int) {
        _deleteActivityResult.value = Event(Result.Loading)
        viewModelScope.launch {
            try {
                val success = repository.deleteActivity(token, id)
                if (success) {
                    _deleteActivityResult.value = Event(Result.Success(true))
                } else {
                    _deleteActivityResult.value = Event(Result.Error("Gagal menghapus aktivitas"))
                }
            } catch (e: Exception) {
                _deleteActivityResult.value = Event(Result.Error(e.message ?: "Terjadi kesalahan"))
            }
        }
    }
}
