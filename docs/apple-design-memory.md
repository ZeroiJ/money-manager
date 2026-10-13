# Apple Design memory (persistent — survives restarts)

Skill installed at: `/home/zeroij/.agents/skills/apple-design`
Source: https://github.com/dickwu/apple-design-skill.git (shallow-cloned 2026-10-13)
Role per its SKILL.md: senior HIG reviewer + studio design lead. Two jobs:
keep the design honest against the platform, keep it from looking templated.

## How to use it in this repo (Compose translation)

The skill speaks SwiftUI/UIKit; translate, never water down (`cross-platform.md`):
- Tab bar → Material3-style custom `IosTabBar` (tabs navigate, never act)
- Sheet → `GlassSheet` (ModalBottomSheet): one at a time, grabber/drag handle,
  swipe-to-dismiss, Cancel/Close always paired with any Done/Commit
- Safe area → `WindowInsets.safeDrawing`; keyboard guide → `WindowInsets.ime`
- Dynamic Type → system font scaling (verified up to 1.3x on-device)
- VoiceOver → TalkBack (content descriptions on icon buttons)
- Liquid Glass → translucency ONLY on floating functional layer (tab bar,
  sheets), never in content (`liquid-glass.md`). Our take: solid bar + hairline.

## Rules that now govern this app

0. **Visual identity = Kinetic Ledger** (PRD `kinetic_ledger_prd_project_brief.md`,
   plan `docs/superpowers/plans/2026-10-13-kinetic-ledger-redesign.md`), the
   apple-design craft rules below sit ON TOP of it (they govern how, not what):
   sage canvas `#B5BFAE` / paper `#F3FCEA` / slab `#1E241C`, ochre `#D9C852`,
   terracotta `#D76449`, maroon `#4A1510`, Obsidian dark; Space Grotesk UI +
   PaperMono tabular figures (Playfair serif retired); single-decimal
   `₹1,951.0` w/ greyed decimal; barcode tick meters; **strictly boxes** —
   ZERO rounded corners anywhere (RoundedCornerShape banned, use
   `RectangleShape`; toggles are square sliders, not pills) since v1.16.0;
   3-tab IA `01 HOME / 02 EXPENSE / 03 REPORTS` (Budget tab removed in
   v1.16.0 — budget data still backed up/exported); Home's recent ledger
   is edge-to-edge single-tone slab rows, hairline-separated, no icons;
   ochre/terracotta surfaces take FIXED ink text in both modes.

1. **Edge-to-edge contract** (from `layout.md › Guides and safe areas` +
   `layout.md › Visual hierarchy`): background extends full-bleed beneath all
   bars; content owns top+horizontal safe insets via Scaffold defaults;
   `IosTabBar` owns bottom+horizontal (`safeDrawing.only(Horizontal+Bottom)`),
   never double-padded. Sheet/form content carries `.imePadding()` so the
   keyboard never covers the commit action (`virtual-keyboards.md › Mobile`).
2. **One color = one thing** (`Lens 3`): blue strictly for actions; amounts
   monochrome + red/green semantics only; charts/heatmap keep data palettes.
3. **One UI voice + one figure voice** (`Lens 3`, Kinetic PRD): Space Grotesk
   for all words (serif heroes retired), PaperMono for all figures — never
   mix a third family in.
4. **No decorative gradients in content** (Origin/Public anti-pattern).
   Ambient = frozen near-monochrome texture + grain, toggle = texture on/off.
5. **Motion ~0.2s, purposeful, reduced-motion aware** (`motion.md` + Nessie log):
   rolling figures, press-shrink, one-shot confetti; all gated by
   `resolveAmbientMode` / animator scale.
6. **Errors say what/why/how-to-fix in the UI voice** (Lens 5 + v1.13.0):
   `ErrorBus` → snackbar + Diagnostics. No apologizing, no silent failures.
7. **Template check before every visual change** (`Lens 3 craft`): warm-cream +
   serif + terracotta, near-black + acid accent, hairline broadsheet — if the
   plan reads as a default, revise and say why. Boldness in exactly one place.

## Review cadence

Before any release: portrait + landscape + keyboard-open + 1.3x text screenshots;
contrast computed from hex (never eyeballed); fresh-reviewer pass on the diff.
