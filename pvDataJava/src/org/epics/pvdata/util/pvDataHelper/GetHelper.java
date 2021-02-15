/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.util.pvDataHelper;

import java.util.Vector;

import org.epics.pvdata.pv.BooleanArrayData;
import org.epics.pvdata.pv.ByteArrayData;
import org.epics.pvdata.pv.DoubleArrayData;
import org.epics.pvdata.pv.LongArrayData;
import org.epics.pvdata.pv.PVBooleanArray;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.StringArrayData;

/**
 * GetHelper is a utility class with methods to help application level code
 * copy the contents of PVData array types out to Java vectors.
 *
 * <p> As described in the <a
 * href="http://epics-pvdata.sourceforge.net/docbuild/pvDataJava/tip/documentation/pvDataJava.html">pvDataJava
 * Reference Guide</a> on the <a
 * href="http://epics-pvdata.sourceforge.net/docbuild/pvDataJava/tip/documentation/pvDataJava.html#pvfield_">pvField
 * data interface</a>, a "caller must be prepared to make multiple
 * calls to retrieve or put an entire array." The methods of GetHelper
 * make those multiple calls assuming the caller wishes to get all the
 * values of the whole array. The returned types are Vectors so that
 * the required size need not be known by the caller.  </p> <p>If the
 * calling code will know the size of the returned data a priori, then
 * see also the utilities in {@link org.epics.pvdata.pv.Convert
 * Convert}.
 * </p>
 *
 * @see <a
 * href="http://epics-pvdata.sourceforge.net/docbuild/pvDataJava/tip/documentation/pvDataJava.html#pvfield_">
 * pvDataJava Reference Guide, pvField data interface</a>
 * @see org.epics.pvdata.pv.Convert
 *
 * @author Greg White, SLAC, 2012
 *
 */

public class GetHelper
{
    /**
     * Copy out the entire array of doubles into a Vector of Double.
     *
     * @param pv the PVDoubleArray to copy to
     * @return the Vector containg the values from the specified array
     */
    public static Vector<Double> getDoubleVector( PVDoubleArray pv )
    {
        int len = pv.getLength();
        Vector<Double> ret = new Vector<Double>();
        DoubleArrayData data = new DoubleArrayData();
        int offset = 0;
        while(offset < len) {
            int num = pv.get(offset,(len-offset),data);
            for (int i=0; i<num; i++) ret.add(new Double(data.data[offset+i]));
            offset += num;
        }
        return ret;
    }

    /**
     * Copy out the entire array of longs into a Vector of Long.
     *
     * @param pv the PVLongArray to copy to
     * @return the Vector containg the values from the specified array
     */
    public static Vector<Long> getLongVector( PVLongArray pv )
    {
        int len = pv.getLength();
        Vector<Long> ret = new Vector<Long>();
        LongArrayData data = new LongArrayData();
        int offset = 0;
        while(offset < len) {
            int num = pv.get(offset,(len-offset),data);
            for (int i=0; i<num; i++) ret.add(new Long(data.data[offset+i]));
                offset += num;
        }
        return ret;
    }

    /**
     * Copy out the entire array of bytes into a Vector of Byte.
     *
     * @param pv the PVByteArray to copy to
     * @return the Vector containg the values from the specified array
     */
    public static Vector<Byte> getByteVector( PVByteArray pv )
    {
        int len = pv.getLength();
        Vector<Byte> ret = new Vector<Byte>();
        ByteArrayData data = new ByteArrayData();
        int offset = 0;
        while(offset < len) {
            int num = pv.get(offset,(len-offset),data);
            for (int i=0; i<num; i++) ret.add(new Byte(data.data[offset+i]));
            offset += num;
        }
        return ret;
    }

    /**
     * Copy out the entire array of strings into a Vector of String.
     *
     * @param pv the PVStringArray to copy to
     * @return the Vector containg the values from the specified array
     */
    public static Vector<String> getStringVector( PVStringArray pv )
    {
        int len = pv.getLength();
        // double[] storage = new double[len];
        Vector<String> ret = new Vector<String>();
        StringArrayData data = new StringArrayData();
        int offset = 0;
        while(offset < len) {
            int num = pv.get(offset,(len-offset),data);
            for (int i=0; i<num; i++) {
                if (data.data[offset+i] == null) {
                    ret.add(new String(""));
                } else {
                    ret.add(new String(data.data[offset+i]));
                }
            }
            // System.arraycopy(data.data,data.offset,storage,offset,num);
            offset += num;
        }
        return ret;
    }

    /**
     * Copy out the entire array of booleans into a Vector of Boolean.
     *
     * @param pv the PVBooleanArray to copy to
     * @return the Vector containg the values from the specified array
     */
    public static Vector<Boolean> getBooleanVector( PVBooleanArray pv )
    {
        int len = pv.getLength();
        // double[] storage = new double[len];
        Vector<Boolean> ret = new Vector<Boolean>();
        BooleanArrayData data = new BooleanArrayData();
        int offset = 0;
        while(offset < len) {
            int num = pv.get(offset,(len-offset),data);
            for (int i=0; i<num; i++) ret.add(data.data[offset + i]);
            // System.arraycopy(data.data,data.offset,storage,offset,num);
            offset += num;
        }
        return ret;
    }
}
