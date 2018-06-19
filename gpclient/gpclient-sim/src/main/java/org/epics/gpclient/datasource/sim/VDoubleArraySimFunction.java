/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import java.time.Duration;
import java.time.Instant;
import org.epics.util.array.ListDouble;
import org.epics.vtype.Alarm;
import org.epics.vtype.Display;
import org.epics.vtype.Time;
import org.epics.vtype.VDoubleArray;

/**
 * Base class for all simulated functions that return numbers.
 *
 * @author carcassi
 */
abstract class VDoubleArraySimFunction extends SimFunction<VDoubleArray> {

    /**
     * The display to be used for all values.
     */
    protected final Display display;
    
    /**
     * The timestamp for the last reset.
     */
    private Instant initialReference;

    /**
     * Creates a new simulation function.
     *
     * @param secondsBeetwenSamples seconds between each samples
     * @param classToken simulated class
     */
    VDoubleArraySimFunction(double secondsBeetwenSamples, Display display) {
        super(secondsBeetwenSamples, VDoubleArray.class);
        this.display = display;
    }

    @Override
    Instant resetTime() {
        initialReference = Instant.now();
        return initialReference;
    }

    @Override
    final VDoubleArray nextValue(Instant instant) {
        Duration difference = Duration.between(initialReference, instant);
        double t = difference.getSeconds() + difference.getNano() / 1000000000.0;
        ListDouble value = nextListDouble(t);
        return VDoubleArray.of(value, Alarm.none(), Time.of(instant), display);
    }

    /**
     * Returns the next value in the sequence.
     * 
     * @return the new value
     */
    abstract ListDouble nextListDouble(double time);
    
}
