package com.batch.datatransformer.consumer.model;

import java.util.ArrayList;
import java.util.List;

public class FileConfig {
    private String fileName;
    private String key;
    private boolean applyDistinct;
    private ArrayList<LinkConfig> links;
    private List<FilterConditionConfig> filterConditionConfigs;
    private List<FilterLinkConfig> filterLinkConfigs;
    private List<FieldConfig> fields;

    public FileConfig() {
    }

    public boolean isApplyDistinct() {
        return applyDistinct;
    }

    public void setApplyDistinct(boolean applyDistinct) {
        this.applyDistinct = applyDistinct;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<FieldConfig> getFields() {
        return fields;
    }

    public void setFields(List<FieldConfig> fields) {
        this.fields = fields;
    }

    public ArrayList<LinkConfig> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<LinkConfig> links) {
        this.links = links;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<FilterConditionConfig> getFilterConditionConfigs() {
        return filterConditionConfigs;
    }

    public void setFilterConditionConfigs(List<FilterConditionConfig> filterConditionConfigs) {
        this.filterConditionConfigs = filterConditionConfigs;
    }

    public List<FilterLinkConfig> getFilterLinkConfigs() {
        return filterLinkConfigs;
    }

    public void setFilterLinkConfigs(List<FilterLinkConfig> filterLinkConfigs) {
        this.filterLinkConfigs = filterLinkConfigs;
    }
}
