package com.acme.reportai.analyzer;

public class RuleBasedFailureClassifier {
    public String classify(String normalizedMessage) {
        String msg = normalizedMessage == null ? "" : normalizedMessage;
        if (msg.contains("timeoutexception") || msg.contains("expected condition failed") || msg.contains("timeout")) {
            return "timeout";
        }
        if (msg.contains("nosuchelementexception") || msg.contains("staleelementreference") || msg.contains("elementclickintercepted")) {
            return "selenium";
        }
        if (msg.contains("connectexception") || msg.contains("502") || msg.contains("503") || msg.contains("504")) {
            return "ambiente";
        }
        if (msg.contains("cotizacion") && msg.contains("no se encontro")) {
            return "datos";
        }
        if (msg.contains("assertionerror") || msg.contains("expected") || msg.contains("actual")) {
            return "funcional";
        }
        return "funcional";
    }
}
