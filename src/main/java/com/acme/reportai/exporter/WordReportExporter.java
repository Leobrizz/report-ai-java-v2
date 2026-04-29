package com.acme.reportai.exporter;

import com.acme.reportai.model.AnalysisResult;
import com.acme.reportai.model.FailureCluster;
import com.acme.reportai.model.TestCaseResult;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

public class WordReportExporter {
    public Path export(Path outputDir, AnalysisResult analysis) throws Exception {
        Files.createDirectories(outputDir);
        Path out = nextIndexedReportPath(outputDir);

        try (XWPFDocument doc = new XWPFDocument()) {
            title(doc, "Reporte de análisis – Ejecución automatizada");
            subtitle(doc, "Fecha: " + analysis.getExecutionReport().getExecutionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            paragraph(doc, "Ambiente: " + analysis.getExecutionReport().getEnvironment());
            paragraph(doc, "Framework: " + analysis.getExecutionReport().getFramework());
            paragraph(doc, "Proyecto: " + analysis.getExecutionReport().getProject());

            heading(doc, "Resumen ejecutivo", 1);
            XWPFTable summary = doc.createTable(4, 2);
            setTableWidth(summary, "7000");
            summary.getRow(0).getCell(0).setText("Total de casos");
            summary.getRow(0).getCell(1).setText(String.valueOf(analysis.getExecutionReport().total()));
            summary.getRow(1).getCell(0).setText("Pasados");
            summary.getRow(1).getCell(1).setText(String.valueOf(analysis.getExecutionReport().passed()));
            summary.getRow(2).getCell(0).setText("Fallados");
            summary.getRow(2).getCell(1).setText(String.valueOf(analysis.getExecutionReport().failed()));
            summary.getRow(3).getCell(0).setText("Clusters detectados");
            summary.getRow(3).getCell(1).setText(String.valueOf(analysis.getRepeatedFailures().size()));

            heading(doc, "Errores repetidos detectados", 1);
            if (analysis.getRepeatedFailures().isEmpty()) {
                paragraph(doc, "No se detectaron fallos repetidos en la ejecución analizada.");
            } else {
                for (FailureCluster cluster : analysis.getRepeatedFailures()) {
                    heading(doc, cluster.getCategory().toUpperCase() + " – " + cluster.getCases().size() + " caso(s)", 2);
                    paragraph(doc, "Mensaje normalizado: " + cluster.getNormalizedMessage());
                    paragraph(doc, "Causa probable: " + safe(cluster.getProbableCause()));
                    paragraph(doc, "Recomendación: " + safe(cluster.getRecommendation()));
                    XWPFTable table = doc.createTable(Math.max(2, cluster.getCases().size() + 1), 4);
                    setTableWidth(table, "9000");
                    header(table.getRow(0), "Caso", "Suite", "Estado", "Error original");
                    int rowIdx = 1;
                    for (TestCaseResult tc : cluster.getCases()) {
                        XWPFTableRow row = table.getRow(rowIdx++);
                        row.getCell(0).setText(safe(tc.getName()));
                        row.getCell(1).setText(safe(tc.getSuite()));
                        row.getCell(2).setText(tc.getStatus().name());
                        row.getCell(3).setText(safe(tc.getErrorMessage()));
                    }
                }
            }

            heading(doc, "Detalle de causas", 1);
            if (analysis.getCategoryCount() != null) {
                analysis.getCategoryCount().forEach((category, count) -> paragraph(doc, "• " + category + ": " + count));
            }

            try (FileOutputStream fos = new FileOutputStream(out.toFile())) {
                doc.write(fos);
            }
        }
        return out;
    }

    private Path nextIndexedReportPath(Path outputDir) throws Exception {
        int max = 0;
        try (Stream<Path> files = Files.list(outputDir)) {
            for (Path p : files.toList()) {
                String name = p.getFileName().toString();
                if (name.equals("executive-report.docx")) {
                    max = Math.max(max, 1);
                } else if (name.startsWith("executive-report-") && name.endsWith(".docx")) {
                    String idx = name.substring("executive-report-".length(), name.length() - ".docx".length());
                    try {
                        max = Math.max(max, Integer.parseInt(idx));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return outputDir.resolve(String.format("executive-report-%03d.docx", max + 1));
    }

    private void title(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        setSpacing(p, 200, 120);
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(18);
        r.setText(text);
    }

    private void subtitle(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        setSpacing(p, 80, 120);
        XWPFRun r = p.createRun();
        r.setItalic(true);
        r.setFontSize(10);
        r.setText(text);
    }

    private void heading(XWPFDocument doc, String text, int level) {
        XWPFParagraph p = doc.createParagraph();
        setSpacing(p, 220, 80);
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(level == 1 ? 14 : 12);
        r.setText(text);
    }

    private void paragraph(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        setSpacing(p, 60, 60);
        XWPFRun r = p.createRun();
        r.setFontSize(11);
        r.setText(text);
    }

    private void header(XWPFTableRow row, String... titles) {
        for (int i = 0; i < titles.length; i++) {
            row.getCell(i).setText(titles[i]);
        }
    }

    private void setTableWidth(XWPFTable table, String width) {
        CTTblWidth tblWidth = table.getCTTbl().getTblPr().isSetTblW() ? table.getCTTbl().getTblPr().getTblW() : table.getCTTbl().getTblPr().addNewTblW();
        tblWidth.setW(new BigInteger(width));
        tblWidth.setType(STTblWidth.DXA);
    }

    private void setSpacing(XWPFParagraph p, int before, int after) {
        CTPPr ppr = p.getCTP().isSetPPr() ? p.getCTP().getPPr() : p.getCTP().addNewPPr();
        CTSpacing spacing = ppr.isSetSpacing() ? ppr.getSpacing() : ppr.addNewSpacing();
        spacing.setBefore(BigInteger.valueOf(before));
        spacing.setAfter(BigInteger.valueOf(after));
    }

    private String safe(String text) {
        return text == null || text.isBlank() ? "-" : text;
    }
}
