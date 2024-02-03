package com.batch.datatransformer.consumer.interfaces;

import com.batch.datatransformer.datasource.model.DataSource;

public interface PipelineInterface {
    void dataConsumerPipeline(DataSource dataSource) throws Exception, Error;
}
