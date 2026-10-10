package com.kosio.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kosio.app.R
import com.kosio.app.core.components.KosioCard
import com.kosio.app.core.components.KosioPrimaryButton
import com.kosio.app.core.designsystem.CharcoalText
import com.kosio.app.core.designsystem.CharcoalTextMuted
import com.kosio.app.core.designsystem.CharcoalTextSecondary
import com.kosio.app.core.designsystem.DeepIndigo
import com.kosio.app.core.designsystem.LightIndigo
import com.kosio.app.core.designsystem.LightTeal
import com.kosio.app.core.designsystem.PrimaryIndigo
import com.kosio.app.core.designsystem.PureWhiteSurface
import com.kosio.app.core.designsystem.SuccessTeal
import com.kosio.app.core.designsystem.WarmIvoryBackground
import com.kosio.app.data.preferences.UserPreferencesRepository
import com.kosio.app.domain.model.AppLanguage
import com.kosio.app.domain.model.CurrencyCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedLanguage = MutableStateFlow(AppLanguage.SYSTEM)
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    private val _selectedCurrency = MutableStateFlow(CurrencyCode.IDR)
    val selectedCurrency: StateFlow<CurrencyCode> = _selectedCurrency.asStateFlow()

    fun selectLanguage(language: AppLanguage) {
        _selectedLanguage.value = language
    }

    fun selectCurrency(currency: CurrencyCode) {
        _selectedCurrency.value = currency
    }

    fun completeOnboarding(onCompleted: () -> Unit) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(_selectedLanguage.value)
            preferencesRepository.setCurrency(_selectedCurrency.value)
            preferencesRepository.setOnboardingCompleted(true)
            onCompleted()
        }
    }
}

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit
) {
    val selectedLang by viewModel.selectedLanguage.collectAsState()
    val selectedCurr by viewModel.selectedCurrency.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmIvoryBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(32.dp))

                // App Brand Badge
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(DeepIndigo),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "K",
                        color = PureWhiteSurface,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.onboarding_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.onboarding_subtitle),
                    style = MaterialTheme.typography.bodyLarge.copy(color = CharcoalTextSecondary),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Value points
                OnboardingFeatureItem(
                    icon = Icons.Default.Calculate,
                    title = stringResource(R.string.hpp_results_title),
                    description = stringResource(R.string.batch_breakdown)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OnboardingFeatureItem(
                    icon = Icons.Default.TrendingUp,
                    title = stringResource(R.string.pricing_simulation_title),
                    description = stringResource(R.string.margin_percent) + " & " + stringResource(R.string.marketplace_fee)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OnboardingFeatureItem(
                    icon = Icons.Default.Lock,
                    title = stringResource(R.string.onboarding_offline_notice),
                    description = ""
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Language selection
                Text(
                    text = stringResource(R.string.onboarding_select_language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LanguageChip(
                        title = stringResource(R.string.settings_language_system),
                        isSelected = selectedLang == AppLanguage.SYSTEM,
                        onClick = { viewModel.selectLanguage(AppLanguage.SYSTEM) },
                        modifier = Modifier.weight(1f)
                    )
                    LanguageChip(
                        title = "Indonesia",
                        isSelected = selectedLang == AppLanguage.INDONESIAN,
                        onClick = { viewModel.selectLanguage(AppLanguage.INDONESIAN) },
                        modifier = Modifier.weight(1f)
                    )
                    LanguageChip(
                        title = "English",
                        isSelected = selectedLang == AppLanguage.ENGLISH,
                        onClick = { viewModel.selectLanguage(AppLanguage.ENGLISH) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Currency selection
                Text(
                    text = stringResource(R.string.onboarding_select_currency),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LanguageChip(
                        title = "IDR (Rp)",
                        isSelected = selectedCurr == CurrencyCode.IDR,
                        onClick = { viewModel.selectCurrency(CurrencyCode.IDR) },
                        modifier = Modifier.weight(1f)
                    )
                    LanguageChip(
                        title = "USD ($)",
                        isSelected = selectedCurr == CurrencyCode.USD,
                        onClick = { viewModel.selectCurrency(CurrencyCode.USD) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 16.dp)
            ) {
                KosioPrimaryButton(
                    text = stringResource(R.string.action_get_started),
                    onClick = { viewModel.completeOnboarding(onFinish) }
                )
            }
        }
    }
}

@Composable
private fun OnboardingFeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    KosioCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LightTeal),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SuccessTeal,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalText
                    )
                )
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) DeepIndigo else PureWhiteSurface)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) PureWhiteSurface else CharcoalText
            ),
            textAlign = TextAlign.Center
        )
    }
}
