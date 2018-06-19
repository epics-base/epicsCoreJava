/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
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
