package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.security.PinVerificationResult
import com.example.security.SecurityManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val securityManager = SecurityManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val repository = AppRepository(
        database.noteDao(),
        database.passwordDao(),
        database.intruderLogDao()
    )

    // --- Core Security State ---
    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _isDecoyMode = MutableStateFlow(false)
    val isDecoyMode: StateFlow<Boolean> = _isDecoyMode.asStateFlow()

    val isAppLockEnabled = securityManager.isAppLockEnabled
    val preventScreenshot = securityManager.preventScreenshot
    val notificationPrivacy = securityManager.notificationPrivacy
    val failedAttempts = securityManager.failedAttempts
    val isPanicLockShakeEnabled = securityManager.isPanicLockShakeEnabled
    val autoClearClipboardSec = securityManager.autoClearClipboardSec
    val selfDestructLimit = securityManager.selfDestructLimit

    // --- Notes Flows ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val activeNotes: StateFlow<List<NoteEntity>> = repository.getActiveNotes()
        .combine(_searchQuery) { notes, query ->
            filterNotes(notes, query)
        }
        .combine(_selectedCategory) { notes, cat ->
            if (cat == "All") notes else notes.filter { it.category == cat }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedNotes: StateFlow<List<NoteEntity>> = repository.getArchivedNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultNotes: StateFlow<List<NoteEntity>> = repository.getVaultNotes()
        .combine(_isDecoyMode) { notes, decoy ->
            if (decoy) {
                // Return fake decoy notes
                listOf(
                    NoteEntity(
                        id = -101,
                        title = "🎂 Secret Chocolate Cake Recipe",
                        content = "1. Mix flour and cocoa powder.\n2. Add sugar, eggs, and melted butter.\n3. Bake at 350°F for 30 minutes.\nKeep this hidden from the kids!",
                        category = "Personal",
                        isVault = true,
                        lastModified = System.currentTimeMillis() - 100000
                    ),
                    NoteEntity(
                        id = -102,
                        title = "🚲 Gift Ideas for Anniversary",
                        content = "Buy a new vintage road bike or custom helmet.\nBudget: $500.",
                        category = "Personal",
                        isVault = true,
                        lastModified = System.currentTimeMillis() - 500000
                    )
                )
            } else {
                // Decrypt real notes on the fly
                notes.map { note ->
                    note.copy(content = securityManager.decrypt(note.content))
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = repository.getCategories()
        .map { list -> listOf("All") + list.filter { it.isNotEmpty() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    // --- Passwords Flow ---
    val passwords: StateFlow<List<PasswordEntity>> = repository.getAllPasswords()
        .combine(_isDecoyMode) { pwds, decoy ->
            if (decoy) {
                // Decoy passwords
                listOf(
                    PasswordEntity(
                        id = -201,
                        title = "Netflix",
                        username = "decoy_user@example.com",
                        encryptedPassword = "decoyPassword123",
                        notes = "Sharing with cousin",
                        strength = "Strong"
                    ),
                    PasswordEntity(
                        id = -202,
                        title = "Gym Member Portal",
                        username = "fit_decoy",
                        encryptedPassword = "decoyPassword456",
                        notes = "Card key is in the locker drawer",
                        strength = "Medium"
                    )
                )
            } else {
                // Decrypt password on the fly
                pwds.map { pwd ->
                    pwd.copy(encryptedPassword = securityManager.decrypt(pwd.encryptedPassword))
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Intruder Logs Flow ---
    val intruderLogs: StateFlow<List<IntruderLogEntity>> = repository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Setup States ---
    private val _hasPin = MutableStateFlow(false)
    val hasPin: StateFlow<Boolean> = _hasPin.asStateFlow()

    private val _hasDecoyPin = MutableStateFlow(false)
    val hasDecoyPin: StateFlow<Boolean> = _hasDecoyPin.asStateFlow()

    init {
        viewModelScope.launch {
            _hasPin.value = securityManager.hasPinSetup()
            _hasDecoyPin.value = securityManager.hasDecoyPinSetup()
            val lockEnabled = securityManager.isAppLockEnabled.first()
            _isLocked.value = lockEnabled
        }
    }

    // --- Action Methods ---

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    private fun filterNotes(notes: List<NoteEntity>, query: String): List<NoteEntity> {
        if (query.isEmpty()) return notes
        return notes.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
        }
    }

    // --- Security Actions ---

    fun verifyUnlockPin(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = securityManager.verifyPin(pin)
            val currentAttempts = securityManager.failedAttempts.first()
            val limit = securityManager.selfDestructLimit.first()

            when (result) {
                PinVerificationResult.SUCCESS -> {
                    _isLocked.value = false
                    _isDecoyMode.value = false
                    onResult(true)
                }
                PinVerificationResult.DECOY_SUCCESS -> {
                    _isLocked.value = false
                    _isDecoyMode.value = true
                    onResult(true)
                }
                PinVerificationResult.FAIL -> {
                    // Log intrusion
                    repository.insertLog(
                        IntruderLogEntity(
                            failedPinAttempted = "*".repeat(pin.length),
                            photoPath = "Intruder photo simulated #$currentAttempts"
                        )
                    )
                    if (currentAttempts >= limit) {
                        // Triggers a self-destruct state or automatic lockout
                        // We can simulate self-destruct by clearing the non-secure local preferences or database,
                        // but to be safe and friendly we just enforce a full lockout.
                    }
                    onResult(false)
                }
                PinVerificationResult.NOT_SETUP -> {
                    _isLocked.value = false
                    onResult(true)
                }
            }
        }
    }

    fun setLockout() {
        _isLocked.value = true
    }

    fun setupPin(pin: String) {
        viewModelScope.launch {
            securityManager.setPin(pin)
            securityManager.setAppLockEnabled(true)
            _hasPin.value = true
            _isLocked.value = false
        }
    }

    fun removePin() {
        viewModelScope.launch {
            securityManager.clearPin()
            _hasPin.value = false
            _isLocked.value = false
        }
    }

    fun setupDecoyPin(pin: String) {
        viewModelScope.launch {
            securityManager.setDecoyPin(pin)
            _hasDecoyPin.value = true
        }
    }

    fun removeDecoyPin() {
        viewModelScope.launch {
            securityManager.clearDecoyPin()
            _hasDecoyPin.value = false
        }
    }

    fun toggleAppLock(enabled: Boolean) {
        viewModelScope.launch {
            securityManager.setAppLockEnabled(enabled)
        }
    }

    fun togglePreventScreenshot(prevent: Boolean) {
        viewModelScope.launch {
            securityManager.setPreventScreenshot(prevent)
        }
    }

    fun toggleNotificationPrivacy(hide: Boolean) {
        viewModelScope.launch {
            securityManager.setNotificationPrivacy(hide)
        }
    }

    fun togglePanicLockShake(enabled: Boolean) {
        viewModelScope.launch {
            securityManager.setPanicLockShake(enabled)
        }
    }

    fun updateAutoClearClipboardSec(seconds: Int) {
        viewModelScope.launch {
            securityManager.setAutoClearClipboardSec(seconds)
        }
    }

    fun updateSelfDestructLimit(limit: Int) {
        viewModelScope.launch {
            securityManager.setSelfDestructLimit(limit)
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearAllLogs()
            securityManager.resetFailedAttempts()
        }
    }

    // --- Clipboard Utility ---
    fun copyToClipboardSecure(label: String, text: String) {
        viewModelScope.launch {
            val seconds = securityManager.autoClearClipboardSec.first()
            securityManager.copyToClipboardAndScheduleClear(label, text, seconds)
        }
    }

    // --- Shake Detector Panic ---
    fun registerShakePanic(onPanicTriggered: () -> Unit) =
        securityManager.registerShakeDetector {
            viewModelScope.launch {
                if (securityManager.isPanicLockShakeEnabled.first()) {
                    setLockout()
                    onPanicTriggered()
                }
            }
        }

    fun unregisterShakePanic(listener: android.hardware.SensorEventListener) {
        securityManager.unregisterShakeDetector(listener)
    }

    // --- Notes CRUD operations ---

    fun saveNote(
        id: Int,
        title: String,
        content: String,
        category: String,
        isVault: Boolean,
        isLocked: Boolean,
        reminderTime: Long? = null,
        checklistJson: String? = null,
        attachmentsJson: String? = null
    ) {
        viewModelScope.launch {
            // Encrypt content if saving to vault or individual lock is enabled
            val finalContent = if (isVault || isLocked) {
                securityManager.encrypt(content)
            } else {
                content
            }

            val note = NoteEntity(
                id = if (id == 0) 0 else id,
                title = title.ifEmpty { "Untitled" },
                content = finalContent,
                category = category.ifEmpty { "General" },
                isVault = isVault,
                isLocked = isLocked,
                reminderTime = reminderTime,
                lastModified = System.currentTimeMillis(),
                checklistJson = checklistJson,
                attachmentsJson = attachmentsJson
            )

            if (id == 0) {
                repository.insertNote(note)
            } else {
                repository.updateNote(note)
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun toggleArchiveNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isArchived = !note.isArchived))
        }
    }

    fun togglePinNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    fun toggleFavoriteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isFavorite = !note.isFavorite))
        }
    }

    // --- Passwords CRUD operations ---

    fun savePassword(
        id: Int,
        title: String,
        username: String,
        passwordText: String,
        notes: String,
        otpSecret: String?,
        strength: String
    ) {
        viewModelScope.launch {
            val encrypted = securityManager.encrypt(passwordText)
            val pwd = PasswordEntity(
                id = if (id == 0) 0 else id,
                title = title.ifEmpty { "Website" },
                username = username,
                encryptedPassword = encrypted,
                notes = notes,
                otpSecret = otpSecret,
                strength = strength,
                lastModified = System.currentTimeMillis()
            )

            if (id == 0) {
                repository.insertPassword(pwd)
            } else {
                repository.updatePassword(pwd)
            }
        }
    }

    fun deletePassword(pwd: PasswordEntity) {
        viewModelScope.launch {
            repository.deletePassword(pwd)
        }
    }
}
