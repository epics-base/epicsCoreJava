/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

import org.epics.pvData.factory.AbstractPVArray;
import org.epics.pvData.factory.AbstractPVScalar;
import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.factory.PVDatabaseFactory;
import org.epics.pvData.factory.PVReplaceFactory;
import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.BooleanArrayData;
import org.epics.pvData.pv.ByteArrayData;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.DoubleArrayData;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FloatArrayData;
import org.epics.pvData.pv.IntArrayData;
import org.epics.pvData.pv.LongArrayData;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVBoolean;
import org.epics.pvData.pv.PVBooleanArray;
import org.epics.pvData.pv.PVByte;
import org.epics.pvData.pv.PVByteArray;
import org.epics.pvData.pv.PVDatabase;
import org.epics.pvData.pv.PVDouble;
import org.epics.pvData.pv.PVDoubleArray;
import org.epics.pvData.pv.PVField;
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
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.ShortArrayData;
import org.epics.pvData.pv.StringArrayData;
import org.epics.pvData.pv.Type;
import org.epics.pvData.xml.XMLToPVDatabaseFactory;

/**
 * JUnit test for replacing the default data implementration for a field.
 * @author mrk
 *
 */
public class ReplaceTest extends TestCase {
    private static PVDatabase master = PVDatabaseFactory.getMaster();
    private static Convert convert = ConvertFactory.getConvert();
    /**
     * test replacing the default data implementration for a field.
     */
    public static void testReplaceField() {
        Requester iocRequester = new RequesterForTesting("accessTest");
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/xml/structures.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/example/exampleDB.xml", iocRequester);
        
              
//        System.out.printf("%n%nstructures");
//        PVStructure[] pvStructures = master.getStructures();
//        for(PVStructure pvStructure: pvStructures) {
//          System.out.println(pvStructure.toString());
//        }
        
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/test/analog/analogDB.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/test/powerSupply/powerSupplyDB.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/test/types/allTypesStructure.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/test/types/allTypesRecords.xml", iocRequester);

//        PVRecord pvRecord = master.findRecord("allTypesInitial");
//        if(pvRecord!=null) System.out.println(pvRecord.toString());
//        System.out.printf("%n%nrecords");
//        PVRecord[] pvRecords = master.getRecords();
//        for(PVRecord pvRecord: pvRecords) {
//          System.out.println(pvRecord.toString());
//        }
        
//        System.out.printf("%nrecords%n");
//        Map<String,DBRecord> recordMap = iocdb.getRecordMap();
//        Set<String> keys = recordMap.keySet();
//        for(String key: keys) {
//            DBRecord record = recordMap.get(key);
//            System.out.print(record.toString());
//        }
        
        PVReplaceFactory.replace(master);
        
        System.out.printf("%ntest replaceField%n");
        testReplace("ai","input.value");
        testReplace("ai","value");
        System.out.printf("%n");
        new PVListenerForTesting(master,"ai","input.value");
        new PVListenerForTesting(master,"ai","value");
        testPut("ai","input.value",2.0);
        testPut("ai","value",5.0);
        System.out.printf("%ntest put and listen psSimple%n");
        testReplace("psSimple","power.value");
        testReplace("psSimple","current.value");
        testReplace("psSimple","voltage.value");
        new PVListenerForTesting(master,"psSimple","power.value");
        new PVListenerForTesting(master,"psSimple","current.value");
        new PVListenerForTesting(master,"psSimple","voltage.value");
        testPut("psSimple","current.value",25.0);
        testPut("psSimple","voltage.value",2.0);
        testPut("psSimple","power.value",50.0);
        System.out.printf("%ntest put and listen powerSupplyArray%n");
        testReplace("powerSupplyArray","supply.0.power.value");
        testReplace("powerSupplyArray","supply.0.current.value");
        testReplace("powerSupplyArray","supply.0.voltage.value");
        testReplace("powerSupplyArray","supply.1.power.value");
        testReplace("powerSupplyArray","supply.1.current.value");
        testReplace("powerSupplyArray","supply.1.voltage.value");
        new PVListenerForTesting(master,"powerSupplyArray","supply.0.power.value");
        new PVListenerForTesting(master,"powerSupplyArray","supply.0.current.value");
        new PVListenerForTesting(master,"powerSupplyArray","supply.0.voltage.value");
        new PVListenerForTesting(master,"powerSupplyArray","supply.1.power.value");
        new PVListenerForTesting(master,"powerSupplyArray","supply.1.current.value");
        new PVListenerForTesting(master,"powerSupplyArray","supply.1.voltage.value");
        testPut("powerSupplyArray","supply.0.current.value",25.0);
        testPut("powerSupplyArray","supply.0.voltage.value",2.0);
        testPut("powerSupplyArray","supply.0.power.value",50.0);
        testPut("powerSupplyArray","supply.1.current.value",2.50);
        testPut("powerSupplyArray","supply.1.voltage.value",1.00);
        testPut("powerSupplyArray","supply.1.power.value",2.50);
        System.out.printf("%ntest put and listen allTypes%n");
        testReplace("allTypesInitial","boolean");
        testReplace("allTypesInitial","byte");
        testReplace("allTypesInitial","short");
        testReplace("allTypesInitial","int");
        testReplace("allTypesInitial","long");
        testReplace("allTypesInitial","float");
        testReplace("allTypesInitial","double");
        testReplace("allTypesInitial","string");
        testReplace("allTypesInitial","booleanArray");
        testReplace("allTypesInitial","byteArray");
        testReplace("allTypesInitial","shortArray");
        testReplace("allTypesInitial","intArray");
        testReplace("allTypesInitial","longArray");
        testReplace("allTypesInitial","floatArray");
        testReplace("allTypesInitial","doubleArray");
        testReplace("allTypesInitial","allTypes.boolean");
        testReplace("allTypesInitial","allTypes.byte");
        testReplace("allTypesInitial","allTypes.short");
        testReplace("allTypesInitial","allTypes.int");
        testReplace("allTypesInitial","allTypes.long");
        testReplace("allTypesInitial","allTypes.float");
        testReplace("allTypesInitial","allTypes.double");
        testReplace("allTypesInitial","allTypes.string");
        testReplace("allTypesInitial","allTypes.booleanArray");
        testReplace("allTypesInitial","allTypes.byteArray");
        testReplace("allTypesInitial","allTypes.shortArray");
        testReplace("allTypesInitial","allTypes.intArray");
        testReplace("allTypesInitial","allTypes.longArray");
        testReplace("allTypesInitial","allTypes.floatArray");
        testReplace("allTypesInitial","allTypes.doubleArray");
       
        new PVListenerForTesting(master,"allTypesInitial","boolean");
        new PVListenerForTesting(master,"allTypesInitial","byte");
        new PVListenerForTesting(master,"allTypesInitial","short");
        new PVListenerForTesting(master,"allTypesInitial","int");
        new PVListenerForTesting(master,"allTypesInitial","long");
        new PVListenerForTesting(master,"allTypesInitial","float");
        new PVListenerForTesting(master,"allTypesInitial","double");
        new PVListenerForTesting(master,"allTypesInitial","string");
        new PVListenerForTesting(master,"allTypesInitial","booleanArray");
        new PVListenerForTesting(master,"allTypesInitial","byteArray");
        new PVListenerForTesting(master,"allTypesInitial","shortArray");
        new PVListenerForTesting(master,"allTypesInitial","intArray");
        new PVListenerForTesting(master,"allTypesInitial","longArray");
        new PVListenerForTesting(master,"allTypesInitial","floatArray");
        new PVListenerForTesting(master,"allTypesInitial","doubleArray");
        new PVListenerForTesting(master,"allTypesInitial","allTypes.boolean");
        new PVListenerForTesting(master,"allTypesInitial","allTypes.byte");
        new PVListenerForTesting(master,"allTypesInitial","allTypes.short");
        new PVListenerForTesting(master,"allTypesInitial","allTypes.int");
        new PVListenerForTesting(master,"allTypesInitial","allTypes.long");
        new PVListenerForTesting(master,"allTypesInitial","allTypes.float");
        new PVListenerForTesting(master,"allTypesInitial","allTypes.double");
        new PVListenerForTesting(master,"allTypesInitial","allTypes.string");
        new PVListenerForTesting(master,"allTypesInitial","allTypes.booleanArray");
        new PVListenerForTesting(master,"allTypesInitial","allTypes.byteArray");
        new PVListenerForTesting(master,"allTypesInitial","allTypes.shortArray");
        new PVListenerForTesting(master,"allTypesInitial","allTypes.intArray");
        new PVListenerForTesting(master,"allTypesInitial","allTypes.longArray");
        new PVListenerForTesting(master,"allTypesInitial","allTypes.floatArray");
        new PVListenerForTesting(master,"allTypesInitial","allTypes.doubleArray");
        testPutBoolean("allTypesInitial","boolean",true);
        testPut("allTypesInitial","byte",1.0);
        testPut("allTypesInitial","short",2.0);
        testPut("allTypesInitial","int",3.0);
        testPut("allTypesInitial","long",4.0);
        testPut("allTypesInitial","float",5.0);
        testPut("allTypesInitial","double",6.0);
        testPutString("allTypesInitial","string","test string");
        testPutArray("allTypesInitial","byteArray",1.0,2.0,3.0);
        testPutArray("allTypesInitial","shortArray",1.0,2.0,3.0);
        testPutArray("allTypesInitial","intArray",1.0,2.0,3.0);
        testPutArray("allTypesInitial","longArray",1.0,2.0,3.0);
        testPutArray("allTypesInitial","floatArray",1.0,2.0,3.0);
        testPutArray("allTypesInitial","doubleArray",1.0,2.0,3.0);
        testPutBoolean("allTypesInitial","allTypes.boolean",true);
        testPut("allTypesInitial","allTypes.byte",1.0);
        testPut("allTypesInitial","allTypes.short",2.0);
        testPut("allTypesInitial","allTypes.int",3.0);
        testPut("allTypesInitial","allTypes.long",4.0);
        testPut("allTypesInitial","allTypes.float",5.0);
        testPut("allTypesInitial","allTypes.double",6.0);
        testPutString("allTypesInitial","allTypes.string","test string");
        testPutArray("allTypesInitial","allTypes.byteArray",1.0,2.0,3.0);
        testPutArray("allTypesInitial","allTypes.shortArray",1.0,2.0,3.0);
        testPutArray("allTypesInitial","allTypes.intArray",1.0,2.0,3.0);
        testPutArray("allTypesInitial","allTypes.longArray",1.0,2.0,3.0);
        testPutArray("allTypesInitial","allTypes.floatArray",1.0,2.0,3.0);
        testPutArray("allTypesInitial","allTypes.doubleArray",1.0,2.0,3.0);
    }
    
