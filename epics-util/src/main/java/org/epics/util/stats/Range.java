/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.stats;

/**
 * A range of numeric values.
 * <p>
 * For the purpose of range calculation, NaNs should be skipped. The only case
 * where NaNs are allowed is for the UNDEFINED range.
 * <p>
 * The minimum and maximum are simply double values.
 *
 * @author carcassi
 */
public final class Range {
    
    private static final Range UNDEFINED = new Range(Double.NaN, Double.NaN, false);
    
    private final double min;
    private final double max;
    private final boolean reversed;

    Range(double min, double max, boolean reversed) {
        this.min = min;
        this.max = max;
        this.reversed = reversed;
    }
    
    /**
     * The minimum value.
     * 
     * @return a value
     */
    public double getMinimum() {
        return min;
    }
    
    /**
     * The maximum value.
     * 
     * @return a value
     */
    public double getMaximum() {
        return max;
    }

    /**
     * Whether the range goes from min to max or from max to min.
     * 
     * @return true if range should be traversed from max to min
     */
    public boolean isReversed() {
        return reversed;
    }

    /**
     * Whether the range is finite and non-zero.
     * 
     * @return true if range is finite and non-zero
     */
    public boolean isFinite() {
        return min != max && !Double.isNaN(min) && !Double.isInfinite(min) &&
                !Double.isNaN(max) && !Double.isInfinite(max);
    }
    
    /**
     * Returns the value normalized within the range. It performs a linear
     * transformation where the minimum value of the range becomes 0 while
     * the maximum becomes 1.
     * 
     * @param value a value
     * @return the value transformed based on the range
     */
    public double normalize(double value) {
        return (value - getMinimum()) / (getMaximum() - getMinimum());
    }
    
    /**
     * Takes a normalized value and returns a proportional
     * value within the range. It performs a linear
     * transformation where 0 becomes the minimum value of the range while
     * 1 becomes the maximum.
     * 
     * @param value a value
     * @return the value transformed based on the range
     */
    public double rescale(double value) {
        return getMinimum() + value * (getMaximum() - getMinimum());
    }

    /**
     * Determines whether the value is contained by the range or not.
     * 
     * @param value a value
     * @return true if the value is within the range
     */
    public boolean contains(double value) {
        return value >= getMinimum() && value <= getMaximum();
    }
    
    /**
     * Determines whether the given range is contained by the range or not.
     * 
     * @param range a range
     * @return true if the range is a subrange of this
     */
    public boolean contains(Range range) {
        return getMinimum() <= range.getMinimum()
                && getMaximum() >= range.getMaximum();
        
    }

    /**
     * Determines the range that can contain both ranges. If one of the
     * ranges in contained in the other, the bigger range is returned.
     * 
     * @param other another range
     * @return the bigger range
     */
    public Range combine(Range other) {
        if (this == UNDEFINED) {
            return other;
        }
        
        if (other == UNDEFINED) {
            return this;
        }
        
        if (getMinimum() <= other.getMinimum()) {
            if (getMaximum() >= other.getMaximum()) {
                return this;
            } else {
                return of(getMinimum(), other.getMaximum());
            }
        } else {
            if (getMaximum() >= other.getMaximum()) {
                return of(other.getMinimum(), getMaximum());
            } else {
                return other;
            }
        }
    }

    /**
     * Returns a new range with the same center value and width equal to the
     * original width multiplied by the given factor.
     * 
     * @param factor the multiplicative factor to resize the range width
     * @return a new range
     */
    public Range shrink(double factor) {
        if (this == UNDEFINED) {
            return UNDEFINED;
        }
        
        double center = (min + max) / 2;
        double width = max - min;
        
        return Range.of(center - width * factor / 2, center + width * factor / 2);
    }
    
    /**
     * An undefined range.
     * 
     * @return the undefined range
     */
    public static Range undefined() {
        return UNDEFINED;
    }

    @Override
    public String toString() {
        if (!isReversed()) {
            return "[" + getMinimum() + " - " + getMaximum() + "]";
        } else {
            return "[" + getMaximum() + " - " + getMinimum() + "]";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        
        if (obj instanceof Range) {
            Range other = (Range) obj;
            return getMinimum() == other.getMinimum() &&
                    getMaximum() == other.getMaximum() &&
                    isReversed() == other.isReversed();
        }
        
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.min) ^ (Double.doubleToLongBits(this.min) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.max) ^ (Double.doubleToLongBits(this.max) >>> 32));
        hash = 97 * hash + (this.reversed ? 1 : 0);
        return hash;
    }
    
    /**
     * Range from given min and max. If max is greater than min, a reversed
     * range is returned.
     * 
     * @param minValue minimum value
     * @param maxValue maximum value
     * @return the range
     */
    public static Range of(final double minValue, final double maxValue) {
        if (Double.isNaN(minValue) && Double.isNaN(maxValue)) {
            return Range.UNDEFINED;
        }
        else if (Double.isNaN(minValue)) {
            return new Range(Double.NEGATIVE_INFINITY, maxValue, false);
        }
        else if (Double.isNaN(maxValue)) {
            return new Range(minValue, Double.POSITIVE_INFINITY, false);
        }

        if (minValue > maxValue) {
            return new Range(maxValue, minValue, true);
        }
        return new Range(minValue, maxValue, false);
    }
    
}
