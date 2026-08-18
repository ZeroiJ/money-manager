# Money Manager

A personal, offline Android app for tracking day-to-day and household
spending. Built for a single user — no accounts, no cloud, no server.

## Why
Most expense trackers assume shared/multi-device use or push you toward a
subscription. This is a private, local-first app: your data never leaves
your phone, and household spend tracking (shared rent, groceries, bills)
is a first-class feature, not an afterthought — built for Indian day-to-day
spending patterns (UPI, cash, common recurring bills).

## Stack
- Kotlin + Jetpack Compose
- Room (SQLite), fully offline
- MVVM, Coroutines/Flow, Compose Navigation
- WorkManager for recurring-expense reminders
- Material 3, dark mode default

## Features
- Fast expense entry (< 5s): calculator-style amount input, icon-grid
  categories, payment mode (UPI/cash/card)
- Personal vs household tagging, with simple split tracking
- Budgets per category per month
- Recurring expenses (rent, WiFi, subscriptions) with reminders
- Daily/weekly/monthly views + calendar heatmap
- Reports: category breakdown, spend trend, personal vs household split
- CSV/JSON export for manual backup

## Project docs
- `AGENTS.md` — standing context and conventions for AI coding agents
  working in this repo (stack, commands, deny rules, data model)
- `docs/money-manager-spec.md` — full feature/architecture spec

## Status
Early build — scaffolding in progress via Google Antigravity.
