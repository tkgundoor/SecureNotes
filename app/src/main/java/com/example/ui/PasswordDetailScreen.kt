package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDetailScreen(
    passwordId: Int,
    viewModel: AppViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var otpSecret by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Compute Strength dynamically
    val strength = remember(passwordText) {
        when {
            passwordText.isEmpty() -> "Weak"
            passwordText.length < 8 -> "Weak"
            passwordText.length >= 12 && passwordText.any { !it.isLetterOrDigit() } -> "Strong"
            else -> "Medium"
        }
    }

    LaunchedEffect(passwordId) {
        if (passwordId > 0) {
            val pwd = viewModel.passwords.value.find { it.id == passwordId }
            pwd?.let {
                title = it.title
                username = it.username
                passwordText = it.encryptedPassword
                notes = it.notes
                otpSecret = it.otpSecret ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (passwordId == 0) "Add Password" else "Edit Credentials") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("pwd_detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.savePassword(
                                id = passwordId,
                                title = title,
                                username = username,
                                passwordText = passwordText,
                                notes = notes,
                                otpSecret = otpSecret.ifEmpty { null },
                                strength = strength
                            )
                            onBack()
                        },
                        modifier = Modifier.testTag("save_pwd_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Credentials", tint = TealAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateDark, titleContentColor = EditorialTitle)
            )
        },
        containerColor = SlateDark,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Website / Application Name", color = SlateTextSecondary) },
                placeholder = { Text("e.g. Google, GitHub...", color = SlateTextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pwd_title_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = EditorialDarkText,
                    unfocusedTextColor = EditorialDarkText,
                    focusedBorderColor = LavenderAccent,
                    unfocusedBorderColor = SlateGrey
                )
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username / Email", color = SlateTextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pwd_username_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = EditorialDarkText,
                    unfocusedTextColor = EditorialDarkText,
                    focusedBorderColor = LavenderAccent,
                    unfocusedBorderColor = SlateGrey
                )
            )

            OutlinedTextField(
                value = passwordText,
                onValueChange = { passwordText = it },
                label = { Text("Password", color = SlateTextSecondary) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = SlateTextSecondary
                        )
                    }
                },
                visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pwd_text_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = EditorialDarkText,
                    unfocusedTextColor = EditorialDarkText,
                    focusedBorderColor = LavenderAccent,
                    unfocusedBorderColor = SlateGrey
                )
            )

            // Password Strength Indicator row
            val strengthColor = when (strength) {
                "Strong" -> EmeraldSafe
                "Medium" -> GoldWarning
                else -> CrimsonAlert
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Password Strength Evaluator:", color = SlateTextSecondary, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = strength,
                    color = strengthColor,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedTextField(
                value = otpSecret,
                onValueChange = { otpSecret = it },
                label = { Text("2FA / OTP Secret (Optional)", color = SlateTextSecondary) },
                placeholder = { Text("Enter OTP token secret...", color = SlateTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = EditorialDarkText,
                    unfocusedTextColor = EditorialDarkText,
                    focusedBorderColor = LavenderAccent,
                    unfocusedBorderColor = SlateGrey
                )
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Secure Notes", color = SlateTextSecondary) },
                placeholder = { Text("Add custom note content about this login...", color = SlateTextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = EditorialDarkText,
                    unfocusedTextColor = EditorialDarkText,
                    focusedBorderColor = LavenderAccent,
                    unfocusedBorderColor = SlateGrey
                )
            )
        }
    }
}
