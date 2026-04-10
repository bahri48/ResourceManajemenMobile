// Mendeklarasikan package yang sama untuk semua file dalam modul ini
package com.bahri48.resourcemanagement

// Mengimpor Call dari Retrofit untuk merepresentasikan HTTP request yang bisa dieksekusi
import retrofit2.Call
// Mengimpor annotation @Body untuk menandai parameter yang akan dikirim sebagai request body
import retrofit2.http.Body
// Mengimpor annotation @POST untuk menandai method sebagai HTTP POST request
import retrofit2.http.POST

// Interface ApiService mendefinisikan kontrak untuk semua endpoint API
// Retrofit akan membuat implementasi interface ini secara otomatis saat runtime
// Interface tidak memiliki implementasi, hanya definisi method dan annotation
interface ApiService {
    // Annotation @POST menandai method ini sebagai HTTP POST request
    // Parameter "login" adalah endpoint relatif yang akan ditambahkan ke BASE_URL
    // URL lengkapnya menjadi: https://pendataan-lab.bahri48.com/api/auth/login
    // Komentar lama: "// Ganti dengan endpoint Anda" mengingatkan untuk menyesuaikan endpoint
    @POST("login") // Ganti dengan endpoint Anda
    // Method login menerima parameter params dan mengembalikan Call<UserResponse>
    // @Body berarti parameter params akan diserialisasi menjadi JSON dan dikirim sebagai request body
    // HashMap<String, String> berisi key-value pairs seperti {"email": "...", "password": "..."}
    // Return type Call<UserResponse> adalah objek Retrofit yang merepresentasikan HTTP request
    // Call ini bisa dijalankan secara asynchronous menggunakan .enqueue() atau synchronous dengan .execute()
    fun login(@Body params: HashMap<String, String>): Call<UserResponse>
}