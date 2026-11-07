package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AgentExecutionResult
import com.example.ai.AiEngineType
import com.example.ai.KharchaAgentManager
import com.example.audio.SoundFeedbackHelper
import com.example.audio.VoiceRecognitionManager
import com.example.data.AppDatabase
import com.example.data.CategorySummary
import com.example.data.ExpenseEntity
import com.example.telemetry.NetworkMonitor
import com.example.telemetry.TelemetryState
import com.example.agora.AgoraExpenseController
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class KharchaBoloViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val expenseDao = db.expenseDao()

    val agentManager = KharchaAgentManager(application, expenseDao)
    val voiceManager = VoiceRecognitionManager(application)
    val soundHelper = SoundFeedbackHelper(application)
    val networkMonitor = NetworkMonitor(application)

    val expenses: StateFlow<List<ExpenseEntity>> = expenseDao.getAllExpensesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpense: StateFlow<Double> = expenseDao.getTotalExpenseFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val categorySummaries: StateFlow<List<CategorySummary>> = expenseDao.getCategorySummariesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val telemetryState: StateFlow<TelemetryState> = networkMonitor.telemetry



    private val _budget = MutableStateFlow(25000.0)
    val budget: StateFlow<Double> = _budget.asStateFlow()

    private val _selectedEngine = MutableStateFlow(AiEngineType.LOCAL_OFFLINE)
    val selectedEngine: StateFlow<AiEngineType> = _selectedEngine.asStateFlow()

    val isAiCoreAvailable = agentManager.agentReady
    val engineStatus = agentManager.status
    val agora = AgoraExpenseController(application) { question ->
        agentManager.execute(question, AiEngineType.LOCAL_OFFLINE).also { recordAgoraResult(it) }
    }
    val isListening = combine(voiceManager.isListening, agora.active, agora.busy, selectedEngine) { local, cloud, busy, engine ->
        if (engine == AiEngineType.AGORA_CONVERSATIONAL_CLOUD) cloud || busy else local
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val audioLevel = voiceManager.audioRms
    val transcription = combine(voiceManager.transcription, agora.snapshot, selectedEngine) { local, snapshot, engine ->
        if (engine == AiEngineType.AGORA_CONVERSATIONAL_CLOUD) snapshot.transcriptTurns.lastOrNull()?.text.orEmpty() else local
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val speechStatus = combine(voiceManager.status, agora.status, selectedEngine) { local, cloud, engine ->
        if (engine == AiEngineType.AGORA_CONVERSATIONAL_CLOUD) cloud else local
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _lastAgentResponse = MutableStateFlow<String?>(null)
    val lastAgentResponse: StateFlow<String?> = _lastAgentResponse.asStateFlow()

    private val _lastLatencyMs = MutableStateFlow(12L)
    val lastLatencyMs: StateFlow<Long> = _lastLatencyMs.asStateFlow()

    private val _lastEngineUsed = MutableStateFlow("Not run yet")
    val lastEngineUsed: StateFlow<String> = _lastEngineUsed.asStateFlow()

    private val _cameraDemoMode = MutableStateFlow(false)
    val cameraDemoMode: StateFlow<Boolean> = _cameraDemoMode.asStateFlow()

    private var recentlyDeletedExpense: ExpenseEntity? = null

    init {
        // Pre-populate with realistic Diwali items if database is freshly created
        viewModelScope.launch {
            if (expenseDao.getAllExpenses().isEmpty()) {
                val demoItems = listOf(
                    ExpenseEntity(
                        itemName = "Mithai (Kaju Katli)",
                        amount = 500.0,
                        category = "Mithai & Sweets",
                        rawInputPrompt = "500 ka mithai",
                        processedByEngine = "Sample data"
                    ),
                    ExpenseEntity(
                        itemName = "Fairy Lights (Ladi)",
                        amount = 1200.0,
                        category = "Lights & Decor",
                        rawInputPrompt = "1200 ki lights",
                        processedByEngine = "Sample data"
                    ),
                    ExpenseEntity(
                        itemName = "Clay Diye (50 pcs)",
                        amount = 250.0,
                        category = "Diyas & Pooja",
                        rawInputPrompt = "250 ke diye",
                        processedByEngine = "Sample data"
                    ),
                    ExpenseEntity(
                        itemName = "Silk Kurta",
                        amount = 800.0,
                        category = "Clothes & Attire",
                        rawInputPrompt = "800 ka kurta",
                        processedByEngine = "Sample data"
                    )
                )
                expenseDao.insertExpenses(demoItems)
            }
        }
    }

    fun submitHinglishPrompt(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty() || _isProcessing.value) return
        if (_selectedEngine.value == AiEngineType.AGORA_CONVERSATIONAL_CLOUD) { agora.sendText(trimmed); return }

        viewModelScope.launch {
            _isProcessing.value = true
            val result = try {
                agentManager.execute(trimmed, _selectedEngine.value)
            } finally {
                _isProcessing.value = false
            }

            when (result) {
                is AgentExecutionResult.LoggedExpenses -> {
                    _lastAgentResponse.value = result.responseMessage
                    _lastLatencyMs.value = result.latencyMs
                    _lastEngineUsed.value = result.engineUsed
                    networkMonitor.recordInference(result.latencyMs)
                    soundHelper.playSuccessChime()
                }
                is AgentExecutionResult.QueryResponse -> {
                    _lastAgentResponse.value = result.responseMessage
                    _lastLatencyMs.value = result.latencyMs
                    _lastEngineUsed.value = result.engineUsed
                    networkMonitor.recordInference(result.latencyMs)
                    soundHelper.playDiyaChime()
                }
                is AgentExecutionResult.Error -> {
                    _lastAgentResponse.value = result.message
                    _lastLatencyMs.value = result.latencyMs
                    _lastEngineUsed.value = result.engineUsed
                }
            }
        }
    }

    private fun recordAgoraResult(result: AgentExecutionResult) {
        _lastEngineUsed.value = "Agora cloud voice + local rules"
        when (result) {
            is AgentExecutionResult.LoggedExpenses -> { _lastAgentResponse.value = result.responseMessage; _lastLatencyMs.value = result.latencyMs }
            is AgentExecutionResult.QueryResponse -> { _lastAgentResponse.value = result.responseMessage; _lastLatencyMs.value = result.latencyMs }
            is AgentExecutionResult.Error -> _lastAgentResponse.value = result.message
        }
    }

    fun startVoiceInput() {
        if (_isProcessing.value) return
        if (_selectedEngine.value == AiEngineType.AGORA_CONVERSATIONAL_CLOUD) {
            voiceManager.cancel()
            agora.start()
            return
        }
        voiceManager.startListening(
            onResult = { recognizedText ->
                submitHinglishPrompt(recognizedText)
            },
            onError = { errMsg ->
                _lastAgentResponse.value = errMsg
            }
        )
    }

    fun cancelVoiceInput() { voiceManager.cancel(); if (agora.active.value || agora.busy.value) agora.stop() }

    fun stopVoiceInput() {
        if (_selectedEngine.value == AiEngineType.AGORA_CONVERSATIONAL_CLOUD) { agora.stop(); return }
        voiceManager.stopListening()
    }

    fun toggleEngine(engine: AiEngineType) {
        cancelVoiceInput()
        _selectedEngine.value = engine
    }

    fun toggleAirplaneModeSim() {
        networkMonitor.toggleSimulatedAirplaneMode()
    }

    fun toggleCameraDemoMode() {
        _cameraDemoMode.value = !_cameraDemoMode.value
    }

    fun setBudgetGoal(newBudget: Double) {
        if (newBudget > 0) {
            _budget.value = newBudget
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        recentlyDeletedExpense = expense
        viewModelScope.launch {
            expenseDao.deleteExpense(expense)
            soundHelper.playDiyaChime()
        }
    }

    fun undoDelete() {
        recentlyDeletedExpense?.let { expense ->
            viewModelScope.launch {
                expenseDao.insertExpense(expense.copy(id = 0))
                recentlyDeletedExpense = null
            }
        }
    }

    fun clearAllExpenses() {
        viewModelScope.launch {
            expenseDao.clearAll()
        }
    }

    override fun onCleared() {
        super.onCleared()
        agora.close()
        voiceManager.close()
        soundHelper.release()
        networkMonitor.unregister()
        agentManager.close()
    }
}
