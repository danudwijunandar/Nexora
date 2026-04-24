package com.example.nexora1.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexora1.data.Result
import com.example.nexora1.data.remote.response.LoginResponse
import com.example.nexora1.data.remote.response.RegisterResponse
import com.example.nexora1.data.repository.NexoraRepository
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthViewModel(private val repository: NexoraRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult

    private val _registerResult = MutableLiveData<Result<RegisterResponse>>()
    val registerResult: LiveData<Result<RegisterResponse>> = _registerResult

    fun login(email: String, pass: String) {
        _loginResult.value = Result.Loading
        viewModelScope.launch {
            try {
                val result = repository.login(email, pass)
                _loginResult.value = result
            } catch (e: Exception) {
                _loginResult.value = Result.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun register(username: String, email: String, pass: String, confirm: String) {
        _registerResult.value = Result.Loading
        viewModelScope.launch {
            try {
                val result = repository.register(username, email, pass, confirm)
                _registerResult.value = result
            } catch (e: Exception) {
                _registerResult.value = Result.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }
}
