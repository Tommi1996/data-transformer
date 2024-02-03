package com.batch.datatransformer.consumer.helpers.functionalities.basic;

import com.batch.datatransformer.consumer.interfaces.CombinerManagerInterface;
import com.batch.datatransformer.consumer.model.*;
import com.batch.datatransformer.consumer.utils.Constants;
import com.batch.datatransformer.consumer.utils.ConsumerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Component
public class CombinerManagerHelper implements CombinerManagerInterface {
    static Logger logger = LoggerFactory.getLogger(CombinerManagerHelper.class);

    private final ConsumerUtil cu;

    public CombinerManagerHelper(ConsumerUtil cu) {
        this.cu = cu;
    }

    public List<Map<String, Object>> joinData(List<List<Map<String, Object>>> inputDataMapped,
                                              List<FileConfig> config) throws Exception, Error {
        List<LinkConfig> joinTracker = new ArrayList<>();

        Integer linkCount = config.stream()
                .map(x -> x.getLinks().stream()
                        .map(y -> 1)
                        .reduce(0, Integer::sum))
                .reduce(0, Integer::sum);

        List<List<FieldConfig>> columnTracker = cu.getColumnTracker(config);

        return recursiveJoin(inputDataMapped, config, linkCount, columnTracker, joinTracker);
    }

    public List<Map<String, Object>> recursiveJoin(List<List<Map<String, Object>>> dataList,
                                                   List<FileConfig> config, Integer linkCount,
                                                   List<List<FieldConfig>> columnTracker,
                                                   List<LinkConfig> joinTracker) throws Exception, Error {
        List<Map<String, Object>> result = null;
        DataObject sortedObjects = sort(dataList, config, columnTracker);
        config = sortedObjects.getConfig();
        dataList = sortedObjects.getDataList();
        columnTracker = sortedObjects.getColumnTracker();

        for (int i = 0; i < config.size(); i++) {
            if (joinTracker.size() == linkCount) break;

            FileConfig c = config.get(i);
            String fileName = c.getFileName().toUpperCase();
            for (int currentKeyPos = 0; currentKeyPos < c.getLinks().size(); currentKeyPos++) {
                if (joinTracker.size() == linkCount) break;

                LinkConfig l = c.getLinks().get(currentKeyPos);
                String fileNameTarget = l.getFileNameTarget().toUpperCase();

                //Leggo il tipo di join target (se non esiste imposto di default INNER)
                JoinEnum joinTypeTarget = getJoinEnum(l.getJoinType());

                //Devo fare la join solo se non è stata già fatta tra i due file al contrario
                boolean linkExist = joinTracker.stream()
                        .anyMatch(x -> x.getFileNameTarget().toUpperCase().equals(fileName) &&
                                x.getFileName().toUpperCase().equals(fileNameTarget));
                if (!linkExist) {
                    int currentArrPos = i;
                    List<FileConfig> finalConfig = config;
                    //Posizione dell'oggetto target
                    int targetArrPos = IntStream.range(0, config.size())
                            .filter(x -> finalConfig.get(x).getFileName().toUpperCase().equals(fileNameTarget))
                            .findFirst().orElse(-1);

                    int targetKeyPos = IntStream.range(0, config.get(targetArrPos).getLinks().size())
                            .filter(x -> finalConfig.get(currentArrPos).getFileName()
                                    .equalsIgnoreCase(finalConfig.get(targetArrPos).getLinks().get(x).getFileNameTarget()))
                            .findFirst().orElse(-1);

                    dataList.set(
                            targetArrPos,
                            executeJoin(dataList, config, currentArrPos, currentKeyPos, targetArrPos, targetKeyPos, joinTypeTarget, columnTracker)
                    );

                    columnTracker.get(targetArrPos).addAll(columnTracker.get(currentArrPos));

                    //Aggiungo il legame appena fatto al joinTracker
                    joinTracker.add(config.get(i).getLinks().get(currentKeyPos));
                    joinTracker.add(config.get(targetArrPos).getLinks().get(targetKeyPos));

                    //Elimino il legame appena fatto da config (per sort successivo)
                    config.get(targetArrPos).getLinks().remove(config.get(targetArrPos).getLinks().get(targetKeyPos));

                    if (joinTracker.size() < linkCount)
                        return recursiveJoin(dataList, config, linkCount, columnTracker, joinTracker);

                    result = dataList.get(targetArrPos);
                }
            }
        }
        return result;
    }

