package com.checkspace.backend.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceService {

    public byte[] generateVerificationInvoice(
            String sellerName, String phone,
            String propertyTitle, String invoiceNumber) throws Exception {

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        // Company header
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 11);
        Font boldFont  = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);

        Paragraph title = new Paragraph("CHECKSPACE", headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph tagline = new Paragraph("India's Verified Property Marketplace", normalFont);
        tagline.setAlignment(Element.ALIGN_CENTER);
        document.add(tagline);

        document.add(Chunk.NEWLINE);

        Paragraph invoiceTitle = new Paragraph("TAX INVOICE", boldFont);
        invoiceTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(invoiceTitle);

        document.add(Chunk.NEWLINE);

        // Invoice details table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        table.addCell(new PdfPCell(new Phrase("Invoice Number:", boldFont)));
        table.addCell(new PdfPCell(new Phrase(invoiceNumber, normalFont)));

        table.addCell(new PdfPCell(new Phrase("Date:", boldFont)));
        table.addCell(new PdfPCell(new Phrase(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), normalFont)));

        table.addCell(new PdfPCell(new Phrase("Seller Name:", boldFont)));
        table.addCell(new PdfPCell(new Phrase(sellerName, normalFont)));

        table.addCell(new PdfPCell(new Phrase("Phone:", boldFont)));
        table.addCell(new PdfPCell(new Phrase(phone, normalFont)));

        table.addCell(new PdfPCell(new Phrase("Property:", boldFont)));
        table.addCell(new PdfPCell(new Phrase(propertyTitle, normalFont)));

        document.add(table);
        document.add(Chunk.NEWLINE);

        // Fee breakdown
        PdfPTable feeTable = new PdfPTable(3);
        feeTable.setWidthPercentage(100);

        feeTable.addCell(new PdfPCell(new Phrase("Description", boldFont)));
        feeTable.addCell(new PdfPCell(new Phrase("Amount (₹)", boldFont)));
        feeTable.addCell(new PdfPCell(new Phrase("GST 18% (₹)", boldFont)));

        feeTable.addCell(new PdfPCell(new Phrase("Property Verification Fee", normalFont)));
        feeTable.addCell(new PdfPCell(new Phrase("299.00", normalFont)));
        feeTable.addCell(new PdfPCell(new Phrase("53.82", normalFont)));

        document.add(feeTable);
        document.add(Chunk.NEWLINE);

        Paragraph total = new Paragraph("Total Amount Paid: ₹352.82", boldFont);
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);

        document.add(Chunk.NEWLINE);
        Paragraph note = new Paragraph(
                "Note: This invoice is for property verification service only. " +
                        "Commission of 0.75% + GST will be charged separately on successful sale.",
                normalFont);
        document.add(note);

        Paragraph footer = new Paragraph(
                "Checkspace Technologies Pvt. Ltd. | checkspace.in | GST: [YOUR_GSTIN]",
                normalFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }
}