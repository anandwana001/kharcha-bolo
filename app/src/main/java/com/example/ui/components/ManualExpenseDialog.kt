package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DiwaliGold
import com.example.ui.theme.DiwaliSaffron
import com.example.ui.theme.DiwaliSurfaceDark

@Composable
fun EditBudgetDialog(
    currentBudget: Double,
    onSaveBudget: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var budgetText by remember { mutableStateOf(currentBudget.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DiwaliSurfaceDark,
        title = {
            Text(
                text = "🪔 Set Diwali Budget",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFF8E7)
            )
        },
        text = {
            Column {
                Text(
                    text = "Apna total Diwali expense budget enter karein:",
                    fontSize = 13.sp,
                    color = Color(0xFFFFD180)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it },
                    label = { Text("Budget in ₹ (INR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_input_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DiwaliGold,
                        focusedLabelColor = DiwaliGold,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = budgetText.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onSaveBudget(amount)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DiwaliGold),
                modifier = Modifier.testTag("save_budget_button")
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}

@Composable
fun ManualTextPromptDialog(
    initialPrompt: String = "",
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var textPrompt by remember { mutableStateOf(initialPrompt) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DiwaliSurfaceDark,
        title = {
            Text(
                text = "✍️ Hinglish Command Type Karein",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFF8E7)
            )
        },
        text = {
            Column {
                Text(
                    text = "Jaise: '500 ka mithai, 1200 ki lights' ya 'kitna kharcha hua?'",
                    fontSize = 12.sp,
                    color = Color(0xFFFFD180)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = textPrompt,
                    onValueChange = { textPrompt = it },
                    label = { Text("Hinglish Expense / Query") },
                    placeholder = { Text("500 ka mithai, 1200 ki lights") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_prompt_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DiwaliGold,
                        focusedLabelColor = DiwaliGold,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (textPrompt.isNotBlank()) {
                        onSubmit(textPrompt)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DiwaliGold),
                modifier = Modifier.testTag("submit_manual_prompt_button")
            ) {
                Text("Log Karo", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}
