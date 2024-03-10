package com.batch.datatransformer.consumer.helpers.pipelines;

import com.batch.datatransformer.consumer.config.DataConfig;
import com.batch.datatransformer.consumer.helpers.functionalities.basic.*;
import com.batch.datatransformer.consumer.interfaces.PipelineInterface;
import com.batch.datatransformer.consumer.model.FileConfig;
import com.batch.datatransformer.consumer.utils.PipelineUtil;
import com.batch.datatransformer.datasource.model.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class BasePipelineHelper implements PipelineInterface {

    static Logger logger = LoggerFactory.getLogger(BasePipelineHelper.class);

    private final CombinerManagerHelper combinerManagerHelper;

    private final MapperManagerHelper mapperManagerHelper;

    private final FilterManagerHelper filterManagerHelper;

    private final GrouperManagerHelper grouperManagerHelper;

    private final PipelineUtil pipelineUtil;

    private final DataConfig dataConfig;

    public BasePipelineHelper(CombinerManagerHelper combinerManagerHelper, MapperManagerHelper mapperManagerHelper,
                              FilterManagerHelper filterManagerHelper, GrouperManagerHelper grouperManagerHelper,
                              PipelineUtil pipelineUtil, DataConfig dataConfig) {
        this.combinerManagerHelper = combinerManagerHelper;
        this.mapperManagerHelper = mapperManagerHelper;
        this.filterManagerHelper = filterManagerHelper;
        this.grouperManagerHelper = grouperManagerHelper;
        this.pipelineUtil = pipelineUtil;
        this.dataConfig = dataConfig;
    }

    @Override
    public void dataConsumerPipeline(DataSource dataSource) throws Exception, Error {
        try {
            //Map
            List<List<Map<String, Object>>> inputDataMapped = mapperManagerHelper
                    .mapFileStreamDataToObject(dataSource.getData(), dataSource.getConfig());

            //Filter
            inputDataMapped = filterManagerHelper
                    .filterData(inputDataMapped, dataSource.getConfig());

            //Group
            inputDataMapped = grouperManagerHelper
                    .distinctOnHashmap(inputDataMapped, dataSource.getConfig());

            List<FileConfig> configs = dataSource.getConfig();

            boolean join = configs.size() > 1 && pipelineUtil.allLinksNotEmpty(configs) && !dataConfig.getForceNotJoin();

            List<Map<String, Object>> resultDataObject;

            if(join) {
                resultDataObject = combinerManagerHelper
                        .joinData(inputDataMapped, dataSource.getConfig());

                String dt = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
                String fileName = dataConfig.getDatasetName() + "-out-" + dt + ".csv";

                pipelineUtil.produceOutputCsv(resultDataObject, fileName, logger);
            } else {
                for (int i = 0; i < inputDataMapped.size(); i++) {
                    FileConfig config = dataSource.getConfig().get(i);
                    List<Map<String, Object>> item = inputDataMapped.get(i);

                    String dt = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
                    String fileName = dataConfig.getDatasetName() + "-" + config.getFileName() + "-out-" + dt + ".csv";

                    pipelineUtil.produceOutputCsv(item, fileName, logger);
                }
            }
        } catch (Exception | Error e) {
            if (e instanceof Exception)
                throw new Exception(e);
            else
                throw new Error(e);
        }
    }
}
