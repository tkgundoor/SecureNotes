package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*

@Composable
fun LockScreen(
    viewModel: AppViewModel,
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasPinSetup by viewModel.hasPin.collectAsStateWithLifecycle(false)
    val failedAttempts by viewModel.failedAttempts.collectAsStateWithLifecycle(0)

    var pinEntered by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // First time setup states
    var setupStep by remember { mutableStateOf(1) } // 1: Enter PIN, 2: Confirm PIN
    var firstSetupPin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDark)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = if (!hasPinSetup) GoldWarning else BluePrimary,
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = if (!hasPinSetup) {
                    if (setupStep == 1) "Create Master PIN" else "Confirm Master PIN"
                } else {
                    "System Locked"
                },
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = EditorialDarkText
            )

            Text(
                text = if (!hasPinSetup) {
                    "Set a 4 to 8 digit security PIN code to protect your notes and vaults"
                } else {
                    "Enter security credentials to decrypt container"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = SlateTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp).padding(horizontal = 16.dp)
            )
        }

        // Enter PIN Dots Progress Row
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                val dotsCount = if (!hasPinSetup) pinEntered.length else pinEntered.length
                (1..6).forEach { index ->
                    val isActive = index <= dotsCount
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isActive) BluePrimary else SlateGrey)
                    )
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = CrimsonAlert,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else if (failedAttempts > 0) {
                Text(
                    text = "Incorrect attempts: $failedAttempts",
                    color = GoldWarning,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Tactile Numeric keypad (3x4)
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("Fingerprint", "0", "Delete")
            )

            rows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.2f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SlateCard)
                                .clickable {
                                    errorMessage = ""
                                    when (key) {
                                        "Delete" -> {
                                            if (pinEntered.isNotEmpty()) {
                                                pinEntered = pinEntered.dropLast(1)
                                            }
                                        }
                                        "Fingerprint" -> {
                                            // Simulate biometric unlock
                                            if (hasPinSetup) {
                                                viewModel.verifyUnlockPin("1234") { success ->
                                                    if (success) onUnlocked()
                                                }
                                            }
                                        }
                                        else -> {
                                            if (pinEntered.length < 8) {
                                                pinEntered += key
                                            }
                                        }
                                    }
                                }
                                .testTag("pin_key_$key"),
                            contentAlignment = Alignment.Center
                        ) {
                            when (key) {
                                "Delete" -> Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = EditorialDarkText)
                                "Fingerprint" -> Icon(Icons.Default.Fingerprint, contentDescription = "Biometric", tint = EditorialPrimary)
                                else -> Text(
                                    text = key,
                                    fontSize = 24.sp,
                                    color = EditorialDarkText,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button for Unlock or Save
            Button(
                onClick = {
                    if (pinEntered.length < 4) {
                        errorMessage = "PIN code must be at least 4 digits"
                        return@Button
                    }

                    if (!hasPinSetup) {
                        if (setupStep == 1) {
                            firstSetupPin = pinEntered
                            pinEntered = ""
                            setupStep = 2
                        } else {
                            if (pinEntered == firstSetupPin) {
                                viewModel.setupPin(pinEntered)
                                onUnlocked()
                            } else {
                                errorMessage = "PINs do not match. Try again."
                                pinEntered = ""
                                setupStep = 1
                            }
                        }
                    } else {
                        viewModel.verifyUnlockPin(pinEntered) { success ->
                            if (success) {
                                onUnlocked()
                            } else {
                                errorMessage = "Invalid PIN code. Access denied."
                                pinEntered = ""
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pin_submit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!hasPinSetup) GoldWarning else BluePrimary,
                    contentColor = if (!hasPinSetup) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (!hasPinSetup) {
                        if (setupStep == 1) "Next" else "Save Master PIN"
                    } else {
                        "Decrypt Vault"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
