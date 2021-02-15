/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array.mockapi;

import org.epics.util.array.ListNumber;

/**
 *
 * @author carcassi
 */
public interface NumericArrayField {
    public ListNumber get();
    public void put(int offset, ListNumber data);
}
