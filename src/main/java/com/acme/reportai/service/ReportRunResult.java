package com.acme.reportai.service;

import java.nio.file.Path;

public class ReportRunResult {
    private final Path docxPath;
    private final Path analysisJsonPath;
    private final Path historyPath;

    public ReportRunResult(Path docxPath, Path analysisJsonPath, Path historyPath) {
        this.docxPath = docxPath;
        this.analysisJsonPath = analysisJsonPath;
        this.historyPath = historyPath;
    }

    public Path getDocxPath() { return docxPath; }
    public Path getAnalysisJsonPath() { return analysisJsonPath; }
    public Path getHistoryPath() { return historyPath; }
}
