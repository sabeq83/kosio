# Instruksi Eksekusi untuk Agent Antigravity — Kosio MVP

## Peran dan tujuan

Anda adalah Agent Antigravity yang bertanggung jawab membangun MVP Android **Kosio**, aplikasi offline bilingual untuk menghitung food cost/Harga Pokok Produksi, harga jual, laba, margin, markup, diskon, dan biaya marketplace bagi usaha kuliner kecil.

Kerjakan produk sampai menjadi aplikasi Android yang dapat dibangun, diuji, dan dipakai menyelesaikan alur utama secara offline. Jangan berhenti pada scaffold, placeholder, mock UI, atau sebagian kecil fitur. Implementasi harus mengikuti PRD, mempunyai penyimpanan persisten, memakai rumus yang benar, serta memiliki automated tests untuk perhitungan kritis.

## Sumber kebenaran

Baca seluruh berkas berikut sebelum mengubah atau membuat kode:

1. `KOSIO_PRD.md` — sumber kebenaran untuk scope, perilaku, rumus, model data, acceptance criteria, dan definition of done.
2. `kosio-mockup-primary-navigation.png` — referensi visual Beranda, Produk, Bahan, dan Pengaturan.
3. `kosio-mockup-costing-flow.png` — referensi visual Buat Produk, Hasil HPP, Simulasi Harga, serta Backup & Restore.

Jika dokumen dan mockup berbeda:

- PRD menentukan fungsi, data, rumus, validasi, dan scope.
- Mockup menentukan arah visual, hierarki, warna, spacing, dan gaya komponen.
- Jangan menyalin angka contoh dari mockup sebagai data produksi permanen.
- Jangan mengarang requirement baru hanya untuk mengisi bagian visual.

## Keputusan produk yang tidak boleh diubah

- Nama aplikasi: **Kosio**.
- Platform awal: Android.
- Pasar primer: Indonesia.
- Bahasa rilis: Bahasa Indonesia dan English.
- Mode bahasa: Ikuti perangkat, Bahasa Indonesia, English.
- Fallback language: English.
- Aplikasi bekerja offline tanpa akun dan tanpa backend.
- Data bahan, produk, harga, formula, dan kalkulasi disimpan lokal.
- Bahasa, mata uang, dan data usaha merupakan pengaturan yang terpisah.
- IDR dan USD tersedia pada MVP.
- Metric units dan count units termasuk MVP; imperial units ditunda.
- Tidak ada OCR, barcode, AI, cloud sync, inventory penuh, transaksi penjualan, invoice, atau integrasi marketplace pada MVP.
- Tidak ada iklan interstitial.
- Jangan menambahkan subscription pada MVP.

## Stack teknis

Gunakan stack berikut kecuali repository yang diberikan sudah mempunyai pilihan modern yang setara dan menggantinya akan menimbulkan risiko tidak perlu:

- Kotlin.
- Jetpack Compose.
- Material 3.
- Single-activity architecture.
- Navigation Compose.
- ViewModel dan unidirectional state flow.
- Kotlin Coroutines dan Flow.
- Room untuk database lokal.
- DataStore untuk preferensi sederhana.
- Storage Access Framework untuk backup dan restore.
- Android per-app language APIs melalui AndroidX/AppCompat untuk kompatibilitas lintas versi.
- Gradle version catalog untuk dependency versions bila project baru.
- JUnit untuk unit tests.
- Instrumented/UI tests untuk alur kritis yang sesuai.

Semua dependency harus menggunakan versi eksplisit. Jangan menggunakan versi dinamis seperti `1.+` atau `latest.release`.

## Prinsip arsitektur

Gunakan struktur modular yang proporsional untuk MVP. Jangan membuat microservice, backend, atau framework internal berlebihan.

Pemisahan minimum:

```text
app/
  ui/
    navigation/
    components/
    screens/
    theme/
  domain/
    model/
    calculation/
    repository/
    validation/
  data/
    local/
      entity/
      dao/
      migration/
    repository/
    backup/
  localization/
```

Rumus costing harus berada dalam pure Kotlin domain code dan tidak bergantung pada Compose, Android Context, Room, atau ViewModel. Dengan demikian semua rumus dapat diuji secara deterministik.

UI tidak boleh menghitung HPP sendiri. UI hanya mengirim input ke domain layer dan menampilkan hasil.

## Presisi uang dan angka

