package com.kosio.app.ui.ingredients

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.kosio.app.R
import com.kosio.app.core.components.EmptyStateCard
import com.kosio.app.core.components.KosioCard
import com.kosio.app.core.components.KosioNumberField
import com.kosio.app.core.components.KosioPrimaryButton
import com.kosio.app.core.components.KosioSecondaryButton
import com.kosio.app.core.components.KosioTextField
import com.kosio.app.core.components.KosioTopAppBar
import com.kosio.app.core.components.WarningCard
import com.kosio.app.core.designsystem.BorderSubtle
import com.kosio.app.core.designsystem.CharcoalText
import com.kosio.app.core.designsystem.CharcoalTextMuted
import com.kosio.app.core.designsystem.CharcoalTextSecondary
import com.kosio.app.core.designsystem.DeepIndigo
import com.kosio.app.core.designsystem.ErrorRed
import com.kosio.app.core.designsystem.LightIndigo
import com.kosio.app.core.designsystem.LightTeal
import com.kosio.app.core.designsystem.PureWhiteSurface
import com.kosio.app.core.designsystem.SuccessTeal
import com.kosio.app.core.designsystem.WarmIvoryBackground
import com.kosio.app.data.ocr.ReceiptOcrScanner
import com.kosio.app.domain.model.CurrencyCode
import com.kosio.app.domain.model.Ingredient
import com.kosio.app.domain.model.UnitDimension
import com.kosio.app.domain.ocr.ParsedReceiptResult
import com.kosio.app.domain.ocr.ScannedReceiptItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScanReviewScreen(
    viewModel: IngredientViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val userPrefs by viewModel.currency.collectAsState()
    val currency = userPrefs?.currency ?: CurrencyCode.IDR
    val existingIngredients by viewModel.ingredientsList.collectAsState()

    var isScanning by remember { mutableStateOf(false) }
    var parsedResult by remember { mutableStateOf<ParsedReceiptResult?>(null) }
    val scannedItems = remember { mutableStateListOf<ScannedReceiptItem>() }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val ocrQuotaManager = com.kosio.app.KosioApplication.instance.ocrQuotaManager
    val quotaState by ocrQuotaManager.quotaState.collectAsState()
    var showQuotaExhaustedDialog by remember { mutableStateOf(false) }

    val processImageUri = { uri: Uri ->
        if (!ocrQuotaManager.hasAvailableQuota()) {
            showQuotaExhaustedDialog = true
        } else {
            ocrQuotaManager.consumeQuota()
            scope.launch {
                isScanning = true
                try {
                    val result = withContext(Dispatchers.IO) {
                        ReceiptOcrScanner.scanReceiptFromUri(context, uri)
                    }
                    parsedResult = result
                    scannedItems.clear()
                    scannedItems.addAll(result.items.map { item ->
                        // Match with existing ingredient by name similarity
                        val matched = existingIngredients.firstOrNull {
                            it.name.equals(item.name, ignoreCase = true) ||
                                    it.name.contains(item.name, ignoreCase = true) ||
                                    item.name.contains(it.name, ignoreCase = true)
                        }
                        if (matched != null) {
                            item.copy(
                                matchedIngredientId = matched.id,
                                unitDimension = matched.unitDimension,
                                purchaseUnit = matched.purchaseUnit,
                                baseUnit = matched.baseUnit
                            )
                        } else {
                            item
                        }
                    })

                    if (result.items.isEmpty()) {
                        snackbarHostState.showSnackbar("Tidak menemukan rincian barang belanja pada gambar.")
                    } else {
                        snackbarHostState.showSnackbar("Berhasil mendeteksi ${result.items.size} bahan belanjaan.")
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Gagal memproses gambar: ${e.message}")
                } finally {
                    isScanning = false
                }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            processImageUri(uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            processImageUri(tempCameraUri!!)
        }
    }

    if (showQuotaExhaustedDialog) {
        com.kosio.app.ui.ads.OcrQuotaExhaustedDialog(
            quotaState = quotaState,
            onDismiss = { showQuotaExhaustedDialog = false },
            onRewardEarned = {
                ocrQuotaManager.addBonusQuota(3)
                showQuotaExhaustedDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            KosioTopAppBar(
                title = "Scan Struk Belanja (OCR)",
                subtitle = "Ekstrak otomatis harga beli bahan",
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onNavigateBack,
                actions = {
                    com.kosio.app.ui.ads.OcrQuotaBadge(
                        totalAvailable = quotaState.totalAvailable,
                        onClick = {
                            if (quotaState.totalAvailable == 0) {
                                showQuotaExhaustedDialog = true
                            }
                        }
                    )
                }
            )
        },
        bottomBar = {
            if (scannedItems.isNotEmpty()) {
                val selectedCount = scannedItems.count { it.isSelected }
                androidx.compose.material3.Surface(
                    shadowElevation = 8.dp,
                    color = PureWhiteSurface
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        KosioPrimaryButton(
                            text = "Simpan ke Master Bahan ($selectedCount item)",
                            icon = Icons.Default.Check,
                            enabled = selectedCount > 0,
                            onClick = {
                                val selected = scannedItems.filter { it.isSelected }
                                scope.launch {
                                    selected.forEach { item ->
                                        viewModel.saveIngredient(
                                            id = item.matchedIngredientId,
                                            name = item.name,
                                            category = item.category ?: "Hasil Scan Struk",
                                            dimension = item.unitDimension,
                                            purchasePrice = item.purchasePrice,
                                            purchaseQuantity = item.purchaseQuantity,
                                            purchaseUnit = item.purchaseUnit,
                                            baseUnit = item.baseUnit,
                                            customPackQty = null,
                                            wastePercent = item.wastePercent,
                                            note = "Diimpor dari Scan Struk ${parsedResult?.merchantName ?: ""}",
                                            onSuccess = {}
                                        )
                                    }
                                    snackbarHostState.showSnackbar("$selectedCount bahan berhasil disimpan/diperbarui!")
                                    onNavigateBack()
                                }
                            }
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = WarmIvoryBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Action Banner: Ambil Foto / Pilih Galeri
            KosioCard(modifier = Modifier.padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Foto Struk Belanja Pasar / Minimarket",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )
                    Text(
                        text = "Sistem OCR on-device akan otomatis membaca nama bahan dan harga beli tanpa koneksi internet.",
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KosioPrimaryButton(
                            text = "Kamera",
                            icon = Icons.Default.CameraAlt,
                            onClick = {
                                val photoFile = File(context.cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    photoFile
                                )
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        KosioSecondaryButton(
                            text = "Galeri",
                            icon = Icons.Default.Image,
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (isScanning) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = DeepIndigo)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Memindai & mengekstrak data struk...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalTextSecondary)
                        )
                    }
                }
            } else if (scannedItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            tint = DeepIndigo.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum ada struk yang dipindai.",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CharcoalText)
                        )
                        Text(
                            text = "Klik tombol Kamera atau Galeri di atas untuk mulai memindai struk belanjaan Anda.",
                            style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!parsedResult?.merchantName.isNullOrBlank()) {
                        item {
                            Text(
                                text = "Toko: ${parsedResult?.merchantName}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepIndigo
                                )
                            )
                        }
                    }

                    itemsIndexed(scannedItems, key = { index, item -> item.id }) { index, item ->
                        ScannedReceiptItemRow(
                            item = item,
                            currency = currency,
                            onUpdate = { updated -> scannedItems[index] = updated },
                            onDelete = { scannedItems.removeAt(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannedReceiptItemRow(
    item: ScannedReceiptItem,
    currency: CurrencyCode,
    onUpdate: (ScannedReceiptItem) -> Unit,
    onDelete: () -> Unit
) {
    KosioCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isSelected,
                    onCheckedChange = { onUpdate(item.copy(isSelected = it)) },
                    colors = CheckboxDefaults.colors(checkedColor = DeepIndigo)
                )

                Column(modifier = Modifier.weight(1f)) {
                    if (item.matchedIngredientId != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LightTeal)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🔄 Perbarui Harga Lama",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessTeal
                                )
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LightIndigo)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "✨ Bahan Baru",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepIndigo
                                )
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = ErrorRed)
                }
            }

            KosioTextField(
                value = item.name,
                onValueChange = { onUpdate(item.copy(name = it)) },
                label = "Nama Bahan"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KosioNumberField(
                    value = item.purchasePrice.stripTrailingZeros().toPlainString(),
                    onValueChange = {
                        val p = try { BigDecimal(it.ifEmpty { "0" }) } catch (e: Exception) { BigDecimal.ZERO }
                        onUpdate(item.copy(purchasePrice = p))
                    },
                    label = "Harga Beli",
                    prefix = currency.symbol + " ",
                    modifier = Modifier.weight(1.3f)
                )

                KosioNumberField(
                    value = item.purchaseQuantity.stripTrailingZeros().toPlainString(),
                    onValueChange = {
                        val q = try { BigDecimal(it.ifEmpty { "1" }) } catch (e: Exception) { BigDecimal.ONE }
                        onUpdate(item.copy(purchaseQuantity = q))
                    },
                    label = "Jumlah",
                    modifier = Modifier.weight(0.8f)
                )

                KosioTextField(
                    value = item.purchaseUnit,
                    onValueChange = { onUpdate(item.copy(purchaseUnit = it)) },
                    label = "Satuan",
                    modifier = Modifier.weight(0.8f)
                )
            }
        }
    }
}
