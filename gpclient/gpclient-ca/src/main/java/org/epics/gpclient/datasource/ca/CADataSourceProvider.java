package org.epics.gpclient.datasource.ca;

import org.epics.gpclient.datasource.DataSource;
import org.epics.gpclient.datasource.DataSourceProvider;

public class CADataSourceProvider extends DataSourceProvider {

    @Override
    public String getName() {
        return "ca";
    }

    @Override
    public DataSource createInstance() {
        return new CADataSource();
    }

}
