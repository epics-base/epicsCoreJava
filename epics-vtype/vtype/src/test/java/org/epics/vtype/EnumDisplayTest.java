/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import java.util.Arrays;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class EnumDisplayTest {

    @Test
    public void equals1() {
        assertThat(EnumDisplay.of("A", "B", "C", "D"), equalTo(EnumDisplay.of("A", "B", "C", "D")));
        assertThat(EnumDisplay.of("A", "B", "C", "D"), not(equalTo(EnumDisplay.of("1", "2", "3", "4"))));
    }

    @Test
    public void hashCode1() {
        assertThat(EnumDisplay.of("A", "B", "C", "D").hashCode(), equalTo(EnumDisplay.of("A", "B", "C", "D").hashCode()));
        assertThat(EnumDisplay.of("A", "B", "C", "D").hashCode(), not(equalTo(EnumDisplay.of("1", "2", "3", "4").hashCode())));
    }

    @Test
    public void of1() {
        EnumDisplay enumDisplay = EnumDisplay.of("A", "B", "C", "D");
        assertThat(enumDisplay.getChoices(), equalTo(Arrays.asList("A", "B", "C", "D")));
    }

    @Test
    public void of2() {
        EnumDisplay enumDisplay = EnumDisplay.of(Arrays.asList("A", "B", "C", "D"));
        assertThat(enumDisplay.getChoices(), equalTo(Arrays.asList("A", "B", "C", "D")));
    }

    @Test
    public void of3() {
        EnumDisplay enumDisplay = EnumDisplay.of(5);
        assertThat(enumDisplay.getChoices(), equalTo(Arrays.asList("0", "1", "2", "3", "4")));
    }

    @Test
    public void toString1() {
        assertThat(EnumDisplay.of("A", "B", "C", "D").toString(), equalTo("[A, B, C, D]"));
    }
}
