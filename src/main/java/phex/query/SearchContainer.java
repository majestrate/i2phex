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
 *  $Id: SearchContainer.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.query;

import java.util.ArrayList;
import java.util.List;

import org.bushe.swing.event.annotation.EventTopicSubscriber;

import phex.common.address.DestAddress;
import phex.event.ChangeEvent;
import phex.event.ContainerEvent;
import phex.event.PhexEventTopics;
import phex.event.ContainerEvent.Type;
import phex.host.Host;
import phex.msg.GUID;
import phex.msg.InvalidMessageException;
import phex.msg.QueryResponseMsg;
import phex.msghandling.MessageSubscriber;
import phex.servent.OnlineStatus;
import phex.servent.Servent;

public class SearchContainer implements MessageSubscriber<QueryResponseMsg>
{
    protected final Servent servent;
    
    // to let the background search container share.
    protected List<Search> searchList;

    public SearchContainer( Servent servent )
    {
        this.servent = servent;
        searchList = new ArrayList<Search>();
        
        servent.getEventService().processAnnotations( this );
    }

    /**
     * Create a Search object and call startSearching on it.
     */
    public synchronized Search createSearch( String queryStr )
    {
        KeywordSearch search = new KeywordSearch( queryStr, servent.isFirewalled(), servent );
        insertToSearchList( search, 0 );
        search.startSearching( );
        return search;
    }
    
    /**
     * Create a Search object and call startSearching on it.
     */
    public synchronized Search createWhatsNewSearch( )
    {
        WhatsNewSearch search = new WhatsNewSearch( servent.isFirewalled(), servent );
        insertToSearchList( search, 0 );
        search.startSearching( );
        return search;
    }

    public synchronized BrowseHostResults createBrowseHostSearch(
        DestAddress hostAddress, GUID hostGUID )
    {
        BrowseHostResults search = new BrowseHostResults( servent, hostAddress, hostGUID );
        insertToSearchList( search, 0 );
        search.startSearching( );
        return search;
    }

    /**
     * The only method allowed to actually add a search to the list.
     */
    protected synchronized void insertToSearchList( Search search, int position )
    {
        searchList.add( position, search );
        fireSearchAdded( search, position );
    }

    /**
     * The only method allowed to actually remove a search from the list.
     * The search is stopped before it's removed
     */
    protected synchronized void removeFromSearchList( int index )
    {
        Search search = getSearchAt( index );
        search.stopSearching();
        searchList.remove( index  );
        fireSearchRemoved( search, index );
    }

    /**
     * Returns the first found existing Search with the specified search string
     * if it is still searching. If there is no running search with the given
     * search string is found null is returned.
     */
    public synchronized Search getRunningKeywordSearch( String searchString )
    {
        for ( Search search : searchList )
        {
            if ( search.isSearching() && search instanceof KeywordSearch
                && ((KeywordSearch)search).getSearchString().equals( searchString ) )
            {
                return search;
            }
        }
        return null;
    }
    
    /**
     * Returns the first found existing Search with the specified search string
     * if it is still searching. If there is no running search with the given
     * search string is found null is returned.
     */
    public synchronized Search getRunningBrowseHost( DestAddress hostAddress,
        GUID hostId )
    {
        for ( Search search : searchList )
        {
            if ( search.isSearching() && search instanceof BrowseHostResults )
            {
                BrowseHostResults browseHost = (BrowseHostResults) search;
                DestAddress destAddress = browseHost.getDestAddress();
                GUID hostGUID = browseHost.getHostGUID();
                if ( (destAddress != null && destAddress.equals( hostAddress )) ||
                     (hostGUID != null && hostGUID.equals( hostId ) ) )
                {
                    return search;
                }
            }
        }
        return null;
    }

    public synchronized int getSearchCount()
    {
        return searchList.size();
    }
    
    public synchronized int getIndexOfSearch( Search search )
    {
        int size = searchList.size();
        for ( int i = 0; i < size; i++ )
        {
            if ( search == searchList.get( i ) )
            {
                return i;
            }
        }
        return -1;
    }

    public synchronized Search getSearchAt( int index )
    {
        if (index < 0 || index >= getSearchCount() )
        {
            return null;
        }
        return searchList.get( index );
    }

    /**
     * Removes the search from the search list. The search will be stopped before
     * it's removed.
     */
    public synchronized void removeSearch( Search search )
    {
        int index = searchList.indexOf( search );
        // if a search was found.
        if ( index >= 0 )
        {
            removeFromSearchList( index );
        }
    }

    public synchronized void removeSearch( int index )
    {
        removeFromSearchList( index );
    }

    /**
     * Stops all searches where the timeout has passed.
     */
    public synchronized void stopExpiredSearches( long currentTime )
    {
        for (int i = 0; i < searchList.size(); i++)
        {
            searchList.get( i ).checkForSearchTimeout( currentTime );
        }
    }

    private synchronized void stopAllSearches()
    {
        for (int i = 0; i < searchList.size(); i++)
        {
            searchList.get( i ).stopSearching();
        }
    }

    public synchronized void removeAllSearches()
    {
        for ( int i = searchList.size() - 1; i >= 0; i-- )
        {
            removeFromSearchList( i );
        }
    }
    
    /**
     * Implements MessageSubscriber method to process the query response. 
     * No IP filtering is done from here on.
     * @throws InvalidMessageException 
     */
    public void onMessage(QueryResponseMsg message, Host sourceHost)
        throws InvalidMessageException
    {
        for (int i = 0; i < searchList.size(); i++)
        {
            Search search = searchList.get( i );
            if ( search instanceof BrowseHostResults )
            {
                continue;
            }
            search.processResponse( message );
        }
    }

    ///////////////////// START event handling methods ////////////////////////
    protected void fireSearchAdded( final Search search, final int position )
    {
        servent.getEventService().publish( PhexEventTopics.Search_Update,
            new ContainerEvent( Type.ADDED, search, this, position ) );
    }

    protected void fireSearchRemoved( final Search search, final int position )
    {
        servent.getEventService().publish( PhexEventTopics.Search_Update,
            new ContainerEvent( Type.REMOVED, search, this, position ) );
    }
    
    /**
     * Reacts on online status changes to stop all searches.
     */
    @EventTopicSubscriber(topic=PhexEventTopics.Servent_OnlineStatus)
    public void onOnlineStatusEvent( String topic, ChangeEvent event )
    {
        OnlineStatus oldStatus = (OnlineStatus) event.getOldValue();
        OnlineStatus newStatus = (OnlineStatus) event.getNewValue();
        if ( newStatus == OnlineStatus.OFFLINE && 
            oldStatus != OnlineStatus.OFFLINE )
        {// switch from any online to offline status
            stopAllSearches();
        }
    }
    ///////////////////// END event handling methods ////////////////////////
}