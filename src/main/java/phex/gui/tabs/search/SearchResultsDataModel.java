/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2008 Phex Development Group
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
 *  $Id: SearchResultsDataModel.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.search;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.bushe.swing.event.annotation.EventTopicSubscriber;

import phex.common.address.AddressUtils;
import phex.common.address.DestAddress;
import phex.common.address.IpAddress;
import phex.download.RemoteFile;
import phex.event.ContainerEvent;
import phex.event.PhexEventTopics;
import phex.gui.common.GUIRegistry;
import phex.gui.models.ISearchDataModel;
import phex.gui.models.SearchTreeTableModel;
import phex.query.QueryManager;
import phex.query.Search;
import phex.query.SearchDataEvent;
import phex.rules.Rule;
import phex.rules.SearchFilterRules;
import phex.security.HittingIpCidrPair;
import phex.security.IpUserSecurityRule;
import phex.security.SecurityRule;
import phex.servent.Servent;

/**
 * This data model is doing the transition between the search result coming
 * from phex.query.Search and the data that is displayed through 
 * phex.gui.tabs.search.SearchResultsPanel.
 * Every instance of phex.query.Search that is displayed on the UI has its
 * associated phex.gui.tabs.search.SearchResultsDataModel. The lookup and
 * association is also handled in this class through static helpers.
 */
public class SearchResultsDataModel implements ISearchDataModel
{
    /**
     * To allow easy lookup this HashMap maps a search object to
     * its corresponding SearchResultDataModel. 
     */
    private static final HashMap<Search, SearchResultsDataModel> searchToDataModelMap = 
        new HashMap<Search, SearchResultsDataModel>();
    
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
     * the allSearchResultSHA1Set is used. This also counts search results
     * without SHA1, therefore it can differ from allSearchResultsSHA1Set.size()
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
    
    private Search search;
    
    private Rule quickFilterRule;
    private Set<Rule> searchFilterRuleSet;
        
    private SearchResultsDataModel( Search search, SearchFilterRules filterRules )
    {
        this.search = search;
        allRemoteFiles = new ArrayList<RemoteFile>();
        allSearchResultSHA1Set = new HashSet<String>();
        allSearchResultCount = 0;
        displayedSearchResults = new ArrayList<SearchResultElement>();
        displayedSearchResultSHA1Map = new HashMap<String, SearchResultElement>();
        comparator = new SearchResultElementComparator();
        searchFilterRuleSet = new HashSet<Rule>();
        
        searchFilterRuleSet.addAll( filterRules.getPermanentList() );
        
        GUIRegistry.getInstance().getServent().getEventService().processAnnotations( this );
    }
    
    public int getSearchElementCount()
    {
        return displayedSearchResults.size();
    }
    
