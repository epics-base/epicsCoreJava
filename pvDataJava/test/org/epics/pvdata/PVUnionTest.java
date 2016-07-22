/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata;

import junit.framework.TestCase;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVTimeStampFactory;
import org.epics.pvdata.property.TimeStamp;
import org.epics.pvdata.property.TimeStampFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.PVUnionArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardField;



/**
 * JUnit test for pvAccess.
 * It also provides examples of how to use the pvAccess interfaces.
 * @author mrk
 *
 */
public class PVUnionTest extends TestCase {
    
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static final StandardField standardField = StandardFieldFactory.getStandardField();
	
    public static void testPVUnion() {
        int numunion = 3;
        String[] uname = new String[numunion];
        Field[]  ufield = new Field[numunion];
        uname[0] = "doubleValue";
        uname[1] = "intValue";
        uname[2] = "timeStamp";
        ufield[0] = fieldCreate.createScalar(ScalarType.pvDouble);
        ufield[1] = fieldCreate.createScalar(ScalarType.pvInt);
        ufield[2] = standardField.timeStamp();
        int num = 3;
        String[] names = new String[num];
        Field[] fields = new Field[num];
        names[0] = "value";
        names[1] = "alarm";
        names[2] = "timeStamp";
        fields[0] = fieldCreate.createUnion(uname, ufield);
        fields[1] = standardField.alarm();
        fields[2] = standardField.timeStamp();
        PVStructure pvStructure = pvDataCreate.createPVStructure(fieldCreate.createStructure(names, fields));
        PVUnion pvValue = pvStructure.getSubField(PVUnion.class,"value");
        PVStructure pvTime = pvValue.select(PVStructure.class, "timeStamp");
        TimeStamp timeStamp = TimeStampFactory.create();
        PVTimeStamp pvTimeStamp = PVTimeStampFactory.create();
        timeStamp.getCurrentTime();
        pvTimeStamp.attach(pvTime);
        pvTimeStamp.set(timeStamp);
        assertTrue(
            pvTime.getSubField(PVLong.class, "secondsPastEpoch")
            ==
            pvValue.get(PVStructure.class).getSubField(PVLong.class, "secondsPastEpoch")
        );
System.out.println(pvStructure);
        PVDouble pvDouble = pvValue.select(PVDouble.class, "doubleValue");
        pvDouble.put(1e5);
        assertTrue(pvDouble.get()== pvValue.get(PVDouble.class).get());
System.out.println(pvStructure);
        PVInt pvInt = pvValue.select(PVInt.class, "intValue");
        pvInt.put(15);
        assertTrue(pvInt.get()== pvValue.get(PVInt.class).get());
System.out.println(pvStructure);
    }
    
    public static void testPVUnionArray() {
        int numunion = 3;
        String[] uname = new String[numunion];
        Field[]  ufield = new Field[numunion];
        uname[0] = "doubleValue";
        uname[1] = "intValue";
        uname[2] = "timeStamp";
        ufield[0] = fieldCreate.createScalar(ScalarType.pvDouble);
        ufield[1] = fieldCreate.createScalar(ScalarType.pvInt);
        ufield[2] = standardField.timeStamp();
        int num = 3;
        String[] names = new String[num];
        Field[] fields = new Field[num];
        names[0] = "value";
        names[1] = "alarm";
        names[2] = "timeStamp";
        fields[0] = fieldCreate.createUnionArray(fieldCreate.createUnion(uname, ufield));
        fields[1] = standardField.alarm();
        fields[2] = standardField.timeStamp();
        PVStructure pvStructure = pvDataCreate.createPVStructure(fieldCreate.createStructure(names, fields));
        PVUnionArray pvValue = pvStructure.getSubField(PVUnionArray.class,"value");
        int len = 3;
        PVUnion[] pvUnions = new PVUnion[len];
        for(int i=0; i<len; ++i) pvUnions[i] = pvDataCreate.createPVUnion(pvValue.getUnionArray().getUnion());
        pvUnions[0].select("doubleValue");
        pvUnions[1].select("intValue");
        pvUnions[2].select("timeStamp");
        PVDouble pvDouble = pvUnions[0].get(PVDouble.class);
        pvDouble.put(1.235);
        PVInt pvInt = pvUnions[1].get(PVInt.class);
        pvInt.put(5);
        PVStructure pvTime = pvUnions[2].get(PVStructure.class);
        TimeStamp timeStamp = TimeStampFactory.create();
        PVTimeStamp pvTimeStamp = PVTimeStampFactory.create();
        timeStamp.getCurrentTime();
        pvTimeStamp.attach(pvTime);
        pvTimeStamp.set(timeStamp);
        pvValue.put(0, len, pvUnions, 0);
        assertTrue(pvDouble.get()==pvUnions[0].get(PVDouble.class).get());
        assertTrue(pvInt.get()==pvUnions[1].get(PVInt.class).get());
        assertTrue(
                pvTime.getSubField(PVLong.class, "secondsPastEpoch")
                ==
                pvUnions[2].get(PVStructure.class).getSubField(PVLong.class, "secondsPastEpoch")
            );
System.out.println(pvStructure);
    }
    
}

