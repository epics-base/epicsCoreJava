/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.epics.util.stats.Range;
import org.epics.vtype.Display;
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
    public void create1() {
        NumberFormat format = new DecimalFormat();
        Display display = Display.of(Range.of(0, 10), Range.of(1, 9), Range.of(2, 8), Range.of(0, 10), "m", format);
        assertThat(display.getDisplayRange(), equalTo(Range.of(0.0, 10.0)));
        assertThat(display.getWarningRange(), equalTo(Range.of(1.0, 9.0)));
        assertThat(display.getAlarmRange(), equalTo(Range.of(2.0, 8.0)));
        assertThat(display.getControlRange(), equalTo(Range.of(0.0, 10.0)));
        assertThat(display.getUnit(), equalTo("m"));
        assertThat(display.getFormat(), equalTo(format));
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
    public void equals1() {
        NumberFormat format = new DecimalFormat();
        Display display1 = Display.of(Range.of(0, 10), Range.of(1, 9), Range.of(2, 8), Range.of(0, 10), "m", format);
        Display display2 = Display.of(Range.of(0, 10), Range.of(1, 9), Range.of(2, 8), Range.of(0, 10), "m", format);
        assertThat(display1, equalTo(display2));
    }
    
}
