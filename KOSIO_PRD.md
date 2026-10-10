# PRD — Kosio

**Versi:** 1.1  
**Status:** Draft untuk MVP  
**Platform awal:** Android  
**Penyimpanan:** Lokal/offline  
**Bahasa rilis:** Bahasa Indonesia dan English  
**Pasar primer:** Indonesia  
**Nama listing Indonesia:** Kosio: Kalkulator HPP  
**Nama listing English:** Kosio: Food Cost Calculator  

## 1. Ringkasan produk

Kosio adalah aplikasi Android offline bilingual untuk membantu pelaku usaha makanan menghitung biaya produksi sebenarnya, Harga Pokok Produksi (HPP) atau unit cost, harga jual, laba, dan margin.

Produk ditujukan untuk penjual makanan rumahan, katering, frozen food, bakery, minuman, reseller makanan, dan usaha kuliner skala kecil yang saat ini menghitung biaya menggunakan catatan manual, kalkulator, atau spreadsheet.

Pertanyaan utama yang dijawab aplikasi:

> “Berapa modal sebenarnya dan berapa harga jual yang menghasilkan margin yang saya inginkan?”

Pengguna memasukkan harga pembelian bahan sesuai kondisi mereka sendiri. Developer tidak menyediakan atau memperbarui harga pasar. Ketika harga satu bahan berubah, seluruh produk yang memakai bahan tersebut dapat dihitung ulang menggunakan harga terbaru.

Kosio diluncurkan dengan strategi **bilingual product, Indonesia-first business**. Bahasa Indonesia menjadi bahasa utama pemasaran dan validasi, sementara English tersedia sejak rilis agar fondasi produk siap digunakan dan diuji di pasar lain. Bahasa tampilan, mata uang, format angka, dan data usaha merupakan pengaturan yang terpisah.

## 2. Latar belakang dan masalah

Pelaku usaha kuliner sering menentukan harga jual hanya dari harga bahan utama atau mengikuti harga kompetitor. Biaya kemasan, bahan terbuang, gas, listrik, tenaga kerja, potongan marketplace, dan biaya lain sering tidak dihitung secara lengkap.

Masalah yang ingin diselesaikan:

1. Pengguna tidak mengetahui biaya produksi sebenarnya per porsi.
2. Harga bahan dibeli dalam kg, liter, pak, atau dus, sedangkan resep menggunakan gram, ml, atau pcs.
3. Perubahan harga satu bahan mengharuskan pengguna menghitung ulang banyak produk.
4. Biaya tidak langsung seperti gas, listrik, dan tenaga kerja sering terlewat.
5. Pengguna sering tertukar antara markup dan margin.
6. Diskon dan biaya marketplace dapat membuat penjualan terlihat untung padahal merugi.
7. Catatan perhitungan tersebar di buku, chat, kalkulator, dan spreadsheet.

## 3. Tujuan produk

### 3.1 Tujuan MVP

- Pengguna dapat menghitung HPP satu produk dalam waktu kurang dari lima menit setelah data bahan tersedia.
- Pengguna dapat mengetahui HPP per unit, laba per unit, dan margin dari harga jual.
- Pengguna dapat memperbarui harga bahan sekali dan melihat produk yang terdampak.
- Pengguna dapat membandingkan beberapa skenario harga jual, diskon, dan biaya marketplace.
- Seluruh fungsi inti dapat digunakan tanpa akun dan tanpa koneksi internet.
- Pengguna dapat mencadangkan dan memulihkan data melalui satu file lokal.

### 3.2 Bukan tujuan MVP

- Menjadi aplikasi pembukuan atau akuntansi lengkap.
- Menyediakan harga bahan pasar secara real-time.
- Mengelola kas, utang, piutang, payroll, pajak, atau laporan keuangan formal.
- Mengelola transaksi penjualan dan stok secara penuh.
- Menyinkronkan data antarperangkat.
- Membaca struk menggunakan OCR atau AI.
- Terhubung langsung ke marketplace.
- Memberikan nasihat bisnis atau jaminan keuntungan.

## 4. Target pengguna

### 4.1 Pengguna utama

- Penjual makanan dan minuman rumahan.
- Pemilik usaha bakery atau kue pesanan.
- Penjual frozen food.
- Katering kecil.
- Penjual minuman per cup atau per botol.
- Reseller makanan yang menambah biaya kemasan atau pengiriman lokal.

### 4.2 Persona utama

**Nama:** Rina  
**Profil:** Menjual dessert box dan kue rumahan melalui WhatsApp dan marketplace.  
**Kondisi saat ini:** Harga bahan dicatat di buku; HPP dihitung menggunakan kalkulator.  
**Kesulitan:** Tidak yakin apakah harga promo masih memberikan keuntungan setelah potongan marketplace dan kemasan.  
**Kebutuhan:** Mengubah harga bahan sekali, menghitung biaya per box, dan membandingkan harga jual normal dengan harga promo.

### 4.3 Persona tambahan

**Nama:** Arif  
**Profil:** Menjual kopi susu dan minuman literan.  
**Kebutuhan:** Mengubah pembelian dalam liter atau pak menjadi biaya per ml dan per cup, lalu menambahkan cup, tutup, sedotan, es, dan biaya marketplace.

## 5. Prinsip produk

