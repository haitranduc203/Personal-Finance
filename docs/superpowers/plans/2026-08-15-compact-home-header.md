# Compact Home Header Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove the redundant top inset on Home while preserving its two-line greeting header and existing controls.

**Architecture:** `FinTrackAppContent`'s `Scaffold` supplies safe-area padding to `FinTrackNavHost`, so `HomeScreenContent` must not apply a second status-bar inset. The change remains local to the Home layout; no navigation, state, or click handling changes.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Gradle Android application plugin.

## Global Constraints

- Preserve the copy `Xin chào 👋` and `FinTrack Dashboard`.
- Preserve the selected-month pill and notification button behavior, color, and accessibility labels.
- Change only the Home header/layout spacing in production code.
- Verify with the module's Kotlin debug compilation and a whitespace diff check.

---

### Task 1: Remove the duplicate Home status inset and tighten local spacing

**Files:**
- Modify: `app/src/main/java/com/fintrack/app/ui/screens/home/HomeScreen.kt:16,148-207`
- Test: Gradle task `:app:compileDebugKotlin`

**Interfaces:**
- Consumes: `Scaffold`-provided `innerPadding` applied by `FinTrackApp` to `FinTrackNavHost`.
- Produces: `HomeScreenContent` header with a single safe-area inset, greeting, title, month selector, and notification button unchanged.

- [ ] **Step 1: Inspect the spacing contract before editing**

Confirm that `FinTrackApp` calls `FinTrackNavHost(modifier = Modifier.padding(innerPadding))` and that `HomeScreenContent` adds `.statusBarsPadding()` to its header. This proves that the safe-area inset is duplicated.

- [ ] **Step 2: Apply the minimal header layout change**

Remove the `statusBarsPadding` import and matching modifier call. Keep the `Column` for the two text lines and action `Row` intact. Change header padding from `vertical = 6.dp` to `vertical = 4.dp`, then set `LazyColumn` top content padding from `4.dp` to `0.dp`.

- [ ] **Step 3: Compile the Debug Kotlin sources**

Run `.\gradlew.bat :app:compileDebugKotlin`.

Expected: `BUILD SUCCESSFUL` and no Compose/Kotlin errors.

- [ ] **Step 4: Validate the patch is narrowly scoped**

Run `git diff --check` and inspect the HomeScreen diff.

Expected: no whitespace errors; only the approved header spacing changes appear.

- [ ] **Step 5: Commit the production change separately**

Run `git add -- app/src/main/java/com/fintrack/app/ui/screens/home/HomeScreen.kt` followed by `git commit -m "style: compact home header spacing"`.
