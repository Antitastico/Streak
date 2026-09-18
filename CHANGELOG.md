# Changelog

All notable changes to **Streak** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.5.0] - 2026-09-18

### Added
- **Home-screen widget** (Jetpack Glance) in three sizes: small (mark today done),
  medium (a minimalist dot calendar of completed days — filled = done), and large
  (a mini interactive app with streak, calendar and check-in).
- **Nightly reminder at 22:00** (urgent channel) with actions to mark whether you
  did the habit: *"Sí, lo hice"* / *"Hoy no"* — straight from the notification.
- **Congratulation messages** on streak milestones, and **encouraging messages**
  after a streak is broken.
- **Sound effect** when you check in.
- **Automatic local backup**: atomic writes plus a backup copy and restore-on-
  corruption, so your data survives updates.
- **New minimalist app icon** (a white flame), including a themed-icon (monochrome)
  variant for Android 13+.

### Changed
- **Lighter, more delicate typography** (thin/light weights, never italic).
- **Removed the monogram initials** next to the habit name (hyper-minimalist).
- The app reloads its data on resume, so a check-in from the widget or the
  notification shows up immediately.

## [0.4.0] - 2026-09-04

### Added
- First-run onboarding: enter your name and pick habits from a catalog or create
  your own.
- Style toggle as a minimalist corner symbol (◐); the app now defaults to the
  Minimal style.
- Default habit: mark a habit with a star to choose which one opens on launch.
- Tapping the streak number opens a centered overlay window with the calendar and
  statistics, without leaving the home screen.

### Changed
- Removed the bottom navigation bar; the home screen is now the single main view.
- Habit switching moved to a modal sheet opened from the habit name.
- Persist the user's name, the chosen style and the default habit.

## [0.3.0] - 2026-09-04

First feature-complete preview.

### Added
- Kotlin + Jetpack Compose + Material 3 app with edge-to-edge UI.
- `Habit` model that stores the **set of check-in dates**; streaks and statistics
  are derived from those dates.
- **Focused-habit home screen**: one habit in view with a large streak counter
  and a check-in button (correct `día`/`días` plural).
- **Habit switcher & creator** in a modal bottom sheet (open by tapping the habit
  name); create a habit with a name and an emoji.
- **Bottom navigation** with three sections: Home, Calendar and Statistics.
- **Calendar** screen: monthly view of the focused habit with completed days
  highlighted, today ringed, and month navigation.
- **Statistics** screen: current streak, longest streak, 30-day consistency and
  total check-ins, plus a weekly bar chart and a 35-day heatmap.
- **Switchable interface style** (`UiStyle` MODERN / MINIMAL): MINIMAL uses a
  black-and-white theme, monogram icons and outlined buttons.
- **Local persistence**: habits, check-in dates and the chosen style are saved to
  a JSON file in the app's private storage and restored on launch.

[0.5.0]: https://github.com/Antitastico/Streak/releases/tag/v0.5.0
[0.4.0]: https://github.com/Antitastico/Streak/releases/tag/v0.4.0
[0.3.0]: https://github.com/Antitastico/Streak/releases/tag/v0.3.0
