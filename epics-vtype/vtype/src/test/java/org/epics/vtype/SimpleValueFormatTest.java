/*
 * Copyright (C) 2010-18 epics and diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import org.epics.util.array.*;
import org.epics.util.stats.Range;
import org.epics.util.text.NumberFormats;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 * @author carcassi
 */
public class SimpleValueFormatTest {

    static {
        Locale.setDefault(new Locale("en", "US"));
    }

    Range maxDoubleRange = Range.of(Double.MIN_VALUE, Double.MAX_VALUE);
    Display display = Display.of(maxDoubleRange, maxDoubleRange, maxDoubleRange, maxDoubleRange, "",
            NumberFormats.precisionFormat(3));

    Display displayInt = Display.of(maxDoubleRange, maxDoubleRange, maxDoubleRange, maxDoubleRange, "",
            NumberFormats.precisionFormat(0));

    @Test
    public void defaultPrecision() {
        ValueFormat f = new SimpleValueFormat(3);
        assertThat(f.format(VDouble.of(1234.5678, Alarm.none(), Time.now(), display)), equalTo("1234.568"));
        assertThat(f.format(VIntArray.of(ArrayInteger.of(1, 2, 3), Alarm.none(), Time.now(), displayInt)), equalTo("[1, 2, 3]"));
        assertThat(f.format(VIntArray.of(ArrayInteger.of(1), Alarm.none(), Time.now(), displayInt)), equalTo("[1]"));
        assertThat(f.format(VIntArray.of(ArrayInteger.of(1, 2, 3, 4, 5), Alarm.none(), Time.now(), displayInt)), equalTo("[1, 2, 3, ...]"));
        assertThat(f.format(VFloatArray.of(ArrayFloat.of(new float[] {1, 2, 3}), Alarm.none(), Time.now(), display)), equalTo("[1.000, 2.000, 3.000]"));
        assertThat(f.format(VFloatArray.of(ArrayFloat.of(new float[] {1}), Alarm.none(), Time.now(), display)), equalTo("[1.000]"));
        assertThat(f.format(VFloatArray.of(ArrayFloat.of(new float[] {1, 2, 3, 4, 5}), Alarm.none(), Time.now(), display)), equalTo("[1.000, 2.000, 3.000, ...]"));
        assertThat(f.format(VDoubleArray.of(ArrayDouble.of(1, 2, 3), Alarm.none(), Time.now(), display)), equalTo("[1.000, 2.000, 3.000]"));
        assertThat(f.format(VDoubleArray.of(ArrayDouble.of(1), Alarm.none(), Time.now(), display)), equalTo("[1.000]"));
        assertThat(f.format(VDoubleArray.of(ArrayDouble.of(1, 2, 3, 4, 5), Alarm.none(), Time.now(), display)), equalTo("[1.000, 2.000, 3.000, ...]"));
        assertThat(f.format(VStringArray.of(Arrays.asList("A", "B", "C"), Alarm.none(), Time.now())), equalTo("[A, B, C]"));
        assertThat(f.format(VStringArray.of(Collections.singletonList("A"), Alarm.none(), Time.now())), equalTo("[A]"));
        assertThat(f.format(VStringArray.of(Arrays.asList("A", "B", "C", "D", "E"), Alarm.none(), Time.now())), equalTo("[A, B, C, ...]"));
//        assertThat(f.format(newVEnumArray(ArrayInteger.of(2, 0, 0), Arrays.asList("A", "B", "C"), Alarm.none(), Time.now())), equalTo("[C, A, A]"));
//        assertThat(f.format(newVEnumArray(ArrayInteger.of(2), Arrays.asList("A", "B", "C"), Alarm.none(), Time.now())), equalTo("[C]"));
//        assertThat(f.format(newVEnumArray(ArrayInteger.of(2, 0, 0, 1, 0), Arrays.asList("A", "B", "C"), Alarm.none(), Time.now())), equalTo("[C, A, A, ...]"));
    }