1. **Cepat:** input menggunakan istilah yang dipahami pedagang, bukan istilah akuntansi yang rumit.
2. **Transparan:** hasil harus memperlihatkan komponen biaya, bukan hanya satu angka akhir.
3. **Dapat diedit:** semua asumsi biaya dapat diubah pengguna.
4. **Offline-first:** fungsi inti tidak bergantung pada server.
5. **Aman:** aplikasi tidak menghapus atau menimpa data tanpa konfirmasi.
6. **Jujur:** hasil merupakan estimasi berdasarkan input pengguna, bukan jaminan laba.
7. **Lokal:** format rupiah, satuan umum Indonesia, dan istilah UMKM diprioritaskan.
8. **Bilingual sejak fondasi:** seluruh teks antarmuka berasal dari resource terjemahan; tidak ada teks UI yang ditulis langsung di dalam logika aplikasi.
9. **Data bebas bahasa:** database menyimpan kode stabil dan angka mentah, bukan label terjemahan atau nilai uang yang telah diformat.

## 5.1 Strategi bahasa dan pasar

Kosio mendukung tiga pilihan bahasa aplikasi:

- **Follow device / Ikuti perangkat** — pilihan default.
- **Bahasa Indonesia**.
- **English**.

Aturan fallback:

- Perangkat berbahasa Indonesia menggunakan Bahasa Indonesia.
- Perangkat berbahasa English menggunakan English.
- Perangkat dengan bahasa lain menggunakan English sebagai fallback.
- Pengguna dapat mengganti bahasa Kosio tanpa mengubah bahasa perangkat.
- Pilihan bahasa dipertahankan setelah aplikasi ditutup, perangkat direstart, backup dipulihkan, atau aplikasi diperbarui.

Pasar dan lokalisasi:

- Pasar validasi awal adalah UMKM kuliner Indonesia.
- Store listing utama, customer support awal, contoh data, dan materi onboarding memprioritaskan Indonesia.
- English merupakan bahasa produk, bukan klaim bahwa semua kebutuhan pasar global telah didukung.
- Imperial units seperti oz, lb, dan fl oz ditunda sampai validasi pasar English; metric units tetap menjadi scope MVP.
- Terjemahan tambahan tidak boleh memerlukan migrasi database atau perubahan rumus kalkulasi.

## 6. Istilah

| Istilah | Definisi dalam aplikasi |
| --- | --- |
| Bahan | Komponen yang dipakai untuk menghasilkan produk, termasuk bahan makanan dan kemasan yang ingin dicatat sebagai bahan |
| Harga pembelian | Harga yang dibayar untuk jumlah pembelian tertentu |
| Unit dasar | Satuan terkecil yang dipakai untuk kalkulasi, misalnya gram, ml, atau pcs |
| Formula | Daftar bahan dan jumlah yang digunakan untuk satu batch produk |
| Batch | Satu kali proses produksi |
| Yield | Jumlah unit atau porsi layak jual yang dihasilkan satu batch |
| Waste | Bagian bahan yang dibeli tetapi tidak menjadi hasil produksi |
| Overhead | Biaya produksi selain bahan langsung, misalnya gas, listrik, dan tenaga kerja |
| HPP batch | Total biaya untuk membuat satu batch |
| HPP per unit | HPP batch dibagi jumlah unit layak jual |
| Laba per unit | Harga jual efektif dikurangi HPP per unit dan biaya penjualan per unit yang belum dimasukkan |
| Margin | Laba dibagi pendapatan, dinyatakan dalam persen |
| Markup | Laba dibagi HPP, dinyatakan dalam persen |

### 6.1 Glossary bilingual

| Bahasa Indonesia | English | Catatan |
| --- | --- | --- |
| Bahan | Ingredient | Nama bahan buatan pengguna tidak diterjemahkan otomatis |
| Harga pembelian | Purchase price | Harga untuk jumlah pembelian tertentu |
| Harga per unit | Unit price | Harga per gram, ml, pcs, dan seterusnya |
| Harga Pokok Produksi (HPP) | Production cost / Unit cost | Singkatan HPP hanya digunakan pada UI Indonesia |
| Biaya bahan | Ingredient cost | Bahan langsung dalam formula |
| Biaya kemasan | Packaging cost | Tidak boleh dihitung ganda |
| Biaya tambahan | Additional cost | Label generik untuk biaya lain |
| Biaya tidak langsung | Overhead | Istilah overhead boleh disertai penjelasan Indonesia |
| Hasil produksi | Production yield | Banyak unit layak jual per batch |
| Bahan terbuang | Ingredient waste | Persentase atau usable quantity |
| Harga jual | Selling price | Harga sebelum/atau setelah diskon harus diberi label jelas |
| Pendapatan bersih | Net revenue | Setelah diskon dan biaya transaksi dalam simulasi |
| Laba per unit | Profit per unit | Estimasi berdasarkan input |
| Margin keuntungan | Profit margin | Laba dibagi pendapatan |
| Markup | Markup | Laba dibagi cost |
| Riwayat kalkulasi | Calculation history | Snapshot yang tidak berubah |
| Ikuti perangkat | Follow device | Menggunakan locale sistem |

Glossary ini menjadi sumber istilah untuk UI, onboarding, bantuan, store listing, dan pengujian. Terjemahan harus menyampaikan konsep bisnis yang sama, bukan sekadar terjemahan kata per kata.

## 7. Scope MVP

### 7.1 Termasuk dalam MVP

