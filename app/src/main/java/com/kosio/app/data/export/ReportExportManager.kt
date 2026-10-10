package com.kosio.app.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.kosio.app.domain.calculation.HppResult
import com.kosio.app.domain.calculation.PricingCalculator
import com.kosio.app.domain.model.CurrencyCode
import com.kosio.app.domain.model.Ingredient
import com.kosio.app.domain.model.Product
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExportManager {

    fun generateProductPdfReport(
        context: Context,
        product: Product,
        hpp: HppResult,
        currency: CurrencyCode
    ): File {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val sanitizedName = product.name.replace(Regex("[^a-zA-Z0-9_]"), "_")
        val file = File(reportsDir, "Kosio_Resep_${sanitizedName}.pdf")

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(30, 41, 59) // Deep Navy
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(100, 116, 139) // Slate grey
            textSize = 10f
            isAntiAlias = true
        }

        val sectionPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 10f
            isAntiAlias = true
        }

        val boldBodyPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(241, 245, 249) // Soft grey-blue
            style = Paint.Style.FILL
        }

        val cardBgPaint = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }

        val primaryBgPaint = Paint().apply {
            color = Color.rgb(37, 99, 235) // Primary Blue
            style = Paint.Style.FILL
        }

        val whiteTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val whiteSubPaint = Paint().apply {
            color = Color.rgb(224, 231, 255)
            textSize = 10f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
        }

        // 1. Header Banner
        canvas.drawText("KOSIO — LEMBAR RESEP & HPP", 40f, 50f, titlePaint)
        val dateStr = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
        canvas.drawText("Dibuat otomatis oleh Kosio App • $dateStr", 40f, 66f, subtitlePaint)

        // 2. Summary Hero Card (Total HPP & Unit Cost)
        val heroRect = RectF(40f, 85f, 555f, 160f)
        canvas.drawRoundRect(heroRect, 10f, 10f, primaryBgPaint)

        canvas.drawText("PRODUK: ${product.name.uppercase(Locale.ROOT)}", 55f, 110f, whiteTextPaint)
        canvas.drawText("Hasil: ${product.yieldQuantity.stripTrailingZeros().toPlainString()} ${product.yieldUnitLabel}", 55f, 128f, whiteSubPaint)
        canvas.drawText("HPP per ${product.yieldUnitLabel}: ${currency.format(hpp.unitCost)}", 55f, 146f, whiteSubPaint)

        canvas.drawText("TOTAL 1 BATCH", 380f, 115f, whiteSubPaint)
        canvas.drawText(currency.format(hpp.totalBatchCost), 380f, 140f, whiteTextPaint)

        // 3. Table of Recipe Ingredients
        var curY = 190f
        canvas.drawText("KOMPOSISI BAHAN BAKU", 40f, curY, sectionPaint)
        curY += 12f

        // Table Header
        canvas.drawRect(40f, curY, 555f, curY + 22f, headerBgPaint)
        canvas.drawText("Nama Bahan", 48f, curY + 15f, boldBodyPaint)
        canvas.drawText("Takaran Pakai", 240f, curY + 15f, boldBodyPaint)
        canvas.drawText("Harga Dasar", 360f, curY + 15f, boldBodyPaint)
        canvas.drawText("Biaya (Subtotal)", 460f, curY + 15f, boldBodyPaint)
        curY += 24f

        // Rows
        for (item in hpp.ingredientBreakdowns) {
            canvas.drawText(item.ingredientName, 48f, curY + 14f, bodyPaint)
            canvas.drawText("${item.amountUsed.stripTrailingZeros().toPlainString()} ${item.unit}", 240f, curY + 14f, bodyPaint)
            canvas.drawText("${currency.format(item.effectiveUnitPrice)}/${item.unit}", 360f, curY + 14f, bodyPaint)
            canvas.drawText(currency.format(item.cost), 460f, curY + 14f, boldBodyPaint)
            curY += 20f
            canvas.drawLine(40f, curY, 555f, curY, linePaint)
        }

        // 4. Breakdown Operasional & Kemasan
        curY += 20f
        canvas.drawText("RINCIAN BIAYA TAMBAHAN (OPERASIONAL & KEMASAN)", 40f, curY, sectionPaint)
        curY += 14f

        canvas.drawRoundRect(RectF(40f, curY, 555f, curY + 65f), 8f, 8f, cardBgPaint)
        canvas.drawText("• Total Biaya Bahan Baku:", 55f, curY + 20f, bodyPaint)
        canvas.drawText(currency.format(hpp.totalIngredientCost), 460f, curY + 20f, boldBodyPaint)

        canvas.drawText("• Biaya Kemasan per Porsi:", 55f, curY + 36f, bodyPaint)
        canvas.drawText(currency.format(hpp.totalPackagingCost), 460f, curY + 36f, boldBodyPaint)

        canvas.drawText("• Biaya Operasional / Overhead:", 55f, curY + 52f, bodyPaint)
        canvas.drawText(currency.format(hpp.totalOverheadCost), 460f, curY + 52f, boldBodyPaint)
        curY += 80f

        // 5. Rekomendasi Multi-Channel Pricing Table
        canvas.drawText("REKOMENDASI HARGA JUAL MULTI-CHANNEL (DENGAN FEE PLATFORM)", 40f, curY, sectionPaint)
        curY += 12f

        val multiChannels = PricingCalculator.calculateMultiChannelPricing(
            baseNetRevenue = hpp.unitCost.multiply(java.math.BigDecimal("1.5")), // contoh benchmark markup 50%
            unitCost = hpp.unitCost
        )

        canvas.drawRect(40f, curY, 555f, curY + 22f, headerBgPaint)
        canvas.drawText("Saluran Penjualan", 48f, curY + 15f, boldBodyPaint)
        canvas.drawText("Biaya Platform", 200f, curY + 15f, boldBodyPaint)
        canvas.drawText("Saran Harga Jual", 340f, curY + 15f, boldBodyPaint)
        canvas.drawText("Estimasi Bersih", 460f, curY + 15f, boldBodyPaint)
        curY += 24f

        for (ch in multiChannels) {
            canvas.drawText(ch.channelName, 48f, curY + 14f, bodyPaint)
            canvas.drawText(if (ch.feePercentage > java.math.BigDecimal.ZERO) "${ch.feePercentage.stripTrailingZeros().toPlainString()}%" else "0%", 200f, curY + 14f, bodyPaint)
            canvas.drawText(currency.format(ch.suggestedPrice), 340f, curY + 14f, boldBodyPaint)
            canvas.drawText(currency.format(ch.netRevenue), 460f, curY + 14f, boldBodyPaint)
            curY += 20f
            canvas.drawLine(40f, curY, 555f, curY, linePaint)
        }

        // Footer
        canvas.drawText("Kosio — Kalkulator HPP & Simulasi Harga Jual Kuliner • 100% Offline & Aman", 40f, 810f, subtitlePaint)

        document.finishPage(page)

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    fun generateIngredientsCsv(
        context: Context,
        ingredients: List<Ingredient>,
        currency: CurrencyCode
    ): File {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(reportsDir, "Kosio_Master_Bahan_Baku.csv")

        FileWriter(file).use { writer ->
            writer.append("Nama Bahan,Kategori,Jenis Satuan,Harga Beli,Jumlah Beli,Satuan Beli,Satuan Dasar,Susut (%),Harga Efektif per Satuan Dasar\n")
            for (ing in ingredients) {
                val effective = com.kosio.app.domain.calculation.HppCalculator.calculateEffectiveUnitPrice(
                    purchasePrice = ing.purchasePrice,
                    purchaseQuantity = ing.purchaseQuantity,
                    purchaseUnit = ing.purchaseUnit,
                    customPackQuantity = ing.customPackQuantity,
                    wastePercent = ing.wastePercent
                )
                writer.append("\"${ing.name.replace("\"", "\"\"")}\",")
                writer.append("\"${(ing.category ?: "").replace("\"", "\"\"")}\",")
                writer.append("\"${ing.unitDimension.name}\",")
                writer.append("${ing.purchasePrice},")
                writer.append("${ing.purchaseQuantity},")
                writer.append("\"${ing.purchaseUnit}\",")
                writer.append("\"${ing.baseUnit}\",")
                writer.append("${ing.wastePercent},")
                writer.append("${effective}\n")
            }
        }
        return file
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
