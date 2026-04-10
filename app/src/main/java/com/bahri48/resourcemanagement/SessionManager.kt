// Mendeklarasikan package yang sama untuk semua file dalam modul ini
package com.bahri48.resourcemanagement

// Mengimpor Context dari Android framework untuk mengakses SharedPreferences
import android.content.Context
// Mengimpor Gson dari library Google Gson untuk serialisasi/deserialisasi JSON
import com.google.gson.Gson

// Class SessionManager mengelola session pengguna menggunakan SharedPreferences
// SharedPreferences adalah cara Android menyimpan data persisten sederhana (key-value pairs)
// Data akan bertahan meskipun aplikasi ditutup atau device di-restart
class SessionManager(context: Context) {

    // Mengambil instance SharedPreferences dengan nama "app_prefs"
    // MODE_PRIVATE berarti file ini hanya bisa dibaca oleh aplikasi ini saja (aman)
    // 'prefs' akan digunakan untuk membaca dan menulis data session
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    
    // Membuat instance Gson untuk konversi antara objek Kotlin dan JSON string
    // Gson bisa mengonversi User object -> JSON string (saat save)
    // dan JSON string -> User object (saat load)
    private val gson = Gson()

    // Method saveSession menyimpan data user dan token ke SharedPreferences
    // Method ini dipanggil setelah login berhasil
    fun saveSession(user: User, token: String) {
        // Mengonversi objek User menjadi JSON string menggunakan Gson
        // Contoh hasil: {"id":1,"name":"John Doe","email":"john@example.com"}
        val json = gson.toJson(user)
        
        // Memulai transaksi edit pada SharedPreferences
        // .edit() membuat Editor object yang bisa mengubah data
        prefs.edit()
            // Menyimpan JSON user dengan key "user_data"
            .putString("user_data", json)
            // Menyimpan token autentikasi dengan key "token"
            .putString("token", token)
            // Menyimpan flag bahwa user sudah login dengan key "is_login"
            // true berarti user sedang dalam keadaan login
            .putBoolean("is_login", true)
            // .apply() menyimpan data secara asynchronous (non-blocking)
            // Lebih disarankan daripada .commit() yang synchronous dan bisa blocking main thread
            .apply()
    }

    // Method getUser mengambil data User dari SharedPreferences
    // Return type User? berarti bisa mengembalikan User object atau null (jika belum login)
    fun getUser(): User? {
        // Membaca JSON string dengan key "user_data" dari SharedPreferences
        // Parameter kedua (null) adalah default value jika key tidak ditemukan
        val json = prefs.getString("user_data", null)
        
        // Menggunakan try-catch untuk menangani kemungkinan error saat parsing JSON
        // JSON bisa corrupted atau formatnya berubah, jadi perlu error handling
        return try {
            // Jika JSON tidak null, parse kembali menjadi User object menggunakan Gson
            // fromJson mengonversi JSON string kembali menjadi objek User
            if (json != null) gson.fromJson(json, User::class.java) else null
        } catch (e: Exception) {
            // Jika terjadi error saat parsing (JSON rusak, class berubah, dll), return null
            // Ini mencegah aplikasi crash saat data corrupt
            null
        }
    }

    // Method getToken mengambil token autentikasi dari SharedPreferences
    // Return type String? berarti bisa mengembalikan token string atau null
    fun getToken(): String? {
        // Membaca value dengan key "token" dari SharedPreferences
        // Parameter kedua (null) adalah default value jika key tidak ditemukan
        return prefs.getString("token", null)
    }

    // Method isLoggedIn mengecek apakah user sudah login
    // Mengembalikan boolean: true jika sudah login, false jika belum
    // Single expression function (langsung return tanpa curly braces)
    fun isLoggedIn(): Boolean = prefs.getBoolean("is_login", false)
    // prefs.getBoolean("is_login", false) membaca flag login
    // Parameter kedua (false) adalah default value jika key belum ada
    // Default false berarti user dianggap belum login kalau belum pernah save session

    // Method logout menghapus semua data session dari SharedPreferences
    // Method ini dipanggil saat user ingin keluar dari aplikasi
    fun logout() {
        // .edit().clear() menghapus SEMUA key-value pairs dari SharedPreferences "app_prefs"
        // Ini akan menghapus user_data, token, dan is_login sekaligus
        // .apply() menyimpan perubahan secara asynchronous
        prefs.edit().clear().apply()
    }
}