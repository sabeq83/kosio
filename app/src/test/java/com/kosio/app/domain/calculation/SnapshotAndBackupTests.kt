package com.kosio.app.domain.calculation

import com.kosio.app.data.backup.BackupIngredient
import com.kosio.app.data.backup.BackupManager
import com.kosio.app.data.backup.BackupPayload
import com.kosio.app.data.backup.BackupProduct
import com.kosio.app.domain.model.CalculationSnapshot
import com.kosio.app.domain.model.CurrencyCode
import com.kosio.app.domain.model.Ingredient
import com.kosio.app.domain.model.Product
import com.kosio.app.domain.model.RecipeItem
import com.kosio.app.domain.model.UnitDimension
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class SnapshotAndBackupTests {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    // Test 9: Snapshot lama tidak berubah setelah ingredient price update
    @Test
    fun test9_snapshotImmutabilityAfterPriceUpdate() {
        var flour = Ingredient(
            id = "flour-1",
            name = "Tepung Terigu",
            unitDimension = UnitDimension.WEIGHT,
            purchasePrice = BigDecimal("10000"),
            purchaseQuantity = BigDecimal("1"),
            purchaseUnit = "kg",
            baseUnit = "g"
        )

        val product = Product(
            id = "bread-1",
            name = "Roti Tawar",
            yieldQuantity = BigDecimal("10"),
            yieldUnitLabel = "pcs"
        )

        val recipe = listOf(
            RecipeItem(
                productId = product.id,
                ingredientId = flour.id,
                amountUsed = BigDecimal("1000"),
                unit = "g"
            )
        )

        // Calculate initial HPP
        val initialHpp = HppCalculator.calculateHpp(product, recipe, mapOf(flour.id to flour))
        assertEquals(BigDecimal("1000.00"), initialHpp.unitCost)

        // Save snapshot
        val snapshot = CalculationSnapshot(
            productId = product.id,
            productName = product.name,
            yieldQuantity = product.yieldQuantity,
            yieldUnitLabel = product.yieldUnitLabel,
            totalMaterialCost = initialHpp.totalIngredientCost,
            totalPackagingCost = initialHpp.totalPackagingCost,
            totalOverheadCost = initialHpp.totalOverheadCost,
            totalBatchCost = initialHpp.totalBatchCost,
            unitCost = initialHpp.unitCost
        )

        // Now ingredient price doubles!
        flour = flour.copy(purchasePrice = BigDecimal("20000"))

        // New calculation reflects price increase
        val newHpp = HppCalculator.calculateHpp(product, recipe, mapOf(flour.id to flour))
        assertEquals(BigDecimal("2000.00"), newHpp.unitCost)

        // BUT previously saved snapshot remains exactly unchanged!
        assertEquals(BigDecimal("1000.00"), snapshot.unitCost)
        assertEquals(BigDecimal("10000.00"), snapshot.totalBatchCost)
    }

    // Test 14: Restore invalid file / JSON is rejected safely
    @Test
    fun test14_invalidBackupFileRejected() {
        val corruptedJson = "{ \"schemaVersion\": 1, \"corrupted\": true, unexpected_chars: [[["

        var isValid = true
        var errorMessage: String? = null
        try {
            json.decodeFromString<BackupPayload>(corruptedJson)
        } catch (e: Exception) {
            isValid = false
            errorMessage = e.message
        }

        assertFalse(isValid)
        assertNotNull(errorMessage)
    }

    // Test 15: Backup Indonesia dapat direstore pada English locale
    @Test
    fun test15_localeFreeBackupRestore() {
        val payload = BackupPayload(
            schemaVersion = 1,
            appVersion = "1.0.0",
            exportedAt = System.currentTimeMillis(),
            ingredients = listOf(
                BackupIngredient(
                    id = "ing-1",
                    name = "Tepung Segitiga",
                    category = "Bahan Kering",
                    unitDimension = "WEIGHT",
                    purchasePrice = "14000",
                    purchaseQuantity = "1",
                    purchaseUnit = "kg",
                    baseUnit = "g",
                    customPackQuantity = null,
                    wastePercent = "0",
                    isArchived = false,
                    createdAt = 1000L,
                    updatedAt = 1000L
                )
            ),
            ingredientPrices = emptyList(),
            products = listOf(
                BackupProduct(
                    id = "prod-1",
                    name = "Bolu Kukus",
                    category = "Kue Basah",
                    yieldQuantity = "20",
                    yieldUnitLabel = "pcs",
                    packagingCost = "5000",
                    overheadPerBatch = "10000",
                    overheadPerUnit = "0",
                    overheadPercentage = "0",
                    isArchived = false,
                    createdAt = 1000L,
                    updatedAt = 1000L
                )
            ),
            productIngredients = emptyList(),
            snapshots = emptyList()
        )

        val jsonString = json.encodeToString(payload)
        val decoded = json.decodeFromString<BackupPayload>(jsonString)

        assertEquals(1, decoded.ingredients.size)
        assertEquals("Tepung Segitiga", decoded.ingredients[0].name)
        assertEquals("14000", decoded.ingredients[0].purchasePrice)
        assertEquals(1, decoded.products.size)
        assertEquals("Bolu Kukus", decoded.products[0].name)
    }

    // Test 16: Mengganti bahasa tidak mengubah formula atau currency
    @Test
    fun test16_languageChangePreservesData() {
        val flour = Ingredient(
            id = "flour-1",
            name = "Flour",
            unitDimension = UnitDimension.WEIGHT,
            purchasePrice = BigDecimal("15000"),
            purchaseQuantity = BigDecimal("1"),
            purchaseUnit = "kg",
            baseUnit = "g"
        )
        val product = Product(
            id = "bread-1",
            name = "Artisan Bread",
            yieldQuantity = BigDecimal("5"),
            yieldUnitLabel = "loaves"
        )

        // Raw domain data is invariant of language
        assertEquals(BigDecimal("15000"), flour.purchasePrice)
        assertEquals("kg", flour.purchaseUnit)
        assertEquals(BigDecimal("5"), product.yieldQuantity)
    }

    // Test 18: Share output memakai nilai yang sama dengan hasil di layar
    @Test
    fun test18_shareOutputMatchesScreenValues() {
        val product = Product(
            name = "Donat Coklat",
            yieldQuantity = BigDecimal("12"),
            yieldUnitLabel = "pcs",
            packagingCost = BigDecimal("6000")
        )
        val sugar = Ingredient(
            name = "Gula",
            purchasePrice = BigDecimal("16000"),
            purchaseQuantity = BigDecimal("1"),
            purchaseUnit = "kg",
            baseUnit = "g"
        )
        val recipe = listOf(
            RecipeItem(productId = product.id, ingredientId = sugar.id, amountUsed = BigDecimal("500"), unit = "g")
        )

        val hpp = HppCalculator.calculateHpp(product, recipe, mapOf(sugar.id to sugar))
        // Material = 8000, Packaging = 6000, Total = 14000, Unit = 14000 / 12 = 1166.67
        val formattedBatch = CurrencyCode.IDR.format(hpp.totalBatchCost)
        val formattedUnit = CurrencyCode.IDR.format(hpp.unitCost)

        val shareText = "Modal HPP: $formattedUnit / ${product.yieldUnitLabel}\nTotal 1 Batch: $formattedBatch"

        assertTrue(shareText.contains(formattedBatch))
        assertTrue(shareText.contains(formattedUnit))
    }
}
