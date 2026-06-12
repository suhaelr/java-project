# Selisih Kurang - Rekonsiliasi RC vs EJ

Aplikasi desktop Java untuk rekonsiliasi data RC (Recon/ICONS) dan EJ (E-Journal) guna mendeteksi:

- **Nasabah Diuntungkan** — transaksi terbuku di ICONS, suspect di EJ, dengan Norek terisi
- **ACQ Kurang Posting** — transaksi ada di EJ tetapi tidak terbuku di RC

## Untuk rekan (tanpa install / tanpa build)

Kirim file ini ke rekan:

**`release/SelisihKurang-portable.zip`**

Cara pakai di laptop rekan:
1. Extract ZIP ke folder mana saja
2. Double-click **`SelisihKurang.exe`**
3. Selesai — tidak perlu install Java, tidak perlu build

Di dalam paket ada folder `samples/` (data contoh) dan `CARA_PAKAI.txt`.

## Untuk developer (build paket portable)

```powershell
.\package-portable.ps1
```

Script ini compile + bundle JRE ke `release/SelisihKurang/` dan membuat ZIP siap kirim.

## Jalankan dari source (butuh Java terinstall)

```powershell
.\build.ps1
.\run.ps1
```

## Build dengan Maven (jika tersedia)

```bash
mvn clean package
java -jar target/selisih-kurang.jar
```

## Format File TXT

File RC dan EJ harus berformat teks dengan delimiter tab (`|`), pipe (`|`), atau titik koma (`;`).

Baris pertama boleh berisi header. Kolom yang dikenali (case-insensitive):

| Kolom | Alias |
|-------|-------|
| Tanggal | date, tgl |
| Card | nokartu, kartu, pan |
| Norek | norekening, rekening, account |
| Record | recnum, norecord, seq |
| Type | jenis, rectype, trxtype |
| Amount | nominal, jumlah, nilai |
| Status | suspect, flag, keterangan |

Contoh sample ada di folder `samples/`.

## Alur Penggunaan

1. Pilih **File RC** dan **File EJ** (Browse)
2. Isi **Tanggal Awal/Akhir** dan **Record Awal/Akhir** (auto-detect saat browse)
3. Klik **Rekon**
4. Lihat hasil di tab Match, EJ Suspect, RC Suspect, Nasabah Diuntungkan, ACQ
5. Tab **Hasil Rekonsiliasi** — lihat kolom Excel A–R (isi manual M & P jika perlu)
6. Tab **Berita Acara** → **Print Berita Acara**
7. Tab **Report Berita Acara** — preview format cetak resmi, cetak landscape, atau simpan HTML

## Cross-Platform

Aplikasi menggunakan Java Swing dan berjalan di Windows, macOS, dan Linux tanpa modifikasi.
