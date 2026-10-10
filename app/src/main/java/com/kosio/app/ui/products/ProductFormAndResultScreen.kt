package com.kosio.app.ui.products

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.kosio.app.core.designsystem.PrimaryIndigo
import com.kosio.app.core.designsystem.PureWhiteSurface
import com.kosio.app.core.designsystem.SuccessTeal
import com.kosio.app.core.designsystem.SurfaceVariantLight
import com.kosio.app.core.designsystem.WarmIvoryBackground
import com.kosio.app.domain.calculation.HppResult
import com.kosio.app.domain.model.CurrencyCode
import com.kosio.app.domain.model.Ingredient
import com.kosio.app.domain.model.Product
import com.kosio.app.domain.model.RecipeItem
import com.kosio.app.domain.model.UnitDimension
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

data class RecipeItemDraft(
    val id: String = UUID.randomUUID().toString(),
    var ingredientId: String = "",
    var ingredientName: String = "",
    var amountUsedStr: String = "1",
    var unit: String = "g"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    productId: String?,
    viewModel: ProductViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddIngredient: () -> Unit,
    onSavedAndNavigateToHpp: (String) -> Unit
) {
    val userPrefs by viewModel.currency.collectAsState()
    val currency = userPrefs?.currency ?: CurrencyCode.IDR

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var yieldQtyStr by remember { mutableStateOf("1") }
    var yieldLabel by remember { mutableStateOf("porsi") }
    var packagingCostStr by remember { mutableStateOf("0") }
    var overheadBatchStr by remember { mutableStateOf("0") }
    var overheadUnitStr by remember { mutableStateOf("0") }
    var overheadPercentStr by remember { mutableStateOf("0") }

    val recipeDrafts = remember { mutableStateListOf<RecipeItemDraft>() }
    var availableIngredients by remember { mutableStateOf<List<Ingredient>>(emptyList()) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var yieldError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(productId) {
        availableIngredients = viewModel.getActiveIngredients()
        if (productId != null) {
            val productWithRecipe = viewModel.getProductWithRecipe(productId)
            if (productWithRecipe != null) {
                val prod = productWithRecipe.product
                name = prod.name
                category = prod.category ?: ""
                yieldQtyStr = prod.yieldQuantity.stripTrailingZeros().toPlainString()
                yieldLabel = prod.yieldUnitLabel
                packagingCostStr = prod.packagingCost.stripTrailingZeros().toPlainString()
                overheadBatchStr = prod.overheadPerBatch.stripTrailingZeros().toPlainString()
                overheadUnitStr = prod.overheadPerUnit.stripTrailingZeros().toPlainString()
                overheadPercentStr = prod.overheadPercentage.stripTrailingZeros().toPlainString()

                recipeDrafts.clear()
                recipeDrafts.addAll(productWithRecipe.recipeItems.map {
                    RecipeItemDraft(
                        id = it.id,
                        ingredientId = it.ingredientId,
                        ingredientName = it.ingredientName,
                        amountUsedStr = it.amountUsed.stripTrailingZeros().toPlainString(),
                        unit = it.unit
                    )
                })
            }
        }
    }

    val onSubmit = {
        if (name.isBlank()) {
            nameError = "Nama produk wajib diisi"
        } else {
            val yieldQty = BigDecimal(yieldQtyStr.ifEmpty { "1" })
            if (yieldQty <= BigDecimal.ZERO) {
                yieldError = "Yield harus lebih dari 0"
            } else {
                val items = recipeDrafts.map { draft ->
                    RecipeItem(
                        id = draft.id,
                        productId = productId ?: "",
                        ingredientId = draft.ingredientId,
                        ingredientName = draft.ingredientName,
                        amountUsed = BigDecimal(draft.amountUsedStr.ifEmpty { "0" }),
                        unit = draft.unit
                    )
                }

                viewModel.saveProductWithRecipe(
                    id = productId,
                    name = name,
                    category = category,
                    yieldQty = yieldQty,
                    yieldLabel = yieldLabel,
                    packagingCost = BigDecimal(packagingCostStr.ifEmpty { "0" }),
                    overheadBatch = BigDecimal(overheadBatchStr.ifEmpty { "0" }),
                    overheadUnit = BigDecimal(overheadUnitStr.ifEmpty { "0" }),
                    overheadPercent = BigDecimal(overheadPercentStr.ifEmpty { "0" }),
                    recipeItems = items,
                    onSuccess = { savedId ->
                        onSavedAndNavigateToHpp(savedId)
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            KosioTopAppBar(
                title = if (productId == null) stringResource(R.string.action_create_product) else stringResource(R.string.action_edit),
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onNavigateBack
            )
        },
        bottomBar = {
            androidx.compose.material3.Surface(
                shadowElevation = 8.dp,
                color = PureWhiteSurface
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    KosioPrimaryButton(
                        text = "Hitung HPP & Simpan",
                        icon = Icons.Default.Calculate,
                        onClick = onSubmit
                    )
                }
            }
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
            // Product Info Card
            KosioCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Informasi Produk",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )

                    KosioTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = if (it.isBlank()) "Nama produk wajib diisi" else null
                        },
                        label = stringResource(R.string.product_name),
                        placeholder = stringResource(R.string.product_name_hint),
                        isError = nameError != null,
                        errorMessage = nameError
                    )

                    KosioTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = stringResource(R.string.product_category),
                        placeholder = "contoh: Dessert, Cake, Minuman"
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KosioNumberField(
                            value = yieldQtyStr,
                            onValueChange = {
                                yieldQtyStr = it
                                yieldError = if (it.isBlank() || BigDecimal(it.ifEmpty { "0" }) <= BigDecimal.ZERO) "Yield harus > 0" else null
                            },
                            label = stringResource(R.string.product_yield),
                            placeholder = "1",
                            isError = yieldError != null,
                            errorMessage = yieldError,
                            modifier = Modifier.weight(1f)
                        )
                        KosioTextField(
                            value = yieldLabel,
                            onValueChange = { yieldLabel = it },
                            label = stringResource(R.string.product_yield_label),
                            placeholder = "porsi",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Recipe Ingredients Builder Card
            KosioCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.recipe_ingredients),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                        )
                        if (availableIngredients.isNotEmpty()) {
                            IconButton(onClick = {
                                val firstIng = availableIngredients.first()
                                recipeDrafts.add(
                                    RecipeItemDraft(
                                        ingredientId = firstIng.id,
                                        ingredientName = firstIng.name,
                                        amountUsedStr = "1",
                                        unit = firstIng.baseUnit
                                    )
                                )
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah Bahan", tint = DeepIndigo)
                            }
                        }
                    }

                    if (availableIngredients.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Belum ada master bahan baku. Tambahkan bahan baku (harga beli & satuan) terlebih dahulu.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalTextSecondary),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            KosioSecondaryButton(
                                text = "+ Tambah Bahan Baku Baru",
                                icon = Icons.Default.Add,
                                onClick = onNavigateToAddIngredient
                            )
                        }
                    } else if (recipeDrafts.isEmpty()) {
                        Text(
                            text = stringResource(R.string.error_no_ingredients),
                            style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalTextMuted),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        KosioSecondaryButton(
                            text = stringResource(R.string.add_recipe_item),
                            icon = Icons.Default.Add,
                            onClick = {
                                val firstIng = availableIngredients.first()
                                recipeDrafts.add(
                                    RecipeItemDraft(
                                        ingredientId = firstIng.id,
                                        ingredientName = firstIng.name,
                                        amountUsedStr = "1",
                                        unit = firstIng.baseUnit
                                    )
                                )
                            }
                        )
                    } else {
                        recipeDrafts.forEachIndexed { index, draft ->
                            RecipeItemDraftRow(
                                draft = draft,
                                availableIngredients = availableIngredients,
                                onUpdate = { updated -> recipeDrafts[index] = updated },
                                onRemove = { recipeDrafts.removeAt(index) }
                            )
                            if (index < recipeDrafts.size - 1) {
                                HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                            }
                        }

                        KosioSecondaryButton(
                            text = stringResource(R.string.add_recipe_item),
                            icon = Icons.Default.Add,
                            onClick = {
                                val firstIng = availableIngredients.first()
                                recipeDrafts.add(
                                    RecipeItemDraft(
                                        ingredientId = firstIng.id,
                                        ingredientName = firstIng.name,
                                        amountUsedStr = "1",
                                        unit = firstIng.baseUnit
                                    )
                                )
                            }
                        )
                    }
                }
            }

            // Packaging & Overhead Card
            KosioCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Kemasan & Biaya Operasional",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )

                    KosioNumberField(
                        value = packagingCostStr,
                        onValueChange = { packagingCostStr = it },
                        label = stringResource(R.string.packaging_cost),
                        prefix = currency.symbol + " "
                    )
                    Text(
                        text = stringResource(R.string.packaging_cost_helper),
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                    )

                    KosioNumberField(
                        value = overheadBatchStr,
                        onValueChange = { overheadBatchStr = it },
                        label = stringResource(R.string.overhead_per_batch),
                        prefix = currency.symbol + " "
                    )

                    KosioNumberField(
                        value = overheadUnitStr,
                        onValueChange = { overheadUnitStr = it },
                        label = stringResource(R.string.overhead_per_unit),
                        prefix = currency.symbol + " "
                    )

                    KosioNumberField(
                        value = overheadPercentStr,
                        onValueChange = { overheadPercentStr = it },
                        label = stringResource(R.string.overhead_percentage),
                        suffix = "%"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeItemDraftRow(
    draft: RecipeItemDraft,
    availableIngredients: List<Ingredient>,
    onUpdate: (RecipeItemDraft) -> Unit,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ingredient dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.weight(1.5f)
            ) {
                OutlinedTextField(
                    value = draft.ingredientName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pilih Bahan") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    availableIngredients.forEach { ing ->
                        DropdownMenuItem(
                            text = { Text(ing.name) },
                            onClick = {
                                onUpdate(
                                    draft.copy(
                                        ingredientId = ing.id,
                                        ingredientName = ing.name,
                                        unit = ing.baseUnit
                                    )
                                )
                                expanded = false
                            }
                        )
                    }
                }
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "Hapus", tint = ErrorRed)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KosioNumberField(
                value = draft.amountUsedStr,
                onValueChange = { onUpdate(draft.copy(amountUsedStr = it)) },
                label = "Jumlah Pakai",
                modifier = Modifier.weight(1.2f)
            )
            KosioTextField(
                value = draft.unit,
                onValueChange = { onUpdate(draft.copy(unit = it)) },
                label = "Satuan",
                modifier = Modifier.weight(0.8f)
            )
        }
    }
}

