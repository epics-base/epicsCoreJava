/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata;

import junit.framework.TestCase;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVByte;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVShort;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.Requester;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;



/**
 * JUnit test for pvAccess.
 * It also provides examples of how to use the pvAccess interfaces.
 * @author mrk
 *
 */
public class PVCloneTest extends TestCase {
    
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
	private static PVStructure pvChannelToClone;
	private static int nlevels = 4;
	
	static {
	    Field[] fields = new Field[2];
        String[] fieldNames = new String[2];
        fields[0] = fieldCreate.createScalar(ScalarType.pvDouble);
        fields[1] = fieldCreate.createScalarArray(ScalarType.pvDouble);
        fieldNames[0] = "a";
        fieldNames[1] = "b";
        Structure temp = fieldCreate.createStructure(fieldNames, fields);
        fields = new Field[3];
        fieldNames = new String[3];
        fields[0] = fieldCreate.createScalar(ScalarType.pvDouble);
        fields[1] = fieldCreate.createScalarArray(ScalarType.pvDouble);
        fields[2] = fieldCreate.createStructureArray(temp);
        fieldNames[0] = "a";
        fieldNames[1] = "b";
        fieldNames[2] = "c";
        Structure temp1 = fieldCreate.createStructure(fieldNames, fields);
        fields = new Field[4];
        fieldNames = new String[4];
        fields[0] = fieldCreate.createScalar(ScalarType.pvDouble);
        fields[1] = fieldCreate.createScalarArray(ScalarType.pvDouble);
        fields[2] = fieldCreate.createStructureArray(temp);
        fields[3] = fieldCreate.createStructure(temp1);
        fieldNames[0] = "a";
        fieldNames[1] = "b";
        fieldNames[2] = "c";
        fieldNames[3] = "d";
        Structure temp2 = fieldCreate.createStructure(fieldNames, fields);
        fields = new Field[4];
        fieldNames = new String[4];
        fields[0] = fieldCreate.createScalar(ScalarType.pvDouble);
        fields[1] = fieldCreate.createScalarArray(ScalarType.pvDouble);
        fields[2] = fieldCreate.createStructureArray(temp);
        fields[3] = fieldCreate.createStructure(temp2);
        fieldNames[0] = "a";
        fieldNames[1] = "b";
        fieldNames[2] = "c";
        fieldNames[3] = "d";
        Structure temp3 = fieldCreate.createStructure(fieldNames, fields);
        fields = new Field[4];
        fieldNames = new String[4];
        fields[0] = fieldCreate.createScalar(ScalarType.pvDouble);
        fields[1] = fieldCreate.createScalarArray(ScalarType.pvDouble);
        fields[2] = fieldCreate.createStructureArray(temp);
        fields[3] = fieldCreate.createStructure(temp3);
        fieldNames[0] = "a";
        fieldNames[1] = "b";
        fieldNames[2] = "c";
        fieldNames[3] = "d";
        Structure temp4 = fieldCreate.createStructure(fieldNames, fields);
        pvChannelToClone = pvDataCreate.createPVStructure(temp4);
        String dname = "a";
        String daname = "b";
        String saname = "c";
        double[] dvalues = null;
        for(int level=0; level<4; level++) {
            if(level!=0) dname = "d." + dname;
            PVDouble pvDouble = pvChannelToClone.getDoubleField(dname);
            pvDouble.put(level+1);
            int len = level+1;
            dvalues = new double[len];
            for(int i=0; i<len; i++) dvalues[i] = i+1;
            if(level!=0) daname = "d." + daname;
            PVDoubleArray pvDoubleArray = (PVDoubleArray)pvChannelToClone.getScalarArrayField(daname, ScalarType.pvDouble);
            pvDoubleArray.put(0, len, dvalues, 0);
            if(level!=0) saname = "d." + saname;
            PVStructureArray pvStructureArray = pvChannelToClone.getStructureArrayField(saname);
            PVStructure[] pvStructures = new PVStructure[len];
            for(int i=0; i<len; i++) {
                pvStructures[i] = pvDataCreate.createPVStructure(temp);
                pvDouble = pvStructures[i].getDoubleField("a");
                pvDouble.put(level+1);
                dvalues = new double[len];
                for(int j=0; j<len; j++) dvalues[i] = i+1;
                pvDoubleArray = (PVDoubleArray)pvStructures[i].getScalarArrayField("b", ScalarType.pvDouble);
                pvDoubleArray.put(0, len, dvalues, 0);
                
            }
            pvStructureArray.put(0, len, pvStructures, 0);
        }
	}
	
