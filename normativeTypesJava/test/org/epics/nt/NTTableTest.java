/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import junit.framework.TestCase;

import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldBuilder;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVULong;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.property.*;

/**
 * JUnit test for NTTable.
 * @author dgh
 *
 */
public class NTTableTest extends NTTestBase
{
    // Test creation of NTTableBuilder

    public static void testCreateBuilder()
	{
        NTTableBuilder builder1 = NTTable.createBuilder();
		assertNotNull(builder1);

        NTTableBuilder builder2 = NTTable.createBuilder();		
		assertNotSame(builder1, builder2);
	}

    // Test NTTables created with Builder

    public static void testNTTable_BuilderCreated1()
    {
        testNTTable_BuilderCreatedImpl(new String[0], new ScalarType[0]);
    }

    public static void testNTTable_BuilderCreated2()
    {
        testNTTable_BuilderCreatedImpl(exampleColumnNames, exampleTypes,
           exampleLabels, 
           new String[] {"timeStamp"});
    }

    public static void testNTTable_BuilderCreated3()
    {
        testNTTable_BuilderCreatedImpl(exampleColumnNames, exampleTypes,
           exampleLabels, 
            new String[] {"descriptor", "alarm"} );
    }

    public static void testNTTable_BuilderCreated5()
    {
        testNTTable_BuilderCreatedImpl(exampleColumnNames, exampleTypes,
           exampleLabels, 
            new String[] {"descriptor", "alarm", "timeStamp"},
            new String[] {"extra1"}, 
            new Field[]  { fieldCreate.createScalar(ScalarType.pvDouble)});
    }


    // Test is_a

    public static void testNTTableIs_a()
    {
        Structure s = NTTable.createBuilder().
            addColumn("value",ScalarType.pvDouble).
            createStructure();
		assertTrue(NTTable.is_a(s));
    }

    public static void testStructureIs_a()
    {
        testStructureIs_aImpl(NTTable.URI, true);
        testStructureIs_aImpl("epics:nt/NTTable:1.0", true);
        testStructureIs_aImpl("epics:nt/NTTable:1.1", true);
        testStructureIs_aImpl("epics:nt/NTTable:2.0", false);
        testStructureIs_aImpl("epics:nt/NTTable", false);
        testStructureIs_aImpl("nt/NTTable:1.0", false);
        testStructureIs_aImpl("NTTable:1.0", false);
        testStructureIs_aImpl("NTTable", false);
        testStructureIs_aImpl("epics:nt/NTScalar:1.0", false);
    }

    // test compatibility - compatible structures


