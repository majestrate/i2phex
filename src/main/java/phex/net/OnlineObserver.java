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
 *  $Id: OnlineObserver.java 4133 2008-03-01 21:38:33Z complication $
 */
package phex.net;

import org.bushe.swing.event.annotation.EventTopicSubscriber;

import phex.common.log.NLogger;
import phex.connection.ConnectionStatusEvent;
import phex.connection.ConnectionStatusEvent.Status;
import phex.event.ChangeEvent;
import phex.event.PhexEventTopics;
import phex.host.HostFetchingStrategy;
import phex.host.NetworkHostsContainer;
import phex.host.HostFetchingStrategy.FetchingReason;
import phex.prefs.core.ConnectionPrefs;
import phex.servent.OnlineStatus;
import phex.servent.Servent;

/**
 * This class tries to observers the online status of a connection.
 * If a certain amount of connection fail due to socket connection 
 * failure the online observer assumes a missing online connection 
 * and disconnects from network.
 */
public class OnlineObserver
{
    /**
     * The number of failed connections in a row.
     */
    private int failedConnections;
    private final Servent servent;
    private final HostFetchingStrategy fetchingStrategy;
    
    public OnlineObserver( Servent servent, HostFetchingStrategy fetchingStrategy )
    {
        this.fetchingStrategy = fetchingStrategy;
        this.servent = servent;
        failedConnections = 0;
        
        servent.getEventService().processAnnotations( this );
    }
    
    @EventTopicSubscriber(topic=PhexEventTopics.Net_ConnectionStatus)
    public void onConnectionStatusEvent( String topic, ConnectionStatusEvent event )
    {
        if ( event.getStatus() == Status.CONNECTION_FAILED )
        {
            // only count if there are no active connections in the network
            NetworkHostsContainer networkHostsContainer = 
                servent.getHostService().getNetworkHostsContainer();
            if ( networkHostsContainer.getTotalConnectionCount() > 0 )
            {
                failedConnections = 0;
                return;
            }
            
            failedConnections ++;
            // I2PMOD: note altered threshold
            if ( NLogger.isDebugEnabled( OnlineObserver.class ) &&
                 failedConnections % 3 == 0 )
            {
                NLogger.debug( OnlineObserver.class,
                    "Observed " + failedConnections + " failed connections.");
            }
            
            //if we have between 15 to 20 failed connections query udp host cache
            // I2PMOD: note altered threshold
            if( failedConnections % 6 == 0 )
            {
                NLogger.info( OnlineObserver.class, 
                    "Started fetching new hosts due to increasing failed connections");
            	fetchingStrategy.fetchNewHosts( FetchingReason.UpdateHosts );
            }
            
            // I2PMOD:
            // In I2P all connections may sometimes fail (if we run out of tunnels).
            // This happens rarely, and should not be a reason for giving up.
            /*
            if ( failedConnections > ConnectionPrefs.OfflineConnectionFailureCount.get().intValue() )
            {
                NLogger.debug( OnlineObserver.class,
                    "Too many connections failed.. disconnecting network.");
                servent.setOnlineStatus( OnlineStatus.OFFLINE );
            }
            */
        }
        else
        {
            // for online status we don't care if handshake failed or not...
            failedConnections = 0;
        }
    }
    
    /**
     * Reacts on online status changes to reset failed connection counter.
     */
    @EventTopicSubscriber(topic=PhexEventTopics.Servent_OnlineStatus)
    public void onOnlineStatusEvent( String topic, ChangeEvent event )
    {
        OnlineStatus oldStatus = (OnlineStatus) event.getOldValue();
        OnlineStatus newStatus = (OnlineStatus) event.getNewValue();
        if ( oldStatus == OnlineStatus.OFFLINE && 
             newStatus != OnlineStatus.OFFLINE )
        {// switch from offline to any online status
            failedConnections = 0;
        }
    }
}
