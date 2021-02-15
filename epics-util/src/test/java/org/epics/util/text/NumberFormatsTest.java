/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.text;

import java.text.NumberFormat;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class NumberFormatsTest {

    public NumberFormatsTest() {
    }

    @Before
    public void setLocale(){
        Locale.setDefault(new Locale("en", "US"));
    }

    @Test
    public void format1() {
        NumberFormat format = NumberFormats.precisionFormat(2);
        assertThat(format.format(2.0), equalTo("2.00"));
        assertThat(format.format(Double.NaN), equalTo("NaN"));
        assertThat(format.format(Double.POSITIVE_INFINITY), equalTo("Infinity"));
        assertThat(format.format(Double.NEGATIVE_INFINITY), equalTo("-Infinity"));
        assertThat(NumberFormats.precisionFormat(2), sameInstance(format));
    }

    @Test
    public void format2() {
        NumberFormat f = NumberFormats.precisionFormat(3);
        assertThat(f.format(1234.4567), equalTo("1234.457"));
        assertThat(f.format(123), equalTo("123.000"));
        assertThat(f.format(123.4), equalTo("123.400"));

        f = NumberFormats.precisionFormat(0);
        assertThat(f.format(1234.4567), equalTo("1234"));
        assertThat(f.format(123), equalTo("123"));
        assertThat(f.format(123.4), equalTo("123"));

        f = NumberFormats.precisionFormat(4);
        assertThat(f.format(1234.4567), equalTo("1234.4567"));
        assertThat(f.format(123), equalTo("123.0000"));
        assertThat(f.format(123.4), equalTo("123.4000"));
    }

    @Test
    public void toStringFormat() {
        NumberFormat format = NumberFormats.toStringFormat();
        assertThat(format.format(2.0), equalTo("2.0"));
        assertThat(format.format(Double.NaN), equalTo("NaN"));
        assertThat(format.format(Double.POSITIVE_INFINITY), equalTo("Infinity"));
        assertThat(format.format(Double.NEGATIVE_INFINITY), equalTo("-Infinity"));
        assertThat(NumberFormats.toStringFormat(), sameInstance(format));
    }

    @Test
    public void printfFormat1() {
        NumberFormat format = NumberFormats.printfFormat("Value: %10.2f mm");
        assertThat(format.format(2.0), equalTo("Value:       2.00 mm"));
        assertThat(format.format(Double.NaN), equalTo("Value:        NaN mm"));
        assertThat(format.format(Double.POSITIVE_INFINITY), equalTo("Value:   Infinity mm"));
        assertThat(format.format(Double.NEGATIVE_INFINITY), equalTo("Value:  -Infinity mm"));
        assertThat(NumberFormats.printfFormat("Value: %10.2f mm"), sameInstance(format));
    }
}
