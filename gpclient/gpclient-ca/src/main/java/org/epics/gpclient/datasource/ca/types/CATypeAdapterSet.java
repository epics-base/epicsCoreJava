/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.ca.types;

import org.epics.gpclient.datasource.DataSourceTypeAdapterSet;

import java.util.Collection;

/**
 *
 * @author carcassi
 */
public interface CATypeAdapterSet extends DataSourceTypeAdapterSet {

    Collection<CATypeAdapter> getAdapters();
}