    private List<Map<String, Object>> executeJoin(List<List<Map<String, Object>>> finalDataList,
                                                  List<FileConfig> finalConfig,
                                                  Integer currentArrPos,
                                                  Integer currentKeyPos,
                                                  Integer targetArrPos,
                                                  Integer targetKeyPos,
                                                  JoinEnum joinType,
                                                  List<List<FieldConfig>> finalColumnTracker) throws Exception, Error {
        List<Map<String, Object>> result = null;

        String currentEntity = finalConfig.get(currentArrPos).getFileName().toUpperCase();
        String targetEntity = finalConfig.get(targetArrPos).getFileName().toUpperCase();

        JoinKeysConfig currentPosJoinKeys = finalConfig.get(currentArrPos).getLinks().get(currentKeyPos).getJoinKeys();
        JoinKeysConfig targetPosJoinKeys = finalConfig.get(targetArrPos).getLinks().get(targetKeyPos).getJoinKeys();
        List<FieldConfig> currentPosFields = finalColumnTracker.get(currentArrPos);
        List<FieldConfig> targetPosFields = finalColumnTracker.get(targetArrPos);

        int currentPosArrLength = finalDataList.get(currentArrPos).size();
        int targetPosArrLength = finalDataList.get(targetArrPos).size();

        cu.logStartAtomicJoinTask(joinType.toString(), currentEntity, targetEntity, currentPosArrLength, targetPosArrLength, logger);

        switch (joinType) {
            case INNER:
                result = finalDataList.get(currentArrPos)
                        .parallelStream()
                        .flatMap(x -> finalDataList.get(targetArrPos)
                                .parallelStream()
                                .filter(joinBy(x, currentPosJoinKeys, targetPosJoinKeys, currentEntity, targetEntity))
                                .map(y -> {
                                    HashMap<String, Object> h = new HashMap<>(x);
                                    h.putAll(y);
                                    return h;
                                })
                        ).collect(toList());
                break;
            case FULL:
                Stream<Map<String, Object>> stream1 = finalDataList.get(currentArrPos)
                        .parallelStream()
                        .filter(x -> finalDataList.get(targetArrPos)
                                .parallelStream()
                                .noneMatch(joinBy(x, currentPosJoinKeys, targetPosJoinKeys, currentEntity, targetEntity)))
                        .peek(x -> targetPosFields.forEach(c -> x.putIfAbsent(c.getName().toUpperCase(), "")));

                Stream<Map<String, Object>> baseJoin = finalDataList.get(currentArrPos)
                        .parallelStream()
                        .flatMap(x -> finalDataList.get(targetArrPos)
                                .parallelStream()
                                .filter(joinBy(x, currentPosJoinKeys, targetPosJoinKeys, currentEntity, targetEntity))
                                .map(y -> {
                                    HashMap<String, Object> h = new HashMap<>(x);
                                    h.putAll(y);
                                    return h;
                                }));

                Stream<Map<String, Object>> stream2 = finalDataList.get(targetArrPos).parallelStream()
                        .filter(x -> finalDataList.get(currentArrPos)
                                .parallelStream()
                                .noneMatch(joinBy(x, targetPosJoinKeys, currentPosJoinKeys, targetEntity, currentEntity)))
                        .peek(x -> currentPosFields.forEach(c -> x.putIfAbsent(c.getName().toUpperCase(), "")));

                result = Stream.of(stream1, baseJoin, stream2)
                        .reduce(Stream::concat).get()
                        .collect(toList());
                break;
            case LEFT:
                result = finalDataList.get(currentArrPos).parallelStream()
                        .flatMap(x -> defaultIfEmpty(finalDataList.get(targetArrPos)
                                        .parallelStream()
                                        .filter(joinBy(x, currentPosJoinKeys, targetPosJoinKeys, currentEntity, targetEntity)),
                                () -> {
                                    targetPosFields.forEach(c -> x.putIfAbsent(c.getName().toUpperCase(), ""));
                                    return x;
                                })
                                .map(y -> {
                                    HashMap<String, Object> h = new HashMap<>(x);
                                    h.putAll(y);
                                    return h;
                                })
                        ).collect(toList());
                break;
            case RIGHT:
                result = finalDataList.get(targetArrPos).parallelStream()
                        .flatMap(x -> defaultIfEmpty(finalDataList.get(currentArrPos)
                                        .parallelStream()
                                        .filter(joinBy(x, targetPosJoinKeys, currentPosJoinKeys, targetEntity, currentEntity)),
                                () -> {
                                    currentPosFields.forEach(c -> x.putIfAbsent(c.getName().toUpperCase(), ""));
                                    return x;
                                })
                                .map(y -> {
                                    HashMap<String, Object> h = new HashMap<>(x);
                                    h.putAll(y);
                                    return h;
                                })
                        ).collect(toList());
                break;
        }
        return result;
    }

