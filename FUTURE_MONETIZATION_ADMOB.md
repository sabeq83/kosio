# 💰 Usulan Strategi Monetisasi & Integrasi Google AdMob — Kosio

Dokumen ini memuat analisis penempatan iklan Google AdMob yang **optimal untuk pendapatan (*High eCPM*)**, namun **tetap menjaga kenyamanan pengguna UMKM (*User-Friendly & Non-Intrusive UX*)**.

---

## 🎯 4 Format Iklan & Strategi Penempatan di Aplikasi

| Format Iklan AdMob | Lokasi Penempatan di Kosio | Potensi eCPM | Pengalaman Pengguna (UX Impact) |
| :--- | :--- | :--- | :--- |
| **1. Native Advanced Ads** *(Rekomendasi Utama)* | Di antara kartu daftar resep / produk (misal: kartu ke-3) & halaman Ringkasan Bisnis | ⭐⭐⭐⭐ (Tinggi) | **Sangat Nyaman (0% Mengganggu)** — Bentuk visual menyatu sempurna dengan Material 3 card Kosio |
| **2. Rewarded Video Ads** *(Value Exchange)* | Buka Template Resep Viral Pro, Ekspor PDF tanpa watermark, atau Bonus Scan OCR | ⭐⭐⭐⭐⭐ (Tertinggi) | **Sangat Positif** — 100% inisiatif pengguna demi mendapatkan keuntungan gratis |
| **3. Collapsible / Adaptive Banner** | Bagian paling bawah (*sticky bottom*) di tab **Bahan** dan **Histori** | ⭐⭐ (Menengah) | **Nyaman** — Tidak menutupi form ketik kalkulator |
| **4. Interstitial Ads** *(Transisi Alami)* | Hanya muncul setelah user **selesai menyimpan resep baru** (Bukan saat pindah tab) | ⭐⭐⭐⭐ (Tinggi) | **Terkontrol** — Dibatasi frekuensi (*Frequency Capping*: Maks 1x per 10 menit) |

---

## 💡 Detail Strategi Tiap Format

### A. Format 1: Native Advanced Card (Iklan Elegan Menyatu Desain)
- **Tampilan**: Didesain menggunakan gaya card Kosio (warna krem/terakota lembut, badge kecil `IKLAN` di pojok kanan atas, gambar produk, dan tombol call-to-action).
- **Target Kategori Iklan**: Peralatan dapur, kemasan standing pouch, bahan baku F&B, platform pesan-antar online.
- **Keunggulan**: Tidak memotong alur pengguna, terlihat seperti rekomendasi suplier/partner bisnis F&B.

### B. Format 2: Rewarded Video Ads ("Nonton Video = Buka Fitur Pro")
Model ini adalah penghasil pendapatan terbesar (*Highest Revenue Driver*) untuk aplikasi utilitas:
1. **Unlock Recipe Starter Pack**: Pengguna baru dapat membuka paket resep usaha viral (lengkap dengan HPP & simulasi harga jual) gratis hanya dengan menonton video 15–30 detik.
2. **Unlock PDF / Excel Export**: Ekspor laporan HPP profesional berkualitas tinggi tanpa watermark.
3. **Bonus Kuota OCR Scan Struk**: Menambah kuota scan ekstra.

### C. Format 3: Adaptive Bottom Banner
- Diletakkan di footer tab **Bahan** dan **Histori**.
- **Aturan Ketat UX**: **DILARANG** dipasang di layar form aktif (seperti kalkulator HPP atau form input bahan) agar tidak menghalangi keyboard virtual dan mencegah klik tidak sengaja (*accidental clicks* yang dilarang AdMob).

### D. Format 4: Interstitial Pasca-Aksi (Capped)
- Hanya dipicu pada momen *Task Completion* (contoh: Saat user klik *"Simpan Perhitungan HPP"* dan muncul dialog sukses).
- **Frequency Capping**: Maksimal 1 impresi per 10 menit per pengguna aktif.

---

## 🛠️ Langkah Teknis Implementasi (Roadmap)

1. **Pendaftaran AdMob & App-ID**:
   - Mendaftarkan App ID di Google AdMob Console.
   - Menambahkan metadata `<meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" ... />` di `AndroidManifest.xml`.
2. **Dependensi Gradle**:
   - `implementation("com.google.android.gms:play-services-ads:23.3.0")`
3. **Komponen Compose AdMob Terisolasi**:
   - Membuat composable wrapper: `KosioNativeAdCard()`, `KosioAdaptiveBanner()`, `AdMobRewardManager`.
4. **GDPR / UMP Consent & Privasi**:
   - Mengintegrasikan *User Messaging Platform (UMP) SDK* untuk kepatuhan regulasi privasi Google Play.
5. **Mode Test Mode vs Production**:
   - Menggunakan Test Ad Unit IDs selama masa *Development* & *QA* agar akun AdMob tidak dibatasi (*policy violation*).

---

## 📈 Rencana Model Bisnis Jangka Panjang (Hybrid Freemium)
- **Kosio Free (Ad-Supported)**: Gratis 100% dengan banner non-intrusif & Rewarded Ads untuk fitur lanjutan.
- **Kosio Pro (In-App Purchase / One-Time Unlock)**: Opsi bayar sekali seumur hidup (misal Rp 29.000) untuk hapus seluruh iklan dan akses instan semua template resep + PDF export.
