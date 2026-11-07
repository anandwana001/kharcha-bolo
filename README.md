# Kharcha Bolo

An Android Diwali expense tracker with bundled offline Hindi voice input, optional on-device Gemini Nano inference, Room storage, category totals, and a budget dashboard. Built with Kotlin and Jetpack Compose.

## How it works

1. Tap the mic and grant microphone permission, type an expense using the keyboard button, or select a suggested phrase.
2. Voice input uses a bundled Vosk Hindi speech model (`vosk-model-small-hi-0.22`). It works inside the app on emulators and phones without a system Hindi pack. Audio stays on-device; the app never switches to online recognition. The first mic tap extracts and loads the bundled model.
3. **Basic offline voice** is the default: Hindi transcripts and spoken rupee amounts are normalized, then local rules extract expenses or queries. It is explicitly labeled and does not claim Nano inference. Select a Nano engine for real model processing on supported hardware; those modes never silently substitute basic rules.
4. Saved expenses live in the local Room database `kharcha_bolo.db`. Database flows update the expense list, total, and category breakdown.
5. Responses appear as text with sound feedback. There is no text-to-speech response.

Try `500 ka mithai, 1200 ki lights`, `250 ke diye aur 800 ka kurta`, `kitna kharcha hua?`, or `mithai pe kitna kharcha hua?`.

An empty database receives four sample expenses totaling ₹2,750 when the ViewModel starts, labeled as sample data. Clearing the database and restarting can restore those samples. Budget starts at ₹25,000; budget edits remain in memory and reset after process recreation. Expenses persist. Delete supports undo for the most recently deleted item.

## AI engines and requirements

| Engine | Implementation | Requirements / status |
| --- | --- | --- |
| Basic offline voice (default) | Bundled Hindi speech, spoken-number normalization, local expense rules, and Room writes. | Works on emulators and phones without Nano. No internet or system speech-pack setup required. |
| AICore agentic | ML Kit `AgentRunner` with a preview/FULL Nano model and registered expense AppFunctions. Collects text and tool events through completion. Only tools write expenses; there is no second parser write. | Supported Nano hardware and AICore Developer Preview access. This app's current AppFunctions service requires Android 16+. Full inference still needs verification on eligible hardware. |
| Offline Prompt API | ML Kit `Generation` client, model availability/download checks, structured-output capability check, and typed expense extraction. Validates every item before a batch write; total queries do not write. | Supported Gemini Nano hardware with AICore and structured-output capability. A generic emulator or any Android phone is not sufficient. |
| Agora | Disabled in the selector; direct execution returns an explicit not-connected error. | Planned for later. No Agora session or cloud inference is implemented. |
| Firebase AI | Inherited dependency; no generation calls. | Unused by these workflows. |

Both Nano paths check feature status before inference. Downloadable models are downloaded with progress; initial setup needs internet. Inference runs locally after the model is available. Downloading/unsupported models produce actionable messages. Requests are serialized, processing state resets on failures, and cancellation is propagated. Agent failures can happen after a successful tool write: check the expense list before retrying. The app does not automatically retry agent writes.

Prompt output uses generated `ExpenseIntent` and `ExpenseIntentItem` schemas. Unknown input, empty purchase lists, non-finite/zero/negative amounts, and inconsistent query output are rejected. The model interprets intent and amount; the local category keyword helper assigns display categories. Hindi/Hinglish accuracy still depends on the installed Nano model and requires device evaluation.

The agent's two tools are `log_diwali_expense` and `calculate_total_expenses`. They are generated from `KharchaAppFunctions`, registered through `KharchaExpenseService`, and restricted to callers from this app. No constructor injection is needed; the service resolves its Room DAO from the application context.

