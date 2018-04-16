/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource.sim;

import java.time.Instant;
import org.epics.util.stats.Range;
import org.epics.vtype.Display;
import org.epics.vtype.Time;
import org.epics.vtype.VDouble;

/**
 * Base class for all simulated functions that return numbers.
 *
 * @author carcassi
 */
abstract class VDoubleSimFunction extends SimFunction<VDouble> {

    /**
     * The display to be used for all values.
     */
    protected final Display display;

    /**
     * Creates a new simulation function.
     *
     * @param secondsBeetwenSamples seconds between each samples
     * @param classToken simulated class
     */
    VDoubleSimFunction(double secondsBeetwenSamples, Display display) {
        super(secondsBeetwenSamples, VDouble.class);
        this.display = display;
    }
    
    
    @Override
    final VDouble nextValue(Instant instant) {
        double value = nextDouble();
        return VDouble.of(value, display.newAlarmFor(value), Time.of(instant), display);
    }

    /**
     * Returns the next value in the sequence.
     * 
     * @return the new value
     */
    abstract double nextDouble();
    
}
