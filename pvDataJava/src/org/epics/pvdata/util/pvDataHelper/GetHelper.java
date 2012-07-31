package org.epics.pvdata.util.pvDataHelper;

import java.util.Vector;
import org.epics.pvdata.pv.*;

/**
 * GetHelper is a utility class with methods to help copy the contents of PVData types
 * out to more general types. 
 * 
 * @author greg
 *
 */	

public class GetHelper 
{
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
		
		public static Vector<Boolean> getBooleanVector( PVBooleanArray pv )
		{
	        int len = pv.getLength();
	        // double[] storage = new double[len];
	        Vector<Boolean> ret = new Vector<Boolean>();
	        BooleanArrayData data = new BooleanArrayData();
	        int offset = 0;
	        while(offset < len) {
	            int num = pv.get(offset,(len-offset),data);
	            for (int i=0; i<num; i++) ret.add(new Boolean(data.data[offset+i]));
	            // System.arraycopy(data.data,data.offset,storage,offset,num);
	            offset += num;
	        }
	        return ret;
		}
}
