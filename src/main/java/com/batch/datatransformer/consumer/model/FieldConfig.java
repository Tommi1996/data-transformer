package com.batch.datatransformer.consumer.model;

public class FieldConfig {
    private String name;
    private String type;

    public FieldConfig(){}

    public FieldConfig(String name,
                       String type) {
        this.name = name;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