- Master bahan dan harga pembelian.
- Satuan berat, volume, dan jumlah.
- Konversi kg–g dan liter–ml.
- Satuan pembelian kustom seperti pak, botol, dus, atau tray yang dikonversi ke pcs atau unit dasar.
- Produk/formula produksi.
- Jumlah hasil produksi atau yield.
- Biaya kemasan.
- Biaya gas, listrik, tenaga kerja, dan biaya lain.
- Waste bahan dalam persen atau berat bersih.
- HPP batch dan HPP per unit.
- Simulasi harga jual, margin, dan markup.
- Simulasi diskon serta biaya marketplace.
- Penyimpanan skenario kalkulasi.
- Riwayat perubahan harga bahan dan kalkulasi produk.
- Share ringkasan sebagai teks.
- Share hasil sebagai gambar sederhana.
- Backup dan restore menggunakan file lokal.
- Tema terang; tema gelap opsional jika tidak menghambat jadwal.
- Antarmuka lengkap dalam Bahasa Indonesia dan English.
- Pemilih bahasa: Ikuti perangkat, Bahasa Indonesia, dan English.
- Pemilih mata uang yang terpisah dari bahasa; IDR dan USD tersedia pada MVP.
- Format angka dan mata uang mengikuti locale tampilan tanpa mengubah nilai yang tersimpan.

### 7.2 Ditunda

- Login dan akun pengguna.
- Cloud sync atau sinkronisasi keluarga/tim.
- OCR struk.
- Barcode scanner.
- Import harga otomatis.
- Integrasi toko atau marketplace.
- Inventory dan pengurangan stok otomatis.
- Pencatatan transaksi penjualan.
- Daftar mata uang global penuh dan aturan pajak tiap negara.
- Imperial units seperti oz, lb, fl oz, pint, dan gallon.
- Laporan akuntansi dan pajak.
- Invoice dan nota.
- Aplikasi web atau iOS.
- AI untuk menyarankan harga.

## 8. User journey utama

### 8.1 Pengguna baru

1. Membuka aplikasi.
2. Melihat penjelasan singkat bahwa semua data tersimpan di perangkat.
3. Aplikasi mengikuti bahasa perangkat atau meminta pengguna memilih Bahasa Indonesia/English bila diperlukan.
4. Untuk locale Indonesia, aplikasi memilih IDR dan format angka Indonesia sebagai default; pengguna tetap dapat mengganti mata uang.
5. Menambahkan bahan pertama.
6. Memasukkan harga pembelian dan jumlah pembelian.
7. Membuat produk.
8. Menambahkan bahan ke formula.
9. Mengisi yield, kemasan, dan overhead.
10. Melihat HPP/unit cost per unit.
11. Memasukkan harga jual atau target margin.
12. Menyimpan kalkulasi.

### 8.2 Memperbarui harga bahan

1. Pengguna membuka bahan.
2. Memilih **Perbarui harga**.
3. Memasukkan harga dan jumlah pembelian terbaru.
4. Aplikasi menampilkan harga unit lama dan baru.
5. Aplikasi menampilkan produk yang menggunakan bahan tersebut.
6. Setelah disimpan, kalkulasi produk menggunakan harga terbaru.
7. Riwayat harga lama tetap tersedia.

### 8.3 Mensimulasikan promo marketplace

1. Pengguna membuka produk.
2. Memilih **Simulasi harga jual**.
3. Memasukkan harga normal, diskon, biaya marketplace, dan biaya tetap transaksi jika ada.
4. Aplikasi menghitung pendapatan bersih, laba, dan margin.
5. Jika harga efektif di bawah HPP, tampilkan peringatan yang jelas.
6. Pengguna dapat menyimpan skenario tanpa mengubah formula produk.

## 9. Kebutuhan fungsional

### 9.1 Dashboard

Dashboard menampilkan:

- CTA **Buat produk**.
- CTA **Tambah bahan**.
- Jumlah produk dan bahan.
- Produk yang terakhir dihitung.
- Bahan yang harganya paling lama belum diperbarui, jika tersedia.
- Produk dengan margin negatif atau di bawah target, jika ada.
- Shortcut backup data.

Dashboard tidak menampilkan omzet atau laba aktual karena MVP tidak mencatat transaksi penjualan.

### 9.2 Master bahan

Pengguna dapat:

- Membuat, mengedit, mengarsipkan, dan mencari bahan.
- Mengisi nama bahan.
- Memilih kategori opsional: bahan utama, bumbu, kemasan, atau lainnya.
- Memilih tipe satuan: berat, volume, atau jumlah.
- Mengisi harga pembelian.
- Mengisi jumlah pembelian dan satuannya.
- Melihat harga per unit dasar.
- Menambahkan catatan merek atau toko secara opsional.
- Menyimpan tanggal pembelian atau pembaruan harga.
- Melihat riwayat harga.
- Melihat produk yang menggunakan bahan tersebut.

Contoh input:

```text
Nama               Tepung terigu
Harga pembelian    Rp14.000
Jumlah pembelian   1 kg
Harga unit         Rp14/gram
```

### 9.3 Satuan dan konversi

Satuan bawaan MVP:

| Tipe | Satuan |
| --- | --- |
| Berat | mg, g, kg |
| Volume | ml, liter |
| Jumlah | pcs, butir, lembar, sachet, botol, cup, pak, dus, tray |

Aturan:

- Konversi dalam keluarga yang sama dilakukan otomatis.
- `1 kg = 1.000 g`.
- `1 liter = 1.000 ml`.
- Aplikasi tidak mengubah volume menjadi berat tanpa faktor konversi khusus.
- Untuk pak/dus/tray, pengguna harus menentukan jumlah unit di dalam kemasan.
- Aplikasi tidak mengasumsikan satu sendok bahan selalu mempunyai berat yang sama.

Contoh:

```text
Cup plastik, 1 pak = 50 pcs, harga Rp25.000
Harga per pcs = Rp500
```

### 9.4 Waste bahan

Pengguna dapat memakai salah satu dari dua metode:

1. **Persentase waste** — misalnya ayam memiliki waste 20%.
2. **Berat bersih** — misalnya membeli 1 kg tetapi berat layak pakai 800 g.

