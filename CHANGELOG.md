# Changelog

All notable changes to this project will be documented in this file.

## [v1.6.1] - 2026-08-22
### Fixed
- **XLSX Import Crash:** Fixed app crashing when importing XLSX files by copying stream to temp file before POI processes it, catching all throwable errors (not just exceptions), and adding R8 keep rules for Apache POI classes.

## [v1.6.0] - 2026-08-22
### Added
- **Custom Date for Transactions:** Added Material 3 DatePickerDialog with quick-select chips (Today, Yesterday, 3 Days Ago, 1 Week Ago) to the Add Transaction screen.
- **Delete Transactions:** Added delete button on each transaction card in the Transactions list, wired to a confirmation dialog.
### Fixed
- **XLSX Import:** Fixed off-by-one cell iteration, expanded header column matching with auto-detect fallback, added more date format patterns, and improved error handling.

## [v1.4.0] - 2026-08-20
### Added
- **Responsive Masonry Layout:** Replaced standard lists with `LazyVerticalStaggeredGrid` for a responsive, two-column Pinterest-style transaction feed on the Home screen.
- **Navigation Fixes:** Resolved bottom navigation back-stack issues to ensure seamless state preservation when switching between tabs (Home, Add, Report).
- **Lint Fixes:** Fixed Android 13 `POST_NOTIFICATIONS` permission requirement for the `RecurringExpenseWorker`.

## [v1.3.0] - 2026-08-19
### Removed
- **Feed Tab:** Removed the unused 'Feed' tab from the bottom navigation.
- **TopAppBar:** Eliminated the bulky TopAppBar across all screens to maximize vertical screen space and give a cleaner, app-like feel.

## [v1.2.0] - 2026-08-19
### Changed
- **Compact HomeScreen Layout:** Reorganized the hero card and personal/household split cards into a more compact, horizontal layout to reclaim ~80dp of vertical space.

## [v1.1.0] - 2026-08-19
### Added
- **Custom App Icon:** Designed a bold white ₹ (Rupee) symbol on an obsidian black background with retro corner dot accents.
### Fixed
- **Performance Optimizations:** Enabled R8 minification for production-ready performance.
- Optimized Compose allocations using `remember()` for Shapes to eliminate frame drops and UI stuttering.

## [v1.0.0] - 2026-08-19
### Added
- **Chroma Neo-Brutalist Design:** Implemented a stark, high-contrast black-and-white neo-brutalist UI architecture inspired by trychroma.com.
- **Core Tracking System:** Sub-5-second fast entry, dual Personal/Household tracking, and offline Room SQLite database.
- Initial app scaffolding using Kotlin, Jetpack Compose, MVVM, and Coroutines/Flow.
