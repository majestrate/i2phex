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
 *  $Id: DynamicQueryEngine.java 4166 2008-04-15 19:41:30Z complication $
 */
package phex.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import phex.common.QueryRoutingTable;
import phex.host.Host;
import phex.host.NetworkHostsContainer;
import phex.msg.QueryMsg;
import phex.msghandling.MessageService;
import phex.common.log.NLogger;

/**
 * The DynamicQueryEngine is responsible for submitting the query 
 * over time to different hosts with different ttls depending on the
 * number of results received.
 * Queries are first checked against connected leaves, then probe 
 * queries with ttl of 1 are sent to ultrapeers to determine the file
 * popularity, the last step is to sent regular queries with adjusted
 * ttl to single ultrapeers.
 * Only Ultrapeers use this engine to submit there queries. Leaves are
 * sending there queries in the old style by broadcasting them to all 
 * there connections. The Ultrapeer of the leaf should then start the
 * dynamic query process for its leaf.
 */
public class DynamicQueryEngine implements DynamicQueryConstants
{
    private final NetworkHostsContainer hostsContainer;
    private final MessageService messageService;
    
    /**
     * Indicates if the dynamic query process has started.
     */
    private boolean isDynamicQueryStarted;
    
    /**
     * Indicates if the dynamic query process was forced to
     * stop. Like after user interaction.
     */
    private boolean isDynamicQueryStopped;
    
    /**
     * Indicates if leaves are queried.
     */
    private boolean areLeavesQueried;
    
    /**
     * Indicates if the probe query is sent.
     */
    private boolean isProbeQuerySent;
    
    /**
     * The number of millis a query is running before it times out.
     */
	private long queryTimeout;

    /**
     * The time when the query was started.
     */
    private long queryStartTime;
    
    /**
     * The time when the next query process step is
     * taken.
     */
    private long nextProcessTime;
    
    /**
     * The time to wait per hop between dynamic query iterations.
     */
    private int timeToWaitPerHop;

    /**
	 * The number of results desired to gain.
	 */
	private int desiredResults;
    
    /**
     * The number of results already received.
     */
    private int receivedResults;
    
    /**
     * The estimated number of host in the horizon that have been reached
     * by this query.
     */
    private int estimatedQueriedHorizon;
    
    /**
     * A list of hosts that we send a standard query too.
     */
    private List<Host> queriedHosts;
    
    /**
     * The base query of this DynamicQueryEngine. Form it
     * new queries are build.
     */
    private QueryMsg query;

    /**
     * Constructs a new DynamicQueryEngine.
     * @param query the query to base the process on.
     */
    public DynamicQueryEngine( QueryMsg query, int desiredResults, 
        NetworkHostsContainer hostsContainer, MessageService messageService )
    {
        this.hostsContainer = hostsContainer;
        this.messageService = messageService;
        this.query = query;
        this.desiredResults = desiredResults;
        isDynamicQueryStarted = false;
        isDynamicQueryStopped = false;
        areLeavesQueried = false;
        // I2P:
        // Let's pretend we already sent probe queries.
        // This disables their sending entirely.
        isProbeQuerySent = true;
        queryTimeout = DEFAULT_QUERY_TIMEOUT;
        timeToWaitPerHop = DEFAULT_TIME_TO_WAIT_PER_HOP;
        estimatedQueriedHorizon = 1;
        receivedResults = 0;
        queriedHosts = new ArrayList<Host>();
        
        
        if ( query.hasQueryURNs() )
        {// adjust desired results
            desiredResults = DESIRED_HASH_RESULTS;
        }
    }
    
    /**
     * Increments the received result count.
     * @param inc the value to increment the received result count.
     */
    public void incrementResultCount( int inc )
    {
        receivedResults += inc;
    }
    
    /**
	 * @return the number of received results
	 */
	public int getResultCount()
	{
		return receivedResults;
	}
    
    /**
     * Forces to stop the dynamic query. Like after user interaction.
     */
    public void stopQuery()
    {
        isDynamicQueryStopped = true;
    }
    