Rumus persentase:

```text
yieldFactor = 1 - wastePercent / 100
hargaUnitEfektif = hargaUnitPembelian / yieldFactor
```

Contoh:

```text
Harga ayam 1 kg       Rp40.000
Waste                 20%
Berat bersih          800 g
Harga efektif         Rp50/gram bersih
```

Waste 100% tidak boleh disimpan. Waste harus berada pada rentang 0% sampai kurang dari 100%.

### 9.5 Produk dan formula

Pengguna dapat:

- Membuat, menduplikasi, mengedit, mengarsipkan, dan mencari produk.
- Menambahkan nama dan kategori produk.
- Menambahkan foto opsional dari perangkat.
- Menambahkan bahan dari master bahan.
- Menentukan kuantitas tiap bahan.
- Menentukan jumlah hasil produksi/yield.
- Menentukan label unit hasil seperti porsi, box, botol, cup, bungkus, atau pcs.
- Menambahkan biaya kemasan.
- Menambahkan overhead.
- Menulis catatan produksi opsional.

Perubahan formula tidak menghapus kalkulasi lama dari riwayat. Riwayat menyimpan snapshot komponen biaya saat kalkulasi disimpan.

### 9.6 Kemasan

Kemasan dapat dicatat melalui dua cara:

- Sebagai bahan pada formula, misalnya cup, tutup, sedotan, stiker, dan paper bag.
- Sebagai biaya kemasan total per batch atau per unit.

Aplikasi perlu menjelaskan agar pengguna tidak memasukkan biaya yang sama dua kali.

Untuk kesederhanaan, rekomendasi antarmuka adalah menggunakan bahan untuk kemasan yang mempunyai stok/harga pembelian, dan field kemasan tambahan hanya untuk biaya yang belum dibuat sebagai bahan.

### 9.7 Overhead

Jenis overhead bawaan:

- Gas.
- Listrik.
- Air.
- Tenaga kerja.
- Sewa/peralatan.
- Transport produksi.
- Lainnya.

Overhead dapat dimasukkan sebagai:

- Nominal per batch.
- Nominal per unit.
- Persentase dari biaya bahan langsung.

Semua jenis overhead harus menunjukkan basis perhitungannya. Aplikasi tidak menghitung biaya gas/listrik otomatis pada MVP.

### 9.8 Perhitungan HPP

Rumus dasar:

```text
biayaBahanItem = kuantitasDipakaiDalamUnitDasar × hargaUnitEfektif

totalBiayaBahan = jumlah seluruh biayaBahanItem

totalOverhead = overheadPerBatch
              + overheadPerUnit × yield
              + persentaseOverhead × totalBiayaBahan

hppBatch = totalBiayaBahan
         + biayaKemasanTambahan
         + totalOverhead

hppPerUnit = hppBatch / yield
```

Aturan:

- Yield harus lebih besar dari nol.
- Nilai uang internal disimpan dengan presisi cukup; pembulatan dilakukan saat ditampilkan.
- Ringkasan harus memperlihatkan biaya bahan, kemasan, overhead, HPP batch, dan HPP per unit.
- Persentase kontribusi biaya dapat ditampilkan per komponen.
- Semua hasil diberi label **estimasi berdasarkan data yang dimasukkan**.

### 9.9 Harga jual, laba, margin, dan markup

Rumus:

```text
labaPerUnit = hargaJualBersih - hppPerUnit

marginPercent = labaPerUnit / hargaJualBersih × 100

markupPercent = labaPerUnit / hppPerUnit × 100

hargaUntukTargetMargin = hppPerUnit / (1 - targetMarginPercent / 100)
```

Aturan:

- Margin dan markup selalu diberi label berbeda.
- Target margin harus kurang dari 100%.
- Harga di bawah HPP menampilkan status rugi.
- Aplikasi tidak menyebut margin sebagai laba bersih bisnis karena belum mencakup seluruh biaya perusahaan dan transaksi aktual.

### 9.10 Simulasi marketplace dan diskon

Input simulasi:

- Harga katalog.
- Diskon penjual dalam persen atau nominal.
- Biaya marketplace dalam persen.
- Biaya transaksi tetap.
- Biaya layanan/promosi tambahan opsional.
- Subsidi ongkir yang ditanggung penjual opsional.

Rumus umum:

```text
hargaSetelahDiskon = hargaKatalog - diskonNominal

biayaPersentase = hargaSetelahDiskon × biayaMarketplacePercent / 100

pendapatanBersih = hargaSetelahDiskon
                 - biayaPersentase
                 - biayaTransaksiTetap
                 - biayaTambahan

labaPerUnit = pendapatanBersih - hppPerUnit
```

Aplikasi tidak menyimpan tarif marketplace sebagai angka yang dianggap selalu benar. Preset dapat disediakan, tetapi harus dapat diedit dan diberi keterangan **periksa kembali tarif terbaru**.

### 9.11 Riwayat kalkulasi

Saat pengguna menyimpan kalkulasi, aplikasi menyimpan snapshot:

- Nama produk.
- Tanggal dan waktu.
- Komponen biaya.
- Harga bahan yang dipakai.
- Yield.
- HPP batch dan per unit.
- Harga jual/skenario.
- Margin dan markup.

Riwayat tidak berubah ketika harga bahan master diperbarui. Pengguna dapat membandingkan kalkulasi sekarang dengan kalkulasi sebelumnya.

### 9.12 Share

MVP mendukung:

- Share sebagai teks menggunakan Android share sheet.
- Share sebagai gambar ringkasan sederhana.

Ringkasan default tidak menampilkan formula lengkap bila pengguna hanya ingin membagikan hasil. Pengguna dapat memilih:

- Ringkasan hasil.
- Rincian biaya.

Contoh ringkasan:

```text
HPP Dessert Box

HPP batch: Rp150.000
Hasil: 20 box
HPP per box: Rp7.500
Harga jual: Rp15.000
Estimasi laba per box: Rp7.500
Margin: 50%

Dihitung dengan Kosio.
```

Aplikasi tidak mengirim pesan otomatis. Pengguna selalu memilih aplikasi dan penerima melalui share sheet.

### 9.13 Backup dan restore

Backup:

- Mengekspor seluruh database aplikasi ke satu file.
- File memiliki versi schema dan tanggal pembuatan.
- Pengguna memilih lokasi menggunakan Android system file picker.
- Data foto dapat disertakan atau tidak, dengan penjelasan ukuran file.

Restore:

- Memvalidasi format dan versi file.
- Menampilkan ringkasan isi sebelum restore.
- Meminta konfirmasi karena restore dapat mengganti data aktif.
- Membuat backup otomatis dari data saat ini sebelum mengganti, bila memungkinkan.
- Menolak file rusak tanpa mengubah database aktif.

MVP tidak mengenkripsi file backup dengan password. Aplikasi harus memberi tahu pengguna bahwa file dapat berisi informasi biaya usaha dan perlu disimpan dengan aman.

### 9.14 Bahasa, locale, dan mata uang

Kosio menyediakan pemilih bahasa di Onboarding dan Pengaturan. Implementasi menggunakan mekanisme per-app language Android sehingga pilihan aplikasi konsisten dengan pengaturan sistem pada versi Android yang mendukungnya.

Persyaratan:

- Seluruh judul, label, tombol, pesan validasi, empty state, dialog, notifikasi lokal, accessibility label, bantuan, dan teks share bawaan tersedia dalam dua bahasa.
- Nama brand **Kosio** tidak diterjemahkan.
- Nama bahan, produk, kategori custom, catatan, dan data lain yang dibuat pengguna tidak diterjemahkan otomatis.
- Mengganti bahasa tidak boleh mengubah formula, mata uang, nilai, urutan data, ataupun calculation snapshot.
- Bahasa dan mata uang adalah dua preferensi berbeda. Pengguna dapat memakai English dengan IDR atau Bahasa Indonesia dengan USD.
- Nilai uang disimpan sebagai angka dan currency code ISO, misalnya `IDR` atau `USD`, bukan sebagai teks `Rp15.000` atau `$15.00`.
- Nilai enum disimpan sebagai kode stabil, misalnya `PER_BATCH`, `PER_UNIT`, `WEIGHT`, dan `VOLUME`; label diterjemahkan hanya saat ditampilkan.
- Tanggal, angka, separator desimal, dan simbol mata uang diformat sesuai locale tampilan serta pilihan currency.
- File backup bebas bahasa dan dapat dipulihkan pada Kosio yang sedang menggunakan bahasa berbeda.
- Pergantian bahasa boleh merekreasi layar, tetapi input form yang belum disimpan tidak boleh hilang tanpa peringatan atau mekanisme pemulihan state.

Contoh representasi:

```text
Database:
calculationType = PER_BATCH
currencyCode = IDR
amountMinor = 1500000

Bahasa Indonesia:
Per batch · Rp15.000

English:
Per batch · IDR 15,000
```

Untuk nilai Rupiah, keputusan penggunaan minor unit harus konsisten. Bila aplikasi memakai integer dalam rupiah penuh, nama field internal tidak boleh menyiratkan sen; bila memakai minor unit generik, currency metadata harus menentukan jumlah digit pecahan.

## 10. Struktur navigasi dan layar

Bottom navigation yang disarankan:

1. **Beranda**
2. **Produk**
3. **Bahan**
4. **Pengaturan**

Layar MVP:

| Layar | Fungsi |
| --- | --- |
| Onboarding singkat | Menjelaskan fungsi, offline storage, serta pilihan bahasa dan mata uang |
| Beranda | Shortcut, ringkasan, kalkulasi terbaru, peringatan margin |
| Daftar bahan | Cari, filter, tambah, dan edit bahan |
| Detail bahan | Harga, konversi, waste, riwayat, produk terdampak |
| Form bahan | Input harga pembelian dan satuan |
| Daftar produk | Cari, kategori, HPP terbaru, status margin |
| Form produk | Nama, formula, yield, kemasan, overhead |
| Hasil HPP | Breakdown HPP batch dan per unit |
| Simulasi harga | Harga jual, target margin, diskon, marketplace |
| Riwayat | Snapshot kalkulasi sebelumnya |
| Share preview | Pilih ringkas/rinci dan teks/gambar |
| Backup & restore | Export, import, dan penjelasan keamanan file |
| Pengaturan | Bahasa, mata uang, pembulatan, tema, data, tentang, privacy policy |

## 11. Model data lokal

### Ingredient

| Field | Tipe/keterangan |
| --- | --- |
| id | UUID lokal |
| name | Wajib, unik secara case-insensitive untuk bahan aktif |
| category | Optional enum/custom |
| measurementType | weight, volume, count |
| baseUnit | mg/g/ml/pcs dan seterusnya |
| archived | Boolean |
| notes | Opsional |
| createdAt | Timestamp |
| updatedAt | Timestamp |

### IngredientPrice

| Field | Tipe/keterangan |
| --- | --- |
| id | UUID lokal |
| ingredientId | Foreign key |
| purchasePrice | Decimal |
| purchaseQuantity | Decimal |
| purchaseUnit | Unit |
| unitsPerPackage | Nullable untuk pak/dus/tray |
| wastePercent | Nullable |
| usableQuantity | Nullable, alternatif wastePercent |
| effectiveBaseUnitPrice | Decimal hasil kalkulasi |
| purchasedAt | Tanggal opsional |
| createdAt | Timestamp |

