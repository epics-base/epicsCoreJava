/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.epics.vtype.gson.GsonArrays.*;

/**
 *
 * @author carcassi
 */
public class GsonArraysTest {

    public String integerArray = "[0,1,2]";
    public String doubleArray = "[0.0,0.1,0.2]";
    public String stringArray = "[\"A\",\"B\",\"C\"]";
    public String mixedArray = "[\"A\",1,\"C\"]";

    public JsonArray parseJson(String json) {
        return JsonParser.parseString(json).getAsJsonArray();
    }

    @Test
    public void isNumericArray1() {
        assertThat(isNumericArray(parseJson(integerArray)), equalTo(true));
        assertThat(isNumericArray(parseJson(doubleArray)), equalTo(true));
        assertThat(isNumericArray(parseJson(stringArray)), equalTo(false));
        assertThat(isNumericArray(parseJson(mixedArray)), equalTo(false));
    }

    @Test
    public void isStringArray1() {
        assertThat(isStringArray(parseJson(integerArray)), equalTo(false));
        assertThat(isStringArray(parseJson(doubleArray)), equalTo(false));
        assertThat(isStringArray(parseJson(stringArray)), equalTo(true));
        assertThat(isStringArray(parseJson(mixedArray)), equalTo(false));
    }
}
