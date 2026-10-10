package com.kosio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.navigation.compose.rememberNavController
import com.kosio.app.core.designsystem.KosioTheme
import com.kosio.app.domain.model.AppLanguage
import com.kosio.app.ui.navigation.KosioAppNavigation

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as KosioApplication

        setContent {
            val userPrefs by app.userPreferencesRepository.userPreferencesFlow.collectAsState(initial = null)

            // Sync per-app locale
            userPrefs?.let { prefs ->
                val targetLocale = when (prefs.language) {
                    AppLanguage.INDONESIAN -> LocaleListCompat.forLanguageTags("in")
                    AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en")
                    AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
                }
                if (AppCompatDelegate.getApplicationLocales() != targetLocale) {
                    AppCompatDelegate.setApplicationLocales(targetLocale)
                }
            }

            KosioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val onboardingCompleted = userPrefs?.isOnboardingCompleted ?: false

                    if (userPrefs != null) {
                        KosioAppNavigation(
                            navController = navController,
                            isOnboardingCompleted = onboardingCompleted,
                            app = app
                        )
                    }
                }
            }
        }
    }
}
