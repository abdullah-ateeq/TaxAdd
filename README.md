# TaxAdd 🧮

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9%2B-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2026%2B-green.svg?logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material%203-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**TaxAdd** is a modern, intuitive Android application designed for quick, accurate Tax and VAT calculations. Built entirely with Kotlin, Jetpack Compose, and Material Design 3, TaxAdd provides a seamless calculation experience with history tracking, customizable currency options, and flexible user settings.

---

## ✨ Features

- 🧮 **Tax & VAT Calculation**:
  - Calculate tax addition or tax extraction (reverse calculation) with real-time validation.
  - Detailed breakdown showing Base Amount, Tax Amount, and Final Total.
- ⚡ **Quick Presets**:
  - One-tap selection for base amount and common tax rate presets.
- 💱 **Multi-Currency & Custom Symbols**:
  - Built-in support for major currencies (`$`, `€`, `£`, `¥`, `₹`, etc.).
  - Ability to add and select custom currency symbols.
- 📜 **Calculation History**:
  - Automatically or manually save calculations to history.
  - Restore past calculations directly to the home screen.
  - Swipe/button to delete individual history entries or clear history completely.
- 🎨 **Material 3 & Personalization**:
  - Dynamic Material You colors on supported Android versions.
  - Full support for Dark, Light, and System Default themes.
  - Haptic feedback and smooth UI transitions (with toggle settings).
- ⚙️ **Comprehensive Settings**:
  - Configure default tax rates and currencies.
  - Option to preserve the last calculation session upon relaunch.
  - Configurable history deletion safeguards.

---

## 🛠 Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel) pattern with StateFlow and Unidirectional Data Flow (UDF)
- **Navigation**: Navigation Compose
- **Persistence**: Lightweight SharedPreferences-backed repositories for settings and history
- **Asynchronous Processing**: Kotlin Coroutines & Flow
- **Testing**: Unit tests and Compose UI instrumentation tests (`TaxAddComprehensiveTest`)

---

## 📂 Project Structure

```text
com.example.taxadd
├── data/           # Custom storage utilities (InMemorySharedPreferences)
├── domain/         # Core business logic (Calculators, Formatters, Parsers, Validators)
├── model/          # State models, UI states, settings, and domain data classes
├── repository/     # Repositories for History and App Settings
├── ui/             # Composable UI screens, components, and ViewModel
│   ├── home/       # Main Calculator Screen
│   ├── history/    # History List Screen
│   ├── settings/   # Application Settings Screen
│   ├── about/      # App Info & About Screen
│   ├── splash/     # Splash Screen
│   └── theme/      # Color palettes, typography, and Material 3 theme setups
└── util/           # Helpers for Haptics, Clipboard, and Sharing
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Android Studio Ladybug (2024.2.1) or newer recommended
- **JDK**: Version 11 or higher
- **Android SDK**: Minimum API 26 (Android 8.0) | Target API 37

### Building from Source

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/TaxAdd.git
   cd TaxAdd
   ```

2. **Build the project using Gradle**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

---

## 📱 Screenshots & Screens

- **Home Screen**: Main calculator interface with base amount, tax rate sliders/presets, and calculation results.
- **History Screen**: Chronological log of previous calculations with restore and delete options.
- **Settings Screen**: Theme selection, dynamic color toggles, haptics, default values, and data management.
- **About Screen**: App info, build version, and project overview.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
