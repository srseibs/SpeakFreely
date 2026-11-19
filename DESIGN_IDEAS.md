# SpeakFreely - Design & Architecture Ideas

A living document to capture approved design decisions, architectural patterns, and feature ideas
for **SpeakFreely**.

---

## Approved Design Decisions

### 1. Standardized Language Terminology & Asymmetric Pipeline Model

* **Status**: Approved
* **Category**: Domain & Architecture Naming
* **Standard Terminology**:
  - `ownerLanguage`: The primary language of the phone owner (e.g. `en-US`).
  - `foreignInputLanguage`: The incoming language spoken into the microphone (e.g. `ja-JP` or
    `es-MX`).
  - `foreignOutputLanguage`: The outgoing target language translated to and spoken via TTS (e.g.
    `es-ES` or `de-DE`).

### 2. `UiText` Pattern for Localized UI State & Messages

* **Status**: Approved
* **Category**: Presentation & ViewModel State
* **Problem**: Passing raw `String` resolved via `Context.getString()` in ViewModels locks strings
  to the current locale and breaks pure ViewModel unit testing without Android mocks.
* **Decision**: Use a `UiText` sealed interface (`DynamicString` vs `StringResource`) for all UI
  labels, error messages, and validation state passed from ViewModels.
* **Benefits**:
    - ViewModels remain 100% Android-decoupled and easy to unit test.
    - UI automatically updates when the system locale or language changes at runtime.

### 3. Device Scope & Orientation Strategy

* **Status**: Approved
* **Category**: Presentation / Responsive Layout
* **Primary Target**: Optimized specifically for **Phone in Portrait Mode**.
* **Secondary Target**: Responsive baseline for tablets/foldables in portrait mode (using standard
  Compose flexible layouts like `Modifier.weight()`, `fillMaxSize()`, and `widthIn(max = 600.dp)` to
  prevent text stretching).
* **Scope Restriction**: No dedicated landscape or multi-pane / side-by-side tablet layout
  optimizations are required.

### 4. 4-Block Global Engine Pipeline & Turn Reversal Flow

* **Status**: Approved
* **Category**: Core Data Processing Architecture
* **The 4 Modular Processing Blocks**:
  1. **STT (Speech-to-Text)**: Converts incoming microphone audio $\rightarrow$ text.
  2. **Translate 1**: Translates `Foreign Input Text` $\rightarrow$ `Owner Language`.
  3. **Translate 2**: Translates `Owner Language Text` $\rightarrow$ `Foreign Output Language`.
  4. **TTS (Text-to-Speech)**: Synthesizes `Foreign Output Text` $\rightarrow$ audio played out of
     loudspeaker.
* **Orchestration Rules**:
  - **Single Universal Text Rule**: Any text displayed on screen is editable (`TextField`) before
    downstream translation or TTS synthesis.
  - **Reverse Languages (`⇄`) Action**: Swaps active input and output language roles in 1 tap,
    automatically reversing both STT recognition language and TTS audio playback language.
  - **Dynamic Bypassing**: The ViewModel orchestrator dynamically bypasses unused Translate nodes
    depending on turn direction and language equality.

### 5. Production Technology Stack (STT, Translation, TTS)

* **Status**: Approved
* **Category**: Core Dependencies & Framework Engines
* **Baseline Constraints**: `minSdk = 24` (Android 7.0+), 100% free, 0 MB API subscription costs, 0
  MB unnecessary APK bloat.
* **Engine Selections**:
  - **Speech-to-Text (STT)**: Android native `SpeechRecognizer` API
    (`android.speech.SpeechRecognizer`). Uses `createOnDeviceSpeechRecognizer` on Android 12+ (API
    31+) with fallback for Android 7.0–11. Emits live partial results.
  - **Text Translation**: Google ML Kit On-Device Translation SDK (`com.google.mlkit:translate`).
    100% on-device local model execution (~30 MB / language pack downloaded on demand), zero rate
    limits, zero throttling.
  - **Text-to-Speech (TTS)**: Android native `TextToSpeech` API (`android.speech.tts.TextToSpeech`).
    Uses pre-installed Android system voice packs for offline synthesis with speech rate & pitch
    controls.

### 6. Detected Language Indicator & Manual Override

