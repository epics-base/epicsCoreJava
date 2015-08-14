/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import junit.framework.TestCase;

import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVScalar;

/**
 * JUnit test for NTUtils.
 * @author dgh
 *
 */
public class NTUtilsTest extends TestCase
{
    private static void ntutilTest(String u1, String u2, boolean expected)
    {
        boolean is_a1 = NTUtils.is_a(u1,u2);
        boolean is_a2 = NTUtils.is_a(u2,u1);
        assertEquals(is_a1, is_a2);
        assertEquals(is_a1, expected);
    }

    public static void test1()
	{
        ntutilTest(NTScalar.URI,NTScalar.URI, true);
        ntutilTest(NTScalar.URI,NTScalarArray.URI, false);
        ntutilTest(NTScalar.URI,"epics:nt/NtScalar:1.0", false);
        ntutilTest(NTScalar.URI,"epics:nt/NTScalar:1.1", true);
        ntutilTest(NTScalar.URI,"epics:nt/NTScalar:11.0", false);
        ntutilTest(NTScalar.URI,"epics:nt/NTScalar:1.1.0", false);
        ntutilTest(NTScalar.URI,"epics:nt/NTScalar:2.0", false);
        ntutilTest(NTScalar.URI,"NTScalar:1.0", false);
        ntutilTest(NTScalar.URI,"/NTScalar:1.0", false);
        ntutilTest(NTScalar.URI,"epics/NTScalar:1.1", false);
        ntutilTest(NTScalar.URI,"epics/NTScalar:1.1", false);
        ntutilTest(NTScalar.URI,"NTScalar", false);
	}


}

