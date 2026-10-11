# iOS Liquid Glass Lite Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restyle all 5 screens + Add flow + nav to iOS Liquid Glass Lite with a full-screen ambient gradient, keeping all behavior and data code untouched.

**Architecture:** New `theme/Ios.kt` tokens and `ui/ios/` component kit land first; each screen is then converted top-down while ViewModels, DAO, Workers, and utils stay byte-identical. Logo already done (committed).

**Tech Stack:** Kotlin, Jetpack Compose (Material3), stock Compose animation APIs only — no new dependencies.

**Spec:** `docs/superpowers/specs/2026-10-11-ios-liquid-glass-redesign-design.md`

## Global Constraints

- Offline-only: no backend, sync, analytics, or new Gradle dependency.
- `minSdk 26`, `compileSdk/targetSdk 36`, Java 17; Room schema untouched (no migration).
- All 25 existing unit tests stay green (`./gradlew :app:testDebugUnitTest`).
- Touch targets ≥ 44dp; text contrast ≥ 4.5:1 on every surface including over blur.
- Blur/ translucency ONLY on bottom tab bar + modal sheets; content cards stay solid opaque.
- Inter + IBM Plex Mono stay (no new fonts); legacy `mipmap-*/ic_launcher*.webp` untouched.

## Review Focus

- 360dp-small screens: gradient + cards must not clip or overflow → pinned in Task 5 test (layout assertions via `createComposeRule`? not available — manual check + `assembleDebug`).
- Dark-mode secondary text `#AEAEB2` on card `#1C1C1E` must pass 4.5:1 → pinned in Task 1 (`IosTokensTest.contrast_darkSecondaryOnCard`).
- Reduced-motion on: ambient animation must render fully static → pinned in Task 2 (`AmbientModeTest.reducedMotion_forcesStatic`).
- Animator scale 0 / low-end: same static fallback → pinned in Task 2 (`animatorScaleZero_forcesStatic`).
- 200% display font size: `IosRow` subtitle must ellipsize, never push amount off-screen → pinned in Task 3 (manual check recorded in commit message).

---

### Task 1: Ios theme tokens

**Files:**
- Create: `app/src/main/java/com/example/moneymanager/theme/Ios.kt`
- Test: `app/src/test/java/com/example/moneymanager/theme/IosTokensTest.kt`

**Interfaces:**
- Consumes: nothing new (`theme/Color.kt` hexes as reference only).
- Produces: `object Ios { val Light: IosScheme; val Dark: IosScheme; val CardRadius=12.dp; val SheetRadius=16.dp }`, `data class IosScheme(bg, card, secondaryFill, label, secondaryLabel, accent, destructive, success, separator: Color; gradient: List<Color>)` with exact spec hexes (light bg `#FFF6EA` wash stops `#FFF6EA,#FFDFB5,#FF9500`; dark bg stops `#060B24,#101C4E,#0A84FF`); `fun contrastRatio(fg: Color, bg: Color): Double` (pure, testable).

- [ ] **Step 1: Write the failing test** — `IosTokensTest.kt`: assert `Ios.Light.gradient == listOf(Color(0xFFFFF6EA), Color(0xFFFFDFB5), Color(0xFFFF9500))`, same shape for Dark, and `contrastRatio(secondaryLabel, card) >= 4.5` for both schemes:

```kotlin
@Test fun contrast_lightSecondaryOnCard() {
    assertTrue(contrastRatio(Ios.Light.secondaryLabel, Ios.Light.card) >= 4.5)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.moneymanager.theme.IosTokensTest"`
Expected: FAIL (`Ios` unresolved).

- [ ] **Step 3: Implement `Ios`, `IosScheme`, `contrastRatio` in `theme/Ios.kt`** with the spec's exact hexes and WCAG relative-luminance math.
- [ ] **Step 4: Run test to verify it passes**

