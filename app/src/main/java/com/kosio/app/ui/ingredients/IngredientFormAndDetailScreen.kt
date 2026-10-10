package com.kosio.app.ui.ingredients

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kosio.app.R
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
import com.kosio.app.core.designsystem.ErrorRed
import com.kosio.app.core.designsystem.LightAmber
import com.kosio.app.core.designsystem.LightIndigo
import com.kosio.app.core.designsystem.LightTeal
import com.kosio.app.core.designsystem.PureWhiteSurface
import com.kosio.app.core.designsystem.SuccessTeal
import com.kosio.app.core.designsystem.WarmIvoryBackground
import com.kosio.app.domain.calculation.HppCalculator
import com.kosio.app.domain.model.CurrencyCode
import com.kosio.app.domain.model.Ingredient
import com.kosio.app.domain.model.IngredientPriceHistory
import com.kosio.app.domain.model.Product
import com.kosio.app.domain.model.UnitDimension
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientFormScreen(
    ingredientId: String?,
    viewModel: IngredientViewModel,
    onNavigateBack: () -> Unit
) {
    val userPrefs by viewModel.currency.collectAsState()
    val currency = userPrefs?.currency ?: CurrencyCode.IDR

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedDimension by remember { mutableStateOf(UnitDimension.WEIGHT) }
    var purchasePriceStr by remember { mutableStateOf("") }
    var purchaseQuantityStr by remember { mutableStateOf("1") }
    var purchaseUnit by remember { mutableStateOf("kg") }
    var baseUnit by remember { mutableStateOf("g") }
    var customPackQtyStr by remember { mutableStateOf("") }
    var wastePercentStr by remember { mutableStateOf("0") }
    var priceNote by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var wasteError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(ingredientId) {
        if (ingredientId != null) {
            val ing = viewModel.getIngredient(ingredientId)
            if (ing != null) {
                name = ing.name
                category = ing.category ?: ""
                selectedDimension = ing.unitDimension
                purchasePriceStr = ing.purchasePrice.stripTrailingZeros().toPlainString()
                purchaseQuantityStr = ing.purchaseQuantity.stripTrailingZeros().toPlainString()
                purchaseUnit = ing.purchaseUnit
                baseUnit = ing.baseUnit
                customPackQtyStr = ing.customPackQuantity?.stripTrailingZeros()?.toPlainString() ?: ""
                wastePercentStr = ing.wastePercent.stripTrailingZeros().toPlainString()
            }
        }
    }

    // Live calculation of effective unit price
    val effectivePrice: BigDecimal = remember(purchasePriceStr, purchaseQuantityStr, purchaseUnit, customPackQtyStr, wastePercentStr) {
        try {
            val price = BigDecimal(purchasePriceStr.ifEmpty { "0" })
            val qty = BigDecimal(purchaseQuantityStr.ifEmpty { "1" })
            val waste = BigDecimal(wastePercentStr.ifEmpty { "0" })
            val customPack = customPackQtyStr.ifBlank { null }?.let { BigDecimal(it) }

            if (waste >= BigDecimal("100")) {
                wasteError = "Susut harus kurang dari 100%"
                BigDecimal.ZERO
            } else {
                wasteError = null
                HppCalculator.calculateEffectiveUnitPrice(
                    purchasePrice = price,
                    purchaseQuantity = qty,
                    purchaseUnit = purchaseUnit,
                    customPackQuantity = customPack,
                    wastePercent = waste
                )
            }
        } catch (e: Exception) {
            BigDecimal.ZERO
        }
    }

    Scaffold(
        topBar = {
            KosioTopAppBar(
                title = if (ingredientId == null) stringResource(R.string.action_add_ingredient) else stringResource(R.string.action_edit),
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Info Card
            KosioCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Informasi Bahan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )

                    KosioTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = if (it.isBlank()) "Nama bahan wajib diisi" else null
                        },
                        label = stringResource(R.string.ingredient_name),
                        placeholder = stringResource(R.string.ingredient_name_hint),
                        isError = nameError != null,
                        errorMessage = nameError
                    )

                    KosioTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = stringResource(R.string.ingredient_category),
                        placeholder = stringResource(R.string.ingredient_category_hint)
                    )
                }
            }

            // Unit & Dimension Selection Card
            KosioCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.ingredient_unit_type),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DimensionChip(
                            title = "Berat",
                            isSelected = selectedDimension == UnitDimension.WEIGHT,
                            onClick = {
                                selectedDimension = UnitDimension.WEIGHT
                                purchaseUnit = "kg"
                                baseUnit = "g"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        DimensionChip(
                            title = "Volume",
                            isSelected = selectedDimension == UnitDimension.VOLUME,
                            onClick = {
                                selectedDimension = UnitDimension.VOLUME
                                purchaseUnit = "l"
                                baseUnit = "ml"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        DimensionChip(
                            title = "Jumlah",
                            isSelected = selectedDimension == UnitDimension.COUNT,
                            onClick = {
                                selectedDimension = UnitDimension.COUNT
                                purchaseUnit = "pcs"
                                baseUnit = "pcs"
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (selectedDimension == UnitDimension.COUNT) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UnitOptionChip(
                                title = "pcs",
                                isSelected = purchaseUnit == "pcs",
                                onClick = { purchaseUnit = "pcs"; baseUnit = "pcs" },
                                modifier = Modifier.weight(1f)
                            )
                            UnitOptionChip(
                                title = "pack",
                                isSelected = purchaseUnit == "pack",
                                onClick = { purchaseUnit = "pack"; baseUnit = "pcs" },
                                modifier = Modifier.weight(1f)
                            )
                            UnitOptionChip(
                                title = "box",
                                isSelected = purchaseUnit == "box",
                                onClick = { purchaseUnit = "box"; baseUnit = "pcs" },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (purchaseUnit == "pack" || purchaseUnit == "box") {
                            KosioNumberField(
                                value = customPackQtyStr,
                                onValueChange = { customPackQtyStr = it },
                                label = stringResource(R.string.custom_pack_quantity),
                                placeholder = "50",
                                suffix = "pcs"
                            )
                        }
                    }
                }
            }

            // Purchase Price & Quantity Card
            KosioCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Harga Pembelian & Susut",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )

                    KosioNumberField(
                        value = purchasePriceStr,
                        onValueChange = {
                            purchasePriceStr = it
                            priceError = if (it.isBlank() || BigDecimal(it.ifEmpty { "0" }) <= BigDecimal.ZERO) "Harga harus lebih dari 0" else null
                        },
                        label = stringResource(R.string.purchase_price),
                        prefix = currency.symbol + " ",
                        isError = priceError != null,
                        errorMessage = priceError
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KosioNumberField(
                            value = purchaseQuantityStr,
                            onValueChange = { purchaseQuantityStr = it },
                            label = stringResource(R.string.purchase_quantity),
                            modifier = Modifier.weight(1f)
                        )
                        KosioTextField(
                            value = purchaseUnit,
                            onValueChange = { purchaseUnit = it },
                            label = stringResource(R.string.purchase_unit),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    KosioNumberField(
                        value = wastePercentStr,
                        onValueChange = { wastePercentStr = it },
                        label = stringResource(R.string.waste_percentage),
                        placeholder = "0",
                        suffix = "%",
                        isError = wasteError != null,
                        errorMessage = wasteError
                    )
                    Text(
                        text = stringResource(R.string.waste_helper),
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                    )
                }
            }

            // Live Effective Cost Preview Card
            KosioCard(backgroundColor = LightTeal, borderColor = SuccessTeal) {
                Column {
                    Text(
                        text = stringResource(R.string.effective_unit_price),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SuccessTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${currency.format(effectivePrice)} / $baseUnit",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepIndigo
                        )
                    )
                    Text(
                        text = stringResource(R.string.effective_unit_price_desc),
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                    )
                }
            }

            // Save Button
            KosioPrimaryButton(
                text = stringResource(R.string.action_save),
                onClick = {
                    if (name.isBlank()) {
                        nameError = "Nama bahan wajib diisi"
                        return@KosioPrimaryButton
                    }
                    val price = BigDecimal(purchasePriceStr.ifEmpty { "0" })
                    if (price <= BigDecimal.ZERO) {
                        priceError = "Harga beli harus diisi"
                        return@KosioPrimaryButton
                    }
                    val qty = BigDecimal(purchaseQuantityStr.ifEmpty { "1" })
                    val waste = BigDecimal(wastePercentStr.ifEmpty { "0" })
                    if (waste >= BigDecimal("100")) {
                        wasteError = "Susut harus kurang dari 100%"
                        return@KosioPrimaryButton
                    }
                    val customPack = customPackQtyStr.ifBlank { null }?.let { BigDecimal(it) }

                    viewModel.saveIngredient(
                        id = ingredientId,
                        name = name,
                        category = category,
                        dimension = selectedDimension,
                        purchasePrice = price,
                        purchaseQuantity = qty,
                        purchaseUnit = purchaseUnit,
                        baseUnit = baseUnit,
                        customPackQty = customPack,
                        wastePercent = waste,
                        onSuccess = onNavigateBack
                    )
                }
            )
        }
    }
}

