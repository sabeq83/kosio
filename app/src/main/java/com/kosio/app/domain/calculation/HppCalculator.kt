package com.kosio.app.domain.calculation

import com.kosio.app.domain.model.Ingredient
import com.kosio.app.domain.model.Product
import com.kosio.app.domain.model.RecipeItem
import com.kosio.app.domain.model.StandardUnit
import com.kosio.app.domain.model.UnitConverter
import java.math.BigDecimal
import java.math.RoundingMode

data class IngredientCostBreakdown(
    val ingredientId: String,
    val ingredientName: String,
    val amountUsed: BigDecimal,
    val unit: String,
    val effectiveUnitPrice: BigDecimal, // Per base unit
    val cost: BigDecimal,
    val hasMissingPrice: Boolean
)

data class HppResult(
    val totalIngredientCost: BigDecimal,
    val totalPackagingCost: BigDecimal,
    val totalOverheadCost: BigDecimal,
    val overheadBatchComponent: BigDecimal,
    val overheadUnitComponent: BigDecimal,
    val overheadPercentageComponent: BigDecimal,
    val totalBatchCost: BigDecimal,
    val yieldQuantity: BigDecimal,
    val yieldUnitLabel: String,
    val unitCost: BigDecimal,
    val ingredientBreakdowns: List<IngredientCostBreakdown>,
    val hasMissingPrice: Boolean
)

object HppCalculator {

    private val HUNDRED = BigDecimal("100")
    private val SIX_DECIMALS = 6

    /**
     * Calculates the effective purchase price per base unit after factoring in waste/shrinkage.
     * Formula:
     * yieldFactor = 1 - (wastePercent / 100)
     * purchaseUnitPrice = purchasePrice / purchaseQuantityInBaseUnit
     * effectiveUnitPrice = purchaseUnitPrice / yieldFactor
     */
    fun calculateEffectiveUnitPrice(
        purchasePrice: BigDecimal,
        purchaseQuantity: BigDecimal,
        purchaseUnit: String,
        customPackQuantity: BigDecimal? = null,
        wastePercent: BigDecimal = BigDecimal.ZERO
    ): BigDecimal {
        if (purchasePrice <= BigDecimal.ZERO || purchaseQuantity <= BigDecimal.ZERO) {
            return BigDecimal.ZERO
        }
        if (wastePercent >= HUNDRED) {
            throw IllegalArgumentException("Waste percent must be strictly less than 100%")
        }
        if (wastePercent < BigDecimal.ZERO) {
            throw IllegalArgumentException("Waste percent cannot be negative")
        }

        // Convert purchase quantity to base unit (e.g. 1 kg -> 1000 g, 1 box -> 50 pcs)
        val (baseQuantity, _) = UnitConverter.convertToBase(purchaseQuantity, purchaseUnit, customPackQuantity)
        if (baseQuantity <= BigDecimal.ZERO) {
            return BigDecimal.ZERO
        }

        val nominalUnitPrice = purchasePrice.divide(baseQuantity, 10, RoundingMode.HALF_UP)
        val yieldFactor = BigDecimal.ONE.subtract(wastePercent.divide(HUNDRED, 10, RoundingMode.HALF_UP))

        return nominalUnitPrice.divide(yieldFactor, 10, RoundingMode.HALF_UP)
    }

    /**
     * Calculates single ingredient item cost in recipe.
     */
    fun calculateIngredientItemCost(
        ingredient: Ingredient,
        amountUsed: BigDecimal,
        recipeUnit: String
    ): Pair<BigDecimal, BigDecimal> {
        val effectiveUnitPrice = calculateEffectiveUnitPrice(
            purchasePrice = ingredient.purchasePrice,
            purchaseQuantity = ingredient.purchaseQuantity,
            purchaseUnit = ingredient.purchaseUnit,
            customPackQuantity = ingredient.customPackQuantity,
            wastePercent = ingredient.wastePercent
        )

        val (amountUsedInBaseUnit, _) = UnitConverter.convertToBase(
            amount = amountUsed,
            unitCode = recipeUnit,
            customPackQuantity = ingredient.customPackQuantity
        )

        val cost = amountUsedInBaseUnit.multiply(effectiveUnitPrice).setScale(4, RoundingMode.HALF_UP)
        return Pair(cost, effectiveUnitPrice)
    }

