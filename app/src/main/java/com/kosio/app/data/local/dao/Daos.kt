package com.kosio.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kosio.app.data.local.entity.CalculationSnapshotEntity
import com.kosio.app.data.local.entity.IngredientEntity
import com.kosio.app.data.local.entity.IngredientPriceEntity
import com.kosio.app.data.local.entity.ProductEntity
import com.kosio.app.data.local.entity.ProductIngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllActiveIngredients(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getIngredientById(id: String): IngredientEntity?

    @Query("SELECT * FROM ingredients WHERE id = :id")
    fun getIngredientFlow(id: String): Flow<IngredientEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity)

    @Update
    suspend fun updateIngredient(ingredient: IngredientEntity)

    @Query("UPDATE ingredients SET isArchived = :isArchived, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setArchivedStatus(id: String, isArchived: Boolean, updatedAt: Long)

    @Query("DELETE FROM ingredients WHERE id = :id")
    suspend fun deleteIngredientById(id: String)

    // Price History
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(price: IngredientPriceEntity)

    @Query("SELECT * FROM ingredient_prices WHERE ingredientId = :ingredientId ORDER BY effectiveDate DESC")
    fun getPriceHistory(ingredientId: String): Flow<List<IngredientPriceEntity>>

    // Check usage in products
    @Query("SELECT COUNT(*) FROM product_ingredients WHERE ingredientId = :ingredientId")
    suspend fun countProductUsage(ingredientId: String): Int

    @Query("SELECT p.* FROM products p INNER JOIN product_ingredients pi ON p.id = pi.productId WHERE pi.ingredientId = :ingredientId AND p.isArchived = 0")
    suspend fun getActiveProductsUsingIngredient(ingredientId: String): List<ProductEntity>
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun getAllActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY updatedAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductFlow(id: String): Flow<ProductEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET isArchived = :isArchived, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setArchivedStatus(id: String, isArchived: Boolean, updatedAt: Long)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: String)

    // Recipe Items
    @Query("SELECT * FROM product_ingredients WHERE productId = :productId")
    fun getRecipeItemsForProduct(productId: String): Flow<List<ProductIngredientEntity>>

    @Query("SELECT * FROM product_ingredients WHERE productId = :productId")
    suspend fun getRecipeItemsList(productId: String): List<ProductIngredientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeItems(items: List<ProductIngredientEntity>)

    @Query("DELETE FROM product_ingredients WHERE productId = :productId")
    suspend fun deleteRecipeItemsForProduct(productId: String)

    @Transaction
    suspend fun saveProductWithRecipe(product: ProductEntity, items: List<ProductIngredientEntity>) {
        insertProduct(product)
        deleteRecipeItemsForProduct(product.id)
        insertRecipeItems(items)
    }
}

@Dao
interface SnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: CalculationSnapshotEntity)

    @Query("SELECT * FROM calculation_snapshots WHERE productId = :productId ORDER BY createdAt DESC")
    fun getSnapshotsForProduct(productId: String): Flow<List<CalculationSnapshotEntity>>

    @Query("SELECT * FROM calculation_snapshots ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentSnapshots(limit: Int = 10): Flow<List<CalculationSnapshotEntity>>

    @Query("DELETE FROM calculation_snapshots WHERE id = :id")
    suspend fun deleteSnapshotById(id: String)
}