* **Status**: Approved
* **Category**: Presentation & STT Diagnostics
* **Problem**: Auto-detecting input speech on short phrases (e.g. *"Si"*, *"Uno"*, *"Ciao"*) often
  misidentifies language variants (e.g. Spanish vs Portuguese vs Italian), leaving the user with
  incorrect translations and no diagnostic feedback.
* **Decision**:
  - **Detected Language Badge**: Display an explicit `INPUT · Detected: 🇲🇽 Spanish` label in the
    input card header whenever Auto-Detect is active.
  - **Manual Override Selector**: Tapping `[ 🌐 Auto-Detect ]` allows the user to manually override
    and lock an explicit input language (e.g., `es-MX`) to prevent Auto-Detect errors.

### 7. 2-Card Screen Layout & `TRANSLATION` Card Specification

* **Status**: Approved
* **Category**: UI Specification & Card Architecture
* **Card Positioning**:
  - **Top Card (`INPUT`)**: Captures incoming STT speech or text input. Header dynamically reflects
    auto-detected status (e.g. `INPUT · Auto-detecting language into English...`).
  - **Bottom Card (`TRANSLATION`)**: Displays translated result text + Play button `▶` at
    bottom-right. Header reflects target language (e.g. `TRANSLATION · Spanish (MX)`).
* **Zero Top Bar Clutter**: Eliminates top language bar dropdowns completely.
* **Input Always Auto-Detect**: `INPUT` always auto-detects incoming speech without requiring manual
  language selection.
* **Dynamic Reverse Action Button**: Positioned between cards
  (`[ ⇄ Set TRANSLATION to Spanish (MX) ]`), allowing 1-tap target language reversal.
* **Play Button (`▶`) Placement**: Positioned at bottom-right of `TRANSLATION` card, giving explicit
  user control over TTS audio playback.

---

## Use Cases & User Flows

### Use Case 1: One-Way Foreign Listening (Listen & Read)

* **Goal**: Allow the Owner to immediately understand spoken foreign speech in real-time by
  listening to a foreign speaker and reading the translated output on screen in the Owner's
  language.
* **Actors**:
  - **Owner**: Primary phone owner / reader (`ownerLanguage`).
  - **Foreign Speaker**: Speaker talking in foreign language (`foreignInputLanguage`).
* **Sequence**:
  1. **Owner Opens App & Listens**: Owner points/taps mic to listen to Foreign Speaker.
  2. **Transcription & Translation**: App transcribes foreign audio
     (`foreignInputLanguage`) $\rightarrow$ translates text into `ownerLanguage`.
  3. **Owner Reads**: English/Owner translation appears on screen in large, clear text.
  4. **Bypass TTS**: `Translate 2` and `TTS` are bypassed (zero audio playback required).
* **Key Technical Requirements**:
  - Fast single-pass STT + Translate 1 pipeline.
  - Zero audio feedback risk (TTS bypassed).

### Use Case 2: Face-to-Face Shared Phone Conversation (Turn-Taking)

* **Goal**: Enable seamless face-to-face conversation between two people speaking different
  languages using a single shared phone.
* **Actors**:
  - **Owner**: Primary phone owner / English speaker (`ownerLanguage`).
  - **Foreign Speaker**: Conversation partner across the table (`foreignInputLanguage` /
    `foreignOutputLanguage`).
* **Turn Sequence**:
  1. **Foreign Speaker Speaks (Foreign Input Language)**: Foreign speaker speaks into the microphone
     in Spanish/Japanese.
  2. **Transcription & Translation**: App transcribes foreign audio → translates text into
     Owner Language (English).
  3. **Owner Reads (Owner Language)**: Owner reads the English translation on the screen.
  4. **Owner Responds (Owner Language)**: Owner taps/speaks a response in English.
  5. **Transcription, Translation & TTS**: App transcribes English audio → translates into Foreign
     Output Language text → generates Foreign Output Text-to-Speech (TTS) audio.
  6. **Playback to Foreign Speaker**: App plays back the Foreign Output TTS audio so Foreign Speaker
     hears the response (and displays foreign text on screen).
* **Key Technical Requirements**:
    - Speech-to-Text (STT) for both languages.
    - Text Translation engine.
    - Text-to-Speech (TTS) playback engine.
    - Turn-taking state machine (Idle → Listening → Translating → Playing).
  - Manual TTS replay trigger (in case Foreign Speaker needs to hear it again).

