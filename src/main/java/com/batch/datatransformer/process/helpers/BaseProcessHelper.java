package com.batch.datatransformer.process.helpers;

import com.batch.datatransformer.consumer.helpers.pipelines.BasePipelineHelper;
import com.batch.datatransformer.datasource.helpers.BaseDataSourceManagerHelper;
import com.batch.datatransformer.process.interfaces.ProcessInterface;
import com.batch.datatransformer.process.utils.ProcessUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BaseProcessHelper implements ProcessInterface {

    static Logger logger = LoggerFactory.getLogger(BaseProcessHelper.class);

    private final ProcessUtil processUtil;

    private final BaseDataSourceManagerHelper dataSourceManagerHelper;

    private final BasePipelineHelper pipelineHelper;

    public BaseProcessHelper(ProcessUtil processUtil, BaseDataSourceManagerHelper dataSourceManagerHelper, BasePipelineHelper pipelineHelper) {
        this.processUtil = processUtil;
        this.dataSourceManagerHelper = dataSourceManagerHelper;
        this.pipelineHelper = pipelineHelper;
    }

    @Override
    public boolean executeProcess() {
        return processUtil.dataIngestionAndConsumption(dataSourceManagerHelper, pipelineHelper, logger);
    }
}
