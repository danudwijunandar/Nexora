package com.example.nexora1.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.nexora1.data.local.prefs.SessionManager
import com.example.nexora1.data.remote.retrofit.ApiConfig
import com.example.nexora1.databinding.FragmentUpdateUserBinding
import com.example.nexora1.ui.auth.AuthActivity
import kotlinx.coroutines.launch
import org.json.JSONObject

class UpdateUserFragment : Fragment() {
    private var _binding: FragmentUpdateUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUpdateUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        binding.tilUsername.editText?.setText(sessionManager.getUsername())
        binding.tilEmail.editText?.setText(sessionManager.getEmail())

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnSave.setOnClickListener { updateUser() }
    }

    private fun updateUser() {
        val username = binding.tilUsername.editText?.text.toString().trim()
        val email = binding.tilEmail.editText?.text.toString().trim()
        val token = sessionManager.getToken() ?: ""

        if (username.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Harap isi semua field", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = ApiConfig.getApiService().updateUser("Bearer $token", username, email)
                if (response.isSuccessful) {
                    sessionManager.saveSession(token, username, email)
                    Toast.makeText(requireContext(), "Profil diperbarui", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else if (response.code() == 401) {
                    handleUnauthorized()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = JSONObject(errorBody ?: "{}").optString("message", "Gagal memperbarui profil")
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