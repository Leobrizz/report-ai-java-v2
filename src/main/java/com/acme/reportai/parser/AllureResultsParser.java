package com.acme.reportai.parser;

import com.acme.reportai.model.ExecutionReport;
import com.acme.reportai.model.TestCaseResult;
import com.acme.reportai.model.TestStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AllureResultsParser implements ReportParser {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean supports(Path source) {
        return Files.isDirectory(source) || (Files.isRegularFile(source) && source.getFileName().toString().endsWith("-result.json"));
    }

    @Override
    public ExecutionReport parse(Path source) throws Exception {
        ExecutionReport report = new ExecutionReport();
        report.setExecutionId("allure-" + System.currentTimeMillis());
        report.setProject("QA Automation");
        report.setEnvironment("UAT");
        report.setFramework("Allure + Selenium");

        Stream<Path> allureFiles = Files.isDirectory(source)
                ? Files.list(source).filter(p -> p.getFileName().toString().endsWith("-result.json"))
                : Stream.of(source);

        try (Stream<Path> files = allureFiles) {
            files
                .forEach(path -> {
                    try {
                        JsonNode root = mapper.readTree(path.toFile());
                        TestCaseResult tc = new TestCaseResult();
                        tc.setSource("allure");
                        tc.setId(text(root, "uuid", path.getFileName().toString()));
                        tc.setName(text(root, "name", "Caso sin nombre"));
                        tc.setFeature(text(root, "fullName", tc.getName()));
                        tc.setSuite(findLabel(root.path("labels"), "suite", "suite-desconocida"));
                        tc.setTags(findAllLabels(root.path("labels"), "tag"));
                        tc.setStatus(parseStatus(text(root, "status", "unknown")));
                        tc.setDurationMillis(root.path("stop").asLong(0L) > 0 ? Math.max(0L, root.path("stop").asLong() - root.path("start").asLong()) : 0L);
                        for (JsonNode step : root.path("steps")) {
                            tc.getSteps().add(text(step, "name", "step"));
                            if (parseStatus(text(step, "status", "unknown")).isFailure()) {
                                JsonNode details = step.path("statusDetails");
                                tc.setErrorMessage(text(details, "message", tc.getErrorMessage()));
                                tc.setStackTrace(text(details, "trace", tc.getStackTrace()));
                            }
                        }
                        if (tc.getErrorMessage() == null) {
                            JsonNode details = root.path("statusDetails");
                            tc.setErrorMessage(text(details, "message", null));
                            tc.setStackTrace(text(details, "trace", null));
                        }
                        report.getTestCases().add(tc);
                    } catch (Exception e) {
                        throw new RuntimeException("Error leyendo archivo Allure " + path, e);
                    }
                });
        }
        return report;
    }

    private String text(JsonNode node, String field, String def) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? def : value.asText(def);
    }

    private String findLabel(JsonNode labels, String name, String def) {
        for (JsonNode label : labels) {
            if (name.equalsIgnoreCase(text(label, "name", ""))) {
                return text(label, "value", def);
            }
        }
        return def;
    }

    private List<String> findAllLabels(JsonNode labels, String name) {
        List<String> result = new ArrayList<>();
        for (JsonNode label : labels) {
            if (name.equalsIgnoreCase(text(label, "name", ""))) {
                result.add(text(label, "value", ""));
            }
        }
        return result;
    }

    private TestStatus parseStatus(String raw) {
        return switch (raw.toLowerCase()) {
            case "passed" -> TestStatus.PASSED;
            case "failed" -> TestStatus.FAILED;
            case "broken" -> TestStatus.BROKEN;
            case "skipped", "pending", "unknown" -> TestStatus.SKIPPED;
            default -> TestStatus.UNKNOWN;
        };
    }
}
