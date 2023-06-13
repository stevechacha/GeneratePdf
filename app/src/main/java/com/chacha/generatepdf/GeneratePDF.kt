package com.chacha.generatepdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import com.chacha.generatepdf.R

fun generatePDF(context: Context, directory: File, transactionList: List<TransactionHistory>) {
    val pageHeight = 2000
    val pageWidth = 1000
    val pdfDocument = PdfDocument()
    val paint = Paint()
    val title = Paint()
    val marginSize = 50f
    val marginWith = 40f
    val totalHeight = pageHeight + marginSize.toInt()
    val totalWith = pageWidth + marginWith.toInt()

    val myPageInfo = PageInfo.Builder(pageWidth, totalHeight, 1).create()
    val pageList = mutableListOf<PdfDocument.Page>()

    var yOffset = 0f // Vertical offset for content within each page
    var pageNumber = 1

    var currentPage = pdfDocument.startPage(myPageInfo)
    var canvas: Canvas = currentPage.canvas ?: return


    canvas.drawColor(Color.WHITE) // Set background color to white

    val bitmap: Bitmap? = drawableToBitmap(context.resources.getDrawable(R.drawable.ic_launcher_foreground))
    val scaleBitmap: Bitmap? = Bitmap.createScaledBitmap(bitmap!!, 120, 120, false)
    canvas.drawBitmap(scaleBitmap!!, 240f, 40f, paint)

    val headerText = "Statement Details"
    val customerName = "Stephen Chacha Marwa"
    val mobileNumber = "0746656813"
    val emailAddress = "stevechacha4@gmail.com"
    val statementPeriod = "12 Jun 2022 - 12 Jun 2023"
    val requestDate = "12 Jun 2023"


// Call the drawHeader function
    drawHeader(canvas, headerText, customerName, mobileNumber, emailAddress, statementPeriod, requestDate, paint)



    title.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    title.textSize = 15f
    title.color = Color.BLACK // Set text color to black
    canvas.drawText("Jetpack Compose", 400f, 100f, title)
    canvas.drawText("Stephen ChaCha", 400f, 80f, title)
    title.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
    title.color = Color.BLACK // Set text color to black
    title.textSize = 20f
    title.textAlign = Paint.Align.CENTER
    canvas.drawText("Transaction History", 80f, 200f, title)




    val columnWidths = listOf(
        pageWidth.toFloat() * 0.45f, // Transaction Detail
        pageWidth.toFloat() * 0.1f, // Recipient No
        pageWidth.toFloat() * 0.1f, // Date
        pageWidth.toFloat() * 0.1f, // Money In
        pageWidth.toFloat() * 0.1f, // Money Out
        pageWidth.toFloat() * 0.15f // Money Balance
    )

    yOffset += 200f // Increase the vertical offset for the header

    // Draw table headers
    drawTableHeader(canvas, yOffset, columnWidths)

    yOffset += 50f // Increase the vertical offset for the header row

    var totalMoneyIn = 0.0
    var totalMoneyOut = 0.0
    var totalMoneyBalance = 0.0

    for (transaction in transactionList) {
        // Check if the next transaction exceeds the page height
        if (yOffset + 50f > pageHeight) {
            pdfDocument.finishPage(currentPage)

            pageNumber++
            val newPage = pdfDocument.startPage(myPageInfo)
            pageList.add(newPage)
            currentPage = newPage
            canvas = currentPage.canvas ?: break
            canvas.drawColor(Color.WHITE) // Set background color to white

            yOffset = 40f // Reset the vertical offset for the new page

            // Draw page number
            val pageNumberText = "Page $pageNumber of ${pageList.size + 1}"
            val pageNumberPaint = Paint()
            pageNumberPaint.textSize = 15f
            pageNumberPaint.color = Color.BLACK
            pageNumberPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(pageNumberText, 40f, 30f, pageNumberPaint)


            // Draw logo in footer
            val footerBitmap: Bitmap? = drawableToBitmap(context.resources.getDrawable(R.drawable.ic_launcher_foreground))
            val scaledFooterBitmap: Bitmap? = Bitmap.createScaledBitmap(footerBitmap!!, 120, 120, false)
            canvas.drawBitmap(scaledFooterBitmap!!, pageWidth - 160f, pageHeight - 160f, paint)

            // Draw table headers
            drawTableHeader(canvas, yOffset, columnWidths)

            yOffset += 50f // Increase the vertical offset for the header row
        }

        // Draw row separator before the transaction detail
        drawRowSeparator(canvas, yOffset, columnWidths)
        drawTransactionRow(canvas, yOffset, transaction, columnWidths, paint)
        yOffset += 50f // Increase the vertical offset for the transaction row

        // Update totals
        totalMoneyIn += transaction.moneyIn
        totalMoneyOut += transaction.moneyOut
        totalMoneyBalance += transaction.moneyBalance
    }

    // Draw totals row
    drawTotalsRow(canvas, yOffset, totalMoneyIn, totalMoneyOut, totalMoneyBalance, columnWidths, paint)

    // Draw row separator after the last transaction
    drawRowSeparator(canvas, yOffset + 50f, columnWidths)


    // Draw page number on each page
    for (page in pageList) {
        val pageNumberText = "Page ${pageNumber++}"
        val pageNumberPaint = Paint()
        pageNumberPaint.textSize = 15f
        pageNumberPaint.color = Color.BLACK
        pageNumberPaint.textAlign = Paint.Align.LEFT
        val pageNumberCanvas = page.canvas ?: continue
        pageNumberCanvas.drawText(pageNumberText, 40f, 50f, pageNumberPaint)

        // Draw logo in footer
        val footerBitmap: Bitmap? =
            drawableToBitmap(context.resources.getDrawable(R.drawable.ic_launcher_foreground))
        val scaledFooterBitmap: Bitmap? = Bitmap.createScaledBitmap(footerBitmap!!, 120, 120, false)
        pageNumberCanvas.drawBitmap(
            scaledFooterBitmap!!,
            pageWidth - 160f,
            pageHeight - 160f,
            paint
        )
    }

    pdfDocument.finishPage(currentPage)

    // Save the PDF file
    val file = File(directory, "TransactionHistory.pdf")
    try {
        val fileOutputStream = FileOutputStream(file)
        pdfDocument.writeTo(fileOutputStream)
        fileOutputStream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    pdfDocument.close()
    Toast.makeText(context, "PDF generated successfully", Toast.LENGTH_SHORT).show()
}


private fun drawHeader(
    canvas: Canvas,
    headerText: String,
    customerName: String,
    mobileNumber: String,
    emailAddress: String,
    statementPeriod: String,
    requestDate: String,
    paint: Paint
) {
    paint.textSize = 14f
    paint.typeface = Typeface.DEFAULT_BOLD
    paint.color = Color.BLACK // Set text color to black
    paint.textAlign = Paint.Align.LEFT

    val headerYOffset = 50f
    val detailsYOffset = headerYOffset + 30f

    // Draw header text
    canvas.drawText(headerText, 40f, headerYOffset, paint)

    // Draw customer details
    val detailsText = "Customer Name: $customerName\n" +
            "Mobile Number: $mobileNumber\n" +
            "Email Address: $emailAddress\n" +
            "Statement Period: $statementPeriod\n" +
            "Request Date: $requestDate"

    paint.textSize = 12f
    val detailsLines = detailsText.split("\n")
    for ((index, line) in detailsLines.withIndex()) {
        canvas.drawText(line, 40f, detailsYOffset + index * 20f, paint)
    }



   /* // Draw customer information
    val customerInfoPaint = Paint()
    customerInfoPaint.textSize = 15f
    customerInfoPaint.color = Color.BLACK
    val customerInfoOffset = 300f
    canvas.drawText("Customer Name: $customerName", 40f, customerInfoOffset, customerInfoPaint)
    canvas.drawText("Mobile Number: $mobileNumber", 40f, customerInfoOffset + 20f, customerInfoPaint)
    canvas.drawText("Email Address: $emailAddress", 40f, customerInfoOffset + 40f, customerInfoPaint)
    canvas.drawText("Statement Period: $statementPeriod", 40f, customerInfoOffset + 60f, customerInfoPaint)
    canvas.drawText("Request Date: $requestDate", 40f, customerInfoOffset + 80f, customerInfoPaint)*/


}


private fun drawTableHeader(canvas: Canvas, yOffset: Float, columnWidths: List<Float>) {
    val headerPaint = Paint()
    headerPaint.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
    headerPaint.textSize = 16f
    headerPaint.color = Color.BLACK // Set text color to black
    headerPaint.textAlign = Paint.Align.CENTER

    var xOffset = 40f // Initial horizontal offset for headers

    // Draw each header column and vertical column separator
    val headers = listOf(
        "Transaction Detail",
        "Recipient No",
        "Date",
        "Money In",
        "Money Out",
        "Money Balance"
    )
    for ((index, header) in headers.withIndex()) {
        // Draw column separator
        canvas.drawLine(xOffset, yOffset, xOffset, yOffset + 50f, headerPaint)

        // Draw header text
        canvas.drawText(header, xOffset + columnWidths[index] / 2, yOffset + 30f, headerPaint)

        xOffset += columnWidths[index]
    }
}

private fun drawTransactionRow(
    canvas: Canvas,
    yOffset: Float,
    transaction: TransactionHistory,
    columnWidths: List<Float>,
    paint: Paint
) {
    paint.textSize = 14f
    paint.color = Color.BLACK // Set text color to black

    val cellHeight = 50f // Height of each row cell

    var xOffset = 40f // Initial horizontal offset for transaction details (adjusted to match header)

    // Draw each transaction detail column and row separator
    val transactionDetails = listOf(
        transaction.transactionDetail.takeIf { it.isNotEmpty() } ?: "-",
        transaction.recipientNo.takeIf { it.isNotEmpty() } ?: "-",
        transaction.date.takeIf { it.isNotEmpty() } ?: "-",
        transaction.moneyIn.toString(),
        transaction.moneyOut.toString(),
        transaction.moneyBalance.toString()
    )

    val textAlignmentList = listOf(
        Paint.Align.LEFT, // Transaction Detail
        Paint.Align.CENTER, // Recipient No
        Paint.Align.CENTER, // Date
        Paint.Align.RIGHT, // Money In
        Paint.Align.RIGHT, // Money Out
        Paint.Align.RIGHT // Money Balance
    )

    for ((index, detail) in transactionDetails.withIndex()) {
        paint.textAlign = textAlignmentList[index]

        val textLines = splitTextIntoLines(detail, columnWidths[index], paint)
        val totalHeight = textLines.size * paint.textSize // Calculate the total height of text lines

        // Calculate the vertical centering offset
        val verticalOffset = (cellHeight - totalHeight) / 2

        // Adjust column width if necessary to accommodate long transaction detail text
        val adjustedColumnWidth = if (index == 0) {
            Math.min(columnWidths[index] - 20f, columnWidths[index + 1] - xOffset - 20f)
        } else {
            columnWidths[index]
        }

        for ((lineIndex, line) in textLines.withIndex()) {
            val lineYOffset =
                yOffset + verticalOffset + lineIndex * paint.textSize // Increase the yOffset for each line
            canvas.drawText(line, xOffset + adjustedColumnWidth / 2, lineYOffset, paint)
        }
        xOffset += columnWidths[index]

        // Draw row separator
        canvas.drawLine(xOffset, yOffset, xOffset, yOffset + 50f, paint)
    }
}


private fun drawRowSeparator(
    canvas: Canvas,
    yOffset: Float,
    columnWidths: List<Float>
) {
    val separatorPaint = Paint()
    separatorPaint.style = Paint.Style.STROKE
    separatorPaint.color = Color.BLACK
    separatorPaint.strokeWidth = 1f
    val totalColumnWidth = columnWidths.sum() // Calculate the total width of all columns

    canvas.drawLine(40f, yOffset, 40f + totalColumnWidth, yOffset, separatorPaint)
}

private fun splitTextIntoLines(text: String, maxWidth: Float, paint: Paint): List<String> {
    val words = text.split(" ")
    var currentLine = ""
    val lines = mutableListOf<String>()

    for (word in words) {
        val lineWithWord = if (currentLine.isEmpty()) word else "$currentLine $word"
        val lineWidth = paint.measureText(lineWithWord)

        if (lineWidth <= maxWidth) {
            currentLine = lineWithWord
        } else {
            lines.add(currentLine)
            currentLine = word
        }
    }

    lines.add(currentLine) // Add the last line

    return lines
}

private fun drawTotalsRow(
    canvas: Canvas,
    yOffset: Float,
    totalMoneyIn: Double,
    totalMoneyOut: Double,
    totalMoneyBalance: Double,
    columnWidths: List<Float>,
    paint: Paint
) {
    paint.textSize = 12f
    paint.typeface = Typeface.DEFAULT_BOLD
    paint.color = Color.BLACK // Set text color to black

    var xOffset = 140f // Initial horizontal offset for total values

    // Draw each total column and row separator
    val totals = listOf(
        "Total",
        "",
        "",
        totalMoneyIn.toString(),
        totalMoneyOut.toString(),
        totalMoneyBalance.toString()
    )
    for ((index, total) in totals.withIndex()) {
        val alignment = when (index) {
            0 -> Paint.Align.LEFT // Align "Total" to the left
            in 3..5 -> Paint.Align.RIGHT // Align money values to the right
            else -> Paint.Align.CENTER // Align empty columns to the center
        }
        paint.textAlign = alignment

        val totalLines = splitTextIntoLines(total, columnWidths[index], paint)
        for ((lineIndex, line) in totalLines.withIndex()) {
            val lineYOffset = yOffset + 30f + lineIndex * 20f // Increase the yOffset for each line
            canvas.drawText(line, xOffset, lineYOffset, paint)
        }
        xOffset += columnWidths[index]

        // Draw row separator
        canvas.drawLine(xOffset, yOffset, xOffset, yOffset + 50f, paint)
    }
}


fun drawableToBitmap(drawable: Drawable): Bitmap? {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }
    val bitmap =
        Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}


