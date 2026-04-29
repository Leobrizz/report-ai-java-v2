package com.acme.reportai.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnalysisResult {
    private ExecutionReport executionReport;
    private final List<FailureCluster> repeatedFailures = new ArrayList<>();
    private Map<String, Long> categoryCount;

    public ExecutionReport getExecutionReport() { return executionReport; }
    public void setExecutionReport(ExecutionReport executionReport) { this.executionReport = executionReport; }
    public List<FailureCluster> getRepeatedFailures() { return repeatedFailures; }
    public Map<String, Long> getCategoryCount() { return categoryCount; }
    public void setCategoryCount(Map<String, Long> categoryCount) { this.categoryCount = categoryCount; }
}
