# Personal Money Manager — Project Spec

Personal, single-user Android app. Tracks day-to-day + household spending (Indian context). Built with Google Antigravity.

You didn't lock in the three decisions from earlier, so I picked sane defaults below — swap any of them and Antigravity handles it fine, this isn't concrete.

## 1. Decisions (defaults — change freely)

| Decision | Default | Why |
|---|---|---|
| Stack | **Kotlin + Jetpack Compose** (native) | Best UI/UX and performance for a single Android app; Material 3 components map 1:1 to Compose; Gemini/Antigravity writes Kotlin well |
| Storage | **Fully offline, local Room (SQLite)** | It's personal finance data, for you only — no backend, no login, no server cost. Add manual JSON/CSV export for backup |
| Scope | **Fuller v1** | Since household tracking is a day-one requirement, not bolted on later |

## 2. Core features

- **Quick add expense**: amount, category, note, payment mode (cash / UPI / card), date, personal-or-household tag
- **Household split**: who paid, simple split among household members (lightweight — not a full Splitwise clone, no multi-device sync needed since it's single-user)
- **Categories**: default Indian set — groceries, rent, electricity, mobile recharge, transport, food delivery, chai/snacks, education, subscriptions, medical, misc — plus custom categories
- **Recurring expenses**: rent, WiFi, subscriptions — auto-log or remind via notification
- **Budgets**: per category per month, with progress indicators
- **Views**: daily / weekly / monthly, calendar heatmap of spend days
- **Reports**: category breakdown (pie/bar), spend trend (line), personal vs household split
- **Search/filter** transactions
- **Export/import** (CSV/JSON) — your only backup path since there's no cloud
- **Later/stretch**: SMS or UPI notification parsing to auto-suggest transactions

## 3. Data model (Room entities)

```
Transaction
  id, amount, type(expense/income), category_id, note, date,
  payment_mode, scope(personal/household), paid_by (nullable)

Category
  id, name, icon, color, is_default

Budget
  id, category_id, month, amount_limit

RecurringRule
  id, template_transaction_fields, frequency, next_due_date

HouseholdMember (only if doing splits)
  id, name
```

## 4. Architecture

- MVVM + Jetpack Compose + Room + Kotlin Coroutines/Flow
- Compose Navigation, bottom nav: Home / Add / Reports / Budgets / More
- Charts: Vico (Compose-native charting lib) or custom Compose Canvas
- Material 3 theming, dark mode as default (quick daily glances, easier on eyes for a habit-tracking app)
- WorkManager for recurring-expense reminders/notifications
- No backend, no auth

## 5. UI/UX direction

- **Fast entry is the whole game.** Home screen should let you log a spend in under 5 seconds — big FAB, calculator-style numeric entry, category as a swipeable icon grid, not a dropdown.
- **Indian-context defaults**: ₹ symbol, lakh/crore-style number grouping (optional toggle), UPI/cash as first-class payment modes rather than buried options.
- **Visual hierarchy**: today's spend front and center on Home, monthly budget shown as a progress ring, personal vs household visually separated (tabs or color-coded chips) instead of mixed into one feed.
- **Reports stay simple**: 1–2 charts per screen, not a dense dashboard — this is for a quick glance, not analysis paralysis.

## 6. Design references & MCPs for design research

Antigravity can browse and use MCP servers too — worth wiring up before you start:

- **Figma MCP** (`mcp.figma.com`, official) — if you build or collect a reference Figma file, Antigravity can pull design context, screenshots, and variables directly instead of guessing from text prompts. Best option if you want to mock up screens first.
- **Material Design 3** (`m3.material.io`) — Google's own component and pattern library. Since you're using Compose, M3 components map directly, so this doubles as an implementation reference, not just inspiration.
- **Mobbin** (`mobbin.com`) — real production app screens, filterable by category ("Finance"). Good for studying concrete Indian fintech patterns (Jupiter, CRED, Walnut/Fold, Splitwise) for layout and flow ideas.
- Practical tip: drop 2–3 reference screenshots (from Mobbin or apps you already use) directly into your Antigravity prompt as images. It follows a visual anchor far better than a text description alone. Use these for pattern/layout inspiration only — don't ask it to clone a specific app's branding or pixel-exact UI.

## 7. Starter prompt for Antigravity

```
Build an Android app in Kotlin + Jetpack Compose: a personal expense tracker
for an Indian user, covering both day-to-day and household spending.

Core screens: Home (today's spend + quick-add FAB + budget ring),
Add Transaction (calculator-style amount entry, category grid, payment
mode, personal/household toggle), Reports (category breakdown, monthly
trend, personal vs household split), Budgets (per-category monthly limits
with progress), Transaction list with search/filter.

Data: Room (SQLite), fully offline, no backend, no auth. Entities:
Transaction, Category, Budget, RecurringRule.

Design: Material 3, dark mode default, ₹ currency formatting, fast entry
as the top priority (log an expense in under 5 seconds). Default category
set for Indian spending (groceries, rent, electricity, mobile recharge,
transport, food delivery, subscriptions, medical, misc).

Architecture: MVVM, Compose Navigation, Coroutines/Flow, WorkManager for
recurring-expense reminders.
```
