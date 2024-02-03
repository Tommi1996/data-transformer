package com.batch.datatransformer.consumer.model;

import java.util.ArrayList;

public class JoinKeysConfig {
    private String logicalOperator;
    private ArrayList<String> joinKeyFields;

    public JoinKeysConfig() {
    }

    public String getLogicalOperator() {
        return logicalOperator;
    }

    public void setLogicalOperator(String logicalOperator) {
        this.logicalOperator = logicalOperator;
    }

    public ArrayList<String> getJoinKeyFields() {
        return joinKeyFields;
    }

    public void setJoinKeyFields(ArrayList<String> joinKeyFields) {
        this.joinKeyFields = joinKeyFields;
    }
}
