package com.example.nexora1.ui.auth.register

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.nexora1.R
import com.example.nexora1.data.Result
import com.example.nexora1.databinding.FragmentRegisterBinding
import com.example.nexora1.ui.ViewModelFactory
import com.example.nexora1.ui.auth.AuthViewModel

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTermsAndConditions()

        binding.tvLoginLink.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSignUp.setOnClickListener {
            val username = binding.tilUserName.editText?.text.toString().trim()
            val email = binding.tilEmail.editText?.text.toString().trim()
            val password = binding.tilCreatePassword.editText?.text.toString().trim()
            val confirm = binding.tilConfirmPassword.editText?.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(requireContext(), "Password tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!binding.checkBox.isChecked) {
                Toast.makeText(requireContext(), "Harap setujui Syarat dan Ketentuan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(username, email, password, confirm)
        }

        observeViewModel()
    }

    private fun setupTermsAndConditions() {
        val fullText = getString(R.string.check_terms_condition)
        val spannableString = SpannableString(fullText)
        
        // "Syarat Ketentuan Nexora" usually starts at index 23 in "Saya setuju dengan Syarat Ketentuan Nexora"
        // But let's find it dynamically to be safe
        val target = "Syarat Ketentuan Nexora"
        val startIndex = fullText.indexOf(target)
        val endIndex = startIndex + target.length

        if (startIndex != -1) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    showTermsDialog()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    ds.color = Color.BLUE // Or use your primary color
                }
            }
            spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.checkBox.text = spannableString
        binding.checkBox.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun showTermsDialog() {
        val termsText = """
            Selamat datang di Nexora. Dengan mengunduh, mengakses, atau menggunakan aplikasi Nexora, Anda setuju untuk terikat oleh Syarat dan Ketentuan ini. Harap baca dokumen ini dengan saksama sebelum mulai menggunakan layanan kami.

            1. Deskripsi Layanan
            Nexora adalah aplikasi produktivitas yang dirancang khusus untuk membantu mengelola rutinitas dan tata guna Anda. Fitur layanan yang kami sediakan meliputi, namun tidak terbatas pada:
            - Pencatatan dan pemantauan aktivitas harian.
            - Pengelolaan dan pelacakan keuangan pribadi.
            - Pengaturan profil pengguna.

            2. Penyimpanan Data dan Mode Luring (Offline-First)
            Penyimpanan Perangkat Lokal: Nexora mengutamakan kapabilitas offline-first. Ini berarti data aktivitas dan catatan keuangan Anda akan disimpan secara langsung di dalam ruang penyimpanan lokal perangkat Anda agar aplikasi tetap dapat diakses tanpa koneksi internet.
            Sinkronisasi Jaringan: Saat perangkat terhubung ke internet, aplikasi mungkin akan berkomunikasi dengan sistem peladen (server) kami untuk keperluan pencadangan, sinkronisasi data antarperangkat, atau otentikasi.
            Keamanan Data Fisik: Karena sebagian besar data sensitif berada langsung di perangkat keras Anda, Anda bertanggung jawab penuh untuk menjaga keamanan perangkat ponsel Anda dari akses fisik maupun digital yang tidak sah.

            3. Tanggung Jawab dan Kewajiban Pengguna
            Sebagai pengguna Nexora, Anda diwajibkan untuk:
            - Menjaga kerahasiaan informasi kredensial akun Anda.
            - Tidak menggunakan aplikasi ini untuk tujuan ilegal, pencucian uang, penipuan, atau tindakan lain yang melanggar hukum.
            - Tidak mencoba untuk menyalin, memodifikasi, meretas, atau melakukan rekayasa balik (reverse engineering) terhadap arsitektur, kode, atau sistem basis data Nexora.

            4. Privasi Pengguna
            Nexora berkomitmen untuk melindungi informasi Anda. Rincian lebih lanjut mengenai bagaimana kami memproses, menggunakan, dan melindungi data pribadi Anda dapat ditemukan dalam dokumen Kebijakan Privasi kami yang terpisah.

            5. Batasan Tanggung Jawab (Limitation of Liability)
            Informasi Keuangan: Nexora murni merupakan alat bantu pencatatan produktivitas dan keuangan. Kami tidak bertindak sebagai penasihat keuangan. Segala keputusan finansial atau kerugian yang timbul akibat kesalahan input data sepenuhnya merupakan tanggung jawab pengguna.
            Kondisi Aplikasi: Aplikasi ini disediakan "sebagaimana adanya" (as is). Kami terus berupaya memberikan pengalaman terbaik, namun kami tidak menjamin aplikasi akan selalu bebas dari galat (bug), gangguan koneksi API, atau kehilangan data lokal yang disebabkan oleh kerusakan perangkat pengguna.

            6. Pembaruan dan Modifikasi Layanan
            Kami berhak untuk mengubah, menangguhkan, atau menghentikan sebagian maupun seluruh fitur aplikasi Nexora kapan saja, dengan atau tanpa pemberitahuan sebelumnya. Kami juga dapat memperbarui Syarat dan Ketentuan ini sewaktu-waktu. Penggunaan berkelanjutan atas aplikasi setelah pembaruan menandakan bahwa Anda menerima perubahan tersebut.

            7. Kontak dan Bantuan
            Jika Anda memiliki pertanyaan, kendala teknis, atau membutuhkan bantuan lebih lanjut terkait aplikasi Nexora, silakan hubungi tim layanan pelanggan kami.
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Syarat dan Ketentuan")
            .setMessage(termsText)
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.registerResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.btnSignUp.isEnabled = false
                    binding.btnSignUp.text = ""
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSignUp.text = getString(R.string.btn_register)
                    Toast.makeText(requireContext(), "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSignUp.text = getString(R.string.btn_register)
                    binding.btnSignUp.isEnabled = true
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
