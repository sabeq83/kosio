package com.kosio.app.data.backup

import com.kosio.app.data.local.KosioDatabase
import com.kosio.app.data.local.entity.CalculationSnapshotEntity
import com.kosio.app.data.local.entity.IngredientEntity
import com.kosio.app.data.local.entity.IngredientPriceEntity
import com.kosio.app.data.local.entity.ProductEntity
import com.kosio.app.data.local.entity.ProductIngredientEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class BackupPayload(
    val schemaVersion: Int = 1,
    val appVersion: String = com.kosio.app.BuildConfig.VERSION_NAME,
    val exportedAt: Long = System.currentTimeMillis(),
    val ingredients: List<BackupIngredient>,
    val ingredientPrices: List<BackupIngredientPrice>,
    val products: List<BackupProduct>,
    val productIngredients: List<BackupProductIngredient>,
    val snapshots: List<BackupSnapshot>
)

@Serializable
data class BackupIngredient(
    val id: String,
    val name: String,
    val category: String? = null,
    val unitDimension: String,
    val purchasePrice: String,
    val purchaseQuantity: String,
    val purchaseUnit: String,
    val baseUnit: String,
    val customPackQuantity: String? = null,
    val wastePercent: String,
    val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class BackupIngredientPrice(
    val id: String,
    val ingredientId: String,
    val purchasePrice: String,
    val purchaseQuantity: String,
    val purchaseUnit: String,
    val effectiveUnitPrice: String,
    val effectiveDate: Long,
    val note: String? = null
)

@Serializable
data class BackupProduct(
    val id: String,
    val name: String,
    val category: String? = null,
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

@Serializable
data class BackupProductIngredient(
    val id: String,
    val productId: String,
    val ingredientId: String,
    val amountUsed: String,
    val unit: String
)

@Serializable
data class BackupSnapshot(
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
    val sellingPrice: String? = null,
    val profitPerUnit: String? = null,
    val marginPercent: String? = null,
    val markupPercent: String? = null,
    val breakdownJson: String,
    val createdAt: Long
)

data class RestoreValidationResult(
    val isValid: Boolean,
    val ingredientCount: Int = 0,
    val productCount: Int = 0,
    val snapshotCount: Int = 0,
    val exportedAt: Long = 0,
    val payload: BackupPayload? = null,
    val errorMessage: String? = null
)

class BackupManager(private val database: KosioDatabase) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    suspend fun generateBackupJson(): String = withContext(Dispatchers.IO) {
        val ingredients = database.ingredientDao().getAllIngredients().first().map {
            BackupIngredient(
                id = it.id,
                name = it.name,
                category = it.category,
                unitDimension = it.unitDimension,
                purchasePrice = it.purchasePrice,
                purchaseQuantity = it.purchaseQuantity,
                purchaseUnit = it.purchaseUnit,
                baseUnit = it.baseUnit,
                customPackQuantity = it.customPackQuantity,
                wastePercent = it.wastePercent,
                isArchived = it.isArchived,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }

        val allIngredientPrices = mutableListOf<BackupIngredientPrice>()
        for (ing in ingredients) {
            val prices = database.ingredientDao().getPriceHistory(ing.id).first()
            allIngredientPrices.addAll(prices.map {
                BackupIngredientPrice(
                    id = it.id,
                    ingredientId = it.ingredientId,
                    purchasePrice = it.purchasePrice,
                    purchaseQuantity = it.purchaseQuantity,
                    purchaseUnit = it.purchaseUnit,
                    effectiveUnitPrice = it.effectiveUnitPrice,
                    effectiveDate = it.effectiveDate,
                    note = it.note
                )
            })
        }

        val products = database.productDao().getAllProducts().first().map {
            BackupProduct(
                id = it.id,
                name = it.name,
                category = it.category,
                yieldQuantity = it.yieldQuantity,
                yieldUnitLabel = it.yieldUnitLabel,
                packagingCost = it.packagingCost,
                overheadPerBatch = it.overheadPerBatch,
                overheadPerUnit = it.overheadPerUnit,
                overheadPercentage = it.overheadPercentage,
                isArchived = it.isArchived,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }

        val allProductIngredients = mutableListOf<BackupProductIngredient>()
        for (prod in products) {
            val items = database.productDao().getRecipeItemsList(prod.id)
            allProductIngredients.addAll(items.map {
                BackupProductIngredient(
                    id = it.id,
                    productId = it.productId,
                    ingredientId = it.ingredientId,
                    amountUsed = it.amountUsed,
                    unit = it.unit
                )
            })
        }

        val snapshots = database.snapshotDao().getRecentSnapshots(1000).first().map {
            BackupSnapshot(
                id = it.id,
                productId = it.productId,
                productName = it.productName,
                yieldQuantity = it.yieldQuantity,
                yieldUnitLabel = it.yieldUnitLabel,
                totalMaterialCost = it.totalMaterialCost,
                totalPackagingCost = it.totalPackagingCost,
                totalOverheadCost = it.totalOverheadCost,
                totalBatchCost = it.totalBatchCost,
                unitCost = it.unitCost,
                sellingPrice = it.sellingPrice,
                profitPerUnit = it.profitPerUnit,
                marginPercent = it.marginPercent,
                markupPercent = it.markupPercent,
                breakdownJson = it.breakdownJson,
                createdAt = it.createdAt
            )
        }

        val payload = BackupPayload(
            ingredients = ingredients,
            ingredientPrices = allIngredientPrices,
            products = products,
            productIngredients = allProductIngredients,
            snapshots = snapshots
        )

        json.encodeToString(payload)
    }

    suspend fun exportBackup(outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val content = generateBackupJson()
        outputStream.bufferedWriter().use { it.write(content) }
    }

    fun validateBackupJson(jsonString: String): RestoreValidationResult {
        return try {
            val payload = json.decodeFromString<BackupPayload>(jsonString)
            if (payload.schemaVersion < 1) {
                RestoreValidationResult(isValid = false, errorMessage = "Unsupported backup schema version")
            } else {
                RestoreValidationResult(
                    isValid = true,
                    ingredientCount = payload.ingredients.size,
                    productCount = payload.products.size,
                    snapshotCount = payload.snapshots.size,
                    exportedAt = payload.exportedAt,
                    payload = payload
                )
            }
        } catch (e: Exception) {
            RestoreValidationResult(isValid = false, errorMessage = e.localizedMessage ?: "Invalid backup file")
        }
    }

    suspend fun restoreBackup(inputStream: InputStream): RestoreValidationResult = withContext(Dispatchers.IO) {
        try {
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val validation = validateBackupJson(jsonString)
            if (!validation.isValid || validation.payload == null) {
                return@withContext validation
            }

            val payload = validation.payload

            // Atomic database restore inside transaction
            database.runInTransaction {
                // Clear existing tables in correct order to respect foreign keys
                database.openHelper.writableDatabase.execSQL("DELETE FROM calculation_snapshots")
                database.openHelper.writableDatabase.execSQL("DELETE FROM product_ingredients")
                database.openHelper.writableDatabase.execSQL("DELETE FROM products")
                database.openHelper.writableDatabase.execSQL("DELETE FROM ingredient_prices")
                database.openHelper.writableDatabase.execSQL("DELETE FROM ingredients")

                // Insert ingredients
                for (ing in payload.ingredients) {
                    database.openHelper.writableDatabase.execSQL(
                        "INSERT INTO ingredients (id, name, category, unitDimension, purchasePrice, purchaseQuantity, purchaseUnit, baseUnit, customPackQuantity, wastePercent, isArchived, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        arrayOf(
                            ing.id, ing.name, ing.category, ing.unitDimension,
                            ing.purchasePrice, ing.purchaseQuantity, ing.purchaseUnit,
                            ing.baseUnit, ing.customPackQuantity, ing.wastePercent,
                            if (ing.isArchived) 1 else 0, ing.createdAt, ing.updatedAt
                        )
                    )
                }

                // Insert ingredient prices
                for (price in payload.ingredientPrices) {
                    database.openHelper.writableDatabase.execSQL(
                        "INSERT INTO ingredient_prices (id, ingredientId, purchasePrice, purchaseQuantity, purchaseUnit, effectiveUnitPrice, effectiveDate, note) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        arrayOf(
                            price.id, price.ingredientId, price.purchasePrice,
                            price.purchaseQuantity, price.purchaseUnit, price.effectiveUnitPrice,
                            price.effectiveDate, price.note
                        )
                    )
                }

                // Insert products
                for (prod in payload.products) {
                    database.openHelper.writableDatabase.execSQL(
                        "INSERT INTO products (id, name, category, yieldQuantity, yieldUnitLabel, packagingCost, overheadPerBatch, overheadPerUnit, overheadPercentage, isArchived, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        arrayOf(
                            prod.id, prod.name, prod.category, prod.yieldQuantity,
                            prod.yieldUnitLabel, prod.packagingCost, prod.overheadPerBatch,
                            prod.overheadPerUnit, prod.overheadPercentage,
                            if (prod.isArchived) 1 else 0, prod.createdAt, prod.updatedAt
                        )
                    )
                }

                // Insert product ingredients
                for (pi in payload.productIngredients) {
                    database.openHelper.writableDatabase.execSQL(
                        "INSERT INTO product_ingredients (id, productId, ingredientId, amountUsed, unit) VALUES (?, ?, ?, ?, ?)",
                        arrayOf(pi.id, pi.productId, pi.ingredientId, pi.amountUsed, pi.unit)
                    )
                }

                // Insert snapshots
                for (snap in payload.snapshots) {
                    database.openHelper.writableDatabase.execSQL(
                        "INSERT INTO calculation_snapshots (id, productId, productName, yieldQuantity, yieldUnitLabel, totalMaterialCost, totalPackagingCost, totalOverheadCost, totalBatchCost, unitCost, sellingPrice, profitPerUnit, marginPercent, markupPercent, breakdownJson, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        arrayOf(
                            snap.id, snap.productId, snap.productName, snap.yieldQuantity,
                            snap.yieldUnitLabel, snap.totalMaterialCost, snap.totalPackagingCost,
                            snap.totalOverheadCost, snap.totalBatchCost, snap.unitCost,
                            snap.sellingPrice, snap.profitPerUnit, snap.marginPercent,
                            snap.markupPercent, snap.breakdownJson, snap.createdAt
                        )
                    )
                }
            }

            validation
        } catch (e: Exception) {
            RestoreValidationResult(isValid = false, errorMessage = e.localizedMessage ?: "Restore execution failed")
        }
    }
}
