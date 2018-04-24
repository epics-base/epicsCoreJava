/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient;

/**
 * A PV that can be both read and written. In general, the read payload will be
 * different from the write payload.
 *
 * @param <R> type of the read payload
 * @param <W> type of the write payload
 * @author carcassi
 */
public interface PV<R, W> extends PVReader<R>, PVWriter<W> {
    
}
