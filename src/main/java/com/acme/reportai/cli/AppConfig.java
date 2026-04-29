package com.acme.reportai.cli;

import java.nio.file.Path;

public class AppConfig {
    private Path cucumberPath;
    private Path allurePath;
    private Path outputDir = Path.of("output");
    private Path historyDir;
    private String historyMode = "append";
    private String project = "QA Automation";
    private String env = "UAT";
    private String framework = "Cucumber + Selenium";
    private String aiMode = "mock";
    private String aiBaseUrl = "http://localhost:11434";
    private String aiModel = "llama3.1";
    private String aiApiKey = "";
    private Path lastHistoryFile;

    public Path getCucumberPath() { return cucumberPath; }
    public void setCucumberPath(Path cucumberPath) { this.cucumberPath = cucumberPath; }
    public Path getAllurePath() { return allurePath; }
    public void setAllurePath(Path allurePath) { this.allurePath = allurePath; }
    public Path getOutputDir() { return outputDir; }
    public void setOutputDir(Path outputDir) { this.outputDir = outputDir; }
    public Path getHistoryDir() { return historyDir; }
    public void setHistoryDir(Path historyDir) { this.historyDir = historyDir; }
    public String getHistoryMode() { return historyMode; }
    public void setHistoryMode(String historyMode) { this.historyMode = historyMode; }
    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }
    public String getEnv() { return env; }
    public void setEnv(String env) { this.env = env; }
    public String getFramework() { return framework; }
    public void setFramework(String framework) { this.framework = framework; }
    public String getAiMode() { return aiMode; }
    public void setAiMode(String aiMode) { this.aiMode = aiMode; }
    public String getAiBaseUrl() { return aiBaseUrl; }
    public void setAiBaseUrl(String aiBaseUrl) { this.aiBaseUrl = aiBaseUrl; }
    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }
    public String getAiApiKey() { return aiApiKey; }
    public void setAiApiKey(String aiApiKey) { this.aiApiKey = aiApiKey; }
    public Path getLastHistoryFile() { return lastHistoryFile; }
    public void setLastHistoryFile(Path lastHistoryFile) { this.lastHistoryFile = lastHistoryFile; }
}
