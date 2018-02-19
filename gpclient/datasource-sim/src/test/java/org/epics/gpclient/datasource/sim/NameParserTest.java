/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource.sim;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.epics.util.stats.Range;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Test simulated pv function names parsing
 *
 * @author carcassi
 */
public class NameParserTest {

    public NameParserTest() {
    }

    @Test
    public void testParameterParsing() {
        // A couple of correct combinations
        List<Object> parameters = NameParser.parseParameters("1.0,2.0");
        assertThat(parameters, equalTo(Arrays.asList((Object) 1.0, 2.0)));
        parameters = NameParser.parseParameters("-1,.5,  23.25");
        assertThat(parameters, equalTo(Arrays.asList((Object) (-1.0), 0.5,  23.25)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testError1() {
        NameParser.parseParameters("1.0 2.0");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testError2() {
        NameParser.parseParameters("1.O");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testError3() {
        NameParser.parseParameters("1.1.2");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testError4() {
        NameParser.parseFunction("test(1.0 2.0)");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testError5() {
        NameParser.parseFunction("test(1.O)");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testError6() {
        NameParser.parseFunction("test(1.1.2)");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testError7() {
        NameParser.parseFunction("test1.2");
    }

    @Test
    public void testParsing() {
        // Couple of correct functions
        List<Object> parameters = NameParser.parseFunction("sine(1.0,2.0)");
        assertThat(parameters, equalTo(Arrays.asList((Object) "sine",  1.0, 2.0)));
        parameters = NameParser.parseFunction("ramp(-1,.5,  23.25)");
        assertThat(parameters, equalTo(Arrays.asList((Object) "ramp", -1.0, 0.5,  23.25)));
        parameters = NameParser.parseFunction("replay(\"test.xml\")");
        assertThat(parameters, equalTo(Arrays.asList((Object) "replay", "test.xml")));
    }

    @Test
    public void testRamp() {
        Ramp ramp = (Ramp) NameParser.createFunction("ramp(1.0, 10.0, 1.0, 1.0)");
        assertThat(ramp.display.getDisplayRange(), equalTo(Range.of(1, 10)));
        assertThat(ramp.step, equalTo(1.0));
        assertThat(ramp.getTimeBetweenSamples(), equalTo(Duration.ofSeconds(1)));

    }

    @Test
    public void testSine() {
        Sine sine = (Sine) NameParser.createFunction("sine(0.0, 10.0, 4.0, 1.0)");
        assertThat(sine.display.getDisplayRange(), equalTo(Range.of(0, 10)));
        assertThat(sine.samplesPerCycle, equalTo(4.0));
        assertThat(sine.getTimeBetweenSamples(), equalTo(Duration.ofSeconds(1)));
    }

    @Test
    public void testNoise() {
        Noise noise1 = (Noise) NameParser.createFunction("noise(0.0, 10.0, 1.0)");
        assertThat(noise1.display.getDisplayRange(), equalTo(Range.of(0, 10)));
        assertThat(noise1.getTimeBetweenSamples(), equalTo(Duration.ofSeconds(1)));
        
        Noise noise2 = (Noise) NameParser.createFunction("noise");
        assertThat(noise2.display.getDisplayRange(), equalTo(Range.of(-5, 5)));
        assertThat(noise2.getTimeBetweenSamples(), equalTo(Duration.ofMillis(500)));
    }

    @Test
    public void gaussianNoise() {
        GaussianNoise noise1 = (GaussianNoise) NameParser.createFunction("gaussianNoise(0.0, 10.0, 1.0)");
        assertThat(noise1.display.getDisplayRange(), equalTo(Range.of(-40, 40)));
        assertThat(noise1.getTimeBetweenSamples(), equalTo(Duration.ofSeconds(1)));

        GaussianNoise noise2 = (GaussianNoise) NameParser.createFunction("gaussianNoise");
        assertThat(noise2.display.getDisplayRange(), equalTo(Range.of(-4, 4)));
        assertThat(noise2.getTimeBetweenSamples(), equalTo(Duration.ofMillis(500)));
        
        GaussianNoise noise3 = (GaussianNoise) NameParser.createFunction("gaussianNoise()");
        assertThat(noise3.display.getDisplayRange(), equalTo(Range.of(-4, 4)));
        assertThat(noise3.getTimeBetweenSamples(), equalTo(Duration.ofMillis(500)));
    }

}