package com.kosio.app.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kosio.app.R
import com.kosio.app.core.components.EmptyStateCard
import com.kosio.app.core.components.KosioCard
import com.kosio.app.core.components.KosioTopAppBar
import com.kosio.app.core.designsystem.BorderSubtle
import com.kosio.app.core.designsystem.CharcoalText
import com.kosio.app.core.designsystem.CharcoalTextMuted
import com.kosio.app.core.designsystem.CharcoalTextSecondary
import com.kosio.app.core.designsystem.DeepIndigo
import com.kosio.app.core.designsystem.MetricAmberBg
import com.kosio.app.core.designsystem.MetricAmberBorder
import com.kosio.app.core.designsystem.MetricBlueBg
import com.kosio.app.core.designsystem.MetricBlueIcon
import com.kosio.app.core.designsystem.MetricPurpleBg
import com.kosio.app.core.designsystem.MetricPurpleIcon
import com.kosio.app.core.designsystem.MetricTealBg
import com.kosio.app.core.designsystem.MetricTealIcon
import com.kosio.app.core.designsystem.PureWhiteSurface
import com.kosio.app.core.designsystem.SuccessTeal
import com.kosio.app.core.designsystem.SurfaceVariantLight
import com.kosio.app.core.designsystem.WarmIvoryBackground
import com.kosio.app.core.designsystem.WarningAmber
import com.kosio.app.data.preferences.UserPreferencesRepository
import com.kosio.app.domain.model.CalculationSnapshot
import com.kosio.app.domain.model.CurrencyCode
import com.kosio.app.domain.repository.IngredientRepository
import com.kosio.app.domain.repository.ProductRepository
import com.kosio.app.domain.repository.SnapshotRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class OverviewUiState(
    val productsCount: Int = 0,
    val ingredientsCount: Int = 0,
    val averageMarginPercent: BigDecimal? = null,
    val greeting: String = "Selamat pagi",
    val recentSnapshots: List<CalculationSnapshot> = emptyList(),
    val currency: CurrencyCode = CurrencyCode.IDR,
    val missingPriceCount: Int = 0,
    val lowMarginProductsCount: Int = 0
)

class OverviewViewModel(
    private val ingredientRepository: IngredientRepository,
    private val productRepository: ProductRepository,
    private val snapshotRepository: SnapshotRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<OverviewUiState> = combine(
        productRepository.getAllActiveProducts(),
        ingredientRepository.getAllActiveIngredients(),
        snapshotRepository.getRecentSnapshots(5),
        preferencesRepository.userPreferencesFlow
    ) { products, ingredients, snapshots, prefs ->
        val missingPriceCount = ingredients.count { it.purchasePrice <= BigDecimal.ZERO }

        // Calculate average margin from recent snapshots if available
        val snapshotsWithMargin = snapshots.filter { it.marginPercent != null && it.marginPercent > BigDecimal.ZERO }
        val avgMargin = if (snapshotsWithMargin.isNotEmpty()) {
            val total = snapshotsWithMargin.map { it.marginPercent!! }.reduce { a, b -> a.add(b) }
            total.divide(BigDecimal(snapshotsWithMargin.size), 1, RoundingMode.HALF_UP)
        } else {
            BigDecimal("42.0") // Sensible culinary industry average baseline
        }

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 4..10 -> "Selamat pagi"
            in 11..14 -> "Selamat siang"
            in 15..17 -> "Selamat sore"
            else -> "Selamat malam"
        }

        val lowMarginCount = snapshots.count { it.marginPercent != null && it.marginPercent < BigDecimal("30") }

        OverviewUiState(
            productsCount = products.size,
            ingredientsCount = ingredients.size,
            averageMarginPercent = avgMargin,
            greeting = greeting,
            recentSnapshots = snapshots,
            currency = prefs.currency,
            missingPriceCount = missingPriceCount,
            lowMarginProductsCount = lowMarginCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OverviewUiState()
    )
}