### Use Case 3: Hands-Free Continuous Conversation Mode

* **Goal**: Allow two speakers to converse continuously without manually tapping record/stop buttons
  between phrases.
* **Actors**: Owner (English) and Foreign Speaker placing the phone between them.
* **Turn Sequence**:
  1. **Session Start**: Users select languages (`ownerLanguage` <-> `foreignInputLanguage` /
     `foreignOutputLanguage`) and tap "Start Continuous Mode".
  2. **Continuous Audio Monitoring**: Microphone streams continuously with Voice Activity
     Detection (VAD).
  3. **Automatic Speech Segment Detection**: When Foreign Speaker finishes speaking (detected via ~
     1.0s silence pause), app transcribes, translates, and displays/speaks in Owner Language.
  4. **Automatic Response Capture**: When Owner speaks, app detects speech pause, transcribes,
     translates to Foreign Output Language, and plays Foreign Output TTS.
  5. **Continuous Loop**: Repeats automatically back and forth until the user pauses or ends the
     session.
* **Key Technical Requirements**:
    - Voice Activity Detection (VAD) / Silence segmentation (~800ms–1200ms threshold).
    - Acoustic Echo Cancellation (AEC) & microphone muting during TTS playback to prevent
      self-translation.
    - Automatic Language Identification or dual STT pipelines.
    - Barge-in / Interruptible TTS support (tapping screen or speaking stops TTS playback
      immediately).

### Use Case 4: Offline Mode (Travel & Low/No Connectivity)

* **Goal**: Allow users to perform real-time speech translation and TTS playback when traveling
  without cellular data or internet connection.
* **Actors**: Owner traveling abroad (e.g. at an airport, remote location, or on airplane mode).
* **Workflow**:
  1. **On-Demand Model Download**: Owner downloads language packs ahead of time over Wi-Fi (~30 MB
       per language via Google ML Kit On-Device Translation).
  2. **Offline Speech Recognition**: App uses Android system `SpeechRecognizer` on-device models
       or embedded `whisper-tiny` model (~39 MB).
  3. **Offline Translation**: ML Kit translates text between downloaded model pairs on-device.
  4. **Offline TTS Playback**: Android system `TextToSpeech` engine plays translated audio without
       internet.
* **Key Technical Requirements**:
    - Google ML Kit On-Device Translation SDK (`com.google.mlkit:translate`).
    - Download Manager & Model Status Observer (`isModelDownloaded()`, `downloadModelIfNeeded()`).
    - Wi-Fi-only download constraint option.
    - Seamless fallback: Automatically use local ML Kit models if network is unreachable.

### Use Case 5: Text Input Fallback & Transcript Correction

* **Goal**: Provide manual keyboard input and transcript editing so mispronunciations, heavy
  accents/dialects, proper nouns, or noisy environments do not block translation.
* **Actors**: Owner or Foreign Speaker encountering speech recognition errors or needing quiet/text
  input.
* **Workflow**:
    1. **Scenario A — Transcript Correction**: STT transcribes speech, but mishears a word due to
       accent/dialect or proper noun. User taps the transcript text box, edits the incorrect word,
       and hits "Translate".
    2. **Scenario B — Direct Keyboard Entry**: User is in a noisy/quiet environment or prefers
       typing. User taps the keyboard icon, types their message in Owner or Foreign Language, and
       taps "Translate & Speak".
    3. **Scenario C — Regional Dialect Selection**: User selects specific BCP-47 locale variants
       (e.g. `es-MX`, `es-ES`, `es-AR`, `en-US`, `en-GB`, `en-IN`) to optimize STT/TTS for regional
       accents.
* **Key Technical Requirements**:
    - Editable Compose `TextField` for STT transcript buffer before final translation.
    - Keyboard toggle affordance in UI.
    - Regional dialect locale selector (`Locale` variant support in STT and TTS engines).

---

## Configuration & User Settings Specification

To support these use cases seamlessly, **SpeakFreely** requires the following persistent settings
(managed via `DataStore Preferences`):

### 1. Language & Regional Dialects

- **Owner Language & Dialect**: Primary phone owner language code + region (e.g. `en-US`, `en-GB`,
  `en-IN`).
