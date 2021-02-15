/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.pva;

import org.epics.gpclient.datasource.DataSourceTypeAdapterSet;

import java.util.Collection;

/**
 *
 * @author carcassi
 */
interface PVATypeAdapterSet extends DataSourceTypeAdapterSet {

    Collection<PVATypeAdapter> getAdapters();
}
