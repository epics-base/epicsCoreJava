/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.pva;

import org.epics.gpclient.datasource.DataSource;
import org.epics.gpclient.datasource.DataSourceProvider;


/**
 * DataSourceProvider for pv access.
 *
 * @author carcassi
 */
public class PVADataSourceProvider extends DataSourceProvider {

    @Override
    public String getName() {
        return "pva";
    }

    @Override
    public DataSource createInstance() {
        return new PVADataSource();
    }

}
