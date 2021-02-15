/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.property;

import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Type;

/**
 * Implementation of PVControl.
 * @author mrk
 *
 */
public final class PVControlFactory implements PVControl{
    private PVDouble pvLow = null;
    private PVDouble pvHigh = null;
    private PVDouble pvMinStep = null;
    private static final String noControlFound = "No control structure was located";
    private static final String notAttached = "Not attached to an control structure";

    /**
     * Create a PVControl.
     *
     * @return the newly created PVControl
     */
    public static PVControl create() { return new PVControlFactory();}

    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVControl#attach(org.epics.pvdata.pv.PVField)
     */
    public boolean attach(PVField pvField) {
        if(pvField.getField().getType()!=Type.structure) {
            throw new IllegalArgumentException(noControlFound);
        }
        PVStructure pvStructure = (PVStructure)(pvField);
        PVDouble pvDouble = pvStructure.getDoubleField("limitLow");
        if(pvDouble==null) {
            throw new IllegalArgumentException(noControlFound);
        }
        pvLow = pvDouble;
        pvDouble = pvStructure.getDoubleField("limitHigh");
        if(pvDouble==null) {
            throw new IllegalArgumentException(noControlFound);
        }
        pvHigh = pvDouble;
        pvDouble = pvStructure.getDoubleField("minStep");
        if(pvDouble==null) {
            throw new IllegalArgumentException(noControlFound);
        }
        pvMinStep = pvDouble;
        return true;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVControl#detach()
     */
    public void detach() {
        pvLow = null;
        pvHigh = null;
        pvMinStep = null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVControl#isAttached()
     */
    public boolean isAttached() {
        if(pvLow==null || pvHigh==null || pvMinStep==null) return false;
        return true;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVControl#get(org.epics.pvdata.property.Control)
     */
    public void get(Control control) {
        if(pvLow==null || pvHigh==null || pvMinStep==null) {
            throw new IllegalStateException(notAttached);
        }
        control.setLow(pvLow.get());
        control.setHigh(pvHigh.get());
        control.setMinStep(pvMinStep.get());
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVControl#set(org.epics.pvdata.property.Control)
     */
    public boolean set(Control control) {
        if(pvLow==null || pvHigh==null || pvMinStep==null) {
            throw new IllegalStateException(notAttached);
        }
        if(pvLow.isImmutable() || pvHigh.isImmutable()) return false;
        Control current = new Control();
        get(current);
        boolean returnValue = false;
        if(current.getLow()!=control.getLow())
        {
            pvLow.put(control.getLow());
            returnValue = true;
        }
        if(current.getHigh()!=control.getHigh())
        {
            pvHigh.put(control.getHigh());
            returnValue = true;
        }
        if(current.getMinStep()!=control.getMinStep())
        {
            pvMinStep.put(control.getMinStep());
            returnValue = true;
        }
        return returnValue;
    }

}
