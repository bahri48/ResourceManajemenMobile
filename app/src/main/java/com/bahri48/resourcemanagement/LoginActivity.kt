// Mendeklarasikan package yang sama untuk semua file dalam modul ini
package com.bahri48.resourcemanagement

// Mengimpor Intent untuk navigasi antar Activity
import android.content.Intent
// Mengimpor Bundle untuk menyimpan state Activity saat configuration changes
import android.os.Bundle
// Mengimpor View untuk mengakses properti visibility dan enable UI elements
import android.view.View
// Mengimpor widget Android yang digunakan di layout
import android.widget.*
// Mengimpor AppCompatActivity sebagai base class untuk Activity dengan support library features
import androidx.appcompat.app.AppCompatActivity
// Mengimpor ViewModelProvider untuk membuat/mendapatkan instance ViewModel
import androidx.lifecycle.ViewModelProvider

// Class LoginActivity adalah UI screen pertama untuk autentikasi user
// AppCompatActivity menyediakan compatibility features untuk Android versi lama
// Activity adalah salah satu dari 4 komponen utama Android dan mewakili satu screen UI
class LoginActivity : AppCompatActivity() {

    // Deklarasi variabel untuk ViewModel yang mengelola logic login
    // 'lateinit' berarti akan diinisialisasi nanti di onCreate() sebelum digunakan
    // Ini menghindari nullable type dan null checks berulang kali
    private lateinit var viewModel: LoginViewModel
    
    // Deklarasi variabel untuk SessionManager yang mengelola session persistence
    // SessionManager akan mengecek apakah user sudah login sebelumnya
    private lateinit var sessionManager: SessionManager

    // Deklarasi variabel UI widget yang ada di layout activity_login.xml
    // Semua menggunakan lateinit karena akan diinisialisasi dengan findViewById() di onCreate()
    
    // EditText untuk input email user
    private lateinit var etEmail: EditText
    // EditText untuk input password user (dengan inputType textPassword untuk mask input)
    private lateinit var etPassword: EditText
    // Button untuk trigger proses login saat diklik
    private lateinit var btnLogin: Button
    // ProgressBar untuk menampilkan loading indicator saat request berlangsung
    private lateinit var loader: ProgressBar

    // onCreate dipanggil saat Activity pertama kali dibuat
    // Ini adalah lifecycle method Android yang wajib di-override untuk setup awal
    // savedInstanceState berisi state sebelumnya jika Activity direcreate (misalnya screen rotation)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Memanggil onCreate parent class (wajib untuk Android lifecycle)
        super.onCreate(savedInstanceState)

        // Membuat instance SessionManager dengan context Activity ini
        // SessionManager akan mengakses SharedPreferences untuk cek session
        sessionManager = SessionManager(this)
        
        // CEK SESSION: Mengecek apakah user sudah login sebelumnya
        // Jika user sudah pernah login dan session belum di-logout
        // langsung navigate ke MainActivity tanpa perlu login ulang
        if (sessionManager.isLoggedIn()) {
            // Memanggil method helper untuk navigasi ke MainActivity
            moveToMainActivity()
            // Return menghentikan execution, sehingga setContentView dan setup tidak dijalankan
            // Ini menghemat resource karena Activity langsung pindah tanpa render UI login
            return
        }

        // Menginflasi layout XML activity_login.xml dan menampilkannya di Activity
        // R.layout.activity_login adalah auto-generated reference dari file XML di res/layout/
        setContentView(R.layout.activity_login)

        // Menghubungkan variabel Kotlin dengan UI element dari XML layout menggunakan findViewById
        // findViewById mencari view berdasarkan ID yang didefinisikan di XML (android:id="@+id/...")
        // R.id.etEmail adalah auto-generated reference ke EditText dengan id "etEmail"
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        loader = findViewById(R.id.loader)

