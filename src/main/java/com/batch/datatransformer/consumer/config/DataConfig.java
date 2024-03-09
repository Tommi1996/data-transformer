package com.batch.datatransformer.consumer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataConfig {
    @Value("${json.config.path}")
    private String jsonConfigPath;

    @Value("${csv.delimiter}")
    private String csvDelimiter;

    @Value("${force.not.join}")
    private Boolean forceNotJoin;

    private String datasetName;

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getJsonConfigPath() {
        return jsonConfigPath;
    }

    public String getCsvDelimiter() {
        return csvDelimiter;
    }

    public Boolean getForceNotJoin() {
        return forceNotJoin;
    }
}
