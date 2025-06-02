package com.bboxxtrack.Service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Service
public class PdfReportService {

    /**
     * Builds a professional PDF report using iText 5.
     *
     * @param companyName your company name to display in the header
     * @param metrics     an ordered map of metric‐name→count
     * @param reportDate  the date to show on the report
     * @return a byte[] containing the PDF
     */
    public byte[] generateReport(String companyName,
                                 Map<String, Long> metrics,
                                 Date reportDate) throws Exception {
        // 1) output buffer & document setup
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 90, 36);
        PdfWriter.getInstance(document, baos);
        document.open();

        // 2) logo in top‐left
        ClassPathResource logoRes = new ClassPathResource("static/images/bboxx.png");
        Image logo = Image.getInstance(logoRes.getURL());
        logo.scaleToFit(100, 50);
        logo.setAlignment(Image.ALIGN_LEFT);
        document.add(logo);

        // 3) title and date centered
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph(companyName, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        String dateText = new SimpleDateFormat("yyyy-MM-dd").format(reportDate);
        Paragraph datePara = new Paragraph("Report Date: " + dateText, dateFont);
        datePara.setAlignment(Element.ALIGN_CENTER);
        document.add(datePara);

        document.add(Chunk.NEWLINE);

        // 4) metrics table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // header cells
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        PdfPCell h1 = new PdfPCell(new Phrase("Metric", headFont));
        h1.setBackgroundColor(BaseColor.LIGHT_GRAY);
        h1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(h1);

        PdfPCell h2 = new PdfPCell(new Phrase("Count", headFont));
        h2.setBackgroundColor(BaseColor.LIGHT_GRAY);
        h2.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(h2);

        // data rows
        Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
        for (Map.Entry<String, Long> e : metrics.entrySet()) {
            PdfPCell cell1 = new PdfPCell(new Phrase(e.getKey(), rowFont));
            cell1.setPadding(5);
            table.addCell(cell1);

            PdfPCell cell2 = new PdfPCell(new Phrase(e.getValue().toString(), rowFont));
            cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell2.setPadding(5);
            table.addCell(cell2);
        }

        document.add(table);

        document.close();
        return baos.toByteArray();
    }
}
