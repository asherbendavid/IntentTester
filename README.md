# IntentTester

A minimal Android developer tool for composing, firing, and managing Android Intents by hand. Built for testing inter-app communication — particularly useful when debugging intent string formatting, URI schemes, or extra payloads expected by third-party apps.

---

## What it does

- Compose a full Android Intent from scratch using a form dialog
- Fire it immediately as `startActivity`, `sendBroadcast`, `startService`, or `startForegroundService`
- Save every fired intent to a persistent history list
- Re-open any history entry to edit and re-fire it
- Long-press any entry to delete it

---

## Requirements

- Android API 26 (Android 8.0) or higher
- The target app must be installed on the same device

---

## Setup

1. Clone the repository and open in Android Studio
2. Build and sideload the APK onto your device

---

## Usage

### Firing a new intent

Tap the **+** FAB in the bottom right corner. Fill in whichever fields apply to your intent and tap **Send**. The intent is fired immediately and saved to the history list.

### Re-firing from history

Tap any item in the history list. The dialog opens pre-filled with all its fields. Edit if needed, then tap **Send** to fire again. The entry is updated in place and its timestamp refreshed.

### Deleting a history entry

Long-press any item in the history list and confirm the deletion prompt.

---

## Field reference

| Field | Description |
|---|---|
| **Label** | Optional friendly name shown in the history list. If blank, the list displays the Action, or Component if Action is also blank. |
| **Dispatch type** | How the intent is fired. See [Dispatch types](#dispatch-types) below. |
| **Action** | The intent action string, e.g. `android.intent.action.VIEW` |
| **Data URI** | The URI the action operates on, e.g. `https://example.com` or `youversion://bible?reference=JHN.3.16` |
| **MIME type** | Content type, e.g. `text/plain`. If both Data URI and MIME type are set, `setDataAndType()` is called — Android requires this because setting them separately clears the other. |
| **Component** | Explicit target in `package/fully.qualified.ClassName` format. Setting this bypasses intent resolution entirely and targets one specific component. |
| **Package** | Target package only. Narrows resolution to one app without specifying the entry point. |
| **Flags** | Multi-select from all standard `Intent.FLAG_*` constants. Resolved to a bitmask at send time. |
| **Categories** | Optional category strings, e.g. `android.intent.category.BROWSABLE`. Tap **+ Add category** for each one. |
| **Extras** | Key/value pairs with explicit types (String, Int, Boolean, Long, Float). Tap **+ Add extra** for each one. Malformed values (e.g. letters in an Int field) are silently skipped. |

---

## Dispatch types

| Type | Android call | When to use |
|---|---|---|
| Start activity | `startActivity()` | Opening a screen in another app |
| Send broadcast | `sendBroadcast()` | Sending a system or app broadcast |
| Start service | `startService()` | Starting a background service |
| Start foreground service | `startForegroundService()` | Starting a service that will show a notification. Required on API 26+ for long-running services — using `startService()` for a foreground service will crash the target. |

---

## A note on flags

`FLAG_IMMUTABLE` and `FLAG_MUTABLE` are **not** in the flags list. These belong to `PendingIntent` wrappers, not to `Intent` objects themselves. When firing an intent directly (as this app does), they are irrelevant. If the intent you are testing was originally wrapped in a `PendingIntent` (e.g. a notification action), simply ignore those flags — the underlying intent fields are what matter.

---

## Design decisions

**Why API 26 minimum?**
Android 8.0 (API 26) is where the most significant intent-related restrictions landed — primarily the implicit broadcast registration block and foreground service requirements. Using this as the floor means the app's own behaviour and the restrictions it tests against are consistent.

**Why Gson/JSON instead of a database?**
The history list is small and append-only in practice. A Room database would add significant boilerplate with no benefit at this scale. A single `intent_history.json` file in internal storage is easy to inspect, back up, or clear manually if needed.

**Why does the flag chooser use a separate dialog instead of inline checkboxes?**
The full flag list is long enough to make the main form unmanageable if expanded inline. A separate multi-choice dialog keeps the main form scrollable and focused.

**Why are extras stored as strings internally?**
All extra values are stored as strings in `SavedIntent` and parsed to their declared type only at send time. This avoids type-conversion complexity in the data layer and makes Gson serialisation trivial. Malformed values (e.g. `"hello"` declared as `INT`) are silently skipped rather than crashing.

**Why does the history list sort by last used?**
For a testing tool, the most recently fired intent is almost always the one you want to fire again. Sorting by `lastUsed` descending puts it at the top without any manual reordering.

**Why no global settings?**
Every field that could be a global default (dispatch type, flags, package) is meaningfully different between intents. A global default would create false confidence that a setting is correct when it may simply be forgotten.

---

## Known limitations

- **No Parcelable or Bundle extras.** These require the receiving app's class definitions to be compiled into the sender, which is not practical for a generic test tool. Apps with public intent interfaces should not require them.
- **No file picker for Data URI.** Content URIs for images or files must be typed or pasted manually. A future version could add a file picker that resolves to a `content://` URI automatically.
- **Broadcast receivers registered in the manifest may not respond** on API 26+ targets, as Android restricts implicit broadcast delivery to manifest-registered receivers. This is a platform restriction, not a limitation of this app.

---

## Future ideas

- **File picker button** next to the Data URI field, auto-populating a `content://` URI and setting `FLAG_GRANT_READ_URI_PERMISSION` automatically
- **Intent templates** for common actions (`ACTION_VIEW`, `ACTION_SEND`, `ACTION_DIAL`) that pre-fill sensible defaults
- **Duplicate entry** option in the history list, for creating variations of an existing intent without editing the original
- **Export/import** history as JSON for sharing test suites between devices or developers
- **Result display** for ordered broadcasts that return a result code
- **Search/filter** on the history list for when the list grows long

---

## Project structure

```
app/src/main/java/cvc/dashingdog/intenttester/
├── MainActivity.kt          — RecyclerView, FAB, fragment management
├── SavedIntent.kt           — Data classes and enums
├── FlagsData.kt             — Flag name → Int mapping
├── IntentRepository.kt      — Gson read/write to intent_history.json
├── IntentViewModel.kt       — In-memory list, LiveData, persistence calls
├── IntentDispatcher.kt      — Builds and fires the Android Intent
├── HistoryAdapter.kt        — RecyclerView adapter
├── IntentDialogFragment.kt  — Add/edit dialog
└── ConfirmDeleteDialog.kt   — Delete confirmation dialog

app/src/main/res/layout/
├── activity_main.xml        — CoordinatorLayout with RecyclerView and FAB
├── dialog_intent.xml        — Scrollable intent composition form
└── item_history.xml         — History list row
```

---

## License

MIT — do whatever you want with it.
