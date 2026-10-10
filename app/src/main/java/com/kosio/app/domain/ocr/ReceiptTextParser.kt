package com.kosio.app.domain.ocr

import com.kosio.app.domain.model.UnitDimension
import java.math.BigDecimal
import java.util.UUID

data class ScannedReceiptItem(
    val id: String = UUID.randomUUID().toString(),
    val rawText: String,
    val name: String,
    val category: String? = null,
    val purchasePrice: BigDecimal,
    val purchaseQuantity: BigDecimal = BigDecimal.ONE,
    val purchaseUnit: String = "pcs",
    val baseUnit: String = "pcs",
    val unitDimension: UnitDimension = UnitDimension.COUNT,
    val wastePercent: BigDecimal = BigDecimal.ZERO,
    val isSelected: Boolean = true,
    val matchedIngredientId: String? = null
)

data class ParsedReceiptResult(
    val merchantName: String? = null,
    val dateText: String? = null,
    val items: List<ScannedReceiptItem> = emptyList(),
    val totalAmount: BigDecimal? = null
)

object ReceiptTextParser {

    private val IGNORED_KEYWORDS = listOf(
        "KASIR", "CASHIER", "TUNAI", "CASH", "CHANGE", "KEMBALIAN", "KEMBALI",
        "SUBTOTAL", "TOTAL", "PPN", "TAX", "DEBIT", "QRIS", "BCA", "MANDIRI",
        "BRI", "BNI", "STRUK", "TELP", "PHONE", "JL.", "JALAN", "ALAMAT",
        "SELAMAT", "TERIMA KASIH", "THANK YOU", "KUPON", "POINT", "MEMBER",
        "KARTU", "CARD", "PEMBULATAN", "ITEM", "ITEMS", "KANTONG KRESEK", "KANTONG PLASTIK"
    )

    private val PRICE_REGEX = Regex("""(?:Rp\.?|RP\.?)?\s*([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]{2})?|[0-9]+)""")
    private val MULTI_QTY_PRICE_REGEX = Regex("""([0-9]+(?:\.[0-9]+)?)\s*(?:x|X|\*)\s*(?:Rp\.?|RP\.?)?\s*([0-9]{1,3}(?:[.,][0-9]{3})*|[0-9]+)""")

    /**
     * Parses raw OCR lines from receipt into structured ingredients and prices.
     */
    fun parseReceiptLines(rawLines: List<String>): ParsedReceiptResult {
        val filteredLines = rawLines
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val items = mutableListOf<ScannedReceiptItem>()
        var merchantName: String? = null
        var totalAmount: BigDecimal? = null

        var i = 0
        while (i < filteredLines.size) {
            val line = filteredLines[i]
            val upper = line.uppercase()

            // Check if it's merchant header (first 1-3 lines without numbers)
            if (merchantName == null && i < 3 && !line.any { it.isDigit() } && line.length >= 3) {
                merchantName = line
                i++
                continue
            }

            // Check if it's Total
            if (upper.startsWith("TOTAL") || upper.contains("TOTAL BAYAR") || upper.contains("GRAND TOTAL")) {
                val price = extractPriceFromText(line)
                if (price != null && price > BigDecimal.ZERO) {
                    totalAmount = price
                }
                i++
                continue
            }

            // Skip ignored lines
            if (IGNORED_KEYWORDS.any { upper.startsWith(it) || upper == it }) {
                i++
                continue
            }

            // Check Pattern 1: Multi-line (Line i is Name, Line i+1 is "1 x 15.000")
            if (i + 1 < filteredLines.size) {
                val nextLine = filteredLines[i + 1]
                val multiMatch = MULTI_QTY_PRICE_REGEX.find(nextLine)
                if (multiMatch != null) {
                    val qtyStr = multiMatch.groupValues[1]
                    val priceStr = multiMatch.groupValues[2].replace(".", "").replace(",", "")
                    val qty = try { BigDecimal(qtyStr) } catch (e: Exception) { BigDecimal.ONE }
                    val price = try { BigDecimal(priceStr) } catch (e: Exception) { BigDecimal.ZERO }

                    if (price > BigDecimal.ZERO) {
                        val parsed = buildScannedItem(rawName = line, rawText = "$line | $nextLine", price = price, quantity = qty)
                        items.add(parsed)
                        i += 2
                        continue
                    }
                }
            }

            // Check Pattern 2: Single line "[Name] [Price]" or "[Name] ... [Price]"
            val singleLinePrice = extractPriceFromText(line)
            if (singleLinePrice != null && singleLinePrice >= BigDecimal("500")) {
                val nameOnly = removePriceFromText(line)
                if (nameOnly.isNotBlank() && nameOnly.length >= 2 && !isNonProduct(nameOnly)) {
                    val parsed = buildScannedItem(rawName = nameOnly, rawText = line, price = singleLinePrice, quantity = BigDecimal.ONE)
                    items.add(parsed)
                    i++
                    continue
                }
            }

            i++
        }

        return ParsedReceiptResult(
            merchantName = merchantName,
            items = items,
            totalAmount = totalAmount
        )
    }

