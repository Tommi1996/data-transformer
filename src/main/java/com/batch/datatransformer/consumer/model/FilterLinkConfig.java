package com.batch.datatransformer.consumer.model;

public class FilterLinkConfig {
    private String conditionKey;
    private String currentConditionKey;
    private String targetConditionKey;
    private String logicalOperator;

    public FilterLinkConfig(){

    }

    public String getConditionKey() {
        return conditionKey;
    }

    public void setConditionKey(String conditionKey) {
        this.conditionKey = conditionKey;
    }

    public String getCurrentConditionKey() {
        return currentConditionKey;
    }

    public void setCurrentConditionKey(String currentConditionKey) {
        this.currentConditionKey = currentConditionKey;
    }

    public String getTargetConditionKey() {
        return targetConditionKey;
    }

    public void setTargetConditionKey(String targetConditionKey) {
        this.targetConditionKey = targetConditionKey;
    }

    public String getLogicalOperator() {
        return logicalOperator;
    }

    public void setLogicalOperator(String logicalOperator) {
        this.logicalOperator = logicalOperator;
    }
}