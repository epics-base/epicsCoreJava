/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;
import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.BooleanArrayData;
import org.epics.pvData.pv.ByteArrayData;
import org.epics.pvData.pv.DoubleArrayData;
import org.epics.pvData.pv.FloatArrayData;
import org.epics.pvData.pv.IntArrayData;
import org.epics.pvData.pv.LongArrayData;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVBoolean;
import org.epics.pvData.pv.PVBooleanArray;
import org.epics.pvData.pv.PVByte;
import org.epics.pvData.pv.PVByteArray;
import org.epics.pvData.pv.PVDouble;
import org.epics.pvData.pv.PVDoubleArray;
import org.epics.pvData.pv.PVFloat;
import org.epics.pvData.pv.PVFloatArray;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVIntArray;
import org.epics.pvData.pv.PVLong;
import org.epics.pvData.pv.PVLongArray;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVShort;
import org.epics.pvData.pv.PVShortArray;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStringArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ShortArrayData;
import org.epics.pvData.pv.StringArrayData;

/**
 * Create a PVField that shares the data from another PVField.
 * The original pvField is replaced by the newlt created PVField.
 * @author mrk
 *
 */
public class PVShareFactory {
    /**
     * Replace pvNow with an implementation that shares the data from pvShare.
     * The original pvNow is replaced with the new implementation.
     * When a get of put is made to the new PVField the get or put method of the shared
     * PVField is called.
     * @param pvNow The original PVScalar to replace.
     * @param pvShare The field from which data will be shared.
     * @return The newly created PVScalar.
     */
    public static PVScalar replace(PVScalar pvNow,PVScalar pvShare) {
        PVScalar newPVField = createScalar(pvNow.getParent(),(PVScalar)pvShare);
        pvNow.replacePVField(newPVField);
        return newPVField;
    }
    /**
     * Replace pvNow with an implementation that shares the data from pvShare.
     * The original pvNow is replaced with the new implementation.
     * When a get of put is made to the new PVField the get or put method of the shared
     * PVField is called.
     * @param pvNow The original PVScalar to replace.
     * @param pvShare The field from which data will be shared.
     * @return The newly created PVScalar.
     */
    public static PVArray replace(PVArray pvNow,PVArray pvShare) {
        PVArray newPVField = createArray(pvNow.getParent(),(PVArray)pvShare);
        pvNow.replacePVField(newPVField);
        return newPVField;
    }
    
    private static PVScalar createScalar(PVStructure pvParent,PVScalar pvShare) {
        Scalar scalar = pvShare.getScalar();
        switch(scalar.getScalarType()) {
        case pvBoolean:
            return new SharePVBooleanImpl(pvParent,(PVBoolean)pvShare);
        case pvByte:
            return new SharePVByteImpl(pvParent,(PVByte)pvShare);
        case pvShort:
            return new SharePVShortImpl(pvParent,(PVShort)pvShare);
        case pvInt:
            return new SharePVIntImpl(pvParent,(PVInt)pvShare);
        case pvLong:
            return new SharePVLongImpl(pvParent,(PVLong)pvShare);
        case pvFloat:
            return new SharePVFloatImpl(pvParent,(PVFloat)pvShare);
        case pvDouble:
            return new SharePVDoubleImpl(pvParent,(PVDouble)pvShare);
        case pvString:
            return new SharePVStringImpl(pvParent,(PVString)pvShare);
        }
        return null;
        
    }
    
    private static PVArray createArray(PVStructure pvParent,PVArray pvShare) {
        Array array = pvShare.getArray();
        switch(array.getElementType()) {
        case pvBoolean:
            return new SharePVBooleanArrayImpl(pvParent,(PVBooleanArray)pvShare);
        case pvByte:
            return new SharePVByteArrayImpl(pvParent,(PVByteArray)pvShare);
        case pvShort:
            return new SharePVShortArrayImpl(pvParent,(PVShortArray)pvShare);
        case pvInt:
            return new SharePVIntArrayImpl(pvParent,(PVIntArray)pvShare);
        case pvLong:
            return new SharePVLongArrayImpl(pvParent,(PVLongArray)pvShare);
        case pvFloat:
            return new SharePVFloatArrayImpl(pvParent,(PVFloatArray)pvShare);
        case pvDouble:
            return new SharePVDoubleArrayImpl(pvParent,(PVDoubleArray)pvShare);
        case pvString:
            return new SharePVStringArrayImpl(pvParent,(PVStringArray)pvShare);
        }
        return null;
    }
    
