package com.batch.datatransformer.datasource.interfaces;


import com.batch.datatransformer.datasource.model.DataSource;

public interface DataSourceManagerInterface {
    DataSource getDataSource() throws Exception;
}
