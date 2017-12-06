/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.json;

import java.util.List;
import javax.json.JsonObject;
import org.epics.util.array.ListNumber;
import org.epics.vtype.EnumMetaData;
import org.epics.vtype.VEnum;
import org.epics.vtype.VNumber;
import org.epics.vtype.VNumberArray;
import org.epics.vtype.VString;
import org.epics.vtype.VType;

/**
 * 
 * @author carcassi
 */
class VTypeToJsonV1 {

    static VType toVType(JsonObject json) {
        switch(typeNameOf(json)) {
            case "VDouble":
            case "VFloat":
            case "VLong":
            case "VInt":
            case "VShort":
            case "VByte":
                return toVNumber(json);
            case "VDoubleArray":
            case "VFloatArray":
            case "VLongArray":
            case "VIntArray":
            case "VShortArray":
            case "VByteArray":
                return toVNumberArray(json);
            case "VString":
                return toVString(json);
            case "VEnum":
                return toVEnum(json);
            default:
                throw new UnsupportedOperationException("Not implemented yet");
        }
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
        } else if (vType instanceof VEnum) {
            return toJson((VEnum) vType);
        }
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    static VNumber toVNumber(JsonObject json) {
        VTypeJsonMapper mapper = new VTypeJsonMapper(json);
        Number value;
        switch(mapper.getTypeName()) {
            case "VDouble":
                value = mapper.getJsonNumber("value").doubleValue();
                break;
            case "VFloat":
                value = (float) mapper.getJsonNumber("value").doubleValue();
                break;
            case "VLong":
                value = (long) mapper.getJsonNumber("value").longValue();
                break;
            case "VInt":
                value = (int) mapper.getJsonNumber("value").intValue();
                break;
            case "VShort":
                value = (short) mapper.getJsonNumber("value").intValue();
                break;
            case "VByte":
                value = (byte) mapper.getJsonNumber("value").intValue();
                break;
            default:
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
        return VEnum.of(mapper.getInt("value"), EnumMetaData.create(labels), mapper.getAlarm(), mapper.getTime());
    }
    
    static VNumberArray toVNumberArray(JsonObject json) {
        VTypeJsonMapper mapper = new VTypeJsonMapper(json);
        ListNumber value;
        switch(mapper.getTypeName()) {
            case "VDoubleArray":
                value = mapper.getListDouble("value");
                break;
            case "VFloatArray":
                value = mapper.getListFloat("value");
                break;
            case "VLongArray":
                value = mapper.getListLong("value");
                break;
            case "VIntArray":
                value = mapper.getListInt("value");
                break;
            case "VShortArray":
                value = mapper.getListShort("value");
                break;
            case "VByteArray":
                value = mapper.getListByte("value");
                break;
            default:
                throw new UnsupportedOperationException("Not implemented yet");
        }
        return VNumberArray.create(value, mapper.getAlarm(), mapper.getTime(), mapper.getDisplay());
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
}