Jangan menggunakan `Float` atau `Double` sebagai sumber kebenaran untuk uang.

Pilih salah satu pendekatan berikut dan gunakan konsisten:

- `BigDecimal` dengan aturan scale dan rounding eksplisit; atau
- integer minor/base units dengan metadata currency yang benar.

IDR tidak mempunyai pecahan yang lazim ditampilkan. USD umumnya mempunyai dua digit pecahan. Jangan berasumsi semua currency mempunyai dua digit pecahan.

Kuantitas bahan boleh menggunakan `BigDecimal`. Pembulatan hanya dilakukan saat tampilan atau ketika pengguna secara eksplisit memilih aturan pembulatan harga. Kalkulasi internal mempertahankan presisi.

## Lokalisasi bilingual

Semua user-facing text harus berasal dari string resources. Dilarang menulis teks UI langsung di Kotlin.

Gunakan:

```text
res/values/strings.xml      → English fallback
res/values-in/strings.xml   → Bahasa Indonesia
```

Persyaratan:

- Terjemahkan judul, tombol, hint, error, empty state, dialog, snackbar, notifikasi lokal, accessibility label, bantuan, dan teks share bawaan.
- Gunakan glossary pada PRD agar istilah margin, markup, yield, waste, overhead, serta unit cost konsisten.
- Nama brand `Kosio` tidak diterjemahkan.
- Nama bahan, produk, kategori custom, dan catatan pengguna tidak diterjemahkan.
- Database menyimpan stable codes seperti `PER_BATCH`, `WEIGHT`, dan `IDR`, bukan label hasil terjemahan.
- Pergantian bahasa tidak boleh mengubah data atau mata uang.
- Pilihan bahasa harus bertahan setelah restart.
- Ikuti mekanisme per-app language Android; jangan membuat pembungkus Context locale buatan sendiri jika API resmi mencukupi.
- Form state yang belum disimpan tidak boleh hilang saat activity direcreate karena pergantian bahasa.
- Jangan membentuk kalimat melalui concatenation yang mengasumsikan urutan kata satu bahasa.

## Design system

Ambil arah visual dari mockup:

- Primary: deep indigo.
- Success: teal.
- Background: warm ivory/off-white.
- Surface: white.
- Text: charcoal/dark navy.
- Warning: amber, hanya untuk perhatian atau risiko.
- Card radius sekitar 16 dp.
- Screen margin 16 dp.
- Spacing menggunakan kelipatan 4 dp.
- Touch target minimum 48 dp.
- Gunakan typography Material 3 yang jelas dan tidak terlalu dekoratif.
- Sediakan light theme. Dark theme boleh ditunda bila menghambat penyelesaian MVP, kecuali PRD atau project awal mewajibkannya.

Logo sementara dapat berupa wordmark `Kosio` dengan abstract K berbasis measurement bars. Jangan membuat logo chef hat, koin beterbangan, atau visual akuntansi klise. Logo final bukan blocker fungsi MVP.

Mockup adalah referensi, bukan pixel-perfect contract. Prioritaskan keterbacaan, accessibility, state kosong, error, keyboard handling, dan perangkat kecil.

## Navigasi

Bottom navigation utama:

1. Beranda / Overview.
2. Produk / Products.
3. Bahan / Ingredients.
4. Pengaturan / Settings.

Secondary screens menggunakan top app bar dengan back navigation dan umumnya tidak menampilkan bottom navigation:

- Form bahan.
- Detail bahan.
- Form produk.
- Hasil HPP.
- Simulasi harga.
- Riwayat.
- Share preview.
- Backup & Restore.

State tab dan posisi scroll yang wajar harus dipertahankan saat berpindah tab.

## Fitur yang wajib selesai

### 1. Onboarding

- Penjelasan singkat manfaat Kosio.
- Penjelasan bahwa data disimpan lokal.
- Pilihan bahasa atau Follow device.
- Default currency berdasarkan locale, dengan pilihan pengguna.
- Tidak membutuhkan login.
- Onboarding hanya muncul sampai diselesaikan, tetapi bahasa dan currency dapat diubah kembali di Settings.

### 2. Beranda

- Shortcut Tambah Bahan dan Buat Produk/Hitung HPP.
- Jumlah produk dan bahan nyata dari database.
- Kalkulasi terbaru.
- Produk yang membutuhkan perhatian berdasarkan kondisi yang benar-benar tersedia, bukan data palsu.
- Jangan menampilkan omzet atau laba aktual karena aplikasi tidak mencatat transaksi penjualan.

