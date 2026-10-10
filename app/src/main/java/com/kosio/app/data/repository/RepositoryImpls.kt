package com.kosio.app.data.repository

import com.kosio.app.data.local.KosioDatabase
import com.kosio.app.data.local.entity.CalculationSnapshotEntity
import com.kosio.app.data.local.entity.IngredientEntity
import com.kosio.app.data.local.entity.IngredientPriceEntity
import com.kosio.app.data.local.entity.ProductEntity
import com.kosio.app.data.local.entity.ProductIngredientEntity
import com.kosio.app.domain.calculation.HppCalculator
import com.kosio.app.domain.model.CalculationSnapshot
import com.kosio.app.domain.model.Ingredient
import com.kosio.app.domain.model.IngredientPriceHistory
import com.kosio.app.domain.model.Product
import com.kosio.app.domain.model.ProductWithRecipe
import com.kosio.app.domain.model.RecipeItem
import com.kosio.app.domain.model.UnitDimension
import com.kosio.app.domain.repository.IngredientRepository
import com.kosio.app.domain.repository.ProductRepository
import com.kosio.app.domain.repository.SnapshotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.util.UUID

class IngredientRepositoryImpl(private val database: KosioDatabase) : IngredientRepository {

    private val dao = database.ingredientDao()

    override fun getAllActiveIngredients(): Flow<List<Ingredient>> {
        return dao.getAllActiveIngredients().map { list -> list.map { it.toDomain() } }
    }

    override fun getAllIngredients(): Flow<List<Ingredient>> {
        return dao.getAllIngredients().map { list -> list.map { it.toDomain() } }
    }

    override fun getIngredient(id: String): Flow<Ingredient?> {
        return dao.getIngredientFlow(id).map { it?.toDomain() }
    }

    override suspend fun getIngredientById(id: String): Ingredient? {
        return dao.getIngredientById(id)?.toDomain()
    }

    override suspend fun saveIngredient(ingredient: Ingredient, priceChangeNote: String?) {
        val entity = ingredient.toEntity()
        val existing = dao.getIngredientById(ingredient.id)
        dao.insertIngredient(entity)

        // If new or price changed, record price history
        if (existing == null || existing.purchasePrice != ingredient.purchasePrice.toPlainString()) {
            val effectiveUnitPrice = HppCalculator.calculateEffectiveUnitPrice(
                purchasePrice = ingredient.purchasePrice,
                purchaseQuantity = ingredient.purchaseQuantity,
                purchaseUnit = ingredient.purchaseUnit,
                customPackQuantity = ingredient.customPackQuantity,
                wastePercent = ingredient.wastePercent
            )
            dao.insertPriceHistory(
                IngredientPriceEntity(
                    id = UUID.randomUUID().toString(),
                    ingredientId = ingredient.id,
                    purchasePrice = ingredient.purchasePrice.toPlainString(),
                    purchaseQuantity = ingredient.purchaseQuantity.toPlainString(),
                    purchaseUnit = ingredient.purchaseUnit,
                    effectiveUnitPrice = effectiveUnitPrice.toPlainString(),
                    effectiveDate = System.currentTimeMillis(),
                    note = priceChangeNote ?: if (existing == null) "Initial price" else "Updated price"
                )
            )
        }
    }

