package com.batch.datatransformer.consumer.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@Component
public class JsonUtil {

    public static Map<String, ArrayList<String>> jsonReader(String filePath, Logger logger) throws IOException {
        Map<String, ArrayList<String>> fieldProperties = null;
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(filePath)) {
            try {
                fieldProperties = (Map<String, ArrayList<String>>) jsonParser.parse(reader);
            } catch (Exception e) {
                logger.error("Errore durante il parsing del file json: " + e);
            }
        }
        return fieldProperties;
    }

    public static <T> T jsonReader(String filePath, Class<T> clazz) throws IOException {
        return new ObjectMapper().readValue(new File(filePath), clazz);
    }
}
