/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata;

import junit.framework.TestCase;

import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.misc.BitSetUtil;
import org.epics.pvdata.misc.BitSetUtilFactory;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardField;



/**
 * JUnit test for bitSetUtil
 * @author mrk
 *
 */
public class BitSetUtilTest extends TestCase {
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static final StandardField standardField = StandardFieldFactory.getStandardField();
    private static final BitSetUtil bitSetUtil = BitSetUtilFactory.getCompressBitSet();
	private static boolean debug = false;
    
    static private void print(String name,String value) {
        if(!debug) return;
        System.out.println(name);
        System.out.println(value);
    }
    
    public static void testSimple() {     
        String[] fieldNames = {"timeStamp","alarm","voltage","power","current"};
        PVField[] pvFields = new PVField[5];
        pvFields[0] = pvDataCreate.createPVStructure(standardField.timeStamp());
        pvFields[1] = pvDataCreate.createPVStructure(standardField.alarm());
        pvFields[2] = pvDataCreate.createPVStructure(standardField.scalar(
              ScalarType.pvDouble,"alarm"));
        pvFields[3] = pvDataCreate.createPVStructure(standardField.scalar(
                ScalarType.pvDouble,"alarm"));
        pvFields[4] = pvDataCreate.createPVStructure(standardField.scalar(
                ScalarType.pvDouble,"alarm"));
        PVStructure pvs = pvDataCreate.createPVStructure(fieldNames,pvFields);
        print("pvs",pvs.toString());
        int nfields = pvs.getNumberFields();
        BitSet bitSet = new BitSet(nfields);       
        for(int i=0; i<nfields; i++) bitSet.set(i);
        print("bitSet",bitSet.toString());
        bitSetUtil.compress(bitSet, pvs);
        print("bitSet",bitSet.toString());
        bitSet.clear();
        PVField pvField = pvs.getSubField("timeStamp");
        int offsetTimeStamp = pvField.getFieldOffset();
        pvField = pvs.getSubField("timeStamp.secondsPastEpoch");
        int offsetSeconds = pvField.getFieldOffset();
        pvField = pvs.getSubField("timeStamp.nanoseconds");
        int offsetNano = pvField.getFieldOffset();
        pvField = pvs.getSubField("timeStamp.userTag");
        int offsetUserTag = pvField.getFieldOffset();
        bitSet.set(offsetSeconds);
        bitSetUtil.compress(bitSet,pvs);
        assert(bitSet.get(offsetSeconds)==true);
        bitSet.set(offsetNano);
        bitSet.set(offsetUserTag);
        print("bitSet",bitSet.toString());
        bitSetUtil.compress(bitSet,pvs);
        assert(bitSet.get(offsetSeconds)==false);
        assert(bitSet.get(offsetTimeStamp)==true);
        print("bitSet",bitSet.toString());
        bitSet.clear();
        
        pvField = pvs.getSubField("current");
        int offsetCurrent = pvField.getFieldOffset();
        pvField = pvs.getSubField("current.value");
        int offsetValue = pvField.getFieldOffset();
        pvField = pvs.getSubField("current.alarm");
        int offsetAlarm = pvField.getFieldOffset();
        pvField = pvs.getSubField("current.alarm.severity");
        int offsetSeverity = pvField.getFieldOffset();
        pvField = pvs.getSubField("current.alarm.status");
        int offsetStatus = pvField.getFieldOffset();
        pvField = pvs.getSubField("current.alarm.message");
        int offsetMessage = pvField.getFieldOffset();
        bitSet.set(offsetValue);
        bitSet.set(offsetSeverity);
        bitSet.set(offsetStatus);
        bitSet.set(offsetMessage);
        print("bitSet",bitSet.toString());
        bitSetUtil.compress(bitSet,pvs);
        print("bitSet",bitSet.toString());
        assert(bitSet.get(offsetCurrent)==true);
        bitSet.clear();
        bitSet.set(offsetSeverity);
        bitSet.set(offsetStatus);
        bitSet.set(offsetMessage);
        print("bitSet",bitSet.toString());
        bitSetUtil.compress(bitSet,pvs);
        print("bitSet",bitSet.toString());
        assert(bitSet.get(offsetAlarm)==true);
        bitSet.clear();
        System.out.println("testBitSetUtil PASSED\n");
    }
}