/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype.json;

import java.util.List;
import javax.json.JsonObject;
import org.epics.util.array.ListNumber;
import org.epics.util.number.UByte;
import org.epics.util.number.UInteger;
import org.epics.util.number.ULong;
import org.epics.util.number.UShort;
import org.epics.vtype.EnumDisplay;
import org.epics.vtype.VDouble;
import org.epics.vtype.VEnum;
import org.epics.vtype.VNumber;
import org.epics.vtype.VNumberArray;
import org.epics.vtype.VString;
import org.epics.vtype.VStringArray;
import org.epics.vtype.VType;

/**
 *
 * @author carcassi
 */
class VTypeToJsonV1 {

    static VType toVType(JsonObject json) {
        String s = typeNameOf(json);
        if ("VDouble".equals(s) || "VFloat".equals(s) || "VULong".equals(s) || "VLong".equals(s) || "VUInt".equals(s) || "VInt".equals(s) || "VUShort".equals(s) || "VShort".equals(s) || "VUByte".equals(s) || "VByte".equals(s)) {
            return toVNumber(json);
        } else if ("VDoubleArray".equals(s) || "VFloatArray".equals(s) || "VULongArray".equals(s) || "VLongArray".equals(s) || "VUIntArray".equals(s) || "VIntArray".equals(s) || "VUShortArray".equals(s) || "VShortArray".equals(s) || "VUByteArray".equals(s) || "VByteArray".equals(s)) {
            return toVNumberArray(json);
        } else if ("VString".equals(s)) {
            return toVString(json);
        } else if ("VStringArray".equals(s)) {
            return toVStringArray(json);
        } else if ("VEnum".equals(s)) {
            return toVEnum(json);
        }
        throw new UnsupportedOperationException("Not implemented yet");
    }

    static String typeNameOf(JsonObject json) {
        JsonObject type = json.getJsonObject("type");
        if (type == null) {
            return null;
        }
        return type.getString("name");
    }

    static JsonObject toJson(VType vType) {
        if (vType instanceof VNumber) {
            return toJson((VNumber) vType);
        } else if (vType instanceof VNumberArray) {
            return toJson((VNumberArray) vType);
        } else if (vType instanceof VString) {
            return toJson((VString) vType);
        } else if (vType instanceof VStringArray) {
            return toJson((VStringArray) vType);
        } else if (vType instanceof VEnum) {
            return toJson((VEnum) vType);
        }
        throw new UnsupportedOperationException("Not implemented yet");
    }

    static VNumber toVNumber(JsonObject json) {
        VTypeJsonMapper mapper = new VTypeJsonMapper(json);
        Number value;
        String typeName = mapper.getTypeName();
        if ("VDouble".equals(typeName)) {
            Double doubleValue = getDoubleFromJsonString(json.get("value").toString());
            if (doubleValue != null) {
                return VDouble.of(doubleValue, mapper.getAlarm(), mapper.getTime(), mapper.getDisplay());
            }
            value = mapper.getJsonNumber("value").doubleValue();
        } else if ("VFloat".equals(typeName)) {
            value = (float) mapper.getJsonNumber("value").doubleValue();
        } else if ("VULong".equals(typeName)) {
            value = new ULong(mapper.getJsonNumber("value").longValue());
        } else if ("VLong".equals(typeName)) {
            value = (long) mapper.getJsonNumber("value").longValue();
        } else if ("VUInt".equals(typeName)) {
            value = new UInteger(mapper.getJsonNumber("value").intValue());
        } else if ("VInt".equals(typeName)) {
            value = (int) mapper.getJsonNumber("value").intValue();
        } else if ("VUShort".equals(typeName)) {
            value = new UShort((short) mapper.getJsonNumber("value").intValue());
        } else if ("VShort".equals(typeName)) {
            value = (short) mapper.getJsonNumber("value").intValue();
        } else if ("VUByte".equals(typeName)) {
            value = new UByte((byte) mapper.getJsonNumber("value").intValue());
        } else if ("VByte".equals(typeName)) {
            value = (byte) mapper.getJsonNumber("value").intValue();
        } else {
            throw new UnsupportedOperationException("Not implemented yet");
        }
        return VNumber.of(value, mapper.getAlarm(), mapper.getTime(), mapper.getDisplay());
    }

    static VString toVString(JsonObject json) {
        VTypeJsonMapper mapper = new VTypeJsonMapper(json);
        return VString.of(mapper.getString("value"), mapper.getAlarm(), mapper.getTime());
    }

