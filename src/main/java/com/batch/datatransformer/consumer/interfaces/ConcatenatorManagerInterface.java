package com.batch.datatransformer.consumer.interfaces;

import com.batch.datatransformer.consumer.model.KeyValue;

import java.util.List;
import java.util.Map;

public interface ConcatenatorManagerInterface {
    <T>List<Map<String, Object>> unionAllOnHashmap(List<T>... lists);

    <T>List<Map<String, Object>> unionOnHashmap(List<T>... lists);

    <T>List<List<KeyValue>> unionAllOnList(List<T>... lists);

    <T>List<List<KeyValue>> unionOnList(List<T>... lists);
}