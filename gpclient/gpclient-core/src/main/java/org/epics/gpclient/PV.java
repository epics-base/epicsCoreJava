/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

/**
 * A PV that can be both read and written. In general, the read payload will be
 * different from the write payload. See {@link PVReader} and {@link PVWriter} for
 * more information.
 *
 * @param <R> type of the read payload
 * @param <W> type of the write payload
 * @author carcassi
 */
public interface PV<R, W> extends PVReader<R>, PVWriter<W> {

}