### Product

| Field | Tipe/keterangan |
| --- | --- |
| id | UUID lokal |
| name | Wajib |
| category | Opsional |
| imageUri | Opsional |
| yieldQuantity | Decimal > 0 |
| yieldLabel | porsi/pcs/box/cup/botol/custom |
| notes | Opsional |
| archived | Boolean |
| createdAt | Timestamp |
| updatedAt | Timestamp |

### ProductIngredient

| Field | Tipe/keterangan |
| --- | --- |
| id | UUID lokal |
| productId | Foreign key |
| ingredientId | Foreign key |
| quantity | Decimal > 0 |
| unit | Harus kompatibel dengan tipe bahan |
| sortOrder | Integer |

### Overhead

| Field | Tipe/keterangan |
| --- | --- |
| id | UUID lokal |
| productId | Foreign key |
| name | Gas/listrik/tenaga kerja/custom |
| calculationType | per_batch, per_unit, percentage_material |
| value | Decimal >= 0 |

### CalculationSnapshot

| Field | Tipe/keterangan |
| --- | --- |
| id | UUID lokal |
| productId | Nullable agar sejarah tidak hilang jika produk diarsipkan |
| productName | Snapshot teks |
| calculatedAt | Timestamp |
| costBreakdownJson | Snapshot rincian biaya |
| batchCost | Decimal |
| yieldQuantity | Decimal |
| unitCost | Decimal |
| sellingScenarioJson | Nullable |
| sellingPrice | Nullable |
| netRevenue | Nullable |
| profit | Nullable |
| marginPercent | Nullable |
| markupPercent | Nullable |

### AppSettings

- Pilihan bahasa aplikasi: system, id, atau en.
- Currency code terpisah dari pilihan bahasa.
- Locale efektif untuk formatting.
- Aturan pembulatan tampilan.
- Tema.
- Onboarding completed.
- Waktu backup terakhir.
- Versi schema.

## 12. Validasi dan edge cases

- Harga pembelian tidak boleh negatif.
- Kuantitas pembelian harus lebih besar dari nol.
- Yield produk harus lebih besar dari nol.
- Formula kosong boleh disimpan sebagai draft, tetapi HPP tidak dapat dihitung.
- Bahan tanpa harga aktif ditandai dan memblokir hasil final.
- Unit bahan dan unit formula harus kompatibel.
- Waste tidak boleh mencapai atau melebihi 100%.
- Target margin harus kurang dari 100%.
- Diskon tidak boleh membuat harga transaksi negatif.
- Biaya marketplace boleh melebihi laba dan menghasilkan peringatan rugi.
- Produk yang memakai bahan yang diarsipkan tetap mempertahankan referensi dan menampilkan peringatan.
- Penghapusan permanen bahan yang masih dipakai tidak diizinkan; gunakan arsip.
- Nilai sangat besar harus ditampilkan tanpa overflow.
- Perhitungan desimal tidak boleh menggunakan floating point biner mentah untuk nilai uang.
- Restore file versi lebih baru harus ditolak dengan pesan yang jelas jika belum didukung.
- Foto yang hilang tidak menghalangi pembukaan produk.

## 13. Non-functional requirements

### 13.1 Offline dan performa

- Seluruh fungsi inti bekerja tanpa internet.
- Daftar 1.000 bahan dan 500 produk tetap dapat dicari dengan responsif pada perangkat kelas menengah.
- Kalkulasi satu produk selesai secara lokal dalam waktu kurang dari satu detik untuk formula normal.
- Tidak ada loading network pada alur utama.

### 13.2 Keandalan

- Perubahan penting disimpan secara transaksional.
- Kegagalan kalkulasi tidak merusak formula.
- Restore gagal tidak mengubah data aktif.
- Migrasi schema database diuji untuk setiap versi aplikasi.

### 13.3 Privasi

- Tidak ada akun pada MVP.
- Data bahan, produk, harga, dan kalkulasi disimpan di perangkat.
- Analytics, jika digunakan, tidak mengirim nama bahan, nama produk, harga, formula, atau isi backup.
- Privacy policy menjelaskan dengan tepat data yang dikumpulkan dan SDK pihak ketiga.
- Pengguna dapat menghapus seluruh data lokal dari Pengaturan.

### 13.4 Aksesibilitas

- Area sentuh minimum 48 dp.
- Teks mendukung ukuran font perangkat.
- Warna bukan satu-satunya penanda laba/rugi.
- Nilai laba/rugi mempunyai label dan ikon yang dapat dibaca screen reader.
- Form panjang dapat digulir dan tidak tertutup keyboard.

### 13.5 Lokalisasi

- Tidak ada user-facing string yang di-hardcode dalam Kotlin/Java.
- Resource default/fallback menggunakan English; resource `values-in` menggunakan Bahasa Indonesia.
- CI atau lint harus mendeteksi missing translation key sejauh tooling memungkinkan.
- Semua layar diuji pada English dan Bahasa Indonesia.
- Layout diuji dengan font scale besar dan pseudo-localization untuk menemukan teks terpotong.
- Format plural, angka, tanggal, dan mata uang tidak dibentuk melalui string concatenation yang bergantung pada urutan kata satu bahasa.
- Screenshot dan copy Play Store disediakan secara terpisah untuk Indonesia dan English.
- Privacy policy tersedia dalam kedua bahasa atau memiliki satu versi bilingual yang mudah diakses.

