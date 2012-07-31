package org.epics.pvdata.util.namedValues;

import java.io.PrintStream;

/**
 * NamedValuesFormatter is a base class for the classes which provide support 
 * for printing the values in a NamedValues instance.
 * <p>
 * NamedValues is a class for holding lists of String values where each list is associated 
 * with a name. NamedValuesFormatter can print the name/values in a NamedValues as a table 
 * or as a list of rows.
 * </p>
 * <p>
 * See NamedValuesColumnFormatter for help with formatting a NamedValues as a 
 * table (each name and all its values in one column). See NamedValuesRowFormatter 
 * for help printing as a list of row oriented data (each name and all its values on one row).
 * </p> 
 * For example, to create a table style output formatter, assign it data, and print the data:
 * <pre>
 * 			NamedValuesFormatter formatter =
 *				NamedValuesFormatter.create( NamedValuesFormatter.STYLE_COLUMNS );
 *			formatter.setWhetherDisplayLabels( true );
 *			formatter.assignNamedValues( namedValues );  // A named values system created elsewhere
 *			formatter.display( System.out );
 * </pre>
 * @see NamedValuesColumnFormatter
 * @see NamedValuesRowFormatter
 * 
 * @author 11.11.11 Greg White (greg@slac.stanford.edu) and Chris Larrieu.
 */
public abstract class NamedValuesFormatter
{
	/**
	 * To create a NamedValuesFormatter that prints NamedValues as 1 name and its values on 1 row.
	 */
	public static final int STYLE_ROWS = 1;     
	
	/**
	 * To create a NamedValuesFormatter that prints NamedValues as a table, 1 name and its values in a column.
	 */
	public static final int STYLE_COLUMNS = 2;
	
    boolean shouldDisplayLabels_ = true;
    protected String[] labels = new String[0];
    protected String[][] cells = new String[0][];
    protected int numRows = 0;
    protected int numCols = 0;

    /**
     * Creates an NamedValuesFormatter that will print a NamedValues 
     * object in either columns or rows orientation, depending on the argument.
     * 
     * @param style If style is given and is valued NamedValuesFormatter.STYLE_ROWS (1) then 
     * a row formatter is created, otherwise a column formatter is created.
     * @see STYLE_ROWS
     * @see STYLE_COLUMNS
     * @return An instance of a NamedValuesFormatter of the given style.
     */
    public static NamedValuesFormatter create( int style )
    {
        if (style == STYLE_ROWS)
            return new NamedValuesRowFormatter();
        else
            return new NamedValuesColumnFormatter();
    }

    /**
     * Tells the NamedValuesFormatter which NamedValues system it should format.
     * 
     * @param namedValues The system of named values that should be formatted by this NamedValuesFormatter.
     */
    public void assignNamedValues( NamedValues namedValues )
    {
        labels = namedValues.getLabels();
        cells = namedValues.getValues();
        updateDimensions();
    }

    /**
     * Finds the widest cell of the given column and returns its width. This is used internally 
     * to assess the character width that should be used to print a column.   
     * 
     * @param col
     * 
     * @return int width of the widest cell in the given column.
     */
    protected int getColumnWidth( int col )
    {
        String longest = "";
        for (int row = 0; row < numRows; ++row)
        {
            String text = getCell (row, col);
            if (text.length() > longest.length())
                longest = text;
        }

        return longest.length();
    }

    /**
     * Prints the formatted NamedValues as a table or list of rows, according to the style
     * used to create the formatter.
     * 
     * @param out The stream to which to print the output.
     */
    public void display( PrintStream out )
    {
        // generate formatting spaces for column widths
        String[] spaces = new String[numCols];
        for (int n = 0; n < spaces.length; ++n)
        {
            int width = getColumnWidth (n) + 2;
            StringBuffer space = new StringBuffer (width);
            for (int i = 0; i < width; ++i)
                space.append (' ');
            spaces[n] = space.toString();
        }

        for (int m = 0; m < numRows; ++m)
        {
            for (int n = 0; n < numCols; ++n)
                out.print (getFormattedCell (m, n, spaces[n]));

            out.println ("");
        }
    }

    /**
     * Returns boolean indicating if the printed output 
     * will include headings. If so, the names in the names/values 
     * system will be used as the heading labels.
     * 
     * @see setWhetherDisplaylabels
     * 
     * @return true if the formatter will print the labels, and false otherwise. See 
     * setWhetherDisplayLabels to change the value of the print labels setting.
     */
    public boolean getWhetherDisplayLabels()
    {
        return shouldDisplayLabels_;
    }

    /**
     * Sets whether or not headings will be printed with the data. 
     * 
     * @param yesno If false the formatter will not print the labels. If true, or 
     * this method is not used, the headings will be printed. 
     */
    public void setWhetherDisplayLabels( boolean yesno )
    {
        shouldDisplayLabels_ = yesno;
        updateDimensions();
    }

    /**
     * Returns the given "text" overlaid in the center of the space string.
     * @param text The string of data. This would be one cell's value data.
     * @param space A string into which to put the text. Usually space would be a String of spaces.
     * @return space, same width as given, with text in the middle.
     */
    protected static String centerText (String text, String space)
    {
        int rem = space.length() - text.length();
        int b = rem / 2;
        int a = rem - b;
        return space.substring (0,a) + text + space.substring (0,b);
    }

    /**
     * Returns the given "text" overlaid and to the left of the space string.
     * @param text The string of data. This would be one cell's value data.
     * @param space A string into which to put the text. Usually space would be a String of spaces.
     * @return space, same width as given, with text in it and left justified.
     */
    protected static String leftJustifyText (String text, String space)
    {
        return text + space.substring (text.length());
    }

    /**
     * Returns the given "text" overlaid and to the right of the space string.
     * @param text The string of data. This would be one cell's value data.
     * @param space A string into which to put the text. Usually space would be a String of spaces.
     * @return space, same width as given, with text in it and right justified.
     */
    protected static String rightJustifyText (String text, String space)
    {
        return space.substring (text.length()) + text;
    }

    /**
     * Thinking of the namedValues system as a matrix of strings, return whether 
     * the given cell is a label cell or not. Basically, if the system is column oriented,
     * is it the first row.
     * @param row
     * @param col
     * @return true if the cell value should be treated as a label.
     */
    abstract public boolean isLabelCell (int row, int col);

    /**
     * Thinking of the namedValues system as a matrix of strings, return a given cell's string data
     * @param row Specifies the index of the vector of values that were added to the NamedValues system,
     * starting from 0 being the first name and value vector added.
     * @param col Specifies the index of a column, where 0 indicates the name, and >0 indicates one of
     * the values associated with that name.
     * @return The cell's string data.
     */
    abstract public String getCell (int row, int col);
    
    /**
     * Thinking of the namedValues system as a matrix of strings, return a given cell's string data
     * formatted in a string according to whether the system as a whole would be printed in column
     * or row formatting.
     * @param row Specifies the index of the vector of values that were added to the NamedValues system,
     * starting from 0 being the first name and value vector added.
     * @param col Specifies the index of a column, where 0 indicates the name, and >0 indicates one of
     * the values associated with that name.
     * @return The cell's string data, formatted appropriately as being printed for the system as a whole.
     */
    abstract public String getFormattedCell (int row, int col, String space);
    
    /**
     * Thinking of the namedValues system as a matrix of strings, reset the internal row and column
     * count based on things like whether labels would be printed (if not, that's one less row).
     */
    abstract protected void updateDimensions ();
}
