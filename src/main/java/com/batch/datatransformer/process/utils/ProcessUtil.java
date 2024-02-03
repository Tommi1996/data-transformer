package com.batch.datatransformer.process.utils;

import com.batch.datatransformer.consumer.interfaces.PipelineInterface;
import com.batch.datatransformer.datasource.interfaces.DataSourceManagerInterface;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ProcessUtil {
    public boolean dataIngestionAndConsumption(DataSourceManagerInterface dmi,
                                               PipelineInterface fi,
                                               Logger logger) {
        boolean batchOk = true;
        String message = "";
        try {
            logger.info("START PROCESSING AT " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            fi.dataConsumerPipeline(dmi.getDataSource());
        } catch (Exception | Error e) {
            batchOk = false;
            message = e.getMessage();
        } finally {
            if (batchOk) {
                logger.info("END PROCESSING AT " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            } else {
                logger.error("KO! " + message);
            }
        }
        return batchOk;
    }
}
