package com.example.nexora1.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.nexora1.data.local.prefs.SessionManager
import com.example.nexora1.data.remote.retrofit.ApiConfig
import com.example.nexora1.databinding.FragmentUpdatePasswordBinding
import com.example.nexora1.ui.auth.AuthActivity
import kotlinx.coroutines.launch
import org.json.JSONObject

class UpdatePasswordFragment : Fragment() {
    private var _binding: FragmentUpdatePasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUpdatePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnSave.setOnClickListener { updatePassword() }
    }

    private fun updatePassword() {
        val oldPass = binding.tilOldPassword.editText?.text.toString().trim()
        val newPass = binding.tilNewPassword.editText?.text.toString().trim()
        val confirm = binding.tilConfirmPassword.editText?.text.toString().trim()
        val token = sessionManager.getToken() ?: ""

        binding.tilNewPassword.error = null

        if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(requireContext(), "Harap isi semua field", Toast.LENGTH_SHORT).show()
            return
        }

        if (!newPass.any { it.isDigit() }) {
            binding.tilNewPassword.error = "Password harus mengandung minimal satu angka"
            return
        }

        if (newPass != confirm) {
            Toast.makeText(requireContext(), "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = ApiConfig.getApiService().updatePassword("Bearer $token", oldPass, newPass, confirm)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Password berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else if (response.code() == 401) {
                    handleUnauthorized()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = JSONObject(errorBody ?: "{}").optString("message", "Gagal memperbarui password")
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleUnauthorized() {
        sessionManager.logout()
        startActivity(Intent(requireContext(), AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}