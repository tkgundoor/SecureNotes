package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.NoteEntity
import com.example.data.PasswordEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    onNavigateToNote: (Int) -> Unit,
    onNavigateToPassword: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(DashboardTab.NOTES) }
    
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isDecoyMode by viewModel.isDecoyMode.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = if (isDecoyMode) GoldWarning else BluePrimary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            if (isDecoyMode) {
                                Text(
                                    text = "System Core",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = EditorialTitle,
                                    modifier = Modifier.testTag("app_title")
                                )
                                Text(
                                    text = "Safe Environment Active",
                                    color = GoldWarning,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Secure",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Light,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = EditorialTitle,
                                        modifier = Modifier.testTag("app_title")
                                    )
                                    Text(
                                        text = "Notes",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = EditorialTitle
                                    )
                                }
                                Text(
                                    text = "ENCRYPTED & SECURE",
                                    color = EditorialPrimary,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setLockout() },
                        modifier = Modifier.testTag("lock_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock App",
                            tint = CrimsonAlert
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateDark,
                    titleContentColor = EditorialTitle
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SlateCard,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == DashboardTab.NOTES,
                    onClick = { currentTab = DashboardTab.NOTES },
                    icon = { Icon(Icons.Default.Description, contentDescription = "Notes") },
                    label = { Text("Notes") },
                    modifier = Modifier.testTag("bottom_nav_notes"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EditorialPrimary,
                        selectedTextColor = EditorialPrimary,
                        unselectedIconColor = SlateTextSecondary,
                        unselectedTextColor = SlateTextSecondary,
                        indicatorColor = EditorialSecondary
                    )
                )
                NavigationBarItem(
                    selected = currentTab == DashboardTab.VAULT,
                    onClick = { currentTab = DashboardTab.VAULT },
                    icon = { Icon(Icons.Default.EnhancedEncryption, contentDescription = "Vault") },
                    label = { Text("Vault") },
                    modifier = Modifier.testTag("bottom_nav_vault"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EditorialPrimary,
                        selectedTextColor = EditorialPrimary,
                        unselectedIconColor = SlateTextSecondary,
                        unselectedTextColor = SlateTextSecondary,
                        indicatorColor = EditorialSecondary
                    )
                )
                NavigationBarItem(
                    selected = currentTab == DashboardTab.PASSWORDS,
                    onClick = { currentTab = DashboardTab.PASSWORDS },
                    icon = { Icon(Icons.Default.Key, contentDescription = "Passwords") },
                    label = { Text("Passwords") },
                    modifier = Modifier.testTag("bottom_nav_passwords"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EditorialPrimary,
                        selectedTextColor = EditorialPrimary,
                        unselectedIconColor = SlateTextSecondary,
                        unselectedTextColor = SlateTextSecondary,
                        indicatorColor = EditorialSecondary
                    )
                )
                NavigationBarItem(
                    selected = currentTab == DashboardTab.SECURITY,
                    onClick = { currentTab = DashboardTab.SECURITY },
                    icon = { Icon(Icons.Default.Security, contentDescription = "Security") },
                    label = { Text("Security") },
                    modifier = Modifier.testTag("bottom_nav_security"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EditorialPrimary,
                        selectedTextColor = EditorialPrimary,
                        unselectedIconColor = SlateTextSecondary,
                        unselectedTextColor = SlateTextSecondary,
                        indicatorColor = EditorialSecondary
                    )
                )
            }
        },
        containerColor = SlateDark,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                DashboardTab.NOTES -> NotesTab(viewModel, onNavigateToNote)
                DashboardTab.VAULT -> VaultTab(viewModel, onNavigateToNote)
                DashboardTab.PASSWORDS -> PasswordsTab(viewModel, onNavigateToPassword)
                DashboardTab.SECURITY -> SecurityTab(viewModel)
            }
        }
    }
}

enum class DashboardTab {
    NOTES, VAULT, PASSWORDS, SECURITY
}

