/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.property;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Type;

/**
 * Implementation of PVTimeStamp.
 * @author mrk
 *
 */
public class PVTimeStampFactory implements PVTimeStamp {
    private PVLong pvSecs = null;
    private PVInt pvNano = null;
    private PVInt pvUserTag = null;
    private static final String noTimeStampFound = "No timeStamp structure was located";
    private static final String notAttached = "Not attached to an timeStamp structure";

    /**
     * Create a PVTimeStamp.
     *
     * @return the newly created PVTimeStamp
     */
    public static PVTimeStamp create() { return new PVTimeStampFactory();}

    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVTimeStamp#attach(org.epics.pvdata.pv.PVField)
     */
    public boolean attach(PVField pvField) {
        if(pvField.getField().getType()!=Type.structure) {
            throw new IllegalArgumentException(noTimeStampFound);
        }
        PVStructure pvStructure = (PVStructure)(pvField);
        while(true) {
            PVField xxx = pvStructure.getSubField("secondsPastEpoch");
            if(xxx!=null) xxx = pvStructure.getSubField("nanoseconds");
            if(xxx!=null) xxx = pvStructure.getSubField("userTag");
            if(xxx!=null) {
                pvSecs = pvStructure.getLongField("secondsPastEpoch");
                pvNano = pvStructure.getIntField("nanoseconds");
                pvUserTag = pvStructure.getIntField("userTag");
                if(pvSecs!=null && pvNano!=null && pvUserTag!=null) return true;
            }
            pvSecs = null;
            pvNano = null;
            pvUserTag = null;
            // look up the tree for a timeSyamp
            pvStructure = pvStructure.getParent();
            if(pvStructure==null) break;
        }
        return false;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVTimeStamp#detach()
     */
    public void detach() {
        pvSecs = null;
        pvUserTag = null;
        pvNano = null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVTimeStamp#isAttached()
     */
    public boolean isAttached() {
        if(pvSecs==null || pvNano==null) return false;
        return true;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVTimeStamp#get(org.epics.pvdata.property.TimeStamp)
     */
    public void get(TimeStamp timeStamp) {
        if(pvSecs==null || pvNano==null) {
            throw new IllegalStateException(notAttached);
        }
        timeStamp.put(pvSecs.get(), pvNano.get());
        timeStamp.setUserTag(pvUserTag.get());
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVTimeStamp#set(org.epics.pvdata.property.TimeStamp)
     */
    public boolean set(TimeStamp timeStamp) {
        if(pvSecs==null || pvNano==null || pvUserTag==null) {
            throw new IllegalStateException(notAttached);
        }
        if(pvSecs.isImmutable() || pvNano.isImmutable()) return false;
        TimeStamp current = TimeStampFactory.create();
        get(current);
        boolean returnValue = false;
        if(current.getSecondsPastEpoch()!=timeStamp.getSecondsPastEpoch())
        {
            pvSecs.put(timeStamp.getSecondsPastEpoch());
            returnValue = true;
        }
        if(current.getNanoseconds()!=timeStamp.getNanoseconds())
        {
            pvNano.put(timeStamp.getNanoseconds());
            returnValue = true;
        }
        if(current.getUserTag()!=timeStamp.getUserTag())
        {
            pvUserTag.put(timeStamp.getUserTag());
            returnValue = true;
        }
        return returnValue;
    }


}
