package com.acme.reportai.exporter;

import com.acme.reportai.cli.AppConfig;
import com.acme.reportai.model.AnalysisResult;
import com.acme.reportai.model.ExecutionReport;
import com.acme.reportai.model.FailureCluster;
import com.acme.reportai.model.TestCaseResult;
import com.acme.reportai.model.TestStatus;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

public class WordReportExporter {
    private static final String BLUE = "1D4ED8";
    private static final String BLUE_DARK = "1E3A8A";
    private static final String BLUE_LIGHT = "DBEAFE";
    private static final String GREEN = "16A34A";
    private static final String GREEN_LIGHT = "DCFCE7";
    private static final String RED = "DC2626";
    private static final String RED_LIGHT = "FEE2E2";
    private static final String ORANGE = "EA580C";
    private static final String ORANGE_LIGHT = "FFEDD5";
    private static final String GRAY = "6B7280";
    private static final String GRAY_LIGHT = "F3F4F6";
    private static final String PURPLE = "7C3AED";
    private static final String PURPLE_LIGHT = "EDE9FE";
    private static final String WHITE = "FFFFFF";

    public Path export(Path outputDir, AnalysisResult analysis) throws Exception {
        return export(outputDir, analysis, "nuevo", "mock", false);
    }

    public Path export(Path outputDir, AnalysisResult analysis, AppConfig config, boolean historyUpdated) throws Exception {
        String mode = config != null ? config.getAnalysisMode() : "nuevo";
        String ai = config != null ? config.getAiMode() : "mock";
        return export(outputDir, analysis, mode, ai, historyUpdated);
    }

    public Path export(Path outputDir, AnalysisResult analysis, String mode, String aiMode, boolean historyUpdated) throws Exception {
        Files.createDirectories(outputDir);
        String normalizedMode = isHistorical(mode) ? "historico" : "nuevo";
        Path out = nextPath(outputDir, "allure-report-" + normalizedMode, ".docx");
        try (XWPFDocument doc = new XWPFDocument()) {
            cover(doc, analysis, normalizedMode, aiMode);
            kpis(doc, analysis, normalizedMode, historyUpdated);
            executive(doc, analysis, normalizedMode);
            suiteTable(doc, analysis);
            pageBreak(doc);
            clusters(doc, analysis);
            failedCases(doc, analysis);
            pageBreak(doc);
            technicalDetails(doc, analysis);
            actionPlan(doc, analysis);
            history(doc, normalizedMode, historyUpdated);
            try (FileOutputStream fos = new FileOutputStream(out.toFile())) { doc.write(fos); }
        }
        return out;
    }