	private static void  doit() {
//System.out.println(pvChannelToClone);
        PVStructure clone = pvDataCreate.createPVStructure(pvChannelToClone);
//System.out.println(clone);
        String name = "a";
        for(int level=0; level<nlevels-1; level ++) {
            if(level!=0) name = "d." + name;
            PVDouble pvOrig = pvChannelToClone.getDoubleField(name);
            PVDouble pvClone = clone.getDoubleField(name);
            pvClone.put(1000.0);
//System.out.println("orig " + pvOrig + " clone " + pvClone);
            assertTrue(pvOrig.get()!=pvClone.get());
        }
        name = "";
        PVStructure pvs = clone;
        for(int level = 0; level<nlevels; level ++) {
            if(level>0) {
                if(level>1) name += ".";
                name += "d";
                pvs = clone.getStructureField(name);
            }
            PVByte pvField = (PVByte)pvDataCreate.createPVScalar(ScalarType.pvByte);
            pvField.put((byte)level);
            pvs.appendPVField("e", pvField);
        }
//System.out.println(clone);
        name = "e";
        for(int level=0; level<nlevels; level ++) {
             if(level!=0) name = "d." + name;
             PVByte pvByte = clone.getByteField(name);
             byte value = pvByte.get();
             assertTrue(value==(byte)level);
        }
        pvs = clone;
        name = "";
        for(int level = 0; level<nlevels; level ++) {
            if(level>0) {
                if(level>1) name += ".";
                name += "d";
                pvs = clone.getStructureField(name);
            }
            PVField[] pvFields = new PVField[2];
            String[] names = new String[2];
            pvFields[0] = pvDataCreate.createPVScalar(ScalarType.pvShort);
            pvFields[1] = pvDataCreate.createPVScalar(ScalarType.pvInt);
            names[0] = "f";
            names[1] = "g";
            PVShort pvShort = (PVShort)pvFields[0];
            pvShort.put((short)level);
            PVInt pvInt = (PVInt)pvFields[1];
            pvInt.put((int)level);        
            pvs.appendPVFields(names, pvFields);
        }
        name = "f";
        for(int level=0; level<nlevels; level ++) {
             if(level!=0) name = "d." + name;
             PVShort pvShort = clone.getShortField(name);
             short value = pvShort.get();
             assertTrue(value==(short)level);
//StringBuilder builder = new StringBuilder();
//convert.getFullFieldName(builder, pvShort);
//System.out.printf("fullName %s value %d%n",builder.toString(),(int)value);
        }
        name = "g";
        for(int level=0; level<nlevels; level ++) {
             if(level!=0) name = "d." + name;
             PVInt pvInt = clone.getIntField(name);
             int value = pvInt.get();
             assertTrue(value==level);
//StringBuilder builder = new StringBuilder();
//convert.getFullFieldName(builder, pvInt);
//System.out.printf("fullName %s value %d%n",builder.toString(),value);
        }
//System.out.println(clone);
        pvs = clone;
        name = "";
        for(int level = 0; level<nlevels; level ++) {
            if(level>0) {
                if(level>1) name += ".";
                name += "d";
                pvs = clone.getStructureField(name);
            }
            PVField pvField = pvs.getSubField("f");
            pvField.renameField("ff");
        }
        name = "ff";
        for(int level=0; level<nlevels; level ++) {
             if(level!=0) name = "d." + name;
             PVShort pvShort = clone.getShortField(name);
             short value = pvShort.get();
             assertTrue(value==(short)level);
//StringBuilder builder = new StringBuilder();
//convert.getFullFieldName(builder, pvShort);
//System.out.printf("fullName %s value %d%n",builder.toString(),(int)value);
        }
//System.out.println(clone);
        name = "";
        pvs = clone;
        for(int level = 0; level<nlevels; level ++) {
            if(level>0) {
                if(level>1) name += ".";
                name += "d";
                pvs = clone.getStructureField(name);
            }
            PVField pvField = pvs.getSubField("a");
            PVString newPVField = (PVString)pvDataCreate.createPVScalar(ScalarType.pvString);
            newPVField.put("string" + level);
            pvs.replacePVField(pvField, newPVField);
            pvField = pvs.getSubField("b");
            PVStringArray xxx = (PVStringArray)pvDataCreate.createPVScalarArray(ScalarType.pvString);
            String[] values = new String[2];
            values[0] = "string" + level;
            values[1] = "anotherstring" + level;
            xxx.put(0, 0, values, 0);
            pvs.replacePVField(pvField, xxx);
        }
//System.out.println(clone);
	}
    /**
     * test testPVClone
     */
    public static void testPVClone() {
        String origPVStructure = pvChannelToClone.toString();
        String origStructure = pvChannelToClone.getStructure().toString();
        doit();
        String nowPVStructure = pvChannelToClone.toString();
        String nowStructure = pvChannelToClone.getStructure().toString();
//System.out.println(origPVStructure);
//System.out.println(origStructure);
//System.out.println(nowPVStructure);
//System.out.println(nowPVStructure);
        assertTrue(nowPVStructure.equals(origPVStructure));
        assertTrue(nowStructure.equals(origStructure));
    }
    
    public static void testRequester() {
        PVField pvField = pvChannelToClone.getSubField("d.d.b");
        pvField.message("this is a test", MessageType.info);
        Requester requester = new RequesterImpl();
        pvChannelToClone.setRequester(requester);
//System.out.println(pvChannelToClone);
        pvField.message("this is a test", MessageType.info);
    }
    
    private static class RequesterImpl implements Requester {
        @Override
        public String getRequesterName() {
            return "pvCopyTest";
        }
        @Override
        public void message(String message, MessageType messageType) {
            System.out.printf("pvCopyTest %s messageType %s%n",message,messageType.name());
            
        }
    }
}