        // Membuat atau mendapatkan instance LoginViewModel menggunakan ViewModelProvider
        // ViewModelProvider memastikan ViewModel survive configuration changes (screen rotation)
        // Tanpa ViewModelProvider, ViewModel akan hilang saat rotation dan data login hilang
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // Mengatur click listener untuk tombol Login
        // Lambda { ... } akan dijalankan setiap kali button diklik
        btnLogin.setOnClickListener {
            // Mengambil text dari EditText email, mengonversi ke String, dan menghapus whitespace di awal/akhir
            // .trim() menghapus spasi/tab/newline yang tidak sengaja diinput user
            val email = etEmail.text.toString().trim()
            // Mengambil text dari EditText password dan menghapus whitespace
            val pass = etPassword.text.toString().trim()

            // Validasi input menggunakan 'when' expression (seperti switch-case yang lebih powerful)
            when {
                // Kasus 1: Cek apakah email atau password kosong
                // Jika salah satu kosong, tampilkan toast warning
                email.isEmpty() || pass.isEmpty() -> {
                    // Toast.makeText membuat popup pesan singkat yang hilang otomatis
                    // this = context, parameter kedua = pesan, parameter ketiga = durasi
                    Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
                }
                // Kasus 2: Cek apakah format email tidak valid menggunakan regex pattern dari Android
                // Patterns.EMAIL_ADDRESS adalah constant regex bawaan Android untuk validasi email
                // .matcher(email).matches() mengecek apakah string email match dengan pattern
                // ! berarti negasi: jika TIDAK match, tampilkan error
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Toast.makeText(this, "Format email tidak valid", Toast.LENGTH_SHORT).show()
                }
                // Kasus 3: Jika semua validasi lolos (email dan password tidak kosong, format email valid)
                // Jalankan proses login dengan memanggil ViewModel
                else -> {
                    // Memanggil method performLogin di ViewModel untuk memulai request API
                    // ViewModel akan menangani network call dan mengembalikan hasil via LiveData
                    viewModel.performLogin(email, pass)
                }
            }
        }

        // Memulai observasi LiveData dari ViewModel
        // Method ini akan setup observer yang merespon perubahan data dari ViewModel
        observeData()
    }

    // Method private untuk setup observer LiveData dari ViewModel
    // Observer akan automatically dipanggil setiap kali nilai LiveData berubah
    private fun observeData() {

        // Observer untuk LiveData loginResponse (response sukses dari API)
        // observe(this) mendaftarkan observer dengan LifecycleOwner (Activity)
        // Lambda { res -> ... } akan dijalankan setiap kali loginResponse diupdate
        viewModel.loginResponse.observe(this) { res ->
            // res?.let { ... } hanya menjalankan blok jika res tidak null
            // 'it' adalah implicit parameter yang merepresentasikan res yang sudah di-safe-call
            res?.let {
                // Mengecek apakah status dari response adalah "success"
                // Backend bisa mengembalikan status lain seperti "error", "failed", dll
                if (it.status == "success") {
                    // Membuat objek User dari data response
                    // Mengextract id, name, email dari nested property it.data.user
                    // User object ini akan disimpan ke SharedPreferences
                    val user = User(
                        id = it.data.user.id,
                        name = it.data.user.name,
                        email = it.data.user.email
                    )

                    // Mengextract token autentikasi dari response
                    // Token ini akan disimpan bersama user data untuk request authenticated endpoints
                    val token = it.data.token

                    // Menyimpan user dan token ke SharedPreferences melalui SessionManager
                    // Ini membuat session persistent - user tidak perlu login lagi saat buka aplikasi
                    sessionManager.saveSession(user, token)

                    // Menampilkan pesan sukses dari API response sebagai Toast
                    // Pesan ini biasanya berisi "Login berhasil" atau sejenisnya
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    
                    // Pindah ke MainActivity setelah login sukses dan session tersimpan
                    moveToMainActivity()

                } else {
                    // Jika status bukan "success" (misalnya "error" atau "failed")
                    // Tampilkan pesan error dari server sebagai Toast
                    // Pesan ini biasanya menjelaskan kenapa login gagal (wrong password, etc)
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observer untuk LiveData error (error message dari network/API failure)
        // Error ini diupdate di ViewModel saat request gagal total (bukan HTTP error)
        viewModel.error.observe(this) { errMsg ->
            // Mengecek apakah errMsg tidak null dan tidak kosong
            // isNullOrEmpty() adalah extension function Kotlin yang cek null dan empty string
            if (!errMsg.isNullOrEmpty()) {
                // Menampilkan pesan error sebagai Toast dengan durasi LONG
                // Error biasanya lebih panjang dan user perlu waktu lebih lama untuk baca
                Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show()
            }
        }

        // Observer untuk LiveData isLoading (loading state)
        // isLoading diupdate ViewModel saat request dimulai (true) dan selesai (false)
        viewModel.isLoading.observe(this) { show ->
            // Mengatur visibility ProgressBar berdasarkan loading state
            // Jika show = true: View.VISIBLE (tampilkan spinner)
            // Jika show = false: View.GONE (sembunyikan spinner, tidak ambil space)
            loader.visibility = if (show) View.VISIBLE else View.GONE
            // Mengatur enabled state tombol Login
            // Jika show = true: btnLogin.isEnabled = false (disable button saat loading)
            // Jika show = false: btnLogin.isEnabled = true (enable button setelah loading)
            // Ini mencegah user spam klik tombol saat request sedang berjalan
            btnLogin.isEnabled = !show
        }
    }

    // Method helper untuk navigasi ke MainActivity
    // Dibuat sebagai method terpisah agar reusable dan kode lebih bersih
    private fun moveToMainActivity() {
        // Membuat Intent untuk pindah dari LoginActivity ke MainActivity
        // Intent adalah messaging object untuk komunikasi antar komponen Android
        // this = context (Activity saat ini), MainActivity::class.java = target Activity class
        startActivity(Intent(this, MainActivity::class.java))
    }
}