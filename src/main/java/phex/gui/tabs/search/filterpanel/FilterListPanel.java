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
 *  Created on 18.02.2006
 *  --- CVS Information ---
 *  $Id: FilterListPanel.java 4128 2008-03-01 19:12:47Z complication $
 */
package phex.gui.tabs.search.filterpanel;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.table.*;

import phex.gui.actions.FWAction;
import phex.gui.common.table.FWTable;
import phex.gui.dialogs.filter.AdvSearchRulesDialog;
import phex.gui.tabs.search.SearchResultsDataModel;
import phex.rules.Rule;
import phex.rules.SearchFilterRules;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FilterListPanel extends JPanel
{
    private final SearchFilterRules filterRules;
    private FilterListTableModel filterListTableModel;
    private FWTable searchRuleTable;
    
    public FilterListPanel( SearchFilterRules rules )
    {
        super();
        this.filterRules = rules;
        initializeComponent( );
        updateUI();
    }

    /**
     * 
     */
    private void initializeComponent( )
    {
        setOpaque(false);
        
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout(
            "18dlu, fill:d:grow, 2dlu, d, 6dlu", // columns
            "6dlu, p, 6dlu," ); // rows
        PanelBuilder panelBuilder = new PanelBuilder( layout, this );
        
        filterListTableModel = new FilterListTableModel( this, filterRules );
        searchRuleTable = new FWTable( filterListTableModel );
        searchRuleTable.setVisibleRowCount( 3 );
        searchRuleTable.setShowVerticalLines(false);
        JTableHeader tableHeader = searchRuleTable.getTableHeader();
        tableHeader.setResizingAllowed(false);
        tableHeader.setReorderingAllowed(false);
        // adjust column width of checkbox
        JCheckBox box = (JCheckBox) searchRuleTable.getDefaultRenderer(Boolean.class);
        TableColumn column = searchRuleTable.getColumnModel().getColumn(0);
        column.setMaxWidth( box.getPreferredSize().width+2 );
        column.setMinWidth( box.getPreferredSize().width+2 );
        
        
        TableCellRenderer headerRenderer = searchRuleTable.getTableHeader().getDefaultRenderer();
        if ( headerRenderer instanceof JLabel )
        {
            ((JLabel)headerRenderer).setHorizontalAlignment(JLabel.LEFT);
        }
        column = searchRuleTable.getColumnModel().getColumn(1);
        column.setCellRenderer( new RuleCellRenderer() );
        
        JScrollPane scrollPane = FWTable.createFWTableScrollPane( searchRuleTable );
        panelBuilder.add( scrollPane, cc.xy(2, 2));
        
        //ButtonActionHandler btnActionHandler = new ButtonActionHandler();
        
        JButton btn = new JButton( new AdvancedFilterAction() );
        panelBuilder.add( btn, cc.xy(4, 2) );        
    }
    
    public void setDisplayedSearch( SearchResultsDataModel searchResultsDataModel )
    {
        if ( searchResultsDataModel == null )
        {
            searchRuleTable.setEnabled( false );
            searchRuleTable.clearSelection();
        }
        else
        {
            searchRuleTable.setEnabled( true );
        }
        
        filterListTableModel.setDisplayedSearch( searchResultsDataModel );
    }
    
    public class RuleCellRenderer extends DefaultTableCellRenderer
    {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column)
        {
            super.getTableCellRendererComponent(table, value, isSelected,
                hasFocus, row, column );
            if ( value instanceof Rule )
            {
                setText( ((Rule)value).getName() );
            }
            return this;
        }
    }
    
    private class AdvancedFilterAction extends FWAction
    {
        public AdvancedFilterAction()
        {
            super( Localizer.getString( "SearchTab_EditFilterRules" ), null,
                Localizer.getString( "SearchTab_TTTEditFilterRules" ) );
            refreshActionState();
        }

        public void actionPerformed( ActionEvent e )
        {
            new AdvSearchRulesDialog( filterRules ).setVisible( true );
        }

        @Override
        public void refreshActionState()
        {
            setEnabled( true );
        }
    }
}