    static void testPut(String recordName,
        String fieldName,double value)
    {
        PVRecord pvRecord = master.findRecord(recordName);
        if(pvRecord==null) {
            System.out.printf("%nrecord %s not found%n",recordName);
            return;
        }
        PVField pvField = pvRecord.getSubField(fieldName);
        if(pvField==null){
            System.out.printf("%nfield %s not in record %s%n",fieldName,recordName);
            return;
        }
        
        Type type = pvField.getField().getType();
        if(type==Type.scalar) {
            PVScalar pvScalar = (PVScalar)pvField;
            System.out.printf("%ntestPut recordName %s fieldName %s value %f%n",
                recordName,fieldName,value);
            convert.fromDouble(pvScalar,value);
            return;
        }
        if(type!=Type.structure) {
            System.out.printf("%ntestPut recordName %s fieldName %s cant handle%n",
                fieldName,recordName);
            return;
        }
        PVStructure pvStructure = (PVStructure)pvField;
        PVField[] pvFields = pvStructure.getPVFields();
        System.out.printf("%ntestPut begin structure put %s%n",
                recordName + pvField.getFullFieldName());
        pvRecord.beginGroupPut();
        for(PVField pv : pvFields) {
            if(pv.getField().getType()!=Type.scalar) continue;
            PVScalar pvScalar = (PVScalar)pv;
            ScalarType fieldType = pvScalar.getScalar().getScalarType();
            if(fieldType.isNumeric()) {
                System.out.printf("testPut recordName %s fieldName %s value %f%n",
                        recordName,pv.getField().getFieldName(),value);
                    convert.fromDouble(pvScalar,value);
            } else if (fieldType==ScalarType.pvString) {
                String valueString = Double.toString(value);
                System.out.printf("testPut recordName %s fieldName %s value %s%n",
                        recordName,pv.getField().getFieldName(),valueString);
                PVString pvString = (PVString)pv;
                pvString.put(valueString);
            } else {
                continue;
            }
        }
        pvRecord.endGroupPut();
    }
    
