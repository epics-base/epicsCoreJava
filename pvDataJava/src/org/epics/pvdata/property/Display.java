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
    Display() {}
    /**
     * Get the low limit.
     * @return The value.
     */
    double getLow() {return low;}
    /**
     * Get the High Limit.
     * @return The value.
     */
    double getHigh() { return high;}
    /**
     * set the low limut.
     * @param value The value.
     */
    void setLow(double value){low = value;}
    /**
     * Set the high limit.
     * @param value The value.
     */
    void setHigh(double value){high = value;}
    /**
     * Get the description.
     * @return The value.
     */
    String getDescription() {return description;}
    /**
     * Set the description.
     * @param value The value.
     */
    void setDescription(String value) {description = value;}
    /**
     * Get the format.
     * @return The value.
     */
    String getFormat() {return format;}
    /**
     * Set the format.
     * @param value The value.
     */
    void setFormat(String value) {format = value;}
    /**
     * Get the units.
     * @return The value.
     */
    String getUnits() {return units;}
    /**
     * Set the units.
     * @param value The value.
     */
    void setUnits(String value) {units = value;}
    
    private String description = "";
    private String format = "";
    private String units = "";
    private double low = 0.0;
    private double high = 0.0;

}
