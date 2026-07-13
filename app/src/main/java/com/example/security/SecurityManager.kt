package com.example.security

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.math.sqrt

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_notes_settings")

class SecurityManager(private val context: Context) {

    private val appContext = context.applicationContext

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "SecureNotesCryptoKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_SEPARATOR = "]"

        // Pref Keys
        private val PIN_HASH_KEY = stringPreferencesKey("pin_hash")
        private val DECOY_PIN_HASH_KEY = stringPreferencesKey("decoy_pin_hash")
        private val APP_LOCK_ENABLED_KEY = booleanPreferencesKey("app_lock_enabled")
        private val LOCK_TIMEOUT_KEY = longPreferencesKey("lock_timeout_ms") // default 30s
        private val PREVENT_SCREENSHOT_KEY = booleanPreferencesKey("prevent_screenshot")
        private val NOTIFICATION_PRIVACY_KEY = booleanPreferencesKey("notification_privacy")
        private val FAILED_ATTEMPTS_KEY = intPreferencesKey("failed_attempts")
        private val PANIC_LOCK_SHAKE_KEY = booleanPreferencesKey("panic_lock_shake")
        private val AUTO_CLEAR_CLIPBOARD_SEC_KEY = intPreferencesKey("auto_clear_clipboard_sec") // default 30s
        private val SELF_DESTRUCT_LIMIT_KEY = intPreferencesKey("self_destruct_limit") // e.g. 10 failed -> wipes local database or locks out
    }

    // --- 1. Keystore Cryptography ---
    init {
        generateSecretKeyIfNeeded()
    }

    private fun generateSecretKeyIfNeeded() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                android.security.keystore.KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val spec = android.security.keystore.KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    fun encrypt(plainText: String?): String {
        if (plainText.isNullOrEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)
            val encryptedString = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            "$ivString$IV_SEPARATOR$encryptedString"
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun decrypt(encryptedText: String?): String {
        if (encryptedText.isNullOrEmpty()) return ""
        return try {
            val parts = encryptedText.split(IV_SEPARATOR)
            if (parts.size != 2) return ""
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val cipherBytes = Base64.decode(parts[1], Base64.NO_WRAP)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            val decryptedBytes = cipher.doFinal(cipherBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    // --- 2. Hashing (PIN & Decoy Vault) ---
    fun hashString(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }

    // --- 3. Secure Preferences (Flow & Suspends) ---
    val isAppLockEnabled: Flow<Boolean> = appContext.dataStore.data.map { it[APP_LOCK_ENABLED_KEY] ?: false }
    val lockTimeoutMs: Flow<Long> = appContext.dataStore.data.map { it[LOCK_TIMEOUT_KEY] ?: 30000L }
    val preventScreenshot: Flow<Boolean> = appContext.dataStore.data.map { it[PREVENT_SCREENSHOT_KEY] ?: false }
    val notificationPrivacy: Flow<Boolean> = appContext.dataStore.data.map { it[NOTIFICATION_PRIVACY_KEY] ?: false }
    val failedAttempts: Flow<Int> = appContext.dataStore.data.map { it[FAILED_ATTEMPTS_KEY] ?: 0 }
    val isPanicLockShakeEnabled: Flow<Boolean> = appContext.dataStore.data.map { it[PANIC_LOCK_SHAKE_KEY] ?: false }
    val autoClearClipboardSec: Flow<Int> = appContext.dataStore.data.map { it[AUTO_CLEAR_CLIPBOARD_SEC_KEY] ?: 30 }
    val selfDestructLimit: Flow<Int> = appContext.dataStore.data.map { it[SELF_DESTRUCT_LIMIT_KEY] ?: 5 }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        appContext.dataStore.edit { it[APP_LOCK_ENABLED_KEY] = enabled }
    }

    suspend fun setPin(pin: String) {
        val hash = hashString(pin)
        appContext.dataStore.edit { it[PIN_HASH_KEY] = hash }
    }

    suspend fun clearPin() {
        appContext.dataStore.edit {
            it.remove(PIN_HASH_KEY)
            it.remove(APP_LOCK_ENABLED_KEY)
        }
    }

    suspend fun verifyPin(pin: String): PinVerificationResult {
        val hash = hashString(pin)
        val savedHash = appContext.dataStore.data.map { it[PIN_HASH_KEY] }.first()
        val decoyHash = appContext.dataStore.data.map { it[DECOY_PIN_HASH_KEY] }.first()

        return when {
            savedHash == null -> PinVerificationResult.NOT_SETUP
            hash == savedHash -> {
                resetFailedAttempts()
                PinVerificationResult.SUCCESS
            }
            decoyHash != null && hash == decoyHash -> {
                resetFailedAttempts()
                PinVerificationResult.DECOY_SUCCESS
            }
            else -> {
                incrementFailedAttempts()
                PinVerificationResult.FAIL
            }
        }
    }

    suspend fun hasPinSetup(): Boolean {
        return appContext.dataStore.data.map { it[PIN_HASH_KEY] != null }.first()
    }

    suspend fun setDecoyPin(pin: String) {
        val hash = hashString(pin)
        appContext.dataStore.edit { it[DECOY_PIN_HASH_KEY] = hash }
    }

    suspend fun clearDecoyPin() {
        appContext.dataStore.edit { it.remove(DECOY_PIN_HASH_KEY) }
    }

    suspend fun hasDecoyPinSetup(): Boolean {
        return appContext.dataStore.data.map { it[DECOY_PIN_HASH_KEY] != null }.first()
    }

    suspend fun setLockTimeout(ms: Long) {
        appContext.dataStore.edit { it[LOCK_TIMEOUT_KEY] = ms }
    }

    suspend fun setPreventScreenshot(prevent: Boolean) {
        appContext.dataStore.edit { it[PREVENT_SCREENSHOT_KEY] = prevent }
    }

    suspend fun setNotificationPrivacy(hide: Boolean) {
        appContext.dataStore.edit { it[NOTIFICATION_PRIVACY_KEY] = hide }
    }

    suspend fun setPanicLockShake(enabled: Boolean) {
        appContext.dataStore.edit { it[PANIC_LOCK_SHAKE_KEY] = enabled }
    }

    suspend fun setAutoClearClipboardSec(seconds: Int) {
        appContext.dataStore.edit { it[AUTO_CLEAR_CLIPBOARD_SEC_KEY] = seconds }
    }

    suspend fun setSelfDestructLimit(limit: Int) {
        appContext.dataStore.edit { it[SELF_DESTRUCT_LIMIT_KEY] = limit }
    }

    private suspend fun incrementFailedAttempts() {
        appContext.dataStore.edit {
            val current = it[FAILED_ATTEMPTS_KEY] ?: 0
            it[FAILED_ATTEMPTS_KEY] = current + 1
        }
    }

    suspend fun resetFailedAttempts() {
        appContext.dataStore.edit { it[FAILED_ATTEMPTS_KEY] = 0 }
    }

    // --- 4. Auto-Clear Clipboard ---
    fun copyToClipboardAndScheduleClear(label: String, text: String, seconds: Int) {
        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)

        if (seconds > 0) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(seconds * 1000L)
                // Double check if it's still the same sensitive text to clear
                val currentClip = clipboard.primaryClip
                if (currentClip != null && currentClip.itemCount > 0) {
                    val currentText = currentClip.getItemAt(0).text?.toString()
                    if (currentText == text) {
                        clipboard.setPrimaryClip(ClipData.newPlainText("Cleared", ""))
                    }
                }
            }
        }
    }

    // --- 5. Shake Gesture Detector (Panic Lock) ---
    fun registerShakeDetector(onShake: () -> Unit): SensorEventListener? {
        val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return null
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return null

        val listener = object : SensorEventListener {
            private var lastUpdate: Long = 0
            private var lastX = 0f
            private var lastY = 0f
            private var lastZ = 0f
            private val shakeThreshold = 800 // acceleration threshold

            override fun onSensorChanged(event: SensorEvent) {
                val curTime = System.currentTimeMillis()
                // Only allow one shake check per 100ms
                if ((curTime - lastUpdate) > 100) {
                    val diffTime = curTime - lastUpdate
                    lastUpdate = curTime

                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val speed = sqrt((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY) + (z - lastZ) * (z - lastZ)) / diffTime * 10000

                    if (speed > shakeThreshold) {
                        onShake()
                    }

                    lastX = x
                    lastY = y
                    lastZ = z
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        return listener
    }

    fun unregisterShakeDetector(listener: SensorEventListener) {
        val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        sensorManager?.unregisterListener(listener)
    }
}

enum class PinVerificationResult {
    SUCCESS,
    DECOY_SUCCESS,
    FAIL,
    NOT_SETUP
}
