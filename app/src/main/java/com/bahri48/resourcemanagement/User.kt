// Mendeklarasikan package tempat class ini berada
// Semua file dalam aplikasi ini menggunakan package yang sama
package com.bahri48.resourcemanagement

// Mendefinisikan data class User yang merepresentasikan data pengguna
// 'data class' secara otomatis menghasilkan method: equals(), hashCode(), toString(), copy(), dan componentN()
// Class ini digunakan untuk menyimpan informasi user yang didapat dari API response
data class User(
    // Properti id bertipe Int untuk menyimpan ID unik user dari database
    // 'val' membuat properti ini immutable (tidak bisa diubah setelah diinisialisasi)
    val id: Int,
    
    // Properti name bertipe String untuk menyimpan nama lengkap user
    val name: String,
    
    // Properti email bertipe String untuk menyimpan alamat email user
    // Email ini juga digunakan sebagai credential untuk login
    val email: String
)
