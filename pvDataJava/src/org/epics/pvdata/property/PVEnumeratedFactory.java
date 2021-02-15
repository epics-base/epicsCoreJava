/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.property;

import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StringArrayData;
import org.epics.pvdata.pv.Type;

/**
 * Implementation of PVEnumerated.
 * @author mrk
 *
 */
public final class PVEnumeratedFactory implements PVEnumerated{
    private PVInt pvIndex = null;
    private PVStringArray pvChoices = null;
    private StringArrayData data = new StringArrayData();
    private static final String notStructure = "field is not a structure";
    private static final String notEnumerated = "field is not an enumerated structure";
    private static final String notAttached = "Not attached to an enumerated structure";

    /**
     * Create a PVControl.
     *
     * @return the newly created PVControl
     */
    public static PVEnumerated create() { return new PVEnumeratedFactory();}

    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVControl#attach(org.epics.pvdata.pv.PVField)
     */
    public boolean attach(PVField pvField) {
        if(pvField.getField().getType()!=Type.structure) {
            throw new IllegalArgumentException(notStructure);
        }
        PVStructure pvStructure = (PVStructure)pvField;
        PVInt pvInt = pvStructure.getIntField("index");
        if(pvInt==null) {
            throw new IllegalArgumentException(notEnumerated);
        }
        PVScalarArray pvScalarArray = pvStructure.getScalarArrayField("choices",ScalarType.pvString);
        if(pvScalarArray==null) {
            throw new IllegalArgumentException(notEnumerated);
        }
        pvIndex = pvInt;
        pvChoices = (PVStringArray)pvScalarArray;
        return true;

    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVControl#detach()
     */
    public void detach() {
        pvIndex = null;
        pvChoices = null;

    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVControl#isAttached()
     */
    public boolean isAttached() {
        if(pvIndex==null || pvChoices==null) return false;
        return true;

    }
    public boolean choicesMutable() {
        if(pvIndex==null || pvChoices==null) {
            throw new IllegalStateException(notAttached);
       }
       return pvChoices.isImmutable();
    }
    public String getChoice() {
        if(pvIndex==null || pvChoices==null) {
            throw new IllegalStateException(notAttached);
       }
       int index = pvIndex.get();
       pvChoices.get(0,pvChoices.getLength(),data);
       if(data.data.length<=index) {
           return String.format("PVEnumerated.getChoice index out of bounds pvChoices %s%n",pvChoices);
       }
       return data.data[index];

    }
    public String[] getChoices() {
        if(pvIndex==null || pvChoices==null) {
            throw new IllegalStateException(notAttached);
       }
       pvChoices.get(0,pvChoices.getLength(),data);
       return data.data;

    }
    public int getIndex() {
        if(pvIndex==null || pvChoices==null) {
            throw new IllegalStateException(notAttached);
       }
       return pvIndex.get();

    }
    public boolean setChoices(String[] choices) {
        if(pvIndex==null || pvChoices==null) {
            throw new IllegalStateException(notAttached);
       }
       if(pvChoices.isImmutable()) return false;
       pvChoices.put(0,choices.length,choices,0);
       return true;

    }
    public boolean setIndex(int index) {
        if(pvIndex==null || pvChoices==null) {
            throw new IllegalStateException(notAttached);
       }
       if(pvIndex.isImmutable()) return false;
       pvIndex.put(index);
       return true;

    }

}
