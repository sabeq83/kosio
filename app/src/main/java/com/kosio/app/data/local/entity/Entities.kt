package com.kosio.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "ingredients",
    indices = [Index(value = ["name"])]
)
data class IngredientEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String?,
    val unitDimension: String, // WEIGHT, VOLUME, COUNT
    val purchasePrice: String, // Stored as string for exact BigDecimal representation
    val purchaseQuantity: String,
    val purchaseUnit: String,
    val baseUnit: String,
    val customPackQuantity: String?,
    val wastePercent: String,
    val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "ingredient_prices",
    foreignKeys = [
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ingredientId"])]
)
data class IngredientPriceEntity(
    @PrimaryKey
    val id: String,
    val ingredientId: String,
    val purchasePrice: String,
    val purchaseQuantity: String,
    val purchaseUnit: String,
    val effectiveUnitPrice: String,
    val effectiveDate: Long,
    val note: String?
)

@Entity(
    tableName = "products",
    indices = [Index(value = ["name"])]
)
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String?,
    val yieldQuantity: String,
    val yieldUnitLabel: String,
    val packagingCost: String,
    val overheadPerBatch: String,
    val overheadPerUnit: String,
    val overheadPercentage: String,
    val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "product_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["productId"]),
        Index(value = ["ingredientId"])
    ]
)
data class ProductIngredientEntity(
    @PrimaryKey
    val id: String,
    val productId: String,
    val ingredientId: String,
    val amountUsed: String,
    val unit: String
)

@Entity(
    tableName = "calculation_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"])]
)
data class CalculationSnapshotEntity(
    @PrimaryKey
    val id: String,
    val productId: String,
    val productName: String,
    val yieldQuantity: String,
    val yieldUnitLabel: String,
    val totalMaterialCost: String,
    val totalPackagingCost: String,
    val totalOverheadCost: String,
    val totalBatchCost: String,
    val unitCost: String,
    val sellingPrice: String?,
    val profitPerUnit: String?,
    val marginPercent: String?,
    val markupPercent: String?,
    val breakdownJson: String,
    val createdAt: Long
)