    static void testPutArray(String recordName,
            String fieldName,double value1,double value2,double value3)
    {
        PVRecord pvRecord = master.findRecord(recordName);
        if(pvRecord==null) {
            System.out.printf("record %s not found%n",recordName);
            return;
        }
        PVField pvField = pvRecord.getSubField(fieldName);
        if(pvField==null) {
            System.out.printf("field %s not in record %s%n",
                fieldName,recordName);
            return;
        }
        Type type = pvField.getField().getType();
        if(type!=Type.scalarArray) {
            System.out.printf("%ntestPutArray recordName %s fieldName %s no an array%n",
                    fieldName,recordName);
                return;
        }
        PVArray dataArray = (PVArray)pvField;
        ScalarType elementType = dataArray.getArray().getElementType();
        if(elementType.isNumeric()) {
            System.out.printf("%ntestPut recordName %s fieldName %s values %f %f %f",
                recordName,fieldName,value1,value2,value3);
            double[] values = new double[]{value1,value2,value3};
            convert.fromDoubleArray(dataArray,0,3,values,0);
            return;
        } else {
            System.out.printf("%ntestPut recordName %s fieldName %s cant handle%n",
                    fieldName,recordName);
            return;
        }
    }
    
    static void testPutBoolean(String recordName,
            String fieldName,boolean value)
    {
        PVRecord pvRecord = master.findRecord(recordName);
        if(pvRecord==null) {
            System.out.printf("record %s not found%n",recordName);
            return;
        }
        PVField pvField = pvRecord.getSubField(fieldName);
        if(pvField==null) {
            System.out.printf("field %s not in record %s%n",
                fieldName,recordName);
            return;
        }
        if(pvField.getField().getType()!=Type.scalar) return;
        PVScalar pvScalar = (PVScalar)pvField;
        if(pvScalar.getScalar().getScalarType()==ScalarType.pvBoolean) {
            PVBoolean data = (PVBoolean)pvField;
            System.out.printf("%ntestPutBoolean recordName %s fieldName %s value %b",
                recordName,fieldName,value);
            data.put(value);
            return;
        }
    }
    
    static void testPutString(String recordName,
            String fieldName,String value)
    {
        PVRecord pvRecord = master.findRecord(recordName);
        if(pvRecord==null) {
            System.out.printf("record %s not found%n",recordName);
            return;
        }
        PVField pvField = pvRecord.getSubField(fieldName);
        if(pvField==null) {
            System.out.printf("field %s not in record %s%n",
                fieldName,recordName);
            return;
        }
        if(pvField.getField().getType()!=Type.scalar) return;
        PVScalar pvScalar = (PVScalar)pvField;
        if(pvScalar.getScalar().getScalarType()==ScalarType.pvString) {
            PVString data = (PVString)pvField;
            System.out.printf("%ntestPutString recordName %s fieldName %s value %s",
                recordName,fieldName,value);
            data.put(value);
            return;
        }
    }
    
