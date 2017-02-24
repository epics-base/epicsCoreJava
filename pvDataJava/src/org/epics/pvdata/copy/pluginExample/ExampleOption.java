/*
 * The License for this software can be found in the file LICENSE that is included with the distribution.
 */

package org.epics.pvdata.copy.pluginExample;

import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.copy.PVCopy;
import org.epics.pvdata.copy.PVCopyFactory;
import org.epics.pvdata.copy.PVPluginRegistry;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardField;
import org.epics.pvdata.pv.*;
/**
 * An example that uses options.
 * @author mrk
 * @date 2017.02.23
 */


public class ExampleOption
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
//System.out.println("pvRequest\n" + pvRequest);
    	PVCopy pvCopy = PVCopyFactory.create(master,pvRequest,"");
//System.out.println(pvCopy.dump());
    	PVStructure copy = pvCopy.createPVStructure();
    	BitSet bitSet = new BitSet(copy.getNumberFields());
    	pvCopy.updateCopySetBitSet(copy,bitSet);
    	System.out.println("bitSet " + bitSet.toString());
    	System.out.println("copy\n" + copy);
    }

    
    public static void main( String[] args )
    {
        System.out.println("_____ExamplePlugin starting_______");
        HelloPlugin helloPlugin = new HelloPlugin();
        PVPluginRegistry.registerPlugin("hello",helloPlugin);
        
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        StandardField standardField = StandardFieldFactory.getStandardField();
        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
        Structure topStructure = fieldCreate.createFieldBuilder().
                add("alarm",standardField.alarm()).
                add("timeStamp",standardField.timeStamp()).
                addNestedStructure("double").
                add("value",ScalarType.pvDouble).
                endNested().
                createStructure();
        PVStructure pvMaster = pvDataCreate.createPVStructure(topStructure);
        PVDouble pvValue = pvMaster.getSubField(PVDouble.class,"double.value");
        pvValue.put(5.0);
        PVString pvMessage = pvMaster.getSubField(PVString.class,"alarm.message");
        pvMessage.put("test message");
        System.out.println("pvMaster\n" + pvMaster);
        try {
            exampleCopy("field(alarm,timeStamp[hello=true,timestamp=current],double.value[array=0:-1,hello=true])",pvMaster);
           
        }
        catch (Exception e)
        {
            System.err.println("exception " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

}
