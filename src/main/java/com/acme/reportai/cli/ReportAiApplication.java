package com.acme.reportai.cli;

import com.acme.reportai.ai.AiProvider;
import com.acme.reportai.ai.MockAiProvider;
import com.acme.reportai.ai.OllamaAiProvider;
import com.acme.reportai.ai.OpenAiCompatibleProvider;
import com.acme.reportai.analyzer.ReportAnalyzer;
import com.acme.reportai.exporter.WordReportExporter;
import com.acme.reportai.history.HistoryStore;
import com.acme.reportai.model.AnalysisResult;
import com.acme.reportai.model.ExecutionReport;
import com.acme.reportai.parser.AllureResultsParser;
import com.acme.reportai.parser.CucumberJsonParser;
import java.nio.file.Files;
import java.util.UUID;

public class ReportAiApplication {
    public static void main(String[] args) throws Exception {
        AppConfig config = new ArgsParser().parse(args);
        if (config.getCucumberPath() == null && config.getAllurePath() == null) {
            throw new IllegalArgumentException("Debes indicar --cucumber=... o --allure=...");
        }

        ExecutionReport merged = new ExecutionReport();
        merged.setExecutionId(UUID.randomUUID().toString());
        merged.setProject(config.getProject());
        merged.setEnvironment(config.getEnv());
        merged.setFramework(config.getFramework());

        if (config.getCucumberPath() != null) {
            if (!Files.exists(config.getCucumberPath())) {
                throw new IllegalArgumentException("No existe el archivo cucumber: " + config.getCucumberPath());
            }
            ExecutionReport cucumber = new CucumberJsonParser().parse(config.getCucumberPath());
            merged.getTestCases().addAll(cucumber.getTestCases());
        }
        if (config.getAllurePath() != null) {
            if (!Files.exists(config.getAllurePath())) {
                throw new IllegalArgumentException("No existe la carpeta allure: " + config.getAllurePath());
            }
            ExecutionReport allure = new AllureResultsParser().parse(config.getAllurePath());
            merged.getTestCases().addAll(allure.getTestCases());
        }

        AiProvider aiProvider = switch (config.getAiMode().toLowerCase()) {
            case "ollama" -> new OllamaAiProvider(config.getAiBaseUrl(), config.getAiModel());
            case "openai" -> new OpenAiCompatibleProvider(config.getAiBaseUrl(), config.getAiModel(), config.getAiApiKey());
            default -> new MockAiProvider();
        };

        AnalysisResult analysis = new ReportAnalyzer(aiProvider).analyze(merged);
        HistoryStore historyStore = new HistoryStore();
        historyStore.dumpAnalysis(config.getOutputDir(), analysis);
        historyStore.append(config.getOutputDir(), analysis);

        WordReportExporter exporter = new WordReportExporter();
        var docx = exporter.export(config.getOutputDir(), analysis);

        System.out.println("Análisis completado.");
        System.out.println("DOCX generado en: " + docx.toAbsolutePath());
        System.out.println("JSON generado en: " + config.getOutputDir().resolve("analysis.json").toAbsolutePath());
        System.out.println("Historial actualizado en: " + config.getOutputDir().resolve("history/executions.jsonl").toAbsolutePath());
    }
}
