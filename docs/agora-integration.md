# Agora integration source map

App inventory: single native Android/Compose app, Room local data, no existing backend or Agora dependencies. The reference is the user's local checkout of https://github.com/AgoraIO-Conversational-AI/agent-quickstart-android.

| Inspected reference | Destination | Adaptation |
| --- | --- | --- |
| `rtc/AgoraConversationSessionManager.kt`, `rtc/TranscriptAssembler.kt` | `com.example.agora.rtc` | Preserve RTC/RTM joining, renewals, transcript assembly and native microphone lifecycle. |
| `audio/{AudioSessionManager,BargeInDetector,SelfSpeechFilter,TurnManager}.kt` | `com.example.agora.audio` | Preserve reference audio coordination. |
| `data/ConversationAgoraApi.kt`, `ConversationRepository.kt` | `com.example.agora.data` | Preserve bootstrap/join/leave/refresh and authenticated request relay contracts. |
| `model/ConversationModels.kt`, `config/QuickstartConfig.kt` | `com.example.agora` subpackages | Separate namespaces and backend-only credentials. |
| `ui/ConversationViewModel.kt` | `AgoraExpenseController` | Adapt orchestration to Kharcha Bolo. Reuse the authenticated custom-LLM relay to execute expenses against this app's Room database. |
| `server/` | `server/` | Reference backend, token generation and custom-LLM relay; expense greeting and identity replace document persona. |

Agora cloud mode is an explicit opt-in engine. It sends audio to Agora/provider infrastructure and needs internet. Existing offline modes remain separate. Expense writes and totals execute on Android using labeled basic rules; Agora is the speech transport, not a claim of Nano inference. The relay uses the reference's `document_token` wire field for compatibility, although this app handles expenses rather than documents.

Runtime validation must distinguish build/tests, backend health, RTC client connection, agent join, and an actual voice round trip. A passing build does not prove the latter.

## Verified in this branch

Android build and unit tests pass. All 16 backend tests pass. A real emulator test confirmed RTC/RTM client connection, agent RTC join, a cloud text command relayed to Android and saved as a ₹500 expense, and session stop. This does not establish a human microphone-to-spoken-response round trip. The baseline was sourced from the inspected official Android demo; its own RAG UI was not run as part of this task.

For low-storage ARM64 devices use `gradle :app:assembleDebug -PtargetAbi=arm64-v8a`. Omitting the property produces the normal multi-ABI APK.
