/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardField;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.StructureArray;
import org.epics.pvdata.pv.Type;

/**
 * Generate introspection object for standard fields.
 * @author mrk
 *
 */
public final class StandardFieldFactory {
	/**
	 * Get the single instance of StandardField.
	 * @return The interface for StandardField.
	 */
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
	    alarmField = fieldCreate.createStructure("alarm_t",alarmFieldNames,fields);
	    fields = new Field[3];
	    String[] timeStampFieldNames = {"secondsPastEpoch","nanoseconds","userTag"};
	    fields[0] = fieldCreate.createScalar(ScalarType.pvLong);
	    fields[1] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[2] = fieldCreate.createScalar(ScalarType.pvInt);
	    timeStampField = fieldCreate.createStructure("time_t",timeStampFieldNames,fields);
	    fields = new Field[5];
	    String[] displayFieldNames = {"limitLow","limitHigh","description","format","units"};
	    fields[0] = fieldCreate.createScalar(ScalarType.pvDouble);
	    fields[1] = fieldCreate.createScalar(ScalarType.pvDouble);
	    fields[2] = fieldCreate.createScalar(ScalarType.pvString);
	    fields[3] = fieldCreate.createScalar(ScalarType.pvString);
	    fields[4] = fieldCreate.createScalar(ScalarType.pvString);
	    displayField = fieldCreate.createStructure("display_t",displayFieldNames,fields);
	    fields = new Field[3];
	    String[] controlFieldNames = {"limitLow","limitHigh","minStep"};
	    fields[0] = fieldCreate.createScalar(ScalarType.pvDouble);
	    fields[1] = fieldCreate.createScalar(ScalarType.pvDouble);
	    fields[2] = fieldCreate.createScalar(ScalarType.pvDouble);
	    controlField = fieldCreate.createStructure("control_t",controlFieldNames,fields);
	    fields = new Field[4];
	    String[] booleanAlarmFieldNames = {"active","falseSeverity","trueSeverity","changeStateSeverity"};
	    fields[0] = fieldCreate.createScalar(ScalarType.pvBoolean);
	    fields[1] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[2] = fieldCreate.createScalar(ScalarType.pvInt);
	    fields[3] = fieldCreate.createScalar(ScalarType.pvInt);
	   booleanAlarmField = fieldCreate.createStructure("valueAlarm_t",booleanAlarmFieldNames,fields);

	    fields = new Field[10];
	    String[] scalarAlarmFieldNames = {
            "active","lowAlarmLimit","lowWarningLimit","highWarningLimit","highAlarmLimit",
            "lowAlarmSeverity","lowWarningSeverity","highWarningSeverity","highAlarmSeverity",
            "hysteresis"
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
	    byteAlarmField = fieldCreate.createStructure("valueAlarm_t",scalarAlarmFieldNames,fields);
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
	    shortAlarmField = fieldCreate.createStructure("valueAlarm_t",scalarAlarmFieldNames,fields);
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
	    intAlarmField = fieldCreate.createStructure("valueAlarm_t",scalarAlarmFieldNames,fields);
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
	    longAlarmField = fieldCreate.createStructure("valueAlarm_t",scalarAlarmFieldNames,fields);
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
	    floatAlarmField = fieldCreate.createStructure("valueAlarm_t",scalarAlarmFieldNames,fields);
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
	    doubleAlarmField = fieldCreate.createStructure("valueAlarm_t",scalarAlarmFieldNames,fields);
	    fields = new Field[3];
	    String[] enumeratedAlarmFieldNames = {"active","stateSeverity","changeStateSeverity"};
	    fields[0] = fieldCreate.createScalar(ScalarType.pvBoolean);
	    fields[1] = fieldCreate.createScalarArray(ScalarType.pvInt);
	    fields[2] = fieldCreate.createScalar(ScalarType.pvInt);
	    enumeratedAlarmField = fieldCreate.createStructure("valueAlarm_t",enumeratedAlarmFieldNames,fields);

	}
	static Structure createProperties(String id,Field field,String properties)
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
	        if(type==Type.scalar || type==Type.scalarArray) {
	           ScalarType scalarType = (type==Type.scalar) ?
	        		   		((Scalar)field).getScalarType() :
	        		   		((ScalarArray)field).getElementType();
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
	    return fieldCreate.createStructure(id,fieldNames,fields);
	}

	private static final class StandardFieldImpl implements StandardField
	{
		StandardFieldImpl(){}

		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardField#scalar(org.epics.pvdata.pv.ScalarType, java.lang.String)
		 */
		public Structure scalar(ScalarType scalarType,String properties)
		{
			Scalar field = fieldCreate.createScalar(scalarType);	// scalar_t
		    return createProperties("epics:nt/NTScalar:1.0",field,properties);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardField#scalarArray(org.epics.pvdata.pv.ScalarType, java.lang.String)
		 */
		public Structure scalarArray(ScalarType elementType, String properties)
		{
			ScalarArray field = fieldCreate.createScalarArray(elementType);	// scalar_t[]
		    return createProperties("epics:nt/NTScalarArray:1.0",field,properties);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardField#structureArray(org.epics.pvdata.pv.Structure, java.lang.String)
		 */
		public Structure structureArray(Structure structure,String properties)
		{
			StructureArray field = fieldCreate.createStructureArray(structure);
		    return createProperties("epics:nt/NTStructureArray:1.0",field,properties);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardField#enumerated()
		 */
		public Structure enumerated()
		{
			Field[] fields = new Field[2];
			String[] fieldNames = {"index","choices"};
		    fields[0] = fieldCreate.createScalar(ScalarType.pvInt);
		    fields[1] = fieldCreate.createScalarArray(ScalarType.pvString);
		    return fieldCreate.createStructure("enum_t",fieldNames,fields);
		    // NOTE: if this method is used to get NTEnum wihtout properties the ID will be wrong!

		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardField#enumerated(java.lang.String)
		 */
		public Structure enumerated(String properties)
		{
		    Structure field = enumerated();	// enum_t
		    return createProperties("epics:nt/NTEnum:1.0",field,properties);

		}

		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardField#alarm()
		 */
		public Structure alarm()
		{
			return alarmField;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardField#timeStamp()
		 */
		public Structure timeStamp()
		{
			return timeStampField;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardField#display()
		 */
		public Structure display()
		{
			return displayField;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardField#control()
		 */
		public Structure control()
		{
			return controlField;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardField#booleanAlarm()
		 */
		public Structure booleanAlarm()
		{
			return booleanAlarmField;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardField#byteAlarm()
		 */
		public Structure byteAlarm()
		{
			return byteAlarmField;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardField#shortAlarm()
		 */
		public Structure shortAlarm()
		{
			return shortAlarmField;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardField#intAlarm()
		 */
		public Structure intAlarm()
		{
			return intAlarmField;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardField#longAlarm()
		 */
		public Structure longAlarm()
		{
			return longAlarmField;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardField#floatAlarm()
		 */
		public Structure floatAlarm()
		{
			return floatAlarmField;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardField#doubleAlarm()
		 */
		public Structure doubleAlarm()
		{
			return doubleAlarmField;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardField#enumeratedAlarm()
		 */
		public Structure enumeratedAlarm()
		{
			return enumeratedAlarmField;
		}
	}
}
