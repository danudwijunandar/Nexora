package com.example.nexora1.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.nexora1.data.Result
import com.example.nexora1.data.local.room.UserEntity
import com.example.nexora1.data.repository.NexoraRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: NexoraRepository) : ViewModel() {

    private val _updateResult = MutableLiveData<Result<Boolean>>()
    val updateResult: LiveData<Result<Boolean>> = _updateResult

    fun getUserProfile(email: String): LiveData<UserEntity?> {
        return repository.getUserProfile(email).asLiveData()
    }

    fun updateUser(token: String, username: String, email: String) {
        _updateResult.value = Result.Loading
        viewModelScope.launch {
            try {
                val success = repository.updateUser(token, username, email)
                if (success) {
                    _updateResult.value = Result.Success(true)
                } else {
                    _updateResult.value = Result.Error("Gagal memperbarui profil")
                }
            } catch (e: Exception) {
                _updateResult.value = Result.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun updatePassword(token: String, oldPass: String, newPass: String, confirm: String) {
        _updateResult.value = Result.Loading
        viewModelScope.launch {
            try {
                val success = repository.updatePassword(token, oldPass, newPass, confirm)
                if (success) {
                    _updateResult.value = Result.Success(true)
                } else {
                    _updateResult.value = Result.Error("Gagal memperbarui password")
                }
            } catch (e: Exception) {
                _updateResult.value = Result.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun saveProfileImage(email: String, username: String, path: String) {
        viewModelScope.launch {
            repository.saveProfileImage(email, username, path)
        }
    }
}
