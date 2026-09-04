<h1 align="center">🔥 Streak</h1>

<p align="center"><em>Build consistency — one day at a time.</em></p>

<p align="center">
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white">
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4">
  <img alt="minSdk" src="https://img.shields.io/badge/minSdk-26-blue">
  <img alt="License" src="https://img.shields.io/badge/license-see%20LICENSE-lightgrey">
</p>

---

**Streak** is a minimalist Android app for tracking daily habits.
No accounts. No ads. No cloud. Just your streak.

## Philosophy

Streak isn't about motivation — it's about **consistency**.
One habit in focus, one tap a day. The home screen never becomes a cluttered
to‑do list: your habits live one swipe away.

## Features

- ✅ **Daily check-in** with an automatic streak counter
- 🎯 **One focused habit** on the home screen — calm by design
- 📿 **Swipe-up sheet** to switch habits or add a new one
- 🎨 **Two switchable styles** — *Modern* (Material 3, color, emoji) and
  *Minimal* (black & white, monograms)
- 📴 **Offline-first** — your data stays on your device
- 🆓 **Open source**

## Roadmap

- [x] Project setup (Kotlin · Compose · Material 3)
- [x] Home screen with streak counter
- [x] Multiple habits + bottom sheet + add-habit dialog
- [x] Modern / Minimal interface styles
- [x] Local persistence (JSON on device)
- [x] Calendar view
- [x] Statistics & consistency charts
- [ ] Room-backed storage
- [ ] Release v1.0

## Tech stack

| Layer | Choice |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (state holder → ViewModel) |
| Persistence | Local JSON file *(Room planned)* |
| Dates & stats | `java.time` + pure Kotlin |
| Min / Target SDK | 26 / 36 |

## Download

Grab the latest debug APK from the
**[Releases](https://github.com/Antitastico/Streak/releases)** page and open it
on your Android device. You may need to allow *"Install unknown apps"* for your
browser or file manager.

## Build & run

Clone the repository and open it in Android Studio, let Gradle sync, then press
**Run**. To build a debug APK from the command line:

    ./gradlew assembleDebug

The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Project structure

    app/src/main/java/io/github/antitastico/streak/
    ├── MainActivity.kt      # Entry point
    ├── StreakApp.kt         # Theme + bottom navigation
    ├── StreakState.kt       # State holder: habits, selection, style
    ├── Habit.kt             # Habit model + UiStyle enum
    ├── HabitStats.kt        # Pure functions: streaks, consistency, charts
    ├── HabitStore.kt        # Local JSON persistence
    └── ui/theme/
        ├── HomeScreen.kt     # Home + habit sheet + add dialog
        ├── CalendarScreen.kt # Monthly calendar
        ├── StatsScreen.kt    # Stats, bar chart & heatmap
        ├── Theme.kt          # Modern / Minimal themes
        ├── Color.kt          # Color palette
        └── Type.kt           # Typography

## License

Released under the terms described in [LICENSE.md](LICENSE.md).
