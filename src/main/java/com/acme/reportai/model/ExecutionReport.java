package com.acme.reportai.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExecutionReport {
    private String executionId;
    private String project;
    private String environment;
    private String framework;
    private LocalDateTime executionDate = LocalDateTime.now();
    private final List<TestCaseResult> testCases = new ArrayList<>();

    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getFramework() { return framework; }
    public void setFramework(String framework) { this.framework = framework; }
    public LocalDateTime getExecutionDate() { return executionDate; }
    public void setExecutionDate(LocalDateTime executionDate) { this.executionDate = executionDate; }
    public List<TestCaseResult> getTestCases() { return testCases; }

    public int total() { return testCases.size(); }
    public long passed() { return testCases.stream().filter(t -> t.getStatus() == TestStatus.PASSED).count(); }
    public long failed() { return testCases.stream().filter(t -> t.getStatus().isFailure()).count(); }
    public long skipped() { return testCases.stream().filter(t -> t.getStatus() == TestStatus.SKIPPED).count(); }
}
