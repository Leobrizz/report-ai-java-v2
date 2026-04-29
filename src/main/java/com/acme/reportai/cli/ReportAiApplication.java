package com.acme.reportai.cli;

public class ReportAiApplication {
    public static void main(String[] args) throws Exception {
        AppConfig config = new ArgsParser().parse(args);
        var docx = ReportAiRunner.run(config);

        System.out.println("Análisis completado.");
        System.out.println("DOCX generado en: " + docx.toAbsolutePath());
        System.out.println("JSON generado en: " + config.getOutputDir().resolve("analysis.json").toAbsolutePath());
        var historyPath = config.getLastHistoryFile() != null ? config.getLastHistoryFile() : config.getOutputDir().resolve("history/executions.jsonl");
        System.out.println("Historial actualizado en: " + historyPath.toAbsolutePath());
    }
}
