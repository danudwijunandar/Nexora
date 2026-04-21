package com.example.nexora1.ui.finance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.nexora1.data.Result
import com.example.nexora1.data.local.room.FinanceEntity
import com.example.nexora1.data.repository.NexoraRepository
import kotlinx.coroutines.launch

class FinanceViewModel(private val repository: NexoraRepository) : ViewModel() {

    private val _financeResult = MutableLiveData<Result<Boolean>>()
    val financeResult: LiveData<Result<Boolean>> = _financeResult

    fun getFinance(): LiveData<List<FinanceEntity>> {
        return repository.getLocalFinance().asLiveData()
    }

    fun syncFinance(token: String) {
        viewModelScope.launch {
            repository.syncFinance(token)
        }
    }

    fun addFinance(token: String, type: String, category: String, amount: String, date: String, note: String) {
        _financeResult.value = Result.Loading
        viewModelScope.launch {
            try {
                val success = repository.addFinance(token, type, category, amount, date, note)
                if (success) {
                    _financeResult.value = Result.Success(true)
                } else {
                    _financeResult.value = Result.Error("Gagal menyimpan data keuangan")
                }
            } catch (e: Exception) {
                _financeResult.value = Result.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun updateFinance(token: String, id: Int, type: String, category: String, amount: String, date: String) {
        _financeResult.value = Result.Loading
        viewModelScope.launch {
            try {
                val success = repository.updateFinance(token, id, type, category, amount, date)
                if (success) {
                    _financeResult.value = Result.Success(true)
                } else {
                    _financeResult.value = Result.Error("Gagal memperbarui data keuangan")
                }
            } catch (e: Exception) {
                _financeResult.value = Result.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun deleteFinance(token: String, id: Int) {
        _financeResult.value = Result.Loading
        viewModelScope.launch {
            try {
                val success = repository.deleteFinance(token, id)
                if (success) {
                    _financeResult.value = Result.Success(true)
                } else {
                    _financeResult.value = Result.Error("Gagal menghapus data")
                }
            } catch (e: Exception) {
                _financeResult.value = Result.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }
}