    override suspend fun updatePrice(ingredientId: String, newPrice: IngredientPriceHistory) {
        val ingredient = dao.getIngredientById(ingredientId) ?: return
        val updated = ingredient.copy(
            purchasePrice = newPrice.purchasePrice.toPlainString(),
            purchaseQuantity = newPrice.purchaseQuantity.toPlainString(),
            purchaseUnit = newPrice.purchaseUnit,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateIngredient(updated)
        dao.insertPriceHistory(
            IngredientPriceEntity(
                id = newPrice.id,
                ingredientId = ingredientId,
                purchasePrice = newPrice.purchasePrice.toPlainString(),
                purchaseQuantity = newPrice.purchaseQuantity.toPlainString(),
                purchaseUnit = newPrice.purchaseUnit,
                effectiveUnitPrice = newPrice.effectiveUnitPrice.toPlainString(),
                effectiveDate = newPrice.effectiveDate,
                note = newPrice.note
            )
        )
    }

    override suspend fun setArchived(id: String, isArchived: Boolean) {
        dao.setArchivedStatus(id, isArchived, System.currentTimeMillis())
    }

    override suspend fun deleteIngredient(id: String): Boolean {
        val usageCount = dao.countProductUsage(id)
        if (usageCount > 0) {
            return false // Cannot delete if in use, must archive
        }
        dao.deleteIngredientById(id)
        return true
    }

    override fun getPriceHistory(ingredientId: String): Flow<List<IngredientPriceHistory>> {
        return dao.getPriceHistory(ingredientId).map { list ->
            list.map {
                IngredientPriceHistory(
                    id = it.id,
                    ingredientId = it.ingredientId,
                    purchasePrice = BigDecimal(it.purchasePrice),
                    purchaseQuantity = BigDecimal(it.purchaseQuantity),
                    purchaseUnit = it.purchaseUnit,
                    effectiveUnitPrice = BigDecimal(it.effectiveUnitPrice),
                    effectiveDate = it.effectiveDate,
                    note = it.note
                )
            }
        }
    }

    override suspend fun countProductUsage(ingredientId: String): Int {
        return dao.countProductUsage(ingredientId)
    }

    override suspend fun getActiveProductsUsingIngredient(ingredientId: String): List<Product> {
        return dao.getActiveProductsUsingIngredient(ingredientId).map { it.toDomain() }
    }
}

class ProductRepositoryImpl(private val database: KosioDatabase) : ProductRepository {

    private val dao = database.productDao()
    private val ingredientDao = database.ingredientDao()

    override fun getAllActiveProducts(): Flow<List<Product>> {
        return dao.getAllActiveProducts().map { list -> list.map { it.toDomain() } }
    }

    override fun getAllProducts(): Flow<List<Product>> {
        return dao.getAllProducts().map { list -> list.map { it.toDomain() } }
    }

    override fun getProductWithRecipe(id: String): Flow<ProductWithRecipe?> {
        return combine(
            dao.getProductFlow(id),
            dao.getRecipeItemsForProduct(id),
            ingredientDao.getAllIngredients()
        ) { productEntity, itemEntities, ingredientsList ->
            if (productEntity == null) return@combine null
            val ingredientMap = ingredientsList.associateBy { it.id }
            val items = itemEntities.map { item ->
                val ing = ingredientMap[item.ingredientId]
                RecipeItem(
                    id = item.id,
                    productId = item.productId,
                    ingredientId = item.ingredientId,
                    ingredientName = ing?.name ?: "",
                    amountUsed = BigDecimal(item.amountUsed),
                    unit = item.unit
                )
            }
            ProductWithRecipe(product = productEntity.toDomain(), recipeItems = items)
        }
    }

    override suspend fun getProductWithRecipeDirect(id: String): ProductWithRecipe? {
        val productEntity = dao.getProductById(id) ?: return null
        val itemEntities = dao.getRecipeItemsList(id)
        val items = itemEntities.map { item ->
            val ing = ingredientDao.getIngredientById(item.ingredientId)
            RecipeItem(
                id = item.id,
                productId = item.productId,
                ingredientId = item.ingredientId,
                ingredientName = ing?.name ?: "",
                amountUsed = BigDecimal(item.amountUsed),
                unit = item.unit
            )
        }
        return ProductWithRecipe(product = productEntity.toDomain(), recipeItems = items)
    }

    override suspend fun saveProductWithRecipe(product: Product, recipeItems: List<RecipeItem>) {
        val productEntity = product.toEntity()
        val itemEntities = recipeItems.map {
            ProductIngredientEntity(
                id = it.id,
                productId = product.id,
                ingredientId = it.ingredientId,
                amountUsed = it.amountUsed.toPlainString(),
                unit = it.unit
            )
        }
        dao.saveProductWithRecipe(productEntity, itemEntities)
    }

    override suspend fun setArchived(id: String, isArchived: Boolean) {
        dao.setArchivedStatus(id, isArchived, System.currentTimeMillis())
    }

    override suspend fun deleteProduct(id: String) {
        dao.deleteProductById(id)
    }

    override suspend fun duplicateProduct(productId: String, newName: String): String {
        val existing = getProductWithRecipeDirect(productId) ?: return ""
        val newId = UUID.randomUUID().toString()
        val duplicatedProduct = existing.product.copy(
            id = newId,
            name = newName,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val duplicatedItems = existing.recipeItems.map {
            it.copy(id = UUID.randomUUID().toString(), productId = newId)
        }
        saveProductWithRecipe(duplicatedProduct, duplicatedItems)
        return newId
    }
}

class SnapshotRepositoryImpl(private val database: KosioDatabase) : SnapshotRepository {

    private val dao = database.snapshotDao()

