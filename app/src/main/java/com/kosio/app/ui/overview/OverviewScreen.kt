package com.kosio.app.ui.overview

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kosio.app.R
import com.kosio.app.core.components.EmptyStateCard
import com.kosio.app.core.components.KosioCard
import com.kosio.app.core.components.KosioPrimaryButton
import com.kosio.app.core.components.KosioSecondaryButton
import com.kosio.app.core.components.KosioTopAppBar
import com.kosio.app.core.components.WarningCard
import com.kosio.app.core.designsystem.BorderSubtle
import com.kosio.app.core.designsystem.CharcoalText
import com.kosio.app.core.designsystem.CharcoalTextMuted
import com.kosio.app.core.designsystem.CharcoalTextSecondary
import com.kosio.app.core.designsystem.DeepIndigo
import com.kosio.app.core.designsystem.LightIndigo
import com.kosio.app.core.designsystem.LightTeal
import com.kosio.app.core.designsystem.PureWhiteSurface
import com.kosio.app.core.designsystem.SuccessTeal
import com.kosio.app.core.designsystem.WarmIvoryBackground
import com.kosio.app.data.preferences.UserPreferencesRepository
import com.kosio.app.domain.model.CalculationSnapshot
import com.kosio.app.domain.model.CurrencyCode
import com.kosio.app.domain.model.Ingredient
import com.kosio.app.domain.model.Product
import com.kosio.app.domain.repository.IngredientRepository
import com.kosio.app.domain.repository.ProductRepository
import com.kosio.app.domain.repository.SnapshotRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class OverviewUiState(
    val productsCount: Int = 0,
    val ingredientsCount: Int = 0,
    val recentSnapshots: List<CalculationSnapshot> = emptyList(),
    val currency: CurrencyCode = CurrencyCode.IDR,
    val missingPriceCount: Int = 0
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
        // Check if any ingredients have 0 price
        val missingPriceCount = ingredients.count { it.purchasePrice <= BigDecimal.ZERO }

        OverviewUiState(
            productsCount = products.size,
            ingredientsCount = ingredients.size,
            recentSnapshots = snapshots,
            currency = prefs.currency,
            missingPriceCount = missingPriceCount
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
                subtitle = stringResource(R.string.app_tagline)
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
            // Quick Action Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KosioPrimaryButton(
                        text = stringResource(R.string.action_create_product),
                        icon = Icons.Default.Add,
                        onClick = onNavigateToCreateProduct,
                        modifier = Modifier.weight(1f)
                    )
                    KosioSecondaryButton(
                        text = stringResource(R.string.action_add_ingredient),
                        icon = Icons.Default.Add,
                        onClick = onNavigateToAddIngredient,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Stats Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = stringResource(R.string.overview_total_products),
                        count = state.productsCount.toString(),
                        icon = Icons.Default.Fastfood,
                        iconBg = LightIndigo,
                        iconTint = DeepIndigo,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = stringResource(R.string.overview_total_ingredients),
                        count = state.ingredientsCount.toString(),
                        icon = Icons.Default.Kitchen,
                        iconBg = LightTeal,
                        iconTint = SuccessTeal,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Warning if missing prices
            if (state.missingPriceCount > 0) {
                item {
                    WarningCard(
                        message = stringResource(R.string.overview_missing_price_warning, state.missingPriceCount)
                    )
                }
            }

            // Recent Calculations Section
            item {
                Text(
                    text = stringResource(R.string.overview_recent_calculations),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
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
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    KosioCard(modifier = modifier) {
        Column {
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
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
            )
        }
    }
}

@Composable
private fun SnapshotCard(
    snapshot: CalculationSnapshot,
    currency: CurrencyCode,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(snapshot.createdAt))

    KosioCard(onClick = onClick) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = snapshot.productName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextMuted)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.margin_percent),
                            style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                        )
                        Text(
                            text = "${snapshot.marginPercent.setScale(1, java.math.RoundingMode.HALF_UP)}%",
                            style = MaterialTheme.typography.titleMedium.copy(
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
