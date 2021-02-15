/*
 * Copyright (C) 2010-18 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import org.epics.util.compat.legacy.lang.Objects;

/**
 * Statistics for double with alarm, timestamp and display information.
 *
 * @author carcassi
 */
public abstract class VStatistics extends Statistics {

    /**
     * Creates a new VStatistics.
     *
     * @param average average
     * @param stdDev standard deviation
     * @param min minimum
     * @param max maximum
     * @param nSamples number of samples
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VStatistics of(final double average, final double stdDev,
            final double min, final double max, final int nSamples, final Alarm alarm,
            final Time time, final Display display) {
        return new IVStatistics(average, stdDev, min, max, nSamples, alarm, time, display);
    }

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        Class type = typeOf(this);
        builder.append(type.getSimpleName())
                .append('[')
                .append("max:").append(getMax())
                .append(", min:").append(getMin())
                .append(", mean:").append(getAverage())
                .append(", std:").append(getStdDev())
                .append(", #samples").append(getNSamples())
                .append(", ")
                .append(getAlarm())
                .append(", ")
                .append(getTime())
                .append(']');
        return builder.toString();
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof VStatistics) {
            VStatistics other = (VStatistics) obj;

            return getClass().equals(other.getClass()) &&
                    getMax().equals(other.getMax()) &&
                    getMin().equals(other.getMin()) &&
                    getAverage().equals(other.getAverage()) &&
                    getStdDev().equals(other.getStdDev()) &&
                    getNSamples().equals(other.getNSamples()) &&
                    getAlarm().equals(other.getAlarm()) &&
                    getTime().equals(other.getTime()) &&
                    getDisplay().equals(other.getDisplay());
        }

        return false;
    }

    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(getMax());
        hash = 23 * hash + Objects.hashCode(getMin());
        hash = 23 * hash + Objects.hashCode(getAverage());
        hash = 23 * hash + Objects.hashCode(getStdDev());
        hash = 23 * hash + Objects.hashCode(getNSamples());
        hash = 23 * hash + Objects.hashCode(getAlarm());
        hash = 23 * hash + Objects.hashCode(getTime());
        hash = 23 * hash + Objects.hashCode(getDisplay());
        return hash;
    }
}