    private void cover(XWPFDocument doc, AnalysisResult analysis, String mode, String aiMode) {
        ExecutionReport r = analysis.getExecutionReport();
        XWPFTable t = doc.createTable(1, 2);
        width(t, 9500);
        shade(t.getRow(0).getCell(0), BLUE_DARK);
        shade(t.getRow(0).getCell(1), BLUE);
        clear(t.getRow(0).getCell(0));
        clear(t.getRow(0).getCell(1));
        text(t.getRow(0).getCell(0), "REPORTE ALLURE", 26, true, WHITE);
        text(t.getRow(0).getCell(0), "Dashboard ejecutivo y tecnico de automatizacion", 13, false, "BFDBFE");
        text(t.getRow(0).getCell(0), safe(r.getProject()) + " | " + safe(r.getEnvironment()) + " | " + safe(r.getFramework()), 10, false, WHITE);
        text(t.getRow(0).getCell(0), "Fecha: " + r.getExecutionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), 10, false, "DBEAFE");
        text(t.getRow(0).getCell(1), "Salud de ejecucion", 13, true, WHITE);
        text(t.getRow(0).getCell(1), passRate(r) + "% passed", 24, true, WHITE);
        text(t.getRow(0).getCell(1), "Modo: " + mode + " | IA: " + safe(aiMode), 10, false, "DBEAFE");
    }

    private void kpis(XWPFDocument doc, AnalysisResult analysis, String mode, boolean historyUpdated) {
        ExecutionReport r = analysis.getExecutionReport();
        heading(doc, "1. Indicadores principales", "Vista rapida de la ejecucion procesada", BLUE_DARK);
        XWPFTable t = doc.createTable(2, 3);
        width(t, 9500);
        kpi(t.getRow(0).getCell(0), "TOTAL", String.valueOf(r.total()), "Escenarios procesados", BLUE_LIGHT, BLUE_DARK);
        kpi(t.getRow(0).getCell(1), "PASSED", String.valueOf(r.passed()), passRate(r) + "% de exito", GREEN_LIGHT, GREEN);
        kpi(t.getRow(0).getCell(2), "FAILED", String.valueOf(r.failed()), failRate(r) + "% con falla", RED_LIGHT, RED);
        kpi(t.getRow(1).getCell(0), "BROKEN", String.valueOf(count(r, TestStatus.BROKEN)), "Errores tecnicos", ORANGE_LIGHT, ORANGE);
        kpi(t.getRow(1).getCell(1), "SKIPPED", String.valueOf(r.skipped()), "No ejecutados", GRAY_LIGHT, GRAY);
        kpi(t.getRow(1).getCell(2), "CLUSTERS", String.valueOf(analysis.getRepeatedFailures().size()), "Errores agrupados", PURPLE_LIGHT, PURPLE);
        callout(doc, isHistorical(mode) ? "Modo historico: se actualiza executions.jsonl para comparar corridas." : "Analisis nuevo: se genera un Word independiente sin tocar el historico.", historyUpdated ? GREEN_LIGHT : GRAY_LIGHT, historyUpdated ? GREEN : GRAY);
    }

    private void executive(XWPFDocument doc, AnalysisResult analysis, String mode) {
        ExecutionReport r = analysis.getExecutionReport();
        heading(doc, "2. Resumen ejecutivo", "Lectura para lideres, QA y desarrollo", BLUE_DARK);
        XWPFTable t = doc.createTable(1, 2);
        width(t, 9500);
        shade(t.getRow(0).getCell(0), GRAY_LIGHT);
        shade(t.getRow(0).getCell(1), riskLight(r));
        clear(t.getRow(0).getCell(0));
        clear(t.getRow(0).getCell(1));
        text(t.getRow(0).getCell(0), "Lectura ejecutiva", 13, true, BLUE_DARK);
        text(t.getRow(0).getCell(0), "La ejecucion presenta " + r.failed() + " fallas sobre " + r.total() + " casos. El pass rate es " + passRate(r) + "%. La suite con mayor concentracion de fallas es " + topSuite(r) + ".", 10, false, "111827");
        text(t.getRow(0).getCell(1), "Riesgo principal", 13, true, risk(r));
        text(t.getRow(0).getCell(1), riskText(r) + ". Prioridad sugerida: " + priority(r) + ".", 10, false, "111827");
    }

    private void suiteTable(XWPFDocument doc, AnalysisResult analysis) {
        heading(doc, "3. Resumen por suite / producto", "Estado funcional por agrupador", BLUE_DARK);
        Map<String, List<TestCaseResult>> bySuite = analysis.getExecutionReport().getTestCases().stream().collect(Collectors.groupingBy(this::suite, LinkedHashMap::new, Collectors.toList()));
        XWPFTable t = doc.createTable(Math.max(2, bySuite.size() + 1), 7);
        width(t, 9500);
        header(t.getRow(0), "Suite", "Total", "Passed", "Failed", "Broken", "Pass rate", "Accion sugerida");
        int i = 1;
        for (var e : bySuite.entrySet()) {
            List<TestCaseResult> list = e.getValue();
            long total = list.size();
            long passed = list.stream().filter(x -> x.getStatus() == TestStatus.PASSED).count();
            long failed = list.stream().filter(x -> x.getStatus().isFailure()).count();
            long broken = list.stream().filter(x -> x.getStatus() == TestStatus.BROKEN).count();
            XWPFTableRow row = t.getRow(i++);
            set(row, 0, e.getKey()); set(row, 1, String.valueOf(total)); set(row, 2, String.valueOf(passed)); set(row, 3, String.valueOf(failed)); set(row, 4, String.valueOf(broken)); set(row, 5, pct(passed, total) + "%");
            set(row, 6, failed == 0 ? "Monitorear" : "Revisar fallas repetidas y evidencia Allure");
        }
    }

    private void clusters(XWPFDocument doc, AnalysisResult analysis) {
        heading(doc, "4. Clusters de fallas", "Errores repetidos agrupados por fingerprint y causa probable", BLUE_DARK);
        List<FailureCluster> clusters = analysis.getRepeatedFailures();
        if (clusters.isEmpty()) { callout(doc, "No se detectaron clusters de fallas repetidas.", GREEN_LIGHT, GREEN); return; }
        XWPFTable t = doc.createTable(Math.min(clusters.size(), 12) + 1, 7);
        width(t, 9500);
        header(t.getRow(0), "#", "Categoria", "Patron", "Casos", "Suites", "Causa probable", "Recomendacion");
        for (int i = 0; i < t.getNumberOfRows() - 1; i++) {
            FailureCluster c = clusters.get(i);
            XWPFTableRow row = t.getRow(i + 1);
            set(row, 0, String.valueOf(i + 1)); set(row, 1, safe(c.getCategory())); set(row, 2, compact(c.getNormalizedMessage(), 160)); set(row, 3, String.valueOf(c.getCases().size())); set(row, 4, c.getCases().stream().map(this::suite).distinct().limit(4).collect(Collectors.joining(", "))); set(row, 5, compact(c.getProbableCause(), 160)); set(row, 6, compact(c.getRecommendation(), 180));
        }
    }

    private void failedCases(XWPFDocument doc, AnalysisResult analysis) {
        heading(doc, "5. Casos fallidos prioritarios", "Detalle ordenado para investigacion", BLUE_DARK);
        List<TestCaseResult> failed = analysis.getExecutionReport().getTestCases().stream().filter(x -> x.getStatus().isFailure()).limit(20).toList();
        if (failed.isEmpty()) { callout(doc, "No hay casos fallidos para detallar.", GREEN_LIGHT, GREEN); return; }
        XWPFTable t = doc.createTable(failed.size() + 1, 6);
        width(t, 9500);
        header(t.getRow(0), "#", "Caso", "Suite", "Estado", "Tags", "Error resumido");
        for (int i = 0; i < failed.size(); i++) {
            TestCaseResult c = failed.get(i);
            XWPFTableRow row = t.getRow(i + 1);
            set(row, 0, String.valueOf(i + 1)); set(row, 1, compact(c.getName(), 120)); set(row, 2, suite(c)); set(row, 3, String.valueOf(c.getStatus())); set(row, 4, String.join(", ", c.getTags())); set(row, 5, compact(firstError(c), 180));
        }
    }

    private void technicalDetails(XWPFDocument doc, AnalysisResult analysis) {
        heading(doc, "6. Detalle tecnico", "Informacion para reproducir y corregir", BLUE_DARK);
        List<TestCaseResult> failed = analysis.getExecutionReport().getTestCases().stream().filter(x -> x.getStatus().isFailure()).limit(10).toList();
        for (TestCaseResult c : failed) {
            callout(doc, safe(c.getName()) + "\nSuite: " + suite(c) + " | Estado: " + c.getStatus() + "\nError: " + compact(firstError(c), 500), RED_LIGHT, RED);
        }
    }

    private void actionPlan(XWPFDocument doc, AnalysisResult analysis) {
        heading(doc, "7. Plan de accion", "Siguiente paso sugerido", BLUE_DARK);
        XWPFTable t = doc.createTable(5, 4);
        width(t, 9500);
        header(t.getRow(0), "Prioridad", "Accion", "Responsable", "Resultado esperado");
        setRow(t.getRow(1), "Alta", "Revisar clusters con mas casos afectados", "QA + Dev", "Reducir fallas repetidas");
        setRow(t.getRow(2), "Alta", "Validar evidencias Allure y screenshots", "QA", "Confirmar causa raiz");
        setRow(t.getRow(3), "Media", "Separar fallas de ambiente/infra de fallas funcionales", "QA Automation", "Mejor triage");
        setRow(t.getRow(4), "Media", "Comparar contra historico", "QA Lead", "Detectar regresiones recurrentes");
    }

    private void history(XWPFDocument doc, String mode, boolean updated) {
        heading(doc, "8. Historico", "Comportamiento del guardado de ejecuciones", BLUE_DARK);
        callout(doc, updated ? "Esta corrida fue agregada al historico." : "Esta corrida fue generada como analisis nuevo y no modifico el historico.", updated ? GREEN_LIGHT : GRAY_LIGHT, updated ? GREEN : GRAY);
    }

    private void heading(XWPFDocument doc, String title, String sub, String color) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(260); p.setSpacingAfter(80);
        XWPFRun r = p.createRun(); r.setBold(true); r.setFontSize(16); r.setColor(color); r.setText(title);
        XWPFParagraph s = doc.createParagraph();
        XWPFRun sr = s.createRun(); sr.setFontSize(9); sr.setColor(GRAY); sr.setText(sub);
    }

    private void kpi(XWPFTableCell cell, String title, String value, String sub, String bg, String fg) { clear(cell); shade(cell, bg); text(cell, title, 9, true, fg); text(cell, value, 22, true, fg); text(cell, sub, 9, false, GRAY); }
    private void callout(XWPFDocument doc, String value, String bg, String fg) { XWPFTable t = doc.createTable(1, 1); width(t, 9500); shade(t.getRow(0).getCell(0), bg); clear(t.getRow(0).getCell(0)); text(t.getRow(0).getCell(0), value, 10, false, fg); }
    private void header(XWPFTableRow row, String... values) { for (int i = 0; i < values.length; i++) { shade(row.getCell(i), BLUE_DARK); set(row, i, values[i]); } }
    private void setRow(XWPFTableRow row, String... values) { for (int i = 0; i < values.length; i++) set(row, i, values[i]); }
    private void set(XWPFTableRow row, int index, String value) { XWPFTableCell cell = row.getCell(index); clear(cell); text(cell, safe(value), 9, false, "111827"); }
    private void text(XWPFTableCell cell, String value, int size, boolean bold, String color) { XWPFParagraph p = cell.addParagraph(); XWPFRun r = p.createRun(); r.setFontFamily("Segoe UI"); r.setFontSize(size); r.setBold(bold); r.setColor(color); r.setText(safe(value)); }
    private void clear(XWPFTableCell cell) { while (cell.getParagraphs().size() > 0) cell.removeParagraph(0); }
    private void shade(XWPFTableCell cell, String color) { CTTcPr pr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr(); CTShd shd = pr.isSetShd() ? pr.getShd() : pr.addNewShd(); shd.setVal(STShd.CLEAR); shd.setFill(color); }
    private void width(XWPFTable t, int width) { CTTblWidth w = t.getCTTbl().getTblPr() == null ? t.getCTTbl().addNewTblPr().addNewTblW() : t.getCTTbl().getTblPr().isSetTblW() ? t.getCTTbl().getTblPr().getTblW() : t.getCTTbl().getTblPr().addNewTblW(); w.setType(STTblWidth.DXA); w.setW(BigInteger.valueOf(width)); }
    private void pageBreak(XWPFDocument doc) { XWPFParagraph p = doc.createParagraph(); p.createRun().addBreak(BreakType.PAGE); }
    private Path nextPath(Path dir, String base, String ext) { int i = 1; Path p; do { p = dir.resolve(base + "-" + String.format("%03d", i++) + ext); } while (Files.exists(p)); return p; }
    private String suite(TestCaseResult c) { if (c.getSuite() != null && !c.getSuite().isBlank()) return c.getSuite(); if (c.getFeature() != null && !c.getFeature().isBlank()) return c.getFeature(); return "Sin suite"; }
    private String topSuite(ExecutionReport r) { return r.getTestCases().stream().filter(x -> x.getStatus().isFailure()).collect(Collectors.groupingBy(this::suite, Collectors.counting())).entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("Sin fallas"); }
    private long count(ExecutionReport r, TestStatus s) { return r.getTestCases().stream().filter(x -> x.getStatus() == s).count(); }
    private long passRate(ExecutionReport r) { return pct(r.passed(), r.total()); }
    private long failRate(ExecutionReport r) { return pct(r.failed(), r.total()); }
    private long pct(long part, long total) { return total == 0 ? 0 : Math.round(part * 100.0 / total); }
    private boolean isHistorical(String mode) { return "historico".equalsIgnoreCase(mode) || "history".equalsIgnoreCase(mode) || "historical".equalsIgnoreCase(mode); }
    private String firstError(TestCaseResult c) { return c.getErrorMessage() != null && !c.getErrorMessage().isBlank() ? c.getErrorMessage() : c.getStackTrace(); }
    private String riskText(ExecutionReport r) { if (r.failed() == 0) return "Ejecucion estable"; if (failRate(r) >= 30) return "Alto volumen de fallas"; return "Fallas puntuales a revisar"; }
    private String risk(ExecutionReport r) { if (r.failed() == 0) return GREEN; if (failRate(r) >= 30) return RED; return ORANGE; }
    private String riskLight(ExecutionReport r) { if (r.failed() == 0) return GREEN_LIGHT; if (failRate(r) >= 30) return RED_LIGHT; return ORANGE_LIGHT; }
    private String priority(ExecutionReport r) { if (r.failed() == 0) return "Baja"; if (failRate(r) >= 30) return "Alta"; return "Media"; }
    private String compact(String value, int max) { String s = safe(value).replaceAll("\\s+", " ").trim(); return s.length() <= max ? s : s.substring(0, max - 3) + "..."; }
    private String safe(String value) { return value == null || value.isBlank() ? "-" : value; }
}
