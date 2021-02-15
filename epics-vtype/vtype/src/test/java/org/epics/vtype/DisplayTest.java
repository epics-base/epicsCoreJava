/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.epics.util.stats.Range;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class DisplayTest {

    public DisplayTest() {
    }

    @Test
    public void displayOf1() {
        assertThat(Display.displayOf(null), equalTo(Display.none()));
        assertThat(Display.displayOf(new Object()), equalTo(Display.none()));
        NumberFormat format = new DecimalFormat();
        final Display display = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 9), Range.of(0, 10), "m", format);
        assertThat(Display.displayOf(new DisplayProvider() {
                    public Display getDisplay() {
                        return display;
                    }
                }),
                equalTo(display));
    }

    @Test
    public void equals1() {
        NumberFormat format = new DecimalFormat();
        NumberFormat format2 = new DecimalFormat("0.00");
        Display display01 = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 9), Range.of(0, 10), "m", format);
        Display display02 = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 9), Range.of(0, 10), "m", format);
        Display display03 = Display.of(Range.of(1, 10), Range.of(2, 8), Range.of(1, 9), Range.of(0, 10), "m", format);
        Display display04 = Display.of(Range.of(0, 11), Range.of(2, 8), Range.of(1, 9), Range.of(0, 10), "m", format);
        Display display05 = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(2, 9), Range.of(0, 10), "m", format);
        Display display06 = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 8), Range.of(0, 10), "m", format);
        Display display07 = Display.of(Range.of(0, 10), Range.of(3, 8), Range.of(1, 9), Range.of(0, 10), "m", format);
        Display display08 = Display.of(Range.of(0, 10), Range.of(2, 7), Range.of(1, 9), Range.of(0, 10), "m", format);
        Display display09 = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 9), Range.of(-1, 10), "m", format);
        Display display10 = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 9), Range.of(0, 11), "m", format);
        Display display11 = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 9), Range.of(0, 10), "km", format);
        Display display12 = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 9), Range.of(0, 10), "m", format2);
        assertThat(display01, equalTo(display02));
        assertThat(display01, not(equalTo(display03)));
        assertThat(display01, not(equalTo(display04)));
        assertThat(display01, not(equalTo(display05)));
        assertThat(display01, not(equalTo(display06)));
        assertThat(display01, not(equalTo(display07)));
        assertThat(display01, not(equalTo(display08)));
        assertThat(display01, not(equalTo(display09)));
        assertThat(display01, not(equalTo(display10)));
        assertThat(display01, not(equalTo(display11)));
        assertThat(display01, not(equalTo(display12)));
        assertThat(display01, not(equalTo(null)));
        assertThat(display01, not(equalTo(new Object())));
    }

    @Test
    public void hashCode1() {
        NumberFormat format = new DecimalFormat();
        NumberFormat format2 = new DecimalFormat("##0.#####E0");
        Display display01 = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 9), Range.of(0, 10), "m", format);
        Display display02 = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 9), Range.of(0, 10), "m", format);
        Display display03 = Display.of(Range.of(1, 10), Range.of(2, 8), Range.of(1, 9), Range.of(0, 10), "m", format);
        Display display04 = Display.of(Range.of(0, 11), Range.of(2, 8), Range.of(1, 9), Range.of(0, 10), "m", format);
        Display display05 = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(2, 9), Range.of(0, 10), "m", format);
        Display display06 = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 8), Range.of(0, 10), "m", format);
        Display display07 = Display.of(Range.of(0, 10), Range.of(3, 8), Range.of(1, 9), Range.of(0, 10), "m", format);
        Display display08 = Display.of(Range.of(0, 10), Range.of(2, 7), Range.of(1, 9), Range.of(0, 10), "m", format);
        Display display09 = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 9), Range.of(-1, 10), "m", format);
        Display display10 = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 9), Range.of(0, 11), "m", format);
        Display display11 = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 9), Range.of(0, 10), "km", format);
        Display display12 = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 9), Range.of(0, 10), "m", format2);
        assertThat(display01.hashCode(), equalTo(display02.hashCode()));
        assertThat(display01.hashCode(), not(equalTo(display03.hashCode())));
        assertThat(display01.hashCode(), not(equalTo(display04.hashCode())));
        assertThat(display01.hashCode(), not(equalTo(display05.hashCode())));
        assertThat(display01.hashCode(), not(equalTo(display06.hashCode())));
        assertThat(display01.hashCode(), not(equalTo(display07.hashCode())));
        assertThat(display01.hashCode(), not(equalTo(display08.hashCode())));
        assertThat(display01.hashCode(), not(equalTo(display09.hashCode())));
        assertThat(display01.hashCode(), not(equalTo(display10.hashCode())));
        assertThat(display01.hashCode(), not(equalTo(display11.hashCode())));
        assertThat(display01.hashCode(), not(equalTo(display12.hashCode())));
    }

    @Test
    public void of1() {
        NumberFormat format = new DecimalFormat();
        Display display = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 9), Range.of(0, 10), "m", format);
        assertThat(display.getDisplayRange(), equalTo(Range.of(0.0, 10.0)));
        assertThat(display.getWarningRange(), equalTo(Range.of(1.0, 9.0)));
        assertThat(display.getAlarmRange(), equalTo(Range.of(2.0, 8.0)));
        assertThat(display.getControlRange(), equalTo(Range.of(0.0, 10.0)));
        assertThat(display.getUnit(), equalTo("m"));
        assertThat(display.getFormat(), equalTo(format));
    }

    @Test(expected=NullPointerException.class)
    public void of2() {
        NumberFormat format = new DecimalFormat();
        Display display = Display.of(null, Range.of(2, 8), Range.of(1, 9), Range.of(0, 10), "m", format);
    }

    @Test(expected=NullPointerException.class)
    public void of3() {
        NumberFormat format = new DecimalFormat();
        Display display = Display.of(Range.of(0, 10), Range.of(2, 8), null, Range.of(0, 10), "m", format);
    }

    @Test(expected=NullPointerException.class)
    public void of4() {
        NumberFormat format = new DecimalFormat();
        Display display = Display.of(Range.of(0, 10), null, Range.of(1, 9), Range.of(0, 10), "m", format);
    }

    @Test(expected=NullPointerException.class)
    public void of5() {
        NumberFormat format = new DecimalFormat();
        Display display = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 9), null, "m", format);
    }

    @Test(expected=NullPointerException.class)
    public void of6() {
        NumberFormat format = new DecimalFormat();
        Display display = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 9), Range.of(0, 10), null, format);
    }

    @Test(expected=NullPointerException.class)
    public void of7() {
        NumberFormat format = new DecimalFormat();
        Display display = Display.of(Range.of(0, 10), Range.of(2, 8), Range.of(1, 9), Range.of(0, 10), "m", null);
    }

    @Test
    public void none1() {
        Display display = Display.none();
        assertThat(display.getDisplayRange(), equalTo(Range.undefined()));
        assertThat(display.getWarningRange(), equalTo(Range.undefined()));
        assertThat(display.getAlarmRange(), equalTo(Range.undefined()));
        assertThat(display.getControlRange(), equalTo(Range.undefined()));
        assertThat(display.getUnit(), equalTo(""));
    }

    @Test
    public void newAlarmFor1() {
        Display display = Display.of(Range.of(-10, 10), Range.of(-9, 9), Range.of(-8, 8), Range.undefined(),
                "", Display.defaultNumberFormat());
        assertThat(display.newAlarmFor(0), equalTo(Alarm.none()));
        assertThat(display.newAlarmFor(9.5), equalTo(Alarm.hihi()));
        assertThat(display.newAlarmFor(8.5), equalTo(Alarm.high()));
        assertThat(display.newAlarmFor(-8.5), equalTo(Alarm.low()));
        assertThat(display.newAlarmFor(-9.5), equalTo(Alarm.lolo()));
    }

    @Test
    public void newAlarmFor2() {
        Display display = Display.of(Range.undefined(), Range.of(0, 100), Range.of(20, 80), Range.undefined(),
                "", Display.defaultNumberFormat());
        assertThat(display.newAlarmFor(31), equalTo(Alarm.none()));
        assertThat(display.newAlarmFor(107), equalTo(Alarm.hihi()));
        assertThat(display.newAlarmFor(81.3), equalTo(Alarm.high()));
        assertThat(display.newAlarmFor(15), equalTo(Alarm.low()));
        assertThat(display.newAlarmFor(-0.3), equalTo(Alarm.lolo()));
    }

    @Test
    public void newAlarmFor3() {
        Display display = Display.of(Range.undefined(), Range.undefined(), Range.undefined(), Range.undefined(),
                "", Display.defaultNumberFormat());
        assertThat(display.newAlarmFor(31), equalTo(Alarm.none()));
        assertThat(display.newAlarmFor(107), equalTo(Alarm.none()));
        assertThat(display.newAlarmFor(81.3), equalTo(Alarm.none()));
        assertThat(display.newAlarmFor(15), equalTo(Alarm.none()));
        assertThat(display.newAlarmFor(-0.3), equalTo(Alarm.none()));
    }

    @Test
    public void newAlarmFor4() {
        Display display = Display.of(Range.undefined(), Range.undefined(), Range.of(20, 80), Range.undefined(),
                "", Display.defaultNumberFormat());
        assertThat(display.newAlarmFor(31), equalTo(Alarm.none()));
        assertThat(display.newAlarmFor(107), equalTo(Alarm.high()));
        assertThat(display.newAlarmFor(81.3), equalTo(Alarm.high()));
        assertThat(display.newAlarmFor(15), equalTo(Alarm.low()));
        assertThat(display.newAlarmFor(-0.3), equalTo(Alarm.low()));
    }

    @Test
    public void newAlarmFor5() {
        Display display = Display.of(Range.of(-10, 10), Range.of(-9, 9), Range.undefined(), Range.undefined(),
                "", Display.defaultNumberFormat());
        assertThat(display.newAlarmFor(0), equalTo(Alarm.none()));
        assertThat(display.newAlarmFor(9.5), equalTo(Alarm.hihi()));
        assertThat(display.newAlarmFor(8.5), equalTo(Alarm.none()));
        assertThat(display.newAlarmFor(-8.5), equalTo(Alarm.none()));
        assertThat(display.newAlarmFor(-9.5), equalTo(Alarm.lolo()));
    }

}
