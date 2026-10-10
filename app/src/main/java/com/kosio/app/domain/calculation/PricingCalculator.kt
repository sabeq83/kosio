package com.kosio.app.domain.calculation

import java.math.BigDecimal
import java.math.RoundingMode

enum class DiscountType {
    PERCENTAGE,
    NOMINAL
}

data class PricingScenarioInput(
    val listPrice: BigDecimal,
    val unitCost: BigDecimal,
    val discountType: DiscountType = DiscountType.PERCENTAGE,
    val discountValue: BigDecimal = BigDecimal.ZERO, // e.g. 10 for 10% or 2000 for Rp2.000
    val marketplaceFeePercent: BigDecimal = BigDecimal.ZERO, // e.g. 20 for 20%
    val fixedTransactionFee: BigDecimal = BigDecimal.ZERO,
    val sellerShippingSubsidy: BigDecimal = BigDecimal.ZERO,
    val additionalSellingCost: BigDecimal = BigDecimal.ZERO
)

data class PricingScenarioResult(
    val listPrice: BigDecimal,
    val unitCost: BigDecimal,
    val discountAmount: BigDecimal,
    val priceAfterDiscount: BigDecimal,
    val marketplaceFeeAmount: BigDecimal,
    val fixedTransactionFee: BigDecimal,
    val sellerShippingSubsidy: BigDecimal,
    val additionalSellingCost: BigDecimal,
    val netRevenue: BigDecimal,
    val profitPerUnit: BigDecimal,
    val marginPercent: BigDecimal,
    val markupPercent: BigDecimal,
    val isLoss: Boolean
)

data class ChannelPreset(
    val id: String,
    val name: String,
    val feePercentage: BigDecimal,
    val fixedFee: BigDecimal = BigDecimal.ZERO,
    val isDefault: Boolean = true
)

data class ChannelPricingResult(
    val channelId: String,
    val channelName: String,
    val feePercentage: BigDecimal,
    val fixedFee: BigDecimal,
    val suggestedPrice: BigDecimal,
    val feeAmount: BigDecimal,
    val netRevenue: BigDecimal,
    val profit: BigDecimal,
    val marginPercent: BigDecimal
)

object PricingCalculator {

    private val HUNDRED = BigDecimal("100")

    val DEFAULT_CHANNELS = listOf(
        ChannelPreset("offline", "Offline / Dine-In", BigDecimal.ZERO, BigDecimal.ZERO),
        ChannelPreset("gofood_grab", "GoFood / GrabFood", BigDecimal("20.0"), BigDecimal.ZERO),
        ChannelPreset("shopeefood", "ShopeeFood", BigDecimal("20.0"), BigDecimal("1000")),
        ChannelPreset("tiktok_tokopedia", "TikTok / Tokopedia", BigDecimal("12.0"), BigDecimal.ZERO)
    )

    /**
     * Calculates suggested selling price to achieve target profit margin.
     * Formula: priceForTargetMargin = unitCost / (1 - (targetMarginPercent / 100))
     */
    fun calculatePriceForTargetMargin(
        unitCost: BigDecimal,
        targetMarginPercent: BigDecimal
    ): BigDecimal {
        if (unitCost < BigDecimal.ZERO) {
            throw IllegalArgumentException("Unit cost cannot be negative")
        }
        if (targetMarginPercent >= HUNDRED) {
            throw IllegalArgumentException("Target margin must be strictly less than 100%")
        }
        if (targetMarginPercent <= BigDecimal("-100")) {
            throw IllegalArgumentException("Target margin is invalid")
        }

        val marginFraction = targetMarginPercent.divide(HUNDRED, 10, RoundingMode.HALF_UP)
        val denominator = BigDecimal.ONE.subtract(marginFraction)

        if (denominator <= BigDecimal.ZERO) {
            throw IllegalArgumentException("Denominator must be greater than 0")
        }

        return unitCost.divide(denominator, 4, RoundingMode.HALF_UP)
    }

    /**
     * Calculates suggested selling price to achieve target markup percentage.
     * Formula: priceForTargetMarkup = unitCost * (1 + (targetMarkupPercent / 100))
     */
    fun calculatePriceForTargetMarkup(
        unitCost: BigDecimal,
        targetMarkupPercent: BigDecimal
    ): BigDecimal {
        if (unitCost < BigDecimal.ZERO) {
            throw IllegalArgumentException("Unit cost cannot be negative")
        }
        if (targetMarkupPercent < BigDecimal.ZERO) {
            throw IllegalArgumentException("Target markup cannot be negative")
        }

        val markupFraction = targetMarkupPercent.divide(HUNDRED, 10, RoundingMode.HALF_UP)
        val factor = BigDecimal.ONE.add(markupFraction)
        return unitCost.multiply(factor).setScale(4, RoundingMode.HALF_UP)
    }

