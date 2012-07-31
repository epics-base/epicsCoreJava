package org.epics.pvdata.util.namedValues;

/**
 * NamedValuesColumnFormatter is utility class
 * for printing the values in a NamedValues instance as a table.
 * <p>
 * For more details see NamedValuesFormatter. 
 * </p>
 * @see NamedValuesFormatter
 * @see NamedValuesRowFormatter
 * 
 * @author 11.11.11 Greg White (greg@slac.stanford.edu) and Chris Larrieu.
 */
public class NamedValuesColumnFormatter extends NamedValuesFormatter
{
    public String getCell (int row, int col)
    {
        if (getWhetherDisplayLabels())
            if (row == 0)
                return labels[col];
            else --row;

        if (row >= cells[col].length)
            return "";

        return cells[col][row];
    }


    public String getFormattedCell (int row, int col, String space)
    {
        if (isLabelCell (row, col))
            return centerText (getCell (row, col), space);
        
        return rightJustifyText (getCell (row, col), space);
    }


    public boolean isLabelCell (int row, int col)
    {
        return ((row == 0) && getWhetherDisplayLabels());
    }


    public void updateDimensions ()
    {
        numCols = cells.length;
        
        numRows = 0;
        for (int i = 0; i < cells.length; ++i)
            if (cells[i].length > numRows)
                numRows = cells[i].length;

        if (getWhetherDisplayLabels())
            ++numRows;
    }
}