    public static void testStructureIsCompatible1a()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTTable.URI).
                addArray("labels", ScalarType.pvString).
                add("value", exampleColumns).
                createStructure(),
            true);
    }

    public static void testStructureIsCompatible1b()
    {

        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTTable.URI).
                addArray("labels", ScalarType.pvString).
                add("value", exampleColumns).
                add("extra", ScalarType.pvString).
                createStructure(),
            true);
    }

    public static void testStructureIsCompatible1c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTTable.URI).
                addArray("labels", ScalarType.pvString).
                add("value", exampleColumns).
                add("descriptor", ScalarType.pvString).
                add("alarm", ntField.createAlarm()).
                add("timeStamp", ntField.createTimeStamp()).
                addArray("extra", ScalarType.pvString).
                createStructure(),
            true);
    }

    // test compatibility - incompatible structures

    public static void testStructureIsCompatible2a()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTTable.URI).
                addArray("labels", ScalarType.pvString).
                add("Value", exampleColumns).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTTable.URI).
                addArray("Labels", ScalarType.pvString).
                add("value", exampleColumns).
                createStructure(),
            false);
    }
    public static void testStructureIsCompatible2c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTTable.URI).
                addArray("labels", ScalarType.pvString).
                addArray("value", ScalarType.pvDouble).
                addArray("severity", ScalarType.pvInt).
                addArray("status", ScalarType.pvInt).
                addArray("secondsPastEpoch", ScalarType.pvLong).
                addArray("nanoseconds", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTTable.URI).
                addArray("labels", ScalarType.pvString).
                addArray("x", ScalarType.pvDouble).
                addArray("y", ScalarType.pvDouble).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2e()
    {
       testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTTable.URI).
                addArray("labels", ScalarType.pvString).
                add("Value", ScalarType.pvDouble).
                createStructure(),
            false);
    }


    public static void testStructureIsCompatible2f()
    {
       testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTTable.URI).
                addArray("Labels", ScalarType.pvString).
                add("value", ScalarType.pvDouble).
                createStructure(),
            false);
    }


    public static void testStructureIsCompatible2d1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTTable.URI).
                addArray("labels", ScalarType.pvString).
                add("value", exampleColumns).
                add("descriptor", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTTable.URI).
                addArray("labels", ScalarType.pvString).
                add("value", exampleColumns).
                add("alarm", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d3()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTTable.URI).
                addArray("labels", ScalarType.pvString).
                add("value", exampleColumns).
                add("timeStamp", ScalarType.pvInt).
                createStructure(),
            false);
    }

    // test wrapping compatible structures

    public static void testWrappedNTTable1()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTTable.URI).
            addArray("labels", ScalarType.pvString).
            add("value", exampleColumns).
            createStructure();

        NTTable nttable = NTTable.wrap(dataCreate.createPVStructure(s));

        ntTableChecks(nttable,
            exampleColumnNames, exampleTypes,  // columns
            null,                              // labels
            new String[0],                     // standard fields
            new String[0], new Field[0]);      // extra fields

        NTTable nttable2 = NTTable.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntTableChecks(nttable2,
            exampleColumnNames, exampleTypes,  // columns
            null,                              // labels
            new String[0],                     // standard fields
            new String[0], new Field[0]);      // extra fields
    }


    public static void testWrappedNTTable2()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTTable.URI).
            addArray("labels", ScalarType.pvString).
            add("value", exampleColumns).
            add("descriptor", ScalarType.pvString).
            add("alarm", ntField.createAlarm()).
            add("timeStamp", ntField.createTimeStamp()).
            add("display", ntField.createDisplay()).
            add("control", ntField.createControl()).
            addArray("extra", ScalarType.pvString).
            createStructure();

        PVStructure pvStructure = dataCreate.createPVStructure(s);

        pvStructure.getSubField(PVStringArray.class, "labels").
            put(0, exampleLabels.length, exampleLabels, 0); 

        String[] standardFields = { "descriptor", "alarm", "control",
            "display", "timeStamp" };
        String[] extraNames = { "extra" };
        Field[] extraFields = { fieldCreate.createScalarArray(ScalarType.pvString) };

        NTTable nttable = NTTable.wrap(dataCreate.createPVStructure(s));
        nttable.getLabels().put(0, exampleLabels.length, exampleLabels, 0);

        ntTableChecks(nttable, exampleColumnNames, exampleTypes,
            exampleLabels,
            standardFields, extraNames,extraFields);

        NTTable nttable2 = NTTable.wrapUnsafe(dataCreate.createPVStructure(s));
        nttable2.getLabels().put(0, exampleLabels.length, exampleLabels, 0);

        ntTableChecks(nttable2, exampleColumnNames, exampleTypes,
            exampleLabels,
            standardFields, extraNames,extraFields);
    }

    // test attaching timeStamps

    public static void testTimeStamp1()
    {
        NTTable nttable = NTTable.createBuilder().
            addColumn("value",ScalarType.pvDouble).
            addTimeStamp().create();

        PVTimeStamp pvTimeStamp = PVTimeStampFactory.create();
        boolean attached = nttable.attachTimeStamp(pvTimeStamp);
        assertTrue(attached);

        TimeStamp timeStamp = TimeStampFactory.create();
        long secondsPastEpoch = 1439251159;
        int nanoseconds = 851129075;
        int userTag = 42; 
        timeStamp.put(secondsPastEpoch,nanoseconds);
        timeStamp.setUserTag(userTag);
        pvTimeStamp.set(timeStamp);

        long secondsPastEpoch2 = nttable.getTimeStamp().
            getSubField(PVLong.class, "secondsPastEpoch").get();
        int nanoseconds2 = nttable.getTimeStamp().
            getSubField(PVInt.class, "nanoseconds").get();
        int userTag2 = nttable.getTimeStamp().
            getSubField(PVInt.class, "userTag").get();

        assertEquals(secondsPastEpoch,secondsPastEpoch2);
        assertEquals(nanoseconds,nanoseconds2);
        assertEquals(userTag,userTag2);
    }
 
    public static void testTimeStamp2()
    {
        NTTable nttable = NTTable.createBuilder().
            add("value", exampleColumns).
            create();

        PVTimeStamp pvTimeStamp = PVTimeStampFactory.create();
        boolean attached = nttable.attachTimeStamp(pvTimeStamp);
        assertFalse(attached);
    }

    // test attaching alarms

    public static void testAlarm1()
    {
        NTTable nttable = NTTable.createBuilder().
            addColumn("value",ScalarType.pvDouble).
            addAlarm().create();

        PVAlarm pvAlarm = PVAlarmFactory.create();
        boolean attached = nttable.attachAlarm(pvAlarm);
        assertTrue(attached);

        Alarm alarm = new Alarm();
        int severity  = 1;
        int status   = 3;
        String message = "STATE_ALARM";

        alarm.setSeverity(AlarmSeverity.getSeverity(severity));
        alarm.setStatus(AlarmStatus.getStatus(status));
        alarm.setMessage(message);

        pvAlarm.set(alarm);

        int severity2  = nttable.getAlarm().
            getSubField(PVInt.class, "severity").get();
        int status2  = nttable.getAlarm().
            getSubField(PVInt.class, "status").get();
        String message2 = nttable.getAlarm().
            getSubField(PVString.class, "message").get();

        assertEquals(severity,severity2);
        assertEquals(status,status2);
        assertEquals(message,message2);
    }

    public static void testAlarm2()
    {
        NTTable nttable = NTTable.createBuilder().
            addColumn("value",ScalarType.pvDouble).
            create();

        PVAlarm pvAlarm = PVAlarmFactory.create();
        boolean attached = nttable.attachAlarm(pvAlarm);
        assertFalse(attached);
    }

    // Test that the builder resets after NTTable creation

    public static void testBuilderResets()
    {
        NTTableBuilder builder = NTTable.createBuilder();

        Structure s1 = builder.
            addColumn("value",ScalarType.pvDouble).
            createStructure();

        Structure s2 = builder.
            addColumn("value",ScalarType.pvDouble).
            addDescriptor().
            addTimeStamp().
            addAlarm().
            add("extra", fieldCreate.createScalar(ScalarType.pvInt)).
            createStructure();

        Structure s3 = builder.
            addColumn("value",ScalarType.pvDouble).
            createStructure();

        Structure s4 = builder.
            addColumn("value",ScalarType.pvDouble).
            addDescriptor().
            addTimeStamp().
            addAlarm().
            add("extra", fieldCreate.createScalar(ScalarType.pvInt)).
            createStructure();

        assertEquals(s1.toString(),s3.toString());
        assertEquals(s2.toString(),s4.toString());
        assertFalse(s1.toString().equals(s2.toString()));
        assertFalse(s3.toString().equals(s4.toString()));
    }

    // Implementations of tests

    public static void testStructureIs_aImpl(String str, boolean expected)
    {
        FieldBuilder builder = fieldCreate.createFieldBuilder();
        Structure s = builder.setId(str).createStructure();
        assertEquals(NTTable.is_a(s), expected);
    }



    public static void testStructureIsCompatibleImpl(Structure s, boolean expected)
    {
        PVStructure pvSt = dataCreate.createPVStructure(s);
        assertEquals(NTTable.isCompatible(pvSt), expected);
    }

    
    private static void testNTTable_BuilderCreatedImpl(String[] columnNames, ScalarType[] scalarTypes)
    {
        testNTTable_BuilderCreatedImpl(columnNames, scalarTypes, null,
            new String[0], new String[0], new Field[0]);
    }

    private static void testNTTable_BuilderCreatedImpl(String[] columnNames, ScalarType[] scalarTypes, String[] labels)
    {
        testNTTable_BuilderCreatedImpl(columnNames, scalarTypes, labels,
            new String[0], new String[0], new Field[0]);
    }

    private static void testNTTable_BuilderCreatedImpl(String[] columnNames, ScalarType[] scalarTypes, String[] labels, String[] standardFields)
    {
        testNTTable_BuilderCreatedImpl(columnNames, scalarTypes, labels,
            standardFields, new String[0], new Field[0]);
    }


    private static void testNTTable_BuilderCreatedImpl(String[] columnNames,
         ScalarType[] scalarTypes, String[] labels, String[] standardFields,
         String[] extraNames, Field[] extraFields)
    {
        NTTable nttable = createNTTable(columnNames, scalarTypes,
            standardFields,extraNames,extraFields);

        ntTableChecks(nttable, columnNames, scalarTypes, labels,
            standardFields,extraNames,extraFields);

        NTTable nttable2 = createNTTable2(columnNames, scalarTypes,
            standardFields,extraNames,extraFields);

        ntTableChecks(nttable2, columnNames, scalarTypes, labels,
            standardFields,extraNames,extraFields);        
    }

    private static NTTable createNTTable(String[] columnNames,
         ScalarType[] scalarTypes, String[] standardFields,
        String[] extraNames, Field[] extraFields)
    {
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);

        // Create NTTable
        NTTableBuilder builder = NTTable.createBuilder();

        for (int i = 0; i < columnNames.length; ++i)
            builder.addColumn(columnNames[i], scalarTypes[i]);

        if (hasDescriptor) builder.addDescriptor();
        if (hasTimeStamp) builder.addTimeStamp();
        if (hasAlarm) builder.addAlarm();

        for (int i = 0; i < extraNames.length; ++i)
            builder.add(extraNames[i], extraFields[i]);

        return builder.create();
    }

    private static NTTable createNTTable2(String[] columnNames,
         ScalarType[] scalarTypes, String[] standardFields,
        String[] extraNames, Field[] extraFields)
    {
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);

        // Create NTTable
        NTTableBuilder builder = NTTable.createBuilder();

        for (int i = 0; i < columnNames.length; ++i)
            builder.addColumns(columnNames, scalarTypes);

        if (hasDescriptor) builder.addDescriptor();
        if (hasTimeStamp) builder.addTimeStamp();
        if (hasAlarm) builder.addAlarm();

        for (int i = 0; i < extraNames.length; ++i)
            builder.add(extraNames[i], extraFields[i]);

        return builder.create();
    }

    private static void ntTableChecks(NTTable nttable,
        String[] columnNames, ScalarType[] scalarTypes,
        String[] labels,
        String[] standardFields,
        String[] extraNames, Field[] extraFields)
    {
        // parse optional fields
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);

        // Test required fields through NTTable interface
        PVStructure pvValue = nttable.getValue();
		assertNotNull(pvValue);

        PVStringArray pvLabels = nttable.getLabels();
		assertNotNull(pvLabels);

        String[] columnNames_rb = nttable.getColumnNames();
