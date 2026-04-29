package com.acme.reportai.model;

public class AiClassification {
    private String category;
    private String probableCause;
    private String recommendation;
    private String summary;

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getProbableCause() { return probableCause; }
    public void setProbableCause(String probableCause) { this.probableCause = probableCause; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