    private static class SharePVBooleanImpl extends AbstractSharePVScalar implements PVBoolean
    {
        private PVBoolean pvShare = null;
        
        private SharePVBooleanImpl(PVStructure parent,PVBoolean pvShare) {
            super(parent,pvShare);
            this.pvShare = pvShare;
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVBoolean#get()
         */
        @Override
        public boolean get() {
            super.lockShare();
            try {
                return pvShare.get();
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVBoolean#put(boolean)
         */
        @Override
        public void put(boolean value) {
            super.lockShare();
            try {
                pvShare.put(value);
            } finally {
                super.unlockShare();
            }
            super.postPut();
        }      
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            super.lockThis();
            try {
                return pvShare.equals(obj);
            } finally {
                super.unlockThis();
            }
        }
    }
    
    private static class SharePVByteImpl extends AbstractSharePVScalar implements PVByte
    {
        private PVByte pvShare = null;
        
        private SharePVByteImpl(PVStructure parent,PVByte pvShare) {
            super(parent,pvShare);
            this.pvShare = pvShare;
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVByte#get()
         */
        @Override
        public byte get() {
            super.lockShare();
            try {
                return pvShare.get();
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVByte#put(byte)
         */
        @Override
        public void put(byte value) {
            super.lockShare();
            try {
                pvShare.put(value);
            } finally {
                super.unlockShare();
            }
            super.postPut();
        }      
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            super.lockThis();
            try {
                return pvShare.equals(obj);
            } finally {
                super.unlockThis();
            }
        }
    }
    
    private static class SharePVShortImpl extends AbstractSharePVScalar implements PVShort
    {
        private PVShort pvShare = null;
        
        private SharePVShortImpl(PVStructure parent,PVShort pvShare) {
            super(parent,pvShare);
            this.pvShare = pvShare;
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVShort#get()
         */
        @Override
        public short get() {
            super.lockShare();
            try {
                return pvShare.get();
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVShort#put(short)
         */
        @Override
        public void put(short value) {
            super.lockShare();
            try {
                pvShare.put(value);
            } finally {
                super.unlockShare();
            }
            super.postPut();
        }      
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            super.lockThis();
            try {
                return pvShare.equals(obj);
            } finally {
                super.unlockThis();
            }
        }
    }
    
    private static class SharePVIntImpl extends AbstractSharePVScalar implements PVInt
    {
        private PVInt pvShare = null;
        
        private SharePVIntImpl(PVStructure parent,PVInt pvShare) {
            super(parent,pvShare);
            this.pvShare = pvShare;
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVInt#get()
         */
        @Override
        public int get() {
            super.lockShare();
            try {
                return pvShare.get();
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVInt#put(int)
         */
        @Override
        public void put(int value) {
            super.lockShare();
            try {
                pvShare.put(value);
            } finally {
                super.unlockShare();
            }
            super.postPut();
        }      
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            super.lockThis();
            try {
                return pvShare.equals(obj);
            } finally {
                super.unlockThis();
            }
        }
    }
    
    private static class SharePVLongImpl extends AbstractSharePVScalar implements PVLong
    {
        private PVLong pvShare = null;
        
        private SharePVLongImpl(PVStructure parent,PVLong pvShare) {
            super(parent,pvShare);
            this.pvShare = pvShare;
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVLong#get()
         */
        @Override
        public long get() {
            super.lockShare();
            try {
                return pvShare.get();
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVLong#put(long)
         */
        @Override
        public void put(long value) {
            super.lockShare();
            try {
                pvShare.put(value);
            } finally {
                super.unlockShare();
            }
            super.postPut();
        }      
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            super.lockThis();
            try {
                return pvShare.equals(obj);
            } finally {
                super.unlockThis();
            }
        }
    }
    
    private static class SharePVFloatImpl extends AbstractSharePVScalar implements PVFloat
    {
        private PVFloat pvShare = null;
        
        private SharePVFloatImpl(PVStructure parent,PVFloat pvShare) {
            super(parent,pvShare);
            this.pvShare = pvShare;
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVFloat#get()
         */
        @Override
        public float get() {
            super.lockShare();
            try {
                return pvShare.get();
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVFloat#put(float)
         */
        @Override
        public void put(float value) {
            super.lockShare();
            try {
                pvShare.put(value);
            } finally {
                super.unlockShare();
            }
            super.postPut();
        }      
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            super.lockThis();
            try {
                return pvShare.equals(obj);
            } finally {
                super.unlockThis();
            }
        }
    }
    
    private static class SharePVDoubleImpl extends AbstractSharePVScalar implements PVDouble
    {
        private PVDouble pvShare = null;
        
        private SharePVDoubleImpl(PVStructure parent,PVDouble pvShare) {
            super(parent,pvShare);
            this.pvShare = pvShare;
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDouble#get()
         */
        @Override
        public double get() {
            super.lockShare();
            try {
                return pvShare.get();
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDouble#put(double)
         */
        @Override
        public void put(double value) {
            super.lockShare();
            try {
                pvShare.put(value);
            } finally {
                super.unlockShare();
            }
            super.postPut();
        }      
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            super.lockThis();
            try {
                return pvShare.equals(obj);
            } finally {
                super.unlockThis();
            }
        }
    }
    
    private static class SharePVStringImpl extends AbstractSharePVScalar implements PVString
    {
        private PVString pvShare = null;
        
        private SharePVStringImpl(PVStructure parent,PVString pvShare) {
            super(parent,pvShare);
            this.pvShare = pvShare;
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVString#get()
         */
        @Override
        public String get() {
            super.lockShare();
            try {
                return pvShare.get();
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVString#put(String)
         */
        @Override
        public void put(String value) {
            super.lockShare();
            try {
                pvShare.put(value);
            } finally {
                super.unlockShare();
            }
            super.postPut();
        }      
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            super.lockThis();
            try {
                return pvShare.equals(obj);
            } finally {
                super.unlockThis();
            }
        }
    }
    
    private static class SharePVBooleanArrayImpl extends AbstractSharePVArray implements PVBooleanArray
    {
        private PVBooleanArray pvShare;
        
        private SharePVBooleanArrayImpl(PVStructure parent,PVBooleanArray pvShare)
        {
            super(parent,pvShare);
            this.pvShare = pvShare;
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVBooleanArray#get(int, int, org.epics.pvData.pv.BooleanArrayData)
         */
        @Override
        public int get(int offset, int len, BooleanArrayData data) {
            super.lockShare();
            try {
                return pvShare.get(offset, len, data);
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVBooleanArray#put(int, int, boolean[], int)
         */
        @Override
        public int put(int offset, int length, boolean[] from, int fromOffset) {
            super.lockShare();
            try {
                PVRecord pvShareRecord = pvShare.getPVRecord();
                if(pvShareRecord!=null) pvShareRecord.beginGroupPut();
                int number = pvShare.put(offset, length, from, fromOffset);
                if(pvShareRecord!=null) pvShareRecord.endGroupPut();
                return number;
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVBooleanArray#shareData(boolean[])
         */
        @Override
        public void shareData(boolean[] from) {
            pvShare.shareData(from);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            super.lockThis();
            try {
                return pvShare.equals(obj);
            } finally {
                super.unlockThis();
            }
        }
    }
    
    private static class SharePVByteArrayImpl extends AbstractSharePVArray implements PVByteArray
    {
        private PVByteArray pvShare;
        
        private SharePVByteArrayImpl(PVStructure parent,PVByteArray pvShare)
        {
            super(parent,pvShare);
            this.pvShare = pvShare;
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVByteArray#get(int, int, org.epics.pvData.pv.ByteArrayData)
         */
        @Override
        public int get(int offset, int len, ByteArrayData data) {
            super.lockShare();
            try {
                return pvShare.get(offset, len, data);
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVByteArray#put(int, int, byte[], int)
         */
        @Override
        public int put(int offset, int length, byte[] from, int fromOffset) {
            super.lockShare();
            try {
                PVRecord pvShareRecord = pvShare.getPVRecord();
                if(pvShareRecord!=null) pvShareRecord.beginGroupPut();
                int number = pvShare.put(offset, length, from, fromOffset);
                if(pvShareRecord!=null) pvShareRecord.endGroupPut();
                return number;
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVByteArray#shareData(byte[])
         */
        @Override
        public void shareData(byte[] from) {
            pvShare.shareData(from);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            super.lockThis();
            try {
                return pvShare.equals(obj);
            } finally {
                super.unlockThis();
            }
        }
    }
    
    private static class SharePVShortArrayImpl extends AbstractSharePVArray implements PVShortArray
    {
        private PVShortArray pvShare;
        
        private SharePVShortArrayImpl(PVStructure parent,PVShortArray pvShare)
        {
            super(parent,pvShare);
            this.pvShare = pvShare;
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVShortArray#get(int, int, org.epics.pvData.pv.ShortArrayData)
         */
        @Override
        public int get(int offset, int len, ShortArrayData data) {
            super.lockShare();
            try {
                return pvShare.get(offset, len, data);
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVShortArray#put(int, int, short[], int)
         */
        @Override
        public int put(int offset, int length, short[] from, int fromOffset) {
            super.lockShare();
            try {
                PVRecord pvShareRecord = pvShare.getPVRecord();
                if(pvShareRecord!=null) pvShareRecord.beginGroupPut();
                int number = pvShare.put(offset, length, from, fromOffset);
                if(pvShareRecord!=null) pvShareRecord.endGroupPut();
                return number;
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVShortArray#shareData(short[])
         */
        @Override
        public void shareData(short[] from) {
            pvShare.shareData(from);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            super.lockThis();
            try {
                return pvShare.equals(obj);
            } finally {
                super.unlockThis();
            }
        }
    }
    
    private static class SharePVIntArrayImpl extends AbstractSharePVArray implements PVIntArray
    {
        private PVIntArray pvShare;
        
        private SharePVIntArrayImpl(PVStructure parent,PVIntArray pvShare)
        {
            super(parent,pvShare);
            this.pvShare = pvShare;
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVIntArray#get(int, int, org.epics.pvData.pv.IntArrayData)
         */
        @Override
        public int get(int offset, int len, IntArrayData data) {
            super.lockShare();
            try {
                return pvShare.get(offset, len, data);
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVIntArray#put(int, int, int[], int)
         */
        @Override
        public int put(int offset, int length, int[] from, int fromOffset) {
            super.lockShare();
            try {
                PVRecord pvShareRecord = pvShare.getPVRecord();
                if(pvShareRecord!=null) pvShareRecord.beginGroupPut();
                int number = pvShare.put(offset, length, from, fromOffset);
                if(pvShareRecord!=null) pvShareRecord.endGroupPut();
                return number;
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVIntArray#shareData(int[])
         */
        @Override
        public void shareData(int[] from) {
            pvShare.shareData(from);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            super.lockThis();
            try {
                return pvShare.equals(obj);
            } finally {
                super.unlockThis();
            }
        }
    }
    
    private static class SharePVLongArrayImpl extends AbstractSharePVArray implements PVLongArray
    {
        private PVLongArray pvShare;
        
        private SharePVLongArrayImpl(PVStructure parent,PVLongArray pvShare)
        {
            super(parent,pvShare);
            this.pvShare = pvShare;
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVLongArray#get(int, int, org.epics.pvData.pv.LongArrayData)
         */
        @Override
        public int get(int offset, int len, LongArrayData data) {
            super.lockShare();
            try {
                return pvShare.get(offset, len, data);
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVLongArray#put(int, int, long[], int)
         */
        @Override
        public int put(int offset, int length, long[] from, int fromOffset) {
            super.lockShare();
            try {
                PVRecord pvShareRecord = pvShare.getPVRecord();
                if(pvShareRecord!=null) pvShareRecord.beginGroupPut();
                int number = pvShare.put(offset, length, from, fromOffset);
                if(pvShareRecord!=null) pvShareRecord.endGroupPut();
                return number;
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVLongArray#shareData(long[])
         */
        @Override
        public void shareData(long[] from) {
            pvShare.shareData(from);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            super.lockThis();
            try {
                return pvShare.equals(obj);
            } finally {
                super.unlockThis();
            }
        }
    }
    
    private static class SharePVFloatArrayImpl extends AbstractSharePVArray implements PVFloatArray
    {
        private PVFloatArray pvShare;
        
        private SharePVFloatArrayImpl(PVStructure parent,PVFloatArray pvShare)
        {
            super(parent,pvShare);
            this.pvShare = pvShare;
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVFloatArray#get(int, int, org.epics.pvData.pv.FloatArrayData)
         */
        @Override
        public int get(int offset, int len, FloatArrayData data) {
            super.lockShare();
            try {
                return pvShare.get(offset, len, data);
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVFloatArray#put(int, int, float[], int)
         */
        @Override
        public int put(int offset, int length, float[] from, int fromOffset) {
            super.lockShare();
            try {
                PVRecord pvShareRecord = pvShare.getPVRecord();
                if(pvShareRecord!=null) pvShareRecord.beginGroupPut();
                int number = pvShare.put(offset, length, from, fromOffset);
                if(pvShareRecord!=null) pvShareRecord.endGroupPut();
                return number;
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVFloatArray#shareData(float[])
         */
        @Override
        public void shareData(float[] from) {
            pvShare.shareData(from);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            super.lockThis();
            try {
                return pvShare.equals(obj);
            } finally {
                super.unlockThis();
            }
        }
    }
    
    private static class SharePVDoubleArrayImpl extends AbstractSharePVArray implements PVDoubleArray
    {
        private PVDoubleArray pvShare;
        
        private SharePVDoubleArrayImpl(PVStructure parent,PVDoubleArray pvShare)
        {
            super(parent,pvShare);
            this.pvShare = pvShare;
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDoubleArray#get(int, int, org.epics.pvData.pv.DoubleArrayData)
         */
        @Override
        public int get(int offset, int len, DoubleArrayData data) {
            super.lockShare();
            try {
                return pvShare.get(offset, len, data);
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDoubleArray#put(int, int, double[], int)
         */
        @Override
        public int put(int offset, int length, double[] from, int fromOffset) {
            super.lockShare();
            try {
                PVRecord pvShareRecord = pvShare.getPVRecord();
                if(pvShareRecord!=null) pvShareRecord.beginGroupPut();
                int number = pvShare.put(offset, length, from, fromOffset);
                if(pvShareRecord!=null) pvShareRecord.endGroupPut();
                return number;
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDoubleArray#shareData(double[])
         */
        @Override
        public void shareData(double[] from) {
            pvShare.shareData(from);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            super.lockThis();
            try {
                return pvShare.equals(obj);
            } finally {
                super.unlockThis();
            }
        }
    }
    
    private static class SharePVStringArrayImpl extends AbstractSharePVArray implements PVStringArray
    {
        private PVStringArray pvShare;
        
        private SharePVStringArrayImpl(PVStructure parent,PVStringArray pvShare)
        {
            super(parent,pvShare);
            this.pvShare = pvShare;
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVStringArray#get(int, int, org.epics.pvData.pv.StringArrayData)
         */
        @Override
        public int get(int offset, int len, StringArrayData data) {
            super.lockShare();
            try {
                return pvShare.get(offset, len, data);
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVStringArray#put(int, int, String[], int)
         */
        @Override
        public int put(int offset, int length, String[] from, int fromOffset) {
            super.lockShare();
            try {
                PVRecord pvShareRecord = pvShare.getPVRecord();
                if(pvShareRecord!=null) pvShareRecord.beginGroupPut();
                int number = pvShare.put(offset, length, from, fromOffset);
                if(pvShareRecord!=null) pvShareRecord.endGroupPut();
                return number;
            } finally {
                super.unlockShare();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVStringArray#shareData(java.lang.String[])
         */
        @Override
        public void shareData(String[] from) {
            pvShare.shareData(from);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            super.lockThis();
            try {
                return pvShare.equals(obj);
            } finally {
                super.unlockThis();
            }
        }
    }
}
