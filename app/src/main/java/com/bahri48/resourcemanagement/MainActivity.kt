// Mendeklarasikan package yang sama untuk semua file dalam modul ini
package com.bahri48.resourcemanagement

// Mengimpor ResourceManager untuk demonstrasi resource management dengan .use{}
// Class ini menunjukkan cara mengelola file resource dengan automatic cleanup
import ResourceManager
// Mengimpor ThreadResourceManager untuk demonstrasi coroutine dispatchers
// Class ini menunjukkan cara menjalankan heavy task di background thread
import ThreadResourceManager
// Mengimpor Bundle untuk menyimpan state Activity saat configuration changes
import android.os.Bundle
// Mengimpor Button untuk widget button yang ada di layout
import android.widget.Button
// Mengimpor TextView untuk menampilkan text hasil demonstrasi
import android.widget.TextView
// Mengimpor enableEdgeToEdge untuk mengaktifkan edge-to-edge display (modern Android UI)
import androidx.activity.enableEdgeToEdge
// Mengimpor AppCompatActivity sebagai base class untuk Activity
import androidx.appcompat.app.AppCompatActivity
// Mengimpor lifecycleScope untuk menjalankan coroutine yang terikat dengan lifecycle Activity
import androidx.lifecycle.lifecycleScope
// Mengimpor launch untuk memulai coroutine di dalam lifecycleScope
import kotlinx.coroutines.launch

// Class MainActivity adalah screen utama yang menampilkan 3 demonstrasi resource management
// Activity ini ditampilkan setelah user berhasil login
class MainActivity : AppCompatActivity() {
    // Deklarasi variabel TextView untuk menampilkan hasil demonstrasi
    // Menggunakan nullable type (TextView?) karena akan diinisialisasi nanti dengan findViewById
    // ? memungkinkan variabel bernilai null sebelum initialization
    
    // TextView untuk menampilkan status hasil WeakReference task
    private var tvResWeak: TextView? = null // deklarasi variabel tvResWeak
    
    // TextView untuk menampilkan hasil Thread/Dispatcher task
    private var tvResThread: TextView? = null // deklarasi variabel tvResThread
    
    // TextView untuk menampilkan hasil Resource management task
    private var tvResResource: TextView? = null // deklarasi variabel tvResResource
    
    // Deklarasi variabel Button untuk trigger setiap demonstrasi
    // Menggunakan lateinit karena pasti diinisialisasi di onCreate() sebelum digunakan
    
    // Button untuk menjalankan WeakReference task (memory leak prevention)
    private lateinit var btnWeakRef: Button // deklarasi variabel btnWeakRef
    
    // Button untuk menjalankan Thread Dispatcher task (background threading)
    // Catatan: ID XML-nya adalah btn_thread_dispatchers, tapi variabelnya btnResResource
    private lateinit var btnResResource: Button // deklarasi variabel btnResResource
    
    // Button untuk menjalankan Resource .use{} task (automatic resource cleanup)
    // Catatan: ID XML-nya adalah btn_resource, tapi variabelnya btnResThread
    private lateinit var btnResThread: Button // deklarasi variabel btnResThread

    // onCreate dipanggil saat Activity pertama kali dibuat
    // Lifecycle method Android untuk setup awal Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        // Memanggil onCreate parent class (wajib untuk Android lifecycle)
        super.onCreate(savedInstanceState)
        
        // Mengaktifkan edge-to-edge display (fitur Android modern)
        // Membuat konten aplikasi extends ke bawah system bars (status bar & navigation bar)
        // Ini memberikan tampilan yang lebih immersive dan modern
        enableEdgeToEdge()
        
        // Menginflasi layout XML activity_main.xml dan menampilkannya di Activity
        setContentView(R.layout.activity_main)

        // === INISIALISASI UI ELEMENTS DARI XML LAYOUT ===
        // Menghubungkan variabel Kotlin dengan TextView dari XML menggunakan findViewById
        // R.id.tv_status adalah reference ke TextView dengan id "tv_status" di XML
        tvResWeak = findViewById(R.id.tv_status)
        // Menghubungkan TextView untuk resource task result
        tvResResource = findViewById(R.id.tv_res_resource)
        // Menghubungkan TextView untuk thread task result
        tvResThread = findViewById(R.id.tv_res_thread)

        // Menghubungkan variabel Button dari XML layout
        // btn_weak_ref adalah Button untuk trigger WeakReference demonstration
        btnWeakRef = findViewById(R.id.btn_weak_ref)
        // btn_thread_dispatchers adalah Button untuk Thread/Dispatcher demonstration
        // Catatan: nama ID di XML adalah "btn_thread_dispatchers" (mungkin tertukar dengan resource)
        btnResResource = findViewById(R.id.btn_thread_dispatchers)
        // btn_resource adalah Button untuk Resource .use{} demonstration
        // Catatan: nama ID di XML adalah "btn_resource" (mungkin tertukar dengan thread)
        btnResThread = findViewById(R.id.btn_resource)

        // === SETUP CLICK LISTENERS UNTUK TIGA BUTTON ===
        
        // Button untuk menjalankan WeakReference task (Mencegah Memory Leak)
        // Saat button diklik, panggil method runWeakReferenceTask()
        btnWeakRef.setOnClickListener {
            runWeakReferenceTask()
        }

