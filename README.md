# 🍽️ MealPlan — Aplikasi Resep & Perencana Menu

MealPlan adalah aplikasi Android (Java) untuk **mencari resep masakan, menyimpan favorit, merencanakan menu mingguan, dan membuat daftar belanja otomatis**. Data resep diambil secara online dari **[TheMealDB API](https://www.themealdb.com/api.php)**, lalu disimpan secara lokal agar tetap bisa dibuka saat offline. Aplikasi mendukung **mode terang & gelap**.

> Dibuat untuk Final Lab Pemrograman Mobile.

---

## ✨ Fitur Utama

- 🔍 **Pencarian resep** dengan riwayat pencarian terakhir (recent search)
- 🗂️ **Kategori & resep acak** dari TheMealDB
- 📄 **Detail resep** lengkap: gambar, bahan, langkah memasak, tautan video YouTube, dan tombol bagikan
- ❤️ **Favorit** — simpan resep agar bisa dibuka kapan saja (termasuk offline)
- 📅 **Perencana menu mingguan** (Senin–Minggu)
- 🛒 **Daftar belanja otomatis** yang dibuat dari bahan resep favorit/planner
- 🌗 **Tema terang & gelap** yang bisa diganti pengguna
- 🔄 **Tombol coba lagi** saat gagal memuat data (mis. tidak ada koneksi)

---

## 🛠️ Teknologi yang Digunakan

| Kategori          | Teknologi                                               |
| ----------------- | ------------------------------------------------------- |
| Bahasa            | Java                                                    |
| Arsitektur UI     | Activity + Fragment + Navigation Component              |
| Networking        | Retrofit 2.11 + Gson                                    |
| Gambar            | Glide 4.16                                              |
| Penyimpanan lokal | SQLite (SQLiteOpenHelper) + SharedPreferences           |
| UI Components     | Material Components, RecyclerView, ViewPager2, CardView |
| Async             | Executor & Handler                                      |
| API               | [TheMealDB](https://www.themealdb.com/)                 |

---

## ✅ Pemenuhan Spesifikasi Teknis

Berikut bagaimana setiap syarat teknis diterapkan di aplikasi:

### 1. Activity (minimal 2, salah satu Launcher)

Aplikasi memiliki **5 Activity**: `SplashActivity` (Launcher), `MainActivity`, `DetailActivity`, `SearchActivity`, `GroceryActivity`. `SplashActivity` ditandai sebagai launcher pada `AndroidManifest.xml`, lalu mengarahkan ke `MainActivity`.

### 2. Intent

Navigasi antar-Activity menggunakan **explicit Intent** beserta pengiriman data (`putExtra`), contoh:

- `SplashActivity` → `MainActivity`
- `MainActivity`/Home → `SearchActivity` & `DetailActivity` (mengirim ID & data resep)
- `DetailActivity` → membuka video YouTube (`ACTION_VIEW`) & berbagi resep (`ACTION_SEND`)

### 3. RecyclerView

Digunakan di banyak layar untuk menampilkan daftar data, dengan adapter terpisah: `MealAdapter`, `CategoryAdapter`, `FavoriteAdapter`, `PlannerAdapter`, `GroceryAdapter`, `IngredientAdapter`, dan `RecentSearchAdapter`.

### 4. Fragment & Navigation Component

Terdapat **4 Fragment utama** — `HomeFragment`, `PlannerFragment`, `FavoritesFragment`, `GroceryFragment` — yang dikelola dengan **Navigation Component** (`nav_graph.xml` + `NavHostFragment` + `BottomNavigationView`). `DetailActivity` juga memakai Fragment tab (bahan & instruksi) via ViewPager2.

### 5. Background Thread

Operasi berat (akses database & proses data) dijalankan di luar UI thread menggunakan **`Executors.newSingleThreadExecutor()`** dan **`Handler`** (mis. delay splash screen & debounce pencarian).

### 6. Networking

Data resep diambil dari **TheMealDB API** memakai **Retrofit + Gson** (`ApiClient`, `MealApiService`). API sesuai tema aplikasi (Food & Drink). Bila gagal memuat (mis. tidak ada jaringan), `HomeFragment` menampilkan **tampilan error + tombol coba lagi (retry)**. Status koneksi dicek lewat `NetworkUtils`.

### 7. Local Data Persistent + Tema

- **SQLite** (`DatabaseHelper`, `FavoriteDao`, `PlannerDao`) untuk tabel `favorites` & `planner` → data favorit/planner tetap bisa dibuka **offline**.
- **SharedPreferences** untuk menyimpan riwayat pencarian & preferensi tema.
- **Dua tema**: terang (`values/themes.xml`) & gelap (`values-night/themes.xml`), diatur lewat `ThemeUtils`.

---

## 📂 Struktur Project

app/src/main/java/com/example/mealplan/
├── activity/ # Splash, Main, Detail, Search, Grocery
├── adapter/ # Adapter RecyclerView
├── database/ # DatabaseHelper, FavoriteDao, PlannerDao
├── fragment/ # Home, Planner, Favorites, Grocery (+ tab detail)
├── model/ # Model data (Meal, Category, dll.)
├── network/ # ApiClient, MealApiService
└── utils/ # Constants, NetworkUtils, ThemeUtils

---

## 🚀 Cara Menjalankan

1. **Clone** repository ini:
   git clone <https://github.com/hanifahath/MealPlan>
2. Buka project di **Android Studio**.
3. Tunggu proses **Gradle Sync** selesai.
4. Jalankan di **emulator** atau **perangkat fisik** (minimal Android 7.0 / API 24).
5. Pastikan perangkat **terhubung internet** saat pertama kali memuat resep.

> **Catatan build:** project ini menggunakan Android Gradle Plugin & `compileSdk` versi terbaru. Jika gagal sync, sesuaikan versi AGP/`compileSdk` dengan Android Studio yang dipakai.

---

## 📡 Kredit API

Data resep berasal dari **[TheMealDB](https://www.themealdb.com/)** — API publik gratis untuk resep makanan & minuman. Konten resep (nama, bahan, instruksi) berbahasa Inggris sesuai sumber aslinya.

---

## 👤 Penulis

Tugas Final Lab Pemrograman Mobile — dikerjakan secara individu.
