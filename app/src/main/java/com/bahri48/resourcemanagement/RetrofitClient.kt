// Mendeklarasikan package yang sama untuk semua file dalam modul ini
package com.bahri48.resourcemanagement

// Mengimpor class Retrofit dari library Retrofit2 untuk membangun HTTP client
import retrofit2.Retrofit
// Mengimpor GsonConverterFactory untuk konversi otomatis antara JSON dan Kotlin data class
import retrofit2.converter.gson.GsonConverterFactory

// Object declaration membuat singleton (hanya ada satu instance selama aplikasi berjalan)
// 'object' di Kotlin thread-safe dan lazily initialized (dibuat saat pertama kali diakses)
// Ini memastikan hanya ada satu Retrofit instance di seluruh aplikasi (hemat resource)
object RetrofitClient {
    // Konstanta BASE_URL menyimpan URL dasar API untuk autentikasi
    // 'const val' berarti compile-time constant (lebih efisien dari val biasa)
    // Semua endpoint relatif akan ditambahkan ke URL ini, misalnya: BASE_URL + "login"
    private const val BASE_URL = "https://your-backend-url-api"

    // Properti instance bertipe ApiService yang akan diinisialisasi secara lazy
    // 'by lazy' berarti kode di dalam blok hanya dijalankan saat pertama kali instance diakses
    // Ini adalah optimasi performa - Retrofit tidak dibuat sampai benar-benar dibutuhkan
    val instance: ApiService by lazy {
        // Membuat instance Retrofit menggunakan Builder pattern
        val retrofit = Retrofit.Builder()
            // Menetapkan base URL untuk semua request
            // Endpoint relatif (seperti "login") akan ditambahkan ke URL ini
            .baseUrl(BASE_URL)
            // Menambahkan converter factory Gson untuk otomatis parse JSON ke data class
            // Tanpa ini, Retrofit hanya bisa mengembalikan String atau ResponseBody mentah
            .addConverterFactory(GsonConverterFactory.create())
            // Membangun instance Retrofit setelah semua konfigurasi selesai
            .build()
        // Membuat implementasi konkret dari interface ApiService
        // Retrofit menggunakan reflection dan annotation di interface untuk generate kode HTTP client
        retrofit.create(ApiService::class.java)
    }
}
