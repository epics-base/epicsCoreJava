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
import org.epics.pvdata.pv.PVArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVShort;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVULongArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.property.*;

/**
 * JUnit test for NTScalarMultiChannel.
 * @author dgh
 *
 */
public class NTScalarMultiChannelTest extends NTTestBase
{
    // Test creation of NTScalarMultiChannelBuilder

    public static void testCreateBuilder()
	{
        NTScalarMultiChannelBuilder builder1 = NTScalarMultiChannel.createBuilder();
		assertNotNull(builder1);

        NTScalarMultiChannelBuilder builder2 = NTScalarMultiChannel.createBuilder();		
		assertNotSame(builder1, builder2);
	}

    // Test NTScalarMultiChannels created with Builder

    public static void testNTScalarMultiChannel_BuilderCreated1()
    {
        testNTScalarMultiChannel_BuilderCreatedImpl(PVDoubleArray.class, ScalarType.pvDouble);
    }

    public static void testNTScalarMultiChannel_BuilderCreated2()
    {
        testNTScalarMultiChannel_BuilderCreatedImpl(PVStringArray.class, ScalarType.pvString,
            new String[] {"timeStamp"});
    }

    public static void testNTScalarMultiChannel_BuilderCreated3()
    {
        testNTScalarMultiChannel_BuilderCreatedImpl(PVIntArray.class, ScalarType.pvInt,
            new String[] {"descriptor", "alarm", "timeStamp"} );
    }

    public static void testNTScalarMultiChannel_BuilderCreated4()
    {
        testNTScalarMultiChannel_BuilderCreatedImpl(PVDoubleArray.class, ScalarType.pvDouble,
            new String[] {"secondsPastEpoch", "nanoseconds", "userTag"} );
    }

    public static void testNTScalarMultiChannel_BuilderCreated5()
    {
        testNTScalarMultiChannel_BuilderCreatedImpl(PVDoubleArray.class, ScalarType.pvDouble,
            new String[] {"severity", "status", "message"} );
    }

    public static void testNTScalarMultiChannel_BuilderCreated6()
    {
        testNTScalarMultiChannel_BuilderCreatedImpl(PVULongArray.class, ScalarType.pvULong,
            new String[] {"descriptor", "timeStamp", "alarm"},
            new String[] {"extra1"}, 
            new Field[]  { fieldCreate.createScalar(ScalarType.pvDouble)});
    }


    // Test is_a

    public static void testNTScalarMultiChannelIs_a()
    {
        Structure s = NTScalarMultiChannel.createBuilder().value(ScalarType.pvDouble).createStructure();
		assertTrue(NTScalarMultiChannel.is_a(s));
    }

    public static void testStructureIs_a()
    {
        testStructureIs_aImpl(NTScalarMultiChannel.URI, true);
        testStructureIs_aImpl("epics:nt/NTScalarMultiChannel:1.0", true);
        testStructureIs_aImpl("epics:nt/NTScalarMultiChannel:1.1", true);
        testStructureIs_aImpl("epics:nt/NTScalarMultiChannel:2.0", false);
        testStructureIs_aImpl("epics:nt/NTScalarMultiChannel", false);
        testStructureIs_aImpl("nt/NTScalarMultiChannel:1.0", false);
        testStructureIs_aImpl("NTScalarMultiChannel:1.0", false);
        testStructureIs_aImpl("NTScalarMultiChannel", false);
        testStructureIs_aImpl("epics:nt/NTScalar:1.0", false);
    }

    // test compatibility - compatible structures

