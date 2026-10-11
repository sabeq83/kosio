package com.kosio.app.ui.products

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.kosio.app.core.designsystem.LightIndigo
import com.kosio.app.core.designsystem.PureWhiteSurface
import com.kosio.app.core.designsystem.SuccessTeal
import com.kosio.app.core.designsystem.WarmIvoryBackground
import com.kosio.app.data.preferences.UserPreferencesRepository
import com.kosio.app.domain.calculation.HppCalculator
import com.kosio.app.domain.calculation.HppResult
import com.kosio.app.domain.model.CalculationSnapshot
import com.kosio.app.domain.model.CurrencyCode
import com.kosio.app.domain.model.Ingredient
import com.kosio.app.domain.model.Product
import com.kosio.app.domain.model.ProductWithRecipe
import com.kosio.app.domain.model.RecipeItem
import com.kosio.app.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID

data class ProductSummaryItem(
    val product: Product,
    val unitCost: BigDecimal,
    val itemCount: Int
)

class ProductViewModel(
    private val productRepository: com.kosio.app.domain.repository.ProductRepository,
    private val ingredientRepository: IngredientRepository,
    private val snapshotRepository: com.kosio.app.domain.repository.SnapshotRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filterTab = MutableStateFlow(0) // 0: Active, 1: Archived
    val filterTab = _filterTab.asStateFlow()

    val currency = preferencesRepository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val productsList: StateFlow<List<ProductSummaryItem>> = combine(
        productRepository.getAllProducts(),
        ingredientRepository.getAllIngredients(),
        _searchQuery,
        _filterTab
    ) { products, ingredients, query, tab ->
        val ingredientMap = ingredients.associateBy { it.id }
        products.filter { prod ->
            val matchesTab = if (tab == 0) !prod.isArchived else prod.isArchived
            val matchesQuery = query.isBlank() || prod.name.contains(query, ignoreCase = true) ||
                    (prod.category?.contains(query, ignoreCase = true) == true)
            matchesTab && matchesQuery
        }.map { prod ->
            val recipe = productRepository.getProductWithRecipeDirect(prod.id)?.recipeItems ?: emptyList()
            val hpp = try {
                HppCalculator.calculateHpp(prod, recipe, ingredientMap)
            } catch (e: Exception) {
                null
            }
            ProductSummaryItem(
                product = prod,
                unitCost = hpp?.unitCost ?: BigDecimal.ZERO,
                itemCount = recipe.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setFilterTab(tab: Int) {
        _filterTab.value = tab
    }

    suspend fun getProductWithRecipe(id: String): ProductWithRecipe? {
        return productRepository.getProductWithRecipeDirect(id)
    }

    suspend fun getActiveIngredients(): List<Ingredient> {
        return ingredientRepository.getAllActiveIngredients().first()
    }

    suspend fun calculateHppForProduct(productId: String): Pair<Product, HppResult>? {
        val productWithRecipe = productRepository.getProductWithRecipeDirect(productId) ?: return null
        val ingredients = ingredientRepository.getAllIngredients().first()
        val ingredientMap = ingredients.associateBy { it.id }
        val result = HppCalculator.calculateHpp(
            product = productWithRecipe.product,
            recipeItems = productWithRecipe.recipeItems,
            ingredientMap = ingredientMap
        )
        return Pair(productWithRecipe.product, result)
    }

    fun saveProductWithRecipe(
        id: String?,
        name: String,
        category: String?,
        yieldQty: BigDecimal,
        yieldLabel: String,
        packagingCost: BigDecimal,
        overheadBatch: BigDecimal,
        overheadUnit: BigDecimal,
        overheadPercent: BigDecimal,
        recipeItems: List<RecipeItem>,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val existing = id?.let { productRepository.getProductWithRecipeDirect(it)?.product }
            val prodId = id ?: UUID.randomUUID().toString()
            val product = Product(
                id = prodId,
                name = name.trim(),
                category = category?.trim()?.ifBlank { null },
                yieldQuantity = yieldQty,
                yieldUnitLabel = yieldLabel.trim().ifBlank { "pcs" },
                packagingCost = packagingCost,
                overheadPerBatch = overheadBatch,
                overheadPerUnit = overheadUnit,
                overheadPercentage = overheadPercent,
                isArchived = existing?.isArchived ?: false,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val mappedItems = recipeItems.map { it.copy(productId = prodId) }
            productRepository.saveProductWithRecipe(product, mappedItems)
            onSuccess(prodId)
        }
    }

    fun duplicateProduct(productId: String, onDone: (String) -> Unit) {
        viewModelScope.launch {
            val existing = productRepository.getProductWithRecipeDirect(productId)
            if (existing != null) {
                val newName = "${existing.product.name} (Copy)"
                val newId = productRepository.duplicateProduct(productId, newName)
                onDone(newId)
            }
        }
    }

    fun archiveProduct(id: String, archive: Boolean, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            productRepository.setArchived(id, archive)
            onDone()
        }
    }

    fun deleteProduct(id: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            productRepository.deleteProduct(id)
            onDone()
        }
    }

    fun saveSnapshot(
        product: Product,
        hppResult: HppResult,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val snapshot = CalculationSnapshot(
                productId = product.id,
                productName = product.name,
                yieldQuantity = product.yieldQuantity,
                yieldUnitLabel = product.yieldUnitLabel,
                totalMaterialCost = hppResult.totalIngredientCost,
                totalPackagingCost = hppResult.totalPackagingCost,
                totalOverheadCost = hppResult.totalOverheadCost,
                totalBatchCost = hppResult.totalBatchCost,
                unitCost = hppResult.unitCost,
                createdAt = System.currentTimeMillis()
            )
            snapshotRepository.saveSnapshot(snapshot)
            onDone()
        }
    }
}

