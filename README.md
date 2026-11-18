# Ampere

[![build](https://github.com/aeeL211/Ampere/actions/workflows/build.yml/badge.svg)](https://github.com/aeeL211/Ampere/actions/workflows/build.yml)

Meteran arus baterai Android yang sengaja dibuat sekecil mungkin.

Ampere menampilkan **arus masuk**, **arus keluar**, dan **arus bersih** (`masuk − keluar`)
langsung dari sensor baterai. Tidak ada permission, iklan, analytics, database, atau
library pihak ketiga.

## Fitur

- Arus masuk dan keluar dalam mA, diperbarui setiap detik.
- Arus bersih dengan tanda `+` (mengisi) atau `−` (mengosongkan).
- Persentase baterai dan status pengisian.
- UI Material 3 yang digambar langsung ke `Canvas` — tanpa Material Components.
- Mendukung mode gelap, edge-to-edge, font scaling, dan TalkBack.
- Tanpa dependency runtime sama sekali. Satu activity, satu custom view.

## Kenapa kecil?

Sensor baterai Android hanya memberi **arus bersih** (`BATTERY_PROPERTY_CURRENT_NOW`).
Karena itu:

```text
in  = max(net, 0)
out = max(-net, 0)
in - out = net
```

Pada satu waktu hanya satu arah yang bernilai bukan nol. Ini memang sifat sensor
baterai, bukan bug. Aplikasi memakai API framework saja, jadi APK rilis dapat
di-shrink oleh R8 sampai ukuran puluhan KB.

Ukuran build terakhir: **±63 KB** untuk APK debug yang bisa langsung dipasang, dan **±16 KB** untuk APK release yang sudah di-minify (unsigned). APK debug tersedia sebagai artifact dari setiap build di tab **Actions**.

## Build

Semua build berat dijalankan oleh GitHub Actions (`.github/workflows/build.yml`) agar
tidak membebani perangkat lokal.

Persyaratan lokal jika ingin build sendiri:

- JDK 21
- Android SDK Platform 37.0 dan Build Tools 37.0.0

```bash
sdkmanager --install "platforms;android-37.0" "build-tools;37.0.0"
./gradlew assembleDebug
./gradlew assembleRelease   # unsigned, sudah di-minify dan di-shrink
```

## Versi build

| Komponen | Versi |
| --- | --- |
| Java | 21 |
| Gradle | 9.9.1 |
| Android Gradle Plugin | 9.5.1 |
| compileSdk / targetSdk | 37 |
| Build Tools | 37.0.0 |
| minSdk | 23 |

## Cara kerja

`BatteryManager.getIntProperty(BATTERY_PROPERTY_CURRENT_NOW)` mengembalikan arus bersih
dalam mikroampere. Nilai positif berarti arus masuk ke baterai, negatif berarti keluar.
Beberapa perangkat mengembalikan miliampere atau membalik tanda; `BatteryReader`
menormalkan keduanya berdasarkan status pengisian dan status colokan.

Tidak ada foreground service dan tidak ada polling di background. Pembaruan berhenti
saat activity di-pause.

## Lisensi

Kode: [MIT](LICENSE).

Ikon launcher diturunkan dari Material Symbols `battery_charging_full` (Apache-2.0),
lihat [NOTICE](NOTICE).
