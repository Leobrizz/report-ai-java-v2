package com.acme.reportai.cli;

public class ReportAiApplication {
    public static void main(String[] args) throws Exception {
        AppConfig config = new ArgsParser().parse(args);
        ReportAiRunner.run(config);
        var docx = config.getOutputDir().resolve("executive-report.docx");

        System.out.println("Análisis completado.");
        System.out.println("DOCX generado en: " + docx.toAbsolutePath());
        System.out.println("JSON generado en: " + config.getOutputDir().resolve("analysis.json").toAbsolutePath());
        var historyPath = config.getHistoryDir() != null ? config.getHistoryDir().resolve("executions.jsonl") : config.getOutputDir().resolve("history/executions.jsonl");
        System.out.println("Historial actualizado en: " + historyPath.toAbsolutePath());
    }
}