    public static Predicate<Map<String, Object>> joinBy(Map<String, Object> x,
                                                        JoinKeysConfig firstPosJoinKey,
                                                        JoinKeysConfig secondPosJoinKey,
                                                        String firstEntity,
                                                        String secondEntity) {
        Predicate<Map<String, Object>> predicate = null;
        ArrayList<String> firstJoinKeyFields = firstPosJoinKey.getJoinKeyFields();
        ArrayList<String> secondJoinKeyFields = secondPosJoinKey.getJoinKeyFields();

        for (int i = 0; i < firstJoinKeyFields.size(); i++) {
            String firstJoinKey = firstEntity + "." + firstJoinKeyFields.get(i).toUpperCase();
            String secondJoinKey = secondEntity + "." + secondJoinKeyFields.get(i).toUpperCase();
            Predicate<Map<String, Object>> p = y -> x.get(firstJoinKey).equals(y.get(secondJoinKey));

            if (i > 0) {
                if (firstPosJoinKey.getLogicalOperator().trim().equalsIgnoreCase(Constants.logicalOperatorAnd))
                    predicate = predicate.and(p);
                else if (firstPosJoinKey.getLogicalOperator().trim().equalsIgnoreCase(Constants.logicalOperatorOr))
                    predicate = predicate.or(p);
                else
                    logger.warn("Logical operator between " + firstJoinKey + " and " + secondJoinKey + " is not defined.");
            } else
                predicate = p;
        }
        return predicate;
    }

    static <T> Stream<T> defaultIfEmpty(Stream<T> stream, Supplier<T> supplier) {
        Iterator<T> iterator = stream.iterator();
        if (iterator.hasNext()) {
            return StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(
                            iterator, 0
                    ), false);
        }
        return Stream.of(supplier.get());
    }

    public static DataObject sort(List<List<Map<String, Object>>> dataList,
                                  List<FileConfig> config,
                                  List<List<FieldConfig>> columnTracker) {
        Map<Integer, List<FileConfig>> configGroup = new TreeMap<>();
        Map<Integer, List<List<Map<String, Object>>>> dataListGroup = new TreeMap<>();
        Map<Integer, List<List<FieldConfig>>> columnTrackerGroup = new TreeMap<>();
        List<FileConfig> finalConfig = new ArrayList<>();
        List<List<Map<String, Object>>> finalDataList = new ArrayList<>();
        List<List<FieldConfig>> finalColumnTracker = new ArrayList<>();

        for (int i = 0; i < config.size(); i++) {
            List<FileConfig> configValues = configGroup.get(config.get(i).getLinks().size());
            List<List<Map<String, Object>>> dataValues = dataListGroup.get(config.get(i).getLinks().size());
            List<List<FieldConfig>> columnTrackerValues = columnTrackerGroup.get(config.get(i).getLinks().size());
            if (configValues == null) {
                configValues = new ArrayList<>();
                dataValues = new ArrayList<>();
                columnTrackerValues = new ArrayList<>();
            }

            configValues.add(config.get(i));
            dataValues.add(dataList.get(i));
            columnTrackerValues.add(columnTracker.get(i));
            configGroup.putIfAbsent(config.get(i).getLinks().size(), configValues);
            dataListGroup.putIfAbsent(config.get(i).getLinks().size(), dataValues);
            columnTrackerGroup.putIfAbsent(config.get(i).getLinks().size(), columnTrackerValues);
        }

        configGroup.forEach((k, v) -> finalConfig.addAll(v));
        dataListGroup.forEach((k, v) -> finalDataList.addAll(v));
        columnTrackerGroup.forEach((k, v) -> finalColumnTracker.addAll(v));

        return new DataObject(finalDataList, finalConfig, finalColumnTracker);
    }

    private JoinEnum getJoinEnum(String joinType) {
        JoinEnum joinEnumType;
        String tmp = joinType != null ? joinType.toUpperCase() : null;
        if ((joinType != null && !joinType.isEmpty()) &&
                (tmp.equals(Constants.innerJoin) || tmp.equals(Constants.fullJoin) || tmp.equals(Constants.leftJoin) || tmp.equals(Constants.rightJoin)))
            joinEnumType = JoinEnum.valueOf(joinType.toUpperCase());
        else
            joinEnumType = JoinEnum.INNER;

        return joinEnumType;
    }
}
