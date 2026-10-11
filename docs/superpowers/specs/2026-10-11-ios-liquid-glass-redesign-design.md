# iOS Liquid Glass Redesign — Design Spec (Approach A: Liquid Glass Lite)

- Date: 2026-10-11
- Status: approved direction (Approach A), awaiting spec review before implementation plan
- Scope: all 5 screens at once (Home, Transactions, Budgets, Reports, Settings) + Add flow + nav + app logo. System-auto light/dark.

## 1. Intent

User is bored of the Chroma neo-brutalist "bento" look (sharp 2dp corners, black hairlines,
Plex Mono terminal labels like `RECENT_ACTIVITY.LOG`, `[P]/[H]` rows) and wants an iOS-like
look and feel. User is not a UI/UX expert; success = the app looks calm and familiar like an
iPhone finance app, stays fast (<5s expense entry), and stays readable in light and dark.

What was said vs assumptions:
- Said: Liquid Glass iOS 26 look, all screens at once, follow system light/dark.
- Assumed: hand-rolled in existing Compose (no new library), blur only on bars/sheets for
  perf and contrast, ViewModels/DAO/Workers untouched. Correct me if any assumption is wrong.

## 2. Goals / non-goals

Goals:
- Replace Chroma tokens + ASCII kit with an iOS token set and a small `ui/ios/` component kit.
- Hit Apple HIG basics: inset grouped lists, large titles, 44dp touch targets, system switches,
  chevron rows, swipe-to-delete, spring sheets, 150–300ms transitions.
- Keep the <5s add promise: collapse the 5-step sheet into one grouped sheet.
- New app logo matching the redesign (white ₹ on iOS blue→purple gradient, no brutalist dots).
- Keep all 25 existing unit tests green; no new dependencies.

Non-goals:
- No behavior changes to data layer, recurring worker, SMS import, XLSX/CSV/JSON, widget logic.
- No full-blur-everywhere (perf + contrast risk), no Cupertino KMP library, no backend/sync.

## 3. Visual system (replaces `theme/Chroma.kt` + `ui/ascii/AsciiArt.kt` usage)

New file `theme/Ios.kt` with light + dark schemes, selected by system setting:

| Token | Light | Dark |
|---|---|---|
| Grouped background | #F2F2F7 | #000000 |
| Card / grouped cell | #FFFFFF | #1C1C1E |
| Secondary fill | #E5E5EA | #2C2C2E |
| Label primary | #000000 | #FFFFFF |
| Label secondary | #6E6E73 (3:4.5+ on card) | #AEAEB2 |
| Accent (actions, selected tab) | #007AFF | #0A84FF |
| Destructive | #FF3B30 | #FF453A |
| Success (income, under budget) | #34C759 | #30D158 |
| Separator | #C6C6C8 at 38% alpha | #38383A |

- Shape: 12dp cards, 16dp sheets, continuous (no hairline black borders, no `chromaShadow`).
- Type: keep bundled Inter (close to SF Pro, no new font dep). Titles 22–34sp semibold,
  body 17sp/22, footnote 13sp. Numbers tabular semibold. Kill 9–10sp mono micro-labels except
  debug-style metadata (then min 11sp and secondary color).
- Blur rule: frosted blur (10–20px equivalent scrim + `Modifier.blur`) ONLY on bottom tab bar
  and modal sheets. Content cards stay solid so ₹ figures always pass 4.5:1.
- Motion: sheet slide + fade 150–300ms, spring-ish easing, existing `util/Haptics.kt` kept,
  respect reduced-motion (instant transitions, no blur animation).

## 4. Components (new `ui/ios/`, ViewModels untouched)

- `IosScaffold`: large-title top area + content + floating glass tab bar slot.
- `IosTabBar`: translucent floating bar, 3 tabs (Home, Budgets, Reports) + blue active pill.
  Replaces `ChromaBottomNavigationBar` in `ui/navigation/NavGraph.kt`.
- `IosGroupedList` + `IosRow` (title, subtitle, icon tint box 32dp, chevron, 56dp min height):
  replaces ASCII section headers/dividers and terminal rows everywhere.
