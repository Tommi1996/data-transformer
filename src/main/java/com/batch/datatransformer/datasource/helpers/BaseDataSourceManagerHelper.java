package com.batch.datatransformer.datasource.helpers;

import com.batch.datatransformer.consumer.config.DataConfig;
import com.batch.datatransformer.consumer.model.FileConfig;
import com.batch.datatransformer.datasource.interfaces.DataSourceManagerInterface;
import com.batch.datatransformer.datasource.model.DataSource;
import com.batch.datatransformer.datasource.utils.DataSourceUtil;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.batch.datatransformer.DataTransformerApplication.getContextFilePath;

@Component
public class BaseDataSourceManagerHelper implements DataSourceManagerInterface {

    private final DataSourceUtil dsu;

    private final DataConfig dataConfig;

    public BaseDataSourceManagerHelper(DataSourceUtil dsu, DataConfig dataConfig) {
        this.dsu = dsu;
        this.dataConfig = dataConfig;
    }

    @Override
    public DataSource getDataSource() throws Exception {
        final String configPath = dataConfig.getJsonConfigPath();

        String datasetName = dataConfig.getDatasetName();

        String structurePath = getContextFilePath(configPath + "/" + datasetName + "/structure");
        String dataPath = getContextFilePath(configPath + "/" + datasetName + "/data");

        List<FileConfig> config = dsu.getConfigObjectArray(structurePath);
        List<List<String>> data = dsu.getStreamObjectArray(dataPath);

        return new DataSource(data, config);
    }
}
