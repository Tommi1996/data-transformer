package com.batch.datatransformer.consumer.helpers.functionalities.basic;

import com.batch.datatransformer.consumer.interfaces.GrouperManagerInterface;
import com.batch.datatransformer.consumer.model.FileConfig;
import com.batch.datatransformer.consumer.model.KeyValue;
import com.batch.datatransformer.consumer.utils.ConsumerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.batch.datatransformer.consumer.utils.Constants.DISTINCT_TASK;


@Component
public class GrouperManagerHelper implements GrouperManagerInterface {
    static Logger logger = LoggerFactory.getLogger(GrouperManagerHelper.class);

    private final ConsumerUtil cu;

    public GrouperManagerHelper(ConsumerUtil cu) {
        this.cu = cu;
    }

    public List<List<Map<String, Object>>> distinctOnHashmap(List<List<Map<String, Object>>> dataList,
                                                             List<FileConfig> config) throws Exception, Error {
        boolean atLeastOneDistinct = false;
        for (int i = 0; i < config.size(); i++) {
            if (config.get(i).isApplyDistinct()) {
                atLeastOneDistinct = true;

                dataList.set(i, dataList.get(i).parallelStream().distinct().collect(Collectors.toList()));
            }
        }

        if (!atLeastOneDistinct)
            logger.info("No case to perform " + DISTINCT_TASK);

        return dataList;
    }

    public List<List<KeyValue>> distinctOnList(List<List<KeyValue>> resultSet) throws Exception, Error {
        AtomicInteger i = new AtomicInteger();
        List<List<String>> keyExtractors = resultSet.parallelStream()
                .map(x -> x.parallelStream()
                        .map(KeyValue::getValue)
                        .collect(Collectors.toList()))
                .toList();

        return resultSet.stream()
                .filter(distinctByKey(x -> keyExtractors.get(i.getAndIncrement())))
                .collect(Collectors.toList());
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public Map<List<List<String>>, List<Map<String, Object>>> groupByOnHashmap(
            List<Map<String, Object>> resultDataObject,
            List<List<String>> keyExtractors) throws Exception, Error {
        AtomicInteger i = new AtomicInteger();

        return resultDataObject.stream()
                .collect(Collectors.groupingByConcurrent(x -> Collections.singletonList(
                        keyExtractors.get(i.getAndIncrement()))));
    }

    public Map<List<List<String>>, List<List<KeyValue>>> groupByOnList(
            List<List<KeyValue>> resultSet,
            List<List<String>> keyExtractors) throws Exception, Error {
        AtomicInteger i = new AtomicInteger();
        return resultSet.stream()
                .collect(Collectors.groupingByConcurrent(x -> Collections.singletonList(
                        keyExtractors.get(i.getAndIncrement()))));
    }
}