- `IosSegmented` (period/scope pickers), `IosSwitchRow`, `IosProgressBar` (replaces `AsciiMoodBar`),
  `GlassSheet` (16dp, drag handle, blur scrim) replacing `AddSpendSheet` stepper chrome.
- Donut/flame/heatmap canvases in Reports keep their math, get iOS legends + card chrome.

## 5. Screen changes

- **Home** (`ui/screens/home/HomeScreen.kt`): restore a glanceable month-total + today card
  (iOS card, not ASCII hero — v1.8.2 removed the hero entirely leaving only the log feed);
  recent list becomes `IosRow`s (note, category · mode subtitle, amount + date, chevron).
  Settings gear becomes 44dp; FAB becomes iOS blue circular + with haptic.
- **Add** (`ui/screens/add/AddSpendSheet.kt` + `AddTransactionScreen.kt`): one grouped sheet —
  amount field, scope segmented + paid-by, pay-mode segmented, category grid (rounded 12dp
  cells), note, single COMMIT. Keep calculator keypad + `+50/100/500` chips + `ReceiptStorage`.
  Edit screen keeps full form, restyled to grouped sections.
- **Transactions** (`TransactionListScreen.kt`): day groups become iOS section headers
  ("12 Oct · ₹1,240"), rows become `IosRow`s, delete moves to swipe-to-delete + keep confirm
  dialog; search becomes iOS search field; filter chips become `IosSegmented`.
- **Reports** (`ReportsScreen.kt`): large title + period segmented; one chart per card
  (donut + legend, trend, heatmap with accessible labels, settle-up ledger rows).
- **Budgets** (`BudgetsScreen.kt`): grouped limit rows with `IosProgressBar`, OVER red / LEFT green,
  month stepper as iOS stepper, keep PAST MODE gating.
- **Settings** (`SettingsScreen.kt`): true iOS Settings groups (Security, Display, Household,
  Categories, Backup, SMS import); FlatRow/FlatSection/ASCII headers deleted; toggles become
  system switches; pending-import rows become `IosRow`s with Approve/Discard buttons.
- **Nav** (`NavGraph.kt`): same routes/tabs, new bar chrome only. Widget (`widget/`) untouched.

## 5b. App logo (done in this change)

- `res/drawable/ic_launcher_background.xml`: obsidian + retro border → iOS linear gradient
  #0A84FF → #5856D6 with a top white 16% glass sheen.
- `res/drawable/ic_launcher_foreground.xml`: removed corner dots; centered white ₹ glyph
  (same geometry) sized to the 72dp safe zone; monochrome slot unchanged.
- Legacy `mipmap-*/ic_launcher*.webp` fallbacks intentionally untouched (unused on minSdk 26+;
  adaptive icon takes precedence on all supported devices).

## 6. Files touched / untouched

Touched: `theme/Ios.kt` (new), `ui/ios/*` (new), 5 screens + `AddSpendSheet.kt` +
`AddTransactionScreen.kt` + `NavGraph.kt`, 2 launcher drawables, this spec.
Untouched: `data/`, `di/`, `worker/`, `service/`, `util/` (except no changes needed),
`widget/`, ViewModels' public APIs, Room schema (no migration), manifest permissions.

## 7. Guardrails

- Contrast 4.5:1 on every blurred surface (test ₹ figures over tab bar at scroll end).
- 44dp minimum touch targets (fixes 36dp gear, small ✕ targets).
- Blur off/low-end fallback: solid translucent bar when blur unsupported.
- No new Gradle deps; APK delta ≈ vectors only. R8/ProGuard untouched.
- All ViewModel/Util tests keep passing (`./gradlew test`); manual pass on light + dark,
  small (360dp) + large screens, reduced-motion on.

## 8. Rollout

Single reskin change across all screens (user chose all-at-once over incremental) + logo.
If review flags risk, fallback is to land Home + Add first behind the same tokens.

## 9. Self-review

- No TBDs; each section names concrete files. Architecture matches feature list.
- Single-plan scope: pure UI restyle, no schema/behavior change. Logo vectors are the only
  binary-adjacent artifact and use safe-zone geometry.
- Ambiguity resolved: blur limited to bars/sheets, Inter kept, no new library.
