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
 *  Created on 19.02.2006
 *  --- CVS Information ---
 *  $Id: FilterListTableModel.java 4128 2008-03-01 19:12:47Z complication $
 */
package phex.gui.tabs.search.filterpanel;

import javax.swing.table.AbstractTableModel;

import phex.gui.tabs.search.SearchResultsDataModel;
import phex.query.QueryManager;
import phex.rules.Rule;
import phex.rules.SearchFilterRules;
import phex.utils.Localizer;

public class FilterListTableModel extends AbstractTableModel
{
    private FilterListPanel filterListPanel;
    private SearchFilterRules rules;
    private SearchResultsDataModel displayedDataModel;
    
    public FilterListTableModel( FilterListPanel panel, SearchFilterRules rules )
    {
        filterListPanel = panel;
        this.rules = rules;
    }
    
    public void setDisplayedSearch( SearchResultsDataModel dataModel )
    {
        // otherwise no need to update...
        if ( displayedDataModel != dataModel )
        {
            displayedDataModel = dataModel;
            fireTableDataChanged( );
        }
    }

    public int getColumnCount()
    {
        return 2;
    }

    public int getRowCount()
    {
        return rules.getCount();
    }

    public Object getValueAt( int rowIndex, int columnIndex )
    {
        Rule rowRule = rules.getRuleAt(rowIndex);
        switch ( columnIndex )
        {
        case 0:
            if ( displayedDataModel == null )
            {
                return Boolean.FALSE;
            }
            return displayedDataModel.isRuleActive( rowRule ) ? Boolean.TRUE : Boolean.FALSE;
        case 1:
            return rules.getRuleAt(rowIndex);
        }
        return "";
    }

    public String getColumnName( int column )
    {
        switch ( column )
        {
        case 1:
            return Localizer.getString("SearchTab_SelectRulesToActivate");
        }
        return " ";
    }

    public Class getColumnClass( int col )
    {
        switch ( col )
        {
        case 0:
            return Boolean.class;
        case 1:
            return Rule.class;
        }
        return Object.class;
    }

    public boolean isCellEditable( int row, int col )
    {
        return (col == 0) && displayedDataModel != null;
    }

    public void setValueAt( Object aValue, int row, int column )
    {
        Boolean boolVal = (Boolean)aValue;
        Rule rowRule = rules.getRuleAt( row );
        if ( boolVal.booleanValue() )
        {
            displayedDataModel.activateRule( rowRule );
        }
        else
        {
            displayedDataModel.deactivateRule( rowRule );
        }
    }
}
