// Mendeklarasikan package yang sama untuk semua file dalam modul ini
package com.bahri48.resourcemanagement

// Mengimpor Log untuk menulis pesan ke Logcat (debugging tool Android)
import android.util.Log
// Mengimpor LiveData untuk observable data holder yang lifecycle-aware
import androidx.lifecycle.LiveData
// Mengimpor MutableLiveData untuk LiveData yang bisa diubah nilainya
import androidx.lifecycle.MutableLiveData
// Mengimpor ViewModel sebagai base class untuk ViewModel yang lifecycle-aware
import androidx.lifecycle.ViewModel
// Mengimpor Call dari Retrofit untuk tipe parameter callback
import retrofit2.Call
// Mengimpor Callback dari Retrofit untuk menangani response asynchronous
import retrofit2.Callback
// Mengimpor Response dari Retrofit untuk membungkus HTTP response
import retrofit2.Response

// Class LoginViewModel memperluas ViewModel sebagai bagian dari arsitektur MVVM
// ViewModel bertanggung jawab untuk mengelola UI-related data dan bertahan saat configuration changes
// (misalnya screen rotation) sehingga data tidak hilang
class LoginViewModel : ViewModel() {
    // Membuat instance LoginRepository sebagai dependency untuk operasi login
    // ViewModel tidak mengakses Retrofit secara langsung, tapi melalui Repository
    // Ini memisahkan concerns: ViewModel mengurus UI data, Repository mengurus network
    private val repository = LoginRepository()

    // === ENCAPSULATION PATTERN: Private Mutable, Public Immutable ===
    // Pola ini mencegah class lain mengubah LiveData secara langsung
    // Hanya ViewModel yang bisa mengubah nilai (melalui private MutableLiveData)
    // Observer (Activity) hanya bisa membaca/mengobserve (melalui public LiveData)

    // _loginResponse adalah private MutableLiveData yang bisa diubah nilainya dari dalam ViewModel
    // Tipe UserResponse? berarti nullable (bisa bernilai null)
    private val _loginResponse = MutableLiveData<UserResponse?>()
    // loginResponse adalah public LiveData yang hanya bisa diobserve, tidak bisa diubah dari luar
    // Tipe LiveData<UserResponse?> membuat caller tidak bisa memanggil .value = ...
    val loginResponse: LiveData<UserResponse?> = _loginResponse

    // _error menyimpan pesan error sebagai String
    private val _error = MutableLiveData<String>()
    // error adalah public immutable version dari _error
    val error: LiveData<String> = _error

    // _isLoading menunjukkan apakah request login sedang berjalan
    // Boolean true = loading, false = tidak loading
    private val _isLoading = MutableLiveData<Boolean>()
    // isLoading adalah public immutable version dari _isLoading
    val isLoading: LiveData<Boolean> = _isLoading

    // Method performLogin memulai proses login dengan email dan password
    // Method ini dipanggil oleh LoginActivity saat user menekan tombol Login
    fun performLogin(email: String, pass: String) {
        // Set loading state ke true sebelum request dimulai
        // Ini akan membuat Activity menampilkan ProgressBar dan disable tombol Login
        _isLoading.value = true

        // Memanggil repository.loginUser dengan credential dan anonymous Callback object
        // 'object : Callback<UserResponse>' membuat anonymous inner class yang implement Callback interface
        // Callback ini akan menangani response dari server (sukses atau gagal)
        repository.loginUser(email, pass, object : Callback<UserResponse> {
            // Method onResponse dipanggil saat HTTP request selesai (baik status 200 maupun 4xx/5xx)
            // call: objek Call yang bisa digunakan untuk retry/cancel
            // response: objek Response yang berisi status code, headers, dan body
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                // Set loading state ke false karena request sudah selesai
                _isLoading.value = false
                
                // Mengecek apakah response successful (HTTP status code 2xx)
                // response.isSuccessful true untuk status code 200-299
                if (response.isSuccessful) {
                    // Menulis log sukses dengan response body ke Logcat dengan tag "API_SUCCESS"
                    // Log.d adalah debug log yang bisa dilihat di Android Studio Logcat
                    Log.d("API_SUCCESS", "Data: ${response.body()}")
                    // Mengupdate _loginResponse dengan data user dari response body
                    // Ini akan memicu observer di LoginActivity untuk memproses data
                    _loginResponse.value = response.body()
                } else {
                    // Jika response tidak successful (misalnya 401, 500, dll)
                    // Mengambil error body dari response sebagai String
                    // ?. adalah safe call operator untuk menghindari NullPointerException
                    val errorBody = response.errorBody()?.string()
                    // Menulis log error dengan status code dan error body ke Logcat dengan tag "API_ERROR"
                    Log.e("API_ERROR", "Code: ${response.code()} Body: $errorBody")
                    // Mengupdate _error dengan pesan error berisi status code
                    // Ini akan memicu observer error di LoginActivity untuk menampilkan Toast error
                    _error.value = "Error: ${response.code()}"
                }
            }

            // Method onFailure dipanggil saat request gagal total (bukan karena status code)
            // Misalnya: tidak ada koneksi internet, timeout, DNS tidak ditemukan
            // call: objek Call yang gagal
            // t: Throwable yang berisi informasi exception/error
            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                // Set loading state ke false karena request sudah selesai (walaupun gagal)
                _isLoading.value = false
                // Menulis log failure dengan pesan error ke Logcat dengan tag "API_FAILURE"
                Log.e("API_FAILURE", "Message: ${t.message}")
                // Mengupdate _error dengan pesan error dari Throwable
                // Pesan ini akan ditampilkan ke user melalui Toast di LoginActivity
                _error.value = t.message
            }
        // Tutup anonymous Callback object dan kurung tutup method loginUser
        // Kurung tutup ini menutup pemanggilan repository.loginUser(...)
        }) // Tutup object dan method loginUser
    }
}