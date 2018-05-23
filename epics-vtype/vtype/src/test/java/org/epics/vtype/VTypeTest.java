/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import java.util.Arrays;
import org.epics.util.array.ArrayByte;
import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ArrayFloat;
import org.epics.util.array.ArrayInteger;
import org.epics.util.array.ArrayLong;
import org.epics.util.array.ArrayShort;
import org.epics.util.array.ArrayUByte;
import org.epics.util.array.ArrayUInteger;
import org.epics.util.array.ArrayULong;
import org.epics.util.array.ArrayUShort;
import org.epics.util.number.UByte;
import org.epics.util.number.UInteger;
import org.epics.util.number.ULong;
import org.epics.util.number.UShort;
import org.epics.util.stats.Range;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class VTypeTest {

    @Test(expected = IllegalArgumentException.class)
    public void toVTypeChecked1() {
        VType.toVTypeChecked(new Object());
    }

    @Test(expected = IllegalArgumentException.class)
    public void toVTypeChecked2() {
        VType.toVTypeChecked(new Object(), Alarm.none(), Time.now(), Display.none());
    }
    
    @Test
    public void toVType1() {
        // no conversion
        assertThat(VType.toVType(new Object()), equalTo(null));
        
        // primitives
        assertThat(VType.toVType(1.0), instanceOf(VDouble.class));
        assertThat(VType.toVType(1.0f), instanceOf(VFloat.class));
        assertThat(VType.toVType(1L), instanceOf(VLong.class));
        assertThat(VType.toVType(1), instanceOf(VInt.class));
        assertThat(VType.toVType((short) 1), instanceOf(VShort.class));
        assertThat(VType.toVType((byte) 1), instanceOf(VByte.class));

        // wrappers
        assertThat(VType.toVType(new Double(1)), instanceOf(VDouble.class));
        assertThat(VType.toVType(new Float(1)), instanceOf(VFloat.class));
        assertThat(VType.toVType(new Long(1)), instanceOf(VLong.class));
        assertThat(VType.toVType(new Integer(1)), instanceOf(VInt.class));
        assertThat(VType.toVType(new Short((short) 1)), instanceOf(VShort.class));
        assertThat(VType.toVType(new Byte((byte) 1)), instanceOf(VByte.class));

        // unsigned wrappers
        assertThat(VType.toVType(new ULong(1)), instanceOf(VULong.class));
        assertThat(VType.toVType(new UInteger(1)), instanceOf(VUInt.class));
        assertThat(VType.toVType(new UShort((short) 1)), instanceOf(VUShort.class));
        assertThat(VType.toVType(new UByte((byte) 1)), instanceOf(VUByte.class));
        
        // primitive arrays
        assertThat(VType.toVType(new double[] {0, 1, 2, 3, 4}), instanceOf(VDoubleArray.class));
        assertThat(VType.toVType(new float[] {0, 1, 2, 3, 4}), instanceOf(VFloatArray.class));
        assertThat(VType.toVType(new long[] {0, 1, 2, 3, 4}), instanceOf(VLongArray.class));
        assertThat(VType.toVType(new int[] {0, 1, 2, 3, 4}), instanceOf(VIntArray.class));
        assertThat(VType.toVType(new short[] {0, 1, 2, 3, 4}), instanceOf(VShortArray.class));
        assertThat(VType.toVType(new byte[] {0, 1, 2, 3, 4}), instanceOf(VByteArray.class));
        
        // number collections
        assertThat(VType.toVType(ArrayDouble.of(new double[] {0, 1, 2, 3, 4})), instanceOf(VDoubleArray.class));
        assertThat(VType.toVType(ArrayFloat.of(new float[] {0, 1, 2, 3, 4})), instanceOf(VFloatArray.class));
        assertThat(VType.toVType(ArrayLong.of(new long[] {0, 1, 2, 3, 4})), instanceOf(VLongArray.class));
        assertThat(VType.toVType(ArrayInteger.of(new int[] {0, 1, 2, 3, 4})), instanceOf(VIntArray.class));
        assertThat(VType.toVType(ArrayShort.of(new short[] {0, 1, 2, 3, 4})), instanceOf(VShortArray.class));
        assertThat(VType.toVType(ArrayByte.of(new byte[] {0, 1, 2, 3, 4})), instanceOf(VByteArray.class));
        assertThat(VType.toVType(ArrayULong.of(new long[] {0, 1, 2, 3, 4})), instanceOf(VULongArray.class));
        assertThat(VType.toVType(ArrayUInteger.of(new int[] {0, 1, 2, 3, 4})), instanceOf(VUIntArray.class));
        assertThat(VType.toVType(ArrayUShort.of(new short[] {0, 1, 2, 3, 4})), instanceOf(VUShortArray.class));
        assertThat(VType.toVType(ArrayUByte.of(new byte[] {0, 1, 2, 3, 4})), instanceOf(VUByteArray.class));
        
        // String, String arrays and collections
        assertThat(VType.toVType("string"), instanceOf(VString.class));
//        assertThat(VType.toVType(new String[] {"a", "b", "c"}), instanceOf(VStringArray.class));
//        assertThat(VType.toVType(Arrays.asList(new String[] {"a", "b", "c"})), instanceOf(VStringArray.class));
        
        // Boolean, Boolean arrays and collections
        assertThat(VType.toVType(true), instanceOf(VBoolean.class));
    }
    
    @Test
    public void toVType2() {
        Alarm alarm = Alarm.of(AlarmSeverity.MAJOR, AlarmStatus.RECORD, "problem");
        Time time = Time.now();
        Display display = Display.of(Range.of(0, 10), Range.of(1, 9), Range.of(2, 8), Range.of(2, 8), "m", Display.defaultNumberFormat());
        
        // no conversion
        assertThat(VType.toVType(new Object(), alarm, time, display), equalTo(null));
        
        // primitives
        assertThat(VType.toVType(1.0, alarm, time, display), equalTo(VDouble.of(1.0, alarm, time, display)));
        assertThat(VType.toVType(1.0f, alarm, time, display), equalTo(VFloat.of(1.0, alarm, time, display)));
        assertThat(VType.toVType(1L, alarm, time, display), equalTo(VLong.of(1, alarm, time, display)));
        assertThat(VType.toVType(1, alarm, time, display), equalTo(VInt.of(1, alarm, time, display)));
        assertThat(VType.toVType((short) 1, alarm, time, display), equalTo(VShort.of(1, alarm, time, display)));
        assertThat(VType.toVType((byte) 1, alarm, time, display), equalTo(VByte.of(1, alarm, time, display)));
    }
}
