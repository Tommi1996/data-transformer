package com.batch.datatransformer.consumer.model;

public class LinkConfig {
    private JoinKeysConfig joinKeys;
    private String fileName;
    private String fileNameTarget;
    private String joinType;

    public LinkConfig() {
    }

    public JoinKeysConfig getJoinKeys() {
        return joinKeys;
    }

    public void setJoinKeys(JoinKeysConfig joinKeys) {
        this.joinKeys = joinKeys;
    }

    public String getJoinType() {
        return joinType;
    }

    public void setJoinType(String joinType) {
        this.joinType = joinType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileNameTarget() {
        return fileNameTarget;
    }

    public void setFileNameTarget(String fileNameTarget) {
        this.fileNameTarget = fileNameTarget;
    }
}
