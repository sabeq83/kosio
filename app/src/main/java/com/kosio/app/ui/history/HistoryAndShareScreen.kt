package com.kosio.app.ui.history

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kosio.app.R
import com.kosio.app.core.components.EmptyStateCard
import com.kosio.app.core.components.KosioCard
import com.kosio.app.core.components.KosioConfirmDialog
import com.kosio.app.core.components.KosioPrimaryButton
import com.kosio.app.core.components.KosioSecondaryButton
import com.kosio.app.core.components.KosioTopAppBar
import com.kosio.app.core.designsystem.BorderSubtle
import com.kosio.app.core.designsystem.CharcoalText
import com.kosio.app.core.designsystem.CharcoalTextMuted
import com.kosio.app.core.designsystem.CharcoalTextSecondary
import com.kosio.app.core.designsystem.DeepIndigo
import com.kosio.app.core.designsystem.ErrorRed
import com.kosio.app.core.designsystem.PureWhiteSurface
import com.kosio.app.core.designsystem.SuccessTeal
import com.kosio.app.core.designsystem.WarmIvoryBackground
import com.kosio.app.data.preferences.UserPreferencesRepository
import com.kosio.app.domain.calculation.HppCalculator
import com.kosio.app.domain.calculation.HppResult
import com.kosio.app.domain.model.CalculationSnapshot
import com.kosio.app.domain.model.CurrencyCode
import com.kosio.app.domain.model.Product
import com.kosio.app.domain.repository.IngredientRepository
import com.kosio.app.domain.repository.ProductRepository
import com.kosio.app.domain.repository.SnapshotRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryViewModel(
    private val snapshotRepository: SnapshotRepository,
    private val productRepository: ProductRepository,
    private val ingredientRepository: IngredientRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val preferences = preferencesRepository.userPreferencesFlow

    fun getSnapshots(productId: String) = snapshotRepository.getSnapshotsForProduct(productId)

    fun deleteSnapshot(id: String) {
        viewModelScope.launch {
            snapshotRepository.deleteSnapshot(id)
        }
    }

    suspend fun getProductAndCalculation(productId: String): Pair<Product, HppResult>? {
        val prodWithRecipe = productRepository.getProductWithRecipeDirect(productId) ?: return null
        val ingredients = ingredientRepository.getAllIngredients().first().associateBy { it.id }
        val hpp = HppCalculator.calculateHpp(prodWithRecipe.product, prodWithRecipe.recipeItems, ingredients)
        return Pair(prodWithRecipe.product, hpp)
    }
}

@Composable
fun HistoryScreen(
    productId: String,
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit
) {
    val userPrefs by viewModel.preferences.collectAsState(initial = null)
    val currency = userPrefs?.currency ?: CurrencyCode.IDR
    val snapshots by viewModel.getSnapshots(productId).collectAsState(initial = emptyList())

    var snapshotToDelete by remember { mutableStateOf<CalculationSnapshot?>(null) }

    Scaffold(
        topBar = {
            KosioTopAppBar(
                title = stringResource(R.string.history_title),
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onNavigateBack
            )
        },
        containerColor = WarmIvoryBackground
    ) { padding ->
        if (snapshots.isEmpty()) {
            EmptyStateCard(
                message = stringResource(R.string.history_empty),
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(snapshots, key = { it.id }) { snapshot ->
                    val dateFormatted = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(snapshot.createdAt))

                    KosioCard {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateFormatted,
                                    style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                                )
                                IconButton(onClick = { snapshotToDelete = snapshot }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = ErrorRed)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.unit_cost),
                                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                                    )
                                    Text(
                                        text = "${currency.format(snapshot.unitCost)} / ${snapshot.yieldUnitLabel}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = DeepIndigo
                                        )
                                    )
                                }

                                if (snapshot.sellingPrice != null) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = stringResource(R.string.selling_price),
                                            style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                                        )
                                        Text(
                                            text = currency.format(snapshot.sellingPrice),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = CharcoalText
                                            )
                                        )
                                    }
                                }
                            }

                            if (snapshot.marginPercent != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Margin: ${snapshot.marginPercent.setScale(1, RoundingMode.HALF_UP)}%",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessTeal
                                        )
                                    )
                                    if (snapshot.profitPerUnit != null) {
                                        Text(
                                            text = "Laba: ${currency.format(snapshot.profitPerUnit)}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = CharcoalTextSecondary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    snapshotToDelete?.let { snap ->
        KosioConfirmDialog(
            title = "Hapus Snapshot?",
            message = "Apakah Anda yakin ingin menghapus snapshot riwayat kalkulasi ini?",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteSnapshot(snap.id)
                snapshotToDelete = null
            },
            onDismiss = { snapshotToDelete = null }
        )
    }
}

