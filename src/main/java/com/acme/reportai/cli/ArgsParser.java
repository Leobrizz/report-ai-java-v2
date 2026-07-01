package com.acme.reportai.cli;

import java.nio.file.Path;

public class ArgsParser {
    public AppConfig parse(String[] args) {
        AppConfig cfg = new AppConfig();
        for (String arg : args) {
            if (arg.startsWith("--cucumber=")) cfg.setCucumberPath(Path.of(arg.substring("--cucumber=".length())));
            else if (arg.startsWith("--allure=")) cfg.setAllurePath(Path.of(arg.substring("--allure=".length())));
            else if (arg.startsWith("--output=")) cfg.setOutputDir(Path.of(arg.substring("--output=".length())));
            else if (arg.startsWith("--history=")) cfg.setHistoryDir(Path.of(arg.substring("--history=".length())));
            else if (arg.startsWith("--history.mode=")) cfg.setHistoryMode(arg.substring("--history.mode=".length()));
            else if (arg.startsWith("--project=")) cfg.setProject(arg.substring("--project=".length()));
            else if (arg.startsWith("--env=")) cfg.setEnv(arg.substring("--env=".length()));
            else if (arg.startsWith("--framework=")) cfg.setFramework(arg.substring("--framework=".length()));
            else if (arg.startsWith("--ai.mode=")) cfg.setAiMode(arg.substring("--ai.mode=".length()));
            else if (arg.startsWith("--ai.baseUrl=")) cfg.setAiBaseUrl(arg.substring("--ai.baseUrl=".length()));
            else if (arg.startsWith("--ai.model=")) cfg.setAiModel(arg.substring("--ai.model=".length()));
            else if (arg.startsWith("--ai.apiKey=")) cfg.setAiApiKey(arg.substring("--ai.apiKey=".length()));
        }
        return cfg;
    }
}
