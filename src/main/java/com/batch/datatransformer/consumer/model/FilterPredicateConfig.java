package com.batch.datatransformer.consumer.model;

import java.util.Map;
import java.util.function.Predicate;

public class FilterPredicateConfig {
    private String conditionKey;
    private Predicate<Map<String, Object>> predicate;

    public FilterPredicateConfig(String conditionKey, Predicate<Map<String, Object>> predicate){
        this.conditionKey = conditionKey;
        this.predicate = predicate;
    }

    public String getConditionKey() {
        return conditionKey;
    }

    public void setConditionKey(String conditionKey) {
        this.conditionKey = conditionKey;
    }

    public Predicate<Map<String, Object>> getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate<Map<String, Object>> predicate) {
        this.predicate = predicate;
    }
}