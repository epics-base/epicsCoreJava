/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.property;

import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Type;

/**
 * Implementation of PVDisplay.
 * @author mrk
 *
 */
public final class PVDisplayFactory implements PVDisplay{
    private PVString pvDescription = null;
    private PVString pvFormat = null;
    private PVString pvUnits = null;
    private PVDouble pvLow = null;
    private PVDouble pvHigh = null;
    private static final String noDisplayFound = "No display structure was located";
    private static final String notAttached = "Not attached to an display structure";

    /**
     * Create a PVDisplay.
     *
     * @return the newly created PVDisplay
     */
    public static PVDisplay create() { return new PVDisplayFactory();}

    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVDisplay#attach(org.epics.pvdata.pv.PVField)
     */
    public boolean attach(PVField pvField) {
        if(pvField.getField().getType()!=Type.structure) {
            throw new IllegalArgumentException(noDisplayFound);
        }
        PVStructure pvStructure = (PVStructure)(pvField);
        pvDescription = pvStructure.getStringField("description");
        if(pvDescription==null) {
            throw new IllegalArgumentException(noDisplayFound);
        }
        pvFormat = pvStructure.getStringField("format");
        if(pvFormat==null) {
            throw new IllegalArgumentException(noDisplayFound);
        }
        pvUnits = pvStructure.getStringField("units");
        if(pvUnits==null) {
            throw new IllegalArgumentException(noDisplayFound);
        }
        pvLow = pvStructure.getDoubleField("limitLow");
        if(pvLow==null) {
            throw new IllegalArgumentException(noDisplayFound);
        }
        pvHigh = pvStructure.getDoubleField("limitHigh");
        if(pvHigh==null) {
            throw new IllegalArgumentException(noDisplayFound);
        }
        return true;

    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVDisplay#detach()
     */
    public void detach() {
        pvDescription = null;
        pvFormat = null;
        pvUnits = null;
        pvLow = null;
        pvHigh = null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVDisplay#isAttached()
     */
    public boolean isAttached() {
        if(pvDescription==null || pvFormat==null || pvUnits==null || pvLow==null || pvHigh==null) return false;
        return true;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVDisplay#get(org.epics.pvdata.property.Display)
     */
    public void get(Display display) {
        if(pvDescription==null || pvFormat==null || pvUnits==null || pvLow==null || pvHigh==null) {
            throw new IllegalStateException(notAttached);
        }
        display.setDescription(pvDescription.get());
        display.setFormat(pvFormat.get());
        display.setUnits(pvUnits.get());
        display.setLow(pvLow.get());
        display.setHigh(pvHigh.get());

    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVDisplay#set(org.epics.pvdata.property.Display)
     */
    public boolean set(Display display) {
        if(pvDescription==null || pvFormat==null || pvUnits==null || pvLow==null || pvHigh==null) {
            throw new IllegalStateException(notAttached);
        }
        if(pvDescription.isImmutable() || pvFormat.isImmutable() || pvUnits.isImmutable()) return false;
        if(pvLow.isImmutable() || pvHigh.isImmutable()) return false;
        Display current = new Display();
        get(current);
        boolean returnValue = false;
        if(!current.getDescription().equals(display.getDescription()))
        {
            pvDescription.put(display.getDescription());
            returnValue = true;
        }
        if(!current.getFormat().equals(display.getFormat()))
        {
            pvFormat.put(display.getFormat());
            returnValue = true;
        }
        if(!current.getUnits().equals(display.getUnits()))
        {
            pvUnits.put(display.getUnits());
            returnValue = true;
        }
        if(current.getLow()!=display.getLow())
        {
            pvLow.put(display.getLow());
            returnValue = true;
        }
        if(current.getHigh()!=display.getHigh())
        {
            pvHigh.put(display.getHigh());
            returnValue = true;
        }
        return returnValue;
    }
}
