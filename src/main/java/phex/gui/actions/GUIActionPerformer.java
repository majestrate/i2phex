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
 *  $Id: GUIActionPerformer.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.actions;

import phex.common.Environment;
import phex.common.address.DestAddress;
import phex.gui.common.GUIRegistry;
import phex.gui.common.MainFrame;
import phex.gui.tabs.search.SearchResultsDataModel;
import phex.gui.tabs.search.SearchTab;
import phex.host.FavoritesContainer;
import phex.host.HostManager;
import phex.query.BrowseHostResults;
import phex.query.QueryManager;
import phex.query.SearchContainer;
import phex.rules.SearchFilterRules;
import phex.servent.Servent;
import phex.share.FileRescanRunner;

/**
 * A class containing access method with a defined interface to
 * run the functionality of actions called from different tabs.
 * Many tabs provide the same action functions like BrowseHost, 
 * even though each tab has its own way on how to enable/disable
 * and resolve these actions, the base functionality of the action
 * stays the same. This class provides a basis to hold this base 
 * functionality of actions.
 */
public class GUIActionPerformer
{
    /**
     * Initiates the browse host request and switches to the search tab.
     * @param hostAddress the host address to browse.
     */
    public static void browseHost( DestAddress hostAddress )
    {
        QueryManager queryService = GUIRegistry.getInstance().getServent().getQueryService();
        SearchContainer searchContainer = queryService.getSearchContainer();
        SearchFilterRules filterRules = queryService.getSearchFilterRules();
        
        BrowseHostResults result = searchContainer.createBrowseHostSearch(
            hostAddress, null );
        SearchResultsDataModel searchResultsDataModel = 
            SearchResultsDataModel.registerNewSearch( result, filterRules );
        MainFrame mainFrame = GUIRegistry.getInstance().getMainFrame();
        SearchTab searchTab = (SearchTab)mainFrame.getTab(
            MainFrame.SEARCH_TAB_ID );
        mainFrame.setSelectedTab( MainFrame.SEARCH_TAB_ID );
        searchTab.setDisplayedSearch( searchResultsDataModel );
    }
    
    public static void chatToHost( DestAddress hostAddress )
    {
        GUIRegistry.getInstance().getServent().getChatService().openChat( hostAddress );
    }
    
    public static void addHostsToFavorites( DestAddress[] addresses )
    {
        HostManager mgr = GUIRegistry.getInstance().getServent().getHostService();
        FavoritesContainer container = mgr.getFavoritesContainer();
        container.addFavorites( addresses );
    }
    
    public static void rescanSharedFiles()
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                FileRescanRunner.rescan( 
                    GUIRegistry.getInstance().getServent().getSharedFilesService(),
                    true, true);
            }
        };
        Environment.getInstance().executeOnThreadPool(runnable, "SharedFilesRescanExecute");
    }
}
