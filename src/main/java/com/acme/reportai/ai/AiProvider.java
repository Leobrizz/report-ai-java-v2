package com.acme.reportai.ai;

import com.acme.reportai.model.AiClassification;
import com.acme.reportai.model.FailureCluster;

public interface AiProvider {
    AiClassification classify(FailureCluster cluster);
}
