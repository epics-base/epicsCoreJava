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
     * value has type <i>float</i>.
     */
    pvFloat,
    /**
     * Value has type <i>double</i>.
     */
    pvDouble,
    /**
     * Value has type <i>String</i>.
     */
    pvString,
    /**
     * The element is a structure that must be accessed as a single entity, i.e. it is not possible to access subfields of the structure.
     */
    pvStructure;
    /**
     * Is this an integer. true if byte, short, int, or long.
     * @return true if it is an integer type.
     */
    public boolean isInteger() {
        if( (ordinal() >= ScalarType.pvByte.ordinal()) && (ordinal() <= ScalarType.pvLong.ordinal()) ) {
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
        if(type.equals("float")) return ScalarType.pvFloat;
        if(type.equals("double")) return ScalarType.pvDouble;
        if(type.equals("string")) return ScalarType.pvString;
        if(type.equals("structure")) return ScalarType.pvStructure;
        return null;
    }
}

