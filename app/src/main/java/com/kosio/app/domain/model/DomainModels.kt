package com.kosio.app.domain.model

import java.math.BigDecimal
import java.util.UUID

data class Ingredient(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String? = null,
    val unitDimension: UnitDimension = UnitDimension.WEIGHT,
    val purchasePrice: BigDecimal,
    val purchaseQuantity: BigDecimal,
    val purchaseUnit: String,
    val baseUnit: String,
    val customPackQuantity: BigDecimal? = null, // e.g. 50 for 1 pack = 50 pcs
    val wastePercent: BigDecimal = BigDecimal.ZERO, // 0 to 99.99%
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class IngredientPriceHistory(
    val id: String = UUID.randomUUID().toString(),
    val ingredientId: String,
    val purchasePrice: BigDecimal,
    val purchaseQuantity: BigDecimal,
    val purchaseUnit: String,
    val effectiveUnitPrice: BigDecimal,
    val effectiveDate: Long = System.currentTimeMillis(),
    val note: String? = null
)

data class Product(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String? = null,
    val yieldQuantity: BigDecimal = BigDecimal.ONE, // Number of sellable items per batch
    val yieldUnitLabel: String = "pcs", // porsi, pcs, cup, box
    val packagingCost: BigDecimal = BigDecimal.ZERO, // Total packaging cost per batch
    val overheadPerBatch: BigDecimal = BigDecimal.ZERO, // Electricity, gas, water
    val overheadPerUnit: BigDecimal = BigDecimal.ZERO, // Labor per unit
    val overheadPercentage: BigDecimal = BigDecimal.ZERO, // % of total material cost
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class RecipeItem(
    val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val ingredientId: String,
    val ingredientName: String = "",
    val amountUsed: BigDecimal,
    val unit: String,
    val effectiveUnitPrice: BigDecimal = BigDecimal.ZERO,
    val calculatedCost: BigDecimal = BigDecimal.ZERO
)

data class ProductWithRecipe(
    val product: Product,
    val recipeItems: List<RecipeItem>
)

data class CalculationSnapshot(
    val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val productName: String,
    val yieldQuantity: BigDecimal,
    val yieldUnitLabel: String,
    val totalMaterialCost: BigDecimal,
    val totalPackagingCost: BigDecimal,
    val totalOverheadCost: BigDecimal,
    val totalBatchCost: BigDecimal,
    val unitCost: BigDecimal,
    val sellingPrice: BigDecimal? = null,
    val profitPerUnit: BigDecimal? = null,
    val marginPercent: BigDecimal? = null,
    val markupPercent: BigDecimal? = null,
    val breakdownJson: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
