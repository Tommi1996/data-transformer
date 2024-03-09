package com.batch.datatransformer.consumer.utils;

import com.batch.datatransformer.DataTransformerApplication;
import com.batch.datatransformer.consumer.config.DataConfig;
import com.batch.datatransformer.consumer.model.FileConfig;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PipelineUtil {

    private final DataConfig dataConfig;

    public PipelineUtil(DataConfig dataConfig) {
        this.dataConfig = dataConfig;
    }

    public void produceOutputCsv(List<Map<String, Object>> res, String fileName, Logger logger) {
        if(res != null && !res.isEmpty()) {
            try {
                String header = String.join(dataConfig.getCsvDelimiter(), res.get(0).keySet());

                List<String> values = res.stream()
                        .map(x -> String.join(dataConfig.getCsvDelimiter(), x.values().stream()
                                .map(Object::toString)
                                .toList()))
                        .collect(Collectors.toCollection(ArrayList::new));

                values.add(0, header);

                Path dir = Paths.get(DataTransformerApplication.getContextFilePath("output"));
                if(!Files.exists(dir)) {
                    Files.createDirectory(dir);
                }

                Path path = Paths.get(dir + "/" + fileName);
                Files.deleteIfExists(path);

                Files.createFile(path);

                Files.write(path, values, StandardCharsets.UTF_8);
            } catch (Exception ex) {
                logger.error("Error writing output file: " + ex);
            }
        }
    }

    public boolean allLinksNotEmpty(List<FileConfig> config) {
        return config.stream()
                .allMatch(x -> x.getLinks() != null && !x.getLinks().isEmpty());
    }
}
