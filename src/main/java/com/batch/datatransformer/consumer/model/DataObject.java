package com.batch.datatransformer.consumer.model;

import java.util.List;
import java.util.Map;

public class DataObject {
    private List<List<Map<String, Object>>> dataList;
    private List<FileConfig> config;
    private List<List<FieldConfig>> columnTracker;

    public DataObject(List<List<Map<String, Object>>> dataList,
                      List<FileConfig> config,
                      List<List<FieldConfig>> columnTracker) {
        this.dataList = dataList;
        this.config = config;
        this.columnTracker = columnTracker;
    }

    public void setDataList(List<List<Map<String, Object>>> dataList) {
        this.dataList = dataList;
    }

    public List<List<FieldConfig>> getColumnTracker() {
        return columnTracker;
    }

    public void setColumnTracker(List<List<FieldConfig>> columnsTracker) {
        this.columnTracker = columnsTracker;
    }

    public List<List<Map<String, Object>>> getDataList() {
        return dataList;
    }

    public void getDataList(List<List<Map<String, Object>>> fileStreamArray) {
        this.dataList = fileStreamArray;
    }

    public List<FileConfig> getConfig() {
        return config;
    }

    public void setConfig(List<FileConfig> config) {
        this.config = config;
    }
}
