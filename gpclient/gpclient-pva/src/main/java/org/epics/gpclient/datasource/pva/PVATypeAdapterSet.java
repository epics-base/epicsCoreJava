/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource.pva;

import java.util.Collection;
import org.epics.gpclient.datasource.DataSourceTypeAdapterSet;

/**
 *
 * @author carcassi
 */
interface PVATypeAdapterSet extends DataSourceTypeAdapterSet {
    
    @Override
    Collection<PVATypeAdapter> getAdapters();
}
