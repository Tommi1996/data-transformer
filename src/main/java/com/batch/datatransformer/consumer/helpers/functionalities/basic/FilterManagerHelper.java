package com.batch.datatransformer.consumer.helpers.functionalities.basic;

import com.batch.datatransformer.consumer.config.DataConfig;
import com.batch.datatransformer.consumer.interfaces.FilterManagerInterface;
import com.batch.datatransformer.consumer.model.*;
import com.batch.datatransformer.consumer.utils.ConsumerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.batch.datatransformer.consumer.utils.Constants.*;


@Component
public class FilterManagerHelper implements FilterManagerInterface {
    static Logger logger = LoggerFactory.getLogger(FilterManagerHelper.class);

    private final ConsumerUtil cu;

    private final DataConfig dataConfig;

    public FilterManagerHelper(ConsumerUtil cu, DataConfig dataConfig) {
        this.cu = cu;
        this.dataConfig = dataConfig;
    }

    public List<List<Map<String, Object>>> filterData(List<List<Map<String, Object>>> dataList,
                                                      List<FileConfig> config) throws Exception, Error {
        boolean atLeastOneFilter = false;
        for (int i = 0; i < config.size(); i++) {
            FileConfig currentFileConfig = config.get(i);
            List<FilterConditionConfig> filterConditionConfigs = currentFileConfig.getFilterConditionConfigs();
            List<FilterLinkConfig> filterLinkConfigs = currentFileConfig.getFilterLinkConfigs();
            List<FilterPredicateConfig> fpc = new ArrayList<>();
            if (filterConditionConfigs != null && !filterConditionConfigs.isEmpty()) {
                atLeastOneFilter = true;
                for (FilterConditionConfig fcc : filterConditionConfigs) {
                    Predicate<Map<String, Object>> p = getPredicate(fcc, config.get(i));
                    fpc.add(new FilterPredicateConfig(fcc.getConditionKey(), p));
                }

                Predicate<Map<String, Object>> predicate = null;
                if (fpc.size() == 1)
                    predicate = fpc.get(0).getPredicate();
                else {
                    if (filterLinkConfigs != null && !filterLinkConfigs.isEmpty())
                        predicate = recursivePredicateMerge(filterLinkConfigs, fpc);
                }

                if (predicate != null) {
                    dataList.set(i, dataList.get(i).parallelStream()
                            .filter(predicate)
                            .collect(Collectors.toList()));
                }
            }
        }

        if (!atLeastOneFilter)
            logger.info("No case to perform " + DATA_FILTER_TASK);

        return dataList;
    }

    private Predicate<Map<String, Object>> recursivePredicateMerge(List<FilterLinkConfig> filterLinkConfigs,
                                                                   List<FilterPredicateConfig> fpc) {
        for (int i = 0; i < filterLinkConfigs.size(); i++) {
            FilterLinkConfig x = filterLinkConfigs.get(i);
            List<FilterPredicateConfig> pfsc = getPredicatesToMerge(x, fpc);
            if (pfsc.size() > 1) {
                if (x.getLogicalOperator().trim().equalsIgnoreCase(logicalOperatorAnd))
                    fpc.add(new FilterPredicateConfig(x.getConditionKey(), pfsc.get(0).getPredicate().and(pfsc.get(1).getPredicate())));
                else if (x.getLogicalOperator().trim().equalsIgnoreCase(logicalOperatorOr))
                    fpc.add(new FilterPredicateConfig(x.getConditionKey(), pfsc.get(0).getPredicate().or(pfsc.get(1).getPredicate())));

                filterLinkConfigs.remove(x);

                if (!filterLinkConfigs.isEmpty())
                    return recursivePredicateMerge(filterLinkConfigs, fpc);

                break;
            }
        }
        return fpc.get(fpc.size() - 1).getPredicate();
    }

    private List<FilterPredicateConfig> getPredicatesToMerge(FilterLinkConfig filterLinkConfig, List<FilterPredicateConfig> fpc) {
        return fpc.stream()
                .filter(x -> x.getConditionKey().trim().equalsIgnoreCase(filterLinkConfig.getCurrentConditionKey().trim()) ||
                        x.getConditionKey().trim().equalsIgnoreCase(filterLinkConfig.getTargetConditionKey().trim()))
                .collect(Collectors.toList());
    }