    /**
     * Calculates complete HPP for a product given its recipe items and ingredients map.
     */
    fun calculateHpp(
        product: Product,
        recipeItems: List<RecipeItem>,
        ingredientMap: Map<String, Ingredient>
    ): HppResult {
        if (product.yieldQuantity <= BigDecimal.ZERO) {
            throw IllegalArgumentException("Production yield must be greater than 0")
        }

        var totalIngredientCost = BigDecimal.ZERO
        var hasMissingPrice = false
        val breakdowns = mutableListOf<IngredientCostBreakdown>()

        for (item in recipeItems) {
            val ingredient = ingredientMap[item.ingredientId]
            if (ingredient == null || ingredient.purchasePrice <= BigDecimal.ZERO) {
                hasMissingPrice = true
                breakdowns.add(
                    IngredientCostBreakdown(
                        ingredientId = item.ingredientId,
                        ingredientName = ingredient?.name ?: item.ingredientName,
                        amountUsed = item.amountUsed,
                        unit = item.unit,
                        effectiveUnitPrice = BigDecimal.ZERO,
                        cost = BigDecimal.ZERO,
                        hasMissingPrice = true
                    )
                )
            } else {
                val (itemCost, effectiveUnitCost) = calculateIngredientItemCost(
                    ingredient = ingredient,
                    amountUsed = item.amountUsed,
                    recipeUnit = item.unit
                )
                totalIngredientCost = totalIngredientCost.add(itemCost)
                breakdowns.add(
                    IngredientCostBreakdown(
                        ingredientId = item.ingredientId,
                        ingredientName = ingredient.name,
                        amountUsed = item.amountUsed,
                        unit = item.unit,
                        effectiveUnitPrice = effectiveUnitCost.setScale(4, RoundingMode.HALF_UP),
                        cost = itemCost,
                        hasMissingPrice = false
                    )
                )
            }
        }

        val packagingCost = product.packagingCost.max(BigDecimal.ZERO)

        // Overhead calculation
        val overheadBatchComponent = product.overheadPerBatch.max(BigDecimal.ZERO)
        val overheadUnitComponent = product.overheadPerUnit.max(BigDecimal.ZERO).multiply(product.yieldQuantity)
        val overheadPercentageComponent = if (product.overheadPercentage > BigDecimal.ZERO) {
            totalIngredientCost.multiply(product.overheadPercentage.divide(HUNDRED, 10, RoundingMode.HALF_UP))
        } else {
            BigDecimal.ZERO
        }

        val totalOverheadCost = overheadBatchComponent
            .add(overheadUnitComponent)
            .add(overheadPercentageComponent)
            .setScale(4, RoundingMode.HALF_UP)

        val totalBatchCost = totalIngredientCost
            .add(packagingCost)
            .add(totalOverheadCost)
            .setScale(4, RoundingMode.HALF_UP)

        val unitCost = totalBatchCost.divide(product.yieldQuantity, 4, RoundingMode.HALF_UP)

        return HppResult(
            totalIngredientCost = totalIngredientCost.setScale(2, RoundingMode.HALF_UP),
            totalPackagingCost = packagingCost.setScale(2, RoundingMode.HALF_UP),
            totalOverheadCost = totalOverheadCost.setScale(2, RoundingMode.HALF_UP),
            overheadBatchComponent = overheadBatchComponent.setScale(2, RoundingMode.HALF_UP),
            overheadUnitComponent = overheadUnitComponent.setScale(2, RoundingMode.HALF_UP),
            overheadPercentageComponent = overheadPercentageComponent.setScale(2, RoundingMode.HALF_UP),
            totalBatchCost = totalBatchCost.setScale(2, RoundingMode.HALF_UP),
            yieldQuantity = product.yieldQuantity,
            yieldUnitLabel = product.yieldUnitLabel,
            unitCost = unitCost.setScale(2, RoundingMode.HALF_UP),
            ingredientBreakdowns = breakdowns,
            hasMissingPrice = hasMissingPrice
        )
    }
}
