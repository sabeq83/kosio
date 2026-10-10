package com.kosio.app.ui.pricing

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kosio.app.R
import com.kosio.app.core.components.KosioCard
import com.kosio.app.core.components.KosioNumberField
import com.kosio.app.core.components.KosioPrimaryButton
import com.kosio.app.core.components.KosioSecondaryButton
import com.kosio.app.core.components.KosioTopAppBar
import com.kosio.app.core.components.WarningCard
import com.kosio.app.core.designsystem.BorderSubtle
import com.kosio.app.core.designsystem.CharcoalText
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
import com.kosio.app.data.preferences.UserPreferencesRepository
import com.kosio.app.domain.calculation.DiscountType
import com.kosio.app.domain.calculation.HppCalculator
import com.kosio.app.domain.calculation.PricingCalculator
import com.kosio.app.domain.calculation.PricingScenarioInput
import com.kosio.app.domain.calculation.PricingScenarioResult
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

class PricingViewModel(
    private val productRepository: ProductRepository,
    private val ingredientRepository: IngredientRepository,
    private val snapshotRepository: SnapshotRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val preferences = preferencesRepository.userPreferencesFlow

    suspend fun getProductAndCost(productId: String): Pair<Product, BigDecimal>? {
        val prodWithRecipe = productRepository.getProductWithRecipeDirect(productId) ?: return null
        val ingredients = ingredientRepository.getAllIngredients().first().associateBy { it.id }
        val hpp = HppCalculator.calculateHpp(prodWithRecipe.product, prodWithRecipe.recipeItems, ingredients)
        return Pair(prodWithRecipe.product, hpp.unitCost)
    }

    fun savePricingSnapshot(
        product: Product,
        unitCost: BigDecimal,
        result: PricingScenarioResult,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val snapshot = CalculationSnapshot(
                productId = product.id,
                productName = product.name,
                yieldQuantity = product.yieldQuantity,
                yieldUnitLabel = product.yieldUnitLabel,
                totalMaterialCost = BigDecimal.ZERO,
                totalPackagingCost = BigDecimal.ZERO,
                totalOverheadCost = BigDecimal.ZERO,
                totalBatchCost = unitCost.multiply(product.yieldQuantity),
                unitCost = unitCost,
                sellingPrice = result.listPrice,
                profitPerUnit = result.profitPerUnit,
                marginPercent = result.marginPercent,
                markupPercent = result.markupPercent,
                breakdownJson = "{\"netRevenue\":\"${result.netRevenue}\",\"discount\":\"${result.discountAmount}\",\"marketplaceFee\":\"${result.marketplaceFeeAmount}\"}",
                createdAt = System.currentTimeMillis()
            )
            snapshotRepository.saveSnapshot(snapshot)
            onDone()
        }
    }
}

