package com.kosio.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kosio.app.domain.model.AppLanguage
import com.kosio.app.domain.model.CurrencyCode
import com.kosio.app.domain.model.RoundingRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kosio_settings")

data class UserPreferences(
    val language: AppLanguage,
    val currency: CurrencyCode,
    val roundingRule: RoundingRule,
    val isOnboardingCompleted: Boolean
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val LANGUAGE = stringPreferencesKey("app_language")
        val CURRENCY = stringPreferencesKey("app_currency")
        val ROUNDING_RULE = stringPreferencesKey("rounding_rule")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            val langStr = preferences[PreferencesKeys.LANGUAGE] ?: AppLanguage.SYSTEM.code
            val currStr = preferences[PreferencesKeys.CURRENCY] ?: CurrencyCode.IDR.code
            val roundStr = preferences[PreferencesKeys.ROUNDING_RULE] ?: RoundingRule.NONE.code
            val onboarding = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false

            UserPreferences(
                language = AppLanguage.fromCode(langStr),
                currency = CurrencyCode.fromCode(currStr),
                roundingRule = RoundingRule.fromCode(roundStr),
                isOnboardingCompleted = onboarding
            )
        }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language.code
        }
    }

    suspend fun setCurrency(currency: CurrencyCode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY] = currency.code
        }
    }

    suspend fun setRoundingRule(rule: RoundingRule) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ROUNDING_RULE] = rule.code
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
