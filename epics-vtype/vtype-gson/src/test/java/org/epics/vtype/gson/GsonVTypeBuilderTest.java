package org.epics.vtype.gson;

import com.google.gson.JsonObject;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class GsonVTypeBuilderTest {

    @Test
    public void testAddIgnoreNanAndInfinity(){
        GsonVTypeBuilder gsonVTypeBuilder = new GsonVTypeBuilder();
        gsonVTypeBuilder.addIgnoreNaNAndInfinity("thisIsOk", 7.7);
        JsonObject jsonObject = gsonVTypeBuilder.build();
        assertNotNull(jsonObject.get("thisIsOk"));

        gsonVTypeBuilder.addIgnoreNaNAndInfinity("thisIsOkToo", 42);
        jsonObject = gsonVTypeBuilder.build();
        assertNotNull(jsonObject.get("thisIsOkToo"));
        assertNotNull(jsonObject.get("thisIsOk"));

        gsonVTypeBuilder.addIgnoreNaNAndInfinity("NaN", Double.NaN);
        gsonVTypeBuilder.build();
        assertNull(jsonObject.get("NaN"));

        gsonVTypeBuilder.addIgnoreNaNAndInfinity("NegativeInfinity", Double.NEGATIVE_INFINITY);
        gsonVTypeBuilder.build();
        assertNull(jsonObject.get("NegativeInfinity"));

        gsonVTypeBuilder.addIgnoreNaNAndInfinity("PositiveInfinity", Double.POSITIVE_INFINITY);
        gsonVTypeBuilder.build();
        assertNull(jsonObject.get("PositiveInfinity"));
    }
}
