package com.example.nexora1.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexora1.data.local.room.NexoraDatabase
import com.example.nexora1.data.local.room.NotificationEntity
import com.example.nexora1.databinding.FragmentNotificationBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationFragment : Fragment() {
    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NotificationAdapter
    private lateinit var database: NexoraDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = NexoraDatabase.getInstance(requireContext())
        
        setupRecyclerView()
        
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        observeNotifications()
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter { notification ->
            showDeleteDialog(notification)
        }
        binding.rvNotification.layoutManager = LinearLayoutManager(context)
        binding.rvNotification.adapter = adapter
    }

    private fun showDeleteDialog(notification: NotificationEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Notifikasi")
            .setMessage("Hapus notifikasi ini?")
            .setPositiveButton("Ya") { _, _ ->
                deleteNotification(notification)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun deleteNotification(notification: NotificationEntity) {
        viewLifecycleOwner.lifecycleScope.launch {
            database.notificationDao().deleteNotification(notification)
        }
    }

    private fun observeNotifications() {
        viewLifecycleOwner.lifecycleScope.launch {
            database.notificationDao().getAllNotifications().collectLatest { notifications ->
                if (notifications.isEmpty()) {
                    binding.rvNotification.visibility = View.GONE
                    binding.tvEmpty.visibility = View.VISIBLE
                } else {
                    binding.rvNotification.visibility = View.VISIBLE
                    binding.tvEmpty.visibility = View.GONE
                    adapter.submitList(notifications)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}