package com.acme.reportai.service;

import com.acme.reportai.ai.AiProvider;
import com.acme.reportai.ai.CodexAiProvider;
import com.acme.reportai.ai.GeminiAiProvider;
import com.acme.reportai.ai.MockAiProvider;
import com.acme.reportai.ai.OllamaAiProvider;
import com.acme.reportai.ai.OpenAiCompatibleProvider;
import com.acme.reportai.analyzer.ReportAnalyzer;
import com.acme.reportai.cli.AppConfig;
import com.acme.reportai.exporter.WordReportExporter;
import com.acme.reportai.history.HistoryStore;
import com.acme.reportai.model.AnalysisResult;
import com.acme.reportai.model.ExecutionReport;
import com.acme.reportai.parser.AllureResultsParser;
import com.acme.reportai.parser.CucumberJsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ReportRunner {
    public static ReportRunResult run(AppConfig config) throws Exception {
        if (config.getCucumberPath() == null && config.getAllurePath() == null) {
            throw new IllegalArgumentException("Debes seleccionar un cucumber.json o una carpeta allure-results");
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
            merged.getTestCases().addAll(new CucumberJsonParser().parse(config.getCucumberPath()).getTestCases());
        }

        if (config.getAllurePath() != null) {
            if (!Files.exists(config.getAllurePath())) {
                throw new IllegalArgumentException("No existe la ruta allure: " + config.getAllurePath());
            }
            merged.getTestCases().addAll(new AllureResultsParser().parse(config.getAllurePath()).getTestCases());
        }

        AiProvider aiProvider = buildAiProvider(config);
        AnalysisResult analysis = new ReportAnalyzer(aiProvider).analyze(merged);

        HistoryStore historyStore = new HistoryStore();
        historyStore.dumpAnalysis(config.getOutputDir(), analysis);

        Path historyPath = null;
        if (config.isHistoricalMode()) {
            Path historyBaseDir = config.getHistoryDir() != null ? config.getHistoryDir() : config.getOutputDir().resolve("history");
            if ("new".equalsIgnoreCase(config.getHistoryMode())) {
                historyPath = historyStore.writeNewHistoryFile(historyBaseDir, analysis);
            } else {
                historyStore.appendToHistoryDir(historyBaseDir, analysis);
                historyPath = historyBaseDir.resolve("executions.jsonl");
            }
            config.setLastHistoryFile(historyPath);
        }

        Path docx = new WordReportExporter().export(config.getOutputDir(), analysis, config, historyPath != null);
        return new ReportRunResult(docx, config.getOutputDir().resolve("analysis.json"), historyPath);
    }

    private static AiProvider buildAiProvider(AppConfig config) {
        String mode = config.getAiMode() == null ? "mock" : config.getAiMode().toLowerCase();
        return switch (mode) {
            case "ollama", "hollama" -> new OllamaAiProvider(config.getAiBaseUrl(), config.getAiModel());
            case "openai" -> new OpenAiCompatibleProvider(config.getAiBaseUrl(), config.getAiModel(), config.getAiApiKey());
            case "codex" -> new CodexAiProvider(config.getAiBaseUrl(), config.getAiModel(), config.getAiApiKey());
            case "gemini" -> new GeminiAiProvider(config.getAiBaseUrl(), config.getAiModel(), config.getAiApiKey());
            default -> new MockAiProvider();
        };
    }
}
