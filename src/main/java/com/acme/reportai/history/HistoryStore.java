package com.acme.reportai.history;

import com.acme.reportai.model.AnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

public class HistoryStore {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public void append(Path outputDir, AnalysisResult analysis) throws Exception {
        Path historyDir = outputDir.resolve("history");
        Files.createDirectories(historyDir);
        Path file = historyDir.resolve("executions.jsonl");

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("executionId", analysis.getExecutionReport().getExecutionId());
        row.put("date", analysis.getExecutionReport().getExecutionDate().toString());
        row.put("project", analysis.getExecutionReport().getProject());
        row.put("environment", analysis.getExecutionReport().getEnvironment());
        row.put("total", analysis.getExecutionReport().total());
        row.put("passed", analysis.getExecutionReport().passed());
        row.put("failed", analysis.getExecutionReport().failed());
        row.put("clusters", analysis.getRepeatedFailures().size());
        String line = mapper.writeValueAsString(row) + System.lineSeparator();
        Files.writeString(file, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public void dumpAnalysis(Path outputDir, AnalysisResult analysis) throws Exception {
        Files.createDirectories(outputDir);
        mapper.writerWithDefaultPrettyPrinter().writeValue(outputDir.resolve("analysis.json").toFile(), analysis);
    }
}
