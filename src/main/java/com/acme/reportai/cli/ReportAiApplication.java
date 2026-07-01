package com.acme.reportai.cli;

import com.acme.reportai.service.ReportRunResult;
import com.acme.reportai.service.ReportRunner;
import com.acme.reportai.ui.ReportAiGuiApplication;
import javax.swing.SwingUtilities;

public class ReportAiApplication {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            SwingUtilities.invokeLater(ReportAiGuiApplication::show);
            return;
        }

        AppConfig config = new ArgsParser().parse(args);
        ReportRunResult result = ReportRunner.run(config);

        System.out.println("Analisis completado.");
        System.out.println("DOCX generado en: " + result.getDocxPath().toAbsolutePath());
        System.out.println("JSON generado en: " + result.getAnalysisJsonPath().toAbsolutePath());
        if (result.getHistoryPath() != null) {
            System.out.println("Historial actualizado en: " + result.getHistoryPath().toAbsolutePath());
        } else {
            System.out.println("Analisis nuevo: historial sin modificar.");
        }
    }
}
