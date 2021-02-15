/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.loc;

import org.epics.gpclient.datasource.DataSource;
import org.epics.gpclient.datasource.DataSourceProvider;


/**
 * DataSourceProvider for local variables.
 *
 * @author carcassi
 */
public class LocalDataSourceProvider extends DataSourceProvider {

    @Override
    public String getName() {
        return "loc";
    }

    @Override
    public DataSource createInstance() {
        return new LocalDataSource();
    }

}
