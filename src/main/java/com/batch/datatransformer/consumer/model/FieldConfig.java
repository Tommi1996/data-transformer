package com.batch.datatransformer.consumer.model;

public class FieldConfig {
    private String name;
    private String type;
    private String logicalFormatterFunc;

    public FieldConfig(){}

    public FieldConfig(String name,
                       String type,
                       String logicalFormatterFunc) {
        this.name = name;
        this.type = type;
        this.logicalFormatterFunc = logicalFormatterFunc;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getLogicalFormatterFunc() {
        return logicalFormatterFunc;
    }
}
