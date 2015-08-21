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
import org.epics.pvdata.pv.Union;
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
import org.epics.pvdata.pv.PVUnionArray;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.property.*;

/**
 * JUnit test for NTMultiChannel.
 * @author dgh
 *
 */
public class NTMultiChannelTest extends NTTestBase
{
    // Test creation of NTMultiChannelBuilder

    public static void testCreateBuilder()
	{
        NTMultiChannelBuilder builder1 = NTMultiChannel.createBuilder();
		assertNotNull(builder1);

        NTMultiChannelBuilder builder2 = NTMultiChannel.createBuilder();		
		assertNotSame(builder1, builder2);
	}

    // Test NTMultiChannels created with Builder

    public static void testNTMultiChannel_BuilderCreated1()
    {
        testNTMultiChannel_BuilderCreatedImpl(variantUnion);
    }

    public static void testNTMultiChannel_BuilderCreated2()
    {
        testNTMultiChannel_BuilderCreatedImpl(variantUnion,
            new String[] {"timeStamp"});
    }

    public static void testNTMultiChannel_BuilderCreated3()
    {
        testNTMultiChannel_BuilderCreatedImpl(variantUnion,
            new String[] {"descriptor", "alarm", "timeStamp"} );
    }

    public static void testNTMultiChannel_BuilderCreated4()
    {
        testNTMultiChannel_BuilderCreatedImpl(variantUnion,
            new String[] {"secondsPastEpoch", "nanoseconds", "userTag"} );
    }

    public static void testNTMultiChannel_BuilderCreated5()
    {
        testNTMultiChannel_BuilderCreatedImpl(variantUnion,
            new String[] {"severity", "status", "message"} );
    }

    public static void testNTMultiChannel_BuilderCreated6()
    {
        testNTMultiChannel_BuilderCreatedImpl(variantUnion,
            new String[] {"descriptor", "timeStamp", "alarm"},
            new String[] {"extra1"}, 
            new Field[]  { fieldCreate.createScalar(ScalarType.pvDouble)});
    }


    // Test is_a

    public static void testNTMultiChannelIs_a()
    {
        Structure s = NTMultiChannel.createBuilder().value(variantUnion).createStructure();
		assertTrue(NTMultiChannel.is_a(s));
    }

    public static void testStructureIs_a()
    {
        testStructureIs_aImpl(NTMultiChannel.URI, true);
        testStructureIs_aImpl("epics:nt/NTMultiChannel:1.0", true);
        testStructureIs_aImpl("epics:nt/NTMultiChannel:1.1", true);
        testStructureIs_aImpl("epics:nt/NTMultiChannel:2.0", false);
        testStructureIs_aImpl("epics:nt/NTMultiChannel", false);
        testStructureIs_aImpl("nt/NTMultiChannel:1.0", false);
        testStructureIs_aImpl("NTMultiChannel:1.0", false);
        testStructureIs_aImpl("NTMultiChannel", false);
        testStructureIs_aImpl("epics:nt/NTScalar:1.0", false);
    }

    // test compatibility - compatible structures

