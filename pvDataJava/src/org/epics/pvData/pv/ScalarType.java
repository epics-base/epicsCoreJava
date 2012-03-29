/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * Process Variable Scalar Data Type.
 * @author mrk
 *
 */
public enum ScalarType {
    /**
     * Value has type <i>boolean</i>.
     */
    pvBoolean,
    /**
     * Value has type <i>byte</i>.
     */
    pvByte,
    /**
     * Value has type <i>short</i>.
     */
    pvShort,
    /**
     * Value has type <i>int</i>.
     */
    pvInt,
    /**
     * Value has type <i>long</i>.
     */
    pvLong,
    /**
     * Value has type <i>ubyte</i>.
     */
    pvUByte,
    /**
     * Value has type <i>ushort</i>.
     */
    pvUShort,
    /**
     * Value has type <i>uint</i>.
     */
    pvUInt,
    /**
     * Value has type <i>ulong</i>.
     */
    pvULong,
    /**
     * value has type <i>float</i>.
     */
    pvFloat,
    /**
     * Value has type <i>double</i>.
     */
    pvDouble,
    /**
     * Value has type <i>string</i>.
     */
    pvString;
    /**
     * Is this an integer (signed or unsigned). true if byte, short, int, long, ubyte, ushort, uint, or ulong.
     * @return true if it is an integer type.
     */
    public boolean isInteger() {
        if( (ordinal() >= ScalarType.pvByte.ordinal()) && (ordinal() <= ScalarType.pvULong.ordinal()) ) {
            return true;
        }
        return false;
    }
    /**
     * Is this an unsigned integer. true if ubyte, ushort, uint, or ulong.
     * @return true if it is an unsigned integer type.
     */
    public boolean isUInteger() {
        if( (ordinal() >= ScalarType.pvUByte.ordinal()) && (ordinal() <= ScalarType.pvULong.ordinal()) ) {
            return true;
        }
        return false;
    }
    /**
     * Is this a Java numeric type?
     * @return Returns true if the type is a Java numeric type.
     * The numeric types are byte, short, int, long, float, and double.
     */
    public boolean isNumeric() {
        if( (ordinal() >= ScalarType.pvByte.ordinal()) && (ordinal() <= ScalarType.pvDouble.ordinal()) ) {
            return true;
        }
        return false;
    }
    /**
     * Is this a Java primitive type?
     * @return Returns true if the type is a Java primitive type.
     * The numeric types and boolean are primitive types.
     */
    public boolean isPrimitive() {
        if(isNumeric()) return true;
        if(ordinal() == ScalarType.pvBoolean.ordinal()) return true;
        return false;
    }
    /**
     * Get the ScalarType for a string defining the type.
     * @param type A character string defining the type.
     * @return The ScalarType or null if an illegal type.
     */
    public static ScalarType getScalarType(String type) {
        if(type.equals("boolean")) return ScalarType.pvBoolean;
        if(type.equals("byte")) return ScalarType.pvByte;
        if(type.equals("short")) return ScalarType.pvShort;
        if(type.equals("int")) return ScalarType.pvInt;
        if(type.equals("long")) return ScalarType.pvLong;
        if(type.equals("ubyte")) return ScalarType.pvUByte;
        if(type.equals("ushort")) return ScalarType.pvUShort;
        if(type.equals("uint")) return ScalarType.pvUInt;
        if(type.equals("ulong")) return ScalarType.pvULong;
        if(type.equals("float")) return ScalarType.pvFloat;
        if(type.equals("double")) return ScalarType.pvDouble;
        if(type.equals("string")) return ScalarType.pvString;
        return null;
    }
    public String toString() {
        switch(this) {
        case pvBoolean: return "boolean";
        case pvByte: return "byte";
        case pvShort: return "short";
        case pvInt:   return "int";
        case pvLong:  return "long";
        case pvUByte: return "ubyte";
        case pvUShort: return "ushort";
        case pvUInt:   return "uint";
        case pvULong:  return "ulong";
        case pvFloat: return "float";
        case pvDouble: return "double";
        case pvString: return "string";
        }
        throw new IllegalArgumentException("Unknown scalarType");
    }
}