Run: same command as Step 2.
Expected: PASS (all 4+ tests).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/moneymanager/theme/Ios.kt app/src/test/java/com/example/moneymanager/theme/IosTokensTest.kt
git commit -m "feat: add Ios theme tokens with contrast-tested schemes"
```

### Task 2: AmbientBackground + motion decision

**Files:**
- Create: `app/src/main/java/com/example/moneymanager/ui/ios/AmbientBackground.kt`
- Test: `app/src/test/java/com/example/moneymanager/ui/ios/AmbientModeTest.kt`

**Interfaces:**
- Consumes: `Ios.Light/Dark.gradient` from Task 1.
- Produces: `enum class AmbientMode { Animated, Static }`, `fun resolveAmbientMode(animatorScale: Float, userEnabled: Boolean): AmbientMode` (Static when `!userEnabled || animatorScale == 0f`), `@Composable fun AmbientBackground(mode: AmbientMode, dark: Boolean)` (single full-screen layer; Animated = 10–12s `rememberInfiniteTransition` offset drift, Static = fixed gradient).

- [ ] **Step 1: Write the failing test** — `AmbientModeTest.kt`:

```kotlin
@Test fun reducedMotion_forcesStatic() {
    assertEquals(AmbientMode.Static, resolveAmbientMode(animatorScale = 0f, userEnabled = true))
}
@Test fun toggleOff_forcesStatic() {
    assertEquals(AmbientMode.Static, resolveAmbientMode(animatorScale = 1f, userEnabled = false))
}
@Test fun default_animates() {
    assertEquals(AmbientMode.Animated, resolveAmbientMode(animatorScale = 1f, userEnabled = true))
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.moneymanager.ui.ios.AmbientModeTest"`
Expected: FAIL (unresolved).

- [ ] **Step 3: Implement `AmbientMode`, `resolveAmbientMode`, `AmbientBackground` in `ui/ios/AmbientBackground.kt`** (animated layer via `Brush.linearGradient` with infinite-transitioned offsets; static layer fixed stops).
- [ ] **Step 4: Run test to verify it passes**

Run: same command as Step 2.
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/moneymanager/ui/ios/AmbientBackground.kt app/src/test/java/com/example/moneymanager/ui/ios/AmbientModeTest.kt
git commit -m "feat: add ambient gradient background with static fallback"
```

### Task 3: Core iOS component kit

**Files:**
- Create: `app/src/main/java/com/example/moneymanager/ui/ios/IosChrome.kt` (`IosTabBar`, `IosGroupedList`, `IosRow` 56dp min, chevron)
- Create: `app/src/main/java/com/example/moneymanager/ui/ios/IosControls.kt` (`IosSegmented`, `IosSwitchRow`, `IosProgressBar`, `GlassSheet` 16dp + drag handle + blur scrim)

**Interfaces:**
- Consumes: `Ios` schemes (Task 1).
- Produces: the six composables above with signatures fixed here (e.g. `IosRow(title: String, subtitle: String?, icon: ImageVector?, iconTint: Color, trailing: @Composable () -> Unit, onClick: (() -> Unit)?)`); later tasks use only these.

- [ ] **Step 1: Write the failing check** — no unit test is meaningful for chrome; the check is compile + existing suite. Record baseline first:

Run: `./gradlew :app:testDebugUnitTest`
Expected: PASS (baseline green before changes).

- [ ] **Step 2: Implement the six composables** in the two files (44dp+ targets, solid cards, blur confined to `IosTabBar`/`GlassSheet` scrims).
- [ ] **Step 3: Verify compile + suite still green**

Run: `./gradlew :app:testDebugUnitTest`
Expected: PASS; plus manual 200%-font check on `IosRow` (subtitle ellipsizes, amount visible) — note result in commit message.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/moneymanager/ui/ios/IosChrome.kt app/src/main/java/com/example/moneymanager/ui/ios/IosControls.kt
git commit -m "feat: add iOS component kit (tab bar, rows, controls, glass sheet)"
```

### Task 4: Navigation shell (Ambient + IosTabBar)

**Files:**
- Modify: `app/src/main/java/com/example/moneymanager/ui/navigation/NavGraph.kt` (wrap scaffold in `AmbientBackground`, swap `ChromaBottomNavigationBar` for `IosTabBar`; routes/tabs unchanged)

**Interfaces:**
- Consumes: `AmbientBackground`, `IosTabBar` (Tasks 2–3); `UserPreferences` for ambient toggle read (added in Task 9, default-true until then — hardcode `userEnabled = true` here, wire toggle in Task 9).
- Produces: same routes/tabs, new chrome only.

- [ ] **Step 1: Baseline check**

Run: `./gradlew :app:testDebugUnitTest`
Expected: PASS.

- [ ] **Step 2: Implement** — scaffold content over `AmbientBackground`, `IosTabBar` with Home/Budgets/Reports + blue active pill.
- [ ] **Step 3: Verify**

Run: `./gradlew :app:testDebugUnitTest`
Expected: PASS; manual: light + dark tab switch, no content hidden behind bar.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/moneymanager/ui/navigation/NavGraph.kt
git commit -m "feat: glass tab bar and ambient background in nav shell"
```

### Task 5: Home

**Files:**
- Modify: `app/src/main/java/com/example/moneymanager/ui/screens/home/HomeScreen.kt` (month-total + today card, recent list → `IosRow`s, 44dp settings affordance, iOS blue circular FAB; `HomeViewModel` untouched)

**Interfaces:**
- Consumes: kit from Tasks 2–4. Produces: nothing new.

- [ ] **Step 1: Baseline check**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.moneymanager.ui.screens.home.HomeViewModelTest"`
Expected: PASS.

- [ ] **Step 2: Implement** Home restyle per spec §5 (glanceable cards on top, grouped recents, no ASCII headers).
- [ ] **Step 3: Verify**

Run: `./gradlew :app:testDebugUnitTest`
Expected: PASS; manual: 360dp width (no clipping), empty state, tap → edit preserved.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/moneymanager/ui/screens/home/HomeScreen.kt
git commit -m "feat: restyle Home to iOS grouped cards and rows"
```

### Task 6: Add flow (sheet + edit form)

**Files:**
- Modify: `app/src/main/java/com/example/moneymanager/ui/screens/add/AddSpendSheet.kt` (single grouped `GlassSheet`: amount → scope+paid-by → pay-mode → category grid → note → COMMIT; keep keypad, quick chips, presets)
- Modify: `app/src/main/java/com/example/moneymanager/ui/screens/add/AddTransactionScreen.kt` (same fields, grouped sections chrome only; ViewModel untouched)

- [ ] **Step 1: Baseline check**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.moneymanager.ui.screens.add.AddTransactionViewModelTest"`
Expected: PASS.

- [ ] **Step 2: Implement** both files per spec §5 (one-page sheet, grouped edit form).
- [ ] **Step 3: Verify**

Run: `./gradlew :app:testDebugUnitTest`
Expected: PASS; manual: full add in <5s, preset deep links pre-fill, receipt attach intact.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/moneymanager/ui/screens/add/AddSpendSheet.kt app/src/main/java/com/example/moneymanager/ui/screens/add/AddTransactionScreen.kt
git commit -m "feat: collapse add flow into single iOS grouped sheet"
```

### Task 7: Transactions

**Files:**
- Modify: `app/src/main/java/com/example/moneymanager/ui/screens/transactions/TransactionListScreen.kt` (iOS section headers `12 Oct · ₹1,240`, `IosRow`s, swipe-to-delete + keep confirm dialog, iOS search field, `IosSegmented` filters; ViewModel untouched)

- [ ] **Step 1: Baseline check**

Run: `./gradlew :app:testDebugUnitTest`
Expected: PASS.

- [ ] **Step 2: Implement** per spec §5.
- [ ] **Step 3: Verify**

Run: `./gradlew :app:testDebugUnitTest`
Expected: PASS; manual: search/filter/delete-tap-edit all preserved, light + dark.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/moneymanager/ui/screens/transactions/TransactionListScreen.kt
git commit -m "feat: restyle Transactions to iOS grouped list"
```

### Task 8: Reports

**Files:**
- Modify: `app/src/main/java/com/example/moneymanager/ui/screens/reports/ReportsScreen.kt` (large title + period `IosSegmented`, one chart per card with iOS legends; donut/flame/heatmap math untouched; ViewModel untouched)

- [ ] **Step 1: Baseline check**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.moneymanager.ui.screens.reports.ReportsViewModelTest"`
Expected: PASS.

- [ ] **Step 2: Implement** per spec §5 (CATEGORY/TREND/HEATMAP cards, settle-up ledger rows).
- [ ] **Step 3: Verify**

Run: `./gradlew :app:testDebugUnitTest`
Expected: PASS; manual: all 3 tabs + 3 periods render in both themes.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/moneymanager/ui/screens/reports/ReportsScreen.kt
git commit -m "feat: restyle Reports to iOS large-title cards"
```

### Task 9: Budgets

**Files:**
- Modify: `app/src/main/java/com/example/moneymanager/ui/screens/budgets/BudgetsScreen.kt` (grouped limit rows + `IosProgressBar` OVER-red/LEFT-green, iOS month stepper, keep PAST MODE gating; ViewModel untouched)

- [ ] **Step 1: Baseline check**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.moneymanager.ui.screens.budgets.BudgetsViewModelTest"`
Expected: PASS.

- [ ] **Step 2: Implement** per spec §5.
- [ ] **Step 3: Verify**

Run: `./gradlew :app:testDebugUnitTest`
Expected: PASS; manual: set-limit dialog, over-budget haptic, month bounds.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/moneymanager/ui/screens/budgets/BudgetsScreen.kt
git commit -m "feat: restyle Budgets to iOS progress rows"
```

### Task 10: Settings (+ ambient toggle wiring)

**Files:**
- Modify: `app/src/main/java/com/example/moneymanager/data/prefs/UserPreferences.kt` (new `ambient_enabled` key, default `true`, same `StateFlow` pattern)
- Modify: `app/src/main/java/com/example/moneymanager/ui/screens/settings/SettingsScreen.kt` (iOS Settings groups, system switches incl. new Ambient toggle, pending-import `IosRow`s; ViewModel logic unchanged apart from exposing the toggle)
- Modify: `app/src/main/java/com/example/moneymanager/ui/screens/settings/SettingsViewModel.kt` (expose `ambientEnabled` flow only)
- Modify: `NavGraph.kt` (wire `userEnabled` from Task 4's hardcoded `true` to the real flow)

- [ ] **Step 1: Baseline check**

Run: `./gradlew :app:testDebugUnitTest`
Expected: PASS.

- [ ] **Step 2: Implement** per spec §5 + §3b (Display group gains Ambient switch; no ASCII headers anywhere).
- [ ] **Step 3: Verify**

Run: `./gradlew :app:testDebugUnitTest`
Expected: PASS; manual: toggle kills animation instantly (→ static gradient), biometric/display/members/categories/backup/SMS rows all work.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/moneymanager/data/prefs/UserPreferences.kt app/src/main/java/com/example/moneymanager/ui/screens/settings/SettingsScreen.kt app/src/main/java/com/example/moneymanager/ui/screens/settings/SettingsViewModel.kt app/src/main/java/com/example/moneymanager/ui/navigation/NavGraph.kt
git commit -m "feat: iOS Settings groups with ambient background toggle"
```

### Task 11: Cleanup + final gate

**Files:**
- Delete `app/src/main/java/com/example/moneymanager/ui/ascii/AsciiArt.kt` ONLY if zero references remain; else leave it (no dead-code risk allowed).
- Modify: none expected; remove any leftover `Chroma.*` usages in converted screens only.

- [ ] **Step 1: Confirm no ASCII/Chroma references in converted screens**

Run: `grep -rn "Ascii\|Chroma" app/src/main/java/com/example/moneymanager/ui/ || echo CLEAN`
Expected: CLEAN (or only `theme/Chroma*.kt` definitions themselves).

- [ ] **Step 2: Full gate**

Run: `./gradlew :app:testDebugUnitTest`
Expected: PASS (all suites green).

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "chore: remove superseded Chroma/ASCII usages after iOS reskin"
```

## Self-Review

- Spec coverage: §3 tokens → Task 1; §3b ambient → Tasks 2/4/10; §4 kit → Task 3; §5 screens → Tasks 5–10; §5b logo done already; §7 guardrails → Review Focus tests + manual checks in Tasks 3/4/5/10.
- Steps are single-action with exact commands and expected outputs; no bodies beyond signatures and spec values.
- Type consistency: `IosScheme` field names and `resolveAmbientMode(animatorScale: Float, userEnabled: Boolean)` signatures are identical everywhere used (Tasks 1/2/4/10).
- Proportion: plan ≈ spec length; code bodies left to implementers.