Official references: [Agent API and preview requirements](https://developer.android.com/ai/gemini-nano/mlkit-agent-api?hl=en), [Prompt API](https://developers.google.com/ml-kit/genai/prompt/android), [structured output](https://developers.google.com/ml-kit/genai/prompt/android/structured-output), [Android speech recognition](https://developer.android.com/reference/android/speech/SpeechRecognizer).

### Device verification

On November 7, 2026, the updated app built and was installed/launched on the connected DN2101 running Android 13 (API 33). The app screen showed successful speech transcription, then correctly reported that the Nano Prompt API model was unavailable. This phone cannot run the app's Android 16+ agent tools. Installing the app or updating its code cannot supply unsupported hardware or preview entitlement.

**Real Nano inference and agent tool execution have not been verified on eligible hardware.** The integrations compile and their generated schemas/services are present, but that is not evidence of a successful model turn.

## Offline voice on an emulator

The APK bundles Vosk's small Hindi model and native speech library. This adds about 42 MB of compressed model data plus native libraries. On first mic use, it is extracted into app-private storage and reused. All audio capture and speech decoding remain local. Model and library are Apache 2.0 licensed; license and model information are under `app/src/main/assets/speech-hi/`.

Select **Basic offline voice** (default), tap the mic, and say **“पांच सौ की मिठाई और बारह सौ की लाइट्स”**. Pause to finish or tap Stop. The expected total added is ₹1,700. Ask **“मिठाई पे कितना खर्च हुआ”** for a category total. Recognition quality depends on pronunciation and noise; the small Hindi model is not a general multilingual Hinglish recognizer.

Enable the emulator's host microphone input in its Extended Controls → Microphone and allow microphone access for the emulator app in macOS privacy settings. Grant the Android app microphone permission too. Without host audio forwarding, recognition receives silence.

The recognizer stops after one completed utterance or 30 seconds. Stop finishes decoding; backgrounding the app cancels capture. Old callbacks are ignored after cancellation, and a dedicated worker owns model loading and decoding.

## Microphone troubleshooting

- The former system speech-pack error 13 no longer blocks voice input: this version bundles its own Hindi model.
- If no speech is recognized, check host microphone forwarding, operating-system mic access, Android microphone permission, and the input language.
- If the microphone is busy, close other recording apps and tap again.
- Basic voice mode uses limited local rules. It supports Hindi number words, Devanagari digits, conjunctions, and known expense categories; review the recognized text and saved amount.
- Nano engine availability is separate from speech. Switching to Nano on unsupported hardware still returns a clear availability error.

## Run locally

Requirements:

- Android Studio and Android SDK 37 for compilation; minimum install API 26.
- Compatible Java runtime and Gradle installation. The wrapper properties specify Gradle 9.3.1, but this export omits wrapper scripts and JAR.
- A connected Android device or emulator. Nano model tests require supported physical hardware. Bundled Hindi voice mode does not require a system speech service.

Open the project in Android Studio, sync, select the `app` configuration, and run. Or use installed Gradle and Google's Android CLI:

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
gradle :app:assembleDebug
android run --apks=app/build/outputs/apk/debug/app-debug.apk
```

Debug signing uses root `debug.keystore` when supplied, otherwise Android's standard local debug signing configuration. Release signing expects `KEYSTORE_PATH` (default `my-upload-key.jks`), `STORE_PASSWORD`, `KEY_PASSWORD`, and alias `upload`. Updates to an installed app need a compatible signing key.

No API key or Firebase configuration is needed for basic voice or on-device Nano. `.env.example` is inherited scaffolding; adding a key does not activate a cloud engine. The missing `google-services.json` currently produces a permitted build warning. Configure Firebase separately if adding Firebase functionality later.

## Tests

```sh
gradle :app:testDebugUnitTest :app:lintDebug
```

All eleven unit tests passed after the voice update; Android lint completed with zero errors and existing warnings. Tests cover parser examples, valid model-intent persistence, query-only reads, invalid batch rejection without partial writes, and Agora returning an error without database writes. The emulator instrumentation test decodes a locally generated Hindi speech sample through the bundled native model and verifies two Room expenses totaling ₹1,700. A separate test checks microphone start, cancel, and retry. Both instrumentation tests passed on the Android 16.1 emulator with Wi-Fi and mobile data disabled; networking was restored afterwards. These tests do not establish Nano inference or general speech accuracy.

## Remaining demo limitations

The airplane-mode toggle simulates UI state and does not change the system setting. Network byte counters, zero-network verification, and storage-size metrics remain placeholders, not measurements. The camera HUD is a visual demo, not receipt capture or proof of offline inference. Category filtering uses substring matching and may miss aliases. Budget persistence and broader language/device evaluation are still needed.

## Source map

All paths below are under `app/src/main/java/com/example/`:

| File | Purpose |
| --- | --- |
| `ui/KharchaBoloScreen.kt` / `KharchaBoloViewModel.kt` | Dashboard, inputs, state, expense actions |
| `ai/KharchaAgentManager.kt` | Real model routing, availability, inference, result handling |
| `ai/SpokenExpenseNormalizer.kt` | Hindi words/digits to basic expense grammar |
| `ai/ExpenseIntent.kt` | Typed Nano output schemas |
| `ai/HinglishExpenseParser.kt` | Category helper and legacy parser, not a model fallback |
| `agent/KharchaAppFunctions.kt` | Self-access expense tools and generated service entry point |
| `audio/VoiceRecognitionManager.kt` | Bundled Vosk model, audio capture, offline decoding lifecycle |
| `data/` | Room entities, DAO, database |
| `telemetry/NetworkMonitor.kt` | Airplane state and placeholder demo metrics |

Original [AI Studio project](https://ai.studio/apps/21484df9-e3d1-4132-896e-37d049bf58a5).