// ==========================================
// 1. NOTES TAB
// ==========================================
@Composable
fun NotesTab(
    viewModel: AppViewModel,
    onNavigateToNote: (Int) -> Unit
) {
    val notes by viewModel.activeNotes.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search notes, labels...", color = SlateTextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SlateTextSecondary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = SlateTextSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = SlateGrey,
                    focusedContainerColor = SlateCard,
                    unfocusedContainerColor = SlateCard,
                    focusedTextColor = EditorialDarkText,
                    unfocusedTextColor = EditorialDarkText
                )
            )

            // Categories Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(cat) },
                        label = { Text(cat) },
                        modifier = Modifier.testTag("category_chip_${cat.lowercase()}"),
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

            if (notes.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.EditNote,
                    title = "No active notes",
                    subtitle = if (searchQuery.isNotEmpty()) "Try adjusting your search filters" else "Create your first highly secured note!"
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNavigateToNote(note.id) },
                            onPinToggle = { viewModel.togglePinNote(note) },
                            onFavToggle = { viewModel.toggleFavoriteNote(note) },
                            onArchive = { viewModel.toggleArchiveNote(note) },
                            onDelete = { viewModel.deleteNote(note) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Notes
        FloatingActionButton(
            onClick = { onNavigateToNote(0) },
            containerColor = TealAccent,
            contentColor = EditorialTitle,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_note"),
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Note")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onFavToggle: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
            .testTag("note_card_${note.id}"),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = EditorialDarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = BluePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (note.isLocked || note.isVault) "••••••••••" else note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = SlateTextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateGrey)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = note.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = TealAccent
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (note.isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = CrimsonAlert,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (note.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = CrimsonAlert,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false },
        modifier = Modifier.background(SlateGrey)
    ) {
        DropdownMenuItem(
            text = { Text(if (note.isPinned) "Unpin Note" else "Pin Note", color = Color.White) },
            onClick = { onPinToggle(); showMenu = false },
            leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, tint = BluePrimary) }
        )
        DropdownMenuItem(
            text = { Text(if (note.isFavorite) "Remove Favorite" else "Favorite", color = Color.White) },
            onClick = { onFavToggle(); showMenu = false },
            leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = CrimsonAlert) }
        )
        DropdownMenuItem(
            text = { Text("Archive", color = Color.White) },
            onClick = { onArchive(); showMenu = false },
            leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null, tint = SlateTextSecondary) }
        )
        DropdownMenuItem(
            text = { Text("Delete", color = Color.White) },
            onClick = { onDelete(); showMenu = false },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = CrimsonAlert) }
        )
    }
}

// ==========================================
// 2. ENCRYPTED SECURE VAULT TAB
// ==========================================
@Composable
fun VaultTab(
    viewModel: AppViewModel,
    onNavigateToNote: (Int) -> Unit
) {
    val vaultNotes by viewModel.vaultNotes.collectAsStateWithLifecycle()
    var isVaultAuthenticated by remember { mutableStateOf(false) }
    var passwordPrompt by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf(false) }

    val isDecoyMode by viewModel.isDecoyMode.collectAsStateWithLifecycle()

    if (!isVaultAuthenticated && !isDecoyMode) {
        // Vault Authentication Shield
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EnhancedEncryption,
                contentDescription = "Security Vault",
                tint = TealAccent,
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Secure Hidden Vault",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = EditorialDarkText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your Master PIN to unlock the zero-knowledge encrypted hidden vault",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateTextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = passwordPrompt,
                onValueChange = { passwordPrompt = it; authError = false },
                placeholder = { Text("Master PIN") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_auth_pin"),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealAccent,
                    unfocusedBorderColor = SlateGrey,
                    focusedTextColor = EditorialDarkText,
                    unfocusedTextColor = EditorialDarkText
                )
            )

            if (authError) {
                Text(
                    text = "Incorrect PIN code. Intruder attempt logged.",
                    color = CrimsonAlert,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.verifyUnlockPin(passwordPrompt) { success ->
                        if (success) {
                            isVaultAuthenticated = true
                        } else {
                            authError = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("vault_unlock_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TealAccent, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Decrypt Vault", fontWeight = FontWeight.Bold)
            }
        }
    } else {
        // Vault Notes Display List
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(SlateCard, SlateGrey)))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Https,
                                contentDescription = null,
                                tint = TealAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isDecoyMode) "Simulated Vault Active" else "Zero-Knowledge Encryption",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isDecoyMode) "Displaying non-encrypted temporary secure items" else "Notes and files in this view are AES-256-GCM encrypted in database storage",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateTextSecondary
                        )
                    }
                }

                if (vaultNotes.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Outlined.Lock,
                        title = "No vault notes",
                        subtitle = "Tap the '+' to create a highly encrypted, invisible secure note"
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(vaultNotes) { note ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToNote(note.id) }
                                    .testTag("vault_note_item_${note.id}"),
                                colors = CardDefaults.cardColors(containerColor = SlateCard),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = note.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Https,
                                            contentDescription = null,
                                            tint = TealAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = note.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SlateTextSecondary,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { onNavigateToNote(-99) }, // Special flag for vault note
                containerColor = TealAccent,
                contentColor = Color.Black,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("fab_add_vault_note"),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Vault Note")
            }
        }
    }
}