    public static void testStructureIsCompatible1a()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTMultiChannel.URI).
                addArray("value", variantUnion).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                createStructure(),
            true);  
    }

    public static void testStructureIsCompatible1b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTMultiChannel.URI).
                addArray("value", variantUnion).
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
                setId(NTMultiChannel.URI).
                addArray("value", variantUnion).
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
                setId(NTMultiChannel.URI).
                addArray("value", variantUnion).
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
                setId(NTMultiChannel.URI).
                addArray("value", variantUnion).
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
                setId(NTMultiChannel.URI).
                addArray("Value", variantUnion).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTMultiChannel.URI).
                add("value", variantUnion).
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
                setId(NTMultiChannel.URI).
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
                setId(NTMultiChannel.URI).
                addArray("value", variantUnion).
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
                setId(NTMultiChannel.URI).
                addArray("value", variantUnion).
                add("alarm", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d3()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTMultiChannel.URI).
                addArray("value", variantUnion).
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
                setId(NTMultiChannel.URI).
                addArray("value", variantUnion).
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
                setId(NTMultiChannel.URI).
                addArray("value", variantUnion).
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
                setId(NTMultiChannel.URI).
                addArray("value", variantUnion).
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
                setId(NTMultiChannel.URI).
                addArray("value", variantUnion).
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
                setId(NTMultiChannel.URI).
                addArray("value", variantUnion).
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
                setId(NTMultiChannel.URI).
                addArray("value", variantUnion).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
                add("message", ScalarType.pvInt).
                createStructure(),
            false);
    }

    // test wrapping compatible structures

    public static void testWrappedNTMultiChannel1()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTMultiChannel.URI).
            addArray("value", variantUnion).
                addArray("channelName", ScalarType.pvString).
                addArray("isConnected", ScalarType.pvBoolean).
            createStructure();

        NTMultiChannel ntscalarMultiChannel = NTMultiChannel.wrap(dataCreate.createPVStructure(s));

        ntScalarMultiChannelChecks(ntscalarMultiChannel, variantUnion, new String[0],
            new String[0], new Field[0]);

        NTMultiChannel ntscalarMultiChannel2 = NTMultiChannel.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntScalarMultiChannelChecks(ntscalarMultiChannel2, variantUnion, new String[0],
            new String[0], new Field[0]);
    }


    public static void testWrappedNTMultiChannel2()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTMultiChannel.URI).
            addArray("value", variantUnion).
            add("descriptor", ScalarType.pvString).
            add("alarm", ntField.createAlarm()).
            add("timeStamp", ntField.createTimeStamp()).
            addArray("extra", ScalarType.pvString).
            createStructure();

        String[] standardFields = { "descriptor", "alarm", "timeStamp" };
        String[] extraNames = { "extra" };
        Field[] extraFields = { fieldCreate.createScalarArray(ScalarType.pvString) };

        NTMultiChannel ntscalarMultiChannel = NTMultiChannel.wrap(dataCreate.createPVStructure(s));

        ntScalarMultiChannelChecks(ntscalarMultiChannel,variantUnion, standardFields, extraNames,extraFields);

        NTMultiChannel ntscalarMultiChannel2 = NTMultiChannel.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntScalarMultiChannelChecks(ntscalarMultiChannel,variantUnion, standardFields,
            extraNames,extraFields);
    }

    // test attaching timeStamps

    public static void testTimeStamp1()
    {
        NTMultiChannel ntscalarMultiChannel = NTMultiChannel.createBuilder().
            value(variantUnion).
            addTimeStamp().create();

        testAttachTimeStamp(ntscalarMultiChannel, true);
    }
 
    public static void testTimeStamp2()
    {
        NTMultiChannel ntscalarMultiChannel = NTMultiChannel.createBuilder().
            value(variantUnion).create();

        testAttachTimeStamp(ntscalarMultiChannel, false);
    }

    // test attaching alarms

    public static void testAlarm1()
    {
        NTMultiChannel ntscalarMultiChannel = NTMultiChannel.createBuilder().
             value(variantUnion).
            addAlarm().create();

        testAttachAlarm(ntscalarMultiChannel, true);
    }

    public static void testAlarm2()
    {
        NTMultiChannel ntscalarMultiChannel = NTMultiChannel.createBuilder().
             value(variantUnion).create();

        testAttachAlarm(ntscalarMultiChannel, false);
    }


    // test builder resets correctly
    public static void testBuilderResets()
    {
        NTMultiChannelBuilder builder = NTMultiChannel.createBuilder();

        Structure s1 = builder.
            value(variantUnion).
            createStructure();

        Structure s2 = builder.
            value(exampleRegularUnion()).
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
            value(variantUnion).
            createStructure();

        Structure s4 = builder.
             value(exampleRegularUnion()).
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
        assertEquals(expected, NTMultiChannel.is_a(s));
    }



    public static void testStructureIsCompatibleImpl(Structure s, boolean expected)
    {
        PVStructure pvSt = dataCreate.createPVStructure(s);
        assertEquals(expected, NTMultiChannel.isCompatible(pvSt));
    }

    private static
    void testNTMultiChannel_BuilderCreatedImpl(Union u)
    {
        testNTMultiChannel_BuilderCreatedImpl(u, new String[0], new String[0], new Field[0]);
    }

    private static
    void testNTMultiChannel_BuilderCreatedImpl(Union u, String[] standardFields)
    {
        testNTMultiChannel_BuilderCreatedImpl(u, standardFields, new String[0], new Field[0]);
    }


    private static
    void testNTMultiChannel_BuilderCreatedImpl(Union u, String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        NTMultiChannel ntscalarMultiChannel = createNTMultiChannel(u,
            standardFields,extraNames,extraFields);

        ntScalarMultiChannelChecks(ntscalarMultiChannel,u,
            standardFields,extraNames,extraFields);        
    }


    private static
    NTMultiChannel createNTMultiChannel(Union u, String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        boolean hasDescriptor  = find("descriptor", standardFields);
        boolean hasTimeStamp   = find("timeStamp", standardFields);
        boolean hasAlarm       = find("alarm", standardFields);
        boolean hasSeverity    = find("severity", standardFields);
        boolean hasStatus      = find("status", standardFields);
        boolean hasMessage     = find("message", standardFields);
        boolean hasSeconds     = find("secondsPastEpoch", standardFields);
        boolean hasNanoseconds = find("nanoseconds", standardFields);
        boolean hasUserTag     = find("userTag", standardFields);

        // Create NTMultiChannel
        NTMultiChannelBuilder builder = NTMultiChannel.createBuilder().value(u);

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

    private static
    void ntScalarMultiChannelChecks(NTMultiChannel ntscalarMultiChannel, Union u, String[] standardFields,
        String[] extraNames, Field[] extraFields)
    {
        // parse optional fields
        boolean hasDescriptor  = find("descriptor", standardFields);
        boolean hasTimeStamp   = find("timeStamp", standardFields);
        boolean hasAlarm       = find("alarm", standardFields);
        boolean hasSeverity    = find("severity", standardFields);
        boolean hasStatus      = find("status", standardFields);
        boolean hasSeconds     = find("secondsPastEpoch", standardFields);
        boolean hasNanoseconds = find("nanoseconds", standardFields);
        boolean hasUserTag     = find("userTag", standardFields);
        boolean hasMessage     = find("message", standardFields);

        // Test value field through NTMultiChannel interface
        PVUnionArray pvValue = ntscalarMultiChannel.getValue();
		assertNotNull(pvValue);

		// Test optional fields through NTMultiChannel interface
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

        // Test PVStructure from NTMultiChannel
        PVStructure pvStructure = ntscalarMultiChannel.getPVStructure();
        assertTrue(NTMultiChannel.is_a(pvStructure.getStructure()));
        assertTrue(NTMultiChannel.isCompatible(pvStructure));

        assertSame(pvValue, pvStructure.getSubField(PVUnionArray.class, "value"));
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

    private static Union exampleRegularUnion()
    {
        if (_exampleRegularUnion == null)
        {
            FieldBuilder fieldBuilder = FieldFactory.getFieldCreate().createFieldBuilder();

            for (ScalarType st : ScalarType.values())
                fieldBuilder.add(st.toString() + "Value", st);

            _exampleRegularUnion = fieldBuilder.createUnion();

        }
        return _exampleRegularUnion;
    }
    private static Union variantUnion = fieldCreate.createVariantUnion();
    private static Union _exampleRegularUnion = null;
}

