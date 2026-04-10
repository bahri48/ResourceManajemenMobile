// Mengimpor Dispatchers dari Kotlin Coroutines untuk menentukan thread pool yang digunakan
// Dispatchers.IO adalah thread pool yang dioptimalkan untuk I/O operations (file, network, database)
import kotlinx.coroutines.Dispatchers
// Mengimpor withContext untuk berpindah thread/context di dalam coroutine
// withContext memungkinkan kita switch antara Main thread dan Background thread
import kotlinx.coroutines.withContext
// Mengimpor File dari Java standard library untuk operasi file system
import java.io.File

// Class ThreadResourceManager mendemonstrasikan penggunaan Kotlin Coroutines dengan Dispatchers
// untuk mengelola background threading dengan cara yang lebih modern dan mudah
// Class ini menunjukkan cara menjalankan heavy task di background tanpa blocking main thread
class ThreadResourceManager(private val context: android.content.Context) {

    // Fungsi 'suspend' agar bisa dijalankan di dalam Coroutine
    // suspend function adalah fitur utama Kotlin Coroutines yang memungkinkan:
    // - Function bisa di-pause (suspend) dan di-resume tanpa blocking thread
    // - Bisa dipanggil hanya dari dalam coroutine atau suspend function lain
    // - Tampak synchronous tapi sebenarnya asynchronous (tidak blocking thread)
    // 
    // Parameter data: String content yang akan ditulis ke file
    // Return value: String content yang dibaca dari file setelah proses selesai
    suspend fun performHeavyTask(data: String): String {
        // Berpindah ke Dispatchers.IO (Thread khusus Input/Output/Data)
        // 
        // PENJELASAN withContext(Dispatchers.IO):
        // - withContext adalah coroutine builder yang switch execution context
        // - Dispatchers.IO adalah thread pool dengan banyak threads (biasanya 64+ threads)
        // - Dirancang untuk operasi yang banyak menunggu: file I/O, network calls, database queries
        // - Berbeda dengan Dispatchers.Default (untuk CPU-intensive tasks, thread count = CPU cores)
        // - Berbeda dengan Dispatchers.Main (untuk UI updates, hanya 1 thread)
        // 
        // Saat blok withContext selesai:
        // - Execution otomatis return ke caller's context (dalam hal ini: Main/UI thread)
        // - Tidak perlu manual post back ke main thread (seperti handler.post di Java)
        // - Ini membuat kode lebih bersih dan mudah dibaca
        return withContext(Dispatchers.IO) {
            // Menentukan nama file yang akan dibuat di internal storage
            val fileName = "debug_log.txt"
            // Membuat objek File yang menunjuk ke file di internal storage aplikasi
            // context.filesDir adalah direktori: /data/data/<package_name>/files/
            val file = File(context.filesDir, fileName)

            // Menulis ke file (Proses I/O berat)
            // file.outputStream() membuka stream untuk menulis bytes ke file
            // .use { ... } menjamin stream akan ditutup otomatis setelah blok selesai
            // it.write(data.toByteArray()) menulis content string sebagai bytes ke file
            // it adalah implicit parameter untuk outputStream di dalam lambda use{}
            file.outputStream().use { it.write(data.toByteArray()) }

            // Simulasi proses delay 2 detik (misal: proses enkripsi/parsing)
            // 
            // PENJELASAN kotlinx.coroutines.delay vs Thread.sleep:
            // - Thread.sleep(2000): BLOCKING - thread ditahan selama 2 detik (tidak bisa dipakai)
            // - kotlinx.coroutines.delay(2000): NON-BLOCKING - thread dilepas ke thread pool
            //   Selama 2 detik, thread bisa dipakai untuk task lain oleh thread pool
            //   Setelah 2 detik, coroutine di-resume dan lanjutkan execution
            // - delay() hanya bisa dipanggil di dalam suspend function atau coroutine
            // - Ini lebih efisien dan tidak memboroskan resource thread pool
            kotlinx.coroutines.delay(2000)

            // Membaca kembali (Proses I/O berat)
            // file.inputStream() membuka stream untuk membaca bytes dari file
            // .bufferedReader() membungkus InputStream dengan BufferedReader untuk efisiensi
            //   (BufferedReader membaca chunks of data, lebih cepat dari read byte-per-byte)
            // .use { ... } menjamin BufferedReader dan underlying InputStream akan ditutup
            // it.readText() membaca seluruh isi file sebagai String dan mengembalikannya
            // String ini akan menjadi return value dari withContext block, dan akhirnya return ke caller
            file.inputStream().bufferedReader().use { it.readText() }
        } // Setelah blok ini, thread otomatis kembali ke pemanggilnya (Main Thread)
        // withContext selesai: execution kembali ke Main/UI thread
        // Return value (hasil readText) dikembalikan ke caller (MainActivity's lifecycleScope)
        // Caller bisa update UI langsung tanpa perlu runOnUiThread atau handler.post
    }
}