System.out.println("Expected");
for (String columnName :columnNames)
    System.out.println(columnName);

System.out.println();

System.out.println("Got");
for (String columnName_rb :columnNames_rb)
    System.out.println(columnName_rb);

    System.out.println();

        org.junit.Assert.assertArrayEquals(columnNames_rb, columnNames);

		// Test optional fields through NTTable interface

        PVString pvDescriptor = nttable.getDescriptor();
        if (hasDescriptor)
        {
            assertNotNull(pvDescriptor);
        }
        else
            assertNull(pvDescriptor);

        PVStructure pvTimeStamp = nttable.getTimeStamp();
        if (hasTimeStamp)
        {
            assertNotNull(pvTimeStamp);
            assertTrue(ntField.isTimeStamp(pvTimeStamp.getField()));
        }
        else
            assertNull(pvTimeStamp);

        PVStructure pvAlarm = nttable.getAlarm();
        if (hasAlarm)
        {
            assertNotNull(pvAlarm);
            assertTrue(ntField.isAlarm(pvAlarm.getField()));
        }
        else
            assertNull(pvAlarm);


        // Test PVStructure from NTTable
        PVStructure pvStructure = nttable.getPVStructure();
        assertTrue(NTTable.is_a(pvStructure.getStructure()));
        assertTrue(NTTable.isCompatible(pvStructure));
