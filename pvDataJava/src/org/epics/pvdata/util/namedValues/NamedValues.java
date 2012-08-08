package org.epics.pvdata.util.namedValues;

import java.util.Vector;

/**
 * NamedValues is a particular implementation of a named value system, in 
 * which the value in each name/value pair is itself specifically a vector of values.
 * Functionally then, the name identifies a list of data.
 * 
 * named/value pairs are added to a NameValues object through the add method.
 * The list of names (in the system of name/value pairs) is acquired through getLabels.
 * And the whole system (all the names, and all the values associated with each 
 * name, is acquired through getValues.
 *  
 * In this way, a table can be thought of as a NamedValues system, where each
 * column label and the values under it are a single named/value.  A single 
 * NamedValues object holds a whole table.
 *    
 * @author 11.11.11 Greg White (gregory.white@psi.ch) from an idea by Chris Larrieu.
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
     * Add a named vector of values to a NameValues object.
     * 
     * @param name The name of the list of values to add, eg "girlscouts"
     * @param value The list of values associated with name, eg {"Caroline", "Mary", "Beth"}
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
     * @return all names in the NamedValues object, ie the column headings in the table analogy.
     */
    public String[] getLabels ()
    {
        return labels.toArray (new String[labels.size()]);
    }

    /**
     * Gets all the values in the named/values system. 
     * 
     * Since each "value" is itself a list (supplied as a vector to the add method), this
     * method returns a 2d array. 
     *   
     * @return The values arrays presently in the NamedValues object, 
     * ie the contents of the table in the table analogy.
     */
    public String[][] getValues ()
    {
        return values.toArray (new String[values.size()][]);
    }
}
