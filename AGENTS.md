# AGENTS.md

## Project
Personal Android money manager app for a single user (Indian context). Tracks
day-to-day spending and household spending. Personal use only — no multi-user
accounts, no backend, no login.

## Stack
- Kotlin + Jetpack Compose (native Android)
- Room (SQLite) for local storage — fully offline, no server
- MVVM architecture, Kotlin Coroutines/Flow
- Compose Navigation
- WorkManager for recurring-expense reminders
- Material 3 theming, dark mode default
- Charting: Vico (Compose-native) or custom Compose Canvas

## Commands
- build: `./gradlew build`
- run tests: `./gradlew test`
- lint: `./gradlew lint`
- install debug build: `./gradlew installDebug`

## Conventions
- All currency is INR (₹). Use Indian number grouping (lakh/crore) as an
  optional display toggle, not hardcoded.
- Default expense categories: groceries, rent, electricity, mobile recharge,
  transport, food delivery, chai/snacks, education, subscriptions, medical,
  misc. Categories must be user-editable/extendable, not hardcoded enums.
- Every transaction is tagged `personal` or `household` — this scope field
  is required, never optional.
- Payment modes are first-class fields (UPI, cash, card) — not free text.
- Fast entry is the top UX priority: adding an expense should take under
  5 seconds. Prefer a calculator-style amount input and icon-grid category
  picker over dropdowns/forms.
- Room is the single source of truth. No remote sync, no auth flow, no
  analytics/tracking SDKs — this is a private personal-finance app.
- Keep composables small and previewable; one screen = one top-level
  composable + ViewModel.

## Edge-to-edge & system bars (mandatory)
The app is full edge-to-edge: backgrounds bleed behind the punch-hole and
the gesture bar; content never does. Keep all four layers intact:
- `res/values/themes.xml` (and `values-night/`): `statusBarColor` and
  `navigationBarColor` = `@android:color/transparent`,
  `enforceNavigationBarContrast` = false,
  `windowLayoutInDisplayCutoutMode` = shortEdges.
- `MainActivity.onCreate`: `enableEdgeToEdge()` (light transparent bar
  styles) AND on API 28+ set
  `window.attributes.layoutInDisplayCutoutMode =
  LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES`.
- Never pad the root container with `systemBarsPadding()` /
  `safeDrawingPadding()` and never set `fitsSystemWindows` on it — the
  outermost layer stays inset-free so the background draws edge-to-edge.
- Apply insets/padding ONLY to inner interactive elements: top headers pad
  below the status bar/camera (screens use a 44dp top content padding),
  and `IosCommitRail` owns the bottom (96dp tappable row above the gesture
  pill + non-tappable color extension through the gesture zone, horizontal
  safeDrawing inset only). NavHost content must NOT double-pad the bottom.

## Data model (source of truth — see also project spec doc)
- `Transaction`: id, amount, type(expense/income), category_id, note, date,
  payment_mode, scope(personal/household), paid_by (nullable)
- `Category`: id, name, icon, color, is_default
- `Budget`: id, category_id, month, amount_limit
- `RecurringRule`: id, template_transaction_fields, frequency, next_due_date
- `HouseholdMember` (only if implementing splits): id, name

## Device policy
- Ask before using adb or installing/launching builds on the connected
  phone — no installs, taps, or screenshots without explicit approval.
  Local compile and unit tests are fine.

## Deny rules
- Never add a backend, cloud sync, or third-party auth without being asked
  explicitly — this app is offline-only by design.
- Never add analytics, ads, or telemetry SDKs.
- Never commit signing keys, keystores, or API keys to the repo.
- Don't copy UI pixel-for-pixel from a named competitor app (CRED, Jupiter,
  Splitwise, etc.) — use only as layout/pattern inspiration.

## Design references
- Apple Design skill (`~/.agents/skills/apple-design`, HIG-grounded) — repo
  memory in `docs/apple-design-memory.md`; edge-to-edge + craft rules live there
- Material 3 (m3.material.io) — component/pattern source of truth for Compose
- Figma MCP (mcp.figma.com) — if a reference Figma file exists, pull design
  context/screenshots from it instead of guessing
- Mobbin (mobbin.com), Finance category — real app screens for pattern
  inspiration only, not for cloning
