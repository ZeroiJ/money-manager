# Changelog

All notable changes to this project will be documented in this file.

## [v1.16.0] - 2026-10-13 — Strictly boxes

**Hardening the Kinetic Ledger into an all-square system + IA slim-down**
- Strictly boxes: every rounded corner removed app-wide — segmented
  controls, dialogs, text fields, chips, keypad, sheets, receipt buttons,
  progress bars now all `RectangleShape`; pill switch replaced with a
  square sliding toggle (ochre track when on).
- Budget tab removed: 3-tab layout `01 HOME / 02 EXPENSE / 03 REPORTS`;
  budget screen/viewmodel deleted (budget data still exported in backups).
- Home: budget gauge bar, overrun banner and allocations card removed;
  hero is now month spend only; recent-ledger drawer runs edge-to-edge as
  one continuous dark-slab block — single tone, no icons, hairline row
  separators.
- Expense: category picker no longer has its own scroll — static
  4-column block that grows with the category list.

## [v1.15.0] - 2026-10-13 — Kinetic Ledger redesign

**Full visual + IA rebuild per the Kinetic Ledger PRD (brutalist/utilitarian
fintech)**
- Tokens: sage canvas / paper panels / dark-olive slabs, ochre accent,
  terracotta alerts, deep-maroon overrun cards; Obsidian dark variant.
- Type: Space Grotesk (UI) replaces Geist; Playfair serif heroes retired;
  PaperMono tabular figures with always-on single decimal (`₹1,951.0`,
  greyed decimal part).
- Texture: vertical tick barcode meters for budget/capacity gauges; square
  corners + hairlines everywhere (no rounded cards).
- IA: 4-tab layout `01 HOME / 02 BUDGET / 03 EXPENSE / 04 REPORTS` replaces
  3 tabs + FAB; the add flow now lives in tab 03 (quick-add sheet removed).
- Home: `LEDGER // 01` header, available-budget hero + barcode gauge,
  terracotta overrun banner, ochre allocations card, slab ledger rows with
  FILTER cycling + SHOW MORE, footer diagnostics line.
- Budget: capital hero + % utilized density meter, USED/RESERVE, stacked
  category slabs with dual-tone bars, maroon overrun tiles, buffer chip.
- Expense: ochre amount hero with blinking cursor, ink quick-chips, ink
  `CONFIRM & LOG EXPENSE ↗` commit button, secure-enclave footer tag.
- Reports: WEEK/MONTH/QTR/YEAR/ALL temporal filter, accumulated outflow
  hero with inflow pill, earthy donut palette (candy colors removed).
- Fixes: VIBRATE permission declared, splash attrs moved to `values-v31`
  (lint now clean).

## [v1.14.0] - 2026-10-13 — Quiet ledger de-slop

**Anti-slop pass (Origin/Slash/Public/Valon consensus)**
- Animated wash frozen into a static near-monochrome texture + grain (zero
  battery); the Display toggle now means paper texture on/off.
- Category icons monochrome (donut/heatmap keep their data palettes); blue is
  now strictly for actions (FAB, buttons, links, selected states).
- Solid tab bar (translucency without blur read as smudge); 40sp serif hero
  with tight leading; more section air.

## [v1.13.0] - 2026-10-13 — Peace-of-mind error states

**Error visibility (all local, zero telemetry)**
- New `AppError` + `ErrorBus`: every failure now records what happened, where,
  and why (exception cause included). One global snackbar per error with a
  Details action into Settings → Diagnostics.
- Previously-silent write paths now report: save/edit transaction, delete,
  budgets, categories, members, recurring rules, SMS review queue, recurring
  auto-log worker. Export/import failure Toasts migrated to the bus.
- Home + Transactions feeds distinguish empty vs failed: a "Couldn't load" card
  with Try again (retry re-subscribes to the database) instead of a blank list.
- New Settings → Diagnostics section (on-device only): app version, Room/SQLite
  version, per-table row counts, recent-error history with timestamps, copyable
  report, clear-log. No data ever leaves the phone.

## [v1.12.0] - 2026-10-13 — Nessie premium-motion package + AGSL mesh background

