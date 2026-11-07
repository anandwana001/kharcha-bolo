package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.AirplaneModeHud
import com.example.ui.components.CameraDemoOverlay
import com.example.ui.components.CategoryBreakdownRow
import com.example.ui.components.DiyaBudgetCard
import com.example.ui.components.EditBudgetDialog
import com.example.ui.components.EngineSelectorSheet
import com.example.ui.components.ExpenseItemCard
import com.example.ui.components.ManualTextPromptDialog
import com.example.ui.components.QuickHinglishChips
import com.example.ui.components.VoiceWaveformBar
import com.example.ui.theme.DiwaliAmber
import com.example.ui.theme.DiwaliBorderGold
import com.example.ui.theme.DiwaliDeepDark
import com.example.ui.theme.DiwaliGold
import com.example.ui.theme.DiwaliSaffron
import com.example.ui.theme.DiwaliSurfaceDark
import com.example.ui.theme.DiwaliSurfaceElevated
import com.example.ui.theme.DiwaliVermilion
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KharchaBoloScreen(
    viewModel: KharchaBoloViewModel,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) viewModel.cancelVoiceInput()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer); viewModel.cancelVoiceInput() }
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val expenses by viewModel.expenses.collectAsState()
    val totalSpent by viewModel.totalExpense.collectAsState()
    val budget by viewModel.budget.collectAsState()
    val categorySummaries by viewModel.categorySummaries.collectAsState()
    val telemetry by viewModel.telemetryState.collectAsState()
    val selectedEngine by viewModel.selectedEngine.collectAsState()
    val isAiCoreReady by viewModel.isAiCoreAvailable.collectAsState()

    val isListening by viewModel.isListening.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val transcription by viewModel.transcription.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val lastAgentResponse by viewModel.lastAgentResponse.collectAsState()
    val lastLatencyMs by viewModel.lastLatencyMs.collectAsState()
    val lastEngineUsed by viewModel.lastEngineUsed.collectAsState()
    val engineStatus by viewModel.engineStatus.collectAsState()
    val speechStatus by viewModel.speechStatus.collectAsState()
    val cameraDemoMode by viewModel.cameraDemoMode.collectAsState()

    var showEngineSheet by remember { mutableStateOf(false) }
    var showEditBudgetDialog by remember { mutableStateOf(false) }
    var showManualInputDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    // Audio Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceInput()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Microphone permission zaroori hai voice logging ke liye.")
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("kharcha_bolo_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🪔",
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Kharcha Bolo",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = DiwaliGold
                            )
                            Text(
                                text = "Diwali Offline Expense Logger",
                                fontSize = 11.sp,
                                color = Color(0xFFFFD180)
                            )
                        }
                    }
                },
                actions = {
                    // Engine Switcher Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(1.dp, DiwaliGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { showEngineSheet = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("engine_badge_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Engine Selector",
                                tint = DiwaliGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (selectedEngine) {
                                    com.example.ai.AiEngineType.LOCAL_OFFLINE -> "Offline Voice"
                                    com.example.ai.AiEngineType.GEMINI_NANO_4_AGENT -> "Nano 4"
                                    com.example.ai.AiEngineType.GEMINI_NANO_PROMPT_API -> "Prompt API"
                                    com.example.ai.AiEngineType.AGORA_CONVERSATIONAL_CLOUD -> "Agora Voice"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DiwaliGold
                            )
                        }
                    }

                    // Clear All action
                    IconButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier.testTag("clear_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear All",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DiwaliDeepDark,
                    titleContentColor = DiwaliGold
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DiwaliDeepDark,
        bottomBar = {
            VoiceWaveformBar(
                isListening = isListening,
                audioLevel = audioLevel,
                transcription = transcription,
                isProcessing = isProcessing,
                statusMessage = if (isProcessing) engineStatus else speechStatus,
                onStartListening = {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPerm) {
                        viewModel.startVoiceInput()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStopListening = { viewModel.stopVoiceInput() },
                onOpenKeyboardInput = { showManualInputDialog = true },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // 1. Airplane Mode Telemetry HUD
            item {
                AirplaneModeHud(
                    telemetry = telemetry,
                    isCameraDemoMode = cameraDemoMode,
                    onToggleAirplane = { viewModel.toggleAirplaneModeSim() },
                    onToggleCameraDemo = { viewModel.toggleCameraDemoMode() }
                )
            }

            // 2. Camera Demo Proof Overlay (when toggled on)
            item {
                CameraDemoOverlay(
                    isVisible = cameraDemoMode,
                    latencyMs = lastLatencyMs,
                    engineName = lastEngineUsed
                )
            }

            // 3. Diya Budget & Total Card
            item {
                DiyaBudgetCard(
                    totalSpent = totalSpent,
                    budget = budget,
                    engineLabel = lastEngineUsed,
                    onEditBudget = { showEditBudgetDialog = true }
                )
            }

            // 4. Quick Hinglish Tap Chips for Demo
            item {
                QuickHinglishChips(
                    onSelectPhrase = { phrase ->
                        viewModel.submitHinglishPrompt(phrase)
                    }
                )
            }

            // 5. Category Breakdown Row
            item {
                CategoryBreakdownRow(summaries = categorySummaries)
            }

            // 6. Live Agent Response Feedback Card
            if (lastAgentResponse != null) {
                item {
                    AgentFeedbackBanner(
                        responseMessage = lastAgentResponse!!,
                        latencyMs = lastLatencyMs,
                        engineUsed = lastEngineUsed,
                        onDismiss = { /* keep visible */ }
                    )
                }
            }

            // 7. Recent Expenses Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📜 LOGGED EXPENSES (${expenses.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DiwaliGold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Room SQLite",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            // 8. Expense Items
            if (expenses.isEmpty()) {
                item {
                    EmptyExpenseState(onSeedSample = {
                        viewModel.submitHinglishPrompt("500 ka mithai, 1200 ki lights")
                    })
                }
            } else {
                items(expenses, key = { it.id }) { item ->
                    ExpenseItemCard(
                        expense = item,
                        onDelete = {
                            viewModel.deleteExpense(item)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "${item.itemName} deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.undoDelete()
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // Dialogs & Sheets
    if (showEngineSheet) {
        EngineSelectorSheet(
            selectedEngine = selectedEngine,
            isNano4Available = isAiCoreReady,
            onSelectEngine = { viewModel.toggleEngine(it) },
            onDismiss = { showEngineSheet = false }
        )
    }

    if (showEditBudgetDialog) {
        EditBudgetDialog(
            currentBudget = budget,
            onSaveBudget = { viewModel.setBudgetGoal(it) },
            onDismiss = { showEditBudgetDialog = false }
        )
    }

    if (showManualInputDialog) {
        ManualTextPromptDialog(
            onSubmit = { viewModel.submitHinglishPrompt(it) },
            onDismiss = { showManualInputDialog = false }
        )
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = DiwaliSurfaceDark,
            title = {
                Text(
                    text = "Clear All Expenses?",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Kya aap saare logged Diwali expenses delete karna chahte hain?",
                    color = Color(0xFFFFD180),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllExpenses()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Clear", color = DiwaliVermilion, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }
}

@Composable
private fun AgentFeedbackBanner(
    responseMessage: String,
    latencyMs: Long,
    engineUsed: String,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, DiwaliGold, RoundedCornerShape(16.dp)),
        color = DiwaliSurfaceElevated,
        tonalElevation = 5.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✨", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AGENT RESPONSE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = DiwaliGold,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = "${latencyMs}ms • $engineUsed",
                    fontSize = 9.sp,
                    color = Color(0xFFFFD180),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = responseMessage,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFF8E7),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun EmptyExpenseState(onSeedSample: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DiwaliSurfaceDark)
            .border(1.dp, DiwaliBorderGold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🪔", fontSize = 42.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Abhi koi kharcha log nahi hua",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFF8E7)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Mic button dabakar bolein:\n'500 ka mithai, 1200 ki lights'",
                fontSize = 12.sp,
                color = Color(0xFFFFD180),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DiwaliGold)
                    .clickable { onSeedSample() }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Demo Item Log Karein",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
