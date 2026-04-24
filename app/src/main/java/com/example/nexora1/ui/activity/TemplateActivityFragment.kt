package com.example.nexora1.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexora1.R
import com.example.nexora1.databinding.FragmentTemplateActivityBinding

class TemplateActivityFragment : Fragment() {
    private var _binding: FragmentTemplateActivityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTemplateActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val templates = listOf(
            ActivityTemplate("Minum air putih", R.drawable.ic_activity, "kesehatan"),
            ActivityTemplate("Jalan pagi", R.drawable.ic_activity, "kesehatan"),
            ActivityTemplate("Evaluasi diri", R.drawable.ic_activity, "kesehatan"),
            ActivityTemplate("Pergi tidur lebih awal", R.drawable.ic_activity, "kesehatan"),
            ActivityTemplate("Bangun pagi", R.drawable.ic_activity, "kesehatan"),
            ActivityTemplate("Olahraga", R.drawable.ic_activity, "kesehatan"),
            ActivityTemplate("Istirahat", R.drawable.ic_activity, "kesehatan"),
            ActivityTemplate("Makan buah-buahan", R.drawable.ic_activity, "kesehatan")
        )

        val adapter = TemplateActivityAdapter(templates) { template ->
            val bundle = Bundle().apply {
                putString("title", template.title)
                putString("category", template.category)
            }
            findNavController().navigate(R.id.addActivityFragment, bundle)
        }

        binding.rvTemplates.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTemplates.adapter = adapter

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}