    /**
     * Returns if there are already enough results routed to meet the 
     * desired results.
     * @return true if there are already enough results routed to meet the 
     *         desired results, false otherwise.
     */
    public boolean areEnoughResultsRouted()
    {
        // check if we received enough results...
        return receivedResults >= desiredResults;
    }
    
    /**
     * Returns the host this query is originaly comming from.
     * @return the host this query is originaly comming from.
     */
    public Host getFromHost()
    {
        return query.getHeader().getFromHost();
    }

    /**
     * Returns whether this query engine has finished its query process. This
     * can be the case when we received enough results, reached the maximal 
     * estimated horizon, or the query is already running for a too long time.
     * 
     * @return true if the query engine has finished its query process, false
     *         otherwise.
     */
    public boolean isQueryFinished()
    {
        // check if query has started.
        if ( !isDynamicQueryStarted )
        {
            return false;
        }
        if ( isDynamicQueryStopped )
        {
            return true;
        }
        
        // check if we received enough results...
        if( receivedResults >= desiredResults )
        {
            return true;
        }
        
        if ( estimatedQueriedHorizon > MAX_ESTIMATED_QUERY_HORIZON )
        {
            return true;
        }
        
        // check if the query has timed out
        long currentTime = System.currentTimeMillis();
        if ( currentTime > queryStartTime + queryTimeout )
        {
            return true;
        }
        
        return false;
    }
    
    /**
     * Tries a very basic calculation about the search progress.
     * @return the search progess from 0 - 100.
     */
    public int getProgress()
    {
        // check if query has started.
        if ( !isDynamicQueryStarted )
        {
            return 0;
        }
        if ( isDynamicQueryStopped )
        {
            return 100;
        }
        
        int resultProgress = (int)((double)receivedResults / (double)desiredResults
            * 100d );

        int horizonProgress = (int)((double)estimatedQueriedHorizon
            / (double)MAX_ESTIMATED_QUERY_HORIZON * 100d );
        
        // time progress...
        long currentTime = System.currentTimeMillis();
        int timeProgress = (int)(100 - (double)( queryStartTime + queryTimeout - currentTime )
            / (double)queryTimeout * 100d );
            
        // return the max of all these
        return Math.min( Math.max( resultProgress,
            Math.max( horizonProgress, timeProgress ) ), 100);
    }

    /**
     * Runs the dynamic query process. This method is called
     * regulary from the DynamicQueryWorker to continue the
     * query process.
     */
    public void processQuery()
    {
        long currentTime = System.currentTimeMillis();
        if ( currentTime < nextProcessTime )
        {// not our turn to query now...
            return;
        }
        
        if ( !isDynamicQueryStarted )
        {
            isDynamicQueryStarted = true;
            queryStartTime = currentTime;
            
            // first sent to leaves... if ultrapeer.
            // we always are a ultrapeer since only ultrapeer use dynamic query...
            //if ( hostMgr.isUltrapeer() )
            {
                boolean sentToLeaves = processQueryToLeaves();
                if ( sentToLeaves )
                {
                    nextProcessTime = System.currentTimeMillis() + timeToWaitPerHop;
                    return;
                }
            }
        }
        
        // after leaves are querierd... send a probe query...
        if ( !isProbeQuerySent )
        {
            processProbeQuery();
        }
        else
        {
            processStandardQuery();
        }
    }
    