    private fun isNonProduct(text: String): Boolean {
        val upper = text.uppercase()
        return IGNORED_KEYWORDS.any { upper.contains(it) }
    }

    private fun extractPriceFromText(text: String): BigDecimal? {
        val matches = PRICE_REGEX.findAll(text).toList()
        if (matches.isEmpty()) return null

        // Usually the last match in a receipt line is the price
        val lastMatch = matches.last()
        val numClean = lastMatch.groupValues[1]
            .replace(".", "")
            .replace(",", ".")

        return try {
            val bd = BigDecimal(numClean)
            if (bd > BigDecimal.ZERO) bd else null
        } catch (e: Exception) {
            null
        }
    }

    private fun removePriceFromText(text: String): String {
        val matches = PRICE_REGEX.findAll(text).toList()
        if (matches.isEmpty()) return text.trim()
        val lastMatch = matches.last()
        return text.substring(0, lastMatch.range.first).trim().trimEnd('-', ':', '.', ' ')
    }

    private fun buildScannedItem(
        rawName: String,
        rawText: String,
        price: BigDecimal,
        quantity: BigDecimal
    ): ScannedReceiptItem {
        var cleanName = rawName.trim()
        var unitDimension = UnitDimension.COUNT
        var purchaseUnit = "pcs"
        var baseUnit = "pcs"
        var parsedQty = quantity

        val upper = cleanName.uppercase()

        // Weight detection (kg, g, gr, gram)
        val kgMatch = Regex("""([0-9]+(?:\.[0-9]+)?)\s*(?:KG|KILO)""").find(upper)
        val gMatch = Regex("""([0-9]+(?:\.[0-9]+)?)\s*(?:G|GR|GRAM)""").find(upper)

        // Volume detection (l, ltr, liter, ml)
        val lMatch = Regex("""([0-9]+(?:\.[0-9]+)?)\s*(?:L|LTR|LITER)""").find(upper)
        val mlMatch = Regex("""([0-9]+(?:\.[0-9]+)?)\s*(?:ML)""").find(upper)

        if (kgMatch != null) {
            unitDimension = UnitDimension.WEIGHT
            purchaseUnit = "kg"
            baseUnit = "g"
            val qtyVal = try { BigDecimal(kgMatch.groupValues[1]) } catch (e: Exception) { BigDecimal.ONE }
            parsedQty = quantity.multiply(qtyVal)
        } else if (gMatch != null) {
            unitDimension = UnitDimension.WEIGHT
            purchaseUnit = "g"
            baseUnit = "g"
            val qtyVal = try { BigDecimal(gMatch.groupValues[1]) } catch (e: Exception) { BigDecimal("100") }
            parsedQty = quantity.multiply(qtyVal)
        } else if (lMatch != null) {
            unitDimension = UnitDimension.VOLUME
            purchaseUnit = "l"
            baseUnit = "ml"
            val qtyVal = try { BigDecimal(lMatch.groupValues[1]) } catch (e: Exception) { BigDecimal.ONE }
            parsedQty = quantity.multiply(qtyVal)
        } else if (mlMatch != null) {
            unitDimension = UnitDimension.VOLUME
            purchaseUnit = "ml"
            baseUnit = "ml"
            val qtyVal = try { BigDecimal(mlMatch.groupValues[1]) } catch (e: Exception) { BigDecimal("100") }
            parsedQty = quantity.multiply(qtyVal)
        } else if (upper.contains("PACK") || upper.contains("PAK") || upper.contains("BKS")) {
            unitDimension = UnitDimension.COUNT
            purchaseUnit = "pack"
            baseUnit = "pcs"
        } else if (upper.contains("BOX") || upper.contains("DUS")) {
            unitDimension = UnitDimension.COUNT
            purchaseUnit = "box"
            baseUnit = "pcs"
        }

        // Clean up common receipt prefixes (e.g. "*", numbers)
        cleanName = cleanName.replace(Regex("""^[0-9]+[\.\-\s]+"""), "").trim()

        return ScannedReceiptItem(
            rawText = rawText,
            name = cleanName.ifBlank { "Bahan Baru" },
            purchasePrice = price,
            purchaseQuantity = parsedQty,
            purchaseUnit = purchaseUnit,
            baseUnit = baseUnit,
            unitDimension = unitDimension
        )
    }
}
