# Novvy — Screen Layouts

Per-screen layout breakdown: what is on screen (top → bottom), which components
and colours are used, and the file to edit. The app has **no global top app bar** —
each screen draws its own header row (back button + title). The bottom navigation
is always visible.

Root shell: `ui/NovvyApp.kt`
```
Column(fillMaxSize, systemBarsPadding)
├── Box(weight 1f) { NavHost(...) }   // current screen
└── NovvyBottomNavigation(...)         // Home · Tasks · Files · Inbox · Settings
```

---

## 1. Onboarding — `ui/screens/OnboardingScreen.kt`

Centered column (padding 28):

1. 88dp rounded-28 box with **Violet→Cyan gradient** + white `AutoAwesome` icon.
2. `AnimatedContent` step:
   - step icon (30dp, `primary`)
   - title (`headlineSmall`)
   - body (`bodyMedium`, `onSurfaceVariant`)
3. Page dots — active `22×8` `primary`, inactive `8×8` `outline`.
4. Full-width button (`height 52`, radius 16): **Next** → **Get started**.
5. `TextButton`: **Skip for now**.

Pages are defined in the `steps` list (icon / title / body).

---

## 2. Home — `ui/screens/HomeScreen.kt`

`LazyColumn` (horizontal padding 20, spacing 14):

1. **Header** — `Good {morning|afternoon|evening|night}, {name}` (`headlineSmall`)
   + tagline `Your AI team for real work.` (`bodyMedium`, `onSurfaceVariant`).
2. **Account chips** — `Gmail`, `Drive`; `primary` when connected else
   `onSurfaceVariant` (background at 12% alpha).
3. **`NovvyPromptCard`** — input card.
4. **Quick actions** — horizontal `LazyRow` of `QuickActionChip`s
   (Plan my week, Summarize email, Create document, Save to Drive, Draft reply,
   Compare options, Make a budget, Study plan). List is `quickActions`.
5. **SectionTitle** `Recent tasks`.
6. Empty card (`AutoAwesome` + "No active tasks yet…") **or** task rows
   (title, request, status chip, chevron).

`NovvyPromptCard`: rounded-24 card, `OutlinedTextField` (min 2 / max 5 lines,
radius 16) + row of mic + attach icons and a 48dp circular **send** button
(`primary` when text present, else `surfaceVariant`).

Quick action chips use `secondaryContainer` background.

---

## 3. Tasks (tab) — `ui/NovvyApp.kt` (`TasksScreen`)

`LazyColumn` (padding 20, spacing 12):
1. `Tasks` (`headlineSmall`).
2. Empty card (`AutoAwesome` + "No tasks yet…") **or** `TaskCard` list
   (title, request, status chip, chevron).

---

## 4. Live Task — `ui/screens/LiveTaskScreen.kt`

Column (`fillMaxSize`):

1. **Header row** — back `IconButton`, `Live task` (`titleLarge`), and while
   running: pause/play + **stop** (`error`) icon buttons.
2. `LazyColumn` (padding 20, spacing 12):
   - task title (`headlineSmall`) + request (`bodyMedium`, `onSurfaceVariant`)
   - if completed: full-width **View result** button (`primary`)
   - **agent status cards** (`StaggeredAgentCard` → `AgentStatusCard`)
   - **CoordinatorBubble** (violet circle + `primaryContainer` bubble)

**Agent card** (`AgentStatusCard`): rounded-20 card; 40dp icon circle (state
colour at 14% alpha); name (`titleMedium`) + action (`bodySmall`); green check
when complete else `StatePill`; animated `ProgressBar` while running; amber
approval strip when `needsApproval`.

**State colours:** Queued `onSurfaceVariant` · Running `primary` ·
Needs approval `#F59E0B` · Complete `#22C55E` · Failed `#EF4444` ·
Paused `#FB923C` · Cancelled `onSurfaceVariant`.

**Approval dialog:** Material `AlertDialog` (title, summary, detail in a
`surfaceVariant` box, **Approve** / **Cancel**).

---

## 5. Agent Detail — `ui/screens/AgentDetailScreen.kt`

