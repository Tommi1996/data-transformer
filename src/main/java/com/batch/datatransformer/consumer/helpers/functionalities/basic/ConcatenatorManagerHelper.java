package com.batch.datatransformer.consumer.helpers.functionalities.basic;

import com.batch.datatransformer.consumer.interfaces.ConcatenatorManagerInterface;
import com.batch.datatransformer.consumer.model.KeyValue;
import com.batch.datatransformer.consumer.utils.ConsumerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
public class ConcatenatorManagerHelper implements ConcatenatorManagerInterface {
    static Logger logger = LoggerFactory.getLogger(ConcatenatorManagerHelper.class);

    private final ConsumerUtil cu;

    public ConcatenatorManagerHelper(ConsumerUtil cu) {
        this.cu = cu;
    }

    /**
     * @input List<List < Map < String, Object>>>
     */
    public <T> List<Map<String, Object>> unionAllOnHashmap(List<T>... lists) {
        return Stream.of(lists)
                .flatMap(Collection::stream)
                .map(x -> (List<Map<String, Object>>) x)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * @input List<List < Map < String, Object>>>
     */
    public <T> List<Map<String, Object>> unionOnHashmap(List<T>... lists) {
        return Stream.of(lists)
                .flatMap(Collection::stream)
                .map(x -> (List<Map<String, Object>>) x)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * @input List<List < List < KeyValue>>>
     */
    public <T> List<List<KeyValue>> unionAllOnList(List<T>... lists) {
        return Stream.of(lists)
                .flatMap(Collection::stream)
                .map(x -> (List<List<KeyValue>>) x)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * @input List<List < List < KeyValue>>>
     */
    public <T> List<List<KeyValue>> unionOnList(List<T>... lists) {
        AtomicInteger i = new AtomicInteger();

        List<List<KeyValue>> concatenation = Stream.of(lists)
                .flatMap(Collection::stream)
                .map(x -> (List<List<KeyValue>>) x)
                .flatMap(Collection::stream)
                .toList();

        List<List<String>> keyExtractors = concatenation.parallelStream()
                .map(x -> x.parallelStream()
                        .map(KeyValue::getValue)
                        .collect(Collectors.toList()))
                .toList();

        return concatenation.stream()
                .filter(distinctByKey(x -> keyExtractors.get(i.getAndIncrement())))
                .collect(Collectors.toList());
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
