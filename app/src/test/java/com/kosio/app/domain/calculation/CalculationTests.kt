package com.kosio.app.domain.calculation

import com.kosio.app.domain.model.CurrencyCode
import com.kosio.app.domain.model.Ingredient
import com.kosio.app.domain.model.Product
import com.kosio.app.domain.model.RecipeItem
import com.kosio.app.domain.model.UnitConverter
import com.kosio.app.domain.model.UnitDimension
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class CalculationTests {

    // Test 1: Tepung Rp14.000/kg, penggunaan 250 g = Rp3.500
    @Test
    fun test1_flourUsage() {
        val flour = Ingredient(
            name = "Tepung",
            unitDimension = UnitDimension.WEIGHT,
            purchasePrice = BigDecimal("14000"),
            purchaseQuantity = BigDecimal("1"),
            purchaseUnit = "kg",
            baseUnit = "g",
            wastePercent = BigDecimal.ZERO
        )

        val (cost, unitPrice) = HppCalculator.calculateIngredientItemCost(
            ingredient = flour,
            amountUsed = BigDecimal("250"),
            recipeUnit = "g"
        )

        assertEquals(BigDecimal("14.0000"), unitPrice.setScale(4, RoundingMode.HALF_UP))
        assertEquals(BigDecimal("3500.0000"), cost)
    }

    // Test 2: Minyak Rp18.000/liter, penggunaan 100 ml = Rp1.800
    @Test
    fun test2_oilUsage() {
        val oil = Ingredient(
            name = "Minyak",
            unitDimension = UnitDimension.VOLUME,
            purchasePrice = BigDecimal("18000"),
            purchaseQuantity = BigDecimal("1"),
            purchaseUnit = "l",
            baseUnit = "ml",
            wastePercent = BigDecimal.ZERO
        )

        val (cost, unitPrice) = HppCalculator.calculateIngredientItemCost(
            ingredient = oil,
            amountUsed = BigDecimal("100"),
            recipeUnit = "ml"
        )

        assertEquals(BigDecimal("18.0000"), unitPrice.setScale(4, RoundingMode.HALF_UP))
        assertEquals(BigDecimal("1800.0000"), cost)
    }

    // Test 3: Pak cup Rp25.000 isi 50 = Rp500/pcs
    @Test
    fun test3_packCupConversion() {
        val cupPack = Ingredient(
            name = "Cup Plastik",
            unitDimension = UnitDimension.COUNT,
            purchasePrice = BigDecimal("25000"),
            purchaseQuantity = BigDecimal("1"),
            purchaseUnit = "pack",
            baseUnit = "pcs",
            customPackQuantity = BigDecimal("50"),
            wastePercent = BigDecimal.ZERO
        )

        val (cost, unitPrice) = HppCalculator.calculateIngredientItemCost(
            ingredient = cupPack,
            amountUsed = BigDecimal("1"),
            recipeUnit = "pcs"
        )

        assertEquals(BigDecimal("500.0000"), unitPrice.setScale(4, RoundingMode.HALF_UP))
        assertEquals(BigDecimal("500.0000"), cost)
    }

    // Test 4: Ayam Rp40.000/kg dengan waste 20% = Rp50/g usable
    @Test
    fun test4_chickenWithWaste() {
        val chicken = Ingredient(
            name = "Ayam Fillet",
            unitDimension = UnitDimension.WEIGHT,
            purchasePrice = BigDecimal("40000"),
            purchaseQuantity = BigDecimal("1"),
            purchaseUnit = "kg",
            baseUnit = "g",
            wastePercent = BigDecimal("20")
        )

        val unitPrice = HppCalculator.calculateEffectiveUnitPrice(
            purchasePrice = chicken.purchasePrice,
            purchaseQuantity = chicken.purchaseQuantity,
            purchaseUnit = chicken.purchaseUnit,
            wastePercent = chicken.wastePercent
        )

        // 40,000 / (1000g * (1 - 0.2)) = 40,000 / 800g = 50 / g
        assertEquals(BigDecimal("50.0000"), unitPrice.setScale(4, RoundingMode.HALF_UP))
    }

    // Test 5: Batch cost Rp100.000 dan yield 20 = Rp5.000/unit
    @Test
    fun test5_batchCostAndYield() {
        val product = Product(
            name = "Cake Box",
            yieldQuantity = BigDecimal("20"),
            packagingCost = BigDecimal("20000"),
            overheadPerBatch = BigDecimal("30000")
        )

        val flour = Ingredient(
            name = "Tepung",
            purchasePrice = BigDecimal("50000"),
            purchaseQuantity = BigDecimal("1"),
            purchaseUnit = "kg",
            baseUnit = "g"
        )

        val recipe = listOf(
            RecipeItem(
                productId = product.id,
                ingredientId = flour.id,
                amountUsed = BigDecimal("1000"),
                unit = "g"
            )
        )

        val result = HppCalculator.calculateHpp(
            product = product,
            recipeItems = recipe,
            ingredientMap = mapOf(flour.id to flour)
        )

        // Total batch = 50,000 (flour) + 20,000 (packaging) + 30,000 (overhead) = 100,000
        // Unit cost = 100,000 / 20 = 5,000
        assertEquals(BigDecimal("100000.00"), result.totalBatchCost)
        assertEquals(BigDecimal("5000.00"), result.unitCost)
    }

    // Test 6: Unit cost Rp5.000 dan net selling price Rp10.000 = profit Rp5.000, margin 50%, markup 100%
    @Test
    fun test6_profitMarginMarkup() {
        val result = PricingCalculator.simulatePricing(
            PricingScenarioInput(
                listPrice = BigDecimal("10000"),
                unitCost = BigDecimal("5000")
            )
        )

        assertEquals(BigDecimal("5000.00"), result.profitPerUnit)
        assertEquals(BigDecimal("50.00"), result.marginPercent)
        assertEquals(BigDecimal("100.00"), result.markupPercent)
    }

    // Test 7: Target margin 40% dan unit cost Rp6.000 = suggested price Rp10.000 sebelum rounding
    @Test
    fun test7_targetMarginSuggestedPrice() {
        // Price = 6000 / (1 - 0.40) = 6000 / 0.60 = 10000
        val suggestedPrice = PricingCalculator.calculatePriceForTargetMargin(
            unitCost = BigDecimal("6000"),
            targetMarginPercent = BigDecimal("40")
        )

        assertEquals(BigDecimal("10000.0000"), suggestedPrice.setScale(4, RoundingMode.HALF_UP))
    }

    // Test 8: Harga Rp20.000, diskon 10%, marketplace fee 20% = Rp14.400 sebelum fixed/additional fees
    @Test
    fun test8_marketplaceFeeCalculation() {
        // Price after 10% discount = 20,000 - 2,000 = 18,000
        // Marketplace fee 20% of 18,000 = 3,600
        // Net revenue = 18,000 - 3,600 = 14,400
        val result = PricingCalculator.simulatePricing(
            PricingScenarioInput(
                listPrice = BigDecimal("20000"),
                unitCost = BigDecimal("10000"),
                discountType = DiscountType.PERCENTAGE,
                discountValue = BigDecimal("10"),
                marketplaceFeePercent = BigDecimal("20")
            )
        )

        assertEquals(BigDecimal("18000.00"), result.priceAfterDiscount)
        assertEquals(BigDecimal("3600.00"), result.marketplaceFeeAmount)
        assertEquals(BigDecimal("14400.00"), result.netRevenue)
    }

    // Test 10: Weight tidak dapat dikonversi ke volume tanpa conversion factor
    @Test
    fun test10_incompatibleUnitsRejected() {
        assertFalse(UnitConverter.areCompatible("kg", "ml"))
        assertFalse(UnitConverter.areCompatible("g", "l"))
        assertThrows(IllegalArgumentException::class.java) {
            UnitConverter.convert(BigDecimal("100"), "g", "ml")
        }
    }

    // Test 11: Waste 100% ditolak
    @Test
    fun test11_waste100PercentRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            HppCalculator.calculateEffectiveUnitPrice(
                purchasePrice = BigDecimal("10000"),
                purchaseQuantity = BigDecimal("1"),
                purchaseUnit = "kg",
                wastePercent = BigDecimal("100")
            )
        }
    }

    // Test 12: Yield nol ditolak
    @Test
    fun test12_zeroYieldRejected() {
        val product = Product(
            name = "Test Zero Yield",
            yieldQuantity = BigDecimal.ZERO
        )
        assertThrows(IllegalArgumentException::class.java) {
            HppCalculator.calculateHpp(product, emptyList(), emptyMap())
        }
    }

    // Test 13: Target margin 100% ditolak
    @Test
    fun test13_targetMargin100PercentRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            PricingCalculator.calculatePriceForTargetMargin(
                unitCost = BigDecimal("5000"),
                targetMarginPercent = BigDecimal("100")
            )
        }
    }

    // Test 17: English + IDR dan Indonesia + USD menghasilkan nilai matematis identik
    @Test
    fun test17_currencyAndLocaleIndependence() {
        val costIdr = BigDecimal("15000")
        val margin = BigDecimal("25")

        val price1 = PricingCalculator.calculatePriceForTargetMargin(costIdr, margin)
        val formattedIdr = CurrencyCode.IDR.format(price1)

        val costUsd = BigDecimal("15.00")
        val price2 = PricingCalculator.calculatePriceForTargetMargin(costUsd, margin)
        val formattedUsd = CurrencyCode.USD.format(price2)

        // Mathematical ratios must be identical
        assertEquals(BigDecimal("20000.0000"), price1.setScale(4, RoundingMode.HALF_UP))
        assertEquals(BigDecimal("20.0000"), price2.setScale(4, RoundingMode.HALF_UP))
        assertEquals("Rp20.000", formattedIdr)
        assertEquals("$20.00", formattedUsd)
    }

    // Test 19: Target markup 150% pada modal Rp4.000 = Rp10.000
    @Test
    fun test19_targetMarkupCalculation() {
        // Price = 4,000 * (1 + 1.50) = 4,000 * 2.5 = 10,000
        val price = PricingCalculator.calculatePriceForTargetMarkup(
            unitCost = BigDecimal("4000"),
            targetMarkupPercent = BigDecimal("150")
        )
        assertEquals(BigDecimal("10000.0000"), price.setScale(4, RoundingMode.HALF_UP))
    }

    // Test 20: Multi-Channel Pricing (Offline vs GoFood 20% vs ShopeeFood 20%+1000)
    @Test
    fun test20_multiChannelPricing() {
        val baseNetRevenue = BigDecimal("10000") // ingin dapat bersih 10.000 per porsi
        val unitCost = BigDecimal("5000")

        val results = PricingCalculator.calculateMultiChannelPricing(
            baseNetRevenue = baseNetRevenue,
            unitCost = unitCost,
            channels = PricingCalculator.DEFAULT_CHANNELS
        )

        val offline = results.first { it.channelId == "offline" }
        assertEquals(BigDecimal("10000.00"), offline.suggestedPrice)
        assertEquals(BigDecimal("10000.00"), offline.netRevenue)
        assertEquals(BigDecimal("5000.00"), offline.profit)

        // GoFood (20% fee): 10,000 / 0.8 = 12,500
        val gofood = results.first { it.channelId == "gofood_grab" }
        assertEquals(BigDecimal("12500.00"), gofood.suggestedPrice)
        assertEquals(BigDecimal("2500.00"), gofood.feeAmount)
        assertEquals(BigDecimal("10000.00"), gofood.netRevenue)

        // ShopeeFood (20% fee + 1,000): (10,000 + 1,000) / 0.8 = 11,000 / 0.8 = 13,750
        val shopee = results.first { it.channelId == "shopeefood" }
        assertEquals(BigDecimal("13750.00"), shopee.suggestedPrice)
        assertEquals(BigDecimal("10000.00"), shopee.netRevenue)
    }
}