    public static void testStructureIsCompatible1a()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarMultiChannel.URI).
                addArray("value", ScalarType.pvDouble).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                createStructure(),
            true);  
    }

    public static void testStructureIsCompatible1b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarMultiChannel.URI).
                addArray("value", ScalarType.pvDouble).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                add("extra", ScalarType.pvString).
                createStructure(),
            true);
    }

    public static void testStructureIsCompatible1c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarMultiChannel.URI).
                addArray("value", ScalarType.pvUByte).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                add("descriptor", ScalarType.pvString).
                add("alarm", ntField.createAlarm()).
                add("timeStamp", ntField.createTimeStamp()).
                addArray("extra", ScalarType.pvString).
                createStructure(),
            true);
    }

    public static void testStructureIsCompatible1d()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarMultiChannel.URI).
                addArray("value", ScalarType.pvUByte).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                add("descriptor", ScalarType.pvString).
                add("alarm", ntField.createAlarm()).
                add("timeStamp", ntField.createTimeStamp()).
                addArray("extra", ScalarType.pvString).
                createStructure(),
            true);
    }
    public static void testStructureIsCompatible1e()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarMultiChannel.URI).
                addArray("value", ScalarType.pvUByte).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                addArray("severity", ScalarType.pvInt).
                addArray("status", ScalarType.pvInt).
                addArray("message", ScalarType.pvString).
                add("descriptor", ScalarType.pvString).
                add("alarm", ntField.createAlarm()).
                add("timeStamp", ntField.createTimeStamp()).
                add("extra", ScalarType.pvString).
                createStructure(),
            true);
    }

    // test compatibility - incompatible structures

    public static void testStructureIsCompatible2a()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarMultiChannel.URI).
                addArray("Value", ScalarType.pvDouble).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarMultiChannel.URI).
                add("value", ScalarType.pvDouble).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                addArray("ChannelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarMultiChannel.URI).
                addArray("value", ntField.createTimeStamp()).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarMultiChannel.URI).
                addArray("value", ScalarType.pvDouble).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                add("descriptor", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarMultiChannel.URI).
                addArray("value", ScalarType.pvDouble).
                add("alarm", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d3()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarMultiChannel.URI).
                addArray("value", ScalarType.pvDouble).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                add("timeStamp", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d4()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarMultiChannel.URI).
                addArray("value", ScalarType.pvDouble).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                add("secondsPastEpoch", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d5()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarMultiChannel.URI).
                addArray("value", ScalarType.pvDouble).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                add("nanoseconds", ScalarType.pvDouble).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d6()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarMultiChannel.URI).
                addArray("value", ScalarType.pvDouble).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                add("userTag", ScalarType.pvLong).
                createStructure(),
            false);
    }


    public static void testStructureIsCompatible2d7()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarMultiChannel.URI).
                addArray("value", ScalarType.pvDouble).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                add("severity", ScalarType.pvShort).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d8()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarMultiChannel.URI).
                addArray("value", ScalarType.pvDouble).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                add("status", ScalarType.pvShort).
                createStructure(),
            false);
    }
    public static void testStructureIsCompatible2d9()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarMultiChannel.URI).
                addArray("value", ScalarType.pvDouble).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                add("message", ScalarType.pvInt).
                createStructure(),
            false);
    }

    // test wrapping compatible structures

    public static void testWrappedNTScalarMultiChannel1()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTScalarMultiChannel.URI).
            addArray("value", ScalarType.pvDouble).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
            createStructure();

        NTScalarMultiChannel ntscalarMultiChannel = NTScalarMultiChannel.wrap(dataCreate.createPVStructure(s));

        ntScalarMultiChannelChecks(ntscalarMultiChannel, PVDoubleArray.class,
            ScalarType.pvDouble, new String[0],
            new String[0], new Field[0]);

        NTScalarMultiChannel ntscalarMultiChannel2 = NTScalarMultiChannel.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntScalarMultiChannelChecks(ntscalarMultiChannel2, PVDoubleArray.class,
            ScalarType.pvDouble, new String[0],
            new String[0], new Field[0]);
    }


    public static void testWrappedNTScalarMultiChannel2()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTScalarMultiChannel.URI).
            addArray("value", ScalarType.pvString).
            addArray("channelName", ScalarType.pvString).
            add("descriptor", ScalarType.pvString).
            add("alarm", ntField.createAlarm()).
            add("timeStamp", ntField.createTimeStamp()).
            addArray("extra", ScalarType.pvString).
            createStructure();

        String[] standardFields = { "descriptor", "alarm", "timeStamp" };
        String[] extraNames = { "extra" };
        Field[] extraFields = { fieldCreate.createScalarArray(ScalarType.pvString) };

        NTScalarMultiChannel ntscalarMultiChannel = NTScalarMultiChannel.wrap(dataCreate.createPVStructure(s));

        ntScalarMultiChannelChecks(ntscalarMultiChannel, PVStringArray.class,
            ScalarType.pvString, standardFields,
            extraNames,extraFields);

        NTScalarMultiChannel ntscalarMultiChannel2 = NTScalarMultiChannel.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntScalarMultiChannelChecks(ntscalarMultiChannel, PVStringArray.class,
            ScalarType.pvString, standardFields,
            extraNames,extraFields);
    }

    // test isValid()

    public static void testIsValid1()
    {
        testIsValidImpl1(ScalarType.pvInt, new String[0],
            new String[0], new Field[0]);
    }

    public static void testIsValid2()
    {
        testIsValidImpl1(ScalarType.pvUByte,
            new String[] {"severity", "nanoseconds", "timeStamp" },
            new String[0], new Field[0]);
    }

    public static void testIsValid3()
    {
        testIsValidImpl1(ScalarType.pvLong,
            new String[] {"severity", "status", "message", "alarm" },
            new String[0], new Field[0]);
    }

    public static void testIsValid4()
    {
        testIsValidImpl1(ScalarType.pvUShort,
            new String[] {"secondsPastEpoch", "status", "message", "descriptor" },
            new String[0], new Field[0]);
    }

    public static void testIsValid5()
    {
        testIsValidImpl1(ScalarType.pvUShort,
            new String[] {"secondsPastEpoch", "status", "message",
                         "severity", "status", "message",
                         "descriptor", "alarm", "timeStamp" },
            new String[] {"extra"},
            new Field[]{ fieldCreate.createScalar(ScalarType.pvInt) } );
    }

    public static void testIsValidImpl1(ScalarType scalarType,
        String[] standardFields,
        String[] extraNames, Field[] extraFields)
    {
        NTScalarMultiChannel ntscalarMultiChannel = createNTScalarMultiChannel(
            scalarType, standardFields,extraNames,extraFields);

        testIsValidImpl2(ntscalarMultiChannel, new int[] { 0,1,2 }, new int[] {0,1,2,3} );
    }

    public static void testIsValidImpl2(NTScalarMultiChannel ntsmc,
        int[] lengths, int[] badLengths)
    {
        assertTrue(ntsmc.isValid());
        PVArray[] subfields = {
            ntsmc.getValue(),
            ntsmc.getChannelName(),
            ntsmc.getSeverity(),
            ntsmc.getStatus(),
            ntsmc.getMessage(),
            ntsmc.getSecondsPastEpoch(),
            ntsmc.getNanoseconds(),
            ntsmc.getUserTag()
        };

        for (int i : lengths)
        {
            for (PVArray array: subfields)
            {
                 if (array != null) array.setLength(i);
            }
            assertTrue(ntsmc.isValid());

            for (int j : badLengths)
            {
                if (j == i) continue;

                for (PVArray array: subfields)
                {
                    if (array != null)
                    {
                        array.setLength(j);
                        assertFalse(ntsmc.isValid());
                        array.setLength(i);
                    }
                }
            }
            assertTrue(ntsmc.isValid());
        }
    }

    // test attaching timeStamps

    public static void testTimeStamp1()
    {
        NTScalarMultiChannel ntscalarMultiChannel = NTScalarMultiChannel.createBuilder().
            value(ScalarType.pvDouble).
            addTimeStamp().create();

        testAttachTimeStamp(ntscalarMultiChannel, true);
    }
 
    public static void testTimeStamp2()
    {
        NTScalarMultiChannel ntscalarMultiChannel = NTScalarMultiChannel.createBuilder().
            value(ScalarType.pvDouble).create();

        testAttachTimeStamp(ntscalarMultiChannel, false);
    }

    // test attaching alarms

    public static void testAlarm1()
    {
        NTScalarMultiChannel ntscalarMultiChannel = NTScalarMultiChannel.createBuilder().
            value(ScalarType.pvDouble).
            addAlarm().create();

        testAttachAlarm(ntscalarMultiChannel, true);
    }

    public static void testAlarm2()
    {
        NTScalarMultiChannel ntscalarMultiChannel = NTScalarMultiChannel.createBuilder().
            value(ScalarType.pvDouble).create();

        testAttachAlarm(ntscalarMultiChannel, false);
    }


    // test builder resets correctly
    public static void testBuilderResets()
    {
        NTScalarMultiChannelBuilder builder = NTScalarMultiChannel.createBuilder();

        Structure s1 = builder.
            value(ScalarType.pvString).
            createStructure();

        Structure s2 = builder.
            value(ScalarType.pvDouble).
            addDescriptor().
            addTimeStamp().
            addAlarm().
            addSecondsPastEpoch().
            addNanoseconds().
            addUserTag().
            addSeverity().
            addStatus().
            addMessage().
            add("extra", fieldCreate.createScalar(ScalarType.pvInt)).
            createStructure();

        Structure s3 = builder.
            value(ScalarType.pvString).
            createStructure();

        Structure s4 = builder.
            value(ScalarType.pvDouble).
            addDescriptor().
            addTimeStamp().
            addAlarm().
            addSecondsPastEpoch().
            addNanoseconds().
            addUserTag().
            addSeverity().
            addStatus().
            addMessage().
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
        assertEquals(expected, NTScalarMultiChannel.is_a(s));
    }



    public static void testStructureIsCompatibleImpl(Structure s, boolean expected)
    {
        PVStructure pvSt = dataCreate.createPVStructure(s);
        assertEquals(expected, NTScalarMultiChannel.isCompatible(pvSt));
    }

    private static <T extends PVScalarArray>
    void testNTScalarMultiChannel_BuilderCreatedImpl(Class<T> c, ScalarType scalarType)
    {
        testNTScalarMultiChannel_BuilderCreatedImpl(c, scalarType, new String[0], new String[0], new Field[0]);
    }

    private static <T extends PVScalarArray>
    void testNTScalarMultiChannel_BuilderCreatedImpl(Class<T> c, ScalarType scalarType, String[] standardFields)
    {
        testNTScalarMultiChannel_BuilderCreatedImpl(c, scalarType, standardFields, new String[0], new Field[0]);
    }


    private static <T extends PVScalarArray>
    void testNTScalarMultiChannel_BuilderCreatedImpl(Class<T> c, ScalarType scalarType, String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        NTScalarMultiChannel ntscalarMultiChannel = createNTScalarMultiChannel(
            scalarType, standardFields,extraNames,extraFields);

        ntScalarMultiChannelChecks(ntscalarMultiChannel,c,scalarType,
            standardFields,extraNames,extraFields);        
    }


    private static //<T extends PVScalarArray>
    NTScalarMultiChannel createNTScalarMultiChannel(//Class<T> c,
        ScalarType scalarType, String[] standardFields,
        String[] extraNames, Field[] extraFields)
    {
        boolean hasDescriptor  = find("descriptor", standardFields);
        boolean hasTimeStamp   = find("timeStamp", standardFields);
        boolean hasAlarm       = find("alarm", standardFields);
        boolean hasSeconds     = find("secondsPastEpoch", standardFields);
        boolean hasNanoseconds = find("nanoseconds", standardFields);
        boolean hasUserTag     = find("userTag", standardFields);
        boolean hasSeverity    = find("severity", standardFields);
        boolean hasStatus      = find("status", standardFields);
        boolean hasMessage     = find("message", standardFields);

        // Create NTScalarMultiChannel
        NTScalarMultiChannelBuilder builder = NTScalarMultiChannel.createBuilder().value(scalarType);

        if (hasDescriptor) builder.addDescriptor();
        if (hasTimeStamp) builder.addTimeStamp();
        if (hasAlarm) builder.addAlarm();
        if (hasSeconds) builder.addSecondsPastEpoch();
        if (hasNanoseconds) builder.addNanoseconds();
        if (hasUserTag) builder.addUserTag();
        if (hasSeverity) builder.addSeverity();
        if (hasStatus) builder.addStatus();
        if (hasMessage) builder.addMessage();

        for (int i = 0; i < extraNames.length; ++i)
            builder.add(extraNames[i], extraFields[i]);

        return builder.create();
    }

    private static <T extends PVScalarArray>
    void ntScalarMultiChannelChecks(NTScalarMultiChannel ntscalarMultiChannel, Class<T> c,
        ScalarType scalarType, String[] standardFields,
        String[] extraNames, Field[] extraFields)
    {
        // parse optional fields
        boolean hasDescriptor  = find("descriptor", standardFields);
        boolean hasTimeStamp   = find("timeStamp", standardFields);
        boolean hasAlarm       = find("alarm", standardFields);
        boolean hasSeconds     = find("secondsPastEpoch", standardFields);
        boolean hasNanoseconds = find("nanoseconds", standardFields);
        boolean hasUserTag     = find("userTag", standardFields);
        boolean hasSeverity    = find("severity", standardFields);
        boolean hasStatus      = find("status", standardFields);
        boolean hasMessage     = find("message", standardFields);

        // Test value field through NTScalarMultiChannel interface
        PVScalarArray pvValue = ntscalarMultiChannel.getValue();
		assertNotNull(pvValue);
        T pvValue2 = (T)pvValue;
		assertNotNull(pvValue2);
        T pvValue3 = ntscalarMultiChannel.getValue(c);
		assertNotNull(pvValue3);

		// Test optional fields through NTScalarMultiChannel interface
        NTField ntField = NTField.get();

        PVString pvDescriptor = ntscalarMultiChannel.getDescriptor();
        if (hasDescriptor)
        {
            assertNotNull(pvDescriptor);
        }
        else
            assertNull(pvDescriptor);

        PVStructure pvTimeStamp = ntscalarMultiChannel.getTimeStamp();
        if (hasTimeStamp)
        {
            assertNotNull(pvTimeStamp);
            assertTrue(ntField.isTimeStamp(pvTimeStamp.getField()));
        }
        else
            assertNull(pvTimeStamp);

        PVStructure pvAlarm = ntscalarMultiChannel.getAlarm();
        if (hasAlarm)
        {
            assertNotNull(pvAlarm);
            assertTrue(ntField.isAlarm(pvAlarm.getField()));
        }
        else
            assertNull(pvAlarm);

        PVIntArray pvSeverity = ntscalarMultiChannel.getSeverity();
        if (hasSeverity)
        {
            assertNotNull(pvSeverity);
        }
        else
            assertNull(pvSeverity);

        PVIntArray pvStatus = ntscalarMultiChannel.getStatus();
        if (hasStatus)
        {
            assertNotNull(pvStatus);
        }
        else
            assertNull(pvStatus);

        PVStringArray pvMessage = ntscalarMultiChannel.getMessage();
        if (hasMessage)
        {
            assertNotNull(pvMessage);
        }
        else
            assertNull(pvMessage);

        PVLongArray pvSeconds = ntscalarMultiChannel.getSecondsPastEpoch();
        if (hasSeconds)
        {
            assertNotNull(pvSeconds);
        }
        else
            assertNull(pvSeconds);

        PVIntArray pvNanoseconds = ntscalarMultiChannel.getNanoseconds();
        if (hasNanoseconds)
        {
            assertNotNull(pvNanoseconds);
        }
        else
            assertNull(pvNanoseconds);

        PVIntArray pvUserTag = ntscalarMultiChannel.getUserTag();
        if (hasUserTag)
        {
            assertNotNull(pvUserTag);
        }
        else
            assertNull(pvUserTag);

        // Test PVStructure from NTScalarMultiChannel
        PVStructure pvStructure = ntscalarMultiChannel.getPVStructure();
        assertTrue(NTScalarMultiChannel.is_a(pvStructure.getStructure()));
        assertTrue(NTScalarMultiChannel.isCompatible(pvStructure));

        assertSame(pvValue3, pvStructure.getSubField(c, "value"));
        assertSame(pvDescriptor,pvStructure.getSubField(PVString.class, "descriptor"));
        assertSame(pvTimeStamp, pvStructure.getSubField(PVStructure.class, "timeStamp"));
        assertSame(pvAlarm, pvStructure.getSubField(PVStructure.class, "alarm"));

        assertSame(pvSeverity, pvStructure.getSubField(PVIntArray.class, "severity"));

        assertSame(pvStatus, pvStructure.getSubField(PVIntArray.class, "status"));

        assertSame(pvMessage, pvStructure.getSubField(PVStringArray.class, "message"));

        assertSame(pvSeconds, pvStructure.getSubField(PVLongArray.class, "secondsPastEpoch"));

        assertSame(pvNanoseconds, pvStructure.getSubField(PVIntArray.class, "nanoseconds"));

        assertSame(pvUserTag, pvStructure.getSubField(PVIntArray.class, "userTag"));

        for (int i = 0; i < extraNames.length; ++i)
        {
            PVField pvField = pvStructure.getSubField(extraNames[i]);
            assertNotNull(pvField);
            assertSame(pvField.getField(),extraFields[i]);
        }
    }
}

