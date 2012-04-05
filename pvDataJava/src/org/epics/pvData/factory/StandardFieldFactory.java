/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarArray;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.StandardField;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.StructureArray;
import org.epics.pvData.pv.Type;

/**
 * Generate introspection object for standard fields.
 * @author mrk
 *
 */
public final class StandardFieldFactory {
	public static synchronized StandardField getStandardField()
	{
		if(impl==null) {
			impl = new StandardFieldImpl();
			initStatic();
		}
		return impl;
	}
	private static StandardFieldImpl impl = null;
	private static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	static private Structure alarmField;
	static private Structure timeStampField;
	static private Structure displayField;
	static private Structure controlField;
	static private Structure booleanAlarmField;
	static private Structure byteAlarmField;
	static private Structure shortAlarmField;
	static private Structure intAlarmField;
	static private Structure longAlarmField;
	static private Structure floatAlarmField;
	static private Structure doubleAlarmField;
	static private Structure enumeratedAlarmField;


	private static void initStatic()
	{
		Field[] fields = new Field[3];
		String[] alarmFieldNames = {"severity","status","message"};
	    fields[0] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[1] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[2] = fieldCreate.createScalar(ScalarType.pvString);
	    alarmField = fieldCreate.createStructure(alarmFieldNames,fields);
	    fields = new Field[3];
	    String[] timeStampFieldNames = {"secondsPastEpoch","nanoSeconds","userTag"};
	    fields[0] = fieldCreate.createScalar(ScalarType.pvLong);
	    fields[1] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[2] = fieldCreate.createScalar(ScalarType.pvInt);
	    timeStampField = fieldCreate.createStructure(timeStampFieldNames,fields);
	    fields = new Field[5];
	    String[] displayFieldNames = {"limitLow","limitHigh","description","format","units"};
	    fields[0] = fieldCreate.createScalar(ScalarType.pvDouble);
	    fields[1] = fieldCreate.createScalar(ScalarType.pvDouble);
	    fields[2] = fieldCreate.createScalar(ScalarType.pvString);
	    fields[3] = fieldCreate.createScalar(ScalarType.pvString);
	    fields[4] = fieldCreate.createScalar(ScalarType.pvString);
	    displayField = fieldCreate.createStructure(displayFieldNames,fields);
	    fields = new Field[3];
	    String[] controlFieldNames = {"limitLow","limitHigh","minStep"};
	    fields[0] = fieldCreate.createScalar(ScalarType.pvDouble);
	    fields[1] = fieldCreate.createScalar(ScalarType.pvDouble);
	    fields[2] = fieldCreate.createScalar(ScalarType.pvDouble);
	    controlField = fieldCreate.createStructure(controlFieldNames,fields);
	    fields = new Field[4];
	    String[] booleanAlarmFieldNames = {"active","falseSeverity","trueSeverity","changeStateSeverity"};
	    fields[0] = fieldCreate.createScalar(ScalarType.pvBoolean);
	    fields[1] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[2] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[3] = fieldCreate.createScalar(ScalarType.pvInt);
	    booleanAlarmField = fieldCreate.createStructure(booleanAlarmFieldNames,fields);
	    
	    fields = new Field[10];
	    String[] scalarAlarmFieldNames = {
            "active","lowAlarmLimit","lowWarningLimit","highWarningLimit","highAlarmLimit",
            "lowAlarmSeverity","lowWarningSeverity","highWarningSeverity","highAlarmSeverity",
            "hystersis"
	    };
	    fields[0] = fieldCreate.createScalar(ScalarType.pvBoolean);
	    fields[1] = fieldCreate.createScalar(ScalarType.pvByte);
	    fields[2] = fieldCreate.createScalar(ScalarType.pvByte);
	    fields[3] = fieldCreate.createScalar(ScalarType.pvByte);
	    fields[4] = fieldCreate.createScalar(ScalarType.pvByte);
	    fields[5] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[6] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[7] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[8] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[9] = fieldCreate.createScalar(ScalarType.pvByte);
	    byteAlarmField = fieldCreate.createStructure(scalarAlarmFieldNames,fields);
	    fields = new Field[10];
	    fields[0] = fieldCreate.createScalar(ScalarType.pvBoolean);
	    fields[1] = fieldCreate.createScalar(ScalarType.pvShort);
	    fields[2] = fieldCreate.createScalar(ScalarType.pvShort);
	    fields[3] = fieldCreate.createScalar(ScalarType.pvShort);
	    fields[4] = fieldCreate.createScalar(ScalarType.pvShort);
	    fields[5] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[6] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[7] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[8] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[9] = fieldCreate.createScalar(ScalarType.pvShort);
	    shortAlarmField = fieldCreate.createStructure(scalarAlarmFieldNames,fields);
	    fields = new Field[10];
	    fields[0] = fieldCreate.createScalar(ScalarType.pvBoolean);
	    fields[1] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[2] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[3] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[4] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[5] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[6] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[7] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[8] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[9] = fieldCreate.createScalar(ScalarType.pvInt);
	    intAlarmField = fieldCreate.createStructure(scalarAlarmFieldNames,fields);
	    fields = new Field[10];
	    fields[0] = fieldCreate.createScalar(ScalarType.pvBoolean);
	    fields[1] = fieldCreate.createScalar(ScalarType.pvLong);
	    fields[2] = fieldCreate.createScalar(ScalarType.pvLong);
	    fields[3] = fieldCreate.createScalar(ScalarType.pvLong);
	    fields[4] = fieldCreate.createScalar(ScalarType.pvLong);
	    fields[5] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[6] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[7] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[8] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[9] = fieldCreate.createScalar(ScalarType.pvLong);
	    longAlarmField = fieldCreate.createStructure(scalarAlarmFieldNames,fields);
	    fields = new Field[10];
	    fields[0] = fieldCreate.createScalar(ScalarType.pvBoolean);
	    fields[1] = fieldCreate.createScalar(ScalarType.pvFloat);
	    fields[2] = fieldCreate.createScalar(ScalarType.pvFloat);
	    fields[3] = fieldCreate.createScalar(ScalarType.pvFloat);
	    fields[4] = fieldCreate.createScalar(ScalarType.pvFloat);
	    fields[5] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[6] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[7] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[8] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[9] = fieldCreate.createScalar(ScalarType.pvFloat);
	    floatAlarmField = fieldCreate.createStructure(scalarAlarmFieldNames,fields);
	    fields = new Field[10];
	    fields[0] = fieldCreate.createScalar(ScalarType.pvBoolean);
	    fields[1] = fieldCreate.createScalar(ScalarType.pvDouble);
	    fields[2] = fieldCreate.createScalar(ScalarType.pvDouble);
	    fields[3] = fieldCreate.createScalar(ScalarType.pvDouble);
	    fields[4] = fieldCreate.createScalar(ScalarType.pvDouble);
	    fields[5] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[6] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[7] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[8] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[9] = fieldCreate.createScalar(ScalarType.pvDouble);
	    doubleAlarmField = fieldCreate.createStructure(scalarAlarmFieldNames,fields);
	    fields = new Field[3];
	    String[] enumeratedAlarmFieldNames = {"active","stateSeverity","changeStateSeverity"};
	    fields[0] = fieldCreate.createScalar(ScalarType.pvBoolean);
	    fields[1] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[2] = fieldCreate.createScalar(ScalarType.pvInt);
	    enumeratedAlarmField = fieldCreate.createStructure(enumeratedAlarmFieldNames,fields);

	}
	static Structure createProperties(Field field,String properties)
	{
	    boolean gotAlarm = false;
	    boolean gotTimeStamp = false;
	    boolean gotDisplay = false;
	    boolean gotControl = false;
	    boolean gotValueAlarm = false;
	    int numProp = 0;
	    if(properties.contains("alarm")) { gotAlarm = true; numProp++; }
	    if(properties.contains("timeStamp")) { gotTimeStamp = true; numProp++; }
	    if(properties.contains("display")) { gotDisplay = true; numProp++; }
	    if(properties.contains("control")) { gotControl = true; numProp++; }
	    if(properties.contains("valueAlarm")) { gotValueAlarm = true; numProp++; }
	    Structure valueAlarm = null;
	    Type type= field.getType();
	    while(gotValueAlarm) {
	        if(type==Type.scalar) {
	           Scalar scalar = (Scalar)(field);
	           ScalarType scalarType = scalar.getScalarType();
	           switch(scalarType) {
	               case pvBoolean: valueAlarm = booleanAlarmField; break;
	               case pvByte: valueAlarm = byteAlarmField; break;
	               case pvShort: valueAlarm = shortAlarmField; break;
	               case pvInt: valueAlarm = intAlarmField; break;
	               case pvLong: valueAlarm = longAlarmField; break;
	               case pvFloat: valueAlarm = floatAlarmField; break;
	               case pvDouble: valueAlarm = doubleAlarmField; break;
	               default:
	            	   throw new IllegalArgumentException("valueAlarm property for illegal type");
	           }
	           break;
	        }
	        if(type==Type.structure) {
	            Structure structure = (Structure)(field);
	            Field[] fields = structure.getFields();
	            String[] fieldNames = structure.getFieldNames();
	            if(fields.length==2) {
	                Field first = fields[0];
	                Field second = fields[1];
	                if(fieldNames[0].equals("index") && fieldNames[1].equals("choices")) {
	                	if(first.getType()==Type.scalar && second.getType()==Type.scalarArray) {
	                        Scalar scalarFirst = (Scalar)(first);
	                        ScalarArray scalarArraySecond = (ScalarArray)(second);
	                        if(scalarFirst.getScalarType()==ScalarType.pvInt
	                        && scalarArraySecond.getElementType()==ScalarType.pvString) {
	                            valueAlarm = enumeratedAlarmField;
	                            break;
	                        }
	                    }
	                }
	            }
	        }
	        throw new IllegalArgumentException("valueAlarm property for illegal type");
	    }
	    int numFields = numProp+1;
	    Field[] fields = new Field[numFields];
	    String[] fieldNames = new String[numFields];
	    int next = 0;
	    fieldNames[0] = "value";
	    fields[next++] = field;
	    if(gotAlarm) {
	    	fieldNames[next] = "alarm";
	    	fields[next++] = alarmField;
	    }
	    if(gotTimeStamp) {
	    	fieldNames[next] = "timeStamp";
	    	fields[next++] = timeStampField;
	    }
	    if(gotDisplay) {
	    	fieldNames[next] = "display";
	    	fields[next++] = displayField;
	    }
	    if(gotControl) {
	    	fieldNames[next] = "control";
	    	fields[next++] = controlField;
	    }
	    if(gotValueAlarm) {
	    	fieldNames[next] = "valueAlarm";
	    	fields[next++] = valueAlarm;
	    }
	    return fieldCreate.createStructure(fieldNames,fields);
	}