Scroll column (padding 20, spacing 12):
1. back + `Agent detail` (`titleLarge`).
2. Icon circle 52dp (state colour) + name (`headlineSmall`) + tagline + `StatePill`.
3. Current action in a rounded-16 `surfaceVariant` box (`titleMedium`).
4. **What it found / Draft** section (`Surface`, rounded 16).
5. **Sources** list (if any).
6. If `WAITING_APPROVAL`: text + **Approve** / **Cancel** buttons.
7. If `FAILED`: **Retry task** button.

---

## 6. Result — `ui/screens/ResultScreen.kt`

Scroll column (padding 20, spacing 14):
1. back + `Result` (`titleLarge`).
2. Title (`headlineSmall`) + `Prepared by N agents` (`bodySmall`).
3. Rounded-20 `Surface` containing a lightweight markdown render
   (`#` heading, `##` subheading, `-`/`•` bullets).
4. Action rows:
   - **Save to Drive** (`primary`) / **Create PDF** (outlined)
   - **Copy** / **Share** (outlined)
5. **Create email draft** (outlined).

---

## 7. Files — `ui/screens/FilesScreen.kt`

`LazyColumn` (padding 20, spacing 12):
1. `Files & documents` (`headlineSmall`) + subtitle.
2. Empty state **or** `FileCard` per file: rounded-18 card, 42dp icon
   (`primary` @12%), name (`titleMedium`), folder + `FORMAT · size`
   (`bodySmall`, `onSurfaceVariant`), `Cloud` icon (`primary`).

---

## 8. Inbox — `ui/screens/InboxScreen.kt`

`LazyColumn` (padding 20, spacing 12):
1. `Inbox` (`headlineSmall`) + `N unread messages` (or "You're all caught up").
2. Full-width **Summarize unread with Novvy** button (`primary`).
3. Empty state **or** `EmailCard` per email: 38dp circle
   (`errorContainer` if urgent else `primaryContainer`) with `PriorityHigh`/`Email`
   icon; sender (`titleMedium`, bold if unread) + unread dot; subject
   (`bodyMedium`); preview (`bodySmall`, 2 lines); **Urgent** chip (`error`).

---

## 9. Settings — `ui/screens/SettingsScreen.kt`

Scroll column (padding 20, spacing 10):
1. `Settings` (`headlineSmall`).
2. **Profile** — name `OutlinedTextField` (radius 12).
3. **Appearance** — three chips **System / Light / Dark** (`primary` when selected).
4. **AI Providers** — a `ProviderCard` per provider + **+ Add provider**.
5. **Connected accounts** — `Gmail` and `Google Drive` rows with a `Switch`.
6. **About** — version, tagline, note that keys are stored encrypted.

`ProviderCard`: name (`titleMedium`) + **Default** pill; `Model: …`; lock icon +
"Key saved securely" / "No API key"; enable `Switch`; **Edit** / **Set default**
buttons.

`ProviderEditDialog` (`AlertDialog`): API key field (password) + model field,
with a note that keys are stored encrypted and never shown again.

---

## Quick reference — where to change things

| Change | File |
|--------|------|
| Colours / brand | `ui/theme/Color.kt`, `ui/theme/Theme.kt` |
| Fonts / sizes | `ui/theme/Type.kt` |
| Home layout / quick actions | `ui/screens/HomeScreen.kt` |
| Live task / agent cards | `ui/screens/LiveTaskScreen.kt`, `ui/components/Components.kt` |
| Agent detail | `ui/screens/AgentDetailScreen.kt` |
| Result | `ui/screens/ResultScreen.kt` |
| Files | `ui/screens/FilesScreen.kt` |
| Inbox | `ui/screens/InboxScreen.kt` |
| Settings / providers | `ui/screens/SettingsScreen.kt` |
| Onboarding | `ui/screens/OnboardingScreen.kt` |
| Bottom navigation | `ui/components/Components.kt` (`NovvyTab`, `NovvyBottomNavigation`) |
| Routes / shell | `ui/NovvyApp.kt` |