# 📜 Changelog — Kosio

Semua perubahan penting pada proyek **Kosio** akan didokumentasikan di berkas ini.

Format dokumen ini mengacu pada [Keep a Changelog](https://keepachangelog.com/id/1.0.0/) dan mengikuti standar [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]
### Direncanakan (Roadmap)
- 🍰 Template Resep Usaha Viral Bawaan (*Recipe Starter Pack*: Es Krim Gabin, Kopi Gula Aren, Croffle, Dimsum Ayam, Seblak).
- 📖 OCR Scanner Resep dari Majalah / Buku Cetak.
- 📊 Export Laporan PDF & Excel HPP & Rekap Resep.
- ☁️ Backup & Restore Data Lokal via Google Drive (Opsional).

---

## [1.2.0] - 2026-10-11
### Ditambahkan
- **Integrasi Google AdMob & Monetisasi Cerdas**:
  - **Sistem Rewarded Video OCR**: Kuota harian bawaan 3x scan gratis per hari + bonus +3 scan gratis dengan menonton video 15 detik.
  - **Native In-Feed Card**: Iklan bermitra perlengkapan F&B yang menyatu di dalam daftar resep tanpa mengganggu navigasi.
  - **Smart Interstitial**: Iklan pasca-simpan resep dengan batasan interval ketat (*Frequency Capping 10 menit*).
  - 100% bebas *Sticky Banner* agar layar tetap bersih, cepat, dan nyaman.
- **Scan Struk Belanja Otomatis (OCR Kamera)**:
  - Integrasi Google ML Kit Text Recognition (`com.google.mlkit:text-recognition:16.0.1`) 100% on-device & offline.
  - Parser cerdas struk belanja minimarket & supermarket Indonesia (Indomaret, Alfamart, Hypermart, Toko Bahan Kue).
  - Ekstraksi otomatis nama bahan, gramasi/volume/satuan (`kg`, `g`, `l`, `ml`, `pcs`, `pack`, `box`, `sdm`), dan harga beli (`Rp ...`).
  - Layar Review Interaktif ([`ReceiptScanReviewScreen.kt`](file:///app/src/main/java/com/kosio/app/ui/ingredients/ReceiptScanReviewScreen.kt)) untuk verifikasi dan simpan masal ke Master Bahan Baku (Room Database) & riwayat harga (*Price History*).
- **Automated Versioning SOP**:
  - Konfigurasi formula otomatis `versionCode = (MAJOR * 10000) + (MINOR * 100) + PATCH` di [`build.gradle.kts`](file:///app/build.gradle.kts).
  - Panduan resmi [`VERSIONING_AND_RELEASE_SOP.md`](file:///VERSIONING_AND_RELEASE_SOP.md).
- **GitHub Actions CI**:
  - Otomasi pengujian unit test dan build verification pada setiap push / pull request.

### Diperbaiki & Ditingkatkan
- Peningkatan transisi navigasi `KosioNavHost` yang instan dan mulus tanpa kedip (*EnterTransition.None / ExitTransition.None*).
- Perbaikan kata kunci filter struk belanja untuk menghindari terhapusnya bahan kemasan `"CUP PLASTIK"`.

---

## [1.1.0] - 2026-10-10
### Ditambahkan
- **Kalkulator Target Markup (%) Fleksibel**:
  - Dukungan penentuan target margin/markup di atas 100% (misal 150%, 200%, 300% untuk produk margin tinggi seperti minuman & saus).
- **Multi-Channel Pricing Simulation**:
  - Simulasi perbandingan harga jual dan margin bersih lintas saluran penjualan: Dine-in / Offline (0%), GoFood (20%), GrabFood (20%), ShopeeFood (20%).
- **Mode Tab Pricing**:
  - Pilihan mode *"Margin Standar"* vs *"Multi-Channel"* pada halaman Penentuan Harga Jual.

---

## [1.0.0] - 2026-10-10
### Ditambahkan
- **Rilis Awal Kosio MVP**:
  - **Master Bahan Baku**: Kelola harga beli, ukuran kemasan, satuan dasar (`g`, `ml`, `pcs`), konversi otomatis per unit kecil, dan tracking riwayat perubahan harga (*Price History*).
  - **Kalkulator Resep & HPP**: Hitung modal bahan baku, biaya operasional/overhead (listrik, gas, tenaga kerja), biaya kemasan/packaging, dan kalkulasi HPP per porsi / per batch.
  - **Simulasi Harga Jual & Margin**: Rekomendasi harga jual berdasarkan margin laba yang diinginkan (persentase & nominal rupiah).
  - **Histori & Export Ringkasan**: Riwayat perhitungan HPP yang dapat disalin sebagai teks ringkasan untuk dibagikan.
  - **Arsitektur Lokal & Offline**: 100% Room Database SQLite, Jetpack Compose Material 3 Design System bernuansa kuliner hangat, dan dukungan bilingual (Bahasa Indonesia & English).
