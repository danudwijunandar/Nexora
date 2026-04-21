package com.example.nexora1.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nexora1.R
import com.example.nexora1.databinding.BottomSheetAddSelectionBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddSelectionBottomSheet(private val onSelection: (SelectionType) -> Unit) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddSelectionBinding? = null
    private val binding get() = _binding!!

    enum class SelectionType {
        ACTIVITY, FINANCE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnTypeActivity.setOnClickListener {
            onSelection(SelectionType.ACTIVITY)
            dismiss()
        }

        binding.btnTypeFinance.setOnClickListener {
            onSelection(SelectionType.FINANCE)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}