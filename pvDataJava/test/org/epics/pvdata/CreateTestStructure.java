/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */

package org.epics.pvdata;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;

/**
 * @author mrk
 *
 */
public class CreateTestStructure {
    static private final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    
    static public Structure allScalar() {
        String[] fieldNames = {
                "booleanValue",
                "byteValue",
                "shortValue",
                "intValue",
                "longValue",
                "ubyteValue",
                "ushortValue",
                "uintValue",
                "ulongValue",
                "floatValue",
                "doubleValue",
                "stringValue"
        };
        int num = fieldNames.length;
        Field[] fields = new Field[num];
        fields[0] = fieldCreate.createScalar(ScalarType.pvBoolean);
        fields[1] = fieldCreate.createScalar(ScalarType.pvByte);
        fields[2] = fieldCreate.createScalar(ScalarType.pvShort);
        fields[3] = fieldCreate.createScalar(ScalarType.pvInt);
        fields[4] = fieldCreate.createScalar(ScalarType.pvLong);
        fields[5] = fieldCreate.createScalar(ScalarType.pvUByte);
        fields[6] = fieldCreate.createScalar(ScalarType.pvUShort);
        fields[7] = fieldCreate.createScalar(ScalarType.pvUInt);
        fields[8] = fieldCreate.createScalar(ScalarType.pvULong);
        fields[9] = fieldCreate.createScalar(ScalarType.pvFloat);
        fields[10] = fieldCreate.createScalar(ScalarType.pvDouble);
        fields[11] = fieldCreate.createScalar(ScalarType.pvString);
        return fieldCreate.createStructure(fieldNames, fields);
    }
    
    static public Structure allScalarArray() {
        String[] fieldNames = {
                "booleanArrayValue",
                "byteArrayValue",
                "shortArrayValue",
                "intArrayValue",
                "longArrayValue",
                "ubyteArrayValue",
                "ushortArrayValue",
                "uintArrayValue",
                "ulongArrayValue",
                "floatArrayValue",
                "doubleArrayValue",
                "stringArrayValue"
        };
        int num = fieldNames.length;
        Field[] fields = new Field[num];
        fields[0] = fieldCreate.createScalarArray(ScalarType.pvBoolean);
        fields[1] = fieldCreate.createScalarArray(ScalarType.pvByte);
        fields[2] = fieldCreate.createScalarArray(ScalarType.pvShort);
        fields[3] = fieldCreate.createScalarArray(ScalarType.pvInt);
        fields[4] = fieldCreate.createScalarArray(ScalarType.pvLong);
        fields[5] = fieldCreate.createScalarArray(ScalarType.pvUByte);
        fields[6] = fieldCreate.createScalarArray(ScalarType.pvUShort);
        fields[7] = fieldCreate.createScalarArray(ScalarType.pvUInt);
        fields[8] = fieldCreate.createScalarArray(ScalarType.pvULong);
        fields[9] = fieldCreate.createScalarArray(ScalarType.pvFloat);
        fields[10] = fieldCreate.createScalarArray(ScalarType.pvDouble);
        fields[11] = fieldCreate.createScalarArray(ScalarType.pvString);
        return fieldCreate.createStructure(fieldNames, fields);
    }
    
}