@Composable
fun HppResultScreen(
    productId: String,
    viewModel: ProductViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPricing: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToHistory: (String) -> Unit,
    onNavigateToShare: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val userPrefs by viewModel.currency.collectAsState()
    val currency = userPrefs?.currency ?: CurrencyCode.IDR

    var productAndHpp by remember { mutableStateOf<Pair<Product, HppResult>?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        productAndHpp = viewModel.calculateHppForProduct(productId)
    }

    Scaffold(
        topBar = {
            KosioTopAppBar(
                title = productAndHpp?.first?.name ?: stringResource(R.string.hpp_results_title),
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onNavigateBack,
                actions = {
                    IconButton(onClick = {
                        val data = productAndHpp
                        if (data != null) {
                            try {
                                val pdfFile = com.kosio.app.data.export.ReportExportManager.generateProductPdfReport(
                                    context = context,
                                    product = data.first,
                                    hpp = data.second,
                                    currency = currency
                                )
                                com.kosio.app.data.export.ReportExportManager.shareFile(
                                    context = context,
                                    file = pdfFile,
                                    mimeType = "application/pdf",
                                    title = "Lembar Resep Kosio - ${data.first.name}"
                                )
                            } catch (e: Exception) {
                                scope.launch { snackbarHostState.showSnackbar("Gagal membuat PDF: ${e.message}") }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Ekspor PDF", tint = DeepIndigo)
                    }
                    IconButton(onClick = { onNavigateToHistory(productId) }) {
                        Icon(Icons.Default.History, contentDescription = "Riwayat", tint = DeepIndigo)
                    }
                    IconButton(onClick = { onNavigateToEdit(productId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Ubah", tint = DeepIndigo)
                    }
                    IconButton(onClick = {
                        viewModel.duplicateProduct(productId) {
                            scope.launch { snackbarHostState.showSnackbar("Produk berhasil diduplikasi") }
                        }
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplikasi", tint = DeepIndigo)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = WarmIvoryBackground
    ) { padding ->
        val data = productAndHpp
        if (data != null) {
            val (product, hpp) = data

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Missing price banner
                if (hpp.hasMissingPrice) {
                    item {
                        WarningCard(
                            message = "Ada bahan baku dalam resep yang belum memiliki harga beli.",
                            isDanger = true
                        )
                    }
                }

                // Summary Hero Card
                item {
                    KosioCard(backgroundColor = DeepIndigo) {
                        Column {
                            Text(
                                text = stringResource(R.string.unit_cost),
                                style = MaterialTheme.typography.bodySmall.copy(color = PureWhiteSurface.copy(alpha = 0.8f))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${currency.format(hpp.unitCost)} / ${product.yieldUnitLabel}",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PureWhiteSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total 1 Batch (${product.yieldQuantity.stripTrailingZeros().toPlainString()} ${product.yieldUnitLabel})",
                                    style = MaterialTheme.typography.bodySmall.copy(color = PureWhiteSurface.copy(alpha = 0.8f))
                                )
                                Text(
                                    text = currency.format(hpp.totalBatchCost),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessTeal
                                    )
                                )
                            }
                        }
                    }
                }

                // Action Row: Simulate Pricing, Save Snapshot, & Export PDF
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KosioPrimaryButton(
                            text = stringResource(R.string.action_simulate_pricing),
                            icon = Icons.Default.TrendingUp,
                            onClick = { onNavigateToPricing(productId) },
                            modifier = Modifier.weight(1.3f)
                        )
                        KosioSecondaryButton(
                            text = "Ekspor PDF",
                            onClick = {
                                try {
                                    val pdfFile = com.kosio.app.data.export.ReportExportManager.generateProductPdfReport(
                                        context = context,
                                        product = product,
                                        hpp = hpp,
                                        currency = currency
                                    )
                                    com.kosio.app.data.export.ReportExportManager.shareFile(
                                        context = context,
                                        file = pdfFile,
                                        mimeType = "application/pdf",
                                        title = "Lembar Resep Kosio - ${product.name}"
                                    )
                                } catch (e: Exception) {
                                    scope.launch { snackbarHostState.showSnackbar("Gagal membuat PDF: ${e.message}") }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Batch Cost Components Card
                item {
                    KosioCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = stringResource(R.string.batch_breakdown),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText
                                )
                            )

                            CostComponentRow(
                                label = stringResource(R.string.ingredient_cost_total),
                                amount = currency.format(hpp.totalIngredientCost)
                            )
                            CostComponentRow(
                                label = stringResource(R.string.packaging_cost_total),
                                amount = currency.format(hpp.totalPackagingCost)
                            )
                            CostComponentRow(
                                label = stringResource(R.string.overhead_cost_total),
                                amount = currency.format(hpp.totalOverheadCost)
                            )
                            HorizontalDivider(color = BorderSubtle)
                            CostComponentRow(
                                label = stringResource(R.string.total_batch_cost),
                                amount = currency.format(hpp.totalBatchCost),
                                isBold = true
                            )
                        }
                    }
                }

                // Ingredient Items Breakdown Section
                item {
                    Text(
                        text = "Rincian Biaya per Bahan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )
                }

                items(hpp.ingredientBreakdowns, key = { it.ingredientId }) { breakdown ->
                    KosioCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = breakdown.ingredientName,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalText
                                    )
                                )
                                Text(
                                    text = "${breakdown.amountUsed.stripTrailingZeros().toPlainString()} ${breakdown.unit} @ ${currency.format(breakdown.effectiveUnitPrice)}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                                )
                            }

                            Text(
                                text = currency.format(breakdown.cost),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (breakdown.hasMissingPrice) ErrorRed else DeepIndigo
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
private fun CostComponentRow(
    label: String,
    amount: String,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.bodyMedium.copy(color = CharcoalTextSecondary)
        )
        Text(
            text = amount,
            style = if (isBold) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepIndigo)
            else MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = CharcoalText)
        )
    }
}
