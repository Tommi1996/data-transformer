package com.batch.datatransformer.consumer.helpers.functionalities.basic;

import com.batch.datatransformer.consumer.interfaces.LogicalFormatterManagerInterface;
import com.batch.datatransformer.consumer.model.FieldConfig;
import com.batch.datatransformer.consumer.model.FileConfig;
import com.batch.datatransformer.consumer.utils.ConsumerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.batch.datatransformer.consumer.utils.Constants.DATA_LOGICAL_FORMATTING_TASK;

@Component
public class LogicalFormatterManagerHelper implements LogicalFormatterManagerInterface {
    static Logger logger = LoggerFactory.getLogger(LogicalFormatterManagerHelper.class);

    private final ConsumerUtil cu;

    public LogicalFormatterManagerHelper(ConsumerUtil cu) {
        this.cu = cu;
    }

    public <T> List<Map<String, Object>> performLogicalFormatting(List<Map<String, Object>> resultDataObject,
                                                              List<FileConfig> config,
                                                              Class<T> methodHelperClass) throws Exception, Error {
        boolean atLeastOneLogicalFormatFunc = false;

        List<FieldConfig> lfc = cu.getColumnTracker(config).stream()
                .flatMap(List::stream)
                .filter(x -> !x.getLogicalFormatterFunc().isEmpty())
                .toList();

        if (!lfc.isEmpty()) {

            atLeastOneLogicalFormatFunc = true;

            //Il numero delle righe viene moltiplicato per il numero delle funzioni da eseguire perchè per ogni funzione
            //devo scorrere tutta la lista

            for (FieldConfig fc : lfc) {
                List<Map<String, Object>> finalResultDataObject = resultDataObject;
                AtomicInteger rowIndex = new AtomicInteger();
                resultDataObject = resultDataObject.stream()
                        .map(y -> invokeMethodByName(fc, y, methodHelperClass, finalResultDataObject, rowIndex.getAndIncrement(), logger))
                        .collect(Collectors.toList());
            }
        }

        if (!atLeastOneLogicalFormatFunc)
            logger.info("No case to perform " + DATA_LOGICAL_FORMATTING_TASK);

        cu.logEndTask(DATA_LOGICAL_FORMATTING_TASK, logger);
        return resultDataObject;
    }

    public <T> Map<String, Object> invokeMethodByName(FieldConfig fieldConfig, Map<String, Object> row, Class<T> methodHelperClass, List<Map<String, Object>> resultDataObject, Integer rowIndex, Logger logger) {
        Object result;
        Map<String, Object> mapper = null;
        try {
            Constructor<?> constructor = methodHelperClass.getConstructor();
            Object instance = constructor.newInstance();
            Method method = instance.getClass().getMethod(fieldConfig.getLogicalFormatterFunc(), Object.class, FieldConfig.class, List.class, Integer.class);
            result = method.invoke(instance, row, fieldConfig, resultDataObject, rowIndex);
            mapper = ConsumerUtil.genericObjectToHashmap(result);
        } catch (Exception e) {
            logger.error("Error on method invocation " + fieldConfig.getLogicalFormatterFunc() + ": " + e);
        }
        return mapper;
    }
}
