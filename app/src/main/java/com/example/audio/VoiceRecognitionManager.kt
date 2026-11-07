package com.example.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.util.concurrent.Executors
import kotlin.math.log10
import kotlin.math.sqrt

/** Bundled Hindi recognizer. Audio and decoding remain inside the app. */
class VoiceRecognitionManager(private val context: Context) {
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private var model: Model? = null // Accessed only on worker.
    @Volatile private var generation = 0
    @Volatile private var recording = false
    @Volatile private var closed = false
    private var pending = false // Main thread only.
    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()
    private val _audioRms = MutableStateFlow(0f)
    val audioRms = _audioRms.asStateFlow()
    private val _transcription = MutableStateFlow("")
    val transcription = _transcription.asStateFlow()
    private val _status = MutableStateFlow("")
    val status = _status.asStateFlow()

    fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        if (pending || closed) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            _status.value = "Allow microphone access to use voice input."
            onError(_status.value)
            return
        }
        val token = ++generation
        pending = true
        recording = true
        _isListening.value = true
        _transcription.value = ""
        _status.value = "Preparing offline Hindi speech…"
        fun update(block: () -> Unit) { main.post { if (token == generation && !closed) block() } }
        worker.execute {
            var recorder: AudioRecord? = null
            var decoder: Recognizer? = null
            try {
                val activeModel = model ?: Model(installModel().absolutePath).also { model = it }
                if (token != generation || !recording) return@execute
                val sampleRate = 16000
                val minimum = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
                check(minimum > 0) { "Microphone does not support 16 kHz audio." }
                recorder = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, sampleRate,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(minimum, 8192))
                check(recorder.state == AudioRecord.STATE_INITIALIZED) { "Could not initialize microphone." }
                decoder = Recognizer(activeModel, sampleRate.toFloat())
                recorder.startRecording()
                check(recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) { "Microphone is busy or disabled." }
                update { _status.value = "Listening offline in Hindi. Speak, then pause or tap Stop." }
                val buffer = ShortArray(2048)
                val deadline = SystemClock.elapsedRealtime() + 30_000
                var finalText = ""
                while (recording && token == generation && SystemClock.elapsedRealtime() < deadline) {
                    val count = recorder.read(buffer, 0, buffer.size)
                    check(count >= 0) { "Microphone read failed ($count)." }
                    if (count == 0) continue
                    val rms = sqrt((0 until count).sumOf { buffer[it].toDouble() * buffer[it] } / count)
                    val level = ((20 * log10(maxOf(rms, 1.0)) - 25) / 55).toFloat().coerceIn(0f, 1f)
                    val endpoint = decoder.acceptWaveForm(buffer, count)
                    val json = JSONObject(if (endpoint) decoder.result else decoder.partialResult)
                    val text = json.optString(if (endpoint) "text" else "partial").trim()
                    update { _audioRms.value = level; if (text.isNotEmpty()) _transcription.value = text }
                    if (endpoint && text.isNotEmpty()) { finalText = text; break }
                }
                if (finalText.isBlank()) finalText = JSONObject(decoder.finalResult).optString("text").trim()
                val result = finalText
                update {
                    pending = false
                    recording = false
                    _isListening.value = false
                    _audioRms.value = 0f
                    if (result.isBlank()) {
                        _status.value = "No speech recognized. Check emulator microphone access, then try again."
                        onError(_status.value)
                    } else {
                        _transcription.value = result
                        _status.value = "Recognized offline: $result"
                        onResult(result)
                    }
                }
            } catch (e: Exception) {
                update {
                    pending = false; recording = false; _isListening.value = false; _audioRms.value = 0f
                    _status.value = "Offline microphone failed: ${e.localizedMessage}"
                    onError(_status.value)
                }
            } catch (e: LinkageError) {
                update {
                    pending = false; recording = false; _isListening.value = false
                    _status.value = "Offline speech library could not load on this device: ${e.localizedMessage}"
                    onError(_status.value)
                }
            } finally {
                try { recorder?.stop() } catch (_: Exception) {}
                recorder?.release()
                decoder?.close()
                update {
                    if (pending && !recording) { pending = false; _isListening.value = false; _status.value = "Stopped. Tap the mic to try again." }
                }
            }
        }
    }

    // Extract once into app-private storage; completion marker is written last.
    internal fun installModel(): File {
        val root = File(context.filesDir, "speech-hi-v1")
        val ready = File(root, ".ready")
        if (!ready.exists()) {
            fun copyAsset(path: String, destination: File) {
                val children = context.assets.list(path).orEmpty()
                if (children.isEmpty()) {
                    destination.parentFile?.mkdirs()
                    context.assets.open(path).use { input -> destination.outputStream().use { input.copyTo(it) } }
                } else {
                    destination.mkdirs()
                    children.forEach { copyAsset("$path/$it", File(destination, it)) }
                }
            }
            copyAsset("speech-hi", root)
            ready.writeText("vosk-model-small-hi-0.22")
        }
        return root
    }

    fun stopListening() {
        recording = false // Worker finishes decoding and delivers final text.
        _status.value = "Finishing offline transcription…"
    }

    fun cancel() {
        generation++
        recording = false
        pending = false
        _isListening.value = false
        _audioRms.value = 0f
    }

    fun close() {
        if (closed) return
        cancel()
        closed = true
        worker.execute { model?.close(); model = null }
        worker.shutdown()
    }
}
