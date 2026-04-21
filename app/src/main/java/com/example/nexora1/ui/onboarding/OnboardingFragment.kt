package com.example.nexora1.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.nexora1.R
import com.example.nexora1.data.local.prefs.SessionManager
import com.example.nexora1.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        if (sessionManager.isOnboardingDone()) {
            findNavController().navigate(R.id.action_onboardingFragment_to_loginFragment)
            return
        }

        val items = listOf(
            OnboardingItem(R.drawable.ic_onboarding_1, "Organisasi Harimu", "Kelola jadwal, tugas harian, dan pantau kemajuan produk..."),
            OnboardingItem(R.drawable.ic_onboarding_2, "Bangun Kebiasaan Positif", "Lacak rutinitasmu, capai target harian, dan pertahankan..."),
            OnboardingItem(R.drawable.ic_onboarding_3, "Kontrol Keuangan & Kesehatan", "Pantau pengeluaran, perhatikan mood harian, dan pastika...")
        )

        val adapter = OnboardingAdapter(items)
        binding.viewPager.adapter = adapter

        setupIndicators(items.size)
        setCurrentIndicator(0)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
                if (position == items.size - 1) {
                    binding.btnNext.text = getString(R.string.btn_login)
                } else {
                    binding.btnNext.text = getString(R.string.next)
                }
            }
        })

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem + 1 < items.size) {
                binding.viewPager.currentItem += 1
            } else {
                completeOnboarding()
            }
        }

        binding.tvSkip.setOnClickListener {
            completeOnboarding()
        }
    }

    private fun setupIndicators(size: Int) {
        val indicators = arrayOfNulls<ImageView>(size)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        layoutParams.setMargins(8, 0, 8, 0)
        for (i in indicators.indices) {
            indicators[i] = ImageView(requireContext())
            indicators[i]?.apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_launcher_background // Using a placeholder, should be a dot
                    )
                )
                this.layoutParams = layoutParams
            }
            binding.layoutIndicators.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = binding.layoutIndicators.childCount
        for (i in 0 until childCount) {
            val imageView = binding.layoutIndicators.getChildAt(i) as ImageView
            if (i == index) {
                imageView.alpha = 1f
            } else {
                imageView.alpha = 0.5f
            }
        }
    }

    private fun completeOnboarding() {
        sessionManager.setOnboardingDone(true)
        findNavController().navigate(R.id.action_onboardingFragment_to_loginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}