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
 *  $Id: ResultMonitorDataModel.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.search.monitor;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import org.bushe.swing.event.annotation.EventTopicSubscriber;

import phex.download.RemoteFile;
import phex.event.PhexEventTopics;
import phex.gui.common.GUIRegistry;
import phex.gui.models.ISearchDataModel;
import phex.gui.models.SearchTreeTableModel;
import phex.gui.tabs.search.SearchResultElement;
import phex.gui.tabs.search.SearchResultElementComparator;
import phex.query.SearchDataEvent;
import phex.query.SearchFilter;
import phex.servent.Servent;

/**
 * 
 */
public class ResultMonitorDataModel implements ISearchDataModel
{   
    /**
     * A list of all search results in form of RemoteFile. The list must be
     * locked before being modified.
     */
    private ArrayList<RemoteFile> allRemoteFiles;
    
    /**
     * A set to identify all search results.
     * The list allSearchResultSHA1Set must be locked before modifying this.
     */
    private HashSet<String> allSearchResultSHA1Set;
    
    /**
     * The number of SearchResult elements identified. To identify the count
     * the allSearchResultSHA1Set is used.
     */
    private int allSearchResultCount;
    
    /**
     * A list of sorted and filtered search results. This list must always be
     * in a sorted and filtered state. All inserts and removes must ensure that
     * the list maintains to be sorted and filtered. The list must be locked before
     * being modified.
     */
    private ArrayList<SearchResultElement> displayedSearchResults;
    
    /**
     * A map to ensure performant grouping of search results. The key of the 
     * map is the SHA1 value of the RemoteFile. The value is the SearchResultElement
     * that is the group of this SHA1.
     * The SHA1 value is used for grouping the RemoteFiles
     */
    private HashMap<String, SearchResultElement> displayedSearchResultSHA1Map;
    
    /**
     * The visualization model that needs to be updated on data model
     * changes or null if currently not visible.
     */
    private SearchTreeTableModel visualizationModel;
    
    private SearchResultElementComparator comparator;
    
    /**
     * The search filter used for local filtering.
     */
    protected SearchFilter searchFilter;
    
    /**
     * 
     */
    public ResultMonitorDataModel()
    {        
        allRemoteFiles = new ArrayList();
        allSearchResultSHA1Set = new HashSet();
        allSearchResultCount = 0;
        displayedSearchResults = new ArrayList();
        displayedSearchResultSHA1Map = new HashMap();
        comparator = new SearchResultElementComparator();
        searchFilter = null;
        
        GUIRegistry.getInstance().getServent().getEventService().processAnnotations( this );
    }
    
    public int getSearchElementCount()
    {
        return displayedSearchResults.size();
    }
    
    /**
     * Returns the number of hits that get locally filtered because:
     * - the file size is out of bounds
     * - the search string contains a filtered term
     * - the media type does not fit.
     */
    public int getFilteredElementCount()
    {   
        return allSearchResultCount - displayedSearchResults.size();
    }
    
    public SearchResultElement getSearchElementAt( int index )
    {
        if ( index < 0 || index >= displayedSearchResults.size() )
        {
            return null;
        }
        return displayedSearchResults.get( index );
    }
    
    private void addSearchResults( Object[] newSearchResults )
    {
        synchronized( allRemoteFiles )
        {
        synchronized( displayedSearchResults )
        {
            RemoteFile remoteFile;
            for ( int i = 0; i < newSearchResults.length; i++ )
            {
                remoteFile = (RemoteFile)newSearchResults[i];
                addSearchResultForDisplay( remoteFile );
                addSearchResultToAll( remoteFile );
            }
        }
        }
    }
    
    private void addSearchResultToAll( RemoteFile remoteFile )
    {
        allRemoteFiles.add( remoteFile );
        
        String sha1 = remoteFile.getSHA1();        
        boolean found = false;
        if ( sha1 != null )
        {
            found = allSearchResultSHA1Set.contains( sha1 );
        }
        if ( !found )
        {
            allSearchResultCount ++;
            if ( sha1 != null && sha1.length() > 0 )
            {
                allSearchResultSHA1Set.add( sha1 );
            } 
        }
    }

    private void addSearchResultForDisplay(RemoteFile remoteFile)
    {
        boolean isFiltered = isFiltered( remoteFile );
        if ( isFiltered )
        {
            return;
        }
        
        SearchResultElement resultElement = null;
        String sha1 = remoteFile.getSHA1();
        if ( sha1 != null )
        {
            resultElement = displayedSearchResultSHA1Map.get( sha1 );
        }
        if ( resultElement != null )
        {            
            resultElement.addRemoteFile( remoteFile );
            fireSearchResultAdded(remoteFile, resultElement);
        }
        else
        {
            resultElement = new SearchResultElement( remoteFile );
            // search for the right position to add this search result.
            int index = Collections.binarySearch( displayedSearchResults,
                resultElement, comparator );
            if (index <= 0)
            {
                if ( sha1 != null && sha1.length() > 0 )
                {
                    displayedSearchResultSHA1Map.put( sha1, resultElement );
                } 
                displayedSearchResults.add(-index-1, resultElement);
                fireNewSearchResultAdded(resultElement, -index-1);
            }
        }
    }
    
