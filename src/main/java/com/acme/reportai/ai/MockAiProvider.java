package com.acme.reportai.ai;

import com.acme.reportai.model.AiClassification;
import com.acme.reportai.model.FailureCluster;

public class MockAiProvider implements AiProvider {
    @Override
    public AiClassification classify(FailureCluster cluster) {
        AiClassification c = new AiClassification();
        c.setCategory(cluster.getCategory() != null ? cluster.getCategory() : "funcional");
        c.setProbableCause(switch (c.getCategory()) {
            case "timeout" -> "Sincronización inestable o elemento no visible a tiempo.";
            case "selenium" -> "Problema de locator, DOM dinámico o referencia a elemento inválida.";
            case "ambiente" -> "Dependencia externa o servicio no disponible.";
            case "datos" -> "Datos base o precondiciones inconsistentes.";
            default -> "La precondición de negocio no quedó satisfecha antes de validar el flujo.";
        });
        c.setRecommendation(switch (c.getCategory()) {
            case "timeout" -> "Agregar waits explícitos y revisar performance del paso fallido.";
            case "selenium" -> "Revisar locator, sincronización y estabilidad del DOM.";
            case "ambiente" -> "Validar servicio, red y dependencias antes de ejecutar la suite.";
            case "datos" -> "Asegurar generación de datos y limpieza del ambiente.";
            default -> "Revisar lógica funcional y datos previos a la ejecución.";
        });
        c.setSummary("Clasificación generada en modo mock para validar el flujo end-to-end.");
        return c;
    }
}
