/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.hamcrest.Matchers;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.epics.util.array.CollectionNumbers.*;

/**
 *
 * @author carcassi
 */
public class ConcatenatedListDoubleTest extends FeatureTestListNumber {

    @Override
    public ListDouble createConstantCollection() {
        ArrayDouble list1 = unmodifiableListDouble(1, 1, 1, 1, 1);
        ArrayDouble list2 = unmodifiableListDouble(1, 1, 1, 1, 1);
        return ListNumbers.concatenate(list1 , list2);
    }

    @Override
    public ListDouble createRampCollection() {
        ArrayDouble list1 = unmodifiableListDouble(0, 1, 2, 3, 4);
        ArrayDouble list2 = unmodifiableListDouble(5, 6, 7, 8, 9);
        return ListNumbers.concatenate(list1 , list2);
    }

    @Override
    public ListNumber createModifiableCollection() {
        ArrayDouble list1 = toListDouble(new double[5]);
        ArrayDouble list2 = toListDouble(new double[5]);
        return ListNumbers.concatenate(list1 , list2);
    }

    @Override
    public ListNumber createEmpty() {
        ArrayDouble list1 = toListDouble(new double[0]);
        ArrayDouble list2 = toListDouble(new double[0]);
        return ListNumbers.concatenate(list1 , list2);
    }

    @Test
    public void testConcatenation2() {
        ArrayDouble[] lists = new ArrayDouble[10];
        for (int i = 0; i < lists.length; i++) {
            lists[i] = unmodifiableListDouble(i);
        }
        ListDouble concatenated = ListNumbers.concatenate(lists);
        assertThat(concatenated.toString(), equalTo("[0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0]"));
    }

    @Test
    public void testConcatenation3() {
        ArrayDouble[] lists = new ArrayDouble[10];
        for (int i = 0; i < lists.length; i++) {
            lists[i] = unmodifiableListDouble();
        }
        ListDouble concatenated = ListNumbers.concatenate(lists);
        assertThat(concatenated.size(), equalTo(0));
    }

    @Test
    public void testConcatenation4() {
        ArrayDouble l1 = unmodifiableListDouble(1);
        ArrayDouble l2 = unmodifiableListDouble(1, 2);
        ArrayDouble l3 = unmodifiableListDouble(1, 2, 3);
        ArrayDouble l4 = unmodifiableListDouble(1, 2, 3, 4);
        ArrayDouble l5 = unmodifiableListDouble(1, 2, 3, 4, 5);

        ListDouble concatenated = ListNumbers.concatenate(l1, l2, l3, l4, l5);
        assertThat(concatenated, Matchers.<ListDouble>equalTo(unmodifiableListDouble(1, 1, 2, 1, 2, 3, 1, 2, 3, 4, 1, 2, 3, 4, 5)));
    }
}
