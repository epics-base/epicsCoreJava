/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.gpclient.datasource.DataSource;
import org.epics.gpclient.datasource.DataSourceProvider;


/**
 * DataSourceProvider for simulated data.
 *
 * @author carcassi
 */
public class SimulationDataSourceProvider extends DataSourceProvider {

    @Override
    public String getName() {
        return "sim";
    }

    @Override
    public DataSource createInstance() {
        return new SimulationDataSource();
    }

}