    static VEnum toVEnum(JsonObject json) {
        VTypeJsonMapper mapper = new VTypeJsonMapper(json);
        List<String> labels = mapper.getJsonObject("enum").getListString("labels");
        return VEnum.of(mapper.getInt("value"), EnumDisplay.of(labels), mapper.getAlarm(), mapper.getTime());
    }

    static VNumberArray toVNumberArray(JsonObject json) {
        VTypeJsonMapper mapper = new VTypeJsonMapper(json);
        ListNumber value;
        String typeName = mapper.getTypeName();
        if ("VDoubleArray".equals(typeName)) {
            value = mapper.getListDouble("value");
        } else if ("VFloatArray".equals(typeName)) {
            value = mapper.getListFloat("value");
        } else if ("VULongArray".equals(typeName)) {
            value = mapper.getListULong("value");
        } else if ("VLongArray".equals(typeName)) {
            value = mapper.getListLong("value");
        } else if ("VUIntArray".equals(typeName)) {
            value = mapper.getListUInteger("value");
        } else if ("VIntArray".equals(typeName)) {
            value = mapper.getListInt("value");
        } else if ("VUShortArray".equals(typeName)) {
            value = mapper.getListUShort("value");
        } else if ("VShortArray".equals(typeName)) {
            value = mapper.getListShort("value");
        } else if ("VUByteArray".equals(typeName)) {
            value = mapper.getListUByte("value");
        } else if ("VByteArray".equals(typeName)) {
            value = mapper.getListByte("value");
        } else {
            throw new UnsupportedOperationException("Not implemented yet");
        }
        return VNumberArray.of(value, mapper.getAlarm(), mapper.getTime(), mapper.getDisplay());
    }

    static VStringArray toVStringArray(JsonObject json) {
        VTypeJsonMapper mapper = new VTypeJsonMapper(json);
        return VStringArray.of(mapper.getListString("value"), mapper.getAlarm(), mapper.getTime());

    }

    static JsonObject toJson(VNumber vNumber) {
        return new JsonVTypeBuilder()
                .addType(vNumber)
                .addObject("value", vNumber.getValue())
                .addAlarm(vNumber.getAlarm())
                .addTime(vNumber.getTime())
                .addDisplay(vNumber.getDisplay())
                .build();
    }

    static JsonObject toJson(VNumberArray vNumberArray) {
        return new JsonVTypeBuilder()
                .addType(vNumberArray)
                .addObject("value", vNumberArray.getData())
                .addAlarm(vNumberArray.getAlarm())
                .addTime(vNumberArray.getTime())
                .addDisplay(vNumberArray.getDisplay())
                .build();
    }

    static JsonObject toJson(VStringArray vStringArray) {
        return new JsonVTypeBuilder()
                .addType(vStringArray)
                .addListString("value", vStringArray.getData())
                .addAlarm(vStringArray.getAlarm())
                .addTime(vStringArray.getTime())
                .build();
    }

    static JsonObject toJson(VString vString) {
        return new JsonVTypeBuilder()
                .addType(vString)
                .add("value", vString.getValue())
                .addAlarm(vString.getAlarm())
                .addTime(vString.getTime())
                .build();
    }

    static JsonObject toJson(VEnum vEnum) {
        return new JsonVTypeBuilder()
                .addType(vEnum)
                .add("value", vEnum.getIndex())
                .addAlarm(vEnum.getAlarm())
                .addTime(vEnum.getTime())
                .addEnum(vEnum)
                .build();
    }

    /**
     * Converts an input (JSON) string to a {@link Double}, taking into account the special cases
     * {@link Double#NaN}, {@link Double#POSITIVE_INFINITY} and {@link Double#NEGATIVE_INFINITY}.
     * If the input string is not parseable as a double (including the special cases), a
     * {@link NumberFormatException} is thrown.
     * @param valueAsString value as a string
     * @return A valid double number, otherwise
     * {@link Double#NaN}, {@link Double#POSITIVE_INFINITY} and {@link Double#NEGATIVE_INFINITY}.
     */
    public static Double getDoubleFromJsonString(String valueAsString){
        if(VTypeJsonMapper.NAN_QUOTED.equals(valueAsString)){
            return Double.NaN;
        }
        else if(VTypeJsonMapper.POS_INF_QUOTED.equals(valueAsString)){
            return Double.POSITIVE_INFINITY;
        }
        else if(VTypeJsonMapper.NEG_INF_QUOTED.equals(valueAsString)){
            return Double.NEGATIVE_INFINITY;
        }
        else{
            return Double.parseDouble(valueAsString);
        }
    }
}
