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
- Parser fleksibel: delimiter tab, pipe (`|`), titik koma (`;`)

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
- `sample-rc.txt` — 6 transaksi contoh RC
- `sample-ej.txt` — 6 transaksi contoh EJ

Gunakan filter: tanggal `03-05-2026` s/d `10-05-2026`, record `6480`–`6510`.

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

### Delimiter yang didukung
Parser otomatis mendeteksi:
- Tab (`\t`) — paling umum
- Pipe (`|`)
- Titik koma (`;`)
- Koma (`,`) — jika tidak ada delimiter lain

### Encoding
Dicoba berurutan: **UTF-8** → **Windows-1252** → **ISO-8859-1**

### Header
Baris pertama boleh berisi nama kolom. Jika tidak ada header, parser mengasumsikan urutan:

```
Tanggal | Card | Norek | Record | Type | Amount
```

### Kolom yang dikenali (case-insensitive)

| Field | Nama kolom yang dikenali |
|-------|--------------------------|
| Tanggal | `tanggal`, `date`, `tgl`, `trxdate`, `transactiondate` |
| Card | `card`, `nokartu`, `kartu`, `cardno`, `pan` |
| Norek | `norek`, `norekening`, `rekening`, `account`, `accountno` |
| Record | `record`, `recnum`, `norecord`, `recordno`, `seq`, `sequenceno` |
| Type | `type`, `jenis`, `rectype`, `trxtype`, `kode` |
| Amount | `amount`, `nominal`, `jumlah`, `nilai`, `amt` |
| Suspect | `suspect`, `status`, `keterangan`, `flag` |

### Contoh file valid

```text
Tanggal	Card	Norek	Record	Type	Amount	Status
03/05/2026	6019001234567890	0015360100001234	6480	W	500000
03/05/2026	6019009876543210		6481	W	1000000	SUSPECT
```

### Kode parser
Implementasi: `src/main/java/com/selisihkurang/service/TxtFileParser.java`

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
│   ├── TxtFileParser.java         # Baca & parse file TXT → Transaction
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
| Format baca TXT produksi bank | `TxtFileParser.java` |
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
| Format TXT produksi bank | ⚠️ Parser generik — perlu penyesuaian setelah uji file asli |
| Logo perusahaan di laporan | ❌ Kotak kosong (belum ada gambar logo) |
| Import Excel langsung | ❌ Belum — input via form aplikasi |
| Download otomatis RC/EJ | ❌ Belum — file dipilih manual (Browse) |
| macOS/Linux portable | ⚠️ Perlu build ulang di OS tersebut |

---

## Roadmap / Pengembangan Lanjutan

- [ ] Sesuaikan `TxtFileParser` dengan format TXT produksi bank
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
