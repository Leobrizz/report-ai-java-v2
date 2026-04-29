package com.acme.reportai.parser;

import com.acme.reportai.model.ExecutionReport;
import com.acme.reportai.model.TestCaseResult;
import com.acme.reportai.model.TestStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CucumberJsonParser implements ReportParser {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean supports(Path source) {
        return Files.isRegularFile(source) && source.toString().endsWith(".json");
    }

    @Override
    public ExecutionReport parse(Path source) throws Exception {
        JsonNode root = mapper.readTree(source.toFile());
        ExecutionReport report = new ExecutionReport();
        report.setExecutionId("cucumber-" + System.currentTimeMillis());
        report.setProject("QA Automation");
        report.setEnvironment("UAT");
        report.setFramework("Cucumber + Selenium");

        if (!root.isArray()) {
            throw new IllegalArgumentException("El cucumber.json debe ser un array de features");
        }

        for (JsonNode featureNode : root) {
            String featureName = text(featureNode, "name", "Feature sin nombre");
            JsonNode elements = featureNode.path("elements");
            if (!elements.isArray()) {
                continue;
            }
            for (JsonNode element : elements) {
                TestCaseResult tc = new TestCaseResult();
                tc.setSource("cucumber");
                tc.setId(text(element, "id", text(element, "name", "sin-id")));
                tc.setName(text(element, "name", "Scenario sin nombre"));
                tc.setFeature(featureName);
                tc.setSuite(featureName);
                tc.setTags(readTags(element.path("tags")));

                long totalDurationNanos = 0;
                TestStatus finalStatus = TestStatus.PASSED;
                for (JsonNode step : iterable(element.path("steps"))) {
                    tc.getSteps().add(text(step, "name", "step"));
                    JsonNode result = step.path("result");
                    totalDurationNanos += result.path("duration").asLong(0L);
                    String rawStatus = text(result, "status", "unknown");
                    TestStatus stepStatus = parseStatus(rawStatus);
                    if (stepStatus.isFailure()) {
                        finalStatus = stepStatus;
                        tc.setErrorMessage(text(result, "error_message", null));
                        tc.setStackTrace(text(result, "error_message", null));
                    } else if (finalStatus != TestStatus.FAILED && stepStatus == TestStatus.SKIPPED) {
                        finalStatus = TestStatus.SKIPPED;
                    }
                }

                tc.setStatus(finalStatus);
                tc.setDurationMillis(totalDurationNanos / 1_000_000);
                report.getTestCases().add(tc);
            }
        }
        return report;
    }

    private List<String> readTags(JsonNode tagsNode) {
        List<String> tags = new ArrayList<>();
        if (tagsNode != null && tagsNode.isArray()) {
            for (JsonNode tag : tagsNode) {
                tags.add(text(tag, "name", ""));
            }
        }
        return tags;
    }

    private String text(JsonNode node, String field, String def) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? def : value.asText(def);
    }

    private Iterable<JsonNode> iterable(JsonNode array) {
        return () -> array == null ? List.<JsonNode>of().iterator() : array.elements();
    }

    private TestStatus parseStatus(String raw) {
        return switch (raw.toLowerCase()) {
            case "passed" -> TestStatus.PASSED;
            case "failed" -> TestStatus.FAILED;
            case "broken" -> TestStatus.BROKEN;
            case "skipped", "pending", "undefined" -> TestStatus.SKIPPED;
            default -> TestStatus.UNKNOWN;
        };
    }
}
