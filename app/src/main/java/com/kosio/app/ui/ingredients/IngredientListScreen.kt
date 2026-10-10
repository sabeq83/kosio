package com.kosio.app.ui.ingredients

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kosio.app.R
import com.kosio.app.core.components.EmptyStateCard
import com.kosio.app.core.components.KosioCard
import com.kosio.app.core.components.KosioConfirmDialog
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
import com.kosio.app.core.designsystem.LightAmber
import com.kosio.app.core.designsystem.LightIndigo
import com.kosio.app.core.designsystem.LightTeal
import com.kosio.app.core.designsystem.PrimaryIndigo
import com.kosio.app.core.designsystem.PureWhiteSurface
import com.kosio.app.core.designsystem.SuccessTeal
import com.kosio.app.core.designsystem.WarmIvoryBackground
import com.kosio.app.data.preferences.UserPreferencesRepository
import com.kosio.app.domain.calculation.HppCalculator
import com.kosio.app.domain.model.CurrencyCode
import com.kosio.app.domain.model.Ingredient
import com.kosio.app.domain.model.IngredientPriceHistory
import com.kosio.app.domain.model.Product
import com.kosio.app.domain.model.StandardUnit
import com.kosio.app.domain.model.UnitDimension
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
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class IngredientViewModel(
    private val ingredientRepository: IngredientRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filterTab = MutableStateFlow(0) // 0: Active, 1: Archived
    val filterTab = _filterTab.asStateFlow()

    val currency = preferencesRepository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val ingredientsList: StateFlow<List<Ingredient>> = combine(
        ingredientRepository.getAllIngredients(),
        _searchQuery,
        _filterTab
    ) { list, query, tab ->
        list.filter { ing ->
            val matchesTab = if (tab == 0) !ing.isArchived else ing.isArchived
            val matchesQuery = query.isBlank() || ing.name.contains(query, ignoreCase = true) ||
                    (ing.category?.contains(query, ignoreCase = true) == true)
            matchesTab && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setFilterTab(tab: Int) {
        _filterTab.value = tab
    }

    suspend fun getIngredient(id: String): Ingredient? {
        return ingredientRepository.getIngredientById(id)
    }

    suspend fun getPriceHistory(id: String): List<IngredientPriceHistory> {
        return ingredientRepository.getPriceHistory(id).first()
    }

    suspend fun getUsedInProducts(id: String): List<Product> {
        return ingredientRepository.getActiveProductsUsingIngredient(id)
    }

    fun saveIngredient(
        id: String?,
        name: String,
        category: String?,
        dimension: UnitDimension,
        purchasePrice: BigDecimal,
        purchaseQuantity: BigDecimal,
        purchaseUnit: String,
        baseUnit: String,
        customPackQty: BigDecimal?,
        wastePercent: BigDecimal,
        note: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val existing = id?.let { ingredientRepository.getIngredientById(it) }
            val ingredient = Ingredient(
                id = id ?: UUID.randomUUID().toString(),
                name = name.trim(),
                category = category?.trim()?.ifBlank { null },
                unitDimension = dimension,
                purchasePrice = purchasePrice,
                purchaseQuantity = purchaseQuantity,
                purchaseUnit = purchaseUnit,
                baseUnit = baseUnit,
                customPackQuantity = customPackQty,
                wastePercent = wastePercent,
                isArchived = existing?.isArchived ?: false,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            ingredientRepository.saveIngredient(ingredient, note)
            onSuccess()
        }
    }

    fun archiveIngredient(id: String, archive: Boolean, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            ingredientRepository.setArchived(id, archive)
            onDone()
        }
    }

    fun deleteIngredient(id: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val deleted = ingredientRepository.deleteIngredient(id)
            onResult(deleted)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientListScreen(
    viewModel: IngredientViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToScanReceipt: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val ingredients by viewModel.ingredientsList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterTab by viewModel.filterTab.collectAsState()
    val userPrefs by viewModel.currency.collectAsState()
    val currency = userPrefs?.currency ?: CurrencyCode.IDR

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            KosioTopAppBar(
                title = stringResource(R.string.ingredients_title),
                subtitle = stringResource(R.string.nav_ingredients),
                actions = {
                    IconButton(onClick = onNavigateToScanReceipt) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Scan Struk", tint = DeepIndigo)
                    }
                    if (ingredients.isNotEmpty()) {
                        IconButton(onClick = {
                            try {
                                val csvFile = com.kosio.app.data.export.ReportExportManager.generateIngredientsCsv(
                                    context = context,
                                    ingredients = ingredients,
                                    currency = currency
                                )
                                com.kosio.app.data.export.ReportExportManager.shareFile(
                                    context = context,
                                    file = csvFile,
                                    mimeType = "text/csv",
                                    title = "Master Bahan Baku Kosio"
                                )
                            } catch (e: Exception) {
                                scope.launch { snackbarHostState.showSnackbar("Gagal mengekspor CSV: ${e.message}") }
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Ekspor CSV", tint = DeepIndigo)
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = DeepIndigo,
                contentColor = PureWhiteSurface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add_ingredient))
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

            if (ingredients.isEmpty()) {
                EmptyStateCard(
                    message = if (searchQuery.isBlank()) stringResource(R.string.ingredients_empty) else "Tidak ada bahan yang cocok.",
                    actionText = if (searchQuery.isBlank() && filterTab == 0) stringResource(R.string.action_add_ingredient) else null,
                    onActionClick = onNavigateToAdd
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(ingredients, key = { it.id }) { ingredient ->
                        IngredientListItemCard(
                            ingredient = ingredient,
                            currency = currency,
                            onClick = { onNavigateToDetail(ingredient.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientListItemCard(
    ingredient: Ingredient,
    currency: CurrencyCode,
    onClick: () -> Unit
) {
    val effectivePrice = HppCalculator.calculateEffectiveUnitPrice(
        purchasePrice = ingredient.purchasePrice,
        purchaseQuantity = ingredient.purchaseQuantity,
        purchaseUnit = ingredient.purchaseUnit,
        customPackQuantity = ingredient.customPackQuantity,
        wastePercent = ingredient.wastePercent
    )

    KosioCard(onClick = onClick) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )
                    if (!ingredient.category.isNullOrBlank()) {
                        Text(
                            text = ingredient.category,
                            style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                        )
                    }
                }
                if (ingredient.wastePercent > BigDecimal.ZERO) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LightAmber)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Waste ${ingredient.wastePercent.stripTrailingZeros().toPlainString()}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepIndigo
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.purchase_price),
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                    )
                    Text(
                        text = "${currency.format(ingredient.purchasePrice)} / ${ingredient.purchaseQuantity.stripTrailingZeros().toPlainString()} ${ingredient.purchaseUnit}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = CharcoalText
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.effective_unit_price),
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                    )
                    Text(
                        text = "${currency.format(effectivePrice)} / ${ingredient.baseUnit}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SuccessTeal
                        )
                    )
                }
            }
        }
    }
}
