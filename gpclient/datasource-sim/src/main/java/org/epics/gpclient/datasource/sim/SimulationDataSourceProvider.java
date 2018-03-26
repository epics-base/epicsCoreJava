/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource.sim;

import org.epics.gpclient.datasource.DataSource;
import org.epics.gpclient.datasource.DataSourceProvider;
import org.epics.gpclient.datasource.sim.SimulationDataSource;


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
