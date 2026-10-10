package com.kosio.app.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class CurrencyCode(
    val code: String,
    val symbol: String,
    val decimalDigits: Int
) {
    IDR("IDR", "Rp", 0),
    USD("USD", "$", 2);

    fun format(amount: BigDecimal): String {
        val symbols = DecimalFormatSymbols(if (this == IDR) Locale("in", "ID") else Locale.US)
        if (this == IDR) {
            symbols.groupingSeparator = '.'
            symbols.decimalSeparator = ','
            val df = DecimalFormat("#,##0", symbols)
            return "Rp" + df.format(amount.setScale(0, RoundingMode.HALF_UP))
        } else {
            symbols.groupingSeparator = ','
            symbols.decimalSeparator = '.'
            val df = DecimalFormat("#,##0.00", symbols)
            return "$" + df.format(amount.setScale(2, RoundingMode.HALF_UP))
        }
    }

    companion object {
        fun fromCode(code: String): CurrencyCode {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: IDR
        }
    }
}

enum class RoundingRule(val code: String, val increment: BigDecimal?) {
    NONE("NONE", null),
    UP_100("UP_100", BigDecimal("100")),
    UP_500("UP_500", BigDecimal("500")),
    UP_1000("UP_1000", BigDecimal("1000"));

    fun apply(value: BigDecimal): BigDecimal {
        if (increment == null || increment <= BigDecimal.ZERO) {
            return value
        }
        val divided = value.divide(increment, 0, RoundingMode.CEILING)
        return divided.multiply(increment)
    }

    companion object {
        fun fromCode(code: String): RoundingRule {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: NONE
        }
    }
}

enum class AppLanguage(val code: String) {
    SYSTEM("system"),
    INDONESIAN("in"),
    ENGLISH("en");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: SYSTEM
        }
    }
}
