# Money Manager (Chroma Edition)

A personal, local-first Android app for tracking day-to-day and household spending. Built for a single user — no accounts, no cloud, no server. 
Designed with a stark, high-contrast **Neo-Brutalist (Chroma)** aesthetic.

## Why
Most expense trackers assume shared/multi-device use or push you toward a subscription. This is a private, local-first app: your data never leaves your phone, and household spend tracking (shared rent, groceries, bills) is a first-class feature, not an afterthought — built for Indian day-to-day spending patterns (UPI, cash, common recurring bills).

## Design System
The app features a **reducto+ASCII** take on the Chroma (trychroma.com) Neo-Brutalist language:
- Flat hard-edge surfaces: 1dp hairlines (`Ascii.hairline` / `Ascii.hairlineStrong`) instead of heavy strokes, `RoundedCornerShape(2.dp)` corners.
- Minimal shadows — a `Chroma.flat` token group caps card/button/FAB shadow offsets at 1dp.
- ASCII terminal blocks for data viz and chrome: `#` ₹ hero figure, `[████····]` mood bars, `// prompt` section headers, `─` dividers, `│` rules — all in IBM Plex Mono.
- Shared component contract in `ui/ascii/AsciiArt.kt`: `AsciiDivider`, `AsciiSectionHeader`, `AsciiHeroFigure`, `AsciiEmptyState`, `AsciiMoodBar`, `AsciiSelectChip`.
- Dense single-column feeds with one-line terminal rows instead of masonry grids.
- No bulky `TopAppBar`s — pure content focus.

## Stack
- **UI:** Kotlin + Jetpack Compose, Chroma design tokens (Inter + IBM Plex Mono)
- **Architecture:** MVVM, Coroutines/Flow, Compose Navigation
- **Local Storage:** Room (SQLite), fully offline
- **Background Tasks:** WorkManager for recurring-expense reminders

## Features
- **Fast Expense Entry (< 5s):** calculator-style amount input, icon-grid categories, payment mode (UPI/cash/card).
- **Scope Tagging:** Personal vs Household tracking with simple split logic.
- **Transaction Feed:** dense one-line terminal rows grouped by day with ASCII headers.
- **Budgets:** Per-category, per-month tracking.
- **Recurring Expenses:** Rent, WiFi, and subscriptions with WorkManager reminders.
- **Reporting:** Daily/weekly/monthly views + calendar heatmap, category breakdown, spend trend.
- **Export:** CSV/JSON export for manual backup.

## Project Docs
- `CHANGELOG.md` — History of features, redesigns, and releases.
- `AGENTS.md` — standing context and conventions for AI coding agents working in this repo (stack, commands, deny rules, data model).
- `docs/money-manager-spec.md` — full feature/architecture spec.

## Status
Active development. Current version: **v1.8.0**.
