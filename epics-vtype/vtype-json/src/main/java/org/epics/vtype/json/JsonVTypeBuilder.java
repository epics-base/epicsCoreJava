/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype.json;

import org.epics.util.array.ListBoolean;
import org.epics.util.array.ListNumber;
import org.epics.util.number.UByte;
import org.epics.util.number.UInteger;
import org.epics.util.number.ULong;
import org.epics.util.number.UShort;
import org.epics.vtype.*;

import javax.json.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import static org.epics.vtype.json.JsonArrays.fromListNumber;
import static org.epics.vtype.json.JsonArrays.fromListString;

/**
 * @author carcassi
 */
class JsonVTypeBuilder implements JsonObjectBuilder {

    public static final JsonBuilderFactory factory = Json.createBuilderFactory(Collections.<String, Object>emptyMap());
    private final JsonObjectBuilder builder = factory.createObjectBuilder();

    public JsonVTypeBuilder add(String string, JsonValue jv) {
        builder.add(string, jv);
        return this;
    }

    public JsonVTypeBuilder add(String string, String string1) {
        builder.add(string, string1);
        return this;
    }

    public JsonVTypeBuilder add(String string, BigInteger bi) {
        builder.add(string, bi);
        return this;
    }

    public JsonVTypeBuilder add(String string, BigDecimal bd) {
        builder.add(string, bd);
        return this;
    }

    public JsonVTypeBuilder add(String string, int i) {
        builder.add(string, i);
        return this;
    }

    public JsonVTypeBuilder add(String string, long l) {
        builder.add(string, l);
        return this;
    }

    public JsonVTypeBuilder add(String string, double d) {
        if (Double.isNaN(d)) {
            builder.add(string, VTypeJsonMapper.NAN);
        } else if (Double.valueOf(d).equals(Double.POSITIVE_INFINITY)) {
            builder.add(string, VTypeJsonMapper.POS_INF);
        } else if (Double.valueOf(d).equals(Double.NEGATIVE_INFINITY)) {
            builder.add(string, VTypeJsonMapper.NEG_INF);
        } else if (!Double.isInfinite(d)) {
            builder.add(string, d);
        }
        return this;
    }

    public JsonVTypeBuilder addIgnoreNaN(String string, double d) {
        if (!java.lang.Double.isNaN(d)) {
            builder.add(string, d);
        }
        return this;
    }

    public JsonVTypeBuilder add(String string, boolean bln) {
        builder.add(string, bln);
        return this;
    }

    public JsonVTypeBuilder addNull(String string) {
        builder.addNull(string);
        return this;
    }

    public JsonVTypeBuilder add(String string, JsonObjectBuilder job) {
        builder.add(string, job);
        return this;
    }

    public JsonVTypeBuilder add(String string, JsonArrayBuilder jab) {
        builder.add(string, jab);
        return this;
    }

    public JsonObject build() {
        return builder.build();
    }

    public JsonVTypeBuilder addAlarm(Alarm alarm) {
        return add("alarm", new JsonVTypeBuilder()
                .add("severity", alarm.getSeverity().toString())
                .add("status", alarm.getStatus().toString())
                .add("name", alarm.getName()));
    }

    public JsonVTypeBuilder addTime(Time time) {
        return add("time", new JsonVTypeBuilder()
                .add("unixSec", time.getTimestamp().getMillis() / 1000L)
                .add("nanoSec", (time.getTimestamp().getMillis() % 1000L) * 1000000L)
                .addNullableObject("userTag", time.getUserTag()));
    }

    public JsonVTypeBuilder addDisplay(Display display) {
        return add("display", new JsonVTypeBuilder()
                .addIgnoreNaN("lowAlarm", display.getAlarmRange().getMinimum())
                .addIgnoreNaN("highAlarm", display.getAlarmRange().getMaximum())
                .addIgnoreNaN("lowDisplay", display.getDisplayRange().getMinimum())
                .addIgnoreNaN("highDisplay", display.getDisplayRange().getMaximum())
                .addIgnoreNaN("lowWarning", display.getWarningRange().getMinimum())
                .addIgnoreNaN("highWarning", display.getWarningRange().getMaximum())
                .add("units", display.getUnit()));
    }

    public JsonVTypeBuilder addEnum(VEnum en) {
        return add("enum", new JsonVTypeBuilder()
                .addListString("labels", en.getDisplay().getChoices()));
    }

    public JsonVTypeBuilder addListString(String string, List<String> ls) {
        add(string, fromListString(ls));
        return this;
    }

    public JsonVTypeBuilder addListColumnType(String string, List<Class<?>> ls) {
        JsonArrayBuilder b = factory.createArrayBuilder();
        for (Class<?> element : ls) {
            if (element.equals(String.class)) {
                b.add("String");
            } else if (element.equals(double.class)) {
                b.add("double");
            } else if (element.equals(float.class)) {
                b.add("float");
            } else if (element.equals(long.class)) {
                b.add("long");
            } else if (element.equals(int.class)) {
                b.add("int");
            } else if (element.equals(short.class)) {
                b.add("short");
            } else if (element.equals(byte.class)) {
                b.add("byte");
            } else if (element.equals(Timestamp.class)) {
                b.add("Timestamp");
            } else {
                throw new IllegalArgumentException("Column type " + element + " not supported");
            }
        }
        add(string, b);
        return this;
    }

    public JsonVTypeBuilder addListNumber(String string, ListNumber ln) {
        add(string, fromListNumber(ln));
        return this;
    }

    public JsonVTypeBuilder addListBoolean(String string, ListBoolean lb) {
        JsonArrayBuilder b = factory.createArrayBuilder();
        for (int i = 0; i < lb.size(); i++) {
            b.add(lb.getBoolean(i));
        }
        add(string, b);
        return this;
    }

    public JsonVTypeBuilder addNullableObject(String string, Object o) {
        if (o != null) {
            addObject(string, o);
        }
        return this;
    }

    public JsonVTypeBuilder addObject(String string, Object o) {
        if (o == null) {
            return this;
        }

        if (o instanceof java.lang.Double || o instanceof Float) {
            add(string, ((Number) o).doubleValue());
        } else if (o instanceof Integer || o instanceof UShort || o instanceof Short || o instanceof UByte || o instanceof Byte) {
            add(string, ((Number) o).intValue());
        } else if (o instanceof Long || o instanceof UInteger) {
            add(string, ((Number) o).longValue());
        } else if (o instanceof ULong) {
            add(string, ((ULong) o).bigIntegerValue());
        } else if (o instanceof ListNumber) {
            addListNumber(string, (ListNumber) o);
        } else if (o instanceof ListBoolean) {
            addListBoolean(string, (ListBoolean) o);
        } else {
            throw new UnsupportedOperationException("Class " + o.getClass() + " not supported");
        }

        return this;
    }

    public JsonVTypeBuilder addType(VType vType) {
        Class<?> clazz = VType.typeOf(vType);
        return add("type", new JsonVTypeBuilder()
                .add("name", clazz.getSimpleName())
                .add("version", 1));
    }
}