## 14. Monetisasi

Monetisasi tidak boleh menghalangi validasi fungsi inti.

Usulan tahap awal:

### Gratis

- Maksimal 10 produk aktif.
- Bahan tidak dibatasi atau diberi batas longgar.
- Seluruh kalkulasi inti tersedia.
- Iklan banner terbatas dan tidak muncul pada form input atau hasil kritis.

### Pro sekali bayar

- Produk tanpa batas.
- Tanpa iklan.
- Backup/restore.
- Share gambar dan rincian biaya.
- Riwayat harga dan kalkulasi lebih panjang.

Hipotesis harga untuk diuji, bukan keputusan final:

- Pro lifetime: Rp29.000–59.000.
- Remove ads saja: Rp15.000–25.000.

Subscription, paywall agresif, dan iklan interstitial ditunda.

## 15. Analytics dan ukuran keberhasilan

Analytics bersifat opsional. Jika dipasang, event tidak memuat data usaha pengguna.

Event yang aman:

- `onboarding_completed`
- `language_selected`, hanya mengirim kode bahasa tanpa data usaha
- `ingredient_created`
- `ingredient_price_updated`
- `product_created`
- `calculation_completed`
- `selling_scenario_created`
- `result_shared`
- `backup_created`
- `restore_completed`
- `pro_offer_viewed`
- `pro_purchased`

Indikator validasi awal:

- Minimal 60% tester berhasil membuat satu produk tanpa bantuan.
- Median waktu membuat kalkulasi pertama kurang dari 10 menit termasuk input bahan.
- Minimal 30% tester kembali memperbarui harga atau menghitung produk dalam dua minggu.
- Minimal 40% pengguna yang membuat produk juga menjalankan simulasi harga jual.
- Kurang dari 5% sesi kalkulasi gagal karena error aplikasi.

Angka tersebut merupakan target validasi produk, bukan proyeksi bisnis.

## 16. Acceptance criteria MVP

### Bahan dan harga

- Pengguna dapat memasukkan pembelian dalam kg/g, liter/ml, dan jumlah kemasan.
- Harga per unit dasar dihitung benar.
- Perubahan harga bahan tercermin pada kalkulasi produk berikutnya.
- Riwayat kalkulasi lama tidak berubah.
- Waste meningkatkan harga efektif dengan rumus yang benar.

### Formula dan HPP

- Produk dapat mempunyai banyak bahan.
- Kuantitas lintas satuan kompatibel dikonversi dengan benar.
- HPP batch sama dengan jumlah biaya bahan, kemasan, dan overhead.
- HPP per unit sama dengan HPP batch dibagi yield.
- Bahan tanpa harga memblokir kalkulasi final dengan pesan yang dapat ditindaklanjuti.

### Harga jual

- Laba, margin, dan markup dihitung serta dilabeli dengan benar.
- Target margin menghasilkan harga jual yang benar.
- Diskon dan biaya marketplace mengurangi pendapatan bersih.
- Harga efektif di bawah HPP menampilkan peringatan rugi.

### Penyimpanan

- Data bertahan setelah aplikasi ditutup dan perangkat direstart.
- Backup menghasilkan file yang dapat dipilih pengguna.
- Restore file valid mengembalikan bahan, produk, formula, dan riwayat.
- Restore file rusak tidak merusak database aktif.

### Share

- Pengguna dapat melihat preview sebelum share.
- Share menggunakan Android share sheet.
- Angka pada hasil share sama dengan hasil dalam aplikasi.

### Bahasa dan locale

- Instalasi dengan perangkat berbahasa Indonesia membuka Kosio dalam Bahasa Indonesia.
- Instalasi dengan perangkat berbahasa English membuka Kosio dalam English.
- Bahasa perangkat lain menggunakan English sebagai fallback.
- Pengguna dapat mengganti bahasa dari Pengaturan dan seluruh layar aktif memakai terjemahan baru.
- Pilihan **Ikuti perangkat** mengembalikan aplikasi ke bahasa sistem yang didukung atau English fallback.
- Pilihan bahasa bertahan setelah restart dan update aplikasi.
- Mengganti bahasa tidak mengubah currency, bahan, produk, formula, kalkulasi, atau riwayat.
- English + IDR serta Bahasa Indonesia + USD sama-sama berfungsi.
- Backup dari UI Indonesia dapat direstore pada UI English dan sebaliknya.
- Tidak ada label yang belum diterjemahkan pada alur utama.

## 17. Skenario pengujian penting

1. Tepung Rp14.000/kg, penggunaan 250 g menghasilkan biaya Rp3.500.
2. Minyak Rp18.000/liter, penggunaan 100 ml menghasilkan biaya Rp1.800.
3. Cup Rp25.000 per pak isi 50 menghasilkan biaya Rp500 per cup.
4. Ayam Rp40.000/kg dengan waste 20% menghasilkan harga efektif Rp50/g bersih.
5. HPP batch Rp100.000 dengan yield 20 menghasilkan HPP Rp5.000/unit.
6. HPP Rp5.000 dan harga jual Rp10.000 menghasilkan laba Rp5.000, margin 50%, markup 100%.
7. Target margin 40% untuk HPP Rp6.000 menghasilkan harga Rp10.000 sebelum pembulatan.
8. Harga Rp20.000 dengan diskon 10% dan biaya marketplace 20% dari harga setelah diskon menghasilkan pendapatan sebelum biaya tetap Rp14.400.
9. Mengubah harga tepung hanya mengubah kalkulasi baru; snapshot lama tetap sama.
10. Bahan berat tidak dapat digunakan dengan satuan ml tanpa faktor konversi.
11. Bahan yang digunakan produk tidak dapat dihapus permanen.
12. Restore file rusak gagal dengan aman.
13. Aplikasi tetap dapat menghitung dalam mode pesawat.
14. Share teks dan gambar menampilkan angka identik.
15. Pembulatan tampilan tidak mengubah presisi kalkulasi internal.
16. Ganti bahasa Indonesia ke English pada form produk; draft dan angka yang sedang diisi tetap tersedia.
17. Buka calculation snapshot yang sama dalam dua bahasa; nilai matematis identik dan hanya label/format yang berubah.
18. Restore backup yang dibuat saat bahasa Indonesia aktif ke instalasi English; seluruh data dan enum terbaca benar.
19. Pilih English + IDR; angka memakai format English tanpa mengubah nilai Rupiah tersimpan.
20. Uji setiap layar pada font scale besar dalam kedua bahasa tanpa tombol atau angka penting terpotong.

