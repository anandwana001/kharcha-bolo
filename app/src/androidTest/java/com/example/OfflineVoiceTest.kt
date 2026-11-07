package com.example

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.ai.*
import com.example.audio.VoiceRecognitionManager
import com.example.data.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.vosk.Model
import org.vosk.Recognizer
import org.json.JSONObject

@RunWith(AndroidJUnit4::class)
class OfflineVoiceTest {
    @Test fun microphoneStartsAndCanBeRetried() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.uiAutomation.grantRuntimePermission(context.packageName, android.Manifest.permission.RECORD_AUDIO)
        instrumentation.startActivitySync(android.content.Intent(context, MainActivity::class.java).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
        val voice = VoiceRecognitionManager(context)
        try {
            repeat(2) {
                instrumentation.runOnMainSync { voice.startListening({}, {}) }
                val deadline = android.os.SystemClock.elapsedRealtime() + 20000
                while (!voice.status.value.startsWith("Listening offline") && !voice.status.value.contains("failed") && android.os.SystemClock.elapsedRealtime() < deadline) {
                    Thread.sleep(100)
                }
                assertTrue("Microphone did not start: ${voice.status.value}", voice.status.value.startsWith("Listening offline"))
                instrumentation.runOnMainSync { voice.cancel() }
                assertFalse(voice.isListening.value)
            }
        } finally { instrumentation.runOnMainSync { voice.close() } }
    }

    @Test fun bundledHindiSpeechDecodesAndLogsOffline() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val voice = VoiceRecognitionManager(context)
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val manager = KharchaAgentManager(context, db.expenseDao())
        try {
            // PCM: synthetic Hindi speech generated locally, 16kHz mono signed 16-bit.
            val pcm = instrumentation.context.assets.open("hindi-expenses.pcm").use { it.readBytes() }
            val recognized = StringBuilder()
            Model(voice.installModel().absolutePath).use { model ->
                Recognizer(model, 16000f).use { recognizer ->
                    var offset = 0
                    while (offset < pcm.size) {
                        val end = minOf(offset + 4096, pcm.size)
                        val chunk = pcm.copyOfRange(offset, end)
                        if (recognizer.acceptWaveForm(chunk, chunk.size)) {
                            recognized.append(JSONObject(recognizer.result).optString("text")).append(' ')
                        }
                        offset = end
                    }
                    recognized.append(JSONObject(recognizer.finalResult).optString("text"))
                }
            }
            val transcript = recognized.toString().trim()
            android.util.Log.i("OfflineVoiceTest", "Hindi transcript: $transcript")
            assertTrue("Expected Hindi speech, got: $transcript", transcript.contains("मिठाई"))
            val result = manager.execute(transcript, AiEngineType.LOCAL_OFFLINE)
            assertTrue("Expected logged expenses, got: $result; transcript: $transcript", result is AgentExecutionResult.LoggedExpenses)
            assertEquals(1700.0, db.expenseDao().getTotalExpense(), 0.0)
            assertEquals(2, db.expenseDao().getAllExpenses().size)
        } finally {
            manager.close(); db.close()
            instrumentation.runOnMainSync { voice.close() }
        }
    }
}
