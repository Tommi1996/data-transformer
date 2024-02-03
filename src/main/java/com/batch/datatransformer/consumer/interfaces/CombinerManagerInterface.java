package com.batch.datatransformer.consumer.interfaces;

import com.batch.datatransformer.consumer.model.FileConfig;

import java.util.List;
import java.util.Map;

public interface CombinerManagerInterface {
    List<Map<String, Object>> joinData(List<List<Map<String, Object>>> inputDataMapped,
                                       List<FileConfig> config) throws Exception, Error;
}
