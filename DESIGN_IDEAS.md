# SpeakFreely - Design & Architecture Ideas

A living document to capture approved design decisions, architectural patterns, and feature ideas
for **SpeakFreely**.

---

## Approved Design Decisions

### 1. `UiText` Pattern for Localized UI State & Messages

* **Status**: Approved
* **Category**: Presentation & ViewModel State
* **Problem**: Passing raw `String` resolved via `Context.getString()` in ViewModels locks strings
  to the current locale and breaks pure ViewModel unit testing without Android mocks.
* **Decision**: Use a `UiText` sealed interface (`DynamicString` vs `StringResource`) for all UI
  labels, error messages, and validation state passed from ViewModels.
* **Benefits**:
    - ViewModels remain 100% Android-decoupled and easy to unit test.
    - UI automatically updates when the system locale or language changes at runtime.

### 2. Device Scope & Orientation Strategy

* **Status**: Approved
* **Category**: Presentation / Responsive Layout
* **Primary Target**: Optimized specifically for **Phone in Portrait Mode**.
* **Secondary Target**: Responsive baseline for tablets/foldables in portrait mode (using standard
  Compose flexible layouts like `Modifier.weight()`, `fillMaxSize()`, and `widthIn(max = 600.dp)` to
  prevent text stretching).
* **Scope Restriction**: No dedicated landscape or multi-pane / side-by-side tablet layout
  optimizations are required.

### 3. Standardized Language Terminology & Asymmetric Pipeline Model

* **Status**: Approved
* **Category**: Domain & Architecture Naming
* **Standard Terminology**:
  - `ownerLanguage`: The primary language of the phone owner (e.g. `en-US`).
  - `foreignInputLanguage`: The incoming language spoken into the microphone (e.g. `ja-JP` or
    `es-MX`).
  - `foreignOutputLanguage`: The outgoing target language translated to and spoken via TTS (e.g.
    `es-ES` or `de-DE`).
* **Architectural Impact**:
  - **Standard 2-Way Default**: `foreignInputLanguage` and `foreignOutputLanguage` are set to the
    same language (e.g. `es-MX`).
  - **Asymmetric / 3-Way Generalization**: Enables multi-language facilitation where
    `foreignInputLanguage` differs from `foreignOutputLanguage`, with `ownerLanguage` serving as the
    readable intermediary on the owner's screen.

---

## Use Cases & User Flows

### Use Case 1: Face-to-Face Shared Phone Conversation (Turn-Taking)

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
     hears the
     response (and displays foreign text on screen).
* **Key Technical Requirements**:
    - Speech-to-Text (STT) for both languages.
    - Text Translation engine.
    - Text-to-Speech (TTS) playback engine.
    - Turn-taking state machine (Idle → Listening → Translating → Playing).
  - Manual TTS replay trigger (in case Foreign Speaker needs to hear it again).

### Use Case 2: Hands-Free Continuous Conversation Mode

* **Goal**: Allow two speakers to converse continuously without manually tapping record/stop buttons
  between phrases.
* **Actors**: Owner (English) and Foreign Speaker placing the phone between them.
* **Turn Sequence**:
  1. **Session Start**: Users select languages (`ownerLanguage` <-> `foreignInputLanguage` /
     `foreignOutputLanguage`) and tap "Start Continuous Mode".
    2. **Continuous Audio Monitoring**: Microphone streams continuously with Voice Activity
       Detection (VAD).
  3. **Automatic Speech Segment Detection**: When Foreign Speaker finishes speaking (detected via ~
     1.0s
     silence pause), app transcribes, translates, and displays/speaks in Owner Language.
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

### Use Case 3: Offline Mode (Travel & Low/No Connectivity)

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
       taps
       "Translate & Speak".
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

### 1. Text-Mediated Translation Pipeline Architecture

* **Category**: Data & Domain Pipeline
* **Concept**: Cascading `UserVoice (Audio Buffer)` $\rightarrow$ `Speech-to-Text` $\rightarrow$
  `SourceText (Text-Lang1)` $\rightarrow$ `Text Translation` $\rightarrow$
  `TargetText (Text-Lang2)` $\rightarrow$ `TTS Audio (Target Lang)`. Allows one-to-many language
  retargeting, text editing before translation, and immediate discarding of PCM audio buffers from
  memory.

### 2. Multi-Language Content Model (`LocalizedText`)

* **Category**: Domain & Data Layer
* **Concept**: Representing user speech transcripts and translations alongside their BCP-47 language
  codes (e.g., `"en-US"`, `"es-ES"`) rather than bare strings, ensuring Text-to-Speech (TTS) and
  translation services have exact language metadata.

### 3. Dependency Injection & Networking Evaluation

* **Category**: Architecture & Dependencies
* **Concept**: Evaluating **Koin** (runtime DSL, no KSP annotation processing) and **Ktor Client**
  (lightweight, KMP-ready) alongside Hilt and Retrofit.

---

## Revision Log

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