    private Predicate<Map<String, Object>> getPredicate(FilterConditionConfig fc, FileConfig currentConfig){
        List<Condition> fcc = fc.getConditions();
        Predicate<Map<String, Object>> predicate = null;
        for (int j = 0; j < fcc.size(); j++) {
            int finalJ = j;

            FieldConfig fieldConf = currentConfig.getFields().stream()
                    .filter(x -> currentConfig.getFileName().toUpperCase().concat(".").concat(x.getName().toUpperCase()).equals(currentConfig.getFileName().toUpperCase() + "." + fcc.get(finalJ).getFieldKey().toUpperCase()))
                    .toList().get(0);

            Predicate<Map<String, Object>> p = x -> {
                try {
                    return compareValues(x.get(currentConfig.getFileName().toUpperCase() + "." +
                                    fcc.get(finalJ).getFieldKey().toUpperCase()).toString(),
                            fcc.get(finalJ).getFieldValue(),
                            fcc.get(finalJ).getComparisonOperator(),
                            fieldConf);
                } catch (IOException e) {
                    logger.error("Exception in getPredicate method: " + e);
                }
                return false;
            };

            if (j > 0) {
                if (fc.getLogicalOperator().trim().equalsIgnoreCase(logicalOperatorAnd))
                    predicate = predicate.and(p);
                else if (fc.getLogicalOperator().trim().equalsIgnoreCase(logicalOperatorOr))
                    predicate = predicate.or(p);
            } else
                predicate = p;
        }
        return predicate;
    }

    public boolean compareValues(String value1, String value2, String comparisonOperator, FieldConfig fieldConf) throws IOException {
        boolean compareResult = false;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        String tsFilePath = dataConfig.getJsonConfigPath() + "/typeSupported.json";
        String transcodedFieldType = cu.getTranscodedFieldType(fieldConf.getType().toUpperCase(), tsFilePath, logger);

        try {
            switch (comparisonOperator) {
                case equal:
                    if (transcodedFieldType.equals(numeric) || transcodedFieldType.equals(alphanumeric))
                        compareResult = value1.equals(value2);
                    else if (transcodedFieldType.equals(date))
                        compareResult = df.parse(value1).equals(df.parse(value2));
                    break;
                case notEqual:
                    if (transcodedFieldType.equals(numeric) || transcodedFieldType.equals(alphanumeric))
                        compareResult = !value1.equals(value2);
                    else if (transcodedFieldType.equals(date))
                        compareResult = !df.parse(value1).equals(df.parse(value2));
                    break;
                case greather:
                    if (transcodedFieldType.equals(numeric))
                        compareResult = Double.parseDouble(value1) > Double.parseDouble(value2);
                    else if (transcodedFieldType.equals(date))
                        compareResult = df.parse(value1).after(df.parse(value2));
                    else
                        logger.warn("Warning in compareValues: Non è possibile confrontare due stringhe tramite l'operatore " + greather);
                    break;
                case less:
                    if (transcodedFieldType.equals(numeric))
                        compareResult = Double.parseDouble(value1) < Double.parseDouble(value2);
                    else if (transcodedFieldType.equals(date))
                        compareResult = df.parse(value1).before(df.parse(value2));
                    else
                        logger.warn("Warning in compareValues: Non è possibile confrontare due stringhe tramite l'operatore " + less);
                    break;
                case greatherOrEqual:
                    if (transcodedFieldType.equals(numeric))
                        compareResult = Double.parseDouble(value1) >= Double.parseDouble(value2);
                    else if (transcodedFieldType.equals(date)) {
                        Date d1 = df.parse(value1);
                        Date d2 = df.parse(value2);
                        compareResult = d1.after(d2) || d1.equals(d2);
                    } else
                        logger.warn("Warning in compareValues: Non è possibile confrontare due stringhe tramite l'operatore " + greatherOrEqual);
                    break;
                case lessOrEqual:
                    if (transcodedFieldType.equals(numeric))
                        compareResult = Double.parseDouble(value1) <= Double.parseDouble(value2);
                    else if (transcodedFieldType.equals(date)) {
                        Date d1 = df.parse(value1);
                        Date d2 = df.parse(value2);
                        compareResult = d1.before(d2) || d1.equals(d2);
                    } else
                        logger.warn("Warning in compareValues: Non è possibile confrontare due stringhe tramite l'operatore " + lessOrEqual);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.error("Error in compareValues: " + e);
        }
        return compareResult;
    }
}
