/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2006 Phex Development Group
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Created on 24.11.2005
 *  --- CVS Information ---
 *  $Id: ConditionTableModel.java 3682 2007-01-09 15:32:14Z gregork $
 */
package phex.gui.dialogs.filter.wizard.condition;

import javax.swing.table.AbstractTableModel;

import phex.rules.condition.*;

public class ConditionTableModel extends AbstractTableModel
{
    protected Object[][] conditions = new Object[][]
    {
        { Boolean.FALSE, new FilenameCondition() },
        { Boolean.FALSE, new FileSizeCondition() },
        { Boolean.FALSE, new FileUrnCondition() },
        { Boolean.FALSE, new MediaTypeCondition() } 
    };
    
    private ConditionPanel conditionPanel;
    
    public ConditionTableModel( ConditionPanel panel )
    {
        conditionPanel = panel;
    }

    public int getColumnCount()
    {
        return 2;
    }

    public int getRowCount()
    {
        return conditions.length;
    }

    public Object getValueAt( int rowIndex, int columnIndex )
    {
        return conditions[rowIndex][columnIndex];
    }

    public String getColumnName( int column )
    {
        return "";
    }

    public Class getColumnClass( int col )
    {
        switch ( col )
        {
        case 0:
            return Boolean.class;
        case 1:
            return String.class;
        }
        return Object.class;
    }

    public boolean isCellEditable( int row, int col )
    {
        return (col == 0);
    }

    public void setValueAt( Object aValue, int row, int column )
    {
        // we dont set values directly instead we notify the condition panel
        // to create a new rule this will then update the table model from scratch
        conditionPanel.ruleStatusChanged( conditions[row][1].getClass(),
            ((Boolean)aValue).booleanValue() );        
    }
    
    /**
     * Returns the index of the row that represents the given condition type
     * @param condition
     * @return
     */
    public int getRowOf( Condition condition )
    {
        for ( int i=0; i < conditions.length; i++ )
        {
            if ( conditions[i][1].getClass() == condition.getClass() )
            {
                return i;
            }
        }
        return -1;
    }
}
