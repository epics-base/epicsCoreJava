package org.epics.pvdata.util.namedValues;

public class NamedValuesRowFormatter extends NamedValuesFormatter
{
    public String getCell (int row, int col)
    {
        if (getWhetherDisplayLabels())
            if (col == 0)
                return labels[row];
            else --col;

        if (col >= cells[row].length)
            return "";

        return cells[row][col];
    }


    public String getFormattedCell (int row, int col, String space)
    {
        if (isLabelCell (row, col))
            return leftJustifyText (getCell (row, col), space);
        
        return rightJustifyText (getCell (row, col), space);
    }


    public boolean isLabelCell (int row, int col)
    {
        return ((col == 0) && getWhetherDisplayLabels());
    }


    public void updateDimensions ()
    {
        numRows = cells.length;
        
        numCols = 0;
        for (int i = 0; i < cells.length; ++i)
            if (cells[i].length > numCols)
                numCols = cells[i].length;

        if (getWhetherDisplayLabels())
            ++numCols;
    }
}