@Composable
fun ProductListScreen(
    viewModel: ProductViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val products by viewModel.productsList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterTab by viewModel.filterTab.collectAsState()
    val userPrefs by viewModel.currency.collectAsState()
    val currency = userPrefs?.currency ?: CurrencyCode.IDR

    Scaffold(
        topBar = {
            KosioTopAppBar(
                title = stringResource(R.string.products_title),
                subtitle = stringResource(R.string.nav_products),
                showBrandLogo = true
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = DeepIndigo,
                contentColor = PureWhiteSurface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_create_product))
            }
        },
        containerColor = WarmIvoryBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text(stringResource(R.string.action_search), color = CharcoalTextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CharcoalTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PureWhiteSurface,
                        unfocusedContainerColor = PureWhiteSurface,
                        focusedBorderColor = DeepIndigo,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Tabs: Active / Archived
            TabRow(
                selectedTabIndex = filterTab,
                containerColor = PureWhiteSurface,
                contentColor = DeepIndigo
            ) {
                Tab(
                    selected = filterTab == 0,
                    onClick = { viewModel.setFilterTab(0) },
                    text = { Text("Aktif", fontWeight = if (filterTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = filterTab == 1,
                    onClick = { viewModel.setFilterTab(1) },
                    text = { Text("Arsip", fontWeight = if (filterTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            if (products.isEmpty()) {
                EmptyStateCard(
                    message = if (searchQuery.isBlank()) stringResource(R.string.products_empty) else "Tidak ada produk yang cocok.",
                    actionText = if (searchQuery.isBlank() && filterTab == 0) stringResource(R.string.action_create_product) else null,
                    onActionClick = onNavigateToCreate
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(products, key = { _, it -> it.product.id }) { index, item ->
                        ProductListItemCard(
                            item = item,
                            currency = currency,
                            onClick = { onNavigateToDetail(item.product.id) }
                        )

                        // Insert In-Feed Native Ad Card at position 3
                        if (index == 2 && products.size >= 3) {
                            Spacer(modifier = Modifier.height(6.dp))
                            com.kosio.app.ui.ads.KosioNativeAdCard()
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductListItemCard(
    item: ProductSummaryItem,
    currency: CurrencyCode,
    onClick: () -> Unit
) {
    KosioCard(onClick = onClick) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.product.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )
                    if (!item.product.category.isNullOrBlank()) {
                        Text(
                            text = item.product.category,
                            style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(LightIndigo)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${item.itemCount} Bahan",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepIndigo
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Yield: ${item.product.yieldQuantity.stripTrailingZeros().toPlainString()} ${item.product.yieldUnitLabel}",
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.unit_cost),
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                    )
                    Text(
                        text = "${currency.format(item.unitCost)} / ${item.product.yieldUnitLabel}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepIndigo
                        )
                    )
                }
            }
        }
    }
}
