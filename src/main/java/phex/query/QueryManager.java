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
 *  $Id: QueryManager.java 4418 2009-03-23 11:02:27Z ArneBab $
 */
package phex.query;

import java.io.File;
import java.util.TimerTask;

import phex.common.AbstractLifeCycle;
import phex.common.Environment;
import phex.common.log.NLogger;
import phex.host.Host;
import phex.msg.QueryMsg;
import phex.msg.QueryResponseMsg;
import phex.rules.SearchFilterRules;
import phex.servent.Servent;

public class QueryManager extends AbstractLifeCycle
{
    private final Servent servent;
    private final SearchContainer searchContainer;
    private final BackgroundSearchContainer backgroundSearchContainer;
    private final SearchFilterRules searchFilterRules;
    private final DynamicQueryWorker dynamicQueryWorker;

    public QueryManager( Servent servent )
    {
        this.servent = servent;
        searchContainer = new SearchContainer( servent );
        servent.getMessageService().addMessageSubscriber(
            QueryResponseMsg.class, searchContainer );
        
        backgroundSearchContainer = new BackgroundSearchContainer( servent );
        servent.getMessageService().addMessageSubscriber(
            QueryResponseMsg.class, backgroundSearchContainer );
        
        File filterFile = servent.getGnutellaNetwork().getSearchFilterFile();
        searchFilterRules = new SearchFilterRules( filterFile );
        //researchService = new ResearchService( new ResearchServiceConfig() );
        dynamicQueryWorker = new DynamicQueryWorker();
    }

    @Override
    protected void doStart()
    {
        searchFilterRules.load();
        dynamicQueryWorker.startQueryWorker();
        Environment.getInstance().scheduleTimerTask( 
            new ExpiredSearchCheckTimer(), ExpiredSearchCheckTimer.TIMER_PERIOD,
            ExpiredSearchCheckTimer.TIMER_PERIOD );
    }
    
    @Override
    public void doStop()
    {
        searchFilterRules.save();
    }

    public SearchContainer getSearchContainer()
    {
        return searchContainer;
    }

    public BackgroundSearchContainer getBackgroundSearchContainer()
    {
        return backgroundSearchContainer;
    }

/*    public ResearchService getResearchService()
    {
        return researchService;
    }
*/
    
    /**
     * Removes all running queries for this host.
     * @param host the host to remove its queries for.
     */
    public void removeHostQueries( Host host )
    {
        if ( host.isUltrapeerLeafConnection() )
        {
            dynamicQueryWorker.removeDynamicQuerysForHost( host );
        }
    }
    
    /**
     * Sends a dynamic query using the dynamic query engine.
     * @param query the query to send.
     * @param desiredResults the number of results desired.
     */
    public DynamicQueryEngine sendDynamicQuery( QueryMsg query, int desiredResults )
    {
        DynamicQueryEngine engine = new DynamicQueryEngine( query, desiredResults,
            servent.getHostService().getNetworkHostsContainer(), 
            servent.getMessageService() );
        dynamicQueryWorker.addDynamicQueryEngine( engine );
        return engine;
    }
    
    /**
     * Sends a query for this host, usually initiated by the user.
     * @param queryMsg the query to send.
     * @return the possible dynamic query engine used, or null if no
     *         dynamic query is initiated.
     */
    public DynamicQueryEngine sendMyQuery( QueryMsg queryMsg )
    {
        return servent.getMessageService().sendQuery( queryMsg );
    }

    public SearchFilterRules getSearchFilterRules()
    {
        return searchFilterRules;
    }
    
    /**
     * Stops all searches where the timeout has passed.
     */
    private class ExpiredSearchCheckTimer extends TimerTask
    {

        public static final long TIMER_PERIOD = 5000;

        /**
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run()
        {
            try
            {
                // Stops all searches where the timeout has passed.
                long currentTime = System.currentTimeMillis();
                searchContainer.stopExpiredSearches( currentTime );
                backgroundSearchContainer.stopExpiredSearches( currentTime );
            }
            catch ( Throwable th )
            {
                NLogger.error( QueryManager.ExpiredSearchCheckTimer.class, th, th );
            }
        }
    }
}