    /**
     * Processes the standard query. This is the last step of the
     * dynamic query process after leaves are queried and probe
     * queries are sent.
     */
    private void processStandardQuery()
    {
        Host[] ultrapeers = hostsContainer.getUltrapeerConnections();
        // the number of connections that have not yet received the query
        // used to calculate TTL
        int notQueriedHosts = 0;
        Host hostToQuery = null;
        for ( int i = 0; i < ultrapeers.length; i++ )
        {
            // sort out all not stable and all 
            // connections we already queried.
            if ( !ultrapeers[i].isConnectionStable() ||
                 queriedHosts.contains( ultrapeers[i] ) )
            {
                continue;
            }
            notQueriedHosts ++;
            // found a host to query next...
            hostToQuery = ultrapeers[i];
        }
        
        if ( notQueriedHosts == 0 || hostToQuery == null )
        {// no hosts to query found... try again later...
            NLogger.warn( DynamicQueryEngine.class,
                    "No hosts suitable for a standard query. Is something wrong?");
            nextProcessTime = System.currentTimeMillis() + 5000;
            return;
        }
        
        byte maxTTL = hostToQuery.getMaxTTL();
        int degree = hostToQuery.getUltrapeerDegree();
        byte ttl = calculateTTL( maxTTL, degree, notQueriedHosts );
        
        
        if ( ttl == 1 && hostToQuery.isUPQueryRoutingSupported() )
        {// if we have a UP query routing connection we can
         // pre check if a query with ttl of 1 makes sense.
         // if there would be no hit we set the ttl to 2.
            QueryRoutingTable qrt = hostToQuery.getLastReceivedRoutingTable();
            if ( qrt == null || !qrt.containsQuery( query ) )
            {
                ttl = 2;
            }
        }
        
        QueryMsg newQuery = new QueryMsg( query, ttl );
        hostToQuery.queueMessageToSend( newQuery );
        queriedHosts.add( hostToQuery );

        // calculate the estimated reached horizon
        estimatedQueriedHorizon = calculateEstimatedHorizon( degree, ttl );
        nextProcessTime = System.currentTimeMillis() + (ttl * timeToWaitPerHop);
        
        adjustTimeToWaitPerHop();
    }

    /**
     * Sends the probe query to ultrapeers to check the
     * file popularity.
     */
    private void processProbeQuery()
    {
        Host[] ultrapeers = hostsContainer.getUltrapeerConnections();
        List<Host> directHitList = new ArrayList<Host>( ultrapeers.length );
        List<Host> failedList = new ArrayList<Host>( ultrapeers.length );
        
        for ( int i = 0; i < ultrapeers.length; i++ )
        {
            QueryRoutingTable qrt = ultrapeers[i].getLastReceivedRoutingTable();
            if ( ultrapeers[i].isUPQueryRoutingSupported() && qrt != null )
            {
                if ( qrt.containsQuery( query ) )
                {
                    directHitList.add( ultrapeers[i] );
                }
                else
                {// add to the end of failed list (low priority)
                    failedList.add( ultrapeers[i] );
                }
            }
            else
            {// add to the top of failed list (high priority)
                failedList.add( 0, ultrapeers[i] );
            }
        }
        
        int directProbeSize = directHitList.size();
        int failedProbeSize = 0;

        // send probe query to direct hits (max. 10)... with ttl = 1
        int toIdx = Math.min( 10, directProbeSize );
        sendProbeQueryToHosts( directHitList.subList( 0, toIdx ), (byte)1 );
        directProbeSize = toIdx;
        
        
        // we have not enough direct hits... 
        // probe with some of the failed hosts... with ttl = 2
        if ( directProbeSize < 4 )
        {
            toIdx = Math.min( 3, failedList.size() );
            sendProbeQueryToHosts( failedList.subList( 0, toIdx ), (byte)2 );

            failedProbeSize = toIdx;
        }
        
        // special rule for probe... wait per connection
        nextProcessTime = System.currentTimeMillis() +
            timeToWaitPerHop * ( directProbeSize + failedProbeSize );
        
        isProbeQuerySent = true;
    }
    
    /**
     * Sends querys to my leaves.
     * @return true if the query was forwarded to any leave, false otherwise.
     */
    private boolean processQueryToLeaves()
    {
        NLogger.debug( DynamicQueryEngine.class, "Processing query to leaves." );
        QueryRoutingTable qrt = messageService.getLastSentQueryRoutingTable();
        if ( qrt != null && qrt.containsQuery( query ) )
        {
            QueryMsg newQuery = new QueryMsg( query, (byte)1 );
            estimatedQueriedHorizon += hostsContainer.getLeafConnectionCount();
            
            NLogger.debug( DynamicQueryEngine.class, "Querying " + estimatedQueriedHorizon + " leaves.");
            messageService.forwardQueryToLeaves( newQuery,
                newQuery.getHeader().getFromHost() );
            return true;
        } else {
            if (qrt == null) {
                NLogger.warn( DynamicQueryEngine.class, "Cannot query leaves, QRT was null!" );
            } else {
                NLogger.debug( DynamicQueryEngine.class, "Not querying leaves, QRT does not contain query." );
            }
        }
        return false;
    }
    
