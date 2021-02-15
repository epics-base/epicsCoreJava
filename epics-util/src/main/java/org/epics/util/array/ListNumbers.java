/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * Utilities for manipulating ListNumbers.
 *
 * @author carcassi
 */
public class ListNumbers {

    /**
     * Creates a sorted view of the given ListNumber.
     * <p>
     * The ListNumber is not sorted in place, and the data is not copied out.
     * Therefore it's intended that the ListNumber is not changed while
     * the view is used.
     *
     * @param values the values to be sorted
     * @return the sorted view
     */
    public static SortedListView sortedView(ListNumber values) {
        SortedListView view = new SortedListView(values);
        if (values.size() <= 1) {
            // Nothing to sort
            return view;
        }

        double value = values.getDouble(0);
        for (int i = 1; i < values.size(); i++) {
            double newValue = values.getDouble(i);
            if (value > newValue) {
                SortedListView.quicksort(view);
                return view;
            }
            value = newValue;
        }

        return view;
    }

    /**
     * Creates a sorted view of the given ListNumber based on the indexes provided.
     * This method can be used to sort the given values based on the ordering
     * by another (sorted) list of values.
     * <p>
     * The ListNumber is not sorted in place, and the data is not copied out.
     * Therefore it's intended that the ListNumber is not changed while
     * the view is used.
     *
     * @param values the values to be sorted
     * @param indexes the ordering to be used for the view
     * @return the sorted view
     */
    public static SortedListView sortedView(ListNumber values, ListInteger indexes) {
        SortedListView view = new SortedListView(values, indexes);
        return view;
    }

    /**
     * Finds the value in the list, or the one right below it.
     *
     * @param values a list of values
     * @param value a value
     * @return the index of the value
     */
    public static int binarySearchValueOrLower(ListNumber values, double value) {
        if (value <= values.getDouble(0)) {
            return 0;
        }
        if (value >= values.getDouble(values.size() -1)) {
            return values.size() - 1;
        }

        int index = binarySearch(0, values.size() - 1, values, value);

        while (index != 0 && value == values.getDouble(index - 1)) {
            index--;
        }

        return index;
    }

    /**
     * Finds the value in the list, or the one right above it.
     *
     * @param values a list of values
     * @param value a value
     * @return the index of the value
     */
    public static int binarySearchValueOrHigher(ListNumber values, double value) {
        if (value <= values.getDouble(0)) {
            return 0;
        }
        if (value >= values.getDouble(values.size() -1)) {
            return values.size() - 1;
        }

        int index = binarySearch(0, values.size() - 1, values, value);

        while (index != values.size() - 1 && value > values.getDouble(index)) {
            index++;
        }

        while (index != values.size() - 1 && value == values.getDouble(index + 1)) {
            index++;
        }

        return index;
    }

    private static int binarySearch(int low, int high, ListNumber values, double value) {
        // Taken from JDK
        while (low <= high) {
            int mid = (low + high) >>> 1;
            double midVal = values.getDouble(mid);

            if (midVal < value)
                low = mid + 1;  // Neither val is NaN, thisVal is smaller
            else if (midVal > value)
                high = mid - 1; // Neither val is NaN, thisVal is larger
            else {
                long midBits = Double.doubleToLongBits(midVal);
                long keyBits = Double.doubleToLongBits(value);
                if (midBits == keyBits)     // Values are equal
                    return mid;             // Key found
                else if (midBits < keyBits) // (-0.0, 0.0) or (!NaN, NaN)
                    low = mid + 1;
                else                        // (0.0, -0.0) or (NaN, !NaN)
                    high = mid - 1;
            }
        }

        return low - 1;  // key not found.
    }