        // Button untuk menjalankan Dispatchers task (Thread Management)
        // Saat button diklik, panggil method runThreadTask()
        // Method ini akan menjalankan heavy task di background thread menggunakan Coroutine
        btnResResource.setOnClickListener {
            runThreadTask()
        }

        // Button untuk menjalankan .use{} task (Resource Management)
        // Saat button diklik, panggil method runResourceTask()
        // Method ini akan menulis dan membaca file dengan automatic cleanup menggunakan .use{}
        btnResThread.setOnClickListener {
            runResourceTask()
        }
    }

    // Method private untuk menjalankan demonstrasi WeakReference
    // WeakReference digunakan untuk mencegah memory leak saat background task berjalan
    // sambil Activity masih direferensikan
    private fun runWeakReferenceTask() {
        // Menggunakan WeakReference agar Activity bisa di-GC jika dihancurkan
        // Mengupdate TextView untuk memberi tahu user bahwa task sedang berjalan
        // Pesan meminta user untuk menekan BACK untuk menguji memory leak prevention
        tvResWeak?.text = "Memulai simulasi tugas 5 detik (Coba tekan BACK sekarang)..."

        // Membuat instance MemorySafeTask dan mengirim 'this' (Activity ini) sebagai parameter
        // MemorySafeTask akan membungkus Activity ini dalam WeakReference
        val task = MemorySafeTask(this)

        // Menjalankan tugas di background thread
        // executeTask() akan memulai thread baru dan sleep 5 detik
        // Jika Activity dihancurkan sebelum 5 detik, WeakReference mencegah memory leak
        task.executeTask()
    }

    // Method private untuk menjalankan demonstrasi Thread Management menggunakan Coroutine
    // Coroutine adalah cara modern Android menangani asynchronous programming
    private fun runThreadTask() {
        // Cek apakah tvResThread sudah ada nilainya (tidak null)
        // Membuat instance ThreadResourceManager dengan context Activity ini
        // ThreadResourceManager berisi suspend function yang berjalan di Dispatchers.IO
        val threadManager = ThreadResourceManager(this)
        
        // lifecycleScope.launch memulai coroutine yang terikat dengan lifecycle Activity
        // lifecycleScope otomatis cancel saat Activity dihancurkan (mencegah memory leak)
        // launch adalah non-blocking - tidak menghentikan main thread
        lifecycleScope.launch {
            // Gunakan safe call ?. untuk update text hanya jika tvResThread tidak null
            // Mengupdate UI untuk memberi tahu user bahwa proses sedang berjalan
            tvResThread?.text = "Sedang memproses di background..."
            
            // Try-catch block untuk menangani kemungkinan exception saat heavy task
            try {
                // Memanggil suspend function performHeavyTask dengan parameter data
                // Suspend function bisa di-pause dan di-resume tanpa blocking thread
                // performHeavyTask akan:
                // 1. Switch ke Dispatchers.IO (thread pool untuk I/O operations)
                // 2. Menulis ke file (I/O operation)
                // 3. Delay 2 detik (simulasi proses berat)
                // 4. Membaca file kembali (I/O operation)
                // 5. Return ke main thread dan berikan result
                val result = threadManager.performHeavyTask("Data dari Dispatcher")
                
                // Mengupdate TextView dengan hasil dari heavy task
                // String interpolation $result memasukkan variabel ke dalam string
                tvResThread?.text = "Hasil Thread: $result"
            } catch (e: Exception) {
                // Jika terjadi error (file permission denied, disk full, dll)
                // Tangkap exception dan tampilkan error message ke user
                tvResThread?.text = "Error: ${e.message}"
            }
        }
    }

    // Method private untuk menjalankan demonstrasi Resource Management dengan .use{}
    // .use{} adalah Kotlin extension function yang menjamin resource cleanup
    // (equivalent dengan Java try-with-resources)
    private fun runResourceTask() {
        // Membuat instance ResourceManager dengan context Activity ini
        // ResourceManager berisi method yang menggunakan .use{} untuk otomatis menutup stream
        val resManager = ResourceManager(this)
        
        // Try-catch block untuk menangani kemungkinan exception saat file operations
        try {
            // Menjalankan fungsi manageFileResource yang menggunakan .use{} secara internal
            // Method ini akan:
            // 1. Membuat file "test.txt" di internal storage
            // 2. Menulis "Isi File Resource" ke file menggunakan .use{} (auto-close)
            // 3. Membaca kembali isi file menggunakan .use{} (auto-close)
            // 4. Return isi file sebagai String
            val result = resManager.manageFileResource("test.txt",
                "Isi File Resource")
            
            // Mengupdate TextView dengan hasil pembacaan file
            // Seharusnya menampilkan "Hasil Resource: Isi File Resource"
            tvResResource?.text = "Hasil Resource: $result"
        } catch (e: Exception) {
            // Jika terjadi error (file permission denied, disk full, dll)
            // Tangkap exception dan tampilkan error message ke user
            tvResResource?.text = "Error Resource: ${e.message}"
        }
    }

    // Fungsi tambahan jika digunakan oleh class luar
    // Method public ini dipanggil oleh MemorySafeTask untuk update UI dari background thread
    // MemorySafeTask memanggil method ini via activity.runOnUiThread { activity.updateUI(...) }
    fun updateUI(message: String) {
        // Mengupdate text pada TextView tvResWeak dengan pesan dari MemorySafeTask
        // Safe call operator ?. memastikan tidak crash jika tvResWeak masih null
        tvResWeak?.text = message
    }
}
