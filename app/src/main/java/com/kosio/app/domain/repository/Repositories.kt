package com.kosio.app.domain.repository

import com.kosio.app.domain.model.CalculationSnapshot
import com.kosio.app.domain.model.Ingredient
import com.kosio.app.domain.model.IngredientPriceHistory
import com.kosio.app.domain.model.Product
import com.kosio.app.domain.model.ProductWithRecipe
import com.kosio.app.domain.model.RecipeItem
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    fun getAllActiveIngredients(): Flow<List<Ingredient>>
    fun getAllIngredients(): Flow<List<Ingredient>>
    fun getIngredient(id: String): Flow<Ingredient?>
    suspend fun getIngredientById(id: String): Ingredient?
    suspend fun saveIngredient(ingredient: Ingredient, priceChangeNote: String? = null)
    suspend fun updatePrice(ingredientId: String, newPrice: IngredientPriceHistory)
    suspend fun setArchived(id: String, isArchived: Boolean)
    suspend fun deleteIngredient(id: String): Boolean
    fun getPriceHistory(ingredientId: String): Flow<List<IngredientPriceHistory>>
    suspend fun countProductUsage(ingredientId: String): Int
    suspend fun getActiveProductsUsingIngredient(ingredientId: String): List<Product>
}

interface ProductRepository {
    fun getAllActiveProducts(): Flow<List<Product>>
    fun getAllProducts(): Flow<List<Product>>
    fun getProductWithRecipe(id: String): Flow<ProductWithRecipe?>
    suspend fun getProductWithRecipeDirect(id: String): ProductWithRecipe?
    suspend fun saveProductWithRecipe(product: Product, recipeItems: List<RecipeItem>)
    suspend fun setArchived(id: String, isArchived: Boolean)
    suspend fun deleteProduct(id: String)
    suspend fun duplicateProduct(productId: String, newName: String): String
}

interface SnapshotRepository {
    suspend fun saveSnapshot(snapshot: CalculationSnapshot)
    fun getSnapshotsForProduct(productId: String): Flow<List<CalculationSnapshot>>
    fun getRecentSnapshots(limit: Int = 10): Flow<List<CalculationSnapshot>>
    suspend fun deleteSnapshot(id: String)
}
