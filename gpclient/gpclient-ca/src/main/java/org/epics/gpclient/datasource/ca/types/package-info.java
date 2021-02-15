/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
/**
 * Support for Epics 3 data source
 * (<a href="doc-files/jca-datasource.html">channel syntax</a>).
 * <p>
 * The {@link org.epics.gpclient.datasource.ca.CADataSource} uses the
 * {@link org.epics.gpclient.datasource.MultiplexedChannelHandler}. The
 * connection payload used is the JCA Channel class directly. The payload for
 * each monitor event is the
 * {@link org.epics.gpclient.datasource.ca.CAMessagePayload}, which includes
 * both metadata (taken with a GET at connection time) and value (taken from the
 * MONITOR event).
 * <p>
 * The conversion between JCAMessagePayload and the actual type, is done through
 * the {@code CATypeAdapter}. A CATypeSupport can be
 * passed directly to the JCADataSource so that one can configure support for
 * different types.
 */
package org.epics.gpclient.datasource.ca.types;
