# Kinetic Ledger redesign (v1.15.0)

Source: `kinetic_ledger_prd_project_brief.md` (user-attached PRD) + 3 reference
screenshots. Decision (user, 2026-10-13): **full PRD rebuild** — 4-tab IA,
sage light default + Obsidian dark, ₹ kept with tabular single-decimal
treatment. This file is the durable memory for the direction; also mirrored in
`docs/apple-design-memory.md`.

## Design tokens (PRD §5)
- Canvas: `#B5BFAE` (base) / `#C1CBB9` (raised) / `#F3FCEA` (paper)
- Slab (dark tile): `#1E241C` / `#2C362A` / `#3B4438`
- Accent ochre: `#D9C852` — hero amounts, active toggles, add targets
- Alert terracotta: `#D76449` — overruns, top outflow
- Critical maroon: `#4A1510` — over-budget card
- Dark (Phase 3): Obsidian `#0A0F0D` + sapphire + safety amber
- Type: **Space Grotesk** (UI) + monospace tabular (figures) — replaces Geist;
  Playfair italic heroes retired (PRD has no serif)
- Format: single decimal ₹1,894.0 with greyed decimal part, fixed-width tabular
- Texture: vertical tick barcode meters (`||||`) for density/capacity/progress

## IA (PRD §3) — restructure from 3 tabs + FAB
`[01 HOME] [02 BUDGET] [03 EXPENSE (+)] [04 REPORTS]` fixed bottom bar.
Settings stays a pushed route (9-dot / avatar header entry).

## Screen specs (PRD §4) — restyle targets
- Home: monogram + `LEDGER // 01`, available-budget hero + barcode gauge,
  terracotta overrun banner w/ ↗, ochre quick-assign card, recent ledger slab
  rows + SHOW MORE, footer `SYNCED WITH LEDGER NODE 01`
- Budget: capital hero `$X / MONTH`, barcode density meter %, USED/RESERVE,
  stacked category slabs w/ dual-tone bars, maroon overrun tile, BUFFER chip,
  export action
- Expense: ochre amount hero + blinking cursor, +chips, category grid w/
  capacity gauges, merchant + payment channel fields, buffer banner,
  full-width black `■ CONFIRM & LOG EXPENSE ↗`
- Reports: WEEK/MONTH/QUARTER/YEAR pills, outflow + pacing pill, W1-W4 cadence
  histogram (peak ochre), terracotta top-sector card, tiered segments,
  `GENERATE STATEMENT ↗ / ARCHIVE / EXPORT CSV`

## Rules carried over
- Apple-design craft rules in `docs/apple-design-memory.md` still govern:
  edge-to-edge contract, imePadding, one color = one thing, contrast computed.
  Kinetic Ledger is now the visual identity on top of that craft layer.
- Offline-only, no telemetry, ₹/INR, single user (AGENTS.md deny rules).

## Status
- [x] Foundation: Space Grotesk fonts, Kinetic token schemes, amount format
- [x] Shared components: KineticHeader, BarcodeMeter, LedgerAmount/MonoLabel,
      Slab, Hairline, KineticButton, LedgerPill, MetricRow, KineticInk
- [x] Nav restructure to 4 tabs (Expense tab hosts AddTransactionScreen in
      tab mode: showBack=false, save returns Home); AddSpendSheet deleted
- [x] Home / Budget / Expense / Reports / Transactions / Settings restyle
      (Settings inherits tokens — grouped list headers now mono)
- [x] ReportPeriod rebuilt: WEEK / MONTH / QTR / YEAR / ALL
- [x] Dark = Obsidian variant; ochre/terracotta surfaces use fixed KineticInk
      text in BOTH modes (paper-on-amber failed contrast)
- [x] FormatUtils.formatLedger() — always-on single decimal, Indian grouping;
      maskedCurrency routes through it
- [x] Verified: 51/51 tests, lint clean (pre-existing VIBRATE + splash-API
      errors fixed: manifest permission, values-v31 theme split), on-device
      light/dark/1.3x screenshots
