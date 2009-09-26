/**
 * 
 */
package org.epics.pvData.property;

import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVLong;
import org.epics.pvData.pv.PVRecord;
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
     * Create a TimeStamp.
     * @param secondsPastEpoch initial value for seconds
     * @param nanoSeconds initial value for nanoseconds.
     * @return The TimeStamp interface.
     */
    public static TimeStamp create(long secondsPastEpoch,int nanoSeconds) {
        Field[] fields = new Field[2];
        fields[0] = fieldCreate.createScalar("secondsPastEpoch", ScalarType.pvLong);
        fields[1] = fieldCreate.createScalar("nanoSeconds", ScalarType.pvInt);
        PVStructure pvStructure = pvDataCreate.createPVStructure(null, "timeStamp", fields);
        PVLong pvLong = pvStructure.getLongField("secondsPastEpoch");
        PVInt pvInt = pvStructure.getIntField("nanoSeconds");
        pvLong.put(secondsPastEpoch);
        pvInt.put(nanoSeconds);
        return new TimeStampImpl(pvLong,pvInt);
    }
    /**
     * Get a TimeStamp for the pvStructure if the structure is a TimeStamp structure.
     * @param pvStructure The structure.
     * @return The TimeStamp interface or null of the structure is not a TimeStamp.
     */
    public static synchronized TimeStamp getTimeStamp(PVStructure pvStructure) {
       
        PVField[] pvFields = pvStructure.getPVFields();
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
        return new TimeStampImpl(secondsPastEpoch,nanoSeconds);
    }
    /**
     * Create a TimeStamp.
     * @param milliSeconds the number of milliSeconds since the January 1, 1970, 00:00:00 UTC
     * @return The TimeStamp interface.
     */
    public static TimeStamp create(long milliSeconds) {
    	final TimeStamp timeStamp = TimeStampFactory.create(0, 0);
    	timeStamp.put(milliSeconds);
    	return timeStamp;
    }
    
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    
    private static class TimeStampImpl implements TimeStamp {
        private PVLong pvSecond;
        private PVInt pvNano;

        private TimeStampImpl(PVLong pvSecond,PVInt pvNano) {
            this.pvSecond = pvSecond;
            this.pvNano = pvNano;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.TimeStamp#getMilliSeconds()
         */
        public long getMilliSeconds() {
            return pvSecond.get()*1000 + pvNano.get()/1000000;
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
            PVRecord pvRecord = pvSecond.getPVRecord();
            if(pvRecord!=null) pvRecord.beginGroupPut();
            pvSecond.put(secondsPastEpoch);
            pvNano.put(nanoSeconds);
            if(pvRecord!=null) pvRecord.endGroupPut();
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.TimeStamp#put(long)
         */
        public void put(long milliSeconds) {
            PVRecord pvRecord = pvSecond.getPVRecord();
            if(pvRecord!=null) pvRecord.beginGroupPut();
            pvSecond.put(milliSeconds/1000);
            pvNano.put(((int)(milliSeconds%1000))*1000000);
            if(pvRecord!=null) pvRecord.endGroupPut();
        }

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TimeStampImpl other = (TimeStampImpl) obj;
			if (pvNano == null) {
				if (other.pvNano != null)
					return false;
			} else if (!pvNano.equals(other.pvNano))
				return false;
			if (pvSecond == null) {
				if (other.pvSecond != null)
					return false;
			} else if (!pvSecond.equals(other.pvSecond))
				return false;
			return true;
		}
        
        
        
    }
    
}
