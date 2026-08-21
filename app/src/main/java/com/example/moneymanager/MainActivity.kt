package com.example.moneymanager

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.moneymanager.data.prefs.UserPreferences
import com.example.moneymanager.theme.Chroma
import com.example.moneymanager.ui.navigation.MainNavigation
import com.example.moneymanager.util.BiometricHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
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
      androidx.compose.material3.MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
          primary = Chroma.color.primary,
          onPrimary = Chroma.color.onPrimary,
          primaryContainer = Chroma.color.primaryContainer,
          onPrimaryContainer = Chroma.color.onPrimaryContainer,
          secondary = Chroma.color.secondary,
          onSecondary = Chroma.color.onSecondary,
          secondaryContainer = Chroma.color.secondaryContainer,
          onSecondaryContainer = Chroma.color.onSecondaryContainer,
          tertiary = Chroma.color.tertiary,
          onTertiary = Chroma.color.onTertiary,
          background = Chroma.color.background,
          onBackground = Chroma.color.onBackground,
          surface = Chroma.color.surface,
          onSurface = Chroma.color.onSurface,
          surfaceVariant = Chroma.color.surfaceVariant,
          onSurfaceVariant = Chroma.color.onSurfaceVariant,
          outline = Chroma.color.outline,
          error = Chroma.color.error,
          onError = Chroma.color.onError
        )
      ) {
        MainNavigation(
          presetCategory = presetCategory,
          presetPaymentMode = presetPaymentMode
        )
      }
    }
  }
}
