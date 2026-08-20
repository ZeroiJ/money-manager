package com.example.moneymanager

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.moneymanager.data.prefs.UserPreferences
import com.example.moneymanager.theme.MoneyManagerTheme
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
      MoneyManagerTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          MainNavigation(
            presetCategory = presetCategory,
            presetPaymentMode = presetPaymentMode
          )
        }
      }
    }
  }
}
