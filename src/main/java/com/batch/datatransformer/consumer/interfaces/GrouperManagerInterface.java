package com.batch.datatransformer.consumer.interfaces;

import com.batch.datatransformer.consumer.model.FileConfig;
import com.batch.datatransformer.consumer.model.KeyValue;

import java.util.List;
import java.util.Map;

public interface GrouperManagerInterface {
    List<List<Map<String, Object>>> distinctOnHashmap(List<List<Map<String,Object>>> dataList, List<FileConfig> config) throws Exception, Error;

    List<List<KeyValue>> distinctOnList(List<List<KeyValue>> resultSet) throws Exception, Error;

    Map<List<List<String>>, List<Map<String, Object>>> groupByOnHashmap(List<Map<String, Object>> resultDataObject, List<List<String>> keyExtractors) throws Exception, Error;

    Map<List<List<String>>, List<List<KeyValue>>> groupByOnList(List<List<KeyValue>> resultSet, List<List<String>> keyExtractors) throws Exception, Error;
}
