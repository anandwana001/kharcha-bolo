# Agora setup

Run the Python server with `./server/run.sh` after installing `server/requirements-dev.txt` in a virtual environment. Keep Agora credentials in ignored `server/.env.local`. Expose port 8000 with `./server/tunnel.sh --provider ngrok`, set `PUBLIC_BASE_URL` in server configuration, then set `QUICKSTART_SERVER_URL` in root `local.properties` to the same HTTPS URL and rebuild Android. Never put the Agora App Certificate in Android configuration.

Choose Agora in the engine selector and tap the microphone. Audio uses the cloud; local expense rules and Room provide authoritative saves and totals. Stop the session with the microphone control. Switching engine or backgrounding the app ends local audio and requests agent cleanup.

## Hindi voice

The current local setup uses Murf Falcon 2.1 (`falcon-2.1`), Namrita (`hi-IN-namrita`), locale `hi-IN`, and `Conversational` style. Configure `TTS_VENDOR=murf` and `MURF_API_KEY` only in `server/.env.local`. `ASR_LANGUAGE=multi` enables Hindi/English speech recognition. Restart the backend and reconnect the agent after changing these values; no Android rebuild is needed for server-only settings.

Empty speech transcripts receive a normal Hindi request to repeat, never a replay of an older expense.