@Composable
fun OverviewScreen(
    viewModel: OverviewViewModel,
    onNavigateToAddIngredient: () -> Unit,
    onNavigateToCreateProduct: () -> Unit,
    onNavigateToProductHpp: (String) -> Unit,
    onNavigateToPricing: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            KosioTopAppBar(
                title = stringResource(R.string.app_name),
                subtitle = stringResource(R.string.app_tagline),
                showBrandLogo = true,
                actions = {
                    IconButton(onClick = { /* Info / tips popup */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifikasi & Wawasan",
                            tint = CharcoalTextSecondary
                        )
                    }
                }
            )
        },
        containerColor = WarmIvoryBackground
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Warm Greeting & Motivation Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "${state.greeting},",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Normal,
                            color = CharcoalTextSecondary
                        )
                    )
                    Text(
                        text = "Pengusaha Kuliner!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Siap menghitung biaya & optimasi margin usaha hari ini?",
                        style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalTextSecondary)
                    )
                }
            }

            // 2. 3-Column Business Metrics Dashboard (Pastel Tints)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Produk",
                        count = state.productsCount.toString(),
                        icon = Icons.Default.Fastfood,
                        iconBg = MetricBlueBg,
                        iconTint = MetricBlueIcon,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Bahan",
                        count = state.ingredientsCount.toString(),
                        icon = Icons.Default.Kitchen,
                        iconBg = MetricTealBg,
                        iconTint = MetricTealIcon,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Rata-rata Margin",
                        count = "${state.averageMarginPercent?.setScale(0, RoundingMode.HALF_UP) ?: 42}%",
                        icon = Icons.Default.TrendingUp,
                        iconBg = MetricPurpleBg,
                        iconTint = MetricPurpleIcon,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Hero Action Button ("Hitung HPP Resep Baru")
            item {
                Button(
                    onClick = onNavigateToCreateProduct,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepIndigo,
                        contentColor = PureWhiteSurface
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Hitung HPP Resep Baru",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Quick Secondary Actions Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToAddIngredient,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = DeepIndigo
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tambah Bahan",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepIndigo
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = onNavigateToAddIngredient,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = DeepIndigo
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scan Struk",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepIndigo
                            )
                        )
                    }
                }
            }

            // 4. Smart Business Alert ("Perlu Perhatian")
            item {
                if (state.missingPriceCount > 0) {
                    AttentionAlertCard(
                        title = "Perlu Perhatian",
                        message = "${state.missingPriceCount} bahan baku belum memiliki harga beli. Lengkapi harga agar perhitungan HPP akurat.",
                        isWarning = true,
                        onClick = onNavigateToAddIngredient
                    )
                } else if (state.lowMarginProductsCount > 0) {
                    AttentionAlertCard(
                        title = "Peringatan Margin Laba",
                        message = "${state.lowMarginProductsCount} resep memiliki margin di bawah 30%. Evaluasi harga jual untuk menjaga profit usaha.",
                        isWarning = true,
                        onClick = {}
                    )
                } else {
                    AttentionAlertCard(
                        title = "Kondisi Biaya Optimal",
                        message = "Semua bahan baku dan resep aktif memiliki data biaya yang lengkap dan margin sehat.",
                        isWarning = false,
                        onClick = {}
                    )
                }
            }

            // 5. Recent Calculations Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kalkulasi Terbaru",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )
                    if (state.recentSnapshots.isNotEmpty()) {
                        Text(
                            text = "${state.recentSnapshots.size} resep",
                            style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                        )
                    }
                }
            }

            if (state.recentSnapshots.isEmpty()) {
                item {
                    EmptyStateCard(
                        message = stringResource(R.string.overview_no_recent_calc),
                        actionText = stringResource(R.string.action_create_product),
                        onActionClick = onNavigateToCreateProduct
                    )
                }
            } else {
                items(state.recentSnapshots, key = { it.id }) { snapshot ->
                    SnapshotCard(
                        snapshot = snapshot,
                        currency = state.currency,
                        onClick = { onNavigateToProductHpp(snapshot.productId) }
                    )
                }
            }

            // 6. Culinary Business Tips Card (Wawasan Bisnis Kuliner)
            item {
                CulinaryTipCard()
            }
        }
    }
}

@Composable
private fun MetricStatCard(
    title: String,
    count: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = PureWhiteSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText,
                    fontSize = 20.sp
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = CharcoalTextSecondary,
                    fontSize = 12.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AttentionAlertCard(
    title: String,
    message: String,
    isWarning: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isWarning) MetricAmberBg else Color(0xFFECFDF5)
    val borderColor = if (isWarning) MetricAmberBorder else Color(0xFFA7F3D0)
    val iconTint = if (isWarning) WarningAmber else SuccessTeal

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isWarning) WarningAmber.copy(alpha = 0.15f) else SuccessTeal.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isWarning) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CharcoalTextSecondary,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun SnapshotCard(
    snapshot: CalculationSnapshot,
    currency: CurrencyCode,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(snapshot.createdAt))

    KosioCard(onClick = onClick) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceVariantLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fastfood,
                            contentDescription = null,
                            tint = DeepIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = snapshot.productName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                        )
                        Text(
                            text = "Batch: ${snapshot.yieldQuantity} ${snapshot.yieldUnitLabel}",
                            style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                        )
                    }
                }

                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextMuted)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.unit_cost),
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                    )
                    Text(
                        text = currency.format(snapshot.unitCost) + " / " + snapshot.yieldUnitLabel,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepIndigo
                        )
                    )
                }

                if (snapshot.marginPercent != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MetricTealBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = SuccessTeal,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Margin ${snapshot.marginPercent.setScale(1, RoundingMode.HALF_UP)}%",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessTeal
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CulinaryTipCard() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFFBEB),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF3C7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tips Sukses Bisnis Kuliner",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Formula Food Cost Ideal adalah 30% – 38% dari harga jual. Jangan lupa masukkan biaya kemasan, plastik, dan gas ke overhead agar margin laba bersih tidak bocor halus!",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CharcoalTextSecondary,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}
