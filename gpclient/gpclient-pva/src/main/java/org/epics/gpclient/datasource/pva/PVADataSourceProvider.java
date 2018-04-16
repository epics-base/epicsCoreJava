/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
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