@Composable
fun SharePreviewScreen(
    productId: String,
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val userPrefs by viewModel.preferences.collectAsState(initial = null)
    val currency = userPrefs?.currency ?: CurrencyCode.IDR

    var data by remember { mutableStateOf<Pair<Product, HppResult>?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Compact, 1: Detailed

    LaunchedEffect(productId) {
        data = viewModel.getProductAndCalculation(productId)
    }

    val currentData = data
    val shareText: String = remember(currentData, selectedTab, currency) {
        if (currentData == null) return@remember ""
        val (product, hpp) = currentData
        val sb = StringBuilder()
        sb.append("📊 *KOSIO — Rincian Modal & HPP*\n")
        sb.append("Produk: *${product.name}*\n")
        sb.append("Hasil Produksi: ${product.yieldQuantity.stripTrailingZeros().toPlainString()} ${product.yieldUnitLabel} / batch\n")
        sb.append("----------------------------\n")
        sb.append("Modal HPP: *${currency.format(hpp.unitCost)}* / ${product.yieldUnitLabel}\n")
        sb.append("Total 1 Batch: *${currency.format(hpp.totalBatchCost)}*\n")

        if (selectedTab == 1) {
            sb.append("\n*Komponen Biaya 1 Batch:*\n")
            sb.append("• Biaya Bahan: ${currency.format(hpp.totalIngredientCost)}\n")
            sb.append("• Biaya Kemasan: ${currency.format(hpp.totalPackagingCost)}\n")
            sb.append("• Biaya Overhead: ${currency.format(hpp.totalOverheadCost)}\n")
            sb.append("\n*Rincian Bahan Baku:*\n")
            hpp.ingredientBreakdowns.forEach { b ->
                sb.append(" - ${b.ingredientName}: ${b.amountUsed.stripTrailingZeros().toPlainString()} ${b.unit} = ${currency.format(b.cost)}\n")
            }
        }
        sb.append("\n_Dihitung dengan Kosio Offline Food Cost Calculator_")
        sb.toString()
    }

    Scaffold(
        topBar = {
            KosioTopAppBar(
                title = stringResource(R.string.share_title),
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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = PureWhiteSurface,
                contentColor = DeepIndigo
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.share_compact), fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.share_detailed), fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            // Preview Box
            KosioCard(backgroundColor = PureWhiteSurface) {
                Column {
                    Text(
                        text = "Preview Teks:",
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = shareText,
                        style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalText)
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KosioPrimaryButton(
                    text = stringResource(R.string.share_via_apps),
                    icon = Icons.Default.Share,
                    onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Bagikan Ringkasan Kosio")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.weight(1f)
                )

                KosioSecondaryButton(
                    text = stringResource(R.string.action_copy_text),
                    icon = Icons.Default.ContentCopy,
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Kosio Summary", shareText)
                        clipboard.setPrimaryClip(clip)
                        scope.launch { snackbarHostState.showSnackbar("Teks berhasil disalin") }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // PDF Document Export Button
            KosioCard(backgroundColor = PureWhiteSurface) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Dokumen Cetak & Ekspor Resmi",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )
                    Text(
                        text = "Download dokumen PDF rapi berisi lembar resep, rincian biaya, dan rekomendasi harga jual siap cetak / bagikan.",
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                    )

                    KosioPrimaryButton(
                        text = "Ekspor Lembar Resep (PDF)",
                        onClick = {
                            val cur = currentData
                            if (cur != null) {
                                try {
                                    val pdfFile = com.kosio.app.data.export.ReportExportManager.generateProductPdfReport(
                                        context = context,
                                        product = cur.first,
                                        hpp = cur.second,
                                        currency = currency
                                    )
                                    com.kosio.app.data.export.ReportExportManager.shareFile(
                                        context = context,
                                        file = pdfFile,
                                        mimeType = "application/pdf",
                                        title = "Lembar Resep Kosio - ${cur.first.name}"
                                    )
                                } catch (e: Exception) {
                                    scope.launch { snackbarHostState.showSnackbar("Gagal membuat PDF: ${e.message}") }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
