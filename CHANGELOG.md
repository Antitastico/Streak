# Changelog

All notable changes to **Streak** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Project setup: Android app scaffold with Kotlin, Jetpack Compose and Material 3.
- `MainActivity` with edge-to-edge enabled, wrapping the UI in `StreakTheme`.
- Material 3 theme with dynamic color support (Android 12+) and light/dark
  baseline color schemes.
- `Habit` data class and an initial in-memory sample list (`defaultHabits`).
- `StreakState` state holder (the app "brain"): holds the habit list and the
  selected habit, with `select`, `toggleToday` and `addHabit` actions.
- Multi-habit home: a single focused habit (emoji, streak, correct `día`/`días`
  plural) with a check-in button that toggles today's completion.
- Bottom sheet (`BottomSheetScaffold`) listing all habits to switch between them,
  plus an "Add habit" dialog (name + emoji).

### Changed
- `HomeScreen` reworked from a single counter into a focused-habit view backed by
  `StreakState`, replacing the earlier "Entrené hoy" prototype.

### Notes
- State is still in memory only; it resets when the app closes. Persistence
  (Room) and the Modern/Minimal interface styles are planned next.

[Unreleased]: https://github.com/Antitastico/Streak/commits/main