    private static void testReplace(String recordName,
        String fieldName)
    {
        PVRecord pvRecord = master.findRecord(recordName);
        if(pvRecord==null) {
            System.out.printf("record %s not found%n",recordName);
            return;
        }
        PVField oldField = pvRecord.getSubField(fieldName);
        if(oldField==null) {
            System.out.printf("field %s not in record %s%n",
                fieldName,recordName);
            return;
        }
        PVStructure parent = oldField.getParent();
        Field field = oldField.getField();
        Type type = field.getType();
        PVField newField = null;
        if(type==Type.scalar) {
            PVScalar pvScalar = (PVScalar)oldField;
            ScalarType scalarType = pvScalar.getScalar().getScalarType();
            switch(scalarType) {
            case pvBoolean:
                newField = new BooleanData(parent,field);
                break;
            case pvByte:
                newField = new ByteData(parent,field);
                break;
            case pvShort:
                newField = new ShortData(parent,field);
                break;
            case pvInt:
                newField = new IntData(parent,field);
                break;
            case pvLong:
                newField = new LongData(parent,field);
                break;
            case pvFloat:
                newField = new FloatData(parent,field);
                break;
            case pvDouble:
                newField = new DoubleData(parent,field);
                break;
            case pvString:
                newField = new StringData(parent,field);
                break;
            }
        } else if(type==Type.scalarArray) {
            PVArray pvArray = (PVArray)oldField;
            Array array = pvArray.getArray();
            ScalarType elementType = array.getElementType();
            switch(elementType) {
             case pvBoolean:
                  newField = new BooleanArray(parent,
                    array, 0, true);
                  break;
             case pvByte:
                  newField = new ByteArray(parent,
                    array, 0, true);
                  break;
             case pvShort:
                  newField = new ShortArray(parent,
                    array, 0, true);
                  break;
             case pvInt:
                  newField = new IntArray(parent,
                    array, 0, true);
                  break;
             case pvLong:
                  newField = new LongArray(parent,
                    array, 0, true);
                  break;
             case pvFloat:
                  newField = new FloatArray(parent,
                    array, 0, true);
                  break;
             case pvDouble:
                  newField = new DoubleArray(parent,
                    array, 0, true);
                  break;
             case pvString:
                  newField = new StringArray(parent,
                    array, 0, true);
                  break;
            }
        } else {
            System.out.printf("field %s in record %s is a structure%n",
                    fieldName,recordName);
                return;
        }
        oldField.replacePVField(newField);
    }
    
    
    private static class BooleanData extends AbstractPVScalar
        implements PVBoolean
    {
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVBoolean#get()
         */
        public boolean get() {
            System.out.printf("%n    **%s.get**",getField().getType().toString());
            return value;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVBoolean#put(boolean)
         */
        public void put(boolean value) {
            if(super.isMutable()) {
                System.out.printf("%n    **%s.put**",getField().getType().toString());
                this.value = value;
                super.postPut();
                return ;
            }
            throw new IllegalStateException("PVField.isMutable is false");
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return convert.getString(this);
        }
        
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel);
        }

        BooleanData(PVStructure parent,Field field) {
            super(parent,(Scalar)field);
            value = false;
        }
        