- **Foreign Input Language & Dialect**: Incoming speech language code + region (e.g. `es-MX`,
  `ja-JP`).
- **Foreign Output Language & Dialect**: Outgoing translated speech/TTS language code + region (e.g.
  `es-ES`, `de-DE`). Default: Matches `foreignInputLanguage`.

### 2. Audio & Speech-to-Text (STT) Settings

- **Conversation Mode Default**: `TURN_TAKING` (manual button) vs `CONTINUOUS_HANDS_FREE` (VAD
  auto-detect).
- **VAD Silence Threshold**: Duration of silence required to trigger phrase completion (`0.8s` fast,
  `1.2s` normal, `1.8s` slow).
- **Acoustic Echo Cancellation**: Enable hardware/software echo suppression to prevent mic from
  listening to local TTS speaker.

### 3. Text-to-Speech (TTS) Voice & Playback

- **Auto-Play TTS**: `Boolean` (True = automatically play translated audio out loud; False = display
  text only until tapped).
- **TTS Speech Rate**: Playback speed multiplier (`0.8x` slow, `1.0x` normal, `1.2x` fast).
- **TTS Voice Preference**: Selected voice gender/variant per language.

### 4. Connectivity & Offline Download Settings

- **Translation Engine Priority**: `PREFER_CLOUD` (maximum accuracy/nuance) vs `OFFLINE_ONLY`
  (maximum privacy & zero data usage).
- **Wi-Fi Only Downloads**: `Boolean` (Restrict ML Kit language model downloads to Wi-Fi
  connections).
- **Offline Models Storage Manager**: Status and management of downloaded language packs (~30 MB /
  pack).

### 5. UI & Layout Preferences

- **Dual-Direction Display (180° Flip)**: `Boolean` (Flips the top half of the screen upside down so
  User 2 across the table can read their text right-side up).
- **Font Size Modifier**: Normal, Large, Extra Large (for accessibility and quick reading across a
  table).
- **Show Original STT Transcript**: `Boolean` (Toggle whether the original spoken transcript is
  displayed alongside the translated result).

---

## Screen Architecture & Navigation Destinations

The core navigation structure for **SpeakFreely** consists of three primary screens/destinations,
supplemented by modal sheets:

### 1. Splash Screen (`Destination.Splash`)

* **Purpose**: Smooth app entry using Android 12+ `SplashScreen` API.
* **Responsibilities**:
  - Initializes DataStore preferences, Koin DI, and STT/TTS engine components.
  - Checks microphone permissions (`RECORD_AUDIO`).
  - Navigates automatically to `Destination.Main` once ready.

### 2. Main Speech / Conversation Screen (`Destination.Main`)

* **Purpose**: The central active workspace for real-time speech translation.
* **Responsibilities**:
  - Supports both **Turn-Taking** and **Hands-Free Continuous** modes via mode toggle.
  - Top bar language selector pills (quick swap between `ownerLanguage` and `foreignInputLanguage`/
    `foreignOutputLanguage`).
  - Split display areas: Top box for Foreign Speaker (with optional 180° rotation), Bottom box for
    Owner.
  - Audio visualizer / Mic pulse indicator during active listening.
  - Manual TTS audio replay buttons and text edit fallback (`TextField`).
  - Navigation affordances to `Destination.Settings`.

#### ASCII UI Wireframe:

```text
===============================================================
  SpeakFreely                                              ⚙️
===============================================================

  ┌─────────────────────────────────────────────────────────┐
  │ INPUT · Auto-detecting language into Spanish...        │
  │─────────────────────────────────────────────────────────│
  │                                                         │
  │  "That looks very good!"                                │
  │                                                         │
  │  (Tap text to edit)                 [ ⌨️ ] [ 🎙️ Mute Mic ]│
  └─────────────────────────────────────────────────────────┘

            [ ⇄ Set TRANSLATION to English (US) ]

  ┌─────────────────────────────────────────────────────────┐
  │ TRANSLATION · Spanish (MX)                              │
  │─────────────────────────────────────────────────────────│
  │                                                         │
  │  "¡Eso se ve muy bien!"                                 │
  │                                                         │
  │  (Tap text to edit)                     [ 📋 ]  [ ▶ Play ]│
  └─────────────────────────────────────────────────────────┘

===============================================================
  [ 🔄 Flip 180° ]    Mode: [ Tap to Record | [* Continuous *] ]
===============================================================
```

