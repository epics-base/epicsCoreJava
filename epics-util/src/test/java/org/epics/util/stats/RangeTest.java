/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.stats;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class RangeTest {

    @Test
    public void range1() throws Exception {
        Range range = Range.of(0.0, 10.0);
        assertThat(range.getMinimum(), equalTo(0.0));
        assertThat(range.getMaximum(), equalTo(10.0));
        assertThat(range.isReversed(), equalTo(false));
        assertThat(range.toString(), equalTo("[0.0 - 10.0]"));
    }

    @Test
    public void range2() throws Exception {
        Range range = Range.of(0.0, 0.0);
        assertThat(range.getMinimum(), equalTo(0.0));
        assertThat(range.getMaximum(), equalTo(0.0));
        assertThat(range.isReversed(), equalTo(false));
        assertThat(range.toString(), equalTo("[0.0 - 0.0]"));
    }

    @Test
    public void range3() throws Exception {
        Range range = Range.of(10.0, 0.0);
        assertThat(range.getMinimum(), equalTo(0.0));
        assertThat(range.getMaximum(), equalTo(10.0));
        assertThat(range.isReversed(), equalTo(true));
        assertThat(range.toString(), equalTo("[10.0 - 0.0]"));
    }

    @Test
    public void range4() throws Exception {
        Range range = Range.of(0.0, Double.NaN);
        assertThat(range, sameInstance(Range.undefined()));
    }

    @Test
    public void equal1() throws Exception {
        assertThat(Range.of(0.0, 10.0), equalTo(Range.of(0.0, 10.0)));
        assertThat(Range.of(10.0, 0.0), not(equalTo(Range.of(0.0, 10.0))));
        assertThat(Range.of(10.0, 0.0), not(equalTo(Range.of(1.0, 10.0))));
        assertThat(Range.of(10.0, 0.0), not(equalTo(null)));
        assertThat(Range.undefined(), equalTo(Range.undefined()));
    }

    @Test
    public void isFinite1() {
        Range range1 = Range.of(0.0, 8.0);
        assertThat(range1.isFinite(), equalTo(true));
    }

    @Test
    public void isFinite2() {
        Range range1 = Range.of(5.0, 5.0);
        assertThat(range1.isFinite(), equalTo(false));
    }

    @Test
    public void isFinite3() {
        Range range1 = Range.of(Double.NaN, 8.0);
        assertThat(range1.isFinite(), equalTo(false));
    }

    @Test
    public void isFinite4() {
        Range range1 = Range.of(Double.NEGATIVE_INFINITY, 8.0);
        assertThat(range1.isFinite(), equalTo(false));
    }

    @Test
    public void isFinite5() {
        Range range1 = Range.of(0.0, Double.NaN);
        assertThat(range1.isFinite(), equalTo(false));
    }

    @Test
    public void isFinite6() {
        Range range1 = Range.of(0.0, Double.POSITIVE_INFINITY);
        assertThat(range1.isFinite(), equalTo(false));
    }

    @Test
    public void normalize1() {
        Range range = Range.of(-10.0, 10.0);
        assertThat(range.normalize(0.0), equalTo(0.5));
    }

    @Test
    public void normalize2() {
        Range range = Range.of(-10.0, 10.0);
        assertThat(range.normalize(10.0), equalTo(1.0));
    }

    @Test
    public void normalize3() {
        Range range = Range.of(-10.0, 10.0);
        assertThat(range.normalize(-10.0), equalTo(0.0));
    }

    @Test
    public void contains1() {
        Range range = Range.of(-10.0, 10.0);
        assertThat(range.contains(5.0), equalTo(true));
    }

    @Test
    public void contains2() {
        Range range = Range.of(-10.0, 10.0);
        assertThat(range.contains(7.5), equalTo(true));
    }

    @Test
    public void contains3() {
        Range range = Range.of(-10.0, 10.0);
        assertThat(range.contains(25.0), equalTo(false));
    }

    @Test
    public void contains4() {
        Range range = Range.of(-10.0, 10.0);
        assertThat(range.contains(-25.0), equalTo(false));
    }

    @Test
    public void containsRange1() {
        assertThat(Range.of(0.0, 1.0).contains(Range.of(0.5, 0.75)), equalTo(true));
        assertThat(Range.of(0.0, 1.0).contains(Range.of(0.5, 1.0)), equalTo(true));
        assertThat(Range.of(0.0, 1.0).contains(Range.of(0.0, 0.75)), equalTo(true));
        assertThat(Range.of(0.0, 1.0).contains(Range.of(-1.0, 0.75)), equalTo(false));
        assertThat(Range.of(0.0, 1.0).contains(Range.of(0.0, 1.75)), equalTo(false));
    }

    @Test
    public void combine1() {
        Range range1 = Range.of(0.0, 5.0);
        Range range2 = Range.of(1.0, 2.0);
        assertThat(range1.combine(range2), sameInstance(range1));
        assertThat(range2.combine(range1), sameInstance(range1));
    }

    @Test
    public void combine2() {
        Range range1 = Range.of(0.0, 5.0);
        Range range2 = Range.of(1.0, 6.0);
        Range range = range1.combine(range2);
        assertThat(range.getMinimum(), equalTo((Number) 0.0));
        assertThat(range.getMaximum(), equalTo((Number) 6.0));
        range = range2.combine(range1);
        assertThat(range.getMinimum(), equalTo((Number) 0.0));
        assertThat(range.getMaximum(), equalTo((Number) 6.0));
    }

    @Test
    public void combine3() {
        Range range1 = Range.of(0.0, 3.0);
        Range range2 = Range.of(4.0, 6.0);
        Range range = range1.combine(range2);
        assertThat(range.getMinimum(), equalTo((Number) 0.0));
        assertThat(range.getMaximum(), equalTo((Number) 6.0));
        range = range2.combine(range1);
        assertThat(range.getMinimum(), equalTo((Number) 0.0));
        assertThat(range.getMaximum(), equalTo((Number) 6.0));
    }

    @Test
    public void combine4() {
        Range range1 = Range.of(0.0, 3.0);
        Range range2 = Range.of(0.0, 6.0);
        Range range = range1.combine(range2);
        assertThat(range.getMinimum(), equalTo((Number) 0.0));
        assertThat(range.getMaximum(), equalTo((Number) 6.0));
        range = range2.combine(range1);
        assertThat(range.getMinimum(), equalTo((Number) 0.0));
        assertThat(range.getMaximum(), equalTo((Number) 6.0));
    }

    @Test
    public void combine5() {
        Range range1 = Range.of(0.0, 6.0);
        Range range2 = Range.of(3.0, 6.0);
        Range range = range1.combine(range2);
        assertThat(range.getMinimum(), equalTo((Number) 0.0));
        assertThat(range.getMaximum(), equalTo((Number) 6.0));
        range = range2.combine(range1);
        assertThat(range.getMinimum(), equalTo((Number) 0.0));
        assertThat(range.getMaximum(), equalTo((Number) 6.0));
    }
    @Test
    public void combine6() {
        Range range1 = Range.undefined();
        Range range2 = Range.of(1.0, 2.0);
        assertThat(range1.combine(range2), sameInstance(range2));
        assertThat(range2.combine(range1), sameInstance(range2));
    }

    @Test
    public void shrink1() {
        assertThat(Range.of(-10, 10).shrink(0.5), equalTo(Range.of(-5, 5)));
        assertThat(Range.of(0, 128).shrink(0.125), equalTo(Range.of(56, 72)));
        assertThat(Range.of(0, 100).shrink(0.1), equalTo(Range.of(45, 55)));
        assertThat(Range.of(0, 100).shrink(0), equalTo(Range.of(50, 50)));
        assertThat(Range.of(0, 100).shrink(-1), equalTo(Range.of(100, 0)));
        assertThat(Range.of(0, 200).shrink(2), equalTo(Range.of(-100, 300)));
        assertThat(Range.undefined().shrink(10), equalTo(Range.undefined()));
    }

    @Test
    public void rescale1() {
        assertThat(Range.of(0, 10).rescale(0.0), equalTo(00.0));
        assertThat(Range.of(0, 10).rescale(0.5), equalTo(5.0));
        assertThat(Range.of(0, 10).rescale(1.0), equalTo(10.0));
        assertThat(Range.of(-10, 10).rescale(0.0), equalTo(-10.0));
        assertThat(Range.of(-10, 10).rescale(0.5), equalTo(0.0));
        assertThat(Range.of(-10, 10).rescale(1.0), equalTo(10.0));
    }
}
