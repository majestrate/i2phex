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
 *  --- CVS Information ---
 *  $Id: SearchListPanel.java 4128 2008-03-01 19:12:47Z complication $
 */
package phex.gui.tabs.search;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import phex.common.log.NLogger;
import phex.gui.actions.FWAction;
import phex.gui.common.GUIRegistry;
import phex.gui.common.GUIUtils;
import phex.gui.common.table.FWSortedTableModel;
import phex.gui.common.table.FWTable;
import phex.query.Search;
import phex.query.SearchContainer;
import phex.xml.sax.gui.DGuiSettings;
import phex.xml.sax.gui.DTable;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SearchListPanel extends JPanel
{
    private static final String SEARCHLIST_TABLE_IDENTIFIER = "SearchListTable";
    
    private SearchContainer searchContainer;
    private SearchTab searchTab;
    
    private FWTable searchListTable;
    private JScrollPane searchListTableScrollPane;
    private SearchListTableModel searchListModel;
    private JPopupMenu searchListPopup;
    
    public SearchListPanel( SearchTab tab, SearchContainer searchContainer )
    {
        super( );
        this.searchContainer = searchContainer;
        searchTab = tab;
    }
    
    public void initializeComponent( DGuiSettings guiSettings )
    {
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout(
            "fill:d:grow", // columns
            "fill:d:grow"); //rows
        PanelBuilder panelBuilder = new PanelBuilder( layout, this );
        
        MouseHandler mouseHandler = new MouseHandler();
        
        searchListModel = new SearchListTableModel( searchContainer );
        searchListModel.addTableModelListener( new SearchListTableListener() );
        
        searchListTable = new FWTable( new FWSortedTableModel( searchListModel ) );
        GUIUtils.updateTableFromDGuiSettings( guiSettings, searchListTable, 
            SEARCHLIST_TABLE_IDENTIFIER );
        
        searchListTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        searchListTable.activateAllHeaderActions();
        searchListTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        searchListTable.getSelectionModel().addListSelectionListener(new SelectionHandler());
        searchListTable.addMouseListener( mouseHandler );
        GUIRegistry.getInstance().getGuiUpdateTimer().addTable( searchListTable );
        searchListTableScrollPane = FWTable.createFWTableScrollPane( searchListTable );
        searchListTableScrollPane.addMouseListener( mouseHandler );
        
        panelBuilder.add( searchListTableScrollPane, cc.xy( 1, 1 ) );
        
        // init popup menu
        searchListPopup = new JPopupMenu();
        searchListPopup.add( searchTab.getTabAction(
            SearchTab.CREATE_NEW_SEARCH_ACTION ) );
        searchListPopup.add( searchTab.getTabAction(
            SearchTab.CLEAR_SEARCH_RESULTS_ACTION ) );
        
        FWAction closeSearchAction = searchTab.getTabAction(
            SearchTab.CLOSE_SEARCH_ACTION );
        searchListPopup.add( closeSearchAction );
        searchListTable.getActionMap().put( SearchTab.CLOSE_SEARCH_ACTION, closeSearchAction);
        searchListTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put( 
            (KeyStroke)closeSearchAction.getValue(FWAction.ACCELERATOR_KEY), SearchTab.CLOSE_SEARCH_ACTION );
    }
    
    public void setDisplayedSearch( SearchResultsDataModel searchResultsDataModel )
    {
        if ( searchResultsDataModel == null )
        {
            searchListTable.getSelectionModel().clearSelection();
            return;
        }
        int modelRow = searchContainer.getIndexOfSearch( searchResultsDataModel.getSearch() );
        if ( modelRow != -1 )
        {
            int viewRow = searchListTable.translateRowIndexToView( modelRow );
            if ( viewRow != -1 )
            {
                searchListTable.getSelectionModel().setSelectionInterval( viewRow, viewRow );
            }
            else
            {
                searchListTable.getSelectionModel().clearSelection();
            }
        }
        else
        {
            searchListTable.getSelectionModel().clearSelection();
        }
    }
    
    private Search getSelectedSearch()
    {
        int viewRow = searchListTable.getSelectedRow();
        if ( viewRow < 0 )
        {
            return null;
        }
        int modelRow = searchListTable.translateRowIndexToModel( viewRow );
        Search search = searchContainer.getSearchAt( modelRow );
        return search;
    }
    
    @Override
    public Dimension getPreferredSize()
    {
        Dimension pref = super.getPreferredSize();
        // show 3 rows plus the header plus a bit
        pref.height = (int)(searchListTable.getRowHeight() * 4.7);
        return pref;
    }
    
    /**
     * This is overloaded to update the combo box size on
     * every UI update. Like font size change!
     */
    @Override
    public void updateUI()
    {
        super.updateUI();
        if ( searchListTableScrollPane != null )
        {
            FWTable.updateFWTableScrollPane( searchListTableScrollPane );
        }
    }
    
    public void appendDGuiSettings( DGuiSettings dSettings )
    {
        DTable dTable = GUIUtils.createDTable( searchListTable, SEARCHLIST_TABLE_IDENTIFIER );
        dSettings.getTableList().getTableList().add( dTable );
    }
    
    ////////////////////////// Start inner classes ///////////////////////////////
    
    private class SearchListTableListener implements TableModelListener
    {
        public void tableChanged(TableModelEvent e)
        {
            if ( e.getType() == TableModelEvent.INSERT )
            {
                Search search = searchContainer.getSearchAt( e.getFirstRow() );
                SearchResultsDataModel dataModel = SearchResultsDataModel.lookupResultDataModel( search );
                searchTab.setDisplayedSearch( dataModel );
            }
        }
        
    }
    
    private class SelectionHandler implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            try
            {
	            if ( e.getValueIsAdjusting() )
	            {
	                return;
	            }
	            searchTab.refreshTabActions();
	            Search search = getSelectedSearch();
	            if ( search == null )
	            {
	                return;
	            }
	            SearchResultsDataModel dataModel = SearchResultsDataModel.lookupResultDataModel( search );
	            searchTab.setDisplayedSearch( dataModel );
            }
            catch ( Exception exp)
            {// catch all handler
                NLogger.error( SelectionHandler.class, exp, exp );
            }
        }
    }
    
    private class MouseHandler extends MouseAdapter implements MouseListener
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            try
            {
                if (e.getClickCount() == 2 && e.getSource() == searchListTableScrollPane)
                {
                    // handle double click as new search request
                    searchTab.setDisplayedSearch( null );
                }
            }
            catch (Throwable th)
            {
                NLogger.error( MouseHandler.class, th, th);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                popupMenu((Component)e.getSource(), e.getX(), e.getY());
            }
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                popupMenu((Component)e.getSource(), e.getX(), e.getY());
            }
        }

        private void popupMenu(Component source, int x, int y)
        {
            if ( source == searchListTable || source == searchListTableScrollPane )
            {
                searchTab.refreshTabActions();
                searchListPopup.show(source, x, y);
            }
        }
    }
    
    ////////////////////////// End inner classes ///////////////////////////////
}
