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
 *  $Id: GWebCacheManager.java 4512 2011-06-02 21:30:33Z complication $
 */
package phex.gwebcache;

import java.util.TimerTask;

import phex.common.Environment;
import phex.common.address.DestAddress;
// import phex.common.address.IpAddress;
import phex.common.log.NLogger;
import phex.host.NetworkHostsContainer;
import phex.servent.Servent;

/**
 * 
 */
public class GWebCacheManager
{
    private final Servent servent;
    private final GWebCacheContainer gWebCacheContainer;
    
    /**
     * Lock to make sure not more then one thread request is running in parallel
     * otherwise it could happen that we create thread after thread while each
     * one takes a long time to come back.
     */
    private boolean isThreadRequestRunning = false;
    
    public GWebCacheManager( Servent servent )
    {
        this.servent = servent;
        gWebCacheContainer = new GWebCacheContainer( servent );
    }
    
    /**
     * Starts a query for more hosts in an extra thread.
     */
    public synchronized void invokeQueryMoreHostsRequest( boolean preferPhex )
    {
        // we dont want multiple thread request to run at once. If one thread
        // request is running others are blocked.
        if ( isThreadRequestRunning )
        {
            return;
        }
        isThreadRequestRunning = true;
        Runnable runner = new QueryHostsRunner( preferPhex );
        Environment.getInstance().executeOnThreadPool( runner,
            "GWebCacheQuery-" + Integer.toHexString(runner.hashCode()) );
    }
    /**
     * Starts a update of GWebCaches in an extra thread.
     */
    public void invokeUpdateRemoteGWebCache( final DestAddress myHostAddress, boolean preferPhex )
    {
        Runnable runner = new UpdateGWebCacheRunner(myHostAddress, preferPhex);
        Environment.getInstance().executeOnThreadPool( runner,
            "GWebCacheQuery-" + Integer.toHexString(runner.hashCode()) );
    }
    /**
     * Starts a query for more GWebCaches in an extra thread.
     */
    public synchronized void invokeQueryMoreGWebCachesRequest( boolean preferPhex )
    {
        // we dont want multiple thread request to run at once. If one thread
        // request is running others are blocked.
        if ( isThreadRequestRunning )
        {
            return;
        }
        isThreadRequestRunning = true;
        Runnable runner = new QueryGWebCachesRunner( preferPhex );
        Environment.getInstance().executeOnThreadPool( runner,
            "GWebCacheQuery-" + Integer.toHexString(runner.hashCode()) );
    }

    // temporary workaround method for post manager initialization
    public void postManagerInitRoutine()
    {
        NetworkHostsContainer netCont = servent.getHostService().getNetworkHostsContainer();

        // Perform the first webcache query and the first webcache update
        // nearly immediately after startup (5 and 10 seconds).

        Environment.getInstance().scheduleTimerTask( 
            new QueryGWebCacheTimer( netCont ), 5000,
            QueryGWebCacheTimer.TIMER_PERIOD );
        Environment.getInstance().scheduleTimerTask( 
            new UpdateGWebCacheTimer( netCont ), 10000,
            UpdateGWebCacheTimer.TIMER_PERIOD );
    }
    
    ////////////////////////////////////////////////////////////////////////////
    /// Inner classes
    ////////////////////////////////////////////////////////////////////////////
    private final class QueryGWebCachesRunner implements Runnable
    {
        private boolean preferPhex;

        /**
         * @param preferPhex
         */
        public QueryGWebCachesRunner( boolean preferPhex )
        {
            this.preferPhex = preferPhex;
        }

        public void run()
        {
            try
            {
                gWebCacheContainer.queryMoreGWebCaches( preferPhex );
            }
            finally
            {
                isThreadRequestRunning = false;
            }
        }
    }
    
    private final class UpdateGWebCacheRunner implements Runnable
    {
        private final DestAddress myHostAddress;
        private boolean preferPhex;
        
        /**
         * @param preferPhex
         */
        private UpdateGWebCacheRunner(DestAddress myHostAddress, boolean preferPhex)
        {
            super();
            this.myHostAddress = myHostAddress;
            this.preferPhex = preferPhex;
        }
        
        public void run()
        {
            gWebCacheContainer.updateRemoteGWebCache( myHostAddress, preferPhex );
        }
    }
    
    private final class QueryHostsRunner implements Runnable
    {
        private boolean preferPhex;
        
        /**
         * @param preferPhex
         */
        public QueryHostsRunner( boolean preferPhex )
        {
            this.preferPhex = preferPhex;
        }
        
        public void run()
        {
            try
            {
                gWebCacheContainer.queryMoreHosts( preferPhex );
            }
            finally
            {
                isThreadRequestRunning = false;
            }
        }
    }
    
    private final class QueryGWebCacheTimer extends TimerTask
    {
        // every 60 minutes
        public static final long TIMER_PERIOD = 1000 * 60 * 60;
        
        private final NetworkHostsContainer netHostsContainer;
        
        QueryGWebCacheTimer( NetworkHostsContainer netHostsContainer )
        {
            this.netHostsContainer = netHostsContainer;
        }

        @Override
        public void run()
        {
            try
            {
                // no gwebcache actions if we have no auto connect and are
                // not connected to any host

                // I2PMOD: we must be able to report our existence even if we don't see others,
                // otherwise we can't bootstrap the network. Thus, only check
                // if we want to be online. If yes, report our existence.

                if ( servent.getOnlineStatus().isNetworkOnline() ) 
                   // || netHostsContainer.getTotalConnectionCount() > 0 )
                {
                    invokeQueryMoreHostsRequest( true );
                }
            }
            catch ( Throwable th)
            {
                NLogger.error( GWebCacheManager.class, th, th );
            }
        }
    }
    
    private final class UpdateGWebCacheTimer extends TimerTask
    {
        // once per 90 minutes
        public static final long TIMER_PERIOD = 1000 * 60 * 90;
        
        private final NetworkHostsContainer netHostsContainer;
        
        public UpdateGWebCacheTimer( NetworkHostsContainer netHostsContainer )
        {
            this.netHostsContainer = netHostsContainer;
        }

        @Override
        public void run()
        {
            // no gwebcache actions if we have no auto connect and are
            // not connected to any host

            // I2PMOD: we must be able to report our existence even if we don't see others,
            // otherwise we can't bootstrap the network. Thus, only check
            // if we want to be online. If yes, report our existence.

            if ( servent.getOnlineStatus().isNetworkOnline() ) 
               // || netHostsContainer.getTotalConnectionCount() > 0 )
            {
                // I2PMOD: we are never firewalled and never have a local IP, so just proceed.
                DestAddress localAddress = servent.getLocalAddress();

                // even when localAddress is null update a GWebCache with
                // a new GWebCache URL.

                invokeUpdateRemoteGWebCache( localAddress, true );
                
                //invokeQueryMoreGWebCachesRequest( false );
            }
        }
    }
}
