/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.stats;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.epics.util.stats.Ranges.*;

/**
 *
 * @author carcassi
 */
public class RangesTest {

    public RangesTest() {
    }

    @Test
    public void range1() throws Exception {
        Range range = Range.of(0.0, 10.0);
        assertThat(range.getMinimum(), equalTo((Number) 0.0));
        assertThat(range.getMaximum(), equalTo((Number) 10.0));
    }

    public void range2() throws Exception {
        Range range = Range.of(0.0, 0.0);
        assertThat(range.getMinimum(), equalTo((Number) 0.0));
        assertThat(range.getMaximum(), equalTo((Number) 0.0));
    }

    public void absRange1() {
        Range range = absRange(Range.of(1, 3));
        assertThat(range.getMinimum(), equalTo((Number) 1.0));
        assertThat(range.getMaximum(), equalTo((Number) 3.0));
    }

    public void absRange2() {
        Range range = absRange(Range.of(0, 3));
        assertThat(range.getMinimum(), equalTo((Number) 0.0));
        assertThat(range.getMaximum(), equalTo((Number) 3.0));
    }

    public void absRange3() {
        Range range = absRange(Range.of(-2, 3));
        assertThat(range.getMinimum(), equalTo((Number) 0.0));
        assertThat(range.getMaximum(), equalTo((Number) 3.0));
    }

    public void absRange4() {
        Range range = absRange(Range.of(-4, 2));
        assertThat(range.getMinimum(), equalTo((Number) 0.0));
        assertThat(range.getMaximum(), equalTo((Number) 4.0));
    }

    public void absRange5() {
        Range range = absRange(Range.of(-4, -2));
        assertThat(range.getMinimum(), equalTo((Number) 2.0));
        assertThat(range.getMaximum(), equalTo((Number) 4.0));
    }

    @Test
    public void overlap1() {
        Range range1 = Range.of(0.0, 6.0);
        Range range2 = Range.of(3.0, 6.0);
        assertThat(Ranges.overlap(range1, range2), equalTo(0.5));
    }

    @Test
    public void overlap2() {
        Range range1 = Range.of(0.0, 8.0);
        Range range2 = Range.of(2.0, 4.0);
        assertThat(Ranges.overlap(range1, range2), equalTo(0.25));
    }

    @Test
    public void overlap3() {
        Range range1 = Range.of(0.0, 8.0);
        Range range2 = Range.of(2.0, 4.0);
        assertThat(Ranges.overlap(range1, range2), equalTo(0.25));
    }

    @Test
    public void overlap4() {
        Range range1 = Range.of(0.0, 8.0);
        Range range2 = Range.of(0.0, 4.0);
        assertThat(Ranges.overlap(range1, range2), equalTo(0.5));
    }

    @Test
    public void overlap5() {
        Range range1 = Range.of(0.0, 8.0);
        Range range2 = Range.of(-1.0, 4.0);
        assertThat(Ranges.overlap(range1, range2), equalTo(0.5));
    }

    @Test
    public void overlap6() {
        Range range1 = Range.of(0.0, 8.0);
        Range range2 = Range.of(-1.0, 14.0);
        assertThat(Ranges.overlap(range1, range2), equalTo(1.0));
    }

    @Test
    public void overlap7() {
        Range range1 = Range.of(0.0, 8.0);
        Range range2 = Range.of(2.0, 14.0);
        assertThat(Ranges.overlap(range1, range2), equalTo(0.75));
    }
}
