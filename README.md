# Divinity — Midmines Fork

Ini adalah fork pribadi Divinity untuk eksperimen dan pengembangan oleh
[midmines](https://github.com/midmines). Repository ini berasal dari
[Divinity resmi](https://github.com/magemonkeystudio/divinity), tetapi perubahan
di fork ini tidak otomatis menjadi bagian dari proyek resmi dan tidak didukung
oleh maintainer resmi.

Fork ini ditujukan untuk penggunaan dan pengembangan mandiri. Perubahan dapat
berbeda atau tertinggal dari branch resmi.

## Perbedaan dan perubahan

### Fitur baru

- **Menu `/stats`** — menampilkan statistik aktif pemain dalam GUI dengan
  halaman, nilai dasar/bonus/total, dan deskripsi. Pengaturan tersedia di
  `src/main/resources/stats/settings.yml`; nama, ikon, format, urutan, dan
  tampilan stat dapat dikustomisasi.
- **Filter item group per stat generator** — stat item dapat dibatasi ke item
  group atau subtype tertentu langsung dari YAML generator. Contohnya:

  ```yml
  critical_rate:
    chance: 20.0
    item-groups:
      - sword
      - axe
    scale-by-level: 1.025
    min: 3.0
    max: 6.25
    flat-range: false
  ```

  Opsi ini berlaku untuk item stat di bawah
  `generator.item-stats.list.<stat>`. Hapus `item-groups` jika stat boleh
  muncul untuk semua item. Nilai group harus sesuai group/subtype yang dikenali
  konfigurasi Divinity.
- **Peluang soulbound pada item generator** — opsi `generator.soulbound-chance`
  mengatur peluang item yang dibuat menjadi soulbound dalam rentang 0–100.
- **Dukungan DecentHolograms** untuk indikator damage combat log, selain
  HolographicDisplays.

### Perbaikan dan penyesuaian

- Jarak pencarian target AoE memakai pengaturan combat AoE, bukan jarak tetap.
- Pembaruan durability dapat menyimpan nilai meskipun lore durability tidak
  ditampilkan.
- Pengecekan apakah stat sudah diterapkan juga memeriksa data item, bukan hanya
  baris lore. Ini mendukung stat yang sengaja disimpan tanpa ditampilkan.
- Generator tipe hand tidak menambahkan lore hand pada item yang bukan senjata.

## Pengembangan

Build dan jalankan tes:

```bash
mvn test
```

Fork GitHub: https://github.com/midmines/divinity_midmines_fork

## Upstream

Proyek resmi: https://github.com/magemonkeystudio/divinity

Untuk isu atau perubahan di fork ini, gunakan repository Midmines Fork; jangan
menganggap perubahan di sini sebagai rilis atau rekomendasi resmi Divinity.
