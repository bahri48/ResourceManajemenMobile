// Mengimpor Context dari Android framework untuk mengakses file system aplikasi
// Context memberikan akses ke resources, databases, file system, dll
import android.content.Context
// Mengimpor File dari Java standard library untuk operasi file system
import java.io.File

// Class ResourceManager mendemonstrasikan penggunaan .use{} untuk automatic resource management
// .use{} adalah Kotlin extension function yang menjamin resource (seperti file streams) akan ditutup
// ini equivalent dengan Java try-with-resources, tapi lebih简洁 dan mudah dibaca
// Class ini menunjukkan best practice untuk mengelola resources yang harus ditutup setelah dipakai
class ResourceManager(private val context: Context) {

    // Method manageFileResource menangani write dan read file dengan automatic cleanup
    // Parameter fileName: nama file yang akan dibuat/dibaca di internal storage
    // Parameter content: string content yang akan ditulis ke file
    // Return value: string content yang dibaca dari file (untuk verifikasi write berhasil)
    fun manageFileResource(fileName: String, content: String): String {
        // Membuat objek File yang menunjuk ke file di internal storage aplikasi
        // context.filesDir adalah direktori internal storage khusus untuk aplikasi ini
        // Path lengkapnya biasanya: /data/data/<package_name>/files/<fileName>
        // File di direktori ini hanya bisa diakses oleh aplikasi ini (aman)
        val file = File(context.filesDir, fileName)

        // 1. Menulis ke file menggunakan .use{}
        // FileOutputStream akan otomatis ditutup setelah blok selesai
        // 
        // PENJELASAN .use{} :
        // - .use{} adalah Kotlin extension function untuk Closeable/AutoCloseable objects
        // - Menjamin method close() dipanggil saat blok exit, baik normal maupun via exception
        // - Format: resource.use { variable -> ... kode ... }
        // - Setelah blok selesai (return atau throw exception), resource otomatis di-close
        // - Ini mencegah resource leaks (file descriptors, memory, dll)
        // - Tanpa .use{}, kita harus manual write try-finally block dan panggil close()
        file.outputStream().use { output ->
            // Mengonversi string content menjadi byte array dan menulis ke file
            // .toByteArray() mengonversi String ke ByteArray menggunakan default charset (UTF-8)
            // output.write() menulis bytes ke file
            output.write(content.toByteArray())
        } // Resource 'output' tertutup otomatis di sini
        // Setelah blok .use{} selesai, outputStream.close() otomatis dipanggil
        // Bahkan jika write() throw exception, stream tetap akan ditutup

        // 2. Membaca dari file menggunakan .use{}
        // Menjamin BufferedReader dan InputStreamReader tertutup meski ada error
        // Method ini return string yang dibaca dari file
        return file.inputStream().bufferedReader().use { reader ->
            // Membaca seluruh isi file sebagai String dan mengembalikannya
            // .readText() membaca semua bytes dari InputStream dan mengonversi ke String
            // Ini mengembalikan content yang tadi ditulis, untuk verifikasi bahwa write/read berhasil
            reader.readText() // Mengembalikan teks yang dibaca
        } // Resource 'reader' tertutup otomatis di sini
        // Setelah blok .use{} selesai:
        // - BufferedReader.close() dipanggil (otomatis)
        // - InputStreamReader.close() dipanggil (otomatis)  
        // - FileInputStream.close() dipanggil (otomatis)
        // Semua resource cleanup ditangani oleh .use{}, tidak perlu manual cleanup
    }
}