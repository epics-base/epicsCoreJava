/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import junit.framework.TestCase;

import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Structure;



/**
 * JUnit test for pvAccess.
 * It also provides examples of how to use the pvAccess interfaces.
 * @author mrk
 *
 */
public class DatabaseCreatePerfomTest extends TestCase {
    
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static Field[] propertyFields;
	private static PVStructure pvChannelToClone;
	private static final int numFields = 500;
	private static final int numProperties = 10;
	
	
	static {
		propertyFields = new Field[2];
		propertyFields[0] = fieldCreate.createScalar("value", ScalarType.pvString);
		propertyFields[1] = fieldCreate.createScalar("owner", ScalarType.pvString);
		
		Field[] fields = new Field[2];
		fields[0] = fieldCreate.createScalar("name", ScalarType.pvString);
		fields[1] = fieldCreate.createStructure("properties", new Field[0]);
		pvChannelToClone = pvDataCreate.createPVStructure(null, "pvChannelFinder", fields);
	}
    /**
     * test boolean.
     */
    public static void testCreatePerform() {
        String[] fieldNames = new String[numFields];
        for(int i=0; i<numFields; i++) fieldNames[i] = Integer.toString(i);
        String[] propertyNames = new String[numProperties];
        String[] propertyValues = new String[numProperties];
        for(int i=0; i< numProperties; i++) {
        	propertyNames[i] = Integer.toString(i);
        	propertyValues[i] = "property" +Integer.toString(i);
        }
        long startTime = System.currentTimeMillis();
        PVStructure pvStucture = pvDataCreate.createPVStructure(null, "top", new Field[0]);
        appendFields(pvStucture,fieldNames,propertyNames,propertyValues);
        long endTime = System.currentTimeMillis();
        double elapsedTime = endTime-startTime;
        elapsedTime /= 1000.0;
        System.out.println("appendFields " + elapsedTime + " seconds");
//System.out.println(pvStucture);
        startTime = System.currentTimeMillis();
        pvStucture = pvDataCreate.createPVStructure(null, "top", new Field[0]);
        appendField(pvStucture,fieldNames,propertyNames,propertyValues);
        endTime = System.currentTimeMillis();
        elapsedTime = endTime-startTime;
        elapsedTime /= 1000.0;
        System.out.println("appendField " + elapsedTime + " seconds");
//System.out.println(pvStucture);
    }
        
    private static void appendFields(PVStructure pvStructure, String[] fieldNames,String[] propertyNames,String[]propertyValues){
		
			PVStructure[] pvChannels = new PVStructure[fieldNames.length];
			int i=0;
			for (String fieldName: fieldNames) {
				PVStructure pvChannel = pvChannels[i++] = pvDataCreate.createPVStructure(pvStructure, fieldName, pvChannelToClone);
				PVString pvName = pvChannel.getStringField("name");
				//pvName.put("pvName" + chanName);
				pvName.put(fieldName);
				PVStructure pvProperties = pvChannel.getStructureField("properties");
				PVStructure[] pvProps = new PVStructure[propertyNames.length];
				for(int j=0; j<propertyValues.length; j++) {
					pvProps[j] =appendPropertys(pvProperties, propertyNames[j],propertyValues[j],"irmis");
				}
				pvProperties.appendPVFields(pvProps);
			}
			pvStructure.appendPVFields(pvChannels);
	}
	
	private static PVStructure appendPropertys(PVStructure pvProperties,String name,String value,String owner) {
		Structure structure = fieldCreate.createStructure(name, propertyFields);
		PVStructure pvStructure = pvDataCreate.createPVStructure(pvProperties, structure);
		PVString pvValue = pvStructure.getStringField("value");
		PVString pvOwner = pvStructure.getStringField("owner");
		pvValue.put(value);
		pvOwner.put(owner);
		return pvStructure;
	}
	
	private static void appendField(PVStructure pvStructure, String[] fieldNames,String[] propertyNames,String[]propertyValues){
		int i=0;
		for (String fieldName: fieldNames) {
			PVStructure pvChannel = pvDataCreate.createPVStructure(pvStructure, fieldName, pvChannelToClone);
			PVString pvName = pvChannel.getStringField("name");
			//pvName.put("pvName" + chanName);
			pvName.put(fieldName);
			PVStructure pvProperties = pvChannel.getStructureField("properties");
			
			for(int j=0; j<propertyValues.length; j++) {
			    appendProperty(pvProperties, propertyNames[j],propertyValues[j],"irmis");
			}
			pvStructure.appendPVField(pvChannel);
		}
	}

	private static void appendProperty(PVStructure pvProperties,String name,String value,String owner) {
		Structure structure = fieldCreate.createStructure(name, propertyFields);
		PVStructure pvStructure = pvDataCreate.createPVStructure(pvProperties, structure);
		PVString pvValue = pvStructure.getStringField("value");
		PVString pvOwner = pvStructure.getStringField("owner");
		pvValue.put(value);
		pvOwner.put(owner);
		pvProperties.appendPVField(pvStructure);
	}
}