@Composable
fun PricingSimulationScreen(
    productId: String,
    viewModel: PricingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToShare: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val userPrefs by viewModel.preferences.collectAsState(initial = null)
    val currency = userPrefs?.currency ?: CurrencyCode.IDR
    val roundingRule = userPrefs?.roundingRule

    var product by remember { mutableStateOf<Product?>(null) }
    var unitCost by remember { mutableStateOf(BigDecimal.ZERO) }

    var sellingPriceStr by remember { mutableStateOf("") }
    var isTargetMarkup by remember { mutableStateOf(false) }
    var targetMarginStr by remember { mutableStateOf("40") }
    var targetMarkupStr by remember { mutableStateOf("100") }

    // Multi-channel comparison calculation based on current net selling price
    val multiChannelResults = remember(sellingPriceStr, unitCost) {
        val basePrice = BigDecimal(sellingPriceStr.ifEmpty { "0" })
        PricingCalculator.calculateMultiChannelPricing(
            baseNetRevenue = basePrice,
            unitCost = unitCost
        )
    }
    var isDiscountPercentage by remember { mutableStateOf(true) }
    var discountValueStr by remember { mutableStateOf("0") }
    var marketplaceFeePercentStr by remember { mutableStateOf("0") }
    var fixedFeeStr by remember { mutableStateOf("0") }
    var shippingSubsidyStr by remember { mutableStateOf("0") }
    var additionalCostStr by remember { mutableStateOf("0") }

    LaunchedEffect(productId) {
        val data = viewModel.getProductAndCost(productId)
        if (data != null) {
            product = data.first
            unitCost = data.second
            if (sellingPriceStr.isEmpty()) {
                val initialSuggested = PricingCalculator.calculatePriceForTargetMargin(data.second, BigDecimal("40"))
                val rounded = roundingRule?.apply(initialSuggested) ?: initialSuggested
                sellingPriceStr = rounded.setScale(0, RoundingMode.HALF_UP).toPlainString()
            }
        }
    }

    val scenarioResult: PricingScenarioResult = remember(
        unitCost, sellingPriceStr, isDiscountPercentage, discountValueStr,
        marketplaceFeePercentStr, fixedFeeStr, shippingSubsidyStr, additionalCostStr
    ) {
        try {
            val listPrice = BigDecimal(sellingPriceStr.ifEmpty { "0" })
            val discountVal = BigDecimal(discountValueStr.ifEmpty { "0" })
            val marketFee = BigDecimal(marketplaceFeePercentStr.ifEmpty { "0" })
            val fixedFee = BigDecimal(fixedFeeStr.ifEmpty { "0" })
            val shipping = BigDecimal(shippingSubsidyStr.ifEmpty { "0" })
            val extraCost = BigDecimal(additionalCostStr.ifEmpty { "0" })

            PricingCalculator.simulatePricing(
                PricingScenarioInput(
                    listPrice = listPrice,
                    unitCost = unitCost,
                    discountType = if (isDiscountPercentage) DiscountType.PERCENTAGE else DiscountType.NOMINAL,
                    discountValue = discountVal,
                    marketplaceFeePercent = marketFee,
                    fixedTransactionFee = fixedFee,
                    sellerShippingSubsidy = shipping,
                    additionalSellingCost = extraCost
                )
            )
        } catch (e: Exception) {
            PricingScenarioResult(
                listPrice = BigDecimal.ZERO, unitCost = unitCost, discountAmount = BigDecimal.ZERO,
                priceAfterDiscount = BigDecimal.ZERO, marketplaceFeeAmount = BigDecimal.ZERO,
                fixedTransactionFee = BigDecimal.ZERO, sellerShippingSubsidy = BigDecimal.ZERO,
                additionalSellingCost = BigDecimal.ZERO, netRevenue = BigDecimal.ZERO,
                profitPerUnit = BigDecimal.ZERO, marginPercent = BigDecimal.ZERO,
                markupPercent = BigDecimal.ZERO, isLoss = false
            )
        }
    }

    Scaffold(
        topBar = {
            KosioTopAppBar(
                title = stringResource(R.string.pricing_simulation_title),
                subtitle = product?.name,
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { onNavigateToShare(productId) }) {
                        Icon(Icons.Default.Share, contentDescription = "Bagikan", tint = DeepIndigo)
                    }
                }
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
            // Hero Profit & Margin Card
            KosioCard(backgroundColor = if (scenarioResult.isLoss) ErrorRed else DeepIndigo) {
                Column {
                    Text(
                        text = if (scenarioResult.isLoss) "POTENSI RUGI!" else stringResource(R.string.profit_per_unit),
                        style = MaterialTheme.typography.bodySmall.copy(color = PureWhiteSurface.copy(alpha = 0.8f))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currency.format(scenarioResult.profitPerUnit) + " / " + (product?.yieldUnitLabel ?: "porsi"),
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
                        Column {
                            Text(
                                text = stringResource(R.string.margin_percent),
                                style = MaterialTheme.typography.bodySmall.copy(color = PureWhiteSurface.copy(alpha = 0.8f))
                            )
                            Text(
                                text = "${scenarioResult.marginPercent.setScale(1, RoundingMode.HALF_UP)}%",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (scenarioResult.isLoss) PureWhiteSurface else SuccessTeal
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stringResource(R.string.markup_percent),
                                style = MaterialTheme.typography.bodySmall.copy(color = PureWhiteSurface.copy(alpha = 0.8f))
                            )
                            Text(
                                text = "${scenarioResult.markupPercent.setScale(1, RoundingMode.HALF_UP)}%",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhiteSurface
                                )
                            )
                        }
                    }
                }
            }

            // Loss Warning
            if (scenarioResult.isLoss) {
                WarningCard(
                    message = stringResource(R.string.loss_warning),
                    isDanger = true
                )
            }

            // Target Margin / Markup Calculator Quick Helper Card
            KosioCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Kalkulator Cepat Target Keuntungan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DiscountToggleChip(
                            title = "Target Margin %",
                            isSelected = !isTargetMarkup,
                            onClick = { isTargetMarkup = false },
                            modifier = Modifier.weight(1f)
                        )
                        DiscountToggleChip(
                            title = "Target Markup %",
                            isSelected = isTargetMarkup,
                            onClick = { isTargetMarkup = true },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KosioNumberField(
                            value = if (isTargetMarkup) targetMarkupStr else targetMarginStr,
                            onValueChange = {
                                if (isTargetMarkup) targetMarkupStr = it else targetMarginStr = it
                            },
                            label = if (isTargetMarkup) "Target Markup (%)" else stringResource(R.string.target_margin),
                            suffix = "%",
                            modifier = Modifier.weight(1f)
                        )
                        KosioSecondaryButton(
                            text = "Terapkan",
                            onClick = {
                                try {
                                    if (isTargetMarkup) {
                                        val targetM = BigDecimal(targetMarkupStr.ifEmpty { "0" })
                                        if (targetM < BigDecimal.ZERO) {
                                            scope.launch { snackbarHostState.showSnackbar("Target markup tidak boleh negatif.") }
                                        } else {
                                            val suggested = PricingCalculator.calculatePriceForTargetMarkup(unitCost, targetM)
                                            val rounded = roundingRule?.apply(suggested) ?: suggested
                                            sellingPriceStr = rounded.setScale(0, RoundingMode.HALF_UP).toPlainString()
                                        }
                                    } else {
                                        val targetM = BigDecimal(targetMarginStr.ifEmpty { "0" })
                                        if (targetM >= BigDecimal("100")) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Target margin harus di bawah 100%. Gunakan Target Markup jika ingin untung berlipat dari modal.")
                                            }
                                        } else if (targetM < BigDecimal.ZERO) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Target margin tidak boleh negatif.")
                                            }
                                        } else {
                                            val suggested = PricingCalculator.calculatePriceForTargetMargin(unitCost, targetM)
                                            val rounded = roundingRule?.apply(suggested) ?: suggested
                                            sellingPriceStr = rounded.setScale(0, RoundingMode.HALF_UP).toPlainString()
                                        }
                                    }
                                } catch (e: Exception) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Nilai persentase tidak valid.")
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Selling Price Input Card
            KosioCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Harga Jual Menu (Base Price)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )

                    KosioNumberField(
                        value = sellingPriceStr,
                        onValueChange = { sellingPriceStr = it },
                        label = stringResource(R.string.selling_price),
                        prefix = currency.symbol + " "
                    )

                    Text(
                        text = "Modal HPP: ${currency.format(unitCost)} / ${product?.yieldUnitLabel ?: "porsi"}",
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                    )
                }
            }

            // Multi-Channel Pricing Matrix Comparison Card
            KosioCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Matriks Harga Multi-Channel",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(LightTeal)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Laba Bersih Sama",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessTeal
                                )
                            )
                        }
                    }

                    Text(
                        text = "Rekomendasi harga jual di berbagai channel agar keuntungan bersih per porsi tetap terjaga:",
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                    )

                    multiChannelResults.forEachIndexed { index, channel ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(WarmIvoryBackground)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.3f)) {
                                Text(
                                    text = channel.channelName,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DeepIndigo
                                    )
                                )
                                Text(
                                    text = if (channel.feePercentage > BigDecimal.ZERO) {
                                        "Fee: ${channel.feePercentage.stripTrailingZeros().toPlainString()}%" +
                                                if (channel.fixedFee > BigDecimal.ZERO) " + ${currency.format(channel.fixedFee)}" else ""
                                    } else "Tanpa Potongan",
                                    style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1.2f),
                                horizontalAlignment = Alignment.End
                            ) {
                                val roundedPrice = roundingRule?.apply(channel.suggestedPrice) ?: channel.suggestedPrice
                                Text(
                                    text = currency.format(roundedPrice),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalText
                                    )
                                )
                                Text(
                                    text = "Bersih: ${currency.format(channel.netRevenue)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = SuccessTeal
                                    )
                                )
                            }
                        }
                        if (index < multiChannelResults.size - 1) {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }

            // Discounts & Marketplace Promo Card
            KosioCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Diskon Promo & Marketplace",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DiscountToggleChip(
                            title = "Diskon %",
                            isSelected = isDiscountPercentage,
                            onClick = { isDiscountPercentage = true },
                            modifier = Modifier.weight(1f)
                        )
                        DiscountToggleChip(
                            title = "Diskon Nominal",
                            isSelected = !isDiscountPercentage,
                            onClick = { isDiscountPercentage = false },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    KosioNumberField(
                        value = discountValueStr,
                        onValueChange = { discountValueStr = it },
                        label = if (isDiscountPercentage) stringResource(R.string.discount_percent) else stringResource(R.string.discount_amount),
                        suffix = if (isDiscountPercentage) "%" else null,
                        prefix = if (!isDiscountPercentage) currency.symbol + " " else null
                    )

                    KosioNumberField(
                        value = marketplaceFeePercentStr,
                        onValueChange = { marketplaceFeePercentStr = it },
                        label = stringResource(R.string.marketplace_fee),
                        placeholder = "e.g. 20",
                        suffix = "%"
                    )

                    KosioNumberField(
                        value = fixedFeeStr,
                        onValueChange = { fixedFeeStr = it },
                        label = stringResource(R.string.fixed_fee),
                        placeholder = "0",
                        prefix = currency.symbol + " "
                    )

                    KosioNumberField(
                        value = shippingSubsidyStr,
                        onValueChange = { shippingSubsidyStr = it },
                        label = stringResource(R.string.shipping_subsidy),
                        placeholder = "0",
                        prefix = currency.symbol + " "
                    )

                    KosioNumberField(
                        value = additionalCostStr,
                        onValueChange = { additionalCostStr = it },
                        label = stringResource(R.string.extra_selling_costs),
                        placeholder = "0",
                        prefix = currency.symbol + " "
                    )
                }
            }

            // Net Revenue Breakdown Card
            KosioCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Rincian Pendapatan Bersih",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Harga Setelah Diskon", style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalTextSecondary))
                        Text(text = currency.format(scenarioResult.priceAfterDiscount), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    }

                    if (scenarioResult.marketplaceFeeAmount > BigDecimal.ZERO) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Potongan Marketplace", style = MaterialTheme.typography.bodyMedium.copy(color = ErrorRed))
                            Text(text = "- ${currency.format(scenarioResult.marketplaceFeeAmount)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = ErrorRed))
                        }
                    }

                    if (scenarioResult.fixedTransactionFee > BigDecimal.ZERO) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Biaya Transaksi", style = MaterialTheme.typography.bodyMedium.copy(color = ErrorRed))
                            Text(text = "- ${currency.format(scenarioResult.fixedTransactionFee)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = ErrorRed))
                        }
                    }

                    if (scenarioResult.sellerShippingSubsidy > BigDecimal.ZERO) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Subsidi Ongkir", style = MaterialTheme.typography.bodyMedium.copy(color = ErrorRed))
                            Text(text = "- ${currency.format(scenarioResult.sellerShippingSubsidy)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = ErrorRed))
                        }
                    }

                    HorizontalDivider(color = BorderSubtle)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = stringResource(R.string.net_revenue), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(text = currency.format(scenarioResult.netRevenue), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepIndigo))
                    }
                }
            }

            // Save Scenario Button
            KosioPrimaryButton(
                text = stringResource(R.string.save_scenario),
                icon = Icons.Default.History,
                onClick = {
                    val prod = product
                    if (prod != null) {
                        viewModel.savePricingSnapshot(prod, unitCost, scenarioResult) {
                            scope.launch { snackbarHostState.showSnackbar("Skenario harga berhasil disimpan ke riwayat") }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun DiscountToggleChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) DeepIndigo else WarmIvoryBackground)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) PureWhiteSurface else CharcoalTextSecondary
            )
        )
    }
}
