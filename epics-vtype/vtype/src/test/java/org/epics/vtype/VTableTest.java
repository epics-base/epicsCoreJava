package org.epics.vtype;

import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ArrayFloat;
import org.epics.util.array.ArrayInteger;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VTableTest {

	/**
	 * ignore the types and labels when determining the column count
	 */
    @Test
    public void testIVTableColumnCount() {
        VTable vTable = new IVTable(Arrays.asList(VInt.class, VDouble.class),
                Arrays.asList("int"),
                Arrays.asList(ArrayInteger.of(1, 2, 3), ArrayDouble.of(1.0, 2.0, 3.0), ArrayFloat.of(1.0f, 2.0f, 3.0f)));

        // Assert that column count is based on values data.
        assertEquals(3, vTable.getColumnCount());

        assertEquals(3, vTable.getRowCount());
        assertTrue(vTable.getColumnType(0).isAssignableFrom(VInt.class));
        assertTrue(vTable.getColumnType(1).isAssignableFrom(VDouble.class));
        assertEquals("int", vTable.getColumnName(0));
    }
    
    /**
     * NTTables support columns with different row counts, the table should return the max number of rows
     */
    @Test
    public void testIVTableRowCount() {
        VTable vTable = new IVTable(Arrays.asList(),
                Arrays.asList("int"),
                Arrays.asList(ArrayInteger.of(1, 2, 3), ArrayDouble.of(1.0, 2.0), ArrayFloat.of(1.0f)));

        assertEquals(3, vTable.getRowCount());
    }
}
