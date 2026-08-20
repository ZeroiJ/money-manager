# Changelog

All notable changes to this project will be documented in this file.

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
