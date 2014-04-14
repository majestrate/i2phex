/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2007 Phex Development Group
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
 *  --- SVN Information ---
 *  $Id: SearchMonitorTab.java 4128 2008-03-01 19:12:47Z complication $
 */
package phex.gui.tabs.search.monitor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import phex.gui.common.FWElegantPanel;
import phex.gui.common.GUIRegistry;
import phex.gui.common.IntegerTextField;
import phex.gui.common.MainFrame;
import phex.gui.common.table.FWTable;
import phex.gui.tabs.FWTab;
import phex.query.QueryHistoryMonitor;
import phex.utils.Localizer;
import phex.xml.sax.gui.DGuiSettings;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 */
public class SearchMonitorTab extends FWTab
{
    private final QueryHistoryMonitor queryHistory;
    
    private JCheckBox enableMonitorCheckbox;
    private JTextField numberOfMonitorRows;
    private QueryHistoryMonitorTableModel queryHistoryModel;
    private JTable mMonitorTable;
    private JScrollPane monitorTableScrollPane;

    
    public SearchMonitorTab( QueryHistoryMonitor queryHistory )
    {
        super( MainFrame.SEARCH_MONITOR_TAB_ID,
            Localizer.getString( "SearchMonitorTab_SearchMonitor" ),
            GUIRegistry.getInstance().getPlafIconPack().getIcon( "SearchMonitor.Tab" ),
            Localizer.getString( "SearchMonitorTab_TTTSearchMonitor" ),
            Localizer.getChar( "SearchMonitorTab_Mnemonic"),
            KeyStroke.getKeyStroke( Localizer.getString(
            "SearchMonitorTab_Accelerator" ) ),
            MainFrame.SEARCH_MONITOR_TAB_INDEX);
        this.queryHistory = queryHistory;
    }
    
    public void initComponent( DGuiSettings guiSettings )
    {
        CellConstraints cc = new CellConstraints();
        FormLayout tabLayout = new FormLayout("2dlu, fill:d:grow, 2dlu", // columns
            "2dlu, fill:p:grow, 2dlu"); //rows
        PanelBuilder tabBuilder = new PanelBuilder(tabLayout, this);
        JPanel contentPanel = new JPanel();
        FWElegantPanel banner = new FWElegantPanel( 
            Localizer.getString("SearchMonitorTab_SearchMonitor"), contentPanel );
        tabBuilder.add(banner, cc.xy(2, 2));
        
        FormLayout contentLayout = new FormLayout(
            "fill:d:grow", // columns
            "p, 1dlu, fill:d:grow"); //rows
        PanelBuilder contentBuilder = new PanelBuilder(contentLayout, contentPanel);
            
        JPanel historyHeader = new JPanel( );
        contentBuilder.add( historyHeader, cc.xy( 1, 1 ) );
        FormLayout headerLayout = new FormLayout(
            "d, fill:d:grow, d, 1dlu, d, 1dlu, d", // columns
            "p"); //rows
        PanelBuilder headerBuilder = new PanelBuilder( headerLayout, historyHeader );
        
        enableMonitorCheckbox = new JCheckBox( Localizer.getString(
            "SearchMonitorTab_enable" ) );
        enableMonitorCheckbox.setSelected( queryHistory.isHistoryMonitored() );
        enableMonitorCheckbox.addActionListener( new EnableMonitorActionListener() );
        headerBuilder.add( enableMonitorCheckbox, cc.xy( 1, 1 ) );
        
        headerBuilder.addLabel( Localizer.getString( "SearchMonitorTab_Show" ),
            cc.xy( 3, 1 ) );
        numberOfMonitorRows = new IntegerTextField(
            String.valueOf( queryHistory.getMaxHistorySize() ), 3, 3 );
        numberOfMonitorRows.getDocument().addDocumentListener(
            new MonitorRowsDocumentListener() );
        headerBuilder.add( numberOfMonitorRows, cc.xy( 5, 1 ) );
        headerBuilder.addLabel( Localizer.getString( "SearchMonitorTab_Rows" ),
            cc.xy( 7, 1 ) );

        queryHistoryModel = new QueryHistoryMonitorTableModel( queryHistory );
        mMonitorTable = new JTable( queryHistoryModel );
        monitorTableScrollPane = FWTable.createFWTableScrollPane( mMonitorTable );
        contentBuilder.add( monitorTableScrollPane, cc.xy( 1, 3 ) );
    }
    
    /**
     * Indicates if this tab is visible by default, when there is no known 
     * visible setting from the user.
     * @return true if visible by default false otherwise.
     */
    @Override
    public boolean isVisibleByDefault()
    {
        return false;
    }
    
    @Override
    public void updateUI()
    {
        super.updateUI();
        if ( monitorTableScrollPane != null )
        {
            FWTable.updateFWTableScrollPane( monitorTableScrollPane );
        }
    }
    
    private class EnableMonitorActionListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            queryHistory.setHistoryMonitored(
                enableMonitorCheckbox.isSelected() );
        }
    }
    
    private class MonitorRowsDocumentListener implements DocumentListener
    {
        public void insertUpdate(DocumentEvent documentevent)
        {
            updateMonitorRows();
        }

        public void removeUpdate(DocumentEvent documentevent)
        {
            updateMonitorRows();
        }

        public void changedUpdate(DocumentEvent documentevent)
        {
        }

        private void updateMonitorRows()
        {
            String rowsStr = numberOfMonitorRows.getText();
            try
            {
                int rows = Integer.parseInt( rowsStr );
                rows = Math.max( 1, rows );
                queryHistory.setMaxHistroySize( rows );
            }
            catch ( NumberFormatException sandra )
            {// ignore
            }
        }
    }
}
