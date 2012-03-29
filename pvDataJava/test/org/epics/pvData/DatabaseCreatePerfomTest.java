/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData;

import junit.framework.TestCase;

import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.Type;



/**
 * JUnit test for pvAccess.
 * It also provides examples of how to use the pvAccess interfaces.
 * @author mrk
 *
 */
public class DatabaseCreatePerfomTest extends TestCase {
    
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static Structure propertyStructure;
	private static PVStructure pvChannelToClone;
	private static final int numFields = 500;
	private static final int numProperties = 10;
	
	
	static {
		Field[] propertyFields = new Field[2];
		propertyFields[0] = fieldCreate.createScalar( ScalarType.pvString);
		propertyFields[1] = fieldCreate.createScalar(ScalarType.pvString);
		String[] propertyFieldNames = {"value","owner"};
		propertyStructure = fieldCreate.createStructure(propertyFieldNames, propertyFields);
		Field[] fields = new Field[2];
		fields[0] = fieldCreate.createScalar( ScalarType.pvString);
		fields[1] = fieldCreate.createStructure(new String[0], new Field[0]);
		String[] fieldNames = {"name","properties"};
		Structure structure = fieldCreate.createStructure(fieldNames, fields);
		pvChannelToClone = pvDataCreate.createPVStructure(null,structure);
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
        PVStructure pvStructure = pvDataCreate.createPVStructure(
            null,fieldCreate.createStructure(new String[0], new Field[0]));
//System.out.println(pvStructure);
        appendFields(pvStructure,fieldNames,propertyNames,propertyValues);
        // Make it compute offsets
        pvStructure.getFieldOffset();
        long endTime = System.currentTimeMillis();
        double elapsedTime = endTime-startTime;
        elapsedTime /= 1000.0;
        System.out.println("appendFields " + elapsedTime + " seconds");
//System.out.println(pvStructure);
        startTime = System.currentTimeMillis();
        pvStructure = pvDataCreate.createPVStructure(
            null,fieldCreate.createStructure(new String[0], new Field[0]));
        appendField(pvStructure,fieldNames,propertyNames,propertyValues);
        // Make it compute offsets
        pvStructure.getFieldOffset();
        endTime = System.currentTimeMillis();
        elapsedTime = endTime-startTime;
        elapsedTime /= 1000.0;
        System.out.println("appendField " + elapsedTime + " seconds");
//System.out.println(pvStructure);
        startTime = System.currentTimeMillis();
        PVStructure[] pvStructures = new PVStructure[numFields];
        for(int i=0;i<numFields; i++) {
            pvStructures[i] = pvDataCreate.createPVStructure(null,pvChannelToClone);
            PVString pvName = pvStructures[i].getStringField("name");
            //pvName.put("pvName" + chanName);
            pvName.put(fieldNames[i]);
            PVStructure pvProperties = pvStructures[i].getStructureField("properties");
            PVStructure[] pvProps = new PVStructure[propertyNames.length];
            for(int j=0; j<propertyValues.length; j++) {
                pvProps[j] = appendPropertys(null, propertyNames[j],propertyValues[j],"irmis");
            }
            pvProperties.appendPVFields(propertyNames,pvProps);
        }
        pvStructure = pvDataCreate.createPVStructure(null,fieldNames,(PVField[])pvStructures);
        pvStructure.getFieldOffset();
        endTime = System.currentTimeMillis();
        elapsedTime = endTime-startTime;
        elapsedTime /= 1000.0;
        System.out.println("createSubFields first " + elapsedTime + " seconds");
        PVField[] pvFields = pvStructure.getPVFields();
        for(int i=0;i<pvFields.length; i++) {
            PVField pvField = pvFields[i];
            checkParent(pvField);
           
        }
//System.out.println(pvStructure);        
    }
    
    private static void checkParent(PVField pvField) {
        assert(pvField.getParent()!=null);
        if(pvField.getField().getType()==Type.structure) {
            PVStructure subStruct = (PVStructure)pvField;
            PVField[] subFields = subStruct.getPVFields();
            for(int i=0; i<subFields.length; i++) {
                checkParent(subFields[i]);   
            }
        }
    }
        
    private static void appendFields(PVStructure pvStructure, String[] fieldNames,String[] propertyNames,String[]propertyValues){
			PVStructure[] pvChannels = new PVStructure[fieldNames.length];
			for(int i=0; i<fieldNames.length; i++) {
				String fieldName = fieldNames[i];
				PVStructure pvChannel = pvChannels[i] = pvDataCreate.createPVStructure(pvStructure, pvChannelToClone);
				PVString pvName = pvChannel.getStringField("name");
				//pvName.put("pvName" + chanName);
				pvName.put(fieldName);
				PVStructure pvProperties = pvChannel.getStructureField("properties");
				PVStructure[] pvProps = new PVStructure[propertyNames.length];
				for(int j=0; j<propertyValues.length; j++) {
					pvProps[j] =appendPropertys(pvProperties, propertyNames[j],propertyValues[j],"irmis");
				}
				pvProperties.appendPVFields(propertyNames,pvProps);
			}
			pvStructure.appendPVFields(fieldNames,pvChannels);
	}
	
	private static PVStructure appendPropertys(PVStructure pvProperties,String name,String value,String owner) {
		PVStructure pvStructure = pvDataCreate.createPVStructure(pvProperties, propertyStructure);
		PVString pvValue = pvStructure.getStringField("value");
		PVString pvOwner = pvStructure.getStringField("owner");
		pvValue.put(value);
		pvOwner.put(owner);
		return pvStructure;
	}
	
	private static void appendField(PVStructure pvStructure, String[] fieldNames,String[] propertyNames,String[]propertyValues){
		int numberFields = fieldNames.length;
		for(int i=0; i<numberFields; i++) {
		    String fieldName = fieldNames[i];
			PVStructure pvChannel = pvDataCreate.createPVStructure(pvStructure, pvChannelToClone);
			PVString pvName = pvChannel.getStringField("name");
			//pvName.put("pvName" + chanName);
			pvName.put(fieldName);
			PVStructure pvProperties = pvChannel.getStructureField("properties");
			for(int j=0; j<propertyValues.length; j++) {
			    appendProperty(pvProperties, propertyNames[j],propertyValues[j],"irmis");
			}
			pvStructure.appendPVField(fieldName,pvChannel);
		}
	}

	private static void appendProperty(PVStructure pvProperties,String name,String value,String owner) {
		PVStructure pvStructure = pvDataCreate.createPVStructure(pvProperties, propertyStructure);
		PVString pvValue = pvStructure.getStringField("value");
		PVString pvOwner = pvStructure.getStringField("owner");
		pvValue.put(value);
		pvOwner.put(owner);
		pvProperties.appendPVField(name,pvStructure);
	}
}

