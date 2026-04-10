// Mendeklarasikan package yang sama untuk semua file dalam modul ini
package com.bahri48.resourcemanagement

// Mengimpor WeakReference dari java.lang.ref untuk membuat referensi lemah ke objek
// WeakReference memungkinkan Garbage Collector (GC) untuk reclaim objek meskipun masih direferensikan
// Ini berbeda dengan strong reference (variabel biasa) yang mencegah GC dari reclaim objek
import java.lang.ref.WeakReference
// Mengimpor thread dari Kotlin standard library untuk membuat background thread dengan mudah
// kotlin.concurrent.thread adalah utility function yang menyederhanakan pembuatan thread
import kotlin.concurrent.thread

// Class MemorySafeTask mendemonstrasikan penggunaan WeakReference untuk mencegah memory leak
// Memory leak sering terjadi saat background task memegang reference ke Activity yang sudah destroyed
// Class ini menerima MainActivity dan membungkusnya dalam WeakReference
class MemorySafeTask(activity: MainActivity) {
    // 1. Membungkus Activity di dalam WeakReference
    // Ini memungkinkan Garbage Collector (GC) untuk menghapus Activity dari memori
    // jika Activity tersebut ditutup oleh pengguna sebelum tugas ini selesai.
    // 
    // PENJELASAN WEAKREFERENCE:
    // - Strong reference (variabel biasa): mencegah GC dari reclaim objek
    // - Weak reference: TIDAK mencegah GC reclaim objek jika tidak ada strong reference lain
    // - Saat user menekan BACK, Activity tidak ada strong reference lagi
    // - GC bisa reclaim Activity meskipun background thread masih memegang WeakReference
    // - activityReference.get() akan return null jika Activity sudah di-GC
    private val activityReference: WeakReference<MainActivity> = WeakReference(activity)

    // Method executeTask memulai background task yang memory-safe
    // Method ini bisa dipanggil dari MainActivity untuk menjalankan simulasi heavy task
    fun executeTask() {
        // Mensimulasikan tugas berat di background thread
        // thread { ... } membuat dan memulai thread baru secara otomatis
        // Background thread digunakan karena operasi berat tidak boleh berjalan di main/UI thread
        // Jika berjalan di main thread, UI akan freeze dan bisa menyebabkan ANR (Application Not Responding)
        thread {
            // Menulis log ke Logcat bahwa task sudah dimulai
            // println() di Android akan muncul di Logcat, berguna untuk debugging
            println("Memulai tugas di background...")

            // Simulasi proses memakan waktu 5 detik
            // Thread.sleep() memblokir thread saat ini selama 5000 milliseconds (5 detik)
            // Ini mensimulasikan heavy task seperti download, processing, atau network call
            // PENTING: Thread.sleep() di main thread akan freeze UI, makanya dijalankan di background thread
            Thread.sleep(5000)

            // 2. Mengambil strong reference (referensi kuat) dari WeakReference
            // .get() adalah method WeakReference yang mengembalikan objek yang dibungkus
            // Return value: 
            // - MainActivity object jika Activity masih ada (belum di-GC)
            // - null jika Activity sudah di-GC (user menekan BACK dan memory sudah di-reclaim)
            // Hasilnya disimpan ke variabel 'activity' untuk digunakan di bawah
            val activity = activityReference.get()

            // 3. Mengecek apakah Activity masih ada dan belum dihancurkan
            // Kondisi ini menentukan apakah aman untuk mengupdate UI:
            // - activity != null: Activity belum di-GC (masih ada di memori)
            // - !activity.isFinishing: Activity tidak dalam proses dihancurkan
            //   (isFinishing true saat Activity.finish() dipanggil atau user tekan BACK)
            if (activity != null && !activity.isFinishing) {
                // Jika Activity masih aktif, kita aman untuk mengupdate UI
                // PENTING: UI updates HARUS berjalan di main/UI thread, bukan di background thread
                // Jika update UI dari background thread tanpa runOnUiThread, akan crash!
                
                // runOnUiThread { ... } adalah method Activity yang menjalankan lambda di main thread
                // Ini memastikan UI update berjalan di thread yang benar (main/UI thread)
                activity.runOnUiThread {
                    // Memanggil method updateUI di MainActivity untuk mengupdate TextView
                    // updateUI() mengubah text pada tvResWeak dengan pesan success
                    activity.updateUI("Tugas Selesai dan Memori Aman!")
                }
                // Menulis log ke Logcat bahwa task selesai dan UI sudah diupdate
                println("Tugas selesai, UI di-update.")
            } else {
                // Jika masuk ke sini, berarti Activity sudah ditutup sebelum tugas selesai.
                // Ini terjadi saat:
                // - User menekan tombol BACK selama 5 detik task berjalan
                // - Activity.isFinishing == true (Activity sedang dihancurkan)
                // - activity == null (Activity sudah di-GC karena WeakReference)
                //
                // WeakReference berhasil mencegah Memory Leak karena:
                // - Tanpa WeakReference: background task tetap memegang strong reference ke Activity
                //   → Activity tidak bisa di-GC → Memory Leak → Aplikasi crash (OutOfMemoryError)
                // - Dengan WeakReference: GC bisa reclaim Activity meskipun thread masih berjalan
                //   → Memory dibersihkan → Tidak ada leak → Aplikasi tetap stabil
                println("Activity sudah dihancurkan. Mencegah Memory Leak!")
            }
        // Thread akan otomatis terminate setelah selesai menjalankan semua kode di dalam blok
        // Tidak perlu manual stop atau cleanup thread
    }
}