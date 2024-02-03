package com.batch.datatransformer.datasource.utils;

import com.batch.datatransformer.consumer.model.FileConfig;
import com.batch.datatransformer.consumer.utils.JsonUtil;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
public class DataSourceUtil {

    public ArrayList<FileConfig> getConfigObjectArray(String jsonConfigFilePath) throws Exception {
        ArrayList<FileConfig> arr = new ArrayList<>();

        try(Stream<Path> stream = Files.list(Paths.get(jsonConfigFilePath))) {
            List<Path> paths = stream.toList();

            for (Path path : paths) {
                try {
                    FileConfig object = JsonUtil.jsonReader(path.toString(), FileConfig.class);
                    arr.add(object);
                } catch (Exception | Error e) {
                    if(e instanceof Exception)
                        throw new Exception(e);
                    else
                        throw new Error(e);
                }
            }
        }
        return arr;
    }

    public List<List<String>> getStreamObjectArray(String dataFilePath) throws Exception {
        List<List<String>> streamArr = new ArrayList<>();

        try(Stream<Path> stream = Files.list(Paths.get(dataFilePath))) {
            List<Path> paths = stream.toList();

            for (Path path : paths) {
                try {
                    streamArr.add(Files.readAllLines(path));
                } catch (Exception | Error e) {
                    if(e instanceof Exception)
                        throw new Exception(e);
                    else
                        throw new Error(e);
                }
            }
        }
        return streamArr;
    }
}
