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

## Data model (source of truth — see also project spec doc)
- `Transaction`: id, amount, type(expense/income), category_id, note, date,
  payment_mode, scope(personal/household), paid_by (nullable)
- `Category`: id, name, icon, color, is_default
- `Budget`: id, category_id, month, amount_limit
- `RecurringRule`: id, template_transaction_fields, frequency, next_due_date
- `HouseholdMember` (only if implementing splits): id, name

## Deny rules
- Never add a backend, cloud sync, or third-party auth without being asked
  explicitly — this app is offline-only by design.
- Never add analytics, ads, or telemetry SDKs.
- Never commit signing keys, keystores, or API keys to the repo.
- Don't copy UI pixel-for-pixel from a named competitor app (CRED, Jupiter,
  Splitwise, etc.) — use only as layout/pattern inspiration.

## Design references
- Material 3 (m3.material.io) — component/pattern source of truth for Compose
- Figma MCP (mcp.figma.com) — if a reference Figma file exists, pull design
  context/screenshots from it instead of guessing
- Mobbin (mobbin.com), Finance category — real app screens for pattern
  inspiration only, not for cloning