    @Test
    public void testMandatedPrecision() {
        ValueFormat f = new SimpleValueFormat(3);
        f.setNumberFormat(NumberFormats.precisionFormat(2));
        Display display = Display.of(maxDoubleRange, maxDoubleRange, maxDoubleRange, maxDoubleRange, "", f.getNumberFormat());
        assertThat(f.format(VDouble.of(1234.5678, Alarm.none(), Time.now(), display)), equalTo("1234.57"));
        assertThat(f.format(VIntArray.of(ArrayInteger.of(1, 2, 3), Alarm.none(), Time.now(), displayInt)), equalTo("[1.00, 2.00, 3.00]"));
        assertThat(f.format(VIntArray.of(ArrayInteger.of(1), Alarm.none(), Time.now(), displayInt)), equalTo("[1.00]"));
        assertThat(f.format(VIntArray.of(ArrayInteger.of(1, 2, 3, 4, 5), Alarm.none(), Time.now(), displayInt)), equalTo("[1.00, 2.00, 3.00, ...]"));
        assertThat(f.format(VFloatArray.of(ArrayFloat.of(new float[] {1, 2, 3}), Alarm.none(), Time.now(), display)), equalTo("[1.00, 2.00, 3.00]"));
        assertThat(f.format(VFloatArray.of(ArrayFloat.of(new float[] {1}), Alarm.none(), Time.now(), display)), equalTo("[1.00]"));
        assertThat(f.format(VFloatArray.of(ArrayFloat.of(new float[] {1, 2, 3, 4, 5}), Alarm.none(), Time.now(), display)), equalTo("[1.00, 2.00, 3.00, ...]"));
        assertThat(f.format(VDoubleArray.of(ArrayDouble.of(1, 2, 3), Alarm.none(), Time.now(), display)), equalTo("[1.00, 2.00, 3.00]"));
        assertThat(f.format(VDoubleArray.of(ArrayDouble.of(1), Alarm.none(), Time.now(), display)), equalTo("[1.00]"));
        assertThat(f.format(VDoubleArray.of(ArrayDouble.of(1, 2, 3, 4, 5), Alarm.none(), Time.now(), display)), equalTo("[1.00, 2.00, 3.00, ...]"));
        assertThat(f.format(VStringArray.of(Arrays.asList("A", "B", "C"), Alarm.none(), Time.now())), equalTo("[A, B, C]"));
        assertThat(f.format(VStringArray.of(Collections.singletonList("A"), Alarm.none(), Time.now())), equalTo("[A]"));
        assertThat(f.format(VStringArray.of(Arrays.asList("A", "B", "C", "D", "E"), Alarm.none(), Time.now())), equalTo("[A, B, C, ...]"));
    }

    @Test
    public void parseVDouble1() {
        ValueFormat f = new SimpleValueFormat(3);
        VDouble reference = VDouble.of(3.0, Alarm.none(), Time.now(), Display.none());
        assertThat(f.parseObject("3.14", reference), equalTo((Object) 3.14));
        assertThat(f.parseDouble("3.14"), equalTo(3.14));
        assertThat(f.parseDouble("1333"), equalTo(1333.0));
    }

    @Test
    public void parseVFloat1() {
        ValueFormat f = new SimpleValueFormat(3);
        VFloat reference = Mockito.mock(VFloat.class);
        assertThat(f.parseObject("3.14", reference), equalTo((Object) 3.14f));
        assertThat(f.parseFloat("3.14"), equalTo(3.14f));
        assertThat(f.parseFloat("1333"), equalTo(1333.0f));
    }

    @Test
    public void parseVInt1() {
        ValueFormat f = new SimpleValueFormat(3);
        VInt reference = Mockito.mock(VInt.class);
        assertThat(f.parseObject("314", reference), equalTo((Object) 314));
        assertThat(f.parseInt("314"), equalTo(314));
        assertThat(f.parseInt("1333"), equalTo(1333));
    }

    @Test
    public void parseVShort1() {
        ValueFormat f = new SimpleValueFormat(3);
        VShort reference = Mockito.mock(VShort.class);
        assertThat(f.parseObject("314", reference), equalTo((Object) (short) 314));
        assertThat(f.parseShort("314"), equalTo((short) 314));
        assertThat(f.parseShort("1333"), equalTo((short) 1333));
    }

    @Test
    public void parseVByte1() {
        ValueFormat f = new SimpleValueFormat(3);
        VByte reference = Mockito.mock(VByte.class);
        assertThat(f.parseObject("23", reference), equalTo((Object) (byte) 23));
        assertThat(f.parseByte("23"), equalTo((byte) 23));
        assertThat(f.parseByte("112"), equalTo((byte) 112));
    }

    @Test
    public void parseVString1() {
        ValueFormat f = new SimpleValueFormat(3);
        VString reference = Mockito.mock(VString.class);
        assertThat(f.parseObject("Testing", reference), equalTo((Object) "Testing"));
        assertThat(f.parseString("Testing"), equalTo("Testing"));
        assertThat(f.parseString("Foo"), equalTo("Foo"));
    }

    @Test
    public void parseVEnum1() {
        ValueFormat f = new SimpleValueFormat(3);
        List<String> labels = Arrays.asList("A", "B", "C");
        VEnum reference = VEnum.of(0, EnumDisplay.of(labels), Alarm.none(), Time.now());
        assertThat(f.parseObject("A", reference), equalTo((Object) 0));
        assertThat(f.parseEnum("A", labels), equalTo(0));
        assertThat(f.parseEnum("B", labels), equalTo(1));
    }