data class TransactionHistory(
    val transactionDetail: String,
    val recipientNo: String,
    val date: String,
    val moneyIn: Double,
    val moneyOut: Double,
    val moneyBalance: Double
)

val transactionList = listOf(
    TransactionHistory(
        "Customer Withdrawal At Customer Withdrawal At  Agent Till 424470 - CORAL LAND JOWAN interp BLUE VALLEY",
        "123456789",
        "2023-04-01",
        100.0,
        0.0,
        100.0
    ),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 234563.0, 999998.0, 50.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 234563.0, 999998.0, 50.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 234563.0, 999998.0, 50.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 234563.0, 999998.0, 50.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 234563.0, 999998.0, 50.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 234563.0, 50.0, 98787.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 234563.0, 50.0, 98787.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 234563.0, 50.0, 98787.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 234563.0, 50.0, 98787.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 234563.0, 50.0, 98787.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 234563.0, 50.0, 98787.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 234563.0, 50.0, 98787.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 234563.0, 50.0, 50.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 234563.0, 50.0, 50.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 234563.0, 50.0, 50.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 234563.0, 50.0, 50.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 234563.0, 50.0, 50.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 234563.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Withdrawal", "555555555", "2023-04-03", 0.0, 20.0, 30.0),
    TransactionHistory("Payment received", "123456789", "2023-04-04", 50.0, 0.0, 80.0),
    TransactionHistory("Payment received", "123456789", "2023-04-01", 100.0, 0.0, 100.0),
    TransactionHistory("Transfer to savings", "987654321", "2023-04-02", 0.0, 50.0, 50.0),


    )


























