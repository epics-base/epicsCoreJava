/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.ca.types;

import java.util.Collection;

import org.epics.gpclient.datasource.DataSourceTypeAdapterSet;

/**
 *
 * @author carcassi
 */
public interface CATypeAdapterSet extends DataSourceTypeAdapterSet {
    
    @Override
    Collection<CATypeAdapter> getAdapters();
}