/*
        assertSame(pvValue3, pvStructure.getSubField(c, "value"));*/

        for (String columnName : columnNames)
        {
            PVScalarArray pvSA = pvStructure.getSubField(PVScalarArray.class,
                "value." + columnName);
            assertNotNull("value." + columnName, pvSA);
        }

        assertSame(pvDescriptor,pvStructure.getSubField(PVString.class, "descriptor"));
        assertSame(pvTimeStamp, pvStructure.getSubField(PVStructure.class, "timeStamp"));
        assertSame(pvAlarm, pvStructure.getSubField(PVStructure.class, "alarm"));

        for (int i = 0; i < extraNames.length; ++i)
        {
            PVField pvField = pvStructure.getSubField(extraNames[i]);
            assertNotNull(pvField);
            assertSame(pvField.getField(),extraFields[i]);
        }

        if (labels != null)
        {
            
        }
    }

    private static final String[] exampleColumnNames = {
        "value", "severity", "status", "secondsPastEpoch", "nanoseconds" };

    private static final ScalarType[] exampleTypes = {
        ScalarType.pvDouble,
        ScalarType.pvInt,
        ScalarType.pvInt,
        ScalarType.pvLong,
        ScalarType.pvInt };

    private static final String[] exampleLabels = {
        "Value", "Severity", "Status", "Seconds Past Epoch", "Nanoseconds" };

    private static final Structure exampleColumns = fieldCreate.
            createFieldBuilder().
            addArray("value", ScalarType.pvDouble).
            addArray("severity", ScalarType.pvInt).
            addArray("status", ScalarType.pvInt).
            addArray("secondsPastEpoch", ScalarType.pvLong).
            addArray("nanoseconds", ScalarType.pvInt).
            createStructure();
}

