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

---

## Use Cases & User Flows

### Use Case 1: Face-to-Face Shared Phone Conversation (Turn-Taking)

* **Goal**: Enable seamless face-to-face conversation between two people speaking different
  languages using a single shared phone.
* **Actors**:
    - **User 1**: Primary phone owner / English speaker.
    - **User 2**: Conversation partner / Spanish speaker.
* **Turn Sequence**:
    1. **User 2 Speaks (Spanish)**: User 2 speaks into the microphone in Spanish.
    2. **Transcription & Translation**: App transcribes Spanish audio → translates text into
       English.
    3. **User 1 Reads (English)**: User 1 reads the English translation on the screen.
    4. **User 1 Responds (English)**: User 1 taps/speaks a response in English.
    5. **Transcription, Translation & TTS**: App transcribes English audio → translates into Spanish
       text → generates Spanish Text-to-Speech (TTS) audio.
    6. **Playback to User 2 (Spanish)**: App plays back the Spanish TTS audio so User 2 hears the
       response (and displays Spanish text on screen).
* **Key Technical Requirements**:
    - Speech-to-Text (STT) for both languages.
    - Text Translation engine.
    - Text-to-Speech (TTS) playback engine.
    - Turn-taking state machine (Idle → Listening → Translating → Playing).
    - Manual TTS replay trigger (in case User 2 needs to hear it again).

### Use Case 2: Hands-Free Continuous Conversation Mode

* **Goal**: Allow two speakers to converse continuously without manually tapping record/stop buttons
  between phrases.
* **Actors**: User 1 (English) and User 2 (Spanish) placing the phone between them.
* **Turn Sequence**:
    1. **Session Start**: Users select languages (English <-> Spanish) and tap "Start Continuous
       Mode".
    2. **Continuous Audio Monitoring**: Microphone streams continuously with Voice Activity
       Detection (VAD).
    3. **Automatic Speech Segment Detection**: When User 2 finishes speaking (detected via ~1.0s
       silence pause), app transcribes, translates, and displays/speaks English.
    4. **Automatic Response Capture**: When User 1 speaks, app detects speech pause, transcribes,
       translates to Spanish, and plays Spanish TTS.
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
* **Actors**: User traveling abroad (e.g. at an airport, remote location, or on airplane mode).
* **Workflow**:
    1. **On-Demand Model Download**: User downloads language packs ahead of time over Wi-Fi (~30 MB
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
* **Actors**: User 1 or User 2 encountering speech recognition errors or needing quiet/text input.
* **Workflow**:
    1. **Scenario A — Transcript Correction**: STT transcribes speech, but mishears a word due to
       accent/dialect or proper noun. User taps the transcript text box, edits the incorrect word,
       and hits "Translate".
    2. **Scenario B — Direct Keyboard Entry**: User is in a noisy/quiet environment or prefers
       typing. User taps the keyboard icon, types their message in English or Spanish, and taps
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

- **User 1 Language & Dialect**: Primary speaker language code + region (e.g. `en-US`, `en-GB`,
  `en-IN`).
- **User 2 Language & Dialect**: Secondary speaker language code + region (e.g. `es-ES`, `es-MX`,
  `es-AR`).

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

---

## Revision Log

* **2026-10-14**: Added Configuration & User Settings Specification.
* **2026-10-14**: Added Use Case 5 (Text Input Fallback & Transcript Correction).
* **2026-10-14**: Added Use Case 3 (Offline Mode for Travel/Low Connectivity).
* **2026-10-14**: Added Use Case 2 (Hands-Free Continuous Conversation Mode).
* **2026-10-14**: Added Use Case 1 (Face-to-Face Shared Phone Conversation).
* **2026-10-14**: Initial creation with approved `UiText` pattern.
