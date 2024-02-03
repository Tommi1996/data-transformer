package com.batch.datatransformer.consumer.helpers.pipelines;

import com.batch.datatransformer.consumer.helpers.functionalities.basic.*;
import com.batch.datatransformer.consumer.helpers.functionalities.custom.format.FormatMethods;
import com.batch.datatransformer.consumer.interfaces.PipelineInterface;
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

    private final LogicalFormatterManagerHelper logicalFormatterManagerHelper;

    private final FilterManagerHelper filterManagerHelper;

    private final GrouperManagerHelper grouperManagerHelper;

    private final PipelineUtil pipelineUtil;

    public BasePipelineHelper(CombinerManagerHelper combinerManagerHelper, MapperManagerHelper mapperManagerHelper, LogicalFormatterManagerHelper logicalFormatterManagerHelper, FilterManagerHelper filterManagerHelper, GrouperManagerHelper grouperManagerHelper, PipelineUtil pipelineUtil) {
        this.combinerManagerHelper = combinerManagerHelper;
        this.mapperManagerHelper = mapperManagerHelper;
        this.logicalFormatterManagerHelper = logicalFormatterManagerHelper;
        this.filterManagerHelper = filterManagerHelper;
        this.grouperManagerHelper = grouperManagerHelper;
        this.pipelineUtil = pipelineUtil;
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

            //Combine
            List<Map<String, Object>> resultDataObject = combinerManagerHelper
                    .joinData(inputDataMapped, dataSource.getConfig());

            //Logical formatting
            resultDataObject = logicalFormatterManagerHelper
                    .performLogicalFormatting(resultDataObject, dataSource.getConfig(), FormatMethods.class);

            String dt = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
            String fileName = "out-" + dt + ".csv";

            pipelineUtil.produceOutputCsv(resultDataObject, fileName, logger);

        } catch (Exception | Error e) {
            if (e instanceof Exception)
                throw new Exception(e);
            else
                throw new Error(e);
        }
    }
}
