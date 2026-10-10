package com.kosio.app.domain.ocr

import com.kosio.app.domain.model.UnitDimension
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ReceiptParserTests {

    @Test
    fun testParseIndomaretReceipt() {
        val lines = listOf(
            "INDOMARET KALIURANG",
            "JL KALIURANG KM 5",
            "KASIR : BUDI",
            "---------------------------",
            "SUSU UHT FULL CREAM 1L",
            "1 x 19.500",
            "TEPUNG MAIZENA 150G",
            "1 x 7.000",
            "SKM INDOMILK 370G",
            "2 x 12.000",
            "---------------------------",
            "TOTAL : 50.500",
            "TUNAI : 100.000",
            "KEMBALI : 49.500",
            "TERIMA KASIH"
        )

        val result = ReceiptTextParser.parseReceiptLines(lines)

        assertEquals("INDOMARET KALIURANG", result.merchantName)
        assertEquals(3, result.items.size)

        // Item 1: Susu UHT
        val susu = result.items[0]
        assertEquals("SUSU UHT FULL CREAM 1L", susu.name)
        assertEquals(BigDecimal("19500"), susu.purchasePrice)
        assertEquals("l", susu.purchaseUnit)
        assertEquals("ml", susu.baseUnit)
        assertEquals(UnitDimension.VOLUME, susu.unitDimension)

        // Item 2: Maizena
        val maizena = result.items[1]
        assertEquals("TEPUNG MAIZENA 150G", maizena.name)
        assertEquals(BigDecimal("7000"), maizena.purchasePrice)
        assertEquals(BigDecimal("150"), maizena.purchaseQuantity)
        assertEquals("g", maizena.purchaseUnit)

        // Item 3: SKM
        val skm = result.items[2]
        assertEquals("SKM INDOMILK 370G", skm.name)
        assertEquals(BigDecimal("12000"), skm.purchasePrice)
    }

    @Test
    fun testParseSingleLineReceipt() {
        val lines = listOf(
            "TOKO BAHAN KUE ANUGERAH",
            "WHIPPED CREAM HAAN 200G  Rp 28.000",
            "BISKUIT MALKIST GABIN 1PAK  Rp 8.000",
            "CUP PLASTIK 50PCS  Rp 25.000",
            "TOTAL BELANJA : Rp 61.000"
        )

        val result = ReceiptTextParser.parseReceiptLines(lines)

        assertEquals(3, result.items.size)
        assertEquals(BigDecimal("28000"), result.items[0].purchasePrice)
        assertEquals("WHIPPED CREAM HAAN 200G", result.items[0].name)
        assertEquals(BigDecimal("8000"), result.items[1].purchasePrice)
        assertEquals("pack", result.items[1].purchaseUnit)
    }
}
