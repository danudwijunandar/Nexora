package com.example.nexora1.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.nexora1.R
import com.example.nexora1.data.Result
import com.example.nexora1.data.local.prefs.SessionManager
import com.example.nexora1.data.local.room.UserEntity
import com.example.nexora1.databinding.DialogUpdatePasswordBinding
import com.example.nexora1.databinding.DialogUpdateUserBinding
import com.example.nexora1.databinding.FragmentProfileBinding
import com.example.nexora1.ui.ViewModelFactory
import com.example.nexora1.ui.auth.AuthActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private var currentPhotoPath: String? = null
    private var currentUser: UserEntity? = null

    private val viewModel: ProfileViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) openCamera()
        else Toast.makeText(requireContext(), "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.saveProfileImage(sessionManager.getEmail() ?: "", sessionManager.getUsername() ?: "", it.toString()) }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentPhotoPath?.let { viewModel.saveProfileImage(sessionManager.getEmail() ?: "", sessionManager.getUsername() ?: "", it) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        observeViewModel()

        binding.ivAvatar.setOnClickListener {
            showEnlargedImage()
        }

        binding.layoutEnlargeImage.setOnClickListener {
            binding.layoutEnlargeImage.visibility = View.GONE
        }

        binding.btnChangeAvatar.setOnClickListener {
            showImagePickerOptions()
            binding.layoutEnlargeImage.visibility = View.GONE
        }

        binding.btnUpdateUser.setOnClickListener { showUpdateUserDialog() }
        binding.btnUpdatePassword.setOnClickListener { showUpdatePasswordDialog() }
        binding.btnLogout.setOnClickListener { showLogoutConfirmation() }
    }

    private fun showEnlargedImage() {
        binding.layoutEnlargeImage.visibility = View.VISIBLE
        currentUser?.profileImagePath?.let {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.ic_user_placeholder)
                .into(binding.ivEnlarged)
        } ?: run {
            binding.ivEnlarged.setImageResource(R.drawable.ic_user_placeholder)
        }
    }

    private fun observeViewModel() {
        val email = sessionManager.getEmail() ?: ""
        viewModel.getUserProfile(email).observe(viewLifecycleOwner) { user ->
            currentUser = user
            binding.tvUsername.text = user?.username ?: sessionManager.getUsername()
            binding.tvemail.text = user?.email ?: sessionManager.getEmail()
            
            if (!user?.profileImagePath.isNullOrEmpty()) {
                Glide.with(this@ProfileFragment)
                    .load(user?.profileImagePath)
                    .placeholder(R.drawable.ic_user_placeholder)
                    .circleCrop()
                    .into(binding.ivAvatar)
            } else {
                binding.ivAvatar.setImageResource(R.drawable.ic_user_placeholder)
            }
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {  }
                is Result.Success -> {
                    Toast.makeText(requireContext(), "Berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(), result.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showUpdateUserDialog() {
        val dialogBinding = DialogUpdateUserBinding.inflate(layoutInflater)
        dialogBinding.etUsername.setText(sessionManager.getUsername())
        dialogBinding.etEmail.setText(sessionManager.getEmail())

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSave.setOnClickListener {
            val username = dialogBinding.etUsername.text.toString().trim()
            val email = dialogBinding.etEmail.text.toString().trim()
            val token = sessionManager.getToken() ?: ""

            if (username.isEmpty() || email.isEmpty()) {
                Toast.makeText(requireContext(), "Harap isi semua field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateUser(token, username, email)
            sessionManager.saveSession(token, username, email)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showUpdatePasswordDialog() {
        val dialogBinding = DialogUpdatePasswordBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSave.setOnClickListener {
            val oldPass = dialogBinding.etOldPassword.text.toString().trim()
            val newPass = dialogBinding.etNewPassword.text.toString().trim()
            val confirm = dialogBinding.etConfirmPassword.text.toString().trim()
            val token = sessionManager.getToken() ?: ""

            if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(requireContext(), "Harap isi semua field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!newPass.any { it.isDigit() }) {
                dialogBinding.tilNewPassword.error = "Harus mengandung angka"
                return@setOnClickListener
            }

            if (newPass != confirm) {
                Toast.makeText(requireContext(), "Konfirmasi tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updatePassword(token, oldPass, newPass, confirm)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showImagePickerOptions() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.layout_image_picker, null)
        
        view.findViewById<View>(R.id.btnCamera).setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
            dialog.dismiss()
        }
        
        view.findViewById<View>(R.id.btnGallery).setOnClickListener {
            galleryLauncher.launch("image/*")
            dialog.dismiss()
        }
        
        dialog.setContentView(view)
        dialog.show()
    }

    private fun openCamera() {
        val photoFile: File? = try { createImageFile() } catch (ex: IOException) { null }
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", it)
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }
            cameraLauncher.launch(takePictureIntent)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Keluar Akun")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Keluar") { _, _ -> handleUnauthorized() }
            .show()
    }

    private fun handleUnauthorized() {
        sessionManager.logout()
        startActivity(Intent(requireActivity(), AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