### 3. Master bahan

- Daftar, search, tambah, edit, archive, detail.
- Harga pembelian, jumlah pembelian, unit, dan normalized base-unit cost.
- Weight, volume, dan count units sesuai PRD.
- Custom package conversion untuk pak/dus/tray ke jumlah unit.
- Waste percent atau usable quantity.
- Riwayat harga.
- Daftar produk yang memakai bahan.
- Bahan yang masih dipakai tidak boleh dihapus permanen.

### 4. Produk dan formula

- Daftar, search, tambah, edit, duplicate, archive.
- Nama, kategori opsional, foto opsional, yield, dan yield label.
- Banyak bahan per formula.
- Validasi kompatibilitas unit.
- Kemasan dan overhead.
- Draft formula boleh disimpan, tetapi kalkulasi final diblokir bila input wajib belum lengkap.

### 5. Perhitungan HPP

- Total biaya bahan.
- Effective unit price setelah waste.
- Kemasan.
- Overhead per batch, per unit, dan percentage of material cost.
- HPP/production cost per batch.
- HPP/unit cost per unit.
- Breakdown yang transparan.
- Peringatan untuk bahan tanpa harga.

Implementasikan persis sesuai rumus PRD. Jangan mengganti definisi margin dengan markup.

### 6. Simulasi harga

- Harga katalog/jual.
- Diskon persen atau nominal.
- Marketplace percentage fee.
- Fixed transaction fee.
- Biaya tambahan dan subsidi ongkir opsional.
- Net revenue.
- Profit per unit.
- Profit margin.
- Markup.
- Harga untuk target margin.
- Peringatan rugi ketika pendapatan bersih berada di bawah HPP.
- Skenario dapat disimpan tanpa mengubah formula.

Preset tarif marketplace, jika dibuat, hanya contoh editable dan harus dilabeli agar pengguna memeriksa tarif terbaru. Jangan mengklaim tarif selalu terbaru.

### 7. Riwayat

- Calculation snapshot menyimpan harga bahan dan breakdown pada waktu kalkulasi.
- Snapshot lama tidak berubah ketika master price diperbarui.
- Pengguna dapat melihat dan membandingkan hasil lama.

### 8. Share

- Preview sebelum share.
- Pilihan ringkas atau rinci.
- Share teks melalui Android share sheet.
- Share image summary bila dapat diselesaikan dengan stabil.
- Jangan mengirim otomatis kepada penerima.

### 9. Backup dan restore

- Export seluruh database ke satu file versioned melalui system file picker.
- File memuat schema version dan timestamp.
- Restore memvalidasi file sebelum mengubah database.
- Tampilkan ringkasan isi dan minta konfirmasi.
- Restore gagal tidak boleh merusak database aktif.
- Buat safety backup sebelum replacement bila praktis.
- Backup bebas bahasa dan dapat dipulihkan lintas locale.
- Jelaskan bahwa file memuat data biaya usaha dan perlu disimpan aman.

### 10. Pengaturan

- Bahasa.
- Mata uang.
- Aturan pembulatan tampilan/harga sesuai PRD.
- Backup & Restore.
- Tema jika tersedia.
- Reset data dengan konfirmasi.
- Tentang Kosio.
- Privacy policy.

## Rumus wajib

Implementasikan rumus dari PRD sebagai fungsi domain terpisah. Minimum:

```text
yieldFactor = 1 - wastePercent / 100
effectiveUnitPrice = purchaseUnitPrice / yieldFactor

ingredientItemCost = quantityUsedInBaseUnit × effectiveUnitPrice

totalIngredientCost = sum(ingredientItemCost)

totalOverhead = overheadPerBatch
              + overheadPerUnit × productionYield
              + percentageOverhead × totalIngredientCost

batchCost = totalIngredientCost
          + additionalPackagingCost
          + totalOverhead

unitCost = batchCost / productionYield

profitPerUnit = netSellingRevenue - unitCost
marginPercent = profitPerUnit / netSellingRevenue × 100
markupPercent = profitPerUnit / unitCost × 100
priceForTargetMargin = unitCost / (1 - targetMarginPercent / 100)
```

Marketplace simulation:

