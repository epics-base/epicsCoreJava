/**
 * 
 */
package org.epics.pvData.property;

import java.util.TreeMap;

import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVLong;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Type;

/**
 * @author mrk
 *
 */
public class TimeStampFactory{
    /**
     * Get a TimeStamp for the pvStructure if the structure is a TimeStamp structure.
     * @param pvStructure The structure.
     * @return The TimeStamp interface or null of the structure is not a TimeStamp.
     */
    public static synchronized TimeStamp getTimeStamp(PVStructure pvStructure) {
        String fullName = pvStructure.getFullName();
        TimeStamp timeStamp = timeStampMap.get(fullName);
        if(timeStamp==null) {
            timeStamp = create(pvStructure);
            if(timeStamp==null) return null;
            timeStampMap.put(fullName, timeStamp);
        }
        return timeStamp;
    }
    
    private static TreeMap<String,TimeStamp> timeStampMap = new TreeMap<String,TimeStamp>();
   
    private static TimeStamp create(PVStructure timeStamp) {
        PVField[] pvFields = timeStamp.getPVFields();
        if(pvFields.length!=2) return null;
        PVField fieldPvField = pvFields[0];
        Field field = fieldPvField.getField();
        if(field.getType()!=Type.scalar) return null;
        Scalar scalar = (Scalar)field;
        if(scalar.getScalarType()!=ScalarType.pvLong) return null;
        if(!field.getFieldName().equals("secondsPastEpoch")) return null;
        PVLong secondsPastEpoch = (PVLong)fieldPvField;
        fieldPvField = pvFields[1];
        field = fieldPvField.getField();
        if(field.getType()!=Type.scalar) return null;
        scalar = (Scalar)field;
        if(scalar.getScalarType()!=ScalarType.pvInt) return null;
        if(!field.getFieldName().equals("nanoSeconds")) return null;
        PVInt nanoSeconds = (PVInt)fieldPvField; 
        return new TimeStampImpl(timeStamp,secondsPastEpoch,nanoSeconds);
    }
    
    private static class TimeStampImpl implements TimeStamp {
        private PVLong pvSecond;
        private PVInt pvNano;

        private TimeStampImpl(PVStructure pvTimeStamp,PVLong pvSecond,PVInt pvNano) {
            this.pvSecond = pvSecond;
            this.pvNano = pvNano;
        } 
        /* (non-Javadoc)
         * @see org.epics.pvData.property.TimeStamp#getMilliSeconds()
         */
        public long getMilliSeconds() {
            return pvSecond.get()*1000 + pvNano.get();
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.TimeStamp#getNanoSeconds()
         */
        public int getNanoSeconds() {
            return pvNano.get();
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.TimeStamp#getSecondsPastEpoch()
         */
        public long getSecondsPastEpoch() {
            return pvSecond.get();
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.TimeStamp#put(long, int)
         */
        public void put(long secondsPastEpoch, int nanoSeconds) {
            pvSecond.put(secondsPastEpoch);
            pvNano.put(nanoSeconds);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.TimeStamp#put(long)
         */
        public void put(long milliSeconds) {
            pvSecond.put(milliSeconds/1000);
            pvNano.put(((int)(milliSeconds%1000))*1000000);
        } 
    }
    
}
