package com.kosio.app.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kosio.app.R
import com.kosio.app.core.components.KosioCard
import com.kosio.app.core.components.KosioConfirmDialog
import com.kosio.app.core.components.KosioPrimaryButton
import com.kosio.app.core.components.KosioSecondaryButton
import com.kosio.app.core.components.KosioTopAppBar
import com.kosio.app.core.components.WarningCard
import com.kosio.app.core.designsystem.BorderSubtle
import com.kosio.app.core.designsystem.CharcoalText
import com.kosio.app.core.designsystem.CharcoalTextMuted
import com.kosio.app.core.designsystem.CharcoalTextSecondary
import com.kosio.app.core.designsystem.DeepIndigo
import com.kosio.app.core.designsystem.ErrorRed
import com.kosio.app.core.designsystem.LightAmber
import com.kosio.app.core.designsystem.LightIndigo
import com.kosio.app.core.designsystem.LightRed
import com.kosio.app.core.designsystem.LightTeal
import com.kosio.app.core.designsystem.PureWhiteSurface
import com.kosio.app.core.designsystem.SuccessTeal
import com.kosio.app.core.designsystem.WarmIvoryBackground
import com.kosio.app.data.backup.BackupManager
import com.kosio.app.data.backup.RestoreValidationResult
import com.kosio.app.data.local.KosioDatabase
import com.kosio.app.data.preferences.UserPreferencesRepository
import com.kosio.app.domain.model.AppLanguage
import com.kosio.app.domain.model.CurrencyCode
import com.kosio.app.domain.model.RoundingRule
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val database: KosioDatabase,
    private val backupManager: BackupManager
) : ViewModel() {

    val preferences = preferencesRepository.userPreferencesFlow

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(language)
            val localeList = when (language) {
                AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
                AppLanguage.INDONESIAN -> LocaleListCompat.forLanguageTags("in")
                AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en")
            }
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }

    fun setCurrency(currency: CurrencyCode) {
        viewModelScope.launch {
            preferencesRepository.setCurrency(currency)
        }
    }

    fun setRoundingRule(rule: RoundingRule) {
        viewModelScope.launch {
            preferencesRepository.setRoundingRule(rule)
        }
    }

    fun resetAllData(onDone: () -> Unit) {
        viewModelScope.launch {
            database.runInTransaction {
                database.openHelper.writableDatabase.execSQL("DELETE FROM calculation_snapshots")
                database.openHelper.writableDatabase.execSQL("DELETE FROM product_ingredients")
                database.openHelper.writableDatabase.execSQL("DELETE FROM products")
                database.openHelper.writableDatabase.execSQL("DELETE FROM ingredient_prices")
                database.openHelper.writableDatabase.execSQL("DELETE FROM ingredients")
            }
            onDone()
        }
    }

    suspend fun exportBackup(outputStream: OutputStream) {
        backupManager.exportBackup(outputStream)
    }

    suspend fun restoreBackup(inputStream: InputStream): RestoreValidationResult {
        return backupManager.restoreBackup(inputStream)
    }
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToBackupRestore: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val userPrefs by viewModel.preferences.collectAsState(initial = null)

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showRoundingDialog by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            KosioTopAppBar(
                title = stringResource(R.string.settings_title),
                subtitle = stringResource(R.string.nav_settings)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = WarmIvoryBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Preferences
            KosioCard {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Preferensi Umum",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    SettingsRowItem(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.settings_language),
                        subtitle = when (userPrefs?.language) {
                            AppLanguage.INDONESIAN -> stringResource(R.string.settings_language_id)
                            AppLanguage.ENGLISH -> stringResource(R.string.settings_language_en)
                            else -> stringResource(R.string.settings_language_system)
                        },
                        onClick = { showLanguageDialog = true }
                    )

                    HorizontalDivider(color = BorderSubtle)

                    SettingsRowItem(
                        icon = Icons.Default.AttachMoney,
                        title = stringResource(R.string.settings_currency),
                        subtitle = when (userPrefs?.currency) {
                            CurrencyCode.USD -> "USD ($)"
                            else -> "IDR (Rp)"
                        },
                        onClick = { showCurrencyDialog = true }
                    )

                    HorizontalDivider(color = BorderSubtle)

                    SettingsRowItem(
                        icon = Icons.Default.FormatListNumbered,
                        title = stringResource(R.string.settings_rounding),
                        subtitle = when (userPrefs?.roundingRule) {
                            RoundingRule.UP_100 -> stringResource(R.string.rounding_up_100)
                            RoundingRule.UP_500 -> stringResource(R.string.rounding_up_500)
                            RoundingRule.UP_1000 -> stringResource(R.string.rounding_up_1000)
                            else -> stringResource(R.string.rounding_none)
                        },
                        onClick = { showRoundingDialog = true }
                    )
                }
            }

            // Data & Storage Card
            KosioCard {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Penyimpanan & Cadangan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    SettingsRowItem(
                        icon = Icons.Default.Backup,
                        title = stringResource(R.string.settings_backup_restore),
                        subtitle = stringResource(R.string.settings_backup_desc),
                        onClick = onNavigateToBackupRestore
                    )

                    HorizontalDivider(color = BorderSubtle)

                    SettingsRowItem(
                        icon = Icons.Default.DeleteForever,
                        iconTint = ErrorRed,
                        title = stringResource(R.string.settings_reset_data),
                        subtitle = stringResource(R.string.settings_reset_desc),
                        onClick = { showResetConfirm = true }
                    )
                }
            }

            // About & Info Card
            KosioCard {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Tentang Aplikasi",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    SettingsRowItem(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.settings_about),
                        subtitle = stringResource(R.string.version_format, com.kosio.app.BuildConfig.VERSION_NAME),
                        onClick = onNavigateToAbout
                    )
                }
            }
        }
    }

    // Language Picker Dialog
    if (showLanguageDialog) {
        KosioConfirmDialog(
            title = stringResource(R.string.settings_language),
            message = "Pilih bahasa tampilan antarmuka:",
            confirmText = "Tutup",
            onConfirm = { showLanguageDialog = false },
            onDismiss = { showLanguageDialog = false }
        )
    }

    // Reset Data Dialog
    if (showResetConfirm) {
        KosioConfirmDialog(
            title = stringResource(R.string.dialog_confirm_reset_title),
            message = stringResource(R.string.dialog_confirm_reset_message),
            confirmText = stringResource(R.string.action_reset),
            isDestructive = true,
            onConfirm = {
                showResetConfirm = false
                viewModel.resetAllData {
                    scope.launch { snackbarHostState.showSnackbar("Semua data berhasil dibersihkan") }
                }
            },
            onDismiss = { showResetConfirm = false }
        )
    }
}

