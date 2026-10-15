package com.example.moneymanager

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.moneymanager.data.prefs.UserPreferences
import com.example.moneymanager.theme.Ios
import com.example.moneymanager.ui.navigation.MainNavigation
import com.example.moneymanager.util.BiometricHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    // Light-mode only: force dark status/nav-bar icons so they stay visible on
    // the Sage canvas even when the system is in dark mode (v1.17.0).
    val transparent = android.graphics.Color.TRANSPARENT
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.light(transparent, transparent),
      navigationBarStyle = SystemBarStyle.light(transparent, transparent)
    )
    super.onCreate(savedInstanceState)

    lifecycleScope.launch {
      val prefs = UserPreferences(applicationContext)
      val biometricEnabled = prefs.biometricEnabled.first()

      if (biometricEnabled && BiometricHelper.isBiometricAvailable(applicationContext)) {
        BiometricHelper.showBiometricPrompt(
          activity = this@MainActivity,
          onSuccess = { setupContent() },
          onError = { finish() }
        )
      } else {
        setupContent()
      }
    }
  }

  private fun setupContent() {
    val presetCategory = intent?.extras?.getString("preset_category")
    val presetPaymentMode = intent?.extras?.getString("preset_payment_mode")

    setContent {
      // Light-mode only: the Sage/Paper palette is the app's identity and the
      // Obsidian dark variant is retired from runtime selection (v1.17.0).
      val ios = Ios.Light
      val scheme = lightColorScheme(
        primary = ios.accent,
        onPrimary = Color.White,
        background = ios.background,
        onBackground = ios.label,
        surface = ios.card,
        onSurface = ios.label,
        surfaceVariant = ios.secondaryFill,
        onSurfaceVariant = ios.secondaryLabel,
        outline = ios.separator,
        error = ios.destructive,
        onError = Color.White
      )
      MaterialTheme(colorScheme = scheme) {
        MainNavigation(
          presetCategory = presetCategory,
          presetPaymentMode = presetPaymentMode
        )
      }
    }
  }
}
