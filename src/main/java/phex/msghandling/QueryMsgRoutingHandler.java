package phex.msghandling;

import phex.common.QueryRoutingTable;
import phex.host.Host;
import phex.host.NetworkHostsContainer;
import phex.msg.QueryMsg;
import phex.query.DynamicQueryConstants;
import phex.servent.Servent;


class QueryMsgRoutingHandler implements MessageSubscriber<QueryMsg>
{
    private final Servent servent;
    private final NetworkHostsContainer hostsContainer;

    public QueryMsgRoutingHandler( Servent servent )
    {
        this.servent = servent;
        this.hostsContainer = servent.getHostService().getNetworkHostsContainer();
    }

    /**
     * Routes received QueryMsg through this node.
     * <p>Called to forward a query to connected neighbors. This is only done 
     * under special conditions.<br>
     * When we are in Leaf mode we hold connections to Ultrapeers:<br>
     * - Never forward an incoming query to a ultrapeer.<br>
     * <br>
     * When we are in Ultrapeer mode we hold connections to Ultrapeers and Leafs. 
     * We know the leafs QRT and the Ultrapeers intra-UP QRT therefore:<br>
     * - Never forward a query that does not match a QRT entry.<br>
     * <br>
     * This strategy is used to separate the broadcast traffic of the peer
     * network from the Ultrapeer/Leaf network and is essential for a correct
     * Ultrapeer proposal support.</p>
     *
     * <p>This does not affect the TTL or hops fields of the message.</p>
     *
     * @param queryMsg the QueryMsg to forward
     * @param sourceHost the Host this message was coming from.
     */
    public void onMessage(QueryMsg queryMsg, Host sourceHost)
    {
        // Never forward a message coming from a ultrapeer when in leaf mode!
        if ( !servent.isShieldedLeafNode() )
        {
            if ( sourceHost.isUltrapeerLeafConnection() )
            {// do dynamic query for my leaf.
                servent.getQueryService().sendDynamicQuery( queryMsg,
                    DynamicQueryConstants.DESIRED_LEAF_RESULTS );
            }
            else
            {
                // only forward to ultrapeers if TTL > 0
                if ( queryMsg.getHeader().getTTL() > 0 )
                {
                    forwardQueryToUltrapeers( queryMsg, sourceHost );
                }
                // Forward query to Leafs regardless of TTL
                // see section 2.4 Ultrapeers and Leaves Single Unit of
                // Gnutella Ultrapeer Query Routing v0.1
                forwardQueryToLeaves( queryMsg, sourceHost );
            }
        }
    }
    
    
    
    /**
     * Forward query to Leafs regardless of TTL
     * see section 2.4 Ultrapeers and Leaves Single Unit of
     * Gnutella Ultrapeer Query Routing v0.1
     * @param msg query to forward.
     * @param fromHost the host the query comes from and
     *        query is not forwarded to
     */
    public void forwardQueryToLeaves( QueryMsg msg, Host fromHost )
    {
        Host[] hosts = hostsContainer.getLeafConnections();
        for ( int i = 0; i < hosts.length; i++ )
        {
            if ( hosts[i] == fromHost )
            {
                continue;
            }
            QueryRoutingTable qrt = hosts[i].getLastReceivedRoutingTable();
            if ( qrt != null && !qrt.containsQuery( msg ) )
            {
                continue;
            }
            hosts[i].queueMessageToSend( msg );
        }
    }

    /**
     * Forwards a query to the given hosts but never to the from Host.
     * @param msg the query to forward
     * @param fromHost the host the query came from.
     * @param hosts the hosts to forward to.
     */
    public void forwardQueryToUltrapeers( QueryMsg msg, Host fromHost )
    {
        Host[] ultrapeers = hostsContainer.getUltrapeerConnections();
        boolean lastHop = msg.getHeader().getTTL() == 1;
        for ( int i = 0; i < ultrapeers.length; i++ )
        {
            if ( ultrapeers[i] == fromHost )
            {
                continue;
            }
            // a query on last hop is forwarded to other Ultrapeers
            // with the use of a possibly available QRT.
            if ( lastHop && ultrapeers[i].isUPQueryRoutingSupported() )
            {
                QueryRoutingTable qrt = ultrapeers[i].
                    getLastReceivedRoutingTable();
                if ( qrt != null && !qrt.containsQuery( msg ) )
                {
                    continue;
                }
            }
            ultrapeers[i].queueMessageToSend( msg );
        }
    }
}
