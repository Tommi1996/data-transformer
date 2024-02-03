package com.batch.datatransformer.consumer.interfaces;

import com.batch.datatransformer.consumer.model.FileConfig;

import java.util.List;
import java.util.Map;

public interface MapperManagerInterface {
    List<List<Map<String, Object>>> mapFileStreamDataToObject(List<List<String>> fileStreamArray,
                                                              List<FileConfig> config) throws Exception, Error;
}
