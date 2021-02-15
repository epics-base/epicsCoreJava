/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.number;

import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class UnsignedConversionsTest {

    public UnsignedConversionsTest() {
    }

    @Test
    public void byteToShort() {
        assertThat(UnsignedConversions.toShort((byte) 0), equalTo((short) 0));
        assertThat(UnsignedConversions.toShort((byte) 1), equalTo((short) 1));
        assertThat(UnsignedConversions.toShort((byte) 127), equalTo((short) 127));
        assertThat(UnsignedConversions.toShort((byte) -128), equalTo((short) 128));
        assertThat(UnsignedConversions.toShort((byte) -1), equalTo((short) 255));
    }

    @Test
    public void byteToInt() {
        assertThat(UnsignedConversions.toInt((byte) 0), equalTo((int) 0));
        assertThat(UnsignedConversions.toInt((byte) 1), equalTo((int) 1));
        assertThat(UnsignedConversions.toInt((byte) 127), equalTo((int) 127));
        assertThat(UnsignedConversions.toInt((byte) -128), equalTo((int) 128));
        assertThat(UnsignedConversions.toInt((byte) -1), equalTo((int) 255));
    }

    @Test
    public void byteToLong() {
        assertThat(UnsignedConversions.toLong((byte) 0), equalTo((long) 0));
        assertThat(UnsignedConversions.toLong((byte) 1), equalTo((long) 1));
        assertThat(UnsignedConversions.toLong((byte) 127), equalTo((long) 127));
        assertThat(UnsignedConversions.toLong((byte) -128), equalTo((long) 128));
        assertThat(UnsignedConversions.toLong((byte) -1), equalTo((long) 255));
    }

    @Test
    public void byteToFloat() {
        assertThat(UnsignedConversions.toFloat((byte) 0), equalTo((float) 0));
        assertThat(UnsignedConversions.toFloat((byte) 1), equalTo((float) 1));
        assertThat(UnsignedConversions.toFloat((byte) 127), equalTo((float) 127));
        assertThat(UnsignedConversions.toFloat((byte) -128), equalTo((float) 128));
        assertThat(UnsignedConversions.toFloat((byte) -1), equalTo((float) 255));
    }

    @Test
    public void byteToDouble() {
        assertThat(UnsignedConversions.toDouble((byte) 0), equalTo((double) 0));
        assertThat(UnsignedConversions.toDouble((byte) 1), equalTo((double) 1));
        assertThat(UnsignedConversions.toDouble((byte) 127), equalTo((double) 127));
        assertThat(UnsignedConversions.toDouble((byte) -128), equalTo((double) 128));
        assertThat(UnsignedConversions.toDouble((byte) -1), equalTo((double) 255));
    }

    @Test
    public void shortToInt() {
        assertThat(UnsignedConversions.toInt((short) 0), equalTo((int) 0));
        assertThat(UnsignedConversions.toInt((short) 1), equalTo((int) 1));
        assertThat(UnsignedConversions.toInt((short) 32767), equalTo((int) 32767));
        assertThat(UnsignedConversions.toInt((short) -32768), equalTo((int) 32768));
        assertThat(UnsignedConversions.toInt((short) -1), equalTo((int) 65535));
    }

    @Test
    public void shortToLong() {
        assertThat(UnsignedConversions.toLong((short) 0), equalTo((long) 0));
        assertThat(UnsignedConversions.toLong((short) 1), equalTo((long) 1));
        assertThat(UnsignedConversions.toLong((short) 32767), equalTo((long) 32767));
        assertThat(UnsignedConversions.toLong((short) -32768), equalTo((long) 32768));
        assertThat(UnsignedConversions.toLong((short) -1), equalTo((long) 65535));
    }

    @Test
    public void shortToFloat() {
        assertThat(UnsignedConversions.toFloat((short) 0), equalTo((float) 0));
        assertThat(UnsignedConversions.toFloat((short) 1), equalTo((float) 1));
        assertThat(UnsignedConversions.toFloat((short) 32767), equalTo((float) 32767));
        assertThat(UnsignedConversions.toFloat((short) -32768), equalTo((float) 32768));
        assertThat(UnsignedConversions.toFloat((short) -1), equalTo((float) 65535));
    }

    @Test
    public void shortToDouble() {
        assertThat(UnsignedConversions.toDouble((short) 0), equalTo((double) 0));
        assertThat(UnsignedConversions.toDouble((short) 1), equalTo((double) 1));
        assertThat(UnsignedConversions.toDouble((short) 32767), equalTo((double) 32767));
        assertThat(UnsignedConversions.toDouble((short) -32768), equalTo((double) 32768));
        assertThat(UnsignedConversions.toDouble((short) -1), equalTo((double) 65535));
    }

    @Test
    public void intToLong() {
        assertThat(UnsignedConversions.toLong((int) 0), equalTo((long) 0L));
        assertThat(UnsignedConversions.toLong((int) 1), equalTo((long) 1L));
        assertThat(UnsignedConversions.toLong((int) 2147483647), equalTo((long) 2147483647L));
        assertThat(UnsignedConversions.toLong((int) -2147483648), equalTo((long) 2147483648L));
        assertThat(UnsignedConversions.toLong((int) -1), equalTo((long) 4294967295L));
    }

    @Test
    public void intToFloat() {
        assertThat(UnsignedConversions.toFloat((int) 0), equalTo((float) 0L));
        assertThat(UnsignedConversions.toFloat((int) 1), equalTo((float) 1L));
        assertThat(UnsignedConversions.toFloat((int) 2147483647), equalTo((float) 2147483647L));
        assertThat(UnsignedConversions.toFloat((int) -2147483648), equalTo((float) 2147483648L));
        assertThat(UnsignedConversions.toFloat((int) -1), equalTo((float) 4294967295L));
    }

    @Test
    public void intToDouble() {
        assertThat(UnsignedConversions.toDouble((int) 0), equalTo((double) 0L));
        assertThat(UnsignedConversions.toDouble((int) 1), equalTo((double) 1L));
        assertThat(UnsignedConversions.toDouble((int) 2147483647), equalTo((double) 2147483647L));
        assertThat(UnsignedConversions.toDouble((int) -2147483648), equalTo((double) 2147483648L));
        assertThat(UnsignedConversions.toDouble((int) -1), equalTo((double) 4294967295L));
    }

    @Test
    public void longToFloat() {
        assertThat(UnsignedConversions.toFloat((long) 0), equalTo((float) 0L));
        assertThat(UnsignedConversions.toFloat((long) 1), equalTo((float) 1L));
        assertThat(UnsignedConversions.toFloat((long) 0x7fffffffffffffffL), equalTo((float) 9223372036854775807F));
        assertThat(UnsignedConversions.toFloat((long) 0x8000000000000000L), equalTo((float) 9223372036854775808F));
        assertThat(UnsignedConversions.toFloat((long) -1), equalTo((float) 18446744073709551615F));
    }

    @Test
    public void longToDouble() {
        assertThat(UnsignedConversions.toDouble((long) 0), equalTo((double) 0.0));
        assertThat(UnsignedConversions.toDouble((long) 1), equalTo((double) 1.0));
        assertThat(UnsignedConversions.toDouble((long) 0x7fffffffffffffffL), equalTo((double) 9223372036854775807.0));
        assertThat(UnsignedConversions.toDouble((long) 0x8000000000000000L), equalTo((double) 9223372036854775808.0));
        assertThat(UnsignedConversions.toDouble((long) -1), equalTo((double) 18446744073709551615.0));
    }

    @Test
    public void longToBigInteger() {
        assertThat(UnsignedConversions.toBigInteger((long) 0), equalTo(BigInteger.ZERO));
        assertThat(UnsignedConversions.toBigInteger((long) 1), equalTo(BigInteger.ONE));
        assertThat(UnsignedConversions.toBigInteger((long) 0x7fffffffffffffffL), equalTo(new BigInteger("9223372036854775807")));
        assertThat(UnsignedConversions.toBigInteger((long) 0x8000000000000000L), equalTo(new BigInteger("9223372036854775808")));
        assertThat(UnsignedConversions.toBigInteger((long) -1), equalTo(new BigInteger("18446744073709551615")));
    }
}
