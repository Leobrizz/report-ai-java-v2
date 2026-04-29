package com.acme.reportai.cli;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ReportAiDesktopApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ReportAiDesktopApp::createAndShow);
    }

    private static void createAndShow() {
        JFrame frame = new JFrame("Report AI - Launcher");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(820, 480);

        JPanel panel = new JPanel(new GridLayout(0, 3, 8, 8));

        JTextField cucumber = new JTextField();
        JTextField allure = new JTextField();
        JTextField output = new JTextField("output");
        JTextField history = new JTextField("output/history");
        JComboBox<String> aiMode = new JComboBox<>(new String[]{"mock", "ollama", "hollama", "openai", "codex", "gemini"});
        JTextField baseUrl = new JTextField("http://localhost:11434");
        JTextField model = new JTextField("llama3.1");
        JTextField apiKey = new JTextField();

        addRow(panel, "Cucumber JSON", cucumber, true);
        addRow(panel, "Allure Results Dir", allure, true);
        addRow(panel, "Output Dir", output, true);
        addRow(panel, "History Dir", history, true);
        addRow(panel, "AI Mode", aiMode);
        addRow(panel, "AI Base URL", baseUrl, false);
        addRow(panel, "AI Model", model, false);
        addRow(panel, "AI API Key", apiKey, false);

        JTextArea log = new JTextArea();
        log.setEditable(false);
        JScrollPane logScroll = new JScrollPane(log);

        JButton run = new JButton("Generar reporte");
        run.addActionListener(e -> {
            try {
                AppConfig cfg = new AppConfig();
                if (!cucumber.getText().isBlank()) cfg.setCucumberPath(new File(cucumber.getText()).toPath());
                if (!allure.getText().isBlank()) cfg.setAllurePath(new File(allure.getText()).toPath());
                cfg.setOutputDir(new File(output.getText()).toPath());
                cfg.setHistoryDir(new File(history.getText()).toPath());
                cfg.setAiMode((String) aiMode.getSelectedItem());
                cfg.setAiBaseUrl(baseUrl.getText());
                cfg.setAiModel(model.getText());
                cfg.setAiApiKey(apiKey.getText());

                ReportAiRunner.run(cfg);
                log.append("OK: reporte generado en " + cfg.getOutputDir().toAbsolutePath() + "\n");
            } catch (Exception ex) {
                log.append("ERROR: " + ex.getMessage() + "\n");
            }
        });

        frame.setLayout(new BorderLayout(8, 8));
        frame.add(panel, BorderLayout.NORTH);
        frame.add(run, BorderLayout.CENTER);
        frame.add(logScroll, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private static void addRow(JPanel panel, String label, JTextField text, boolean folderChooser) {
        panel.add(new JLabel(label));
        panel.add(text);
        JButton browse = new JButton("Browse");
        browse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(folderChooser ? JFileChooser.FILES_AND_DIRECTORIES : JFileChooser.FILES_ONLY);
            int result = chooser.showOpenDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                text.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        panel.add(browse);
    }

    private static void addRow(JPanel panel, String label, JComponent comp) {
        panel.add(new JLabel(label));
        panel.add(comp);
        panel.add(new JLabel(""));
    }
}
