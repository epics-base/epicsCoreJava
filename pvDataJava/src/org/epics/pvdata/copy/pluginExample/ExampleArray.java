/*
 * The License for this software can be found in the file LICENSE that is included with the distribution.
 */

package org.epics.pvdata.copy.pluginExample;

import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.copy.PVCopy;
import org.epics.pvdata.copy.PVCopyFactory;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVShortArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
/**
 * An example that uses the array plugin
 * @author mrk
 * @since 2017.02.23
 */


public class ExampleArray
{
    static final Convert convert = ConvertFactory.getConvert();
    static final CreateRequest createRequest = CreateRequest.create();

    static void exampleCopy(String request,PVStructure master)
    {
    	System.out.println("\nrequest " + request);
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		System.out.println("createRequest failed " + createRequest.getMessage());
    		return;
    	}
    	PVCopy pvCopy = PVCopyFactory.create(master,pvRequest,"");
    	PVStructure copy = pvCopy.createPVStructure();
    	BitSet bitSet = new BitSet(copy.getNumberFields());
    	pvCopy.updateCopySetBitSet(copy,bitSet);
    	System.out.println("bitSet " + bitSet.toString());
    	System.out.println("copy\n" + copy);
    }

    
    public static void main( String[] args )
    {
        System.out.println("_____ExampleArray starting_______");
        
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
        Structure topStructure = fieldCreate.createFieldBuilder().
                addNestedStructure("double").
                addArray("value",ScalarType.pvDouble).
                endNested().
                addNestedStructure("byte").
                addArray("value",ScalarType.pvByte).
                endNested().
                addNestedStructure("byte").
                addArray("value",ScalarType.pvByte).
                endNested().
                addNestedStructure("short").
                addArray("value",ScalarType.pvShort).
                endNested().
                addNestedStructure("int").
                addArray("value",ScalarType.pvInt).
                endNested().
                addNestedStructure("long").
                addArray("value",ScalarType.pvLong).
                endNested().
                addNestedStructure("float").
                addArray("value",ScalarType.pvFloat).
                endNested().
                createStructure();
        PVStructure pvMaster = pvDataCreate.createPVStructure(topStructure);
        int len = 10;
        {
        	PVDoubleArray pvValue = pvMaster.getSubField(PVDoubleArray.class,"double.value");
        	double[] value = new double[len];
        	for(int i=0; i<10; ++i) value[i] = i;
        	convert.fromDoubleArray(pvValue,0,len,value,0);

        }
        {
        	PVByteArray pvValue = pvMaster.getSubField(PVByteArray.class,"byte.value");
        	byte[] value = new byte[len];
        	for(byte i=0; i<10; ++i) value[i] = i;
        	convert.fromByteArray(pvValue,0,len,value,0);

        }
        {
        	PVShortArray pvValue = pvMaster.getSubField(PVShortArray.class,"short.value");
        	short[] value = new short[len];
        	for(short i=0; i<10; ++i) value[i] = i;
        	convert.fromShortArray(pvValue,0,len,value,0);

        }
        {
        	PVIntArray pvValue = pvMaster.getSubField(PVIntArray.class,"int.value");
        	int[] value = new int[len];
        	for(int i=0; i<10; ++i) value[i] = i;
        	convert.fromIntArray(pvValue,0,len,value,0);

        }
        {
        	PVLongArray pvValue = pvMaster.getSubField(PVLongArray.class,"long.value");
        	long[] value = new long[len];
        	for(int i=0; i<10; ++i) value[i] = i;
        	convert.fromLongArray(pvValue,0,len,value,0);

        }
        {
        	PVFloatArray pvValue = pvMaster.getSubField(PVFloatArray.class,"float.value");
        	float[] value = new float[len];
        	for(int i=0; i<10; ++i) value[i] = i;
        	convert.fromFloatArray(pvValue,0,len,value,0);

        }
        System.out.println("pvMaster\n" + pvMaster);
        try {
            exampleCopy("double.value[array=0:-1]",pvMaster);
            exampleCopy("double.value[array=0:2:-1]",pvMaster);
            exampleCopy("double.value[array=4]",pvMaster);
            exampleCopy("double.value[array=-4]",pvMaster);
            
            exampleCopy("byte.value[array=0:-1]",pvMaster);
            exampleCopy("byte.value[array=0:2:-1]",pvMaster);
            exampleCopy("byte.value[array=4]",pvMaster);
            exampleCopy("byte.value[array=-4]",pvMaster);

            exampleCopy("short.value[array=0:-1]",pvMaster);
            exampleCopy("short.value[array=0:2:-1]",pvMaster);
            exampleCopy("short.value[array=4]",pvMaster);
            exampleCopy("short.value[array=-4]",pvMaster);

            exampleCopy("int.value[array=0:-1]",pvMaster);
            exampleCopy("int.value[array=0:2:-1]",pvMaster);
            exampleCopy("int.value[array=4]",pvMaster);
            exampleCopy("int.value[array=-4]",pvMaster);

            exampleCopy("long.value[array=0:-1]",pvMaster);
            exampleCopy("long.value[array=0:2:-1]",pvMaster);
            exampleCopy("long.value[array=4]",pvMaster);
            exampleCopy("long.value[array=-4]",pvMaster);

            exampleCopy("float.value[array=0:-1]",pvMaster);
            exampleCopy("float.value[array=0:2:-1]",pvMaster);
            exampleCopy("float.value[array=4]",pvMaster);
            exampleCopy("float.value[array=-4]",pvMaster);
        }
        catch (Exception e)
        {
            System.err.println("exception " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

}
