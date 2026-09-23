# Bus Ease (বাস ইজ) - Dhaka Transit & Bus Guide

**Bus Ease** is a modern, responsive Android application designed to make navigating Dhaka's urban bus transit effortless for daily commuters, students, and visitors. It provides comprehensive route directories, official BRTA fare calculation, GPS-enabled nearest stop detection within 1 km, interactive maps, and bilingual support (English & বাংলা).

---

## ✨ Features

### 1. 🔍 Route Search & Connecting Buses
- Search connecting bus routes between any two stops across Dhaka (e.g., *Mirpur 10* to *Motijheel*, *Uttara* to *Gulistan*).
- Instant stop auto-complete with suggestions as you type.
- One-tap origin/destination stop swap button with real-time route filtering.
- One-tap **GPS Nearest Stop Detection** to automatically set your current departure point.

### 2. 📍 1 km Radius "Nearby Buses" Locator
- Real-time GPS location scan identifying transit hubs and bus stops within a **1 km radius** of your location.
- Lists all buses servicing stops nearby along with walking distance in meters (~m).
- Interactive filter chips by specific local stoppage (e.g., Farmgate, Khamarbari, Ananda Cinema).
- Quick transit hub presets for major Dhaka intersections (Farmgate, Shahbag, Mohakhali, Motijheel, Dhanmondi, Uttara).

### 3. 🚌 Bus Name & Fleet Directory
- Browse 140+ Dhaka metropolitan bus services (e.g., Bikolpo, Shikhor, প্রজাপতি, বিহঙ্গ, Victor Classic, Raida).
- Filter by service type (Local, Seating Service, Semi-Seating, AC, BRTC).
- Search in both English and Bangla script.

### 4. 🗺️ Detailed Route View & Dual-Map Visualizer
- **Native Vector Route Map**: Fast visual waypoint connectivity diagram.
- **Google Maps Integration**: Displays geographic waypoints and markers with interactive camera controls.
- Interactive timeline with start terminals, intermediate stoppage badges, and destination indicators.
- **Forward & Reverse Direction Toggle**: Switch between forward and return trip views.

### 5. 💰 BRTA Official Fare & Route Fare Estimator
- **Dedicated Fare Search (`Kotobara`)**: Search authorized government BRTA fares between city stops bidirectionally.
- **In-Route Fare Calculator Tool**:
  - Select pickup and drop-off stops along any specific bus route.
  - Automatically checks against official BRTA fare databases or calculates distance-based fares based on Dhaka's official rate (~৳2.45/km with minimum ৳10 base fare).
  - Displays intermediate stop counts, cumulative distance (~km), and student **50% Half-Pass** fare.

### 6. ⭐ Favorites & Offline Bookmarks
- Bookmark frequently used bus routes for fast offline access.
- Stored locally using **Room Database** (`FavoriteBusEntity`).

### 7. 🌐 Bilingual & Customization
- Full language support for **Bangla (বাংলা)** and **English**.
- Clean Material 3 UI with adaptive cards, smooth animations, and edge-to-edge layout.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 2.x
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM / Clean Repository Pattern
- **Local Persistence:** Room Database (Room KSP)
- **Coroutines & Flow:** Kotlin Coroutines & StateFlow for reactive UI state management
- **Geospatial & Coordinates:** Custom coordinate engine (`DhakaTransitCoordinates`) with Haversine distance computations
- **Image Loading:** Coil Compose
- **Networking/Maps:** Google Maps Compose & Play Services Location

---

## 📱 App Structure

```
com.example.busease/
├── MainActivity.kt                # Main entry point & bottom navigation
├── data/
│   ├── AppSettings.kt             # Preferences (Language, Themes)
│   ├── AppStrings.kt              # Bilingual localization strings (EN / BN)
│   ├── local/
│   │   ├── AppDatabase.kt         # Room database
│   │   ├── FavoriteBusDao.kt      # DAO for saved routes
│   │   └── FavoriteBusEntity.kt   # Favorite bus entity
│   ├── model/
│   │   ├── BusRoute.kt            # Bus route model
│   │   └── FareItem.kt            # BRTA fare item model
│   ├── repository/
│   │   └── BusRepository.kt       # Bus routes, fare lookup, & nearby stop queries
│   └── util/
│       ├── DhakaTransitCoordinates.kt  # Dhaka transit coordinates database
│       └── LocationHelper.kt      # FusedLocationProviderClient wrapper
└── ui/
    ├── components/                # Reusable UI components (BusCard, AutocompleteStopInput, etc.)
    ├── screens/                   # App screens
    │   ├── BusDetailsScreen.kt    # Route waypoints, maps & fare estimator
    │   ├── BusFareSearchScreen.kt # BRTA fare calculation screen
    │   ├── BusNameSearchScreen.kt # Bus fleet search screen
    │   ├── FavoriteBusesScreen.kt # Saved favorite routes
    │   ├── NearbyBusesScreen.kt   # 1km radius GPS locator
    │   ├── RouteSearchScreen.kt   # Point A to B route finder
    │   └── SettingsScreen.kt      # Language & app settings
    └── theme/                     # Material 3 Color Schemes & Typography
```

---

## 🚀 Getting Started

1. Clone or import the project repository into Android Studio.
2. Ensure Android SDK 34+ and Kotlin Gradle Plugin are configured.
3. Build the project:
   ```bash
   gradle assembleDebug
   ```
4. Run on an Android device or emulator with Location permissions enabled for GPS features.
