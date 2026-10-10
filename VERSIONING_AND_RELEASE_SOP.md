# 📋 SOP Versioning & Release Management — Kosio Android

> **Repository**: [https://github.com/sabeq83/kosio.git](https://github.com/sabeq83/kosio.git)  
> **Aplikasi**: Kosio (Kalkulator HPP & Simulasi Harga Jual Kuliner / F&B)  
> **Standar**: Semantic Versioning 2.0.0 & Google Play Console Guidelines

---

## 1. Fondasi Versioning Android

Setiap build aplikasi Kosio memiliki dua identitas versi utama di [`app/build.gradle.kts`](file:///app/build.gradle.kts):

| Parameter | Tipe Data | Ditampilkan ke User? | Kegunaan |
| :--- | :--- | :--- | :--- |
| **`versionName`** | `String` | **Ya** (Play Store & Info Aplikasi) | Penanda versi bagi pengguna berdasarkan format `MAJOR.MINOR.PATCH` |
| **`versionCode`** | `Integer` | **Tidak** (Internal Android / Google Play) | Integer unik yang **wajib selalu naik** pada setiap upload APK/AAB ke Google Play |

---

## 2. Struktur `versionName` (Semantic Versioning 2.0.0)

Format standar: **`MAJOR.MINOR.PATCH[-CLASSIFIER]`**

```
  v 1 . 2 . 0
    │   │   └── PATCH  : Bug fix, hotfix kecil, penyesuaian UI ringan
    │   └────── MINOR  : Fitur baru yang kompatibel (misal: OCR Struk, Multi-Channel Pricing)
    └────────── MAJOR  : Perombakan arsitektur besar / Redesign UI total / Breaking DB Change
```

### Panduan Kenaikan Versi:
- **`MAJOR` (X.0.0)**:
  - Redesign alur aplikasi secara masif.
  - Perubahan skema Database Room yang bersifat breaking / perombakan struktur data inti.
- **`MINOR` (1.X.0)**:
  - Penambahan modul/fitur baru (misal: *Scan Struk Belanja OCR*, *Kalkulator Multi-Channel*, *Export PDF/Excel*, *Starter Pack Resep*).
  - Mengatur ulang nilai `PATCH` menjadi `0`.
- **`PATCH` (1.0.X)**:
  - Perbaikan bug kalkulasi, crash fixes, optimalisasi performa.
  - Perubahan teks/translasi atau styling tanpa fitur baru.
- **`CLASSIFIER` (Opsional)**:
  - `-alpha.1` : Build pengembangan internal.
  - `-beta.1` : Build uji coba terbatas (Closed Testing).
  - `-rc.1` : Release candidate sebelum dipublikasikan ke Production.

---

## 3. Formula Otomasi `versionCode`

Untuk menjamin `versionCode` **selalu strictly increasing** dan mencegah penolakan dari Google Play Console, Kosio menggunakan rumus matematis:

$$\mathbf{versionCode} = (\mathbf{MAJOR} \times 10000) + (\mathbf{MINOR} \times 100) + \mathbf{PATCH}$$

### Tabel Referensi `versionCode`:
| `versionName` | Rumus Perhitungan | `versionCode` | Status |
| :--- | :--- | :--- | :--- |
| `1.0.0` | $(1 \times 10000) + (0 \times 100) + 0$ | `10000` | Baseline MVP Kosio |
| `1.0.1` | $(1 \times 10000) + (0 \times 100) + 1$ | `10001` | Patch fix margin kalkulator |
| `1.1.0` | $(1 \times 10000) + (1 \times 100) + 0$ | `10100` | Multi-Channel & Target Markup |
| `1.2.0` | $(1 \times 10000) + (2 \times 100) + 0$ | `10200` | OCR Scan Struk Belanja |
| `2.0.0` | $(2 \times 10000) + (0 \times 100) + 0$ | `20000` | Kosio v2.0 Release |

---

## 4. Skema Git Branching & Workflow

```
main (Production Releases) ──────● (v1.0.0) ───────────────● (v1.2.0)
                                 │                         ▲
develop (Active Dev) ────────────●────●────●───● (Merge) ──┤
                                      │        │
feature/* ────────────────────────────● (feat) ┘
```

1. **`main`**: Branch stabil khusus untuk build produksi Google Play Store. Setiap commit di `main` wajib memiliki Git Tag (contoh: `v1.2.0`).
2. **`develop`**: Branch integrasi utama untuk pengembangan fitur yang sedang berlangsung.
3. **`feature/<nama-fitur>`**: Branch terpisah untuk tiap fitur baru (misal: `feature/ocr-receipt-scanner`). Setelah selesai dan lolos unit test, dibuat Pull Request (PR) ke `develop`.
4. **`release/vX.Y.Z`**: Branch persiapan rilis (version bump, final QA, update changelog) sebelum dimerge ke `main` dan `develop`.
5. **`hotfix/vX.Y.Z`**: Branch darurat jika ada bug kritis di production. Dibuat dari `main` dan setelah selesai dimerge kembali ke `main` dan `develop`.

---

## 5. SOP Alur Rilis (Step-by-Step Release Checklist)

Sebelum merilis build baru ke Google Play Console, ikuti checklist berikut secara berurutan:

### Step 1: Update Versi di Gradle
Buka [`app/build.gradle.kts`](file:///app/build.gradle.kts) dan perbarui:
```kotlin
val versionMajor = 1
val versionMinor = 2
val versionPatch = 0
val versionClassifier: String? = null // Set null untuk rilis final
```

### Step 2: Perbarui Catatan Rilis (`CHANGELOG.md`)
Pindahkan item dari section `[Unreleased]` ke versi baru `[X.Y.Z] - YYYY-MM-DD` di [`CHANGELOG.md`](file:///CHANGELOG.md).

### Step 3: Jalankan Automated Test Suite
Pastikan seluruh unit test lulus tanpa error:
```bash
./gradlew testDebugUnitTest
```

### Step 4: Build Bundle Release (AAB)
Generate Android App Bundle (`.aab`) yang sudah ditandatangani keystore:
```bash
./gradlew bundleRelease
```
*Output file:* `app/build/outputs/bundle/release/app-release.aab`

### Step 5: Git Tagging
Setelah merge ke branch `main`, buat tag versi:
```bash
git tag -a v1.2.0 -m "Release version 1.2.0 - OCR Receipt Scan"
git push origin main --tags
```

### Step 6: Jalur Rilis Google Play Console (Staged Rollout)
1. **Internal Testing**: Unggah AAB dan bagikan ke tim tester internal.
2. **Closed / Open Beta**: Uji coba pengguna terbatas untuk mendeteksi crash perangkat beragam.
3. **Production (Staged Rollout)**:
   - Hari 1: **10%** pengguna (pantau Play Console Crash Rate / ANR).
   - Hari 2: **25%** pengguna.
   - Hari 3: **50%** pengguna.
   - Hari 4+: **100%** Full Rollout jika crash rate $< 0.1\%$.

---

## 6. Standar Format Release Notes (What's New)

Gunakan format bilingual saat mengisi form *What's New* di Google Play Console:

```markdown
[ID] Kosio v1.2.0
✨ Fitur Baru:
- Scan Struk Belanja Otomatis (OCR): Foto struk belanja dari minimarket/pasar untuk input master bahan otomatis tanpa ketik manual (100% offline & privat).
- Multi-Channel Pricing: Simulasi komparasi margin di GoFood, GrabFood, ShopeeFood, dan Dine-in.

⚡ Peningkatan:
- Perhitungan margin kini mendukung target markup >100%.
- Navigasi layar lebih cepat dan mulus.

---
[EN] Kosio v1.2.0
✨ What's New:
- On-Device Receipt Scanner (OCR): Snap receipts to automatically extract ingredients & purchase prices offline.
- Multi-Channel Pricing Simulation: Compare profits across delivery channels and dine-in.

⚡ Improvements:
- Margin calculator now supports markup >100%.
- Smoother transitions between screens.
```
