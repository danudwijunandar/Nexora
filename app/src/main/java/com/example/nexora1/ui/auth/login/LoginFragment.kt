package com.example.nexora1.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.nexora1.MainActivity
import com.example.nexora1.R
import com.example.nexora1.data.Result
import com.example.nexora1.data.local.prefs.SessionManager
import com.example.nexora1.databinding.FragmentLoginBinding
import com.example.nexora1.ui.ViewModelFactory
import com.example.nexora1.ui.auth.AuthViewModel

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        binding.tvRegisterLink.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.tilEmail.editText?.text.toString().trim()
            val password = binding.tilPassword.editText?.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.btnLogin.isEnabled = false
                    binding.btnLogin.text = ""
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.text = getString(R.string.btn_login)
                    val loginResponse = result.data
                    sessionManager.saveSession(
                        loginResponse.token ?: "",
                        loginResponse.user?.username ?: "User",
                        binding.tilEmail.editText?.text.toString().trim()
                    )
                    Toast.makeText(requireContext(), "Login Berhasil!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                    requireActivity().finish()
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.text = getString(R.string.btn_login)
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(requireContext(), result.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