## 18. Tahapan pengembangan

### Tahap 1 — Fondasi

- Project Android.
- Design tokens dan navigasi.
- Fondasi localization untuk English dan Bahasa Indonesia.
- Glossary bilingual dan aturan formatting locale/currency.
- Database lokal dan migration strategy.
- Model unit, uang, dan konversi.
- Test unit untuk seluruh rumus.

### Tahap 2 — Bahan

- Daftar dan form bahan.
- Harga pembelian.
- Konversi unit.
- Waste dan riwayat harga.

### Tahap 3 — Produk dan HPP

- Form produk dan formula.
- Yield, kemasan, dan overhead.
- Hasil HPP dan breakdown.
- Riwayat snapshot.

### Tahap 4 — Simulasi

- Harga jual.
- Target margin.
- Diskon dan marketplace.
- Peringatan rugi.

### Tahap 5 — Portabilitas

- Share teks/gambar.
- Backup dan restore.
- Reset data.

### Tahap 6 — Validasi dan rilis

- Uji perangkat dan mode pesawat.
- Uji migrasi database.
- Privacy policy dan Data Safety.
- Closed testing dengan pelaku UMKM.
- Perbaikan berdasarkan observasi penggunaan.

## 19. Rencana validasi pengguna

Rekrut 5–10 pelaku usaha dari minimal tiga jenis usaha, misalnya bakery, makanan rumahan, dan minuman.

Tugas pengujian:

1. Tambahkan lima bahan yang benar-benar mereka beli.
2. Buat satu produk yang benar-benar mereka jual.
3. Bandingkan HPP aplikasi dengan metode mereka sekarang.
4. Ubah satu harga bahan.
5. Simulasikan promo marketplace.
6. Bagikan hasil kepada diri sendiri melalui WhatsApp.
7. Buat backup.

Pertanyaan observasi:

- Istilah apa yang membingungkan?
- Apakah overhead dimengerti?
- Apakah pengguna membedakan margin dan markup?
- Input mana yang paling melelahkan?
- Apakah hasil mengubah keputusan harga jual?
- Fitur apa yang benar-benar akan digunakan kembali?

Jangan menambah OCR, inventory, atau cloud sync sebelum alur inti terbukti bermanfaat.

## 20. Risiko dan mitigasi

| Risiko | Dampak | Mitigasi |
| --- | --- | --- |
| Input awal terasa panjang | Pengguna berhenti sebelum mendapat hasil | Contoh bahan, duplikasi, autocomplete, dan progress yang jelas |
| Pengguna tidak memahami overhead | HPP tetap terlalu rendah | Penjelasan singkat dan contoh nominal per batch |
| Margin disalahartikan sebagai laba bersih | Keputusan bisnis keliru | Label “estimasi laba produk” dan breakdown transparan |
| Tarif marketplace berubah | Preset tidak akurat | Semua tarif dapat diedit dan tidak diklaim selalu terbaru |
| Data hilang saat perangkat rusak | Kehilangan data usaha | Pengingat backup lokal berkala dan restore yang teruji |
| Terlalu banyak iklan | Kepercayaan turun | Tanpa interstitial pada MVP; banner terbatas |
| Scope berkembang menjadi akuntansi | Jadwal dan UX membengkak | Tegaskan non-goals dan validasi alur inti dahulu |

## 21. Keputusan teknis yang disarankan

- Kotlin.
- Jetpack Compose atau XML modern; pilih satu dan gunakan konsisten.
- Room untuk database lokal.
- DataStore untuk pengaturan.
- `BigDecimal` atau representasi integer minor units untuk nilai uang.
- ViewModel dan repository sederhana.
- Storage Access Framework untuk backup/restore.
- Android share sheet untuk berbagi.
- Tidak memerlukan backend pada MVP.
- Crash reporting opsional; pastikan tidak mengirim data bisnis pengguna.

## 22. Definition of Done

MVP dianggap selesai ketika:

1. Seluruh acceptance criteria terpenuhi.
2. Semua skenario rumus utama memiliki automated unit test.
3. Aplikasi dapat digunakan sepenuhnya dalam mode pesawat.
4. Tidak ada crash blocker pada alur bahan → produk → HPP → harga jual → share.
5. Backup dan restore berhasil diuji pada instalasi bersih.
6. Lima pengguna target berhasil menyelesaikan kalkulasi produk nyata.
7. Privacy policy dan deklarasi Data Safety sesuai SDK yang benar-benar digunakan.
8. Tidak ada fitur dalam listing yang belum tersedia dalam aplikasi.
9. Seluruh alur utama lolos pengujian dalam Bahasa Indonesia dan English.
10. Pergantian bahasa, locale formatting, serta backup lintas bahasa lolos acceptance test.