### 3. Settings Screen (`Destination.Settings`)

* **Purpose**: Comprehensive configuration hub for audio, engine, and UI preferences.
* **Responsibilities**:
  - Primary & Secondary language & BCP-47 regional dialect selectors.
  - VAD silence pause threshold slider (0.8s – 1.8s).
  - TTS speech rate slider & voice gender picker.
  - Offline Model Download Manager (view/download/delete ML Kit language packs).
  - Cloud vs Offline engine priority toggle.
  - 180° Screen Flip and Font Size accessibility toggles.

### Additional Recommended Modal Sheets / Sub-Destinations:

* **Quick Language Picker Sheet**: Bottom sheet launched from `Main` top bar to swap active
  languages without leaving the conversation screen.
* **Offline Model Download Manager Sheet**: Launched from `Settings` to view storage usage and
  manage offline language models (~30 MB each).

---

## Ideas Under Discussion

### 1. Multi-Language Content Model (`LocalizedText`)

* **Category**: Domain & Data Layer
* **Concept**: Representing user speech transcripts and translations alongside their BCP-47 language
  codes (e.g., `"en-US"`, `"es-ES"`) rather than bare strings, ensuring Text-to-Speech (TTS) and
  translation services have exact language metadata.

### 2. Dependency Injection & Networking Evaluation

* **Category**: Architecture & Dependencies
* **Concept**: Evaluating **Koin** (runtime DSL, no KSP annotation processing) and **Ktor Client**
  (lightweight, KMP-ready) alongside Hilt and Retrofit.

### 3. Recent Languages Stack & History Popup Menu

* **Category**: UI & State Management
* **Intended Use**: Primary intended use is for **TRANSLATION language "go back" / Undo**. If an
  accidental target reversal or selection occurs, opening the stack allows immediate undoing back to
  the previous translation language.
* **Secondary Capabilities**: Maintaining a `recentLanguagesStack: List<Language>` in ViewModel UI
  state (capping the last 5 auto-detected/selected foreign languages). Tapping a dropdown arrow `▼`
  or long-pressing the reversal button opens a quick popup menu allowing 1-tap target switching
  between multiple recent foreign speakers (e.g. Spanish, German, French) during multi-person group
  travel or market interactions.

---

## Revision Log

* **2026-10-14**: Added Recent Languages Stack & History Popup Menu under Ideas Under Discussion.
* **2026-10-14**: Updated Decision 7 & ASCII Wireframe (Zero top bar, INPUT Always Auto-Detect,
  TRANSLATION card, Dynamic `Set TRANSLATION` button).
* **2026-10-14**: Added Main Speech Screen ASCII UI Wireframe under Screen Architecture.
* **2026-10-14**: Added Approved Decision 7 (3-State Dynamic Card Header & Localized Card Body
  Specification).
* **2026-10-14**: Added Approved Decision 6 (Detected Language Indicator & Manual Override).
* **2026-10-14**: Added Approved Decision 5 (Production Technology Stack: Native SpeechRecognizer,
  ML Kit Translate, Native TextToSpeech).
* **2026-10-14**: Added Use Case 1: One-Way Foreign Listening (Listen & Read) as primary use case.
* **2026-10-14**: Added Approved Decision 4 (4-Block Global Engine Pipeline & Turn Reversal Flow).
* **2026-10-14**: Added Standardized Language Terminology & Asymmetric Pipeline Model (Owner,
  Foreign Input, Foreign Output).
* **2026-10-14**: Added Device Scope & Orientation Strategy (Phone Portrait focus).
* **2026-10-14**: Added Screen Architecture & Navigation Destinations (Splash, Main, Settings, Modal
  Sheets).
* **2026-10-14**: Added Configuration & User Settings Specification.
* **2026-10-14**: Added Use Case 5 (Text Input Fallback & Transcript Correction).
* **2026-10-14**: Added Use Case 3 (Offline Mode for Travel/Low Connectivity).
* **2026-10-14**: Added Use Case 2 (Hands-Free Continuous Conversation Mode).
* **2026-10-14**: Added Use Case 1 (Face-to-Face Shared Phone Conversation).
* **2026-10-14**: Initial creation with approved `UiText` pattern.
