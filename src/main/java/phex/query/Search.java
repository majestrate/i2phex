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
 *  --- CVS Information ---
 *  $Id: Search.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.query;

import phex.common.URN;
import phex.common.log.NLogger;
import phex.download.RemoteFile;
import phex.event.PhexEventTopics;
import phex.msg.InvalidMessageException;
import phex.msg.QueryMsg;
import phex.msg.QueryResponseMsg;
import phex.msg.QueryResponseRecord;
import phex.security.AccessType;
import phex.servent.Servent;

public abstract class Search
{   
    protected final Servent servent;
    
    /**
     * The time when the search was started.
     */
    protected long startTime;
    
    /**
     * The dynamic query engine that actually runs the search in case
     * a dynamic query is used. This can attribute can be null in case
     * no dynamic query is used (if we are a leaf). 
     */
    protected DynamicQueryEngine queryEngine;

    /**
     * The MsgQuery object that forms the query for this search.
     */
    protected QueryMsg queryMsg;

    /**
     * The status of the search.
     */
    protected boolean isSearching;
    
    /**
     * Associated class that is able to hold search results. Access to this
     * should be locked by holding 'this'. 
     */
    protected SearchResultHolder searchResultHolder;

    protected Search( Servent servent )
    {
        this.servent = servent;
        isSearching = false;
        searchResultHolder = new SearchResultHolder();
    }
    
    public int getQueryHitCount()
    {
        return searchResultHolder.getQueryHitCount();
    }

    /**
     * Tries a very basic calculation about the search progress.
     * @return the search progress between 0 and 100
     */
    public int getProgress()
    {
        if ( !isSearching )
        {
            return 100;
        }
        if ( queryEngine != null )
        {
            return queryEngine.getProgress();
        }
        else
        {
            long currentTime = System.currentTimeMillis();
            // time progress...
            int timeProgress = (int)(100.0 - (double)( startTime + DynamicQueryConstants.DEFAULT_QUERY_TIMEOUT - currentTime )
                / (double)DynamicQueryConstants.DEFAULT_QUERY_TIMEOUT * 100.0 );
            // return the max of all these
            return Math.min( timeProgress, 100);
        }
    }
   
    public boolean isSearching()
    {
        return isSearching;
    }
    
    public void checkForSearchTimeout( long currentTime )
    {
        if ( queryEngine != null )
        {
            if ( queryEngine.isQueryFinished() )
            {
                stopSearching();
            }
        }
        else if ( currentTime > startTime + DynamicQueryConstants.DEFAULT_QUERY_TIMEOUT )
        {
            // timed out stop search
            stopSearching();
        }
    }

    public void startSearching( )
    {
        startTime = System.currentTimeMillis();
        // set the creation time just before we send the query this
        // will prevent the query to timeout before it could be send
        queryMsg.setCreationTime( startTime );
        NLogger.debug( Search.class,
            "Sending Query " + queryMsg );
        queryEngine = servent.getQueryService().sendMyQuery( queryMsg );
        isSearching = true;
        fireSearchStarted();
    }

    public void stopSearching()
    {
        if ( !isSearching )
        {// already stopped
            return;
        }
        isSearching = false;
        if ( queryEngine != null )
        {
            queryEngine.stopQuery();
        }
        fireSearchStoped();
    }
    
    /**
     * Used by subclasses to check if the record is valid. In this case a 
     * security check is done on the record URN.
     * @param record
     * @return true if valid, false otherwise.
     */
    protected boolean isResponseRecordValid( QueryResponseRecord record )
    {
        // REWORK maybe we should move the altloc security check
        // from GGEPExtension.parseAltExtensionData to here to?
        // MERGE with FilteredQueryResponseMonitor.isResponseRecordValid()
        
        URN urn = record.getURN();
        if ( urn != null && servent.getSecurityService().controlUrnAccess( urn ) != AccessType.ACCESS_GRANTED )
        {
            NLogger.debug( Search.class, "Record contains blocked URN: " + urn.getAsString() );
            return false;
        }
        return true;
    }

    public abstract void processResponse( QueryResponseMsg msg )
        throws InvalidMessageException;
    
    ///////////////////// START event handling methods ////////////////////////

    protected void fireSearchStarted()
    {
        SearchDataEvent searchChangeEvent =
            new SearchDataEvent( this, SearchDataEvent.SEARCH_STARTED );
        fireSearchChangeEvent( searchChangeEvent );
    }

    protected void fireSearchStoped()
    {
        SearchDataEvent searchChangeEvent =
            new SearchDataEvent( this, SearchDataEvent.SEARCH_STOPED );
        fireSearchChangeEvent( searchChangeEvent );
    }

    public void fireSearchChanged()
    {
        SearchDataEvent searchChangeEvent =
            new SearchDataEvent( this, SearchDataEvent.SEARCH_CHANGED );
        fireSearchChangeEvent( searchChangeEvent );
    }

    protected void fireSearchHitsAdded( RemoteFile[] newHits )
    {
        SearchDataEvent searchChangeEvent = new SearchDataEvent( this,
            SearchDataEvent.SEARCH_HITS_ADDED, newHits );
        fireSearchChangeEvent( searchChangeEvent );        
    }

    private void fireSearchChangeEvent( final SearchDataEvent searchChangeEvent )
    {
        servent.getEventService().publish( PhexEventTopics.Search_Data,
            searchChangeEvent );
    }
    
    ///////////////////// END event handling methods ////////////////////////
}