    /**
     * Creates a list of equally spaced values given the range and the number of
     * elements.
     * <p>
     * Note that, due to rounding errors in double precision, the difference
     * between the elements may not be exactly the same.
     *
     * @param minValue the first value in the list
     * @param maxValue the last value in the list
     * @param size the size of the list
     * @return a new list
     */
    public static ListNumber linearListFromRange(final double minValue, final double maxValue, final int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive (was " + size + " )");
        }
        return new LinearListDoubleFromRange(size, minValue, maxValue);
    }

    /**
     * Creates a list of equally spaced values given the first value, the
     * step between element and the size of the list.
     *
     * @param initialValue the first value in the list
     * @param increment the difference between elements
     * @param size the size of the list
     * @return a new list
     */
    public static ListNumber linearList(final double initialValue, final double increment, final int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive (was " + size + " )");
        }
        return new LinearListDouble(size, initialValue, increment);
    }

    /**
     * Tests whether the list contains a equally spaced numbers.
     * <p>
     * Always returns true if the list was created with {@link #linearList(double, double, int) }
     * or {@link #linearListFromRange(double, double, int) }. For all other cases,
     * takes the first and last value, creates a linearListFromRange, and checks
     * whether the difference is greater than the precision allowed by double.
     * Note that this method is really strict, and it may rule out cases
     * that may be considered to be linear.
     *
     * @param listNumber a list number
     * @return true if the elements of the list are equally spaced
     */
    public static boolean isLinear(ListNumber listNumber) {
        if (listNumber instanceof LinearListDouble || listNumber instanceof LinearListDoubleFromRange) {
            return true;
        }
        ListDouble diff = ListMath.subtract(listNumber, linearListFromRange(listNumber.getDouble(0), listNumber.getDouble(listNumber.size() - 1), listNumber.size()));
        for (int i = 0; i < diff.size(); i++) {
            if (Math.abs(diff.getDouble(i)) > Math.ulp(listNumber.getDouble(i))) {
                return false;
            }
        }
        return true;
    }

    private static class LinearListDoubleFromRange extends ListDouble {

        private final int size;
        private final double minValue;
        private final double maxValue;

        public LinearListDoubleFromRange(int size, double minValue, double maxValue) {
            this.size = size;
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        public double getDouble(int index) {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
            }
            return minValue + (index * (maxValue - minValue)) / (size - 1);
        }

        public int size() {
            return size;
        }
    }

    private static class LinearListDouble extends ListDouble {

        private final int size;
        private final double initialValue;
        private final double increment;

        public LinearListDouble(int size, double initialValue, double increment) {
            this.size = size;
            this.initialValue = initialValue;
            this.increment = increment;
        }

        public double getDouble(int index) {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
            }
            return initialValue + index * increment;
        }

            public int size() {
                return size;
            }
    }

    /**
     * Returns a view of the given list that presents only the elements
     * at the given indexes.
     *
     * @param list a numeric list
     * @param indexes the indexes with the values to expose
     * @return a wrapper around list
     */
    public static ListNumber listView(ListNumber list, ListInteger indexes) {
        if (list instanceof ListDouble) {
            return new ListView.Double((ListDouble) list, indexes);
        } else if (list instanceof ListFloat) {
            return new ListView.Float((ListFloat) list, indexes);
        } else if (list instanceof ListLong) {
            return new ListView.Long((ListLong) list, indexes);
        } else if (list instanceof ListInteger) {
            return new ListView.Int((ListInteger) list, indexes);
        } else if (list instanceof ListShort) {
            return new ListView.Short((ListShort) list, indexes);
        } else if (list instanceof ListByte) {
            return new ListView.Byte((ListByte) list, indexes);
        }
        throw new UnsupportedOperationException("Not yet supported");
    }


    /**
     * Concatenates a sequence of lists into a single one. The returned list
     * is a view on the previous lists. This means that no copy is performed
     * during the concatenation and that changes in the arguments will
     * be seen through the concatenation. When reading and writing, the
     * type is always cast to a double.
     *
     * @param lists the lists to concatenate.
     * @return the concatenated list.
     */
    public static ListDouble concatenate(final ListNumber... lists) {
        if (lists.length == 0) {
            return CollectionNumbers.unmodifiableListDouble(new double[0]);
        }

        return new ListDouble() {

            public int size() {
                int size = 0;
                for (ListNumber list : lists) {
                    size += list.size();
                }
                return size;
            }

            public double getDouble( int index ) {
                if (index < 0 || index >= size()) {
                    throw new IndexOutOfBoundsException("Index out of bounds: " + index + ", size: " + size());
                }

                // Iterate through the lists until the right spot is found
                int currentListStart = 0;
                for (ListNumber list : lists) {
                    int currentListEnd = currentListStart + list.size();
                    if (index < currentListEnd) {
                        return list.getDouble(index - currentListStart);
                    }
                    currentListStart = currentListEnd;
                }

                throw new RuntimeException("Reached unreachable code - please contact developers");
            }

            @Override
            public void setDouble(int index, double value) {
                if (index < 0 || index >= size()) {
                    throw new IndexOutOfBoundsException("Index out of bounds: " + index + ", size: " + size());
                }

                // Iterate through the lists until the right spot is found
                int currentListStart = 0;
                for (ListNumber list : lists) {
                    int currentListEnd = currentListStart + list.size();
                    if (index < currentListEnd) {
                        list.setDouble(index - currentListStart, value);
                        return;
                    }
                    currentListStart = currentListEnd;
                }

                throw new RuntimeException("Reached unreachable code - please contact developers");
            }
        };
    }
}
