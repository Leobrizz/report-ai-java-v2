package com.acme.reportai.model;

import java.util.ArrayList;
import java.util.List;

public class TestCaseResult {
    private String id;
    private String name;
    private String feature;
    private String suite;
    private String source;
    private TestStatus status = TestStatus.UNKNOWN;
    private long durationMillis;
    private List<String> tags = new ArrayList<>();
    private List<String> steps = new ArrayList<>();
    private String errorMessage;
    private String stackTrace;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFeature() { return feature; }
    public void setFeature(String feature) { this.feature = feature; }
    public String getSuite() { return suite; }
    public void setSuite(String suite) { this.suite = suite; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public TestStatus getStatus() { return status; }
    public void setStatus(TestStatus status) { this.status = status; }
    public long getDurationMillis() { return durationMillis; }
    public void setDurationMillis(long durationMillis) { this.durationMillis = durationMillis; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
}
