/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.property;

/**
 * @author mrk
 *
 */
public class Display {
    /**
     * The constructor.
     */
    public Display() {}
    /**
     * Get the low limit.
     * @return The value.
     */
    public double getLow() {return low;}
    /**
     * Get the High Limit.
     * @return The value.
     */
    public double getHigh() { return high;}
    /**
     * set the low limut.
     * @param value The value.
     */
    public void setLow(double value){low = value;}
    /**
     * Set the high limit.
     * @param value The value.
     */
    public void setHigh(double value){high = value;}
    /**
     * Get the description.
     * @return The value.
     */
    public String getDescription() {return description;}
    /**
     * Set the description.
     * @param value The value.
     */
    public void setDescription(String value) {description = value;}
    /**
     * Get the format.
     * @return The value.
     */
    public String getFormat() {return format;}
    /**
     * Set the format.
     * @param value The value.
     */
    public void setFormat(String value) {format = value;}
    /**
     * Get the units.
     * @return The value.
     */
    public String getUnits() {return units;}
    /**
     * Set the units.
     * @param value The value.
     */
    public void setUnits(String value) {units = value;}
    
    private String description = "";
    private String format = "";
    private String units = "";
    private double low = 0.0;
    private double high = 0.0;

}
