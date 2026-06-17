package com.signatureapp.service;

import com.signatureapp.model.Document;
import com.signatureapp.model.SignatureRequest;
import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

@Service
public class PdfSigningService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z")
            .withZone(ZoneId.systemDefault());

    public String generateSignedPdf(Document document, List<SignatureRequest> signatures) throws IOException {
        Path original = Path.of(document.getStoragePath());
        Path signed = original.resolveSibling("signed-" + document.getStoredName());

        try (PDDocument pdf = Loader.loadPDF(original.toFile())) {
            for (SignatureRequest signature : signatures) {
                int index = Math.max(0, Math.min(signature.getPageNumber() - 1, pdf.getNumberOfPages() - 1));
                PDPage page = pdf.getPage(index);
                float pageHeight = page.getMediaBox().getHeight();
                float pdfY = pageHeight - signature.getY();

                try (PDPageContentStream content = new PDPageContentStream(
                        pdf,
                        page,
                        PDPageContentStream.AppendMode.APPEND,
                        true,
                        true
                )) {
                    content.setNonStrokingColor(new Color(12, 107, 91));
                    content.addRect(signature.getX(), pdfY - 42, 190, 48);
                    content.fill();
                    content.setNonStrokingColor(Color.WHITE);
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                    content.newLineAtOffset(signature.getX() + 10, pdfY - 18);
                    content.showText("Signed by " + safe(signature.getSignerName()));
                    content.endText();
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
                    content.newLineAtOffset(signature.getX() + 10, pdfY - 32);
                    content.showText(FORMATTER.format(signature.getCompletedAt()));
                    content.endText();
                }
            }
            pdf.save(signed.toFile());
        }

        return signed.toString();
    }

    private String safe(String value) {
        return value == null ? "Signer" : value.replaceAll("[\\r\\n]", " ");
    }
}