// ==========================================
// 3. SECURE PASSWORD MANAGER TAB
// ==========================================
@Composable
fun PasswordsTab(
    viewModel: AppViewModel,
    onNavigateToPassword: (Int) -> Unit
) {
    val passwords by viewModel.passwords.collectAsStateWithLifecycle()
    var isPwdAuthenticated by remember { mutableStateOf(false) }
    var passwordPrompt by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf(false) }

    val isDecoyMode by viewModel.isDecoyMode.collectAsStateWithLifecycle()

    // Dialog for password strength generator
    var showGenerator by remember { mutableStateOf(false) }

    if (!isPwdAuthenticated && !isDecoyMode) {
        // Password Shield
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = "Passwords Vault",
                tint = LavenderAccent,
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Credentials Manager",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = EditorialDarkText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Access your master encrypted passcodes and credential hashes securely",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = passwordPrompt,
                onValueChange = { passwordPrompt = it; authError = false },
                placeholder = { Text("Master PIN") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pwd_auth_pin"),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LavenderAccent,
                    unfocusedBorderColor = SlateGrey,
                    focusedTextColor = EditorialDarkText,
                    unfocusedTextColor = EditorialDarkText
                )
            )

            if (authError) {
                Text(
                    text = "Incorrect PIN. Intruder log created.",
                    color = CrimsonAlert,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.verifyUnlockPin(passwordPrompt) { success ->
                        if (success) {
                            isPwdAuthenticated = true
                        } else {
                            authError = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pwd_unlock_button"),
                colors = ButtonDefaults.buttonColors(containerColor = LavenderAccent, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Unlock Credentials", fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header / Generator option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Encrypted Passwords",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    TextButton(
                        onClick = { showGenerator = true },
                        modifier = Modifier.testTag("trigger_generator")
                    ) {
                        Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Generate Pass", color = LavenderAccent, fontWeight = FontWeight.Bold)
                    }
                }

                if (passwords.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Outlined.Key,
                        title = "No credentials saved",
                        subtitle = "Tap '+' to store login credentials securely"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(passwords) { pwd ->
                            var revealed by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("password_card_${pwd.id}"),
                                colors = CardDefaults.cardColors(containerColor = SlateCard)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = pwd.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Username: ${pwd.username}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SlateTextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (revealed) pwd.encryptedPassword else "••••••••",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = LavenderAccent
                                            )
                                            IconButton(
                                                onClick = { revealed = !revealed },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (revealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = SlateTextSecondary
                                                )
                                            }
                                        }
                                        if (pwd.notes.isNotEmpty()) {
                                            Text(
                                                text = "Notes: ${pwd.notes}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SlateTextSecondary
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        // Strength badge
                                        val color = when (pwd.strength.lowercase()) {
                                            "strong" -> EmeraldSafe
                                            "medium" -> GoldWarning
                                            else -> CrimsonAlert
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(color.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(pwd.strength, color = color, style = MaterialTheme.typography.labelSmall)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row {
                                            IconButton(
                                                onClick = { viewModel.copyToClipboardSecure(pwd.title, pwd.encryptedPassword) },
                                                modifier = Modifier.testTag("copy_password_${pwd.id}")
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Password", tint = BluePrimary, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(
                                                onClick = { onNavigateToPassword(pwd.id) },
                                                modifier = Modifier.testTag("edit_password_${pwd.id}")
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Credentials", tint = SlateTextSecondary, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { onNavigateToPassword(0) },
                containerColor = LavenderAccent,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("fab_add_password"),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Password")
            }
        }
    }

    if (showGenerator) {
        PasswordGeneratorDialog(
            viewModel = viewModel,
            onDismiss = { showGenerator = false }
        )
    }
}

@Composable
fun PasswordGeneratorDialog(
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    var length by remember { mutableStateOf(16f) }
    var includeLetters by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSymbols by remember { mutableStateOf(true) }
    var generatedPassword by remember { mutableStateOf("") }

    fun generate() {
        val letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numbers = "0123456789"
        val symbols = "!@#$%^&*()_+{}[]|:;<>,.?"
        var pool = ""
        if (includeLetters) pool += letters
        if (includeNumbers) pool += numbers
        if (includeSymbols) pool += symbols
        if (pool.isEmpty()) pool = letters

        generatedPassword = (1..length.toInt())
            .map { pool.random() }
            .joinToString("")
    }

    LaunchedEffect(length, includeLetters, includeNumbers, includeSymbols) {
        generate()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate Password", color = EditorialDarkText) },
        text = {
            Column {
                OutlinedTextField(
                    value = generatedPassword,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { viewModel.copyToClipboardSecure("Generated", generatedPassword) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = LavenderAccent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("generated_pass_output"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = EditorialDarkText,
                        unfocusedTextColor = EditorialDarkText,
                        focusedContainerColor = SlateDark,
                        unfocusedContainerColor = SlateDark
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Length: ${length.toInt()}", color = EditorialDarkText, style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = length,
                    onValueChange = { length = it },
                    valueRange = 8f..32f,
                    colors = SliderDefaults.colors(
                        thumbColor = LavenderAccent,
                        activeTrackColor = LavenderAccent,
                        inactiveTrackColor = SlateGrey
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeLetters, onCheckedChange = { includeLetters = it })
                    Text("Letters (a-z, A-Z)", color = EditorialDarkText)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeNumbers, onCheckedChange = { includeNumbers = it })
                    Text("Numbers (0-9)", color = EditorialDarkText)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeSymbols, onCheckedChange = { includeSymbols = it })
                    Text("Symbols (!@#$)", color = EditorialDarkText)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { generate() },
                colors = ButtonDefaults.buttonColors(containerColor = LavenderAccent)
            ) {
                Text("Regenerate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = SlateTextSecondary)
            }
        },
        containerColor = SlateCard
    )
}

// ==========================================
// 4. SECURITY CENTER DASHBOARD TAB
// ==========================================
@Composable
fun SecurityTab(viewModel: AppViewModel) {
    val logs by viewModel.intruderLogs.collectAsStateWithLifecycle()
    val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsStateWithLifecycle(false)
    val preventScreenshot by viewModel.preventScreenshot.collectAsStateWithLifecycle(false)
    val notificationPrivacy by viewModel.notificationPrivacy.collectAsStateWithLifecycle(false)
    val isPanicLockShakeEnabled by viewModel.isPanicLockShakeEnabled.collectAsStateWithLifecycle(false)
    val autoClearSec by viewModel.autoClearClipboardSec.collectAsStateWithLifecycle(30)
    val selfDestructLimit by viewModel.selfDestructLimit.collectAsStateWithLifecycle(5)

    var hasPin by remember { mutableStateOf(false) }
    var hasDecoyPin by remember { mutableStateOf(false) }

    val hasPinSetup by viewModel.hasPin.collectAsStateWithLifecycle(false)
    val hasDecoySetup by viewModel.hasDecoyPin.collectAsStateWithLifecycle(false)

    // Dialog flags
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showDecoySetupDialog by remember { mutableStateOf(false) }

    // Security Score Computing
    val securityScore = remember(hasPinSetup, hasDecoySetup, preventScreenshot, notificationPrivacy, isPanicLockShakeEnabled) {
        var score = 0
        if (hasPinSetup) score += 40
        if (hasDecoySetup) score += 20
        if (preventScreenshot) score += 15
        if (notificationPrivacy) score += 10
        if (isPanicLockShakeEnabled) score += 15
        score
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Score Indicator Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(
                                        CrimsonAlert,
                                        GoldWarning,
                                        EmeraldSafe,
                                        EmeraldSafe
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SlateCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$securityScore%",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (securityScore > 75) EmeraldSafe else if (securityScore > 40) GoldWarning else CrimsonAlert
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        Text(
                            text = "Security Audit Score",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = EditorialDarkText
                        )
                        Text(
                            text = if (securityScore > 75) "Highly Secure Sandbox" else if (securityScore > 40) "Standard Protection Active" else "Warning: Encryption setup required",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateTextSecondary
                        )
                    }
                }
            }
        }

        // Lock & Core Settings
        item {
            Column {
                Text(
                    text = "App Guard & Credentials",
                    style = MaterialTheme.typography.titleSmall,
                    color = SlateTextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        ListItem(
                            headlineContent = { Text("App Master Lock", color = EditorialDarkText) },
                            supportingContent = { Text(if (hasPinSetup) "Master passcode configured" else "Vulnerable: Setup passcode lock", color = SlateTextSecondary) },
                            leadingContent = { Icon(Icons.Default.Lock, contentDescription = null, tint = if (hasPinSetup) EmeraldSafe else CrimsonAlert) },
                            trailingContent = {
                                Switch(
                                    checked = hasPinSetup,
                                    onCheckedChange = {
                                        if (it) {
                                            showPinSetupDialog = true
                                        } else {
                                            viewModel.removePin()
                                        }
                                    },
                                    modifier = Modifier.testTag("app_lock_toggle")
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        HorizontalDivider(color = SlateGrey, thickness = 1.dp)

                        ListItem(
                            headlineContent = { Text("Decoy Vault PIN", color = EditorialDarkText) },
                            supportingContent = { Text(if (hasDecoySetup) "Opens decoy data container" else "Provide a fake PIN to show fake notes", color = SlateTextSecondary) },
                            leadingContent = { Icon(Icons.Default.Masks, contentDescription = null, tint = if (hasDecoySetup) EmeraldSafe else SlateTextSecondary) },
                            trailingContent = {
                                Switch(
                                    checked = hasDecoySetup,
                                    onCheckedChange = {
                                        if (it) {
                                            showDecoySetupDialog = true
                                        } else {
                                            viewModel.removeDecoyPin()
                                        }
                                    },
                                    modifier = Modifier.testTag("decoy_pin_toggle")
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }

        // Defensive Policies Settings
        item {
            Column {
                Text(
                    text = "Defensive Guardrails",
                    style = MaterialTheme.typography.titleSmall,
                    color = SlateTextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Prevent Screenshot (FLAG_SECURE)", color = EditorialDarkText) },
                            supportingContent = { Text("Blocks screen recording & recents preview", color = SlateTextSecondary) },
                            leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = if (preventScreenshot) BluePrimary else SlateTextSecondary) },
                            trailingContent = {
                                Switch(
                                    checked = preventScreenshot,
                                    onCheckedChange = { viewModel.togglePreventScreenshot(it) },
                                    modifier = Modifier.testTag("prevent_screenshot_toggle")
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        HorizontalDivider(color = SlateGrey, thickness = 1.dp)

                        ListItem(
                            headlineContent = { Text("Shake for Panic Lock", color = EditorialDarkText) },
                            supportingContent = { Text("Instantly locks and hides active session on shake", color = SlateTextSecondary) },
                            leadingContent = { Icon(Icons.Default.ScreenRotation, contentDescription = null, tint = if (isPanicLockShakeEnabled) BluePrimary else SlateTextSecondary) },
                            trailingContent = {
                                Switch(
                                    checked = isPanicLockShakeEnabled,
                                    onCheckedChange = { viewModel.togglePanicLockShake(it) },
                                    modifier = Modifier.testTag("panic_lock_shake_toggle")
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        HorizontalDivider(color = SlateGrey, thickness = 1.dp)

                        ListItem(
                            headlineContent = { Text("Auto-Clear Clipboard", color = EditorialDarkText) },
                            supportingContent = { Text("Clear sensitive copies after $autoClearSec seconds", color = SlateTextSecondary) },
                            leadingContent = { Icon(Icons.Default.ContentPasteGo, contentDescription = null, tint = BluePrimary) },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("$autoClearSec s", color = EditorialDarkText, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = {
                                        val next = if (autoClearSec == 15) 30 else if (autoClearSec == 30) 60 else 15
                                        viewModel.updateAutoClearClipboardSec(next)
                                    }) {
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = EditorialDarkText)
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }

        // Intruder & Intrusion logs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Intruder Access Logs",
                    style = MaterialTheme.typography.titleSmall,
                    color = SlateTextSecondary
                )
                if (logs.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.clearLogs() },
                        modifier = Modifier.testTag("clear_logs_button")
                    ) {
                        Text("Clear Logs", color = CrimsonAlert)
                    }
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSafe, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No unauthorized access attempts", color = EditorialDarkText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        } else {
            items(logs) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text("Failed Login Attempt", color = CrimsonAlert, fontWeight = FontWeight.Bold) },
                        supportingContent = {
                            Column {
                                Text("Attempted: ${log.failedPinAttempted}", color = EditorialDarkText)
                                Text("Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp))}", color = SlateTextSecondary)
                            }
                        },
                        leadingContent = {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, tint = CrimsonAlert)
                        },
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SlateGrey)
                                    .padding(8.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = GoldWarning, modifier = Modifier.size(16.dp))
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }

    // --- PIN setup dialog ---
    if (showPinSetupDialog) {
        var pinInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinSetupDialog = false },
            title = { Text("Setup Master PIN", color = EditorialDarkText) },
            text = {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 8) pinInput = it },
                    placeholder = { Text("4 to 8 digits PIN") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("setup_pin_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = EditorialDarkText,
                        unfocusedTextColor = EditorialDarkText,
                        focusedContainerColor = SlateDark,
                        unfocusedContainerColor = SlateDark
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length >= 4) {
                            viewModel.setupPin(pinInput)
                            showPinSetupDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinSetupDialog = false }) {
                    Text("Cancel", color = SlateTextSecondary)
                }
            },
            containerColor = SlateCard
        )
    }

    // --- Decoy Setup Dialog ---
    if (showDecoySetupDialog) {
        var pinInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDecoySetupDialog = false },
            title = { Text("Setup Decoy PIN", color = EditorialDarkText) },
            text = {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 8) pinInput = it },
                    placeholder = { Text("Alternative fake PIN code") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("setup_decoy_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = EditorialDarkText,
                        unfocusedTextColor = EditorialDarkText,
                        focusedContainerColor = SlateDark,
                        unfocusedContainerColor = SlateDark
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length >= 4) {
                            viewModel.setupDecoyPin(pinInput)
                            showDecoySetupDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldWarning, contentColor = Color.Black)
                ) {
                    Text("Save Decoy")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDecoySetupDialog = false }) {
                    Text("Cancel", color = SlateTextSecondary)
                }
            },
            containerColor = SlateCard
        )
    }
}

// ==========================================
// EMPTY STATE / PLACEHOLDER VIEW
// ==========================================
@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SlateTextSecondary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = SlateTextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
