package com.example.moneymanager.data.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("money_manager_prefs", Context.MODE_PRIVATE)

    private val _useIndianGrouping = MutableStateFlow(
        prefs.getBoolean(KEY_INDIAN_GROUPING, true)
    )
    val useIndianGrouping: StateFlow<Boolean> = _useIndianGrouping

    fun setUseIndianGrouping(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_INDIAN_GROUPING, enabled).apply()
        _useIndianGrouping.value = enabled
    }

    private val _biometricEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    )
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        _biometricEnabled.value = enabled
    }

    companion object {
        private const val KEY_INDIAN_GROUPING = "use_indian_grouping"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }
}
