package com.batch.datatransformer.datasource.model;

import com.batch.datatransformer.consumer.model.FileConfig;

import java.util.List;

public class DataSource {
    private List<List<String>> data;
    private List<FileConfig> config;

    public DataSource(List<List<String>> data, List<FileConfig> config) {
        this.data = data;
        this.config = config;
    }

    public List<List<String>> getData() {
        return data;
    }

    public void setData(List<List<String>> data) {
        this.data = data;
    }

    public List<FileConfig> getConfig() {
        return config;
    }

    public void setConfig(List<FileConfig> config) {
        this.config = config;
    }
}