    public int getAllSearchResultCount()
    {
        return allSearchResultCount;
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
    
    private void addSearchResults( RemoteFile[] newSearchResults, boolean fireEvents )
    {
        processFilterRules(newSearchResults);
        
        synchronized( allRemoteFiles )
        {
        synchronized( displayedSearchResults )
        {
            RemoteFile remoteFile;
            for ( int i = 0; i < newSearchResults.length; i++ )
            {
                remoteFile = newSearchResults[i];
                if ( remoteFile.isFilteredRemoved() )
                {
                    continue;
                }
                if ( !remoteFile.isFilteredHidden() )
                {
                    addSearchResultForDisplay( remoteFile, fireEvents );
                }
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

    private void addSearchResultForDisplay( RemoteFile remoteFile, boolean fireEvents )
    {
        SearchResultElement resultElement = null;
        String sha1 = remoteFile.getSHA1();
        if ( sha1 != null )
        {
            resultElement = displayedSearchResultSHA1Map.get( sha1 );
        }
        if ( resultElement != null )
        {            
            resultElement.addRemoteFile( remoteFile );
            
            // TODO in here is the problem of bug 1288776
            // http://sourceforge.net/tracker/index.php?func=detail&aid=1288776&group_id=27021&atid=388892
            // The main cause is that we are having a sorted list to which we
            // add child elements, the number of child elements changes the order
            // of the parent element in the displayedSearchResults.
            // I tried removing the resultElement and read it at its updated 
            // position, but this is not working right. Often the expected position
            // of the resultElement could not be determined right....
            if ( fireEvents )
            {                
                fireSearchResultAdded(remoteFile, resultElement);
            }
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
                if ( fireEvents )
                {
                    fireNewSearchResultAdded(resultElement, -index-1);
                }
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
                // update search workaround to update search count...
                search.fireSearchChanged();
        }
        }
    }
    
    public Search getSearch()
    {
        return search;
    }
    
    public Rule getQuickFilterRule()
    {
        return quickFilterRule;
    }
    
    /**
     * After modifying a existing quick filter rule it needs to be set again
     * to trigger the query list update. 
     * @param rule
     */
    public void setQuickFilterRule( Rule rule )
    {
        quickFilterRule = rule;
        updateFilteredQueryList();
    }
    
    /**
     * Validates if the given rule is in the set of active rules.
     * @param rule
     * @return
     */
    public boolean isRuleActive( Rule rule )
    {
        return searchFilterRuleSet.contains( rule );
    }
    
    public void activateRule( Rule rule )
    {
        searchFilterRuleSet.add(rule);
        updateFilteredQueryList();
    }
    
    public void deactivateRule( Rule rule )
    {
        searchFilterRuleSet.remove(rule);
        updateFilteredQueryList();
    }
    
    /**
     * Processes the filter rules for the remote files. Existing filter flags
     * are not reset!
     * @param remoteFiles
     */
    private void processFilterRules( RemoteFile[] remoteFiles )
    {
        if ( searchFilterRuleSet == null )
        {
            return;
        }
        if ( quickFilterRule != null )
        {
            quickFilterRule.process(search, remoteFiles, 
                GUIRegistry.getInstance().getServent() );
        }
        for ( Rule rule : searchFilterRuleSet )
        {
            rule.process(search, remoteFiles, 
                GUIRegistry.getInstance().getServent() );
        }
    }
    
    private void updateFilteredQueryList()
    {
        synchronized( allRemoteFiles )
        {
        synchronized( displayedSearchResults )
        {
            // create a copy of all
            RemoteFile[] remoteFiles = new RemoteFile[ allRemoteFiles.size() ];
            allRemoteFiles.toArray(remoteFiles);
            
            // clear all and displayed
            allSearchResultCount = 0;
            allRemoteFiles.clear();
            allSearchResultSHA1Set.clear();
            displayedSearchResultSHA1Map.clear();
            displayedSearchResults.clear();
            fireAllSearchResultsChanged();
            
            // reset filter flags
            for ( int i = 0; i < remoteFiles.length; i++ )
            {
                remoteFiles[i].clearFilterFlags();
            }
            
            // add all search results (will trigger filtering)
            addSearchResults( remoteFiles, true );
            
            // update search workaround to update search count...
            search.fireSearchChanged();
        }
        }
    }
    
    /**
     * Releases all resources hold for GC
     */
    public void release()
    {
        // clear visualizationModel to ensure no more updating of it.
        visualizationModel = null;
        clearSearchResults();
    }

    @EventTopicSubscriber(topic=PhexEventTopics.Search_Data)
    public void onSearchDataEvent( String topic, SearchDataEvent event )
    {
        if ( search != event.getSource() )
        {
            return;
        }
        if ( event.getType() == SearchDataEvent.SEARCH_HITS_ADDED )
        {
            RemoteFile[] newSearchResults = event.getSearchData();
            addSearchResults( newSearchResults, true );
        }
    }
    
    @Override
    public String toString()
    {
        return super.toString() + " - " + search;
    }
    
    //////////////////// START SecurityRuleChangeListener //////////////////////
    
    @EventTopicSubscriber(topic=PhexEventTopics.Security_Rule)
    public void onSecurityRuleEvent( String topic, ContainerEvent event )
    {
        final SecurityRule rule = (SecurityRule) event.getSource();
        if ( event.getType() == ContainerEvent.Type.ADDED )
        {
            if ( rule.isDisabled() )
            {
                return;
            }
            if ( rule instanceof IpUserSecurityRule )
            {
                Runnable runnable = new Runnable()
                {
                    public void run()
                    {
                        updateSecurityFilteredQueryList((IpUserSecurityRule) rule);
                    }
                };
                EventQueue.invokeLater(runnable);
            }
        }
    }
    
    private void updateSecurityFilteredQueryList( IpUserSecurityRule rule )
    {
        // the code is basically the same as updateFilteredQueryList
        // except that is using a list for better performance.
        synchronized( allRemoteFiles )
        {
        synchronized( displayedSearchResults )
        {
            // create a copy of all
            List<RemoteFile> remoteFilesList = new ArrayList<RemoteFile>( allRemoteFiles );
            
            // clear all and displayed
            allSearchResultCount = 0;
            allRemoteFiles.clear();
            allSearchResultSHA1Set.clear();
            displayedSearchResultSHA1Map.clear();
            displayedSearchResults.clear();
            fireAllSearchResultsChanged();
            
            HittingIpCidrPair rulePair = rule.getIpCidrPair();
            // reset filter flags
            ListIterator<RemoteFile> iterator = remoteFilesList.listIterator();
            while( iterator.hasNext() )
            {
                RemoteFile remoteFile = iterator.next();
                DestAddress address = remoteFile.getHostAddress();
                IpAddress ipAddress = address.getIpAddress();
                if ( ipAddress != null && 
                     rulePair.contains( AddressUtils.byteIpToIntIp( ipAddress.getHostIP() ), 0xFFFFFFFF ) )
                {
                    rulePair.countHit();
                    iterator.remove();
                    continue;
                }
                remoteFile.clearFilterFlags();
            }
            
            // add all search results (will trigger filtering)
            RemoteFile[] remoteFiles = new RemoteFile[ remoteFilesList.size() ];
            remoteFilesList.toArray(remoteFiles);
            addSearchResults( remoteFiles, false );
            fireAllSearchResultsChanged();
            
            // update search workaround to update search count...
            search.fireSearchChanged();
        }
        }
    }
    ///////////////////// End SecurityRuleChangeListener ///////////////////////
    
    ///////////////////////// START Event forwarding ///////////////////////////
    
    // usually changes to the visualizationModel should happen on the EDT
    // but somehow this will cause the table to not update correctly.
    
    
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
                    SearchResultsDataModel.this, path, null, null );
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
                        SearchResultsDataModel.this, path, indices, changes );
                }
                else
                {
                    visualizationModel.fireTreeNodesInserted(
                        SearchResultsDataModel.this, path, indices, changes );
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
        // copy ref
        final SearchTreeTableModel treeTableModel = visualizationModel;
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                Object[] path = new Object[]
                {
                    treeTableModel.getRoot(),
                    resultElement
                };
                Object[] changes = new Object[]
                {
                    remoteFile
                };
                treeTableModel.fireTreeNodesInserted(
                    SearchResultsDataModel.this, path, null, changes );
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
    
    //////////////////////// START Static Lookup methods ///////////////////////////
    
    public static SearchResultsDataModel registerNewSearch( Search search, 
        SearchFilterRules filterRules )
    {
        SearchResultsDataModel dataModel = new SearchResultsDataModel( search, filterRules );
        searchToDataModelMap.put( search, dataModel );
        return dataModel;
    }
    
    public static SearchResultsDataModel lookupResultDataModel( Search search )
    {
        return searchToDataModelMap.get( search );
    }
    
    public static void unregisterSearch( Search search )
    {
        SearchResultsDataModel dataModel = searchToDataModelMap.remove( search );
        if ( dataModel != null )
        {
            dataModel.release();
        }
    }
    
    //////////////////////// END Static Lookup methods ///////////////////////////
}
