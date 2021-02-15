package org.epics.gpclient.datasource.ca;

import gov.aps.jca.Channel;
import gov.aps.jca.Context;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.event.ConnectionListener;
import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ArrayInteger;
import org.epics.util.array.ListNumber;
import org.epics.vtype.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigInteger;

import static org.mockito.Mockito.*;

public class CAChannelHandlerTest {

    private CADataSource caDataSource;
    private Channel channel;
    private Context context;
    private CAChannelHandler caChannelHandler;

    @Before
    public void init() throws Exception{
        caDataSource = Mockito.mock(CADataSource.class);
        channel = Mockito.mock(Channel.class);
        context = Mockito.mock(Context.class);
        caChannelHandler = new CAChannelHandler("test", caDataSource);

        when(context.createChannel(anyString(), Mockito.any(ConnectionListener.class), anyShort())).thenReturn(channel);
        when(caDataSource.getContext()).thenReturn(context);
        caChannelHandler.connect();
    }

    @Test
    public void testWriteListNumberDouble() throws Exception{

        ListNumber doubles = ArrayDouble.of(Double.valueOf(1.1d), Double.valueOf(2.2d));
        caChannelHandler.write(doubles);
        verify(channel).put(new double[]{1.1d, 2.2d});
    }

    @Test
    public void testWriteVDoubleArray() throws Exception{
        VDoubleArray vDoubleArray = VDoubleArray.of(ArrayDouble.of(1.1d, 2.2d), Alarm.none(), Time.now(), Display.none());
        caChannelHandler.write(vDoubleArray);
        verify(channel).put(new double[]{1.1d, 2.2d});
    }

    @Test
    public void testWriteVIntArray() throws Exception{
        VIntArray vIntArray = VIntArray.of(ArrayInteger.of(1, 2), Alarm.none(), Time.now(), Display.none());
        caChannelHandler.write(vIntArray);
        verify(channel).put(new int[]{1, 2});
    }

    @Test
    public void testWriteDouble() throws Exception{
        Double value = Double.valueOf(7.7d);
        caChannelHandler.write(value);
        verify(channel).put(7.7d);
    }

    @Test
    public void testWriteInteger() throws Exception{
        Integer value = Integer.valueOf(7);
        caChannelHandler.write(value);
        verify(channel).put(7);
    }

    @Test
    public void testWriteFloat() throws Exception{
        Float value = Float.valueOf(7.8f);
        caChannelHandler.write(value);
        verify(channel).put(7.8f);
    }

    @Test
    public void testWriteShort() throws Exception{
        Short value = Short.valueOf((short)7);
        caChannelHandler.write(value);
        verify(channel).put((short)7);
    }

    @Test
    public void testWriteBigInteger() throws Exception{
        BigInteger value = BigInteger.valueOf(7);
        caChannelHandler.write(value);
        verify(channel).put(7);
    }

    @Test
    public void testWriteByte() throws Exception{
        Byte value = Byte.valueOf((byte)7);
        caChannelHandler.write(value);
        verify(channel).put((byte)7);
    }

    @Test
    public void testWriteDoubleArray() throws Exception{
        Double[] value = new Double[]{1.1d, 2.2d};
        caChannelHandler.write(value);
        verify(channel).put(new double[]{1.1d, 2.2d});
    }

    @Test
    public void testWriteIntegerArray() throws Exception{
        Integer[] value =
                new Integer[]{1, 2};
        caChannelHandler.write(value);
        verify(channel).put(new int[]{1, 2});
    }

    @Test
    public void testWriteString() throws Exception{
        String value = "ABC";
        when(channel.getFieldType()).thenReturn(DBRType.STRING);
        when(channel.getElementCount()).thenReturn(value.length());
        caChannelHandler.write(value);
        verify(channel).put("ABC");
    }

    @Test
    public void testWriteStringAsBytes() throws Exception{
        String value = "ABC";
        when(channel.getFieldType()).thenReturn(DBRType.BYTE);
        when(channel.getElementCount()).thenReturn(value.length());
        caChannelHandler.write(value);
        verify(channel).put(new byte[]{0x41, 0x42, 0x43, 0x0});
    }

    @Test
    public void testWriteByteArray() throws Exception{
        byte[] value = new byte[]{0x1, 0x2, 0x3};
        caChannelHandler.write(value);
        verify(channel).put(value);
    }

    @Test
    public void testWriteShortArray() throws Exception{
        short[] value = new short[]{(short)1, (short)2, (short)3};
        caChannelHandler.write(value);
        verify(channel).put(value);
    }

    @Test
    public void testWriteIntArray() throws Exception{
        int[] value = new int[]{1, 2, 3};
        caChannelHandler.write(value);
        verify(channel).put(value);
    }

    @Test
    public void testWriteFloatArray() throws Exception{
        float[] value = new float[]{1.1f, 2.2f, 3.3f};
        caChannelHandler.write(value);
        verify(channel).put(value);
    }

    @Test
    public void testWriteLongArray() throws Exception{
        long[] value = new long[]{1L, 2L, 3L};
        caChannelHandler.write(value);
        verify(channel).put(new double[]{1, 2, 3});
    }

    @Test
    public void testWriteDoubleAsPrimitiveArray() throws Exception{
        double[] value = new double[]{1.1d, 2.2d, 3.3d};
        caChannelHandler.write(value);
        verify(channel).put(value);
    }

    @Test
    public void testWriteVByte() throws Exception{
        VByte value = VByte.of(Byte.valueOf((byte)1), Alarm.none(), Time.now(), Display.none());
        caChannelHandler.write(value);
        verify(channel).put((byte)1);
    }

    @Test
    public void testWriteVShort() throws Exception{
        VShort value = VShort.of(Short.valueOf((short)1), Alarm.none(), Time.now(), Display.none());
        caChannelHandler.write(value);
        verify(channel).put((short)1);
    }

    @Test
    public void testWriteVInt() throws Exception{
        VInt value = VInt.of(Integer.valueOf(1), Alarm.none(), Time.now(), Display.none());
        caChannelHandler.write(value);
        verify(channel).put(1);
    }

    @Test
    public void testWriteVLong() throws Exception{
        VLong value = VLong.of(Long.valueOf(1), Alarm.none(), Time.now(), Display.none());
        caChannelHandler.write(value);
        verify(channel).put(1);

        double doubleValue = Math.pow(2, 33);
        value = VLong.of(Long.valueOf((long)doubleValue), Alarm.none(), Time.now(), Display.none());
        caChannelHandler.write(value);
        verify(channel).put(doubleValue);
    }

    @Test
    public void testWriteVFloat() throws Exception{
        VFloat value = VFloat.of(Float.valueOf(1.1f), Alarm.none(), Time.now(), Display.none());
        caChannelHandler.write(value);
        verify(channel).put(1.1f);
    }

    @Test
    public void testWriteVDouble() throws Exception{
        VDouble value = VDouble.of(Double.valueOf(1.1d), Alarm.none(), Time.now(), Display.none());
        caChannelHandler.write(value);
        verify(channel).put(1.1d);
    }

    @Test
    public void testWriteVEnum() throws Exception{
        VEnum value = VEnum.of(1, EnumDisplay.of("A", "b", "c"), Alarm.none(), Time.now());
        caChannelHandler.write(value);
        verify(channel).put("b");
    }
}