    @Test
    public void parseVDoubleArray1() {
        ValueFormat f = new SimpleValueFormat(3);
        VDoubleArray reference = Mockito.mock(VDoubleArray.class);
        assertThat(f.parseObject("3.14", reference), equalTo((Object) ArrayDouble.of(3.14)));
        assertThat(f.parseDoubleArray("3.14"), equalTo((ListDouble) ArrayDouble.of(3.14)));
        assertThat(f.parseDoubleArray("1333, 3.14"), equalTo((ListDouble) ArrayDouble.of(1333, 3.14)));
        assertThat(f.parseDoubleArray("1.0, 2.0, 3.0, 4.0"), equalTo((ListDouble) ArrayDouble.of(1.0, 2.0, 3.0, 4.0)));
    }

    @Test
    public void parseVFloatArray1() {
        ValueFormat f = new SimpleValueFormat(3);
        VFloatArray reference = Mockito.mock(VFloatArray.class);
        assertThat(f.parseObject("3.14", reference), equalTo((Object) ArrayFloat.of(3.14f)));
        assertThat(f.parseFloatArray("3.14"), equalTo((ListFloat) ArrayFloat.of(3.14f)));
        assertThat(f.parseFloatArray("1333, 3.14"), equalTo((ListFloat) ArrayFloat.of(1333f, 3.14f)));
        assertThat(f.parseFloatArray("1.0, 2.0, 3.0, 4.0"), equalTo((ListFloat) ArrayFloat.of(1.0f, 2.0f, 3.0f, 4.0f)));
    }

    @Test
    public void parseVIntArray1() {
        ValueFormat f = new SimpleValueFormat(3);
        VIntArray reference = Mockito.mock(VIntArray.class);
        assertThat(f.parseObject("3", reference), equalTo((Object) ArrayInteger.of(3)));
        assertThat(f.parseIntArray("3"), CoreMatchers.<ListInteger>equalTo(ArrayInteger.of(3)));
        assertThat(f.parseIntArray("1333, 3"), CoreMatchers.<ListInteger>equalTo(ArrayInteger.of(1333, 3)));
        assertThat(f.parseIntArray("1, 2, 3, 4"), CoreMatchers.<ListInteger>equalTo(ArrayInteger.of(1, 2, 3, 4)));
    }

    @Test
    public void parseVShortArray1() {
        ValueFormat f = new SimpleValueFormat(3);
        VShortArray reference = Mockito.mock(VShortArray.class);
        assertThat(f.parseObject("3", reference), equalTo((Object) ArrayShort.of((short) 3)));
        assertThat(f.parseShortArray("3"), CoreMatchers.<ListShort>equalTo(ArrayShort.of((short) 3)));
        assertThat(f.parseShortArray("1333, 3"), CoreMatchers.<ListShort>equalTo(ArrayShort.of(new short[]{1333, 3})));
        assertThat(f.parseShortArray("1, 2, 3, 4"), CoreMatchers.<ListShort>equalTo(ArrayShort.of(new short[]{1, 2, 3, 4})));
    }

    @Test
    public void parseVByteArray1() {
        ValueFormat f = new SimpleValueFormat(3);
        VByteArray reference = Mockito.mock(VByteArray.class);
        assertThat(f.parseObject("3", reference), equalTo((Object) ArrayByte.of(new byte[] {3})));
        assertThat(f.parseByteArray("3"), equalTo((ListByte) ArrayByte.of(new byte[] {3})));
        assertThat(f.parseByteArray("113, 3"), equalTo((ListByte) ArrayByte.of(new byte[] {113, 3})));
        assertThat(f.parseByteArray("1, 2, 3, 4"), equalTo((ListByte) ArrayByte.of(new byte[] {1, 2, 3, 4})));
    }

    @Test
    public void parseVStringArray1() {
        ValueFormat f = new SimpleValueFormat(3);
        VStringArray reference = Mockito.mock(VStringArray.class);
        assertThat(f.parseObject("test", reference), equalTo((Object) Collections.singletonList("test")));
        assertThat(f.parseStringArray("test"), equalTo(Collections.singletonList("test")));
        assertThat(f.parseStringArray("a, b"), equalTo(Arrays.asList("a", "b")));
        assertThat(f.parseStringArray("a, b, c, d"), equalTo(Arrays.asList("a", "b", "c", "d")));
    }

//    @Test
//    public void parseVEnumArray1() {
//        ValueFormat f = new SimpleValueFormat(3);
//        List<String> labels = Arrays.asList("A", "B", "C");
//        VEnumArray reference = newVEnumArray(ArrayInteger.of(1), labels, Alarm.none(), Time.now());
//        assertThat(f.parseObject("A", reference), equalTo((Object) ArrayInteger.of(0)));
//        assertThat(f.parseEnumArray("A", labels), equalTo((ListInt) ArrayInteger.of(0)));
//        assertThat(f.parseEnumArray("B, A", labels), equalTo((ListInt) ArrayInteger.of(1, 0)));
//        assertThat(f.parseEnumArray("B, A, C,A", labels), equalTo((ListInt) ArrayInteger.of(1, 0, 2, 0)));
//    }

}
