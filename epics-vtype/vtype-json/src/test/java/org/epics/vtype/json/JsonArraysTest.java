/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype.json;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.epics.vtype.json.JsonArrays.*;

/**
 *
 * @author carcassi
 */
public class JsonArraysTest {

    public String integerArray = "[0,1,2]";
    public String doubleArray = "[0.0,0.1,0.2]";
    public String stringArray = "[\"A\",\"B\",\"C\"]";
    public String mixedArray = "[\"A\",1,\"C\"]";

    public JsonArray parseJson(String json) {
        JsonReader reader = null;
        try {
            reader = Json.createReader(new StringReader(json));
            return reader.readArray();
        } finally {
            if ( reader != null ) {
                reader.close();
            }
        }
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
