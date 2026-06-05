Markdown# 💰 ManeyR

> **Aplikasi Pencatat Keuangan Pintar dengan Penyimpanan Lokal & Integrasi AI**

ManeyR adalah aplikasi Android yang dirancang untuk membantu Anda mengelola, mencatat, dan melacak arus kas (pemasukan dan pengeluaran) harian Anda dengan mudah. Dibangun dengan **Kotlin** dan dilengkapi dengan **Penyimpanan Lokal**, aplikasi ini memastikan data keuangan Anda tetap aman di perangkat Anda sendiri. ManeyR juga mengintegrasikan kecerdasan buatan melalui **Google Gemini API** untuk pengalaman pencatatan yang lebih interaktif dan cerdas.

---

## ✨ Fitur Utama

- 📊 **Pencatatan Transaksi Cepat**: Tambah data pemasukan dan pengeluaran hanya dalam beberapa ketukan.
- 💾 **Penyimpanan Lokal (Local Storage)**: Semua data keuangan Anda aman tersimpan langsung di dalam memori internal perangkat, memastikan privasi penuh dan akses secepat kilat tanpa perlu koneksi internet untuk fungsi dasarnya.
- 🤖 **Kecerdasan Buatan (Gemini AI)**: Terintegrasi dengan Google Gemini API yang siap membantu Anda menganalisis, memberikan wawasan (insights), atau mengkategorikan kebiasaan finansial Anda.
- 📱 **UI/UX Intuitif**: Desain antarmuka yang bersih dan mudah dipahami oleh semua kalangan pengguna.
- ⚡ **100% Kotlin**: Dibangun sepenuhnya menggunakan bahasa pemrograman Kotlin yang modern, aman, dan responsif.

---

## 🛠️ Tech Stack

- **Platform:** Android
- **Bahasa Pemrograman:** [Kotlin](https://kotlinlang.org/)
- **AI Integration:** Google Gemini API (via Google AI Studio)
- **Tools:** Android Studio, Gradle

---

## 🚀 Cara Menjalankan Secara Lokal (Getting Started)

Ingin mencoba menjalankan atau mengembangkan ManeyR di perangkat/emulator Anda? Ikuti langkah-langkah mudah berikut:

### Persyaratan Sistem
* [Android Studio](https://developer.android.com/studio) versi terbaru.
* Akun Google untuk mendapatkan [Gemini API Key](https://aistudio.google.com/app/apikey).

### Langkah Instalasi

1. **Clone repositori ini**
   ```bash
   git clone [https://github.com/WILDDG/ManeyR.git](https://github.com/WILDDG/ManeyR.git)
Buka di Android StudioBuka Android Studio, pilih Open, dan arahkan ke folder proyek ManeyR yang baru saja Anda clone. 
Biarkan Android Studio melakukan sinkronisasi Gradle dan memperbaiki inkompatibilitas awal (jika ada).
Konfigurasi API KeyBuat sebuah file baru bernama .env di direktori root proyek.
Masukkan Gemini API key Anda ke dalam file tersebut (Anda bisa merujuk pada file .env.example):Code snippetGEMINI_API_KEY=masukkan_api_key_anda_disini
Penyesuaian Konfigurasi BuildBuka file build.gradle.kts pada level app.
Cari dan hapus baris kode berikut agar aplikasi dapat dijalankan dengan lancar dalam mode debug lokal Anda:KotlinsigningConfig = signingConfigs.getByName("debugConfig")
Jalankan AplikasiTekan tombol Run (Shift + F10) pada emulator Android atau perangkat fisik yang sudah terhubung
