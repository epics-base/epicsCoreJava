/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.property;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVLong;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Type;

/**
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
     * @return The newly created PVTimeStamp.
     */
    public static PVTimeStamp create() { return new PVTimeStampFactory();} 
    /* (non-Javadoc)
     * @see org.epics.pvData.property.PVTimeStamp#attach(org.epics.pvData.pv.PVField)
     */
    @Override
    public boolean attach(PVField pvField) {
        PVStructure pvStructure = null;
        if(!pvField.getField().getFieldName().equals("timeStamp")) {
            if(!pvField.getField().getFieldName().equals("value")) {
                pvField.message(noTimeStampFound,MessageType.error);
                return false;
            }
            PVStructure pvParent = pvField.getParent();
            if(pvParent==null) {
                pvField.message(noTimeStampFound,MessageType.error);
                return false;
            }
            // look up the tree for a timeSyamp
            while(pvParent!=null) {
                PVStructure pvs = pvParent.getStructureField("timeStamp");
                if(pvs!=null) {
                    pvStructure = pvs;
                    break;
                }
                pvParent = pvParent.getParent();
            }
            if(pvStructure==null) {
                pvField.message(noTimeStampFound,MessageType.error);
                return false;
            }
        } else {
            if(pvField.getField().getType()!=Type.structure) {
                pvField.message(noTimeStampFound,MessageType.error);
                return false;
            }
            pvStructure = (PVStructure)(pvField);
        }
        PVLong pvLong = pvStructure.getLongField("secondsPastEpoch");
        if(pvLong==null) {
            pvField.message(noTimeStampFound,MessageType.error);
            return false;
        }
        pvSecs = pvLong;
        PVInt pvInt = pvStructure.getIntField("nanoSeconds");
        if(pvInt==null) {
            pvField.message(noTimeStampFound,MessageType.error);
            return false;
        }
        pvNano = pvInt;
        pvInt = pvStructure.getIntField("userTag");
        if(pvInt==null) {
            pvField.message(noTimeStampFound,MessageType.error);
            return false;
        }
        pvUserTag = pvInt;
        return true;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.PVTimeStamp#detach()
     */
    @Override
    public void detach() {
        pvSecs = null;
        pvUserTag = null;
        pvNano = null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.PVTimeStamp#isAttached()
     */
    @Override
    public boolean isAttached() {
        if(pvSecs==null || pvNano==null) return false;
        return true;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.PVTimeStamp#get(org.epics.pvData.property.TimeStamp)
     */
    @Override
    public void get(TimeStamp timeStamp) {
        if(pvSecs==null || pvNano==null) {
            throw new IllegalStateException(notAttached);
        }
        timeStamp.put(pvSecs.get(), pvNano.get());
        timeStamp.setUserTag(pvUserTag.get());
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.PVTimeStamp#set(org.epics.pvData.property.TimeStamp)
     */
    @Override
    public boolean set(TimeStamp timeStamp) {
        if(pvSecs==null || pvNano==null || pvUserTag==null) {
            throw new IllegalStateException(notAttached);
        }
        if(pvSecs.isImmutable() || pvNano.isImmutable()) return false;
        pvSecs.put(timeStamp.getSecondsPastEpoch());
        pvUserTag.put(timeStamp.getUserTag());
        pvNano.put(timeStamp.getNanoSeconds());
        return true;
    }


}
