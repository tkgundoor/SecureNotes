package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Int,
    viewModel: AppViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Core edit states
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var isVault by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf<Long?>(null) }
    var isPinned by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }

    // Checklist state
    var newChecklistItem by remember { mutableStateOf("") }
    val checklistItems = remember { mutableStateListOf<ChecklistItem>() }

    // OCR simulator flag
    var showOcrScanner by remember { mutableStateOf(false) }

    // Reminder selector flag
    var showReminderDialog by remember { mutableStateOf(false) }

    // Check if loading existing note
    LaunchedEffect(noteId) {
        if (noteId == -99) {
            // New note directly into the Vault
            isVault = true
            category = "Secret"
        } else if (noteId > 0) {
            val note = viewModel.activeNotes.value.find { it.id == noteId }
                ?: viewModel.vaultNotes.value.find { it.id == noteId }
                ?: viewModel.archivedNotes.value.find { it.id == noteId }

            note?.let {
                title = it.title
                content = it.content
                category = it.category
                isVault = it.isVault
                isLocked = it.isLocked
                reminderTime = it.reminderTime
                isPinned = it.isPinned
                isFavorite = it.isFavorite

                // Parse checklist items if they exist
                if (!it.checklistJson.isNullOrEmpty()) {
                    checklistItems.clear()
                    it.checklistJson.split(";;").forEach { itemStr ->
                        val parts = itemStr.split("::")
                        if (parts.size == 2) {
                            checklistItems.add(ChecklistItem(parts[0], parts[1].toBoolean()))
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == 0 || noteId == -99) "Create Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            imageVector = if (isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                            contentDescription = "Pin",
                            tint = if (isPinned) BluePrimary else EditorialTextSecondary
                        )
                    }
                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) CrimsonAlert else EditorialTextSecondary
                        )
                    }
                    IconButton(
                        onClick = {
                            val checklistStr = if (checklistItems.isNotEmpty()) {
                                checklistItems.joinToString(";;") { "${it.text}::${it.checked}" }
                            } else null

                            viewModel.saveNote(
                                id = if (noteId < 0) 0 else noteId,
                                title = title,
                                content = content,
                                category = category,
                                isVault = isVault,
                                isLocked = isLocked,
                                reminderTime = reminderTime,
                                checklistJson = checklistStr
                            )
                            onBack()
                        },
                        modifier = Modifier.testTag("save_note_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Note", tint = TealAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateDark, titleContentColor = EditorialTitle)
            )
        },
        containerColor = SlateDark,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Form Elements
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title", color = SlateTextSecondary) },
                    placeholder = { Text("Enter a descriptive title...", color = SlateTextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = EditorialDarkText,
                        unfocusedTextColor = EditorialDarkText,
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = SlateGrey
                    )
                )
            }

            // Quick Formatting & Tool Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SlateCard)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { content += " <b></b>" }) {
                        Icon(Icons.Default.FormatBold, contentDescription = "Bold", tint = EditorialDarkText)
                    }
                    IconButton(onClick = { content += " <i></i>" }) {
                        Icon(Icons.Default.FormatItalic, contentDescription = "Italic", tint = EditorialDarkText)
                    }
                    IconButton(onClick = { content += " <u></u>" }) {
                        Icon(Icons.Default.FormatUnderlined, contentDescription = "Underline", tint = EditorialDarkText)
                    }
                    IconButton(onClick = { showOcrScanner = true }, modifier = Modifier.testTag("ocr_scanner_button")) {
                        Icon(Icons.Default.DocumentScanner, contentDescription = "ML Kit OCR Scanner", tint = TealAccent)
                    }
                    IconButton(onClick = { showReminderDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Set Reminder",
                            tint = if (reminderTime != null) GoldWarning else EditorialTextSecondary
                        )
                    }
                }
            }

            // Note Content Editor
            item {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Write your thoughts...", color = SlateTextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                        .testTag("note_content_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = EditorialDarkText,
                        unfocusedTextColor = EditorialDarkText,
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = SlateGrey
                    )
                )
            }

            // Security Settings
            item {
                Text("Security Safeguards", style = MaterialTheme.typography.titleSmall, color = SlateTextSecondary)
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = SlateCard)) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Move to Secure Hidden Vault", color = EditorialDarkText) },
                            supportingContent = { Text("Hides note from standard listings", color = SlateTextSecondary) },
                            leadingContent = { Icon(Icons.Default.EnhancedEncryption, contentDescription = null, tint = TealAccent) },
                            trailingContent = {
                                Switch(
                                    checked = isVault,
                                    onCheckedChange = { isVault = it },
                                    modifier = Modifier.testTag("is_vault_toggle")
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                        HorizontalDivider(color = SlateGrey, thickness = 1.dp)
                        ListItem(
                            headlineContent = { Text("Individually Password-Lock", color = EditorialDarkText) },
                            supportingContent = { Text("Prompts for master PIN even if app is unlocked", color = SlateTextSecondary) },
                            leadingContent = { Icon(Icons.Default.Lock, contentDescription = null, tint = CrimsonAlert) },
                            trailingContent = {
                                Switch(
                                    checked = isLocked,
                                    onCheckedChange = { isLocked = it },
                                    modifier = Modifier.testTag("is_locked_toggle")
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }

            // Category Picker
            item {
                Text("Note Category", style = MaterialTheme.typography.titleSmall, color = SlateTextSecondary)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val catList = listOf("General", "Work", "Personal", "Secret", "Financial")
                    catList.forEach { cat ->
                        val isSelected = category == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary,
                                selectedLabelColor = Color.White,
                                containerColor = SlateCard,
                                labelColor = SlateTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = SlateGrey,
                                selectedBorderColor = BluePrimary
                            )
                        )
                    }
                }
            }

            // Checklist Section
            item {
                Text("Active Checklist Items", style = MaterialTheme.typography.titleSmall, color = SlateTextSecondary)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newChecklistItem,
                        onValueChange = { newChecklistItem = it },
                        placeholder = { Text("Add checklist step...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_checklist_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = EditorialDarkText,
                            unfocusedTextColor = EditorialDarkText,
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = SlateGrey
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newChecklistItem.isNotEmpty()) {
                                checklistItems.add(ChecklistItem(newChecklistItem, false))
                                newChecklistItem = ""
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BluePrimary)
                            .testTag("add_checklist_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Item", tint = Color.White)
                    }
                }
            }

            // Render Checklist Items
            if (checklistItems.isNotEmpty()) {
                itemsIndexed(checklistItems) { index, item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SlateCard)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.checked,
                                onCheckedChange = { isChecked ->
                                    checklistItems[index] = item.copy(checked = isChecked)
                                },
                                modifier = Modifier.testTag("checklist_checkbox_$index")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.text,
                                color = if (item.checked) SlateTextSecondary else Color.White,
                                modifier = Modifier.weight(1f),
                                style = if (item.checked) MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else MaterialTheme.typography.bodyMedium
                            )
                            IconButton(onClick = { checklistItems.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove Item", tint = CrimsonAlert)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Simulated OCR Scanning View ---
    if (showOcrScanner) {
        var scannerLaserAnim by remember { mutableStateOf(false) }
        var isScanningFinished by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            scannerLaserAnim = true
            delay(2500)
            isScanningFinished = true
        }

        AlertDialog(
            onDismissRequest = { showOcrScanner = false },
            title = { Text("ML Kit Text Recognition", color = EditorialDarkText) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!isScanningFinished) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SlateDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = TealAccent)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Scanning active viewport...", color = EditorialPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Column {
                            Text("Recognized Document Text:", color = EditorialDarkText, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = SlateDark)) {
                                Text(
                                    text = "SECURE SANDBOX DOCUMENT\nLevel: Alpha Cryptographic\nParsed text: 'Do not distribute sensitive data or passwords without dynamic salt hashes.'",
                                    color = EditorialPrimary,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (isScanningFinished) {
                    Button(
                        onClick = {
                            content += "\n[OCR Parse: SECURE SANDBOX DOCUMENT. Parsed text: 'Do not distribute sensitive data or passwords without dynamic salt hashes.']"
                            showOcrScanner = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealAccent, contentColor = Color.Black)
                    ) {
                        Text("Import Text")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showOcrScanner = false }) {
                    Text("Cancel", color = SlateTextSecondary)
                }
            },
            containerColor = SlateCard
        )
    }

    // --- Reminder Setting Dialog ---
    if (showReminderDialog) {
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text("Set Secured Reminder", color = EditorialDarkText) },
            text = {
                Column {
                    Text("Schedule local secure notification alert for this note's secrets:", color = SlateTextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    listOf("In 15 Minutes", "In 1 Hour", "Tomorrow Morning", "Weekly Recurring").forEach { option ->
                        Button(
                            onClick = {
                                reminderTime = System.currentTimeMillis() + 900000 // simulate scheduling
                                showReminderDialog = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SlateGrey)
                        ) {
                            Text(option, color = EditorialDarkText)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showReminderDialog = false }) {
                    Text("Close", color = SlateTextSecondary)
                }
            },
            containerColor = SlateCard
        )
    }
}

data class ChecklistItem(val text: String, val checked: Boolean)
