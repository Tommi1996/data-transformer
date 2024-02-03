package com.batch.datatransformer.consumer.model;

import java.util.ArrayList;
import java.util.Map;

public class TypeSupported {
    private Map<String, ArrayList<String>> typeSupported;

    public TypeSupported() {

    }

    public Map<String, ArrayList<String>> getTypeSupported() {
        return typeSupported;
    }

    public void setTypeSupported(Map<String, ArrayList<String>> typeSupported) {
        this.typeSupported = typeSupported;
    }
}
