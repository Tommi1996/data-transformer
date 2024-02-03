package com.batch.datatransformer.consumer.interfaces;

import com.batch.datatransformer.consumer.model.FieldConfig;
import com.batch.datatransformer.consumer.model.FileConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FilterManagerInterface {
    List<List<Map<String, Object>>> filterData(List<List<Map<String,Object>>> dataList, List<FileConfig> config) throws Exception, Error;

    boolean compareValues(String value1, String value2, String comparisonOperator, FieldConfig fieldConf) throws IOException;
}
