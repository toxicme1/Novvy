# Novvy — Design System

This document describes the visual design of the Novvy Android app: colours,
typography, shapes, spacing and the reusable components. Use it as the map for
requesting changes — every section names the source file to edit.

> Source of truth: `android/app/src/main/java/com/novvy/app/ui/theme/` and
> `android/app/src/main/java/com/novvy/app/ui/components/`.

---

## 1. Design language

- **Platform:** Jetpack Compose + Material 3 (Material You).
- **Feel:** premium, calm, intelligent, trustworthy — generous spacing, large
  rounded cards, soft elevation, clear hierarchy.
- **Themes:** full light and dark, following the system by default and
  overridable in Settings → Appearance (System / Light / Dark).
- **Layout rule:** every screen is a single scrolling column of cards; no dense
  dashboards, no per-agent chat threads.

---

## 2. Colour system

### 2.1 Brand colours (`Color.kt`)

| Token | Hex | Role |
|-------|-----|------|
| `Violet` | `#6C4CF1` | Primary brand / main actions |
| `VioletDark` | `#5A3BE0` | Violet shade |
| `Cyan` | `#22D3EE` | Secondary accent |
| `Coral` | `#FF8A5B` | Tertiary accent |

### 2.2 Semantic colours (`Color.kt`)

| Token | Hex | Role |
|-------|-----|------|
| `SuccessGreen` | `#22C55E` | Complete tasks, success |
| `WarningAmber` | `#F59E0B` | Approval required / low confidence |
| `ErrorRed` | `#EF4444` | Failed tasks, urgent, destructive |
| `PausedOrange` | `#FB923C` | Paused state |

### 2.3 Light scheme (`Theme.kt` → `LightColors`)

| Role | Hex |
|------|-----|
| background | `#F7F6FC` (soft off-white, violet tint) |
| surface | `#FFFFFF` |
| surfaceVariant | `#EEECF8` |
| onSurface | `#1A1A2E` |
| onSurfaceVariant | `#5A5A72` |
| outline | `#DAD7EA` |
| primary | `#6C4CF1` |
| primaryContainer | `#E4DEFF` |
| secondary | `#22D3EE` |
| secondaryContainer | `#C7F5FC` |
| tertiary | `#FF8A5B` |
| tertiaryContainer | `#FFDBCB` |
| error | `#EF4444` |

### 2.4 Dark scheme (`Theme.kt` → `DarkColors`)

| Role | Hex |
|------|-----|
| background | `#0F0F17` (deep charcoal) |
| surface | `#17171F` |
| surfaceVariant | `#23232F` |
| onSurface | `#E8E8F0` |
| onSurfaceVariant | `#A0A0B8` |
| outline | `#33334A` |
| primary | `#B9A8FF` |
| primaryContainer | `#4A31C4` |
| secondary | `#22D3EE` |
| secondaryContainer | `#0B4A52` |
| tertiary | `#FF8A5B` |
| tertiaryContainer | `#6B2A00` |
| error | `#FF8A80` |

### 2.5 Where each colour is used

| Colour | Used in |
|--------|---------|
| **Primary (violet)** | Send button, primary buttons ("Next", "View result", "Save to Drive", "Summarize"), selected bottom-nav indicator, running agent icon/progress, unread dot, links, "Default" pill, "key saved" text |
| **Secondary (cyan)** | Quick-action chips (container `#C7F5FC` light / `#0B4A52` dark) |
| **Tertiary (coral)** | Reserved for creative/special highlights (currently minimal usage) |
| **Success green** | Completed agent card icon + "Complete" state pill |
| **Warning amber** | "Needs approval" state, approval strip inside agent cards, approval dialog accents |
| **Error red** | Failed state, urgent email icon, Stop button icon |
| **Paused orange** | Paused state pill |
| **onSurfaceVariant** | Secondary text, metadata, mic/attach icons, empty-state subtitles |

To change colours: edit `Color.kt` (raw values) and `Theme.kt` (role mapping).

---

## 3. Typography (`Type.kt`)

Platform default typeface (no custom font bundled). Scale:

| Style | Size / Weight | Used for |
|-------|---------------|----------|
| `displaySmall` | 34sp Bold | (reserved) |
| `headlineSmall` | 22sp SemiBold | Screen titles ("Good evening, Abel", "Settings", task titles) |
| `headlineMedium` | 26sp SemiBold | (reserved) |
| `titleLarge` | 20sp SemiBold | Top-bar titles ("Live task", "Agent detail", "Result") |
| `titleMedium` | 16sp SemiBold | Card titles, agent names, section labels |
| `bodyLarge` | 16sp | (reserved) |
| `bodyMedium` | 14sp | Body text, descriptions |
| `bodySmall` | 12sp | Metadata, subtitles, source lines |
| `labelLarge` | 14sp Medium | Buttons, chips |
| `labelMedium` | 12sp Medium | Pills, badges |

---

## 4. Shapes & spacing

| Element | Corner radius | File |
|---------|---------------|------|
| Large cards (agent, task, file, email, provider) | 18–20dp | `Components.kt`, screens |
| Prompt card | 24dp | `Components.kt` |
| Chips (quick action, theme, state pill) | 50dp (pill) | `Components.kt`, `SettingsScreen.kt` |
| Buttons | 14–16dp | screens |
| Icon containers | 9–14dp | screens |

- Screen padding: **20dp** horizontal.
- Card inner padding: **14–18dp**.
- Spacing between cards: **12dp**; between sections: **14–30dp**.
- Touch targets: ≥ 48dp (send button is 48dp).
- Bottom navigation: Material 3 `NavigationBar`, 5 items.

---

## 5. Reusable components (`ui/components/Components.kt`)

| Component | Purpose |
|-----------|---------|
| `NovvyPromptCard` | Rounded prompt input with mic, attach and circular send button |
| `AgentStatusCard` | Animated agent card: icon, name, action, state pill, progress bar, approval strip |
| `QuickActionChip` | Pill chip for quick actions |
| `NovvyBottomNavigation` | Bottom nav (Home / Tasks / Files / Inbox / Settings) |
| `ProgressBar` | Thin rounded animated progress bar |
| `StatePill` | Coloured status pill (Queued/Running/Complete/…) |
| `SectionTitle` | Section heading |
| `EmptyState` | Icon + title + subtitle empty view |
| `agentIcon(kind)` | Maps an `AgentKind` to its icon |

Agent card state colours and behaviour live in `AgentStatusCard` + `stateColor()`.

---

## 6. Animation inventory

| Animation | Where |
|-----------|-------|
| Agent card entrance (staggered 90ms, fade + slide) | `LiveTaskScreen.kt` (`StaggeredAgentCard`) |
| Pulsing icon while running | `AgentStatusCard` |
| Animated progress bar | `ProgressBar` |
| Onboarding step fade transition | `OnboardingScreen.kt` (`AnimatedContent`) |
| Approval dialog | Material `AlertDialog` (springs from system) |

Reduced-motion is not yet explicitly handled; consider honouring
`Settings.Global.ANIMATOR_DURATION_SCALE` if required.

---

## 7. Source-file map for changes

| Want to change… | Edit this file |
|-----------------|----------------|
| Colours / brand palette | `ui/theme/Color.kt`, `ui/theme/Theme.kt` |
| Fonts / text sizes | `ui/theme/Type.kt` |
| Bottom navigation items | `ui/components/Components.kt` (`NovvyTab`, `NovvyBottomNavigation`) |
| Prompt card | `ui/components/Components.kt` (`NovvyPromptCard`) |
| Agent card look/behaviour | `ui/components/Components.kt` (`AgentStatusCard`, `stateColor`) |
| Quick actions list | `ui/screens/HomeScreen.kt` (`quickActions`) |
| Onboarding copy/pages | `ui/screens/OnboardingScreen.kt` (`steps`) |
| Agent engine wording/flow | `domain/agent/CoordinatorEngine.kt` |
| Mock agent output text | `domain/agent/CoordinatorEngine.kt` (`fallbackContent`) + `domain/provider/AIProvider.kt` |
| App entry / theme switch | `MainActivity.kt` |
| Navigation routes | `ui/NovvyApp.kt` |

See `docs/SCREENS.md` for a per-screen layout breakdown.