/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
     * @return The value.
     */
    public double getLow() {return low;}
    /**
     * Get control high.
     * @return The value.
     */
    public double getHigh() {return high;}
    /**
     * Get control minStep.
     * @return The value.
     */
    public double getMinStep() {return minStep;}
    /**
     * Set control low.
     * @param value The value.
     */
    public void setLow(double value) {low = value;}
    /**
     * Set control high.
     * @param value The value.
     */
    public void setHigh(double value) {high = value;}
    /**
     * Set control minStep.
     * @param value The value.
     */
    public void setMinStep(double value) {minStep = value;}
    
    private double low = 0.0;
    private double high = 0.0;
    private double minStep = 0.0;

}
