package com.example.ui.security

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppSecurityManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("vault_security_prefs", Context.MODE_PRIVATE)

    private val _isLocked = MutableStateFlow(prefs.getBoolean(KEY_IS_PIN_ENABLED, false))
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _isPinEnabled = MutableStateFlow(prefs.getBoolean(KEY_IS_PIN_ENABLED, false))
    val isPinEnabled: StateFlow<Boolean> = _isPinEnabled.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(prefs.getBoolean(KEY_IS_BIOMETRIC_ENABLED, true))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _isPrivacyMode = MutableStateFlow(prefs.getBoolean(KEY_IS_PRIVACY_MODE, false))
    val isPrivacyMode: StateFlow<Boolean> = _isPrivacyMode.asStateFlow()

    private val _storedPin = MutableStateFlow(prefs.getString(KEY_PIN_HASH, "1234") ?: "1234")

    fun unlockWithPin(pin: String): Boolean {
        return if (pin == _storedPin.value) {
            _isLocked.value = false
            true
        } else {
            false
        }
    }

    fun unlockWithBiometrics() {
        _isLocked.value = false
    }

    fun lockApp() {
        if (_isPinEnabled.value) {
            _isLocked.value = true
        }
    }

    fun togglePinSecurity(enabled: Boolean, newPin: String = "1234") {
        prefs.edit()
            .putBoolean(KEY_IS_PIN_ENABLED, enabled)
            .putString(KEY_PIN_HASH, newPin)
            .apply()
        _isPinEnabled.value = enabled
        _storedPin.value = newPin
        if (!enabled) {
            _isLocked.value = false
        }
    }

    fun toggleBiometrics(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IS_BIOMETRIC_ENABLED, enabled).apply()
        _isBiometricEnabled.value = enabled
    }

    fun togglePrivacyMode() {
        val newVal = !_isPrivacyMode.value
        prefs.edit().putBoolean(KEY_IS_PRIVACY_MODE, newVal).apply()
        _isPrivacyMode.value = newVal
    }

    companion object {
        private const val KEY_IS_PIN_ENABLED = "key_is_pin_enabled"
        private const val KEY_IS_BIOMETRIC_ENABLED = "key_is_biometric_enabled"
        private const val KEY_IS_PRIVACY_MODE = "key_is_privacy_mode"
        private const val KEY_PIN_HASH = "key_pin_hash"
    }
}
