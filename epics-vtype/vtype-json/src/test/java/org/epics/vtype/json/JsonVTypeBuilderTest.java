package org.epics.vtype.json;

import org.junit.Test;

import javax.json.JsonObject;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class JsonVTypeBuilderTest {

    @Test
    public void testAddIgnoreNanAndInfinity(){
        JsonVTypeBuilder jsonVTypeBuilder = new JsonVTypeBuilder();
        jsonVTypeBuilder.addIgnoreNaNAndInfinity("thisIsOk", 7.7);
        JsonObject jsonObject = jsonVTypeBuilder.build();
        assertNotNull(jsonObject.get("thisIsOk"));

        jsonVTypeBuilder.addIgnoreNaNAndInfinity("thisIsOkToo", 42);
        jsonObject = jsonVTypeBuilder.build();
        assertNotNull(jsonObject.get("thisIsOkToo"));

        jsonVTypeBuilder.addIgnoreNaNAndInfinity("NaN", Double.NaN);
        jsonObject =  jsonVTypeBuilder.build();
        assertNull(jsonObject.get("NaN"));

        jsonVTypeBuilder.addIgnoreNaNAndInfinity("NegativeInfinity", Double.NEGATIVE_INFINITY);
        jsonObject = jsonVTypeBuilder.build();
        assertNull(jsonObject.get("NegativeInfinity"));

        jsonVTypeBuilder.addIgnoreNaNAndInfinity("PositiveInfinity", Double.POSITIVE_INFINITY);
        jsonObject = jsonVTypeBuilder.build();
        assertNull(jsonObject.get("PositiveInfinity"));
    }
}