```text
priceAfterDiscount = listPrice - discountAmount
percentageFee = priceAfterDiscount × marketplaceFeePercent / 100
netRevenue = priceAfterDiscount
           - percentageFee
           - fixedTransactionFee
           - additionalSellingCost
           - sellerShippingSubsidy
```

Validasi denominator nol, nilai negatif, target margin >= 100%, waste >= 100%, yield <= 0, dan unit tidak kompatibel.

## Database dan migrasi

Implementasikan model minimum sesuai PRD:

- Ingredient.
- IngredientPrice.
- Product.
- ProductIngredient.
- Overhead.
- CalculationSnapshot.
- AppSettings melalui DataStore bila sesuai.

Tambahkan relation/query yang dibutuhkan untuk:

- Produk beserta formula.
- Produk yang memakai bahan tertentu.
- Latest active ingredient price.
- Price history.
- Calculation history.

Gunakan UUID lokal atau ID stabil yang aman untuk backup/restore. Foreign key dan transaksi database harus diaktifkan. Archive lebih disukai daripada hard delete untuk entity yang sudah direferensikan.

Setiap perubahan schema Room harus mempunyai migration. Jangan mengandalkan destructive migration untuk release build.

## State dan kondisi UI

Setiap layar data harus menangani:

- Loading lokal singkat bila diperlukan.
- Empty state dengan CTA relevan.
- Validation error per field.
- Database/restore failure yang dapat dipahami.
- Archived/missing reference.
- Bahan tanpa harga.
- Angka yang sangat besar.
- Foto yang hilang.
- Keyboard, rotation/configuration recreation, serta font scale besar.

Jangan memakai toast untuk error penting. Gunakan inline validation, snackbar, atau dialog sesuai dampaknya.

## Testing wajib

Sebelum menyatakan selesai, buat automated tests untuk sedikitnya kasus berikut:

1. Tepung Rp14.000/kg, penggunaan 250 g = Rp3.500.
2. Minyak Rp18.000/liter, penggunaan 100 ml = Rp1.800.
3. Pak cup Rp25.000 isi 50 = Rp500/pcs.
4. Ayam Rp40.000/kg dengan waste 20% = Rp50/g usable.
5. Batch cost Rp100.000 dan yield 20 = Rp5.000/unit.
6. Unit cost Rp5.000 dan net selling price Rp10.000 = profit Rp5.000, margin 50%, markup 100%.
7. Target margin 40% dan unit cost Rp6.000 = suggested price Rp10.000 sebelum rounding.
8. Harga Rp20.000, diskon 10%, marketplace fee 20% = Rp14.400 sebelum fixed/additional fees.
9. Snapshot lama tidak berubah setelah ingredient price update.
10. Weight tidak dapat dikonversi ke volume tanpa conversion factor.
11. Waste 100% ditolak.
12. Yield nol ditolak.
13. Target margin 100% ditolak.
14. Restore invalid file tidak mengubah data aktif.
15. Backup Indonesia dapat direstore pada English locale.
16. Mengganti bahasa tidak mengubah formula atau currency.
17. English + IDR dan Indonesia + USD menghasilkan nilai matematis identik.
18. Share output memakai nilai yang sama dengan hasil di layar.

Tambahkan tests lain yang diperlukan berdasarkan implementasi aktual.

## Urutan pengerjaan

Kerjakan dalam vertical slices yang dapat diverifikasi:

### Milestone 1 — Fondasi

- Project build berhasil.
- Theme dan navigation.
- Localization Indonesia/English.
- Currency/number model.
- Room schema dasar.
- Calculation engine dengan unit tests.

### Milestone 2 — Bahan

- CRUD/archive/search bahan.
- Harga, unit conversion, waste, history.
- Test database dan domain terkait.

### Milestone 3 — Produk dan HPP

- CRUD/archive/duplicate produk.
- Formula, yield, packaging, overhead.
- Hasil dan breakdown HPP.

### Milestone 4 — Pricing

- Harga jual, margin, markup.
- Diskon dan marketplace simulation.
- Saved scenarios/snapshots.

### Milestone 5 — Portabilitas

- Share.
- Backup/restore.
- Settings dan reset data.

### Milestone 6 — QA dan polish

- Seluruh acceptance criteria PRD.
- Kedua bahasa.
- Mode pesawat.
- Small screen dan large font.
- Database migrations.
- Release build.

Setiap milestone harus menghasilkan build yang berjalan. Jangan membuat seluruh UI dahulu lalu menunda model data dan kalkulasi sampai akhir.

