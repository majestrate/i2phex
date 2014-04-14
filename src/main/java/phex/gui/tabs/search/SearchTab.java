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
 *  --- CVS Information ---
 *  $Id: SearchTab.java 4362 2009-01-16 10:27:18Z gregork $
 */
package phex.gui.tabs.search;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import phex.download.swarming.SwarmingManager;
import phex.gui.actions.FWAction;
import phex.gui.actions.FWToggleAction;
import phex.gui.common.FWElegantPanel;
import phex.gui.common.GUIRegistry;
import phex.gui.common.GUIUtils;
import phex.gui.common.MainFrame;
import phex.gui.tabs.FWTab;
import phex.gui.tabs.search.cp.SearchControlPanel;
import phex.gui.tabs.search.filterpanel.SearchFilterPanel;
import phex.query.Search;
import phex.query.SearchContainer;
import phex.rules.SearchFilterRules;
import phex.utils.Localizer;
import phex.xml.sax.gui.DGuiSettings;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 *
 */
public class SearchTab extends FWTab
{
    private final SearchContainer searchContainer;
    private final SearchFilterRules filterRules;
    private final SwarmingManager downloadService;
    
    private List<FWAction> viewMenuActionList;
    private SearchResultsDataModel displayedDataModel;
    
    private SearchButtonBar searchButtonBar;
    private JPanel buttonBarContainer;
    
    private JPanel contentPanel;
    private PanelBuilder contentBuilder;
    private JPanel mainSearchPanel;
    private JSplitPane searchListSplitPane;
    
    private SearchListPanel searchListPanel;
    
    private JPanel lowerRightPanel;
    private PanelBuilder lowerRightBuilder;
    private SearchFilterPanel searchFilterPanel;
    private SearchControlPanel searchControlPanel;
    private SearchResultsPanel searchResultPanel;
    
    public SearchTab( SearchContainer searchContainer, SearchFilterRules filterRules, SwarmingManager downloadService )
    {
        super( MainFrame.SEARCH_TAB_ID, Localizer.getString( "Search" ),
            GUIRegistry.getInstance().getPlafIconPack().getIcon( "Search.Tab" ),
            Localizer.getString( "TTTSearchTab" ), Localizer.getChar(
            "SearchMnemonic"), KeyStroke.getKeyStroke( Localizer.getString(
            "SearchAccelerator" ) ), MainFrame.SEARCH_TAB_INDEX );
        if ( downloadService == null )
        {
            throw new NullPointerException( "DownloadService missing" );
        }
        if ( searchContainer == null )
        {
            throw new NullPointerException( "searchContainer missing" );
        }
        if ( filterRules == null )
        {
            throw new NullPointerException( "filterRules missing" );
        }
        this.searchContainer = searchContainer;
        this.filterRules = filterRules;
        this.downloadService = downloadService;
    }
    
