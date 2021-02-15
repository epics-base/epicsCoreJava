/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.util.namedValues;

import java.util.Vector;

/**
 * NamedValues is a particular implementation of a named value system,
 * in which the value in each name/value pair is itself specifically a
 * Vector of values.
 *
 * <p> Functionally then, the name identifies a list of
 * data. </p>
 *
 * <p>named/value pairs are added to a NameValues object through the
 * {@code add add} method.  The list of names (in the system of
 * name/value pairs) is acquired through {@code getLabels getLabels}.
 * And the whole system (all the names, and all the values associated
 * with each name, is acquired through {@code getValues getValues}.</p>
 *
 * <p>In this way, a table can be thought of as a NamedValues system, where each
 * column label and the values under it are a single named/value.  A single
 * NamedValues object holds a whole table.</p>
 *
 * @author Greg White and Chris Larrieu. 11-Dec-201, SLAC
 *
 */
public class NamedValues
{
	// The names in the name/values system.
    protected Vector<String> labels = new Vector<String>();

    // The values (Strings) associated with each name. Note, the congruence of
    // values to labels is important - the 5th element of each make the 5th
    // named/value pair.
    protected Vector<String[]> values = new Vector<String[]>();

    /**
     * Add a named Vector of values to a NameValues object.
     *
     * @param name the name of the list of values to add, eg "girlscouts"
     * @param value the list of values associated with name, eg {"Caroline", "Mary", "Beth"}
     */
    @SuppressWarnings("rawtypes")
	public void add (String name,  Vector value)
    {
        labels.addElement (name);
        String[] array = new String[value.size()];

        for (int i = 0; i < array.length; ++i)
            array[i] = value.get(i).toString();

        values.addElement (array);
    }

    /**
     * Gets all the names in the named/values system at once, as an array of Strings.
     *
     * @return all names in the NamedValues object, ie the column headings in the table analogy
     */
    public String[] getLabels ()
    {
        return labels.toArray (new String[0]);
    }

    /**
     * Gets all the values in the named/values system.
     *
     * Since each "value" is itself a list (supplied as a vector to the add method), this
     * method returns a 2d array.
     *
     * @return the values arrays presently in the NamedValues object,
     * i.e. the contents of the table in the table analogy
     */
    public String[][] getValues ()
    {
        return values.toArray (new String[values.size()][]);
    }
}
