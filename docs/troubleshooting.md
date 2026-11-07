# Agora troubleshooting

Verify `/health` returns JSON. Check that `PUBLIC_BASE_URL` and `QUICKSTART_SERVER_URL` point to the running backend's HTTPS tunnel. A changed tunnel URL requires a backend restart and Android rebuild. Enable Conversational AI and RTM for the Agora project. Microphone permission and internet are required. Check local server logs for provider, token, or project errors.

The backend now uses multilingual Hindi/English input and Murf Namrita for Hindi output. Use numeric expense phrases such as “500 for sweets” and review recognized amounts. After a backend restart, stop and restart the Agora session to receive new voice settings.
