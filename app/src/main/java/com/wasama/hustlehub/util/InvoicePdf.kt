package com.wasama.hustlehub.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.wasama.hustlehub.data.local.ClientEntity
import com.wasama.hustlehub.data.local.ProjectEntity
import java.io.File

object InvoicePdf {
    fun generate(context: Context, client: ClientEntity, project: ProjectEntity, amount: Double, hours: Double): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply { textSize = 14f }

        canvas.drawText("HUSTLEHUB INVOICE", 40f, 60f, paint.apply { textSize = 22f; isFakeBoldText = true })
        canvas.drawText("Client: ${client.name} - ${client.company ?: ""}", 40f, 120f, Paint().apply { textSize = 14f })
        canvas.drawText("Project: ${project.title}", 40f, 150f, Paint().apply { textSize = 14f })
        canvas.drawText("Budget: R$amount ($hours hrs x R${client.ratePerHour})", 40f, 180f, Paint().apply { textSize = 14f })
        canvas.drawText("Bank: Capitec **1234", 40f, 240f, Paint().apply { textSize = 12f })
        canvas.drawText("Thank you for hustling smart!", 40f, 800f, Paint().apply { textSize = 12f })

        doc.finishPage(page)
        val file = File(context.filesDir, "invoices/${project.projectId}.pdf")
        file.parentFile?.mkdirs()
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }
}