/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
    private static final String noControlFound = "No control structure was located";
    private static final String notAttached = "Not attached to an control structure";

    /**
     * Create a PVControl.
     * @return The newly created PVControl.
     */
    public static PVControl create() { return new PVControlFactory();} 
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVControl#attach(org.epics.pvdata.pv.PVField)
     */
    @Override
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
        return true;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVControl#detach()
     */
    @Override
    public void detach() {
        pvLow = null;
        pvHigh = null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVControl#isAttached()
     */
    @Override
    public boolean isAttached() {
        if(pvLow==null || pvHigh==null) return false;
        return true;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVControl#get(org.epics.pvdata.property.Control)
     */
    @Override
    public void get(Control control) {
        if(pvLow==null || pvHigh==null) {
            throw new IllegalStateException(notAttached);
        }
        control.setLow(pvLow.get());
        control.setHigh(pvHigh.get());
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVControl#set(org.epics.pvdata.property.Control)
     */
    @Override
    public boolean set(Control control) {
        if(pvLow==null || pvHigh==null) {
            throw new IllegalStateException(notAttached);
        }
        if(pvLow.isImmutable() || pvHigh.isImmutable()) return false;
        pvLow.put(control.getLow());
        pvHigh.put(control.getHigh());
        return true;
    }

}
