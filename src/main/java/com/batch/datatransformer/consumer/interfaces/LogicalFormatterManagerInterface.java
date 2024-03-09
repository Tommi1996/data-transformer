package com.batch.datatransformer.consumer.interfaces;


import com.batch.datatransformer.consumer.model.FieldConfig;

import java.util.List;
import java.util.Map;

public interface LogicalFormatterManagerInterface {
    <T> List<Map<String, Object>> performLogicalFormatting(List<Map<String, Object>> resultDataObject,
                                                       List<FieldConfig> config,
                                                       Class<T> methodHelperClass) throws Exception, Error;
}