@Composable
fun BackupRestoreScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isLoading by remember { mutableStateOf(false) }

    // Export launcher via Storage Access Framework
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            isLoading = true
            scope.launch {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        viewModel.exportBackup(output)
                    }
                    snackbarHostState.showSnackbar(context.getString(R.string.backup_success))
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(context.getString(R.string.backup_failed))
                } finally {
                    isLoading = false
                }
            }
        }
    }

    // Restore launcher via Storage Access Framework
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isLoading = true
            scope.launch {
                try {
                    val result = context.contentResolver.openInputStream(uri)?.use { input ->
                        viewModel.restoreBackup(input)
                    }
                    if (result?.isValid == true) {
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.restore_success, result.ingredientCount, result.productCount)
                        )
                    } else {
                        snackbarHostState.showSnackbar(context.getString(R.string.restore_failed))
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(context.getString(R.string.restore_failed))
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            KosioTopAppBar(
                title = stringResource(R.string.backup_restore_title),
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = WarmIvoryBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Backup Card
            KosioCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(LightTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = SuccessTeal)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.action_backup),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                        )
                    }

                    Text(
                        text = stringResource(R.string.backup_export_desc),
                        style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalTextSecondary)
                    )

                    KosioPrimaryButton(
                        text = stringResource(R.string.backup_export_btn),
                        icon = Icons.Default.FileUpload,
                        onClick = {
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                            exportLauncher.launch("kosio_backup_$timeStamp.json")
                        }
                    )
                }
            }

            // Restore Card
            KosioCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(LightIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, tint = DeepIndigo)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.action_restore),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                        )
                    }

                    Text(
                        text = stringResource(R.string.restore_import_desc),
                        style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalTextSecondary)
                    )

                    WarningCard(
                        message = stringResource(R.string.restore_warning)
                    )

                    KosioSecondaryButton(
                        text = stringResource(R.string.restore_import_btn),
                        icon = Icons.Default.Restore,
                        onClick = {
                            restoreLauncher.launch(arrayOf("application/json", "*/*"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            KosioTopAppBar(
                title = stringResource(R.string.settings_about),
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onNavigateBack
            )
        },
        containerColor = WarmIvoryBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(DeepIndigo),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "K",
                    color = PureWhiteSurface,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 42.sp
                    )
                )
            }

            Text(
                text = "Kosio",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )
            )

            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalTextSecondary)
            )

            Text(
                text = stringResource(R.string.version_format, com.kosio.app.BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelMedium.copy(color = CharcoalTextMuted)
            )

            Spacer(modifier = Modifier.height(16.dp))

            KosioCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Prinsip Privasi & Keamanan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )
                    Text(
                        text = "Kosio adalah aplikasi offline 100%. Data resep, harga bahan, dan asumsi bisnis Anda disimpan secara lokal di perangkat Anda sendiri tanpa dikirim ke server manapun.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalTextSecondary)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconTint: Color = DeepIndigo
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalText
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
            )
        }
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = CharcoalTextMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}