    override suspend fun saveSnapshot(snapshot: CalculationSnapshot) {
        dao.insertSnapshot(
            CalculationSnapshotEntity(
                id = snapshot.id,
                productId = snapshot.productId,
                productName = snapshot.productName,
                yieldQuantity = snapshot.yieldQuantity.toPlainString(),
                yieldUnitLabel = snapshot.yieldUnitLabel,
                totalMaterialCost = snapshot.totalMaterialCost.toPlainString(),
                totalPackagingCost = snapshot.totalPackagingCost.toPlainString(),
                totalOverheadCost = snapshot.totalOverheadCost.toPlainString(),
                totalBatchCost = snapshot.totalBatchCost.toPlainString(),
                unitCost = snapshot.unitCost.toPlainString(),
                sellingPrice = snapshot.sellingPrice?.toPlainString(),
                profitPerUnit = snapshot.profitPerUnit?.toPlainString(),
                marginPercent = snapshot.marginPercent?.toPlainString(),
                markupPercent = snapshot.markupPercent?.toPlainString(),
                breakdownJson = snapshot.breakdownJson,
                createdAt = snapshot.createdAt
            )
        )
    }

    override fun getSnapshotsForProduct(productId: String): Flow<List<CalculationSnapshot>> {
        return dao.getSnapshotsForProduct(productId).map { list -> list.map { it.toDomain() } }
    }

    override fun getRecentSnapshots(limit: Int): Flow<List<CalculationSnapshot>> {
        return dao.getRecentSnapshots(limit).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun deleteSnapshot(id: String) {
        dao.deleteSnapshotById(id)
    }
}

// Entity Mappers
fun IngredientEntity.toDomain() = Ingredient(
    id = id,
    name = name,
    category = category,
    unitDimension = UnitDimension.valueOf(unitDimension),
    purchasePrice = BigDecimal(purchasePrice),
    purchaseQuantity = BigDecimal(purchaseQuantity),
    purchaseUnit = purchaseUnit,
    baseUnit = baseUnit,
    customPackQuantity = customPackQuantity?.let { BigDecimal(it) },
    wastePercent = BigDecimal(wastePercent),
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Ingredient.toEntity() = IngredientEntity(
    id = id,
    name = name,
    category = category,
    unitDimension = unitDimension.name,
    purchasePrice = purchasePrice.toPlainString(),
    purchaseQuantity = purchaseQuantity.toPlainString(),
    purchaseUnit = purchaseUnit,
    baseUnit = baseUnit,
    customPackQuantity = customPackQuantity?.toPlainString(),
    wastePercent = wastePercent.toPlainString(),
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ProductEntity.toDomain() = Product(
    id = id,
    name = name,
    category = category,
    yieldQuantity = BigDecimal(yieldQuantity),
    yieldUnitLabel = yieldUnitLabel,
    packagingCost = BigDecimal(packagingCost),
    overheadPerBatch = BigDecimal(overheadPerBatch),
    overheadPerUnit = BigDecimal(overheadPerUnit),
    overheadPercentage = BigDecimal(overheadPercentage),
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Product.toEntity() = ProductEntity(
    id = id,
    name = name,
    category = category,
    yieldQuantity = yieldQuantity.toPlainString(),
    yieldUnitLabel = yieldUnitLabel,
    packagingCost = packagingCost.toPlainString(),
    overheadPerBatch = overheadPerBatch.toPlainString(),
    overheadPerUnit = overheadPerUnit.toPlainString(),
    overheadPercentage = overheadPercentage.toPlainString(),
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CalculationSnapshotEntity.toDomain() = CalculationSnapshot(
    id = id,
    productId = productId,
    productName = productName,
    yieldQuantity = BigDecimal(yieldQuantity),
    yieldUnitLabel = yieldUnitLabel,
    totalMaterialCost = BigDecimal(totalMaterialCost),
    totalPackagingCost = BigDecimal(totalPackagingCost),
    totalOverheadCost = BigDecimal(totalOverheadCost),
    totalBatchCost = BigDecimal(totalBatchCost),
    unitCost = BigDecimal(unitCost),
    sellingPrice = sellingPrice?.let { BigDecimal(it) },
    profitPerUnit = profitPerUnit?.let { BigDecimal(it) },
    marginPercent = marginPercent?.let { BigDecimal(it) },
    markupPercent = markupPercent?.let { BigDecimal(it) },
    breakdownJson = breakdownJson,
    createdAt = createdAt
)