    public void initComponent( DGuiSettings guiSettings )
    {
        boolean isSearchBarVisible = true;
        if ( guiSettings != null && guiSettings.isSetSearchBarVisible() )
        {
            isSearchBarVisible = guiSettings.isSearchBarVisible();
        }
        boolean isSearchListVisible = false;
        if ( guiSettings != null && guiSettings.isSetSearchListVisible() )
        {
            isSearchListVisible = guiSettings.isSearchListVisible();
        }
        boolean isFilterPanelVisible = true;
        if ( guiSettings != null && guiSettings.isSetSearchFilterPanelVisible() )
        {
            isFilterPanelVisible = guiSettings.isSearchFilterPanelVisible();
        }
        
        ToggleSearchFilterAction filterPanelToggleAction = 
            new ToggleSearchFilterAction( isFilterPanelVisible );
        addTabAction( FILTER_PANEL_TOGGLE_ACTION, filterPanelToggleAction );
        addTabAction( CLEAR_SEARCH_RESULTS_ACTION, new ClearSearchResultsAction() );
        addTabAction( CREATE_NEW_SEARCH_ACTION, new CreateNewSearchAction() );
        addTabAction( CLOSE_SEARCH_ACTION, new CloseSearchAction() );
        
        CellConstraints cc = new CellConstraints();
        FormLayout tabLayout = new FormLayout("2dlu, fill:d:grow, 2dlu", // columns
            "0dlu, p, 2dlu, fill:p:grow, 2dlu"); //rows
        PanelBuilder tabBuilder = new PanelBuilder(tabLayout, this);
        
        buttonBarContainer = new JPanel( new BorderLayout() );
        tabBuilder.add( buttonBarContainer, cc.xy(2, 2) );
        
        contentPanel = new JPanel();
        FWElegantPanel banner = new FWElegantPanel( Localizer.getString("Search"),
            contentPanel );
        
        tabBuilder.add(banner, cc.xy(2, 4));
        
        JPanel filterTogglePanel = new JPanel( new FlowLayout() );
        filterTogglePanel.setOpaque(false);
        JButton activateFilterToggle = new JButton( filterPanelToggleAction );
        activateFilterToggle.setText(null);
        activateFilterToggle.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
        activateFilterToggle.setBorder( GUIUtils.ROLLOVER_BUTTON_BORDER );
        activateFilterToggle.setRolloverEnabled( true );
        activateFilterToggle.setOpaque(false);
        filterTogglePanel.add(activateFilterToggle);
        
        banner.addHeaderPanelComponent( filterTogglePanel, BorderLayout.EAST);
        
        FormLayout contentLayout = new FormLayout(
            "fill:d:grow", // columns
            "fill:d:grow"); //rows
        contentBuilder = new PanelBuilder(contentLayout, contentPanel);
        
        searchListPanel = new SearchListPanel( this, searchContainer );
        searchListPanel.initializeComponent( guiSettings );
        
        mainSearchPanel = new JPanel();
        FormLayout lowerLayout = new FormLayout(
            "p, 1dlu, fill:d:grow", // columns
            "fill:d:grow"); //rows
        PanelBuilder lowerBuilder = new PanelBuilder( lowerLayout, mainSearchPanel );
        //searchControlPanel = new SearchControlPanel( this );
        searchControlPanel = new SearchControlPanel( this, searchContainer,
            filterRules );
        searchControlPanel.setMinimumSize(new Dimension(0,0));
        lowerBuilder.add( searchControlPanel, cc.xy( 1, 1  ) );
        
        lowerRightPanel = new JPanel();
        FormLayout lowerRightLayout = new FormLayout(
            "fill:d:grow", // columns
            "p, fill:d:grow"); //rows
        lowerRightBuilder = new PanelBuilder( lowerRightLayout, lowerRightPanel );
        searchFilterPanel = new SearchFilterPanel( this, filterRules );
        lowerRightBuilder.add( searchFilterPanel, cc.xy( 1, 1  ) );
        
        searchResultPanel = new SearchResultsPanel( this, downloadService );
        searchResultPanel.initializeComponent( guiSettings );
        lowerRightBuilder.add( searchResultPanel, cc.xy( 1, 2 ) );
        
        lowerBuilder.add( lowerRightPanel, cc.xy( 3, 1  ) );
        
        Dimension dim = new Dimension( 400, 200 );
        mainSearchPanel.setPreferredSize( dim );
        mainSearchPanel.setMinimumSize( new Dimension( 0, 0 ) );
        searchListPanel.setMinimumSize( new Dimension( 0, 0 ) );
        
        searchListSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT,
            searchListPanel, mainSearchPanel );
        searchListSplitPane.setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 0) );
        searchListSplitPane.setDividerSize( 4 );
        searchListSplitPane.setOneTouchExpandable( false );
        
        
        setSearchButtonBarVisible( isSearchBarVisible );
        setSearchListVisible( isSearchListVisible );
        setFilterPanelVisible( isFilterPanelVisible );
        
        List<FWAction> actionList = new ArrayList<FWAction>();
        actionList.add( new ToggleSearchBarAction( isSearchBarVisible ) );
        actionList.add( new ToggleSearchListAction( isSearchListVisible ) );
        actionList.add( filterPanelToggleAction );
        viewMenuActionList = Collections.unmodifiableList(actionList);
    }
    
    public void setFilterPanelVisible( boolean state )
    {
        CellConstraints cc = new CellConstraints();
        if ( state )
        {
            
            lowerRightPanel.removeAll();
            lowerRightBuilder.add( searchFilterPanel, cc.xy( 1, 1  ) );
            lowerRightBuilder.add( searchResultPanel, cc.xy( 1, 2 ) );
        }
        else
        {
            lowerRightPanel.removeAll();
            lowerRightBuilder.add( searchResultPanel, cc.xy( 1, 2 ) );
        }
        validate();
    }
    
    public void setSearchButtonBarVisible( boolean state )
    {
        if( state )
        {
            buttonBarContainer.removeAll();
            searchButtonBar = new SearchButtonBar( this, searchContainer );
            buttonBarContainer.add( searchButtonBar, BorderLayout.CENTER );
            validate();
        }
        else
        {
            buttonBarContainer.removeAll();
            validate();
            searchButtonBar = null;
        }
    }
    
    public void setSearchListVisible( boolean state )
    {
        CellConstraints cc = new CellConstraints();
        if ( state )
        {
            contentPanel.removeAll();
            searchListSplitPane.setLeftComponent( searchListPanel );
            searchListSplitPane.setRightComponent( mainSearchPanel );
            contentBuilder.add( searchListSplitPane, cc.xy( 1, 1 ) );
            validate();
        }
        else
        {
            contentPanel.removeAll();
            contentBuilder.add( mainSearchPanel, cc.xy( 1, 1 ) );
            validate();
        }
    }
    
    /**
     * Updates the displayedSearch for parts of the search UI. 
     * @param search the displayed search to show on all parts of the search UI.
     */
    public void setDisplayedSearch( final SearchResultsDataModel searchResultsDataModel )
    {
        if ( displayedDataModel == searchResultsDataModel )
        {
            return;
        }
        displayedDataModel = searchResultsDataModel;
        if ( searchButtonBar != null )
        {
            searchButtonBar.setDisplayedSearch( displayedDataModel );
        }
        searchResultPanel.setDisplayedSearch( displayedDataModel );
        searchListPanel.setDisplayedSearch( displayedDataModel );
        searchFilterPanel.setDisplayedSearch( displayedDataModel );
        searchControlPanel.setDisplayedSearch( displayedDataModel );
        refreshTabActions();
    }
    
    /**
     * Clears the search history in the search control panel and configuration.
     */
    public void clearSearchHistory()
    {
        searchControlPanel.clearSearchHistory();
    }

    @Override
    public void appendDGuiSettings( DGuiSettings dSettings )
    {
        super.appendDGuiSettings( dSettings );
        dSettings.setSearchBarVisible( searchButtonBar != null );
        dSettings.setSearchFilterPanelVisible( searchFilterPanel.getParent() != null );
        dSettings.setSearchListVisible( searchListSplitPane.getParent() != null );
        searchListPanel.appendDGuiSettings( dSettings );
        searchResultPanel.appendDGuiSettings( dSettings );
    }
    
    @Override
    public List<FWAction> getViewMenuActions()
    {
        return viewMenuActionList;
    }
    
    public void closeSearch( Search search )
    {
        searchContainer.removeSearch( search );
        if ( displayedDataModel != null && displayedDataModel.getSearch() == search )
        {
            setDisplayedSearch( null );
        }
        SearchResultsDataModel.unregisterSearch(search);
    }
    
    /////////////////////// Start Tab Actions///////////////////////////////////
    
    public static final String FILTER_PANEL_TOGGLE_ACTION = "FilterPanelToggleAction";
    public static final String CLEAR_SEARCH_RESULTS_ACTION = "ClearSearchResultsAction";
    public static final String CREATE_NEW_SEARCH_ACTION = "CreateNewSearchAction";
    public static final String CLOSE_SEARCH_ACTION = "CloseSearchAction";
    public static final String ADVANCED_FILTER_ACTION = "AdvancedFilterAction";
    

    private class CloseSearchAction extends FWAction
    {
        public CloseSearchAction()
        {
            super( Localizer.getString( "CloseSearch" ),
                GUIRegistry.getInstance().getPlafIconPack().getIcon("Search.Close"),
                Localizer.getString( "TTTCloseSearch" ), null, 
                KeyStroke.getKeyStroke( KeyEvent.VK_DELETE, 0 ) );
            refreshActionState();
        }

        public void actionPerformed( ActionEvent e )
        {
            if ( displayedDataModel != null )
            {
                closeSearch( displayedDataModel.getSearch() );
            }
        }

        @Override
        public void refreshActionState()
        {
            if ( displayedDataModel == null )
            {
                setEnabled( false );
            }
            else
            {
                setEnabled( true );
            }
        }
    }
    
    private class ClearSearchResultsAction extends FWAction
    {
        public ClearSearchResultsAction()
        {
            super( Localizer.getString( "ClearSearchResults" ),
                GUIRegistry.getInstance().getPlafIconPack().getIcon("Search.Clear"),
                Localizer.getString( "TTTClearSearchResults" ) );
            refreshActionState();
        }

        public void actionPerformed( ActionEvent e )
        {
            if ( displayedDataModel != null )
            {
                displayedDataModel.clearSearchResults();
            }
        }

        @Override
        public void refreshActionState()
        {
            if ( displayedDataModel == null ||
                  displayedDataModel.getSearchElementCount()
                + displayedDataModel.getFilteredElementCount() == 0)
            {
                setEnabled( false );
            }
            else
            {
                setEnabled( true );
            }
        }
    }
    
    private class CreateNewSearchAction extends FWAction
    {
        public CreateNewSearchAction()
        {
            super( Localizer.getString( "CreateNewSearch" ),
                GUIRegistry.getInstance().getPlafIconPack().getIcon("Search.Search"),
                Localizer.getString( "TTTCreateNewSearch" ) );
            refreshActionState();
        }

        public void actionPerformed( ActionEvent e )
        {
            setDisplayedSearch( null );
        }

        @Override
        public void refreshActionState()
        {
            setEnabled( true );
        }
    }
    
    public class ToggleSearchBarAction extends FWToggleAction
    {
        /**
         *
         */
        public ToggleSearchBarAction( boolean isButtonbarVisible )
        {
            super( Localizer.getString( "SearchTab_ToggleSearchBarAction" ),
                   null, null, null,  null, null );
            setSelected( isButtonbarVisible );
            updateTooltip();
        }
        
        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            boolean state = !isSelected();
            setSelected( state );
            setSearchButtonBarVisible( state );
            updateTooltip();
        }
        
        private void updateTooltip()
        {
            if ( isSelected() )
            {
                setToolTipText( Localizer.getString( "SearchTab_TTTToggleSearchBarActionHide" ) );
            }
            else
            {
                setToolTipText( Localizer.getString( "SearchTab_TTTToggleSearchBarActionShow" ) );
            }
        }
        
        /**
         * @see phex.gui.actions.FWAction#refreshActionState()
         */
        @Override
        public void refreshActionState()
        {// global actions are not refreshed
        }
    }
    
    public class ToggleSearchListAction extends FWToggleAction
    {
        /**
         *
         */
        public ToggleSearchListAction( boolean isSearchListVisible )
        {
            super( Localizer.getString( "SearchTab_ToggleSearchListAction" ),
                   null, null, null,  null, null );
            setSelected( isSearchListVisible );
            updateTooltip();
        }
        
        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            boolean state = !isSelected();
            setSelected( state );
            setSearchListVisible( state );
            updateTooltip();
        }
        
        private void updateTooltip()
        {
            if ( isSelected() )
            {
                setToolTipText( Localizer.getString( "SearchTab_TTTToggleSearchListActionHide" ) );
            }
            else
            {
                setToolTipText( Localizer.getString( "SearchTab_TTTToggleSearchListActionShow" ) );
            }
        }
        
        /**
         * @see phex.gui.actions.FWAction#refreshActionState()
         */
        @Override
        public void refreshActionState()
        {// global actions are not refreshed
        }
    }
    
    public class ToggleSearchFilterAction extends FWToggleAction
    {
        /**
         *
         */
        public ToggleSearchFilterAction( boolean isSearchFilterVisible )
        {
            super( Localizer.getString( "SearchTab_ToggleSearchFilterAction" ),
                GUIRegistry.getInstance().getPlafIconPack().getIcon( "Search.Filter" ),
                null, null,  null, null );
            setSelected( isSearchFilterVisible );
            updateTooltip();
        }
        
        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            boolean state = !isSelected();
            setSelected( state );
            setFilterPanelVisible( state );
            updateTooltip();
        }
        
        private void updateTooltip()
        {
            if ( isSelected() )
            {
                setToolTipText( Localizer.getString( "SearchTab_TTTToggleSearchFilterActionHide" ) );
            }
            else
            {
                setToolTipText( Localizer.getString( "SearchTab_TTTToggleSearchFilterActionShow" ) );
            }
        }
        
        /**
         * @see phex.gui.actions.FWAction#refreshActionState()
         */
        @Override
        public void refreshActionState()
        {// global actions are not refreshed
        }
    }
    
    /////////////////////// End Tab Actions///////////////////////////////////
}