    /**
     * Calculates multi-channel prices so that each channel yields the desired base net revenue.
     * Formula: channelPrice = (desiredNetRevenue + fixedFee) / (1 - (feePercentage / 100))
     */
    fun calculateMultiChannelPricing(
        baseNetRevenue: BigDecimal,
        unitCost: BigDecimal,
        channels: List<ChannelPreset> = DEFAULT_CHANNELS
    ): List<ChannelPricingResult> {
        return channels.map { channel ->
            val feeFraction = channel.feePercentage.divide(HUNDRED, 10, RoundingMode.HALF_UP)
            val denominator = BigDecimal.ONE.subtract(feeFraction)
            val suggestedPrice = if (denominator > BigDecimal.ZERO) {
                baseNetRevenue.add(channel.fixedFee).divide(denominator, 2, RoundingMode.HALF_UP)
            } else {
                baseNetRevenue.add(channel.fixedFee)
            }

            val feeAmount = suggestedPrice.multiply(feeFraction).add(channel.fixedFee).setScale(2, RoundingMode.HALF_UP)
            val netRevenue = suggestedPrice.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP)
            val profit = netRevenue.subtract(unitCost).setScale(2, RoundingMode.HALF_UP)
            val margin = if (suggestedPrice > BigDecimal.ZERO) {
                profit.divide(suggestedPrice, 4, RoundingMode.HALF_UP).multiply(HUNDRED).setScale(1, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            ChannelPricingResult(
                channelId = channel.id,
                channelName = channel.name,
                feePercentage = channel.feePercentage,
                fixedFee = channel.fixedFee,
                suggestedPrice = suggestedPrice,
                feeAmount = feeAmount,
                netRevenue = netRevenue,
                profit = profit,
                marginPercent = margin
            )
        }
    }

    /**
     * Simulates full pricing, discounts, marketplace fees, net revenue, profit, margin, and markup.
     */
    fun simulatePricing(input: PricingScenarioInput): PricingScenarioResult {
        if (input.listPrice < BigDecimal.ZERO || input.unitCost < BigDecimal.ZERO) {
            throw IllegalArgumentException("Price and cost must be non-negative")
        }

        // Calculate discount amount
        val discountAmount: BigDecimal = when (input.discountType) {
            DiscountType.PERCENTAGE -> {
                val clampedPercent = input.discountValue.max(BigDecimal.ZERO).min(HUNDRED)
                input.listPrice.multiply(clampedPercent.divide(HUNDRED, 10, RoundingMode.HALF_UP))
            }
            DiscountType.NOMINAL -> {
                input.discountValue.max(BigDecimal.ZERO).min(input.listPrice)
            }
        }

        val priceAfterDiscount = input.listPrice.subtract(discountAmount).max(BigDecimal.ZERO)

        // Percentage marketplace fee on discounted price
        val marketplaceFeeAmount = if (input.marketplaceFeePercent > BigDecimal.ZERO) {
            priceAfterDiscount.multiply(input.marketplaceFeePercent.divide(HUNDRED, 10, RoundingMode.HALF_UP))
        } else {
            BigDecimal.ZERO
        }

        val fixedFee = input.fixedTransactionFee.max(BigDecimal.ZERO)
        val shippingSubsidy = input.sellerShippingSubsidy.max(BigDecimal.ZERO)
        val additionalCost = input.additionalSellingCost.max(BigDecimal.ZERO)

        // Net Revenue = priceAfterDiscount - %fee - fixedFee - subsidy - additionalCost
        val netRevenue = priceAfterDiscount
            .subtract(marketplaceFeeAmount)
            .subtract(fixedFee)
            .subtract(shippingSubsidy)
            .subtract(additionalCost)

        val profitPerUnit = netRevenue.subtract(input.unitCost)

        // Profit Margin = (profit / netRevenue) * 100
        val marginPercent = if (netRevenue > BigDecimal.ZERO) {
            profitPerUnit.divide(netRevenue, 6, RoundingMode.HALF_UP).multiply(HUNDRED)
        } else {
            BigDecimal.ZERO
        }

        // Markup = (profit / unitCost) * 100
        val markupPercent = if (input.unitCost > BigDecimal.ZERO) {
            profitPerUnit.divide(input.unitCost, 6, RoundingMode.HALF_UP).multiply(HUNDRED)
        } else {
            BigDecimal.ZERO
        }

        val isLoss = netRevenue < input.unitCost

        return PricingScenarioResult(
            listPrice = input.listPrice.setScale(2, RoundingMode.HALF_UP),
            unitCost = input.unitCost.setScale(2, RoundingMode.HALF_UP),
            discountAmount = discountAmount.setScale(2, RoundingMode.HALF_UP),
            priceAfterDiscount = priceAfterDiscount.setScale(2, RoundingMode.HALF_UP),
            marketplaceFeeAmount = marketplaceFeeAmount.setScale(2, RoundingMode.HALF_UP),
            fixedTransactionFee = fixedFee.setScale(2, RoundingMode.HALF_UP),
            sellerShippingSubsidy = shippingSubsidy.setScale(2, RoundingMode.HALF_UP),
            additionalSellingCost = additionalCost.setScale(2, RoundingMode.HALF_UP),
            netRevenue = netRevenue.setScale(2, RoundingMode.HALF_UP),
            profitPerUnit = profitPerUnit.setScale(2, RoundingMode.HALF_UP),
            marginPercent = marginPercent.setScale(2, RoundingMode.HALF_UP),
            markupPercent = markupPercent.setScale(2, RoundingMode.HALF_UP),
            isLoss = isLoss
        )
    }
}