@Composable
fun IngredientDetailScreen(
    ingredientId: String,
    viewModel: IngredientViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val userPrefs by viewModel.currency.collectAsState()
    val currency = userPrefs?.currency ?: CurrencyCode.IDR

    var ingredient by remember { mutableStateOf<Ingredient?>(null) }
    var priceHistory by remember { mutableStateOf<List<IngredientPriceHistory>>(emptyList()) }
    var usedInProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showCannotDeleteAlert by remember { mutableStateOf(false) }

    LaunchedEffect(ingredientId) {
        ingredient = viewModel.getIngredient(ingredientId)
        priceHistory = viewModel.getPriceHistory(ingredientId)
        usedInProducts = viewModel.getUsedInProducts(ingredientId)
    }

    Scaffold(
        topBar = {
            KosioTopAppBar(
                title = ingredient?.name ?: stringResource(R.string.ingredients_title),
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { onNavigateToEdit(ingredientId) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit), tint = DeepIndigo)
                    }
                    if (ingredient?.isArchived == true) {
                        IconButton(onClick = {
                            viewModel.archiveIngredient(ingredientId, false) {
                                scope.launch {
                                    ingredient = viewModel.getIngredient(ingredientId)
                                    snackbarHostState.showSnackbar("Bahan diaktifkan kembali")
                                }
                            }
                        }) {
                            Icon(Icons.Default.Unarchive, contentDescription = stringResource(R.string.action_unarchive), tint = SuccessTeal)
                        }
                    } else {
                        IconButton(onClick = {
                            viewModel.archiveIngredient(ingredientId, true) {
                                scope.launch {
                                    ingredient = viewModel.getIngredient(ingredientId)
                                    snackbarHostState.showSnackbar("Bahan diarsipkan")
                                }
                            }
                        }) {
                            Icon(Icons.Default.Archive, contentDescription = stringResource(R.string.action_archive), tint = CharcoalTextSecondary)
                        }
                    }
                    IconButton(onClick = {
                        if (usedInProducts.isNotEmpty()) {
                            showCannotDeleteAlert = true
                        } else {
                            showDeleteConfirm = true
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete), tint = ErrorRed)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = WarmIvoryBackground
    ) { padding ->
        val currentIng = ingredient
        if (currentIng != null) {
            val effectivePrice = HppCalculator.calculateEffectiveUnitPrice(
                purchasePrice = currentIng.purchasePrice,
                purchaseQuantity = currentIng.purchaseQuantity,
                purchaseUnit = currentIng.purchaseUnit,
                customPackQuantity = currentIng.customPackQuantity,
                wastePercent = currentIng.wastePercent
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Info
                item {
                    KosioCard {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = currentIng.name,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalText
                                        )
                                    )
                                    if (!currentIng.category.isNullOrBlank()) {
                                        Text(
                                            text = currentIng.category,
                                            style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalTextSecondary)
                                        )
                                    }
                                }
                                if (currentIng.isArchived) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(BorderSubtle)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Arsip",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = CharcoalTextSecondary
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.purchase_price),
                                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                                    )
                                    Text(
                                        text = "${currency.format(currentIng.purchasePrice)} / ${currentIng.purchaseQuantity.stripTrailingZeros().toPlainString()} ${currentIng.purchaseUnit}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
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
                                        text = "${currency.format(effectivePrice)} / ${currentIng.baseUnit}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessTeal
                                        )
                                    )
                                }
                            }

                            if (currentIng.wastePercent > BigDecimal.ZERO) {
                                Spacer(modifier = Modifier.height(12.dp))
                                WarningCard(
                                    message = "Susut pembersihan/waste dihitung sebesar ${currentIng.wastePercent.stripTrailingZeros().toPlainString()}%"
                                )
                            }
                        }
                    }
                }

                // Used In Products Section
                item {
                    Text(
                        text = stringResource(R.string.used_in_products, usedInProducts.size),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )
                }

                if (usedInProducts.isEmpty()) {
                    item {
                        Text(
                            text = "Bahan ini belum dimasukkan ke resep produk manapun.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalTextMuted),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                } else {
                    items(usedInProducts, key = { it.id }) { prod ->
                        KosioCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = DeepIndigo)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = prod.name,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = CharcoalText
                                    )
                                )
                            }
                        }
                    }
                }

                // Price History Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.price_history),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )
                }

                items(priceHistory, key = { it.id }) { historyItem ->
                    val dateFormatted = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(historyItem.effectiveDate))
                    KosioCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = dateFormatted,
                                    style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                                )
                                Text(
                                    text = "${currency.format(historyItem.purchasePrice)} / ${historyItem.purchaseQuantity.stripTrailingZeros().toPlainString()} ${historyItem.purchaseUnit}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalText
                                    )
                                )
                                if (!historyItem.note.isNullOrBlank()) {
                                    Text(
                                        text = historyItem.note,
                                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextMuted)
                                    )
                                }
                            }

                            Text(
                                text = "${currency.format(historyItem.effectiveUnitPrice)} / ${currentIng.baseUnit}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = SuccessTeal
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        KosioConfirmDialog(
            title = stringResource(R.string.dialog_confirm_delete_title),
            message = stringResource(R.string.dialog_confirm_delete_message, ingredient?.name ?: ""),
            isDestructive = true,
            onConfirm = {
                showDeleteConfirm = false
                viewModel.deleteIngredient(ingredientId) { success ->
                    if (success) {
                        onNavigateBack()
                    }
                }
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    if (showCannotDeleteAlert) {
        KosioConfirmDialog(
            title = "Tidak Dapat Menghapus",
            message = stringResource(R.string.cannot_delete_used_ingredient, usedInProducts.size),
            confirmText = "Oke",
            dismissText = "Tutup",
            onConfirm = { showCannotDeleteAlert = false },
            onDismiss = { showCannotDeleteAlert = false }
        )
    }
}

@Composable
private fun DimensionChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) DeepIndigo else WarmIvoryBackground)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) PureWhiteSurface else CharcoalText
            )
        )
    }
}

@Composable
private fun UnitOptionChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) LightTeal else WarmIvoryBackground)
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) SuccessTeal else CharcoalTextSecondary
            )
        )
    }
}
