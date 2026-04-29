package com.acme.reportai.analyzer;

import com.acme.reportai.ai.AiProvider;
import com.acme.reportai.model.AiClassification;
import com.acme.reportai.model.AnalysisResult;
import com.acme.reportai.model.ExecutionReport;
import com.acme.reportai.model.FailureCluster;
import com.acme.reportai.model.TestCaseResult;
import com.acme.reportai.util.ErrorNormalizer;
import com.acme.reportai.util.FingerprintGenerator;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportAnalyzer {
    private final ErrorNormalizer normalizer = new ErrorNormalizer();
    private final FingerprintGenerator fingerprintGenerator = new FingerprintGenerator();
    private final RuleBasedFailureClassifier classifier = new RuleBasedFailureClassifier();
    private final AiProvider aiProvider;

    public ReportAnalyzer(AiProvider aiProvider) {
        this.aiProvider = aiProvider;
    }

    public AnalysisResult analyze(ExecutionReport executionReport) {
        AnalysisResult result = new AnalysisResult();
        result.setExecutionReport(executionReport);

        Map<String, FailureCluster> clusters = new LinkedHashMap<>();
        for (TestCaseResult tc : executionReport.getTestCases()) {
            if (!tc.getStatus().isFailure()) {
                continue;
            }
            String normalized = normalizer.normalize(tc.getErrorMessage() != null ? tc.getErrorMessage() : tc.getName());
            String fingerprint = fingerprintGenerator.fingerprint(normalized);
            FailureCluster cluster = clusters.computeIfAbsent(fingerprint, fp -> {
                FailureCluster c = new FailureCluster();
                c.setFingerprint(fp);
                c.setNormalizedMessage(normalized);
                c.setCategory(classifier.classify(normalized));
                return c;
            });
            cluster.getCases().add(tc);
        }

        for (FailureCluster cluster : clusters.values()) {
            AiClassification ai = aiProvider.classify(cluster);
            cluster.setCategory(ai.getCategory());
            cluster.setProbableCause(ai.getProbableCause());
            cluster.setRecommendation(ai.getRecommendation());
            result.getRepeatedFailures().add(cluster);
        }

        result.getRepeatedFailures().sort(Comparator.comparingInt((FailureCluster c) -> c.getCases().size()).reversed());
        result.setCategoryCount(result.getRepeatedFailures().stream()
                .collect(Collectors.groupingBy(FailureCluster::getCategory, LinkedHashMap::new, Collectors.counting())));
        return result;
    }
}
