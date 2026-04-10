// Mendeklarasikan package yang sama untuk semua file dalam modul ini
package com.bahri48.resourcemanagement

// Data class UserResponse merepresentasikan struktur JSON response dari API login
// Class ini adalah wrapper terluar yang diterima dari server
// Contoh JSON: {"status": "success", "message": "Login berhasil", "data": {...}}
data class UserResponse(
    // Properti status menunjukkan apakah request berhasil atau gagal
    // Biasanya bernilai "success" atau "error"
    val status: String,
    
    // Properti message berisi pesan yang bisa ditampilkan ke user
    // Misalnya: "Login berhasil" atau "Email/password salah"
    val message: String,
    
    // Properti data berisi payload utama dari response
    // Bertipe DataResponse yang akan didefinisikan di bawah
    val data: DataResponse
)

// Data class DataResponse merepresentasikan payload dalam field "data" dari UserResponse
// Class ini berisi informasi user dan token autentikasi
data class DataResponse(
    // Properti user berisi objek User yang sudah terdeserialisasi dari JSON
    // Mengandung id, name, dan email user yang login
    val user: User,
    
    // Properti token berisi JWT token atau access token untuk autentikasi request selanjutnya
    // Token ini harus disimpan dan dikirim di header Authorization untuk request yang memerlukan autentikasi
    val token: String,
    
    // Properti token_type menunjukkan jenis token, biasanya "Bearer"
    // Digunakan bersama token saat mengirim di header: "Authorization: Bearer <token>"
    val token_type: String,
    
    // Properti expires_in menunjukkan waktu kadaluarsa token dalam detik
    // Misalnya 3600 berarti token berlaku selama 1 jam
    val expires_in: Int
)