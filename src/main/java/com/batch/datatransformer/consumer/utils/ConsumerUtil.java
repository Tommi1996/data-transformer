package com.batch.datatransformer.consumer.utils;

import com.batch.datatransformer.DataTransformerApplication;
import com.batch.datatransformer.consumer.model.FieldConfig;
import com.batch.datatransformer.consumer.model.FileConfig;
import com.batch.datatransformer.consumer.model.TypeSupported;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ConsumerUtil {

    public void logEndTask(String task, Logger logger) {
        logger.info("End " + task + ".");
    }

    public void logStartAtomicJoinTask(String joinType, String currentEntity, String targetEntity, int currentPosArrLength, int targetPosArrLength, Logger logger) {
        String log = "Start " + joinType + " JOIN between " +
                currentEntity + " (" + currentPosArrLength + " Records)" +
                " and " +
                targetEntity + " (" + targetPosArrLength + " Records)" +
                "...";
        logger.info(log);
    }

    public static Map<String, Object> genericObjectToHashmap(Object obj) {
        ObjectMapper oMapper = new ObjectMapper();
        return oMapper.convertValue(obj, Map.class);
    }

    public List<List<FieldConfig>> getColumnTracker(List<FileConfig> config){
        return config.stream()
                .map(x -> x.getFields().stream()
                        .map(y -> new FieldConfig(x.getFileName().toUpperCase() + "." + y.getName().toUpperCase(),
                                y.getType())).collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    public List<FieldConfig> getColumnTracker(FileConfig singleFileConfig) {
        return singleFileConfig.getFields().stream()
                        .map(y -> new FieldConfig(singleFileConfig.getFileName().toUpperCase() + "." + y.getName().toUpperCase(),
                                y.getType())).collect(Collectors.toList());
    }

    public String getTranscodedFieldType(String fieldType, String tsFilePath, Logger logger) throws IOException {
        TypeSupported ts = new TypeSupported();
        ts.setTypeSupported(JsonUtil.jsonReader(DataTransformerApplication.getContextFilePath(tsFilePath), logger));
        Map<String, ArrayList<String>> typeSupported = ts.getTypeSupported();
        return typeSupported.entrySet().stream()
                .filter(x -> x.getValue().toString().toUpperCase().contains(fieldType)).toList()
                .get(0)
                .getKey()
                .toUpperCase();
    }
}