## Seed/demo data

Demo data boleh tersedia hanya pada debug/development build untuk mempercepat preview. Release build harus memulai dari kondisi kosong yang jelas dan tidak boleh menampilkan angka usaha fiktif seolah milik pengguna.

Jika menggunakan preview data Compose, pisahkan dari production database.

## Keamanan dan privasi

- Jangan meminta permission yang tidak diperlukan.
- Gunakan system photo picker untuk gambar produk bila foto diimplementasikan.
- Gunakan system file picker untuk backup/restore.
- Jangan mengunggah data usaha.
- Jangan mencatat nama produk, bahan, harga, atau formula ke analytics/crash logs.
- Jangan menyimpan backup di lokasi publik tanpa tindakan eksplisit pengguna.
- Jangan memasukkan API key, service account, atau secret karena MVP tidak membutuhkan backend.

## Batas scope yang harus dijaga

Jangan menambahkan fitur berikut tanpa persetujuan eksplisit:

- Backend atau server.
- Login.
- Cloud sync.
- Kolaborasi tim.
- OCR atau AI.
- Barcode.
- Live market prices.
- Inventory movement.
- POS atau transaksi penjualan.
- Invoice.
- Integrasi marketplace.
- Pajak per negara.
- Imperial units.
- iOS atau web app.
- Subscription.

Jika menemukan ide fitur tambahan, catat dalam `FUTURE_IDEAS.md`; jangan implementasikan dalam MVP.

## Kualitas kode

- Hindari giant Activity, giant ViewModel, dan god repository.
- Jangan menduplikasi rumus antara layar.
- Gunakan nama domain yang jelas dan tidak bergantung pada satu bahasa UI.
- Tambahkan komentar hanya ketika alasan atau constraint tidak jelas dari kode.
- Pastikan error handling eksplisit.
- Jangan meninggalkan TODO pada alur wajib.
- Jangan menonaktifkan lint/test hanya untuk mendapatkan build hijau.
- Format dan lint kode sebelum handoff.

## Verifikasi akhir

Sebelum menyatakan tugas selesai:

1. Jalankan unit tests.
2. Jalankan instrumented/UI tests yang tersedia.
3. Jalankan lint.
4. Buat debug APK.
5. Buat release build atau release-like build tanpa secret signing bila signing key belum tersedia.
6. Uji mode pesawat.
7. Uji Bahasa Indonesia dan English.
8. Uji IDR dan USD secara silang dengan bahasa.
9. Uji perubahan konfigurasi dan font scale besar.
10. Uji backup pada instalasi berisi data dan restore pada instalasi bersih.
11. Bandingkan UI utama dengan kedua mockup.
12. Audit seluruh acceptance criteria dan Definition of Done pada PRD.

## Format laporan akhir

Laporan akhir harus jujur dan berbasis bukti. Sertakan:

- Ringkasan hasil.
- Fitur yang benar-benar selesai.
- Fitur yang belum selesai atau sengaja ditunda.
- Keputusan teknis penting.
- Struktur project utama.
- Daftar migration database.
- Daftar test dan hasilnya.
- Perintah build yang berhasil.
- Lokasi APK/artifact.
- Screenshot layar utama dalam kedua bahasa.
- Known issues.
- Langkah yang masih membutuhkan keputusan pengguna, seperti signing key, Play Console, privacy-policy URL, dan harga pembelian Pro.

Jangan mengklaim fitur selesai jika hanya berupa placeholder atau belum tersambung ke database/rules sebenarnya.

## Definisi keberhasilan

Pekerjaan berhasil bila pengguna baru dapat, tanpa internet dan tanpa akun:

1. Memilih Bahasa Indonesia atau English.
2. Menambahkan bahan beserta harga dan satuannya.
3. Membuat produk dengan formula, yield, kemasan, dan overhead.
4. Mendapat HPP/unit cost yang benar dan transparan.
5. Mensimulasikan harga jual, diskon, serta marketplace fee.
6. Menyimpan dan membuka calculation history.
7. Memperbarui harga bahan tanpa mengubah snapshot lama.
8. Membagikan hasil.
9. Membuat backup dan memulihkannya dengan aman.
10. Mengganti bahasa tanpa kehilangan atau mengubah data usaha.

Prioritaskan produk yang benar, sederhana, dan dapat dipercaya. Jangan memperluas scope sebelum alur tersebut selesai serta teruji.
