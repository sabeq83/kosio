package com.kosio.app.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

enum class UnitDimension {
    WEIGHT,
    VOLUME,
    COUNT
}

enum class StandardUnit(
    val dimension: UnitDimension,
    val code: String,
    val baseUnitCode: String,
    val factorToBase: BigDecimal
) {
    // Weight
    KG(UnitDimension.WEIGHT, "kg", "g", BigDecimal("1000")),
    G(UnitDimension.WEIGHT, "g", "g", BigDecimal("1")),

    // Volume
    LITER(UnitDimension.VOLUME, "l", "ml", BigDecimal("1000")),
    ML(UnitDimension.VOLUME, "ml", "ml", BigDecimal("1")),

    // Count
    PCS(UnitDimension.COUNT, "pcs", "pcs", BigDecimal("1")),
    PORTION(UnitDimension.COUNT, "portion", "pcs", BigDecimal("1")),
    PACK(UnitDimension.COUNT, "pack", "pcs", BigDecimal("1")),
    BOX(UnitDimension.COUNT, "box", "pcs", BigDecimal("1")),
    BOTTLE(UnitDimension.COUNT, "bottle", "pcs", BigDecimal("1"));

    companion object {
        fun fromCode(code: String): StandardUnit? {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
        }
    }
}

object UnitConverter {
    /**
     * Converts a given amount in fromUnit to baseUnit.
     * If fromUnit is a custom pack (e.g. 1 Box = 50 pcs), customPackQuantity is used.
     */
    fun convertToBase(
        amount: BigDecimal,
        unitCode: String,
        customPackQuantity: BigDecimal? = null
    ): Pair<BigDecimal, String> {
        val standard = StandardUnit.fromCode(unitCode)
        if (standard != null) {
            if (standard.dimension == UnitDimension.COUNT && customPackQuantity != null && customPackQuantity > BigDecimal.ZERO) {
                if (standard == StandardUnit.PACK || standard == StandardUnit.BOX || standard == StandardUnit.BOTTLE) {
                    return Pair(amount.multiply(customPackQuantity), "pcs")
                }
            }
            val converted = amount.multiply(standard.factorToBase)
            return Pair(converted, standard.baseUnitCode)
        }
        // Custom or count fallback
        return Pair(amount, unitCode.lowercase())
    }

    /**
     * Validates whether source unit and target unit belong to the same dimension.
     */
    fun areCompatible(unit1: String, unit2: String): Boolean {
        if (unit1.equals(unit2, ignoreCase = true)) return true
        val u1 = StandardUnit.fromCode(unit1)
        val u2 = StandardUnit.fromCode(unit2)
        if (u1 != null && u2 != null) {
            return u1.dimension == u2.dimension
        }
        return false
    }

    /**
     * Converts amount from source unit to target unit within the same dimension.
     */
    fun convert(
        amount: BigDecimal,
        fromUnit: String,
        toUnit: String,
        customPackQuantity: BigDecimal? = null
    ): BigDecimal {
        if (fromUnit.equals(toUnit, ignoreCase = true)) return amount
        val (baseAmount, baseUnit) = convertToBase(amount, fromUnit, customPackQuantity)
        val toStandard = StandardUnit.fromCode(toUnit)
        if (toStandard != null) {
            if (toStandard.baseUnitCode != baseUnit) {
                throw IllegalArgumentException("Incompatible unit conversion from $fromUnit to $toUnit")
            }
            return baseAmount.divide(toStandard.factorToBase, 6, RoundingMode.HALF_UP)
        }
        return baseAmount
    }
}
