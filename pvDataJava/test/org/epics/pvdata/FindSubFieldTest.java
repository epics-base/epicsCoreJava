/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata;

import static junit.framework.TestCase.assertTrue;
import org.epics.pvdata.factory.BaseScalar;
import org.epics.pvdata.factory.BaseStructure;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Type;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author cgarcia
 */
public class FindSubFieldTest {
    
    public FindSubFieldTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void allScalar() {
        Structure struct = CreateTestStructure.allScalar();

        System.out.println("Structura: " + struct.toString());
        BaseStructure bst = new BaseStructure(struct.getFieldNames(), struct.getFields());
        Field field =  bst.findSubField("booleanValue", struct);
        assertTrue(((BaseScalar) field).getScalarType() == ScalarType.pvBoolean);
        field =  bst.findSubField("byteValue", struct);
        assertTrue(((BaseScalar) field).getScalarType() == ScalarType.pvByte);                
        field =  bst.findSubField("shortValue", struct);        
        assertTrue(((BaseScalar) field).getScalarType() == ScalarType.pvShort);         
        field =  bst.findSubField("intValue", struct);
        assertTrue(((BaseScalar) field).getScalarType() == ScalarType.pvInt);         
        field =  bst.findSubField("longValue", struct);   
        assertTrue(((BaseScalar) field).getScalarType() == ScalarType.pvLong);         
        field =  bst.findSubField("ubyteValue", struct);
        assertTrue(((BaseScalar) field).getScalarType() == ScalarType.pvUByte);         
        field =  bst.findSubField("ushortValue", struct);
        assertTrue(((BaseScalar) field).getScalarType() == ScalarType.pvUShort);         
        field =  bst.findSubField("uintValue", struct);
        assertTrue(((BaseScalar) field).getScalarType() == ScalarType.pvUInt);         
        field =  bst.findSubField("ulongValue", struct);
        assertTrue(((BaseScalar) field).getScalarType() == ScalarType.pvULong);         
        field =  bst.findSubField("floatValue", struct);
        assertTrue(((BaseScalar) field).getScalarType() == ScalarType.pvFloat);         
        field =  bst.findSubField("doubleValue", struct);
        assertTrue(((BaseScalar) field).getScalarType() == ScalarType.pvDouble);         
        field =  bst.findSubField("stringValue", struct);
        assertTrue(((BaseScalar) field).getScalarType() == ScalarType.pvString);         

        System.out.println("Campo: " + field.toString());

    }
}
