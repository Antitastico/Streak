# Changelog

All notable changes to **Streak** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Project setup: Android app scaffold with Kotlin, Jetpack Compose and Material 3.
- `MainActivity` with edge-to-edge enabled, wrapping the UI in `StreakTheme`.
- `HomeScreen` composable: a centered streak counter (`"$streak días"`) with an
  "Entrené hoy" Material 3 button that increments the count.
- Material 3 theme with dynamic color support (Android 12+) and light/dark
  baseline color schemes.

### Notes
- Streak state is currently held in memory only; it resets when the app closes.
  Persistence (Room) is planned.

[Unreleased]: https://github.com/Antitastico/Streak/commits/main
