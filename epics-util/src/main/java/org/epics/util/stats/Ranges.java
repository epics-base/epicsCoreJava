/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.stats;

/**
 * Utility classes to compute ranges.
 *
 * @author carcassi
 */
public class Ranges {

    /**
     * Returns the range of the absolute values within the range.
     * <p>
     * If the range is all positive, it returns the same range.
     *
     * @param range a range
     * @return the range of the absolute values
     */
    public static Range absRange(Range range) {
        if (range.getMinimum() >= 0 && range.getMaximum() >= 0) {
            return range;
        } else if (range.getMinimum() < 0 && range.getMaximum() < 0) {
            return Range.of(- range.getMaximum(), - range.getMinimum());
        } else {
            return Range.of(0, Math.max(range.getMinimum(), range.getMaximum()));
        }
    }

    /**
     * Percentage, from 0 to 1, of the first range that is contained by
     * the second range.
     *
     * @param range the range to be contained by the second
     * @param otherRange the range that has to contain the first
     * @return from 0 (if there is no intersection) to 1 (if the ranges are the same)
     */
    public static double overlap(Range range, Range otherRange) {
        double minOverlap = Math.max(range.getMinimum(), otherRange.getMinimum());
        double maxOverlap = Math.min(range.getMaximum(), otherRange.getMaximum());
        double overlapWidth = maxOverlap - minOverlap;
        double rangeWidth = range.getMaximum() - range.getMinimum();
        double fraction = Math.max(0.0, overlapWidth / rangeWidth);
        return fraction;
    }
}