**Motion & feel (inspired by Cecilia Kim's Nessie build log)**
- Rolling per-digit amounts (~0.2s slide+fade) on every `IosAmount`; press-shrink
  feedback on primary CTAs and FAB; one-shot confetti (first entry, first limit)
  with reduced-motion static fallback; celebration state machine unit-tested.
- One-tap privacy eye on Home: all ₹ figures (heroes, rows, subtitles, summaries)
  mask to bullets via `hide_amounts` pref; delete-confirm dialogs stay explicit.
- Splash matches the app (cream/navy `windowBackground`) — no white flash.

**Look**
- AGSL flowing mesh gradient + film grain replaces the flat wash on API 33+
  (old animation below, static on reduced-motion); verified animating on-device.
- Playfair Display Italic (OFL) hero words: month name, empty states, celebration
  titles. Amounts stay sans, like the reference.
- Category grid cells go 2-line (`Chai & Snacks`, `Food Delivery` no longer cut);
  segmented pills gain breathing room at largest text size (verified at 1.3x).

## [v1.11.0] - 2026-10-11 — iOS Liquid Glass Lite reskin

**Design system (replaces Chroma neo-brutalist + ASCII kit)**
- New `theme/Ios.kt` tokens with system-auto light/dark schemes; new `ui/ios/` component kit (`IosTabBar`, `IosGroupedList`/`IosRow`, `IosSegmented`, `IosSwitchRow`, `IosProgressBar`, `GlassSheet`, `IosAmount`).
- Full-screen ambient gradient wash behind every tab: warm orange on light, deep navy on dark. Slow 10–12s drift with static fallback on reduced-motion/low-end, plus a Settings → Display toggle (default ON).
- Typography: Geist Sans for UI + Paper Mono for figures (both SIL OFL, bundled). New app logo: white ₹ on iOS blue→purple gradient with glass sheen.
- Proper OS edge-to-edge: tab bar owns the navigation-bar/cutout insets instead of double-padding content.

**Screens**
- **Home:** glanceable month-total + today card back (from live `todaySpend`/`monthSpend` sums), recents as grouped rows, circular blue FAB.
- **Add:** 5-step stepper collapsed into a single grouped sheet (amount + keypad → scope → pay mode → category grid → note → commit). Edit form keeps all fields (date picker, receipt) with grouped-card chrome.
- **Transactions:** iOS search field, segmented scope/mode filters, day sections with totals, swipe-to-delete (confirm dialog kept).
- **Reports:** large-title cards — outflow/inflow, personal-vs-household bar, donut with legend, rounded trend bars + top-3, calendar heatmap.
- **Budgets:** grouped progress rows with OVER/LEFT labels, iOS month stepper, PAST MODE gating kept.
- **Settings:** true iOS Settings groups with system switches (app lock, Indian grouping, ambient background).

**Internal**
- Deleted `AsciiArt.kt`, `Chroma.kt`, `ChromaDesign.kt`, Inter/Plex Mono fonts (zero references). `Color.kt` raw vals stay for the Glance widget (untouched).
- No Room migration, no new dependencies, no behavior changes to data/worker/SMS/XLSX logic.

## [v1.10.0] - 2026-10-01 — SMS auto-import with review queue

**SMS auto-import (Settings → SMS_AUTO_IMPORT)**
- Bank/UPI SMS parsed on-device into a review queue: `SCAN SMS INBOX` backfills the last 30 days; NotificationListener captures live SMS + GPay payment notifications.
- `UpiSmsParser` now detects transaction type (credited → income, debited → expense) and rejects OTPs and balance-only updates.
- Review queue shows amount, merchant, sender, and date per row with approve (logs the transaction) / discard actions. Duplicates suppressed by UPI reference number.
- Approved transactions land uncategorized (`note = "<merchant> via UPI"`) — assign a category via edit. No per-merchant category pollution.
- New `pending_imports` table (DB v3 with migration). SMS bodies are never logged or retained — only parsed fields.

## [v1.9.0] - 2026-09-06 — XLSX import: crash fix + preview-before-commit

**XLSX import (Settings → IMPORT EXCEL (.XLSX))**
- Fixed the storage-file parse crash: Android does not ship `javax.xml.stream` (StAX), which POI OOXML/XMLBeans require to parse `.xlsx`. Added `stax-api` + `woodstox-core` and proguard keep rules for POI/XMLBeans/StAX so both debug and release builds work.
- Parsing moved off the main thread (`Dispatchers.IO`).
- New **preview-before-commit** flow: picking a file parses rows into an `IMPORT // XLSX` dialog listing every row that will be inserted, with a `COMMIT // INSERT` action to confirm. Summary line shows `X rows ready · Y skipped / Z total`.
- Each preview row shows amount (+ income marker), note, category hint, date, scope, and payment mode.
- Category column is now kept as a separate hint and resolved at commit time: hints matched case-insensitively against existing categories; unmatched hints auto-created as non-default categories.
- Detected `scope` column (personal/household/shared/family/home) now sets the required scope field instead of forcing everything to PERSONAL.
- Unparseable rows are counted as skipped and surfaced in the preview instead of silently dropped.

## [v1.8.2] - 2026-09-06 — home log + stepper quick-add

**Home tab**
- Reduced to the `RECENT_ACTIVITY.LOG` feed only. Hero/₹ figure, month spent/budget blocks, and scope filter chips removed. Settings gear moved next to the log header.

**Quick add (was: full ADD page)**
- `add_transaction` page and its bottom-nav tab deleted entirely; the **ADD SPEND** FAB now opens an inline `ModalBottomSheet` stepper (`ui/screens/add/AddSpendSheet.kt`).
- Flow order: **amount/keypad → personal|household (+ paid-by) → payment mode → category picker → optional note** → commit. Date defaults to today.
- Shortcut / UPI-SMS deep links (`preset_category`) now open the same stepper pre-filled on Home instead of routing to a standalone page.
- Full-page transaction form remains solely for **editing** a tapped row (dates, receipts, income toggle intact).

## [v1.8.1] - 2026-09-05 — recurring live updates

**Recurring expenses**
- New `util/LiveUpdateHelper.kt` centralizing channels and recurring-bill notifications.
- Upcoming-bills notification upgraded to a live-update `Notification.ProgressStyle` (API 36): start/end icons, one milestone segment per bill, milestone points, styled-by-progress; graceful `NotificationCompat` progress fallback below API 36.
- Notification actions: **LOG ₹amount** (logs the next due bill's transaction immediately and advances the cycle) and **SNOOZE 1D** (postpones the next due date by one day), handled by new `RecurringBillActionReceiver` (`worker/`) declared in the manifest.
- `RecurringExpenseWorker` slimmed: DB building and notification posting delegated to `LiveUpdateHelper`; transaction construction and next-due advancement moved onto `RecurringRule` (`toTransaction`, `advanceNextDue`).
- Channels created at app startup (`MoneyManagerApp.onCreate`).

## [v1.8.0] - 2026-09-05 — reducto+ASCII redesign

**Design system**
- New shared ASCII component contract in `ui/ascii/AsciiArt.kt`: `AsciiDivider`, `AsciiSectionHeader`, `AsciiHeroFigure`, `AsciiEmptyState`, `AsciiMoodBar`, `AsciiSelectChip`.
- `Chroma.flat` token group: card/button/FAB shadow offsets flattened to 1dp; corners standardized at 2dp.
- Hero rupee block art, mood bars, and prompt-header vocabulary.

**Screens**
- **Home:** removed the Personal/Household split row; ASCII rupee hero figure with flat mood bar; flat segmented scope filter (ALL/PERSONAL/HOUSEHOLD); top-3 recent feed above view-all; ASCII empty state.
- **Add/Edit:** metadata (scope, pay mode, category) compacted into flat bordered sections with ASCII dividers; chip-based scope and pay-mode pickers; calculator keypad flattened.
- **Reports:** tabbed layout — CATEGORY (donut + top-5 with SHOW ALL + household settle-up ledger) / TREND (ASCII block flame-graph of daily spend) / HEATMAP (calendar density grid); period switcher (THIS MONTH / LAST MONTH / ALL TIME) as flat chips.
- **Budgets:** current (active) month first by default; past months gated behind a PAST MODE toggle; month arrows flat and disabled at bounds; ASCII mood bars with OVER/LEFT labels; ASCII empty state.
- **Transactions:** masonry grid → dense one-line terminal rows, day groups as ASCII headers; flat search + filter chips; delete-with-confirm and tap-to-edit preserved.
- **Settings:** flattened grouped sections with ASCII headers (`SECURITY_STATUS`, `BIOMETRIC_LOCK`, `DISPLAY_FORMAT`, `HOUSEHOLD_MEMBERS`, `CATEGORIES_CONFIG`, `BACKUP_RESTORE`).

**Internal**
- Raised `compileSdk`/`targetSdk` 35 → 36; added `uses-feature` telephony flag (SMS import no longer blocks non-telephony devices).
- Added mockk test dependency; `AddTransactionViewModel` now takes `ReceiptStorage` for photo-receipt persistence tests.
- Recurring-expense DAO additions: `getUpcomingRecurringRules`, `getRecurringRuleById`, `updateRecurringRuleNextDue` (also mirrored in `FakeMoneyDao`).

## [v1.7.1] - 2026-08-22
### Fixed
- **Inter Font Corruption:** Replaced corrupt Inter font files (GitHub raw URLs returned HTML pages instead of TTF data) with valid Inter 4.0 TTFs from the official release zip. Fixed launch crash.

## [v1.7.0] - 2026-08-22
### Changed
- **Stripped Material 3 Theming:** Removed `MoneyManagerTheme`, `Theme.kt`, and all `MaterialTheme.colorScheme` / `MaterialTheme.typography` references across every screen. Colors and typography now flow through a `Chroma` singleton object with exact design tokens extracted from trychroma.com.
- **Bundled Fonts:** Added Inter (sans-serif) and IBM Plex Mono (monospace) as bundled `.ttf` font files in `res/font/`, replacing system `FontFamily.Monospace` fallback with the actual extracted typeface.

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