    /**
     * Sends the probe query to the given host list.
     * @param hostList the host list to send the query to.
     * @param ttl the ttl to use with the query.
     */
    public void sendProbeQueryToHosts( List<Host> hostList, byte ttl )
    {
        Iterator<Host> iterator = hostList.iterator();
        QueryMsg newQuery = new QueryMsg( query, (byte)1 );
        
        // I2P: UPSTREAM:
        int ultrapeerCount = hostsContainer.getUltrapeerConnections().length;
        
        while( iterator.hasNext() )
        {
            // I2P: UPSTREAM:
            // If we probe all connected ultrapeers, they'll drop our subsequent
            // standard query as a duplicate. Refuse to probe if probing would
            // touch 50% or more ultrapeers.
            if (queriedHosts.size() >= (ultrapeerCount / 2))
            {
                NLogger.debug( DynamicQueryEngine.class,
                        "Aborting probe, would touch too many ultrapeers." );
                return;
            }

            Host host = iterator.next();
            host.queueMessageToSend( newQuery );
            // remember that we queried the host
            queriedHosts.add( host );
            
            // calculate the estimated reached horizon
            int degree = host.getUltrapeerDegree();
            estimatedQueriedHorizon = calculateEstimatedHorizon( degree, ttl );
        }
    }
    
    /**
     * Calculates the estimated reached horizon with a query with the
     * given ttl and a given intra ultrapeer connection degree. 
     * hosts(degree,ttl) = Sum[(degree-1)^i, 0 <= i <= ttl-1]
     * @return the estimated number of hosts queried.
     */
    private int calculateEstimatedHorizon( int degree, byte ttl )
    {
        int hostCount = 0;
        while ( ttl > 0 )
        {
            hostCount += Math.pow( degree - 1, ttl - 1 );
            ttl --;
        }
        return hostCount;
    }
    
    /**
     * Calculates the used ttl for the next query. The calculation
     * is based on the availabe connection count, the received results,
     * desired results, estimated queriered horizon, max ttl and intra
     * ultrapeer connection degree.
     * @param maxTTL the max ttl allowed on the host to query.
     * @param degree the intra ultrapeer connection degree of the host to query.
     * @param connectionCount the number of available connections to query.
     * @return the ttl to use.
     */
    private byte calculateTTL( byte maxTTL, int degree, int connectionCount )
    {
        double resultsPerHost = (double)receivedResults / (double)estimatedQueriedHorizon;
        int missingResults = desiredResults - receivedResults;
        
        int hostsNeededToQuery;
        if ( resultsPerHost == 0 )
        {
            hostsNeededToQuery = 50000;
        }
        else
        {
            hostsNeededToQuery = (int)(missingResults / resultsPerHost);
        }
        int hostsPerConnection = hostsNeededToQuery / connectionCount;
        
        for( byte i = 1; i < 6; i++ )
        {
            if( i > maxTTL )
            {
                return maxTTL;
            } 

            int hosts = (int)(16.0 * calculateEstimatedHorizon(degree, i) );
            if( hosts >= hostsPerConnection )
            {
                return i;
            }
        }
        return maxTTL;
    }
    
    /**
     * Adjusts the time to wait per hop. This is done if
     * there have not been enough results received after
     * a while and we want to go faster through the available
     * hosts.
     */
    private void adjustTimeToWaitPerHop()
    {
        // I2P:
        // Consider lowering time per hop only if it's not good enough.
        if ( timeToWaitPerHop > 1000 &&
            ( System.currentTimeMillis() - queryStartTime ) > TIMETOWAIT_ADJUSTMENT_DELAY )
        {
            double ratio;
            if ( receivedResults == 0 )
            {
                ratio = 20;
            }
            else
            {
                ratio = Math.max( 20, (desiredResults/2)/receivedResults );
            } 
            timeToWaitPerHop -= (int)(TIMETOWAIT_ADJUSTMENT * ratio);
            // I2P:
            // Don't let it drop to unrealistically good values.
            if ( timeToWaitPerHop < 1000 )
            {
               timeToWaitPerHop = 1000;
            }
        }
    }
}