	private static final class StandardFieldImpl implements StandardField
	{
		StandardFieldImpl(){}

		@Override
		public Structure scalar(ScalarType scalarType,String properties)
		{
			Scalar field = fieldCreate.createScalar(scalarType);
		    return createProperties(field,properties);
		}
		@Override
		public Structure scalarArray(ScalarType elementType, String properties)
		{
			ScalarArray field = fieldCreate.createScalarArray(elementType);
		    return createProperties(field,properties);
		}
		@Override
		public Structure structureArray(Structure structure,String properties)
		{
			StructureArray field = fieldCreate.createStructureArray(structure);
		    return createProperties(field,properties);
		}
		@Override
		public Structure enumerated()
		{
			Field[] fields = new Field[2];
			String[] fieldNames = {"index","choices"};
		    fields[0] = fieldCreate.createScalar(ScalarType.pvInt);
		    fields[1] = fieldCreate.createScalarArray(ScalarType.pvString);
		    return fieldCreate.createStructure(fieldNames,fields);

		}
		@Override
		public Structure enumerated(String properties)
		{
		    Structure field = enumerated();
		    return createProperties(field,properties);

		}
		
		@Override
		public Structure alarm()
		{
			return alarmField;
		}
		@Override
		public Structure timeStamp()
		{
			return timeStampField;
		}
		@Override
		public Structure display()
		{
			return displayField;
		}
		@Override
		public Structure control()
		{
			return controlField;
		}
		@Override
		public Structure booleanAlarm()
		{
			return booleanAlarmField;
		}
		@Override
		public Structure byteAlarm()
		{
			return byteAlarmField;
		}
		@Override
		public Structure shortAlarm()
		{
			return shortAlarmField;
		}
		@Override
		public Structure intAlarm()
		{
			return intAlarmField;
		}
		@Override
		public Structure longAlarm()
		{
			return longAlarmField;
		}
		@Override
		public Structure floatAlarm()
		{
			return floatAlarmField;
		}
		@Override
		public Structure doubleAlarm()
		{
			return doubleAlarmField;
		}
		@Override
		public Structure enumeratedAlarm()
		{
			return enumeratedAlarmField;
		}
	}
}
