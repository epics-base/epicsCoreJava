/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype.gson;

import com.google.gson.JsonElement;
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

import java.util.List;

/**
 * Gson adapted VTypeToJson
 *
 * @author <a href="mailto:changj@frib.msu.edu">Genie Jhang</a>
 *
 * Original author:
 * @author carcassi
 */
public class VTypeToGson {

    static VType toVType(JsonElement json) {
        switch(typeNameOf(json)) {
            case "VDouble":
            case "VFloat":
            case "VULong":
            case "VLong":
            case "VUInt":
            case "VInt":
            case "VUShort":
            case "VShort":
            case "VUByte":
            case "VByte":
                return toVNumber(json);
            case "VDoubleArray":
            case "VFloatArray":
            case "VULongArray":
            case "VLongArray":
            case "VUIntArray":
            case "VIntArray":
            case "VUShortArray":
            case "VShortArray":
            case "VUByteArray":
            case "VByteArray":
                return toVNumberArray(json);
            case "VString":
                return toVString(json);
            case "VStringArray":
                return toVStringArray(json);
            case "VEnum":
                return toVEnum(json);
            default:
                throw new UnsupportedOperationException("Not implemented yet");
        }
    }
    
    static String typeNameOf(JsonElement json) {
        JsonElement type = json.getAsJsonObject().get("type");
        if (type == null) {
            return null;
        }
        return type.getAsJsonObject().get("name").getAsString();
    }
    
    static JsonElement toJson(VType vType) {
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
    
    static VNumber toVNumber(JsonElement json) {
        VTypeGsonMapper mapper = new VTypeGsonMapper(json.getAsJsonObject());
        Number value;
        switch(mapper.getTypeName()) {
            case "VDouble":
                Double doubleValue = json.getAsJsonObject().get("value").getAsDouble();
                if(doubleValue != null){
                    return VDouble.of(doubleValue, mapper.getAlarm(), mapper.getTime(), mapper.getDisplay());
                }
                value = mapper.getJsonNumber("value").getAsDouble();
                break;
            case "VFloat":
                value = (float) mapper.getJsonNumber("value").getAsDouble();
                break;
            case "VULong":
                value = new ULong(mapper.getJsonNumber("value").getAsLong());
                break;
            case "VLong":
                value = (long) mapper.getJsonNumber("value").getAsLong();
                break;
            case "VUInt":
                value = new UInteger(mapper.getJsonNumber("value").getAsInt());
                break;
            case "VInt":
                value = (int) mapper.getJsonNumber("value").getAsInt();
                break;
            case "VUShort":
                value = new UShort((short) mapper.getJsonNumber("value").getAsInt());
                break;
            case "VShort":
                value = (short) mapper.getJsonNumber("value").getAsInt();
                break;
            case "VUByte":
                value = new UByte((byte) mapper.getJsonNumber("value").getAsInt());
                break;
            case "VByte":
                value = (byte) mapper.getJsonNumber("value").getAsInt();
                break;
            default:
                throw new UnsupportedOperationException("Not implemented yet");
        }
        return VNumber.of(value, mapper.getAlarm(), mapper.getTime(), mapper.getDisplay());
    }
    
    static VString toVString(JsonElement json) {
        VTypeGsonMapper mapper = new VTypeGsonMapper(json.getAsJsonObject());
        return VString.of(mapper.getString("value"), mapper.getAlarm(), mapper.getTime());
    }

    static VStringArray toVStringArray(JsonElement json) {
        VTypeGsonMapper mapper = new VTypeGsonMapper(json.getAsJsonObject());
        return VStringArray.of(mapper.getListString("value"), mapper.getAlarm(), mapper.getTime());
        
    }
    
    static VEnum toVEnum(JsonElement json) {
        VTypeGsonMapper mapper = new VTypeGsonMapper(json.getAsJsonObject());
        List<String> labels = mapper.getJsonObject("enum").getListString("labels");
        return VEnum.of(mapper.getInt("value"), EnumDisplay.of(labels), mapper.getAlarm(), mapper.getTime());
    }
    
    static VNumberArray toVNumberArray(JsonElement json) {
        VTypeGsonMapper mapper = new VTypeGsonMapper(json.getAsJsonObject());
        ListNumber value;
        switch(mapper.getTypeName()) {
            case "VDoubleArray":
                value = mapper.getListDouble("value");
                break;
            case "VFloatArray":
                value = mapper.getListFloat("value");
                break;
            case "VULongArray":
                value = mapper.getListULong("value");
                break;
            case "VLongArray":
                value = mapper.getListLong("value");
                break;
            case "VUIntArray":
                value = mapper.getListUInteger("value");
                break;
            case "VIntArray":
                value = mapper.getListInt("value");
                break;
            case "VUShortArray":
                value = mapper.getListUShort("value");
                break;
            case "VShortArray":
                value = mapper.getListShort("value");
                break;
            case "VUByteArray":
                value = mapper.getListUByte("value");
                break;
            case "VByteArray":
                value = mapper.getListByte("value");
                break;
            default:
                throw new UnsupportedOperationException("Not implemented yet");
        }
        return VNumberArray.of(value, mapper.getAlarm(), mapper.getTime(), mapper.getDisplay());
    }
    
    static JsonElement toJson(VNumber vNumber) {
        return new GsonVTypeBuilder()
                .addType(vNumber)
                .addObject("value", vNumber.getValue())
                .addAlarm(vNumber.getAlarm())
                .addTime(vNumber.getTime())
                .addDisplay(vNumber.getDisplay())
                .build();
    }
    
    static JsonElement toJson(VNumberArray vNumberArray) {
        return new GsonVTypeBuilder()
                .addType(vNumberArray)
                .addObject("value", vNumberArray.getData())
                .addAlarm(vNumberArray.getAlarm())
                .addTime(vNumberArray.getTime())
                .addDisplay(vNumberArray.getDisplay())
                .build();
    }
    
    static JsonElement toJson(VString vString) {
        return new GsonVTypeBuilder()
                .addType(vString)
                .add("value", vString.getValue())
                .addAlarm(vString.getAlarm())
                .addTime(vString.getTime())
                .build();
    }

    static JsonElement toJson(VStringArray vStringArray) {
        return new GsonVTypeBuilder()
                .addType(vStringArray)
                .addListString("value", vStringArray.getData())
                .addAlarm(vStringArray.getAlarm())
                .addTime(vStringArray.getTime())
                .build();
    }

    static JsonElement toJson(VEnum vEnum) {
        return new GsonVTypeBuilder()
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
     * @param valueAsString
     * @return A valid double number, otherwise
     * {@link Double#NaN}, {@link Double#POSITIVE_INFINITY} and {@link Double#NEGATIVE_INFINITY}.
     */
    public static Double getDoubleFromJsonString(String valueAsString){
        if(VTypeGsonMapper.NAN_QUOTED.equals(valueAsString)){
            return Double.NaN;
        }
        else if(VTypeGsonMapper.POS_INF_QUOTED.equals(valueAsString)){
            return Double.POSITIVE_INFINITY;
        }
        else if(VTypeGsonMapper.NEG_INF_QUOTED.equals(valueAsString)){
            return Double.NEGATIVE_INFINITY;
        }
        else{
            return Double.parseDouble(valueAsString);
        }
    }
}
