/*
 * Copyright (C) 2020 Facility for Rare Isotope Beams
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * <p>
 * Contact Information: Facility for Rare Isotope Beam,
 * Michigan State University,
 * East Lansing, MI 48824-1321
 * http://frib.msu.edu
 */
package org.epics.vtype.gson;

import com.google.gson.*;
import org.epics.util.array.ListBoolean;
import org.epics.util.array.ListNumber;
import org.epics.util.number.UByte;
import org.epics.util.number.UInteger;
import org.epics.util.number.ULong;
import org.epics.util.number.UShort;
import org.epics.vtype.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

import static org.epics.vtype.gson.GsonArrays.fromListNumber;
import static org.epics.vtype.gson.GsonArrays.fromListString;

/**
 * VType JsonObject creator for Gson
 *
 * @author <a href="mailto:changj@frib.msu.edu">Genie Jhang</a>
 *
 * Original author:
 * @author carcassi
 */
class GsonVTypeBuilder extends JsonElement {

    private final JsonObject jsonObject;

    public GsonVTypeBuilder() {
        jsonObject = new JsonObject();
    }

    public GsonVTypeBuilder add(String string, JsonElement jv) {
        jsonObject.add(string, jv);
        return this;
    }

    public GsonVTypeBuilder add(String string, String string1) {
        jsonObject.addProperty(string, string1);
        return this;
    }

    public GsonVTypeBuilder add(String string, BigInteger bi) {
        jsonObject.addProperty(string, bi);
        return this;
    }

    public GsonVTypeBuilder add(String string, BigDecimal bd) {
        jsonObject.addProperty(string, bd);
        return this;
    }

    public GsonVTypeBuilder add(String string, int i) {
        jsonObject.addProperty(string, i);
        return this;
    }

    public GsonVTypeBuilder add(String string, long l) {
        jsonObject.addProperty(string, l);
        return this;
    }

    public GsonVTypeBuilder add(String string, double d) {
        if (java.lang.Double.isNaN(d)) {
            jsonObject.addProperty(string, VTypeGsonMapper.NAN);
        } else if (java.lang.Double.valueOf(d).equals(java.lang.Double.POSITIVE_INFINITY)) {
            jsonObject.addProperty(string, VTypeGsonMapper.POS_INF);
        } else if (java.lang.Double.valueOf(d).equals(java.lang.Double.NEGATIVE_INFINITY)) {
            jsonObject.addProperty(string, VTypeGsonMapper.NEG_INF);
        } else if (!Double.isInfinite(d)) {
            jsonObject.addProperty(string, d);
        }
        return this;
    }

    public GsonVTypeBuilder addIgnoreNaN(String string, double d) {
        if (!java.lang.Double.isNaN(d)) {
            jsonObject.addProperty(string, d);
        }
        return this;
    }

    public GsonVTypeBuilder add(String string, boolean bln) {
        jsonObject.addProperty(string, bln);
        return this;
    }

    public GsonVTypeBuilder addNull(String string) {
        jsonObject.add(string, JsonNull.INSTANCE);
        return this;
    }

    public GsonVTypeBuilder add(String string, GsonVTypeBuilder job) {
        jsonObject.add(string, job);
        return this;
    }

    public GsonVTypeBuilder add(String string, JsonArray jab) {
        jsonObject.add(string, jab);
        return this;
    }

    public JsonObject build() {
        return jsonObject;
    }

    public GsonVTypeBuilder addAlarm(Alarm alarm) {
        return add("alarm", new GsonVTypeBuilder()
                .add("severity", alarm.getSeverity().toString())
                .add("status", alarm.getStatus().toString())
                .add("name", alarm.getName())
                .build());
    }

    public GsonVTypeBuilder addTime(Time time) {
        return add("time", new GsonVTypeBuilder()
                .add("unixSec", time.getTimestamp().getMillis() / 1000L)
                .add("nanoSec", time.getTimestamp().getMillis() / 1000000000L)
                .addNullableObject("userTag", time.getUserTag())
                .build());
    }

    public GsonVTypeBuilder addDisplay(Display display) {
        return add("display", new GsonVTypeBuilder()
                .addIgnoreNaN("lowAlarm", display.getAlarmRange().getMinimum())
                .addIgnoreNaN("highAlarm", display.getAlarmRange().getMaximum())
                .addIgnoreNaN("lowDisplay", display.getDisplayRange().getMinimum())
                .addIgnoreNaN("highDisplay", display.getDisplayRange().getMaximum())
                .addIgnoreNaN("lowWarning", display.getWarningRange().getMinimum())
                .addIgnoreNaN("highWarning", display.getWarningRange().getMaximum())
                .add("units", display.getUnit())
                .build());
    }

    public GsonVTypeBuilder addEnum(VEnum en) {
        return add("enum", new GsonVTypeBuilder()
                .addListString("labels", en.getDisplay().getChoices())
                .build());
    }

    public GsonVTypeBuilder addListString(String string, List<String> ls) {
        add(string, fromListString(ls));
        return this;
    }

    public GsonVTypeBuilder addListColumnType(String string, List<Class<?>> ls) {
        JsonArray b = new JsonArray();
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

    public GsonVTypeBuilder addListNumber(String string, ListNumber ln) {
        add(string, fromListNumber(ln));
        return this;
    }

    public GsonVTypeBuilder addListBoolean(String string, ListBoolean lb) {
        JsonArray b = new JsonArray();
        for (int i = 0; i < lb.size(); i++) {
            b.add(lb.getBoolean(i));
        }
        add(string, b);
        return this;
    }

    public GsonVTypeBuilder addNullableObject(String string, Object o) {
        if (o != null) {
            addObject(string, o);
        }
        return this;
    }

    public GsonVTypeBuilder addObject(String string, Object o) {
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

    public GsonVTypeBuilder addType(VType vType) {
        Class<?> clazz = VType.typeOf(vType);
        return add("type", new GsonVTypeBuilder()
                .add("name", clazz.getSimpleName())
                .add("version", 1)
                .build());
    }

    @Override
    public JsonElement deepCopy() {
        return null;
    }

    @Override
    public boolean isJsonObject() {
        return true;
    }

    @Override
    public JsonObject getAsJsonObject() {
        return jsonObject;
    }

    @Override
    public String toString() {
        return new Gson().toJson(jsonObject);
    }
}