        private boolean value;
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
		 */
		public int getSerializationSize() {
			throw new UnsupportedOperationException("not supported");
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
		 */
		public void serialize(ByteBuffer buffer) {
			throw new UnsupportedOperationException("not supported");
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
		 */
		public void deserialize(ByteBuffer buffer) {
			throw new UnsupportedOperationException("not supported");
		}

    }

    private static class ByteData extends AbstractPVScalar implements PVByte {

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVByte#get()
         */
        public byte get() {
            System.out.printf("%n    **%s.get**",getField().getType().toString());
            return value;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVByte#put(byte)
         */
        public void put(byte value) {
            if(super.isMutable()) {
                System.out.printf("%n    **%s.put**",getField().getType().toString());
                this.value = value;
                super.postPut();
                return ;
            }
            throw new IllegalStateException("PVField.isMutable is false");
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return convert.getString(this);
        }
        
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel);
        }

        ByteData(PVStructure parent,Field field) {
            super(parent,(Scalar)field);
            value = 0;
        }
        
        private byte value;
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
		 */
		public int getSerializationSize() {
			throw new UnsupportedOperationException("not supported");
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
		 */
		public void serialize(ByteBuffer buffer) {
			throw new UnsupportedOperationException("not supported");
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
		 */
		public void deserialize(ByteBuffer buffer) {
			throw new UnsupportedOperationException("not supported");
		}

    }

    private static class ShortData extends AbstractPVScalar implements PVShort {

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVShort#get()
         */
        public short get() {
            System.out.printf("%n    **%s.get**",getField().getType().toString());
            return value;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVShort#put(short)
         */
        public void put(short value) {
            if(super.isMutable()) {
                System.out.printf("%n    **%s.put**",getField().getType().toString());
                this.value = value;
                super.postPut();
                return ;
            }
            throw new IllegalStateException("PVField.isMutable is false");
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return convert.getString(this);
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel);
        }

        ShortData(PVStructure parent,Field field) {
            super(parent,(Scalar)field);
            value = 0;
        }
        
        private short value;
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
		 */
		public int getSerializationSize() {
			throw new UnsupportedOperationException("not supported");
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
		 */
		public void serialize(ByteBuffer buffer) {
			throw new UnsupportedOperationException("not supported");
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
		 */
		public void deserialize(ByteBuffer buffer) {
			throw new UnsupportedOperationException("not supported");
		}

    }

    private static class IntData extends AbstractPVScalar implements PVInt {

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVInt#get()
         */
        public int get() {
            System.out.printf("%n    **%s.get**",getField().getType().toString());
            return value;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVInt#put(int)
         */
        public void put(int value) {
            if(super.isMutable()) {
                System.out.printf("%n    **%s.put**",getField().getType().toString());
                this.value = value;
                super.postPut();
                return ;
            }
            throw new IllegalStateException("PVField.isMutable is false");
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return convert.getString(this);
        }
        
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel);
        }

        IntData(PVStructure parent,Field field) {
            super(parent,(Scalar)field);
            value = 0;
        }
        
        private int value;
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
		 */
		public int getSerializationSize() {
			throw new UnsupportedOperationException("not supported");
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
		 */
		public void serialize(ByteBuffer buffer) {
			throw new UnsupportedOperationException("not supported");
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
		 */
		public void deserialize(ByteBuffer buffer) {
			throw new UnsupportedOperationException("not supported");
		}

    }

    private static class LongData extends AbstractPVScalar implements PVLong {

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVLong#get()
         */
        public long get() {
            System.out.printf("%n    **%s.get**",getField().getType().toString());
            return value;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVLong#put(long)
         */
        public void put(long value) {
            if(super.isMutable()) {
                System.out.printf("%n    **%s.put**",getField().getType().toString());
                this.value = value;
                super.postPut();
                return ;
            }
            throw new IllegalStateException("PVField.isMutable is false");
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return convert.getString(this);
        }
        
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel);
        }

        LongData(PVStructure parent,Field field) {
            super(parent,(Scalar)field);
            value = 0;
        }
        
        private long value;
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
		 */
		public int getSerializationSize() {
			throw new UnsupportedOperationException("not supported");
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
		 */
		public void serialize(ByteBuffer buffer) {
			throw new UnsupportedOperationException("not supported");
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
		 */
		public void deserialize(ByteBuffer buffer) {
			throw new UnsupportedOperationException("not supported");
		}

    }

    private static class FloatData extends AbstractPVScalar implements PVFloat {

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVFloat#get()
         */
        public float get() {
            System.out.printf("%n    **%s.get**",getField().getType().toString());
            return value;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVFloat#put(float)
         */
        public void put(float value) {
            if(super.isMutable()) {
                System.out.printf("%n    **%s.put**",getField().getType().toString());
                this.value = value;
                super.postPut();
                return ;
            }
            throw new IllegalStateException("PVField.isMutable is false");
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return convert.getString(this);
        }
        
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel);
        }

        FloatData(PVStructure parent,Field field) {
            super(parent,(Scalar)field);
            value = 0;
        }
        
        private float value;
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
		 */
		public int getSerializationSize() {
			throw new UnsupportedOperationException("not supported");
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
		 */
		public void serialize(ByteBuffer buffer) {
			throw new UnsupportedOperationException("not supported");
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
		 */
		public void deserialize(ByteBuffer buffer) {
			throw new UnsupportedOperationException("not supported");
		}

    }

    private static class DoubleData extends AbstractPVScalar implements PVDouble {

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVDouble#get()
         */
        public double get() {
            System.out.printf("%n    **%s.get**",getField().getType().toString());
            return value;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVDouble#put(double)
         */
        public void put(double value) {
            if(super.isMutable()) {
                System.out.printf("%n    **%s.put**",getField().getType().toString());
                this.value = value;
                super.postPut();
                return ;
            }
            throw new IllegalStateException("PVField.isMutable is false");
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return convert.getString(this);
        }
        
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel);
        }

        DoubleData(PVStructure parent,Field field) {
            super(parent,(Scalar)field);
            value = 0;
        }
        
        private double value;
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
		 */
		public int getSerializationSize() {
			throw new UnsupportedOperationException("not supported");
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
		 */
		public void serialize(ByteBuffer buffer) {
			throw new UnsupportedOperationException("not supported");
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
		 */
		public void deserialize(ByteBuffer buffer) {
			throw new UnsupportedOperationException("not supported");
		}

    }

    private static class StringData extends AbstractPVScalar implements PVString {

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVString#get()
         */
        public String get() {
            System.out.printf("%n    **%s.get**",getField().getType().toString());
            return value;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVString#put(java.lang.String)
         */
        public void put(String value) {
            if(super.isMutable()) {
                System.out.printf("%n    **%s.put**",getField().getType().toString());
                this.value = value;
                super.postPut();
                return ;
            }
            throw new IllegalStateException("PVField.isMutable is false");
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return convert.getString(this);
        }
        
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel);
        }

        StringData(PVStructure parent,Field field) {
            super(parent,(Scalar)field);
            value = null;
        }
        
        private String value;
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
		 */
		public int getSerializationSize() {
			throw new UnsupportedOperationException("not supported");
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
		 */
		public void serialize(ByteBuffer buffer) {
			throw new UnsupportedOperationException("not supported");
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
		 */
		public void deserialize(ByteBuffer buffer) {
			throw new UnsupportedOperationException("not supported");
		}

    }
    
    private static abstract class MyAbstractPVArray extends AbstractPVArray implements PVArray{
        protected int length = 0;
        protected int capacity;
        protected boolean capacityMutable = true;
        /**
         * Constructer that derived classes must call.
         * @param parent The parent interface.
         * @param dbdArrayField The reflection interface for the CDArray data.
         */
        protected MyAbstractPVArray(PVStructure parent,Array array) {
            super(parent,array);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVArray#isCapacityMutable()
         */
        public boolean isCapacityMutable() {
            return capacityMutable;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVArray#getCapacity()
         */
        public int getCapacity() {
            return capacity;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVArray#getLength()
         */
        public int getLength() {
            return length;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVArray#setCapacity(int)
         */
        abstract public void setCapacity(int capacity);
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVArray#setLength(int)
         */
        public void setLength(int len) {
            if(!super.isMutable())
                throw new IllegalStateException("PVField.isMutable is false");
            if(len>capacity) setCapacity(len);
            length = len;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return toString(0);
        }
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
		 */
		public int getSerializationSize() {
			throw new UnsupportedOperationException("not supported");
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
		 */
		public void serialize(ByteBuffer buffer) {
			throw new UnsupportedOperationException("not supported");
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
		 */
		public void deserialize(ByteBuffer buffer) {
			throw new UnsupportedOperationException("not supported");
		}
    }
    private static class BooleanArray extends MyAbstractPVArray implements PVBooleanArray
    {
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVBooleanArray#get(int, int, org.epics.ioc.pv.BooleanArrayData)
         */
        public int get(int offset, int len, BooleanArrayData data) {
            System.out.printf("%n    **%s.get**",getField().getType().toString());
            int n = len;
            if(offset+len > length) n = length;
            data.data = value;
            data.offset = offset;
            return n;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVBooleanArray#share(org.epics.ioc.pv.PVBooleanArray)
         */
        public boolean share(boolean[] value, int length) {
            return false;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVBooleanArray#put(int, int, boolean[], int)
         */
        public int put(int offset, int len, boolean[]from, int fromOffset) {
            System.out.printf("%n    **%s.put**",getField().getType().toString());
            if(!super.isMutable())
                throw new IllegalStateException("PVField.isMutable is false");
            if(offset+len > length) {
                 int newlength = offset + len;
                 if(newlength>capacity) setCapacity(newlength);
                 length = newlength;
            }
            System.arraycopy(from,fromOffset,value,offset,len);
            super.postPut();
            return len;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVArray#setCapacity(int)
         */
        public void setCapacity(int len) {
            if(!capacityMutable)
                throw new IllegalStateException("capacity is immutable");
            if(length>len) length = len;
            boolean[]newarray = new boolean[len];
            if(length>0) System.arraycopy(value,0,newarray,0,length);
            value = newarray;
            capacity = len;
        }
        
        private BooleanArray(PVStructure parent,Array array,
            int capacity,boolean capacityMutable)
        {
            super(parent,array);
            this.capacity = capacity;
            this.capacityMutable = capacityMutable;
            value = new boolean[capacity];
        }
        
        private boolean[] value;
    }

    private static class ByteArray extends MyAbstractPVArray implements PVByteArray
    {
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel);
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVByteArray#get(int, int, org.epics.ioc.pv.ByteArrayData)
         */
        public int get(int offset, int len, ByteArrayData data) {
            System.out.printf("%n    **%s.get**",getField().getType().toString());
            int n = len;
            if(offset+len > length) n = length;
            data.data = value;
            data.offset = offset;
            return n;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVByteArray#share(org.epics.ioc.pv.PVByteArray)
         */
        public boolean share(byte[] value, int length) {
            return false;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVByteArray#put(int, int, byte[], int)
         */
        public int put(int offset, int len, byte[]from, int fromOffset) {
            System.out.printf("%n    **%s.put**",getField().getType().toString());
            if(!super.isMutable())
                throw new IllegalStateException("PVField.isMutable is false");
            if(offset+len > length) {
                 int newlength = offset + len;
                 if(newlength>capacity) setCapacity(newlength);
                 length = newlength;
            }
            System.arraycopy(from,fromOffset,value,offset,len);
            super.postPut();
            return len;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVArray#setCapacity(int)
         */
        public void setCapacity(int len) {
            if(!capacityMutable)
                throw new IllegalStateException("capacity is immutable");
            if(length>len) length = len;
            byte[]newarray = new byte[len];
            if(length>0) System.arraycopy(value,0,newarray,0,length);
            value = newarray;
            capacity = len;
        }

        private ByteArray(PVStructure parent,Array array,
            int capacity,boolean capacityMutable)
        {
            super(parent,array);
            this.capacity = capacity;
            this.capacityMutable = capacityMutable;
            value = new byte[capacity];
        }
        
        private byte[] value;
    }

    private static class ShortArray extends MyAbstractPVArray implements PVShortArray
    {
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVShortArray#get(int, int, org.epics.ioc.pv.ShortArrayData)
         */
        public int get(int offset, int len, ShortArrayData data) {
            System.out.printf("%n    **%s.get**",getField().getType().toString());
            int n = len;
            if(offset+len > length) n = length;
            data.data = value;
            data.offset = offset;
            return n;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVShortArray#share(org.epics.ioc.pv.PVShortArray)
         */
        public boolean share(short[] value, int length) {
            return false;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVShortArray#put(int, int, short[], int)
         */
        public int put(int offset, int len, short[]from, int fromOffset) {
            System.out.printf("%n    **%s.put**",getField().getType().toString());
            if(!super.isMutable())
                throw new IllegalStateException("PVField.isMutable is false");
            if(offset+len > length) {
                 int newlength = offset + len;
                 if(newlength>capacity) setCapacity(newlength);
                 length = newlength;
            }
            System.arraycopy(from,fromOffset,value,offset,len);
            super.postPut();
            return len;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVArray#setCapacity(int)
         */
        public void setCapacity(int len) {
            if(!capacityMutable)
                throw new IllegalStateException("capacity is immutable");
            if(length>len) length = len;
            short[]newarray = new short[len];
            if(length>0) System.arraycopy(value,0,newarray,0,length);
            value = newarray;
            capacity = len;
        }

        private ShortArray(PVStructure parent,Array array,
            int capacity,boolean capacityMutable)
        {
            super(parent,array);
            this.capacity = capacity;
            this.capacityMutable = capacityMutable;
            value = new short[capacity];
        }
        
        private short[] value;
    }

    private static class IntArray extends MyAbstractPVArray implements PVIntArray
    {
       
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVIntArray#get(int, int, org.epics.ioc.pv.IntArrayData)
         */
        public int get(int offset, int len, IntArrayData data) {
            System.out.printf("%n    **%s.get**",getField().getType().toString());
            int n = len;
            if(offset+len > length) n = length;
            data.data = value;
            data.offset = offset;
            return n;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVIntArray#share(org.epics.ioc.pv.PVIntArray)
         */
        public boolean share(int[] value, int length) {
            return false;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVIntArray#put(int, int, int[], int)
         */
        public int put(int offset, int len, int[]from,int fromOffset) {
            System.out.printf("%n    **%s.put**",getField().getType().toString());
            if(!super.isMutable())
                throw new IllegalStateException("PVField.isMutable is false");
            if(offset+len > length) {
                 int newlength = offset + len;
                 if(newlength>capacity) setCapacity(newlength);
                 length = newlength;
            }
            System.arraycopy(from,fromOffset,value,offset,len);
            super.postPut();
            return len;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVArray#setCapacity(int)
         */
        public void setCapacity(int len) {
            if(!capacityMutable)
                throw new IllegalStateException("capacity is immutable");
            if(length>len) length = len;
            int[]newarray = new int[len];
            if(length>0) System.arraycopy(value,0,newarray,0,length);
            value = newarray;
            capacity = len;
        }

        private IntArray(PVStructure parent,Array array,
            int capacity,boolean capacityMutable)
        {
            super(parent,array);
            this.capacity = capacity;
            this.capacityMutable = capacityMutable;
            value = new int[capacity];
        }
        
        private int[] value;
    }

    private static class LongArray extends MyAbstractPVArray implements PVLongArray
    {
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVLongArray#share(org.epics.ioc.pv.PVLongArray)
         */
        public boolean share(long[] value, int length) {
            return false;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVLongArray#get(int, int, org.epics.ioc.pv.LongArrayData)
         */
        public int get(int offset, int len, LongArrayData data) {
            System.out.printf("%n    **%s.get**",getField().getType().toString());
            int n = len;
            if(offset+len > length) n = length;
            data.data = value;
            data.offset = offset;
            return n;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVLongArray#put(int, int, long[], int)
         */
        public int put(int offset, int len, long[]from, int fromOffset) {
            System.out.printf("%n    **%s.put**",getField().getType().toString());
            if(!super.isMutable())
                throw new IllegalStateException("PVField.isMutable is false");
            if(offset+len > length) {
                 int newlength = offset + len;
                 if(newlength>capacity) setCapacity(newlength);
                 length = newlength;
            }
            System.arraycopy(from,fromOffset,value,offset,len);
            super.postPut();
            return len;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVArray#setCapacity(int)
         */
        public void setCapacity(int len) {
            if(!capacityMutable)
                throw new IllegalStateException("capacity is immutable");
            if(length>len) length = len;
            long[]newarray = new long[len];
            if(length>0) System.arraycopy(value,0,newarray,0,length);
            value = newarray;
            capacity = len;
        }

        private LongArray(PVStructure parent,Array array,
            int capacity,boolean capacityMutable)
        {
            super(parent,array);
            this.capacity = capacity;
            this.capacityMutable = capacityMutable;
            value = new long[capacity];
        }
        
        private long[] value;
    }

    private static class FloatArray extends MyAbstractPVArray implements PVFloatArray
    {
        
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVFloatArray#share(org.epics.ioc.pv.PVFloatArray)
         */
        public boolean share(float[] value, int length) {
            return false;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVFloatArray#get(int, int, org.epics.ioc.pv.FloatArrayData)
         */
        public int get(int offset, int len, FloatArrayData data) {
            System.out.printf("%n    **%s.get**",getField().getType().toString());
            int n = len;
            if(offset+len > length) n = length;
            data.data = value;
            data.offset = offset;
            return n;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVFloatArray#put(int, int, float[], int)
         */
        public int put(int offset, int len, float[]from,int fromOffset) {
            System.out.printf("%n    **%s.put**",getField().getType().toString());
            if(!super.isMutable())
                throw new IllegalStateException("PVField.isMutable is false");
            if(offset+len > length) {
                 int newlength = offset + len;
                 if(newlength>capacity) setCapacity(newlength);
                 length = newlength;
            }
            System.arraycopy(from,fromOffset,value,offset,len);
            super.postPut();
            return len;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVArray#setCapacity(int)
         */
        public void setCapacity(int len) {
            if(!capacityMutable)
                throw new IllegalStateException("capacity is immutable");
            if(length>len) length = len;
            float[]newarray = new float[len];
            if(length>0) System.arraycopy(value,0,newarray,0,length);
            value = newarray;
            capacity = len;
        }

        private FloatArray(PVStructure parent,Array array,
            int capacity,boolean capacityMutable)
        {
            super(parent,array);
            this.capacity = capacity;
            this.capacityMutable = capacityMutable;
            value = new float[capacity];
        }
        
        private float[] value;
    }

    private static class DoubleArray extends MyAbstractPVArray implements PVDoubleArray
    {
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVDoubleArray#share(org.epics.ioc.pv.PVDoubleArray)
         */
        public boolean share(double[] value, int length) {
            return false;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel);
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVDoubleArray#get(int, int, org.epics.ioc.pv.DoubleArrayData)
         */
        public int get(int offset, int len, DoubleArrayData data) {
            System.out.printf("%n    **%s.get**",getField().getType().toString());
            int n = len;
            if(offset+len > length) n = length;
            data.data = value;
            data.offset = offset;
            return n;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVDoubleArray#put(int, int, double[], int)
         */
        public int put(int offset, int len, double[]from, int fromOffset) {
            System.out.printf("%n    **%s.put**",getField().getType().toString());
            if(!super.isMutable())
                throw new IllegalStateException("PVField.isMutable is false");
            if(offset+len > length) {
                 int newlength = offset + len;
                 if(newlength>capacity) setCapacity(newlength);
                 length = newlength;
            }
            System.arraycopy(from,fromOffset,value,offset,len);
            super.postPut();
            return len;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVArray#setCapacity(int)
         */
        public void setCapacity(int len) {
            if(!capacityMutable)
                throw new IllegalStateException("capacity is immutable");
            if(length>len) length = len;
            double[]newarray = new double[len];
            if(length>0) System.arraycopy(value,0,newarray,0,length);
            value = newarray;
            capacity = len;
        }

        private DoubleArray(PVStructure parent,Array array,
            int capacity,boolean capacityMutable)
        {
            super(parent,array);
            this.capacity = capacity;
            this.capacityMutable = capacityMutable;
            value = new double[capacity];
        }
        
        private double[] value;
    }

    private static class StringArray extends MyAbstractPVArray implements PVStringArray
    {
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVStringArray#share(org.epics.ioc.pv.PVStringArray)
         */
        public boolean share(String[] value, int length) {
            return false;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVStringArray#get(int, int, org.epics.ioc.pv.StringArrayData)
         */
        public int get(int offset, int len, StringArrayData data) {
            System.out.printf("%n    **%s.get**",getField().getType().toString());
            int n = len;
            if(offset+len > length) n = length;
            data.data = value;
            data.offset = offset;
            return n;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVStringArray#put(int, int, java.lang.String[], int)
         */
        public int put(int offset, int len, String[]from, int fromOffset) {
            System.out.printf("%n    **%s.put**",getField().getType().toString());
            if(!super.isMutable())
                throw new IllegalStateException("PVField.isMutable is false");
            if(offset+len > length) {
                 int newlength = offset + len;
                 if(newlength>capacity) setCapacity(newlength);
                 length = newlength;
            }
            System.arraycopy(from,fromOffset,value,offset,len);
            super.postPut();
            return len;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.PVArray#setCapacity(int)
         */
        public void setCapacity(int len) {
            if(!capacityMutable)
                throw new IllegalStateException("capacity is immutable");
            if(length>len) length = len;
            String[]newarray = new String[len];
            if(length>0) System.arraycopy(value,0,newarray,0,length);
            value = newarray;
            capacity = len;
        }

        private StringArray(PVStructure parent,Array array,
            int capacity,boolean capacityMutable)
        {
            super(parent,array);
            this.capacity = capacity;
            this.capacityMutable = capacityMutable;
            value = new String[capacity];
        }
        
        private String[] value;
    }
}