    /**
     * 
     * @param sortField Must be one of the SearchElementComparater static fields.
     */
    public void setSortBy( int sortField, boolean isSortedAscending )
    {
        synchronized( displayedSearchResults )
        {
            comparator.setSortField( sortField, isSortedAscending );
            Collections.sort( displayedSearchResults, comparator );
            fireAllSearchResultsChanged();
        }
    }
    
    /**
     * Sets the visualization model that needs to be updated on data model
     * changes or null if currently not visible.
     * @param model the new visualization model or null if not visible.
     */
    public void setVisualizationModel( SearchTreeTableModel model )
    {
        visualizationModel = model;
    }
    
    /**
     * Updates the used search filter and forces a filtering of the query hits. 
     * @param aSearchFilter the search filter to use.
     */
    public void updateSearchFilter( SearchFilter aSearchFilter )
    {
        synchronized( allRemoteFiles )
        {
        synchronized( displayedSearchResults )
        {
            searchFilter = aSearchFilter;
            searchFilter.setLastTimeUsed( System.currentTimeMillis() );
            updateFilteredQueryList();
        }
        }
    }
    
    /**
     * Clears all set filter conditions of the search filter.
     */
    public void clearSearchFilter()
    {
        synchronized( allRemoteFiles )
        {
        synchronized( displayedSearchResults )
        {
            searchFilter = null;
            updateFilteredQueryList();
        }
        }
    }
    
    /**
     * Clears all search results of the search. This includes filtered and not
     * filtered search results.
     */
    public void clearSearchResults()
    {
        synchronized( allRemoteFiles )
        {
        synchronized( displayedSearchResults )
        {
                allRemoteFiles.clear();
                allSearchResultSHA1Set.clear();
                allSearchResultCount = 0;
                displayedSearchResults.clear();
                displayedSearchResultSHA1Map.clear();
                fireAllSearchResultsChanged();
        }
        }
    }
    
    /**
     * Returns the used search filter, or null if no search
     * filter is used.
     * @return the used search filter, or null.
     */
    public SearchFilter getSearchFilter( )
    {
        return searchFilter;
    }
    
    private boolean isFiltered( RemoteFile remoteFile )
    {
        boolean isFiltered = false;
        if ( searchFilter != null )
        {
            isFiltered = searchFilter.isFiltered( remoteFile );
        }
        return isFiltered;
    }
    
    private void updateFilteredQueryList()
    {
        synchronized( allRemoteFiles )
        {
        synchronized( displayedSearchResults )
        {
            displayedSearchResultSHA1Map.clear();
            displayedSearchResults.clear();
            fireAllSearchResultsChanged();
            for (RemoteFile file : allRemoteFiles )
            {
                addSearchResultForDisplay( file );
            }
        }
        }
    }


    @EventTopicSubscriber(topic=PhexEventTopics.Search_Monitor_Results)
    public void onSearchDataEvent( String topic, SearchDataEvent event )
    {
        if ( event.getType() == SearchDataEvent.SEARCH_HITS_ADDED )
        {
            Object[] newSearchResults = event.getSearchData();
            addSearchResults( newSearchResults );
        }
    }
    
    ///////////////////////// START Event forwarding ///////////////////////////////
    
    private void fireAllSearchResultsChanged()
    {
        if ( visualizationModel == null )
        {
            return;
        }
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                Object[] path = new Object[]
                {
                    visualizationModel.getRoot()
                };
                
                visualizationModel.fireTreeStructureChanged( 
                    ResultMonitorDataModel.this, path, null, null );
            }
        };
        if ( EventQueue.isDispatchThread() )
        {
            runnable.run();
        }
        else
        {            
            EventQueue.invokeLater( runnable );
        }
    }
    
    private void fireNewSearchResultAdded(
        final SearchResultElement resultElement,
        final int index)
    {
        if ( visualizationModel == null )
        {
            return;
        }
        
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                Object[] path = new Object[]
                {
                    visualizationModel.getRoot()
                };
                int[] indices = new int[]
                {
                    index
                };
                Object[] changes = new Object[]
                {
                    resultElement
                };
                //visualizationModel.fireTreeNodesInserted(
                //    this, path, indices, changes );
                
                if ( displayedSearchResults.size() == 1 )
                { // this was the first element added
                    visualizationModel.fireTreeStructureChanged( 
                        ResultMonitorDataModel.this, path, indices, changes );
                }
                else
                {
                    visualizationModel.fireTreeNodesInserted(
                        ResultMonitorDataModel.this, path, indices, changes );
                }
            }
        };
        //if ( EventQueue.isDispatchThread() )
        {
            runnable.run();
        }
//        else
//        {
//            EventQueue.invokeLater( runnable );
//        }
    }

    private void fireSearchResultAdded(
        final RemoteFile remoteFile,
        final SearchResultElement resultElement)
    {
        if ( visualizationModel == null )
        {
            return;
        }
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                Object[] path = new Object[]
                {
                    visualizationModel.getRoot(),
                    resultElement
                };
                Object[] changes = new Object[]
                {
                    remoteFile
                };
                visualizationModel.fireTreeNodesInserted(
                    ResultMonitorDataModel.this, path, null, changes );
            }
        };
        if ( EventQueue.isDispatchThread() )
        {
            runnable.run();
        }
        else
        {            
            EventQueue.invokeLater( runnable );
        }
    }
    
    ///////////////////////// END Event forwarding ///////////////////////////////
}