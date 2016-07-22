/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.property;

/**
 * A class for control limits.
 * @author mrk
 *
 */
public class Control {
    /**
     * Constructor.
     */
    public Control() {}

    /**
     * Get control low.
     *
     * @return the value
     */
    public double getLow() {return low;}

    /**
     * Get control high.
     *
     * @return the value
     */
    public double getHigh() {return high;}

    /**
     * Get control minStep.
     *
     * @return the value
     */
    public double getMinStep() {return minStep;}

    /**
     * Set control low.
     *
     * @param value the value
     */
    public void setLow(double value) {low = value;}

    /**
     * Set control high.
     *
     * @param value the value
     */
    public void setHigh(double value) {high = value;}

    /**
     * Set control minStep.
     *
     * @param value the value
     */
    public void setMinStep(double value) {minStep = value;}
    
    private double low = 0.0;
    private double high = 0.0;
    private double minStep = 0.0;

}
