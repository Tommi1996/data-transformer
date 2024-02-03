package com.batch.datatransformer.consumer.helpers.functionalities.basic;

import com.batch.datatransformer.consumer.config.DataConfig;
import com.batch.datatransformer.consumer.interfaces.MapperManagerInterface;
import com.batch.datatransformer.consumer.model.FieldConfig;
import com.batch.datatransformer.consumer.model.FileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class MapperManagerHelper implements MapperManagerInterface {
    static Logger logger = LoggerFactory.getLogger(MapperManagerHelper.class);

    private final DataConfig dataConfig;

    public MapperManagerHelper(DataConfig dataConfig) {
        this.dataConfig = dataConfig;
    }

    public List<List<Map<String, Object>>> mapFileStreamDataToObject(List<List<String>> fileStreamArray,
                                                                     List<FileConfig> config) throws Exception, Error {

        List<List<Map<String, Object>>> dataList = IntStream.range(0, fileStreamArray.size())
                .mapToObj(x -> fileStreamArray.get(x).stream().skip(1)
                        .map(y -> Arrays.stream(y.split(dataConfig.getCsvDelimiter(), -1)))
                        .map(z -> {
                            FileConfig fc = config.get(x);
                            Map<String, Object> m = new HashMap<>();
                            AtomicInteger j = new AtomicInteger();
                            z.forEach(c -> {
                                FieldConfig fieldConfig = fc.getFields().get(j.getAndIncrement());

                                m.put(fc.getFileName().toUpperCase() + "." + fieldConfig.getName().toUpperCase(), c);
                            });
                            return m;
                        })
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        config.forEach(x -> x.setFields(x.getFields()));

        return dataList;
    }
}
