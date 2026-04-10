// Mendeklarasikan package yang sama untuk semua file dalam modul ini
package com.bahri48.resourcemanagement

// Mengimpor Callback dari Retrofit untuk menangani response asynchronous
import retrofit2.Callback

// Class LoginRepository berfungsi sebagai abstraction layer antara ViewModel dan network layer
// Repository adalah single source of truth untuk operasi login
// Dalam arsitektur MVVM, ViewModel tidak boleh langsung mengakses Retrofit, harus lewat Repository
class LoginRepository {
    // Mengambil instance ApiService dari singleton RetrofitClient
    // 'private val' berarti properti ini hanya bisa dibaca dari dalam class ini
    // Repository memiliki instance API untuk melakukan request HTTP
    private val api = RetrofitClient.instance

    // Method loginUser menangani proses login dengan menerima email, password, dan callback
    // Callback adalah interface Retrofit yang memiliki method onResponse() dan onFailure()
    // Callback akan dipanggil di main thread setelah request selesai
    fun loginUser(email: String, pass: String, callback: Callback<UserResponse>) {
        // Membuat HashMap berisi credential yang akan dikirim ke server
        // hashMapOf adalah fungsi Kotlin yang membuat mutable HashMap
        // "email" to email adalah sintaks Kotlin untuk membuat Pair yang otomatis dikonversi ke Map entry
        // Hasilnya: {"email": "user@example.com", "password": "secret123"}
        val params = hashMapOf("email" to email, "password" to pass)
        
        // Memanggil method login() dari ApiService dengan parameter params
        // .enqueue() menjalankan request secara asynchronous di background thread
        // Callback akan dipanggil di main thread saat request selesai (sukses atau gagal)
        // Ini mencegah blocking UI thread yang bisa menyebabkan ANR (Application Not Responding)
        api.login(params).enqueue(callback)
    }
}