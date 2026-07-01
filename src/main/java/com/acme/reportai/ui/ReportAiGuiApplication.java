package com.acme.reportai.ui;

import com.acme.reportai.cli.AppConfig;
import com.acme.reportai.service.ReportRunResult;
import com.acme.reportai.service.ReportRunner;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ReportAiGuiApplication {
    private static final String OLLAMA_BASE_URL = "http://localhost:11434";
    private static final String OLLAMA_MODEL = "llama3.1";
    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    private static final String GEMINI_MODEL = "gemini-flash-latest";

    private final JFrame frame = new JFrame("Report AI - Analizador de reportes");
    private final JComboBox<String> reportType = new JComboBox<>(new String[]{"Auto detectar", "Cucumber JSON", "Allure results"});
    private final JTextField inputPath = new JTextField();
    private final JTextField outputPath = new JTextField("output");
    private final JComboBox<String> aiMode = new JComboBox<>(new String[]{"Sin IA / Mock", "Ollama local", "OpenAI compatible", "Codex", "Gemini"});
    private final JTextField aiBaseUrl = new JTextField(OLLAMA_BASE_URL);
    private final JTextField aiModel = new JTextField(OLLAMA_MODEL);
    private final JTextField aiApiKey = new JTextField();
    private final JComboBox<String> analysisMode = new JComboBox<>(new String[]{"Con historico", "Analisis nuevo"});
    private final JTextField project = new JTextField("QA Automation");
    private final JTextField env = new JTextField("UAT");
    private final JTextArea log = new JTextArea(11, 80);
    private ReportRunResult lastResult;

    public static void show() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        new ReportAiGuiApplication().open();
    }

    private void open() {
        aiMode.addActionListener(e -> updateAiDefaults());
        updateAiDefaults();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(980, 720);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.add(header(), BorderLayout.NORTH);
        frame.add(form(), BorderLayout.CENTER);
        frame.add(actions(), BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void updateAiDefaults() {
        String selected = String.valueOf(aiMode.getSelectedItem());
        if ("Gemini".equals(selected)) {
            if (aiBaseUrl.getText().isBlank() || OLLAMA_BASE_URL.equals(aiBaseUrl.getText())) aiBaseUrl.setText(GEMINI_BASE_URL);
            if (aiModel.getText().isBlank() || OLLAMA_MODEL.equals(aiModel.getText())) aiModel.setText(GEMINI_MODEL);
            if (aiApiKey.getText().isBlank()) aiApiKey.setText(System.getenv("GEMINI_API_KEY") == null ? "" : System.getenv("GEMINI_API_KEY"));
        } else if ("Ollama local".equals(selected)) {
            if (aiBaseUrl.getText().isBlank() || GEMINI_BASE_URL.equals(aiBaseUrl.getText())) aiBaseUrl.setText(OLLAMA_BASE_URL);
            if (aiModel.getText().isBlank() || GEMINI_MODEL.equals(aiModel.getText())) aiModel.setText(OLLAMA_MODEL);
        }
    }

    private JPanel header() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(30, 64, 175));
        p.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
        JLabel title = new JLabel("Report AI Dashboard");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        JLabel sub = new JLabel("Carga Cucumber JSON o Allure results, elegi IA y genera reportes Word amigables");
        sub.setForeground(new Color(219, 234, 254));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JPanel texts = new JPanel(new BorderLayout());
        texts.setOpaque(false);
        texts.add(title, BorderLayout.NORTH);
        texts.add(sub, BorderLayout.SOUTH);
        p.add(texts, BorderLayout.WEST);
        return p;
    }

    private JPanel form() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 26, 10, 26));
        card.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 7, 7, 7);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        addRow(card, c, 0, "Tipo de reporte", reportType, null);
        addRow(card, c, 1, "Archivo o carpeta", inputPath, button("Buscar", this::chooseInput));
        addRow(card, c, 2, "Destino resultados", outputPath, button("Destino", this::chooseOutput));
        addRow(card, c, 3, "IA para analizar", aiMode, null);
        addRow(card, c, 4, "Base URL IA", aiBaseUrl, null);
        addRow(card, c, 5, "Modelo", aiModel, null);
        addRow(card, c, 6, "API Key", aiApiKey, null);
        addRow(card, c, 7, "Modo", analysisMode, null);
        addRow(card, c, 8, "Proyecto", project, null);
        addRow(card, c, 9, "Ambiente", env, null);

        log.setEditable(false);
        log.setFont(new Font("Consolas", Font.PLAIN, 12));
        log.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane sp = new JScrollPane(log);
        c.gridx = 0; c.gridy = 10; c.gridwidth = 3; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
        card.add(sp, c);
        return card;
    }

    private JPanel actions() {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(8, 20, 20, 20));
        JButton run = button("Generar reporte Word", this::runReport);
        JButton open = button("Abrir carpeta", this::openOutput);
        p.add(run);
        p.add(open);
        return p;
    }

    private JButton button(String text, Runnable action) {
        JButton b = new JButton(text);
        b.addActionListener(e -> action.run());
        return b;
    }

    private void addRow(JPanel p, GridBagConstraints c, int y, String label, java.awt.Component field, JButton button) {
        c.gridy = y; c.gridwidth = 1; c.weighty = 0; c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.weightx = 0.15;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(l, c);
        c.gridx = 1; c.weightx = 0.75;
        p.add(field, c);
        c.gridx = 2; c.weightx = 0.10;
        if (button != null) p.add(button, c); else p.add(new JLabel(""), c);
    }

    private void chooseInput() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setFileFilter(new FileNameExtensionFilter("JSON o carpeta Allure", "json"));
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            inputPath.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void chooseOutput() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            outputPath.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void runReport() {
        try {
            AppConfig cfg = buildConfig();
            log.setText("Iniciando analisis...\n");
            new SwingWorker<ReportRunResult, String>() {
                @Override protected ReportRunResult doInBackground() throws Exception { return ReportRunner.run(cfg); }
                @Override protected void done() {
                    try {
                        lastResult = get();
                        log.append("Reporte generado: " + lastResult.getDocxPath().toAbsolutePath() + "\n");
                        log.append("JSON generado: " + lastResult.getAnalysisJsonPath().toAbsolutePath() + "\n");
                        if (lastResult.getHistoryPath() != null) log.append("Historico actualizado: " + lastResult.getHistoryPath().toAbsolutePath() + "\n");
                        else log.append("Analisis nuevo: no se modifico el historico.\n");
                        JOptionPane.showMessageDialog(frame, "Reporte generado correctamente");
                    } catch (Exception ex) {
                        log.append("ERROR: " + ex.getMessage() + "\n");
                        JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private AppConfig buildConfig() {
        if (inputPath.getText().isBlank()) throw new IllegalArgumentException("Selecciona un archivo o carpeta de reporte");
        AppConfig cfg = new AppConfig();
        Path input = Path.of(inputPath.getText());
        String type = String.valueOf(reportType.getSelectedItem());
        if ("Allure results".equals(type) || ("Auto detectar".equals(type) && input.toFile().isDirectory())) cfg.setAllurePath(input);
        else cfg.setCucumberPath(input);
        cfg.setOutputDir(Path.of(outputPath.getText().isBlank() ? "output" : outputPath.getText()));
        cfg.setProject(project.getText().isBlank() ? "QA Automation" : project.getText());
        cfg.setEnv(env.getText().isBlank() ? "UAT" : env.getText());
        cfg.setAnalysisMode(analysisMode.getSelectedIndex() == 0 ? "historico" : "nuevo");
        String mode = mapAiMode();
        cfg.setAiMode(mode);
        if ("gemini".equals(mode)) {
            cfg.setAiBaseUrl(aiBaseUrl.getText().isBlank() ? GEMINI_BASE_URL : aiBaseUrl.getText());
            cfg.setAiModel(aiModel.getText().isBlank() ? GEMINI_MODEL : aiModel.getText());
            cfg.setAiApiKey(aiApiKey.getText().isBlank() ? System.getenv("GEMINI_API_KEY") : aiApiKey.getText());
        } else {
            cfg.setAiBaseUrl(aiBaseUrl.getText());
            cfg.setAiModel(aiModel.getText());
            cfg.setAiApiKey(aiApiKey.getText());
        }
        return cfg;
    }

    private String mapAiMode() {
        return switch (String.valueOf(aiMode.getSelectedItem())) {
            case "Ollama local" -> "ollama";
            case "OpenAI compatible" -> "openai";
            case "Codex" -> "codex";
            case "Gemini" -> "gemini";
            default -> "mock";
        };
    }

    private void openOutput() {
        try {
            File dir = lastResult != null ? lastResult.getDocxPath().getParent().toFile() : Path.of(outputPath.getText()).toFile();
            Desktop.getDesktop().open(dir);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "No pude abrir la carpeta: " + ex.getMessage());
        }
    }
}
