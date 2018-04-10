/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
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
