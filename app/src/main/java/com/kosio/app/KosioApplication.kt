package com.kosio.app

import android.app.Application
import android.util.Log
import com.kosio.app.data.ads.AdMobManager
import com.kosio.app.data.ads.OcrQuotaManager
import com.kosio.app.data.backup.BackupManager
import com.kosio.app.data.local.KosioDatabase
import com.kosio.app.data.local.entity.ProductEntity
import com.kosio.app.data.local.entity.ProductIngredientEntity
import com.kosio.app.data.preferences.UserPreferencesRepository
import com.kosio.app.data.repository.IngredientRepositoryImpl
import com.kosio.app.data.repository.ProductRepositoryImpl
import com.kosio.app.data.repository.SnapshotRepositoryImpl
import com.kosio.app.domain.model.Ingredient
import com.kosio.app.domain.model.Product
import com.kosio.app.domain.repository.IngredientRepository
import com.kosio.app.domain.repository.ProductRepository
import com.kosio.app.domain.repository.SnapshotRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class KosioApplication : Application() {

    lateinit var database: KosioDatabase
        private set

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    lateinit var ingredientRepository: IngredientRepository
        private set

    lateinit var productRepository: ProductRepository
        private set

    lateinit var snapshotRepository: SnapshotRepository
        private set

    lateinit var backupManager: BackupManager
        private set

    lateinit var ocrQuotaManager: OcrQuotaManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = KosioDatabase.getDatabase(this)
        userPreferencesRepository = UserPreferencesRepository(this)
        ingredientRepository = IngredientRepositoryImpl(database)
        productRepository = ProductRepositoryImpl(database)
        snapshotRepository = SnapshotRepositoryImpl(database)
        backupManager = BackupManager(database)
        ocrQuotaManager = OcrQuotaManager(this)

        // Initialize Google Mobile Ads SDK (AdMob)
        AdMobManager.initialize(this)

        seedSampleProductsIfNeeded()
    }

    private fun seedSampleProductsIfNeeded() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existing: List<Product> = productRepository.getAllProducts().first()
                if (existing.size < 3) {
                    val ingredients: List<Ingredient> = ingredientRepository.getAllIngredients().first()
                    val malkist = ingredients.find { it.name.contains("Gabin", true) || it.name.contains("Malkist", true) }
                    val skm = ingredients.find { it.name.contains("Kental", true) }
                    val uht = ingredients.find { it.name.contains("UHT", true) }

                    if (existing.none { it.name.contains("Es Gabin", true) }) {
                        val p1Id = UUID.randomUUID().toString()
                        val now = System.currentTimeMillis()
                        val p1 = ProductEntity(
                            id = p1Id,
                            name = "Es Gabin Vla Susu",
                            category = "Dessert",
                            yieldQuantity = "10",
                            yieldUnitLabel = "porsi",
                            packagingCost = "500",
                            overheadPerBatch = "2000",
                            overheadPerUnit = "0",
                            overheadPercentage = "0",
                            createdAt = now,
                            updatedAt = now
                        )
                        val p1Items = mutableListOf<ProductIngredientEntity>()
                        if (malkist != null) {
                            p1Items.add(ProductIngredientEntity(UUID.randomUUID().toString(), p1Id, malkist.id, "10", "pcs"))
                        }
                        if (skm != null) {
                            p1Items.add(ProductIngredientEntity(UUID.randomUUID().toString(), p1Id, skm.id, "50", "g"))
                        }
                        if (uht != null) {
                            p1Items.add(ProductIngredientEntity(UUID.randomUUID().toString(), p1Id, uht.id, "250", "ml"))
                        }
                        database.productDao().saveProductWithRecipe(p1, p1Items)
                    }

                    if (existing.none { it.name.contains("Puding Susu", true) }) {
                        val p2Id = UUID.randomUUID().toString()
                        val now = System.currentTimeMillis()
                        val p2 = ProductEntity(
                            id = p2Id,
                            name = "Puding Susu Creamy",
                            category = "Dessert",
                            yieldQuantity = "5",
                            yieldUnitLabel = "cup",
                            packagingCost = "1000",
                            overheadPerBatch = "3000",
                            overheadPerUnit = "0",
                            overheadPercentage = "0",
                            createdAt = now,
                            updatedAt = now
                        )
                        val p2Items = mutableListOf<ProductIngredientEntity>()
                        if (uht != null) {
                            p2Items.add(ProductIngredientEntity(UUID.randomUUID().toString(), p2Id, uht.id, "500", "ml"))
                        }
                        if (skm != null) {
                            p2Items.add(ProductIngredientEntity(UUID.randomUUID().toString(), p2Id, skm.id, "60", "g"))
                        }
                        database.productDao().saveProductWithRecipe(p2, p2Items)
                    }
                }
            } catch (e: Exception) {
                Log.e("KosioApp", "Error seeding sample products", e)
            }
        }
    }

    companion object {
        lateinit var instance: KosioApplication
            private set
    }
}
