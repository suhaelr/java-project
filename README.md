# Selisih Kurang — Rekonsiliasi RC vs EJ

Aplikasi desktop Java untuk rekonsiliasi data **RC (Recon / ICONS)** dan **EJ (E-Journal)** pada settlement ATM/CRM. Dibangun berdasarkan dokumen spesifikasi **SELISIH KURANG.pdf** untuk mempercepat pencocokan dua sumber data, mendeteksi penyebab selisih, dan menghasilkan **Berita Acara Settlement** siap cetak.

**Repository:** [https://github.com/suhaelr/java-project](https://github.com/suhaelr/java-project)

---

## Daftar Isi

1. [Latar Belakang & Tujuan](#latar-belakang--tujuan)
2. [Fitur Utama](#fitur-utama)
3. [Cara Pakai (End User)](#cara-pakai-end-user)
4. [Paket Portable (Tanpa Install Java)](#paket-portable-tanpa-install-java)
5. [Build dari Source](#build-dari-source)
6. [Format File Input (RC & EJ)](#format-file-input-rc--ej)
7. [Alur Kerja Program (Detail)](#alur-kerja-program-detail)
8. [Logika Rekonsiliasi](#logika-rekonsiliasi)
9. [Kolom Excel Hasil Rekonsiliasi (A–R)](#kolom-excel-hasil-rekonsiliasi-ar)
10. [Berita Acara Settlement](#berita-acara-settlement)
11. [Struktur Kode & File](#struktur-kode--file)
12. [Arsitektur Teknis](#arsitektur-teknis)
13. [Testing](#testing)
14. [Data yang Perlu Diminta ke Rekan](#data-yang-perlu-diminta-ke-rekan)
15. [Keterbatasan Saat Ini](#keterbatasan-saat-ini)
16. [Roadmap / Pengembangan Lanjutan](#roadmap--pengembangan-lanjutan)

---

## Latar Belakang & Tujuan

Dalam operasional settlement ATM/CRM, petugas harus memastikan catatan **sistem bank (RC)** dan catatan **mesin ATM (EJ)** selaras. Ketidakcocokan disebut **Selisih Kurang**. Menurut PDF spesifikasi, penyebab utama yang ditangani aplikasi ini:

| Jenis Selisih | Penjelasan | Ditangani Aplikasi |
|---------------|----------|-------------------|
| **ACQ Kurang Posting** | Transaksi ada di EJ (mesin) tetapi tidak terbuku di RC/ICONS | ✅ Ya — tab **ACQ** |
| **Nasabah Diuntungkan** | Transaksi terbuku di ICONS, suspect di EJ, dengan nomor rekening nasabah terisi | ✅ Ya — tab **Nasabah Diuntungkan** |
| **Vendor Kurang Setor** | Uang fisik setor CDM tidak sesuai catatan mesin | ❌ Belum — butuh sumber data terpisah |

**Tujuan aplikasi:** menggantikan pencocokan manual baris-per-baris di Excel dengan proses otomatis yang lebih cepat, terstruktur, dan menghasilkan laporan **Berita Acara** sesuai format PDF.

---

## Fitur Utama

### Input & Rekonsiliasi
- Browse dan muat file **RC** dan **EJ** (format TXT/CSV)
- Filter berdasarkan **Tanggal Awal/Akhir** dan **Record Awal/Akhir**
- Auto-detect rentang tanggal/record saat browse file
- Proses rekon di background thread (UI tidak freeze)
- Parser otomatis: format **RC (RK_*.txt)** dan **EJ (EJ.TXT)** produksi, plus fallback tabular lama

### Tab Hasil Rekonsiliasi
| Tab | Isi |
|-----|-----|
| **Match** | Transaksi RC & EJ yang cocok (record, amount, type) |
| **EJ Suspect** | Transaksi mencurigakan di sisi EJ |
| **RC Suspect** | Transaksi RC yang tidak cocok / tidak punya pasangan |
| **Reversal** | Transaksi reversal |
| **Nasabah Diuntungkan** | ICONS terbuku + EJ suspect + **Norek terisi** |
| **ACQ (EJ ada, RC tidak)** | Transaksi hanya ada di EJ — ACQ kurang posting |

### Hasil Rekonsiliasi Excel (Kolom A–R)
- Perhitungan otomatis kolom I, N, O, Q1, Q2, R sesuai PDF
- Input manual kolom **M** (Pembukuan SR) dan **P** (TRX by Rec Num)
- Status **KLOP / SELISIH KURANG / SELISIH LEBIH**
- Status **SUSPECT PENDING / TIDAK SUSPECT PENDING**

### Berita Acara Settlement
- Form input data petugas, rekening kas, saldo, restocking/collecting
- Generate laporan HTML format cetak resmi:
  - Header berkotak + ruang logo
  - RESTOCKING & COLLECTING (2 kolom)
  - SETTLEMENT RC vs EJ (tabel berdampingan)
  - Tabel **TRX DI ICONS TERBUKU, DI EJ SUSPECT** (kiri)
  - Tabel **TRX DI EJ ADA, RC TIDAK TERBUKU** (kanan)
  - Format angka `(600.000)` dan `RP1000000`
  - Pagination **Page X of Y**
- Cetak landscape + simpan HTML

### Distribusi
- JAR standalone (`dist/selisih-kurang.jar`)
- Paket portable Windows dengan JRE terbundle (`release/SelisihKurang-portable.zip`)

---

## Cara Pakai (End User)

### Langkah 1 — Siapkan file
1. Download file **RC** (Recon) dari sistem otomasi → format TXT
2. Download file **EJ** (E-Journal) dari mesin ATM → format TXT
3. Pastikan **periode tanggal dan record** sama antara kedua file

### Langkah 2 — Jalankan aplikasi
- **Portable:** extract `release/SelisihKurang-portable.zip` → double-click `SelisihKurang.exe`
- **Dari source:** lihat [Build dari Source](#build-dari-source)

### Langkah 3 — Rekonsiliasi
1. Klik **Browse** → pilih **File RC**
2. Klik **Browse** → pilih **File EJ**
3. Periksa/isi **Tanggal Awal**, **Tanggal Akhir**, **Record Awal**, **Record Akhir**
4. Klik tombol **Rekon**
5. Tunggu status bar menampilkan "Selesai..."

### Langkah 4 — Tinjau hasil
1. Buka tab **Match**, **ACQ**, **Nasabah Diuntungkan**, dll.
2. Buka tab **Hasil Rekonsiliasi** → isi kolom **M** dan **P** jika diperlukan
3. Buka tab **Berita Acara** → lengkapi data petugas & saldo
4. Klik **Print Berita Acara**
5. Buka tab **Report Berita Acara** → review, **Cetak (Landscape)**, atau **Simpan HTML**

### Contoh dengan data sample
Folder `samples/` berisi:
- `sample-rc.txt` — format RC asli (semicolon, `RK_*.txt`)
- `sample-ej.txt` — format EJ asli (journal `EJ.TXT`)
- `format data RC.jpeg` / `format data EJ.jpeg` — screenshot referensi format produksi

Gunakan filter: tanggal `06-12-2026` s/d `06-12-2026`, record `5780`–`5810`.

Hasil yang diharapkan: **4 Match**, **2 ACQ**, **Nasabah Diuntungkan** terdeteksi.

---

## Paket Portable (Tanpa Install Java)

File siap pakai untuk rekan yang **tidak punya Java**:

```
release/SelisihKurang-portable.zip   (~46 MB)
```

**Isi setelah extract:**
```
SelisihKurang/
├── SelisihKurang.exe      ← double-click ini
├── app/                   ← JAR aplikasi
├── runtime/               ← JRE terbundle
├── samples/               ← data contoh
└── CARA_PAKAI.txt
```

**Cara pakai:** extract → double-click `SelisihKurang.exe`. Tidak perlu install Java atau build.

> **Catatan:** Paket portable ini untuk **Windows 64-bit**. Untuk macOS/Linux, build ulang dengan `package-portable.ps1` di OS tersebut.

---

## Build dari Source

### Persyaratan
- **Java 17+** (JDK 26 sudah diuji)
- **PowerShell** (Windows) untuk script build
- **Maven 3.8+** (opsional)

### Opsi A — Script PowerShell (disarankan di Windows)

```powershell
# Compile JAR
.\build.ps1

# Jalankan
.\run.ps1

# Buat paket portable + ZIP (butuh JDK dengan jpackage)
.\package-portable.ps1
```

**Output:**
| Path | Keterangan |
|------|------------|
| `dist/selisih-kurang.jar` | JAR executable |
| `release/SelisihKurang/` | Folder portable dengan JRE |
| `release/SelisihKurang-portable.zip` | ZIP siap distribusi |

### Opsi B — Maven

```bash
mvn clean package
java -jar target/selisih-kurang.jar
```

### Opsi C — Manual javac

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.1"
& "$env:JAVA_HOME\bin\javac" -encoding UTF-8 -d build/classes (Get-ChildItem src\main\java -Recurse -Filter *.java).FullName
# ... lihat build.ps1 untuk langkah lengkap
```

---

## Format File Input (RC & EJ)

Program mendeteksi format file secara otomatis lewat `TxtFileParser` yang mendelegasikan ke parser khusus.

### Format RC — file `RK_*.txt` (Recon/ICONS)

Satu transaksi per baris, **7 field** dipisah titik koma (`;`):

```
card/bank;terminal/record/ref;detail;amount;type;balance;date
```

**Contoh baris:**
```
5264222571513795/BNI;S1BBBIR018/5780/188700;0210/S1BBBIR018/5780/ 5264222571513795/1889058327/VL;4000000;D;751800000;06/12/26
```

| Field | Isi | Keterangan |
|-------|-----|------------|
| 1 | `5264222571513795/BNI` | Nomor kartu / kode bank |
| 2 | `S1BBBIR018/5780/188700` | Terminal / **record** / referensi |
| 3 | Detail transaksi | Berisi norek 10 digit, kode VL/VK |
| 4 | `4000000` | Nominal (integer) |
| 5 | `D` atau `K` | `D` = penarikan, `K` = setoran |
| 6 | Saldo | Saldo setelah transaksi |
| 7 | `06/12/26` | Tanggal `DD/MM/YY` |

**Baris yang diabaikan:** bank `OTHR`, detail berisi `/REST/` (restocking).

Parser: `RcFileParser.java`

### Format EJ — file `EJ.TXT` (Electronic Journal)

Format **journal multi-baris**. Setiap transaksi diawali `TRANSACTION START`.

**Field penting dalam blok:**
- Timestamp: `06/12/2026 08:15:22`
- `CARD NUMBER 526422******3795` (kartu masked)
- `TRAN SEQ NR [5780]` → **record** untuk matching
- `Amount : 50000` / `TOTAL AMOUNT IDR 50000`
- Tipe: `WITHDRAWAL`/`DISPENSE` = penarikan (`D`), `Cash Deposit`/`DEPOSIT Button` = setoran (`K`)
- Flag suspect: `SUSPECT`, `FAIL`, `TIMEOUT`, `HOST DECLINE`

Parser: `EjFileParser.java`

### Matching RC ↔ EJ

- **Record** utama: angka di field 2 RC (mis. `5780`) = `TRAN SEQ NR [5780]` di EJ
- **Kartu masked:** dibandingkan 6 digit awal + 4 digit akhir (`526422` + `3795`)
- **Tipe:** `D`/`W` = penarikan, `K`/`S` = setoran

### Fallback — format tabular lama

Jika file tidak cocok format RC/EJ di atas, parser fallback mendukung delimiter tab, pipe, titik koma dengan header kolom.

### Encoding
Dicoba berurutan: **UTF-8** → **Windows-1252** → **ISO-8859-1**

### Kode parser
- `TxtFileParser.java` — router deteksi format
- `RcFileParser.java` — parse RC semicolon
- `EjFileParser.java` — parse EJ journal

---

## Alur Kerja Program (Detail)

```
┌─────────────────────────────────────────────────────────────────┐
│  SelisihKurangApp.main()                                        │
│       ↓                                                         │
│  MainFrame.launch()          ← UI utama (Swing)                 │
│       ↓                                                         │
│  User: Browse RC + EJ, set filter, klik Rekon                   │
│       ↓                                                         │
│  SwingWorker (background thread)                                │
│       ├── TxtFileParser.parse(rc)  → List<Transaction>          │
│       ├── TxtFileParser.parse(ej)  → List<Transaction>          │
│       └── ReconciliationService.reconcile() → ReconciliationResult│
│       ↓                                                         │
│  updateTables()              ← isi tab Match, ACQ, dll.         │
│  updateHasilRekonsiliasi()   ← hitung Excel A–R                 │
│  prefillBeritaAcara()        ← isi form default                 │
│       ↓                                                         │
│  User: Print Berita Acara                                         │
│       ├── BeritaAcaraPanel.collectData()                        │
│       ├── ReportGenerator.generateHtml()                        │
│       └── ReportPanel.setReportHtml()  ← preview & cetak        │
└─────────────────────────────────────────────────────────────────┘
```

### Entry point
```java
// SelisihKurangApp.java
public static void main(String[] args) {
    MainFrame.launch();
}
```

### Orkestrasi rekon
```java
// MainFrame.java → runReconciliation()
rcData = parser.parse(rcPath, Source.RC);
ejData = parser.parse(ejPath, Source.EJ);
lastResult = reconciliationService.reconcile(rcData, ejData, filter);
```

---

## Logika Rekonsiliasi

**File:** `src/main/java/com/selisihkurang/service/ReconciliationService.java`

### Tahap 1 — Filter
Hanya transaksi dalam rentang tanggal dan record yang dipilih user (`ReconciliationFilter.java`).

### Tahap 2 — Indexing (O(n))
Data diindeks di `HashMap` untuk pencarian cepat:
- **By Record** — kunci utama: nomor record
- **By Loose Key** — fallback: `tanggal|card|amount|type`

### Tahap 3 — Matching
Untuk setiap transaksi **EJ**, cari pasangan di **RC**:

```
EJ ditemukan di RC?
├── YA → amount & type cocok?
│         ├── YA → MATCH ✅
│         └── TIDAK → RC Suspect + EJ Suspect ⚠️
│         └── EJ suspect atau tidak exact?
│                   ├── YA → ICONS Terbuku EJ Suspect
│                   └── Norek terisi? → NASABAH DIUNTUNGKAN 🟡
└── TIDAK → EJ ADA RC TIDAK TERBUKU (ACQ) ❌
```

### Tahap 4 — RC yang tidak terpakai
Transaksi RC tanpa pasangan EJ → **RC Suspect**.

### Tahap 5 — Ringkasan settlement
Hitung per RC dan per EJ:
- Qty & total **Penarikan**
- Qty & total **Setoran**
- Qty & total **Suspect**
- **Selisih Periode** = setoran − penarikan

**Model hasil:** `ReconciliationResult.java`

---

## Kolom Excel Hasil Rekonsiliasi (A–R)

**File model:** `HasilRekonsiliasiData.java`  
**File service:** `HasilRekonsiliasiService.java`  
**File UI:** `HasilRekonsiliasiPanel.java`

| Kolom | Label | Sumber | Rumus |
|-------|-------|--------|-------|
| A | ID ATM | Manual / CRM ID | — |
| B | Nominal Pengisian | Manual | — |
| C | Tgl Awal | Filter rekon | — |
| D | Tgl Akhir | Filter rekon | — |
| E | Record Awal | Filter rekon | — |
| F | Record Akhir | Filter rekon | — |
| G | Sisa Menurut Admin | Form Berita Acara | — |
| H | Sisa Fisik | Form Berita Acara | — |
| I | Selisih Admin | **Otomatis** | `H − G` |
| J | Setoran SKA | Dari hasil rekon RC | — |
| K | Penarikan SKA | Dari hasil rekon RC | — |
| L | Sisa Rest ICONS | Form / auto | — |
| M | Pembukuan SR | **Manual** | — |
| N | Selisih Per Periode | **Otomatis** | `M − L` |
| O | Keterangan | **Otomatis** | N=0 → KLOP, N<0 → SELISIH KURANG, N>0 → SELISIH LEBIH |
| P | TRX by Rec Num | **Manual** | — |
| Q1 | Akumulasi | **Otomatis** | `M − P` |
| Q2 | Net | **Otomatis** | `L − P` |
| R | Status | **Otomatis** | Ada suspect → SUSPECT PENDING, else TIDAK SUSPECT PENDING |

---

## Berita Acara Settlement

**Form:** `BeritaAcaraPanel.java` + `BeritaAcaraData.java`  
**Generator:** `ReportGenerator.java`  
**Preview:** `ReportPanel.java`

### Struktur laporan (sesuai PDF)

```
┌──────────────────────────────────────────────────────────────┐
│  [BERITA ACARA SETTLEMENT CRM ID xxxxxx]     [LOGO]          │
├──────────────────────┬───────────────────────────────────────┤
│ a. NAMA CRM ID       │ c. Petugas Settlement                 │
│ b. NO. REKENING KAS  │ d. Penyelia Penunjang                 │
│                      │ e. Pengelola                          │
├──────────────────────┴───────────────────────────────────────┤
│  RESTOCKING & COLLECTING                                     │
│  f. Periode CRM  │  i. Fisik Collecting                      │
│  g. Record Awal  │  j. Saldo Pembukuan                       │
│  h. Record Akhir │  k. Saldo Billcount                       │
│                  │  l. Saldo Admin                            │
│                  │  m. Selisih Akumulatif (600.000)           │
├──────────────────────┬───────────────────────────────────────┤
│  SETTLEMENT RC       │  SETTLEMENT EJ                        │
│  Restocking          │  Restocking                           │
│  Collecting          │  Collecting                           │
│  Penarikan (QTY/Amt) │  Penarikan (QTY/Amt)                  │
│  Setoran (QTY/Amt)   │  Setoran (QTY/Amt)                    │
│  Selisih Periode     │  Selisih Periode                      │
│  EJ Suspect          │  RC Suspect                           │
├──────────────────────┼───────────────────────────────────────┤
│ TRX ICONS TERBUKU    │ TRX EJ ADA, RC TIDAK TERBUKU          │
│ DI EJ SUSPECT        │ (ACQ)                                 │
│ [tabel transaksi]    │ [tabel transaksi]                     │
└──────────────────────┴───────────────────────────────────────┘
                                          Page 1 of N
```

### Format angka
- Positif: `193.300.000`
- Negatif: `(600.000)`
- Transaksi: `RP1000000`

Implementasi: `ParseUtil.formatAmountAccounting()` dan `ParseUtil.formatRp()`

---

## Struktur Kode & File

```
java-project/
│
├── SelisihKurangApp.java          # Entry point — main()
│
├── model/                         # Struktur data (POJO / record)
│   ├── Transaction.java           # 1 baris transaksi dari RC/EJ
│   ├── Source.java                # Enum: RC | EJ
│   ├── MatchedPair.java           # Pasangan transaksi RC+EJ cocok
│   ├── ReconciliationFilter.java  # Filter tanggal & record
│   ├── ReconciliationResult.java  # Semua hasil rekon (list + summary)
│   ├── SettlementSummary.java     # Ringkasan penarikan/setoran/suspect
│   ├── BeritaAcaraData.java       # Data form Berita Acara
│   └── HasilRekonsiliasiData.java # Kolom Excel A–R + rumus
│
├── service/                       # Logika bisnis
│   ├── TxtFileParser.java         # Router parser RC/EJ/tabular
│   ├── RcFileParser.java          # Parse RK_*.txt (semicolon)
│   ├── EjFileParser.java          # Parse EJ.TXT (journal)
│   ├── ReconciliationService.java # CORE: cocokkan RC vs EJ
│   ├── HasilRekonsiliasiService.java # Gabung data → Excel A–R
│   └── ReportGenerator.java       # Generate HTML Berita Acara
│
├── ui/                            # Antarmuka Swing
│   ├── MainFrame.java             # Jendela utama, orkestrasi alur
│   ├── BeritaAcaraPanel.java       # Form input Berita Acara
│   ├── HasilRekonsiliasiPanel.java# Tampilan kolom Excel A–R
│   ├── ReportPanel.java           # Preview HTML + cetak/simpan
│   └── TransactionTableModel.java # Model tabel untuk tab hasil
│
├── util/
│   └── ParseUtil.java             # Parse/format tanggal, angka, RP
│
├── samples/                       # Data contoh untuk uji coba
│   ├── sample-rc.txt
│   └── sample-ej.txt
│
├── dist/                          # Output build
│   └── selisih-kurang.jar
│
├── release/                       # Paket portable
│   ├── SelisihKurang/
│   └── SelisihKurang-portable.zip
│
├── build.ps1                      # Script compile JAR
├── package-portable.ps1           # Script buat paket portable + ZIP
├── run.ps1                        # Script jalankan aplikasi
├── pom.xml                        # Konfigurasi Maven
└── SELISIH KURANG.pdf             # Dokumen spesifikasi asli
```

### File mana yang diedit untuk perubahan tertentu?

| Ingin ubah... | Edit file |
|---------------|-----------|
| Format TXT produksi bank | `RcFileParser.java`, `EjFileParser.java`, `TxtFileParser.java` |
| Aturan Match / ACQ / Nasabah Diuntungkan | `ReconciliationService.java` |
| Tampilan tab / tombol / alur UI | `MainFrame.java` |
| Rumus Excel A–R | `HasilRekonsiliasiData.java` |
| Layout cetak Berita Acara | `ReportGenerator.java` |
| Form input petugas/saldo | `BeritaAcaraPanel.java` |
| Format angka/tanggal | `ParseUtil.java` |

---

## Arsitektur Teknis

| Aspek | Pilihan |
|-------|---------|
| Bahasa | Java 17+ |
| UI Framework | Java Swing |
| Build | PowerShell script + Maven (opsional) |
| Packaging | `jpackage` (portable Windows + JRE) |
| Dependency | Zero external runtime dependency (pure JDK) |
| Threading | `SwingWorker` untuk proses rekon di background |
| Data structure | `HashMap` indexing untuk matching O(n) |
| Output laporan | HTML + CSS print (@page landscape) |

### Diagram komponen

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────────────┐
│  UI Layer   │────▶│  Service Layer   │────▶│  Model Layer        │
│  MainFrame  │     │  TxtFileParser   │     │  Transaction        │
│  Panels     │     │  Reconciliation  │     │  ReconciliationResult│
│  TableModel │     │  ReportGenerator │     │  BeritaAcaraData    │
└─────────────┘     └──────────────────┘     └─────────────────────┘
                            │
                            ▼
                    ┌──────────────────┐
                    │  Util Layer      │
                    │  ParseUtil       │
                    └──────────────────┘
```

---

## Testing

### Unit test
```powershell
# Butuh Maven
mvn test
```

File test: `src/test/java/com/selisihkurang/service/ReconciliationServiceTest.java`

### Manual test
```powershell
javac -encoding UTF-8 -d build/test-classes -cp build/classes src/test/java/com/selisihkurang/ManualTest.java
java -cp "build/classes;build/test-classes" com.selisihkurang.ManualTest
```

### Preview laporan HTML
```powershell
java -cp "build/classes;build/test-classes" com.selisihkurang.ReportPreviewTest
# Output: build/report-preview.html
```

### Hasil uji sample data
| Metrik | Nilai |
|--------|-------|
| Match | 4 transaksi |
| ACQ | 2 transaksi (record 6500, 6510) |
| Nasabah Diuntungkan | 1 transaksi (record 6490, Norek terisi) |

---

## Data yang Perlu Diminta ke Rekan

Agar software **100% akurat** dengan data produksi, minta rekan kirim:

### Wajib
1. **1 file RC/Recon TXT asli** (1 periode, boleh disamarkan)
2. **1 file EJ TXT** periode yang sama
3. **Jawaban rekon manual** periode itu: jumlah Match, ACQ, Nasabah Diuntungkan (record + norek)

### Sangat membantu
4. Excel **Hasil Rekonsiliasi** 1 baris terisi (kolom A–R)
5. **1 contoh Berita Acara** yang sudah pernah dicetak (PDF)
6. Cara download RC & EJ (menu sistem + format delimiter)

### Opsional
7. Daftar kode **Type** transaksi (W, S, K, R, dll.)
8. Aturan kapan transaksi dianggap **Suspect**

---

## Keterbatasan Saat Ini

| Area | Status |
|------|--------|
| Vendor Kurang Setor | ❌ Belum diimplementasi |
| Format TXT produksi bank | ✅ Parser RC (`RK_*.txt`) & EJ (`EJ.TXT`) sesuai format produksi |
| Logo perusahaan di laporan | ❌ Kotak kosong (belum ada gambar logo) |
| Import Excel langsung | ❌ Belum — input via form aplikasi |
| Download otomatis RC/EJ | ❌ Belum — file dipilih manual (Browse) |
| macOS/Linux portable | ⚠️ Perlu build ulang di OS tersebut |

---

## Roadmap / Pengembangan Lanjutan

- [x] Sesuaikan parser dengan format TXT produksi bank (RC semicolon + EJ journal)
- [ ] Validasi hasil vs Excel manual rekan (regression test)
- [ ] Tambah logo perusahaan di `ReportGenerator`
- [ ] Export PDF native (selain HTML print)
- [ ] Modul Vendor Kurang Setor
- [ ] Import template Excel A–R
- [ ] GitHub Actions CI (build + test otomatis)

---

## Lisensi & Kontak

Proyek internal rekonsiliasi Selisih Kurang.  
Dokumen referensi: `SELISIH KURANG.pdf`

**Maintainer:** [suhaelr](https://github.com/suhaelr)
