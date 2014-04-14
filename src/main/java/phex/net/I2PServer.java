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
 *  $Id$
 */

package phex.net;

import java.io.IOException;
import java.net.SocketException;

import phex.common.Environment;
import phex.common.address.DestAddress;
import phex.common.address.IpAddress;
import phex.connection.IncomingConnectionDispatcher;
import phex.host.NetworkHostsContainer;
import phex.net.repres.PresentationManager;
import phex.net.repres.SocketFacade;
import phex.net.repres.i2p.I2PSocketFacade;
import phex.prefs.core.NetworkPrefs;
import phex.security.AccessType;
import phex.security.PhexSecurityException;
import phex.security.PhexSecurityManager;
import phex.common.log.NLogger;
import phex.common.address.MalformedDestAddressException;
import phex.servent.Servent;

import net.i2p.client.streaming.I2PSocketManager;
import net.i2p.client.streaming.I2PSocketManagerFull;
import net.i2p.client.streaming.I2PServerSocket;
import net.i2p.client.streaming.I2PSocket;
import net.i2p.data.Destination;
import net.i2p.data.DataFormatException;
import net.i2p.I2PException;

/*
 * An I2P-specific server class instantiated by NetworkManager.
 * A sufficiently ugly kludge to currently be mutually exclusive with
 * both OIOServer and NIOServer.
 *
 * Fixing would likely include moving Internet-specific code out of Server
 * into NIOServer and OIOServer, while containing I2P-specific code
 * strictly inside I2PServer.
 */

public class I2PServer extends Server {
    
    // Cache the socket manager reference locally,
    // getting it each time from I2PPresentationManager is wasteful.
    protected I2PSocketManager socketManager;
    
    protected I2PServerSocket serverSocket;
    
    public I2PServer( Servent servent )
    {
        super( servent );
        // Firewall checks aren't necessay in I2P, don't schedule them.
    }

    public int getListeningLocalPort()
    {
        // Returning a valid port isn't possible in I2P.
        return INVALID_PORT;
    }
    
    public IpAddress resolveLocalHostIP()
    {
        // Returning a vald IP address isn't possible in I2P.
        return null;
    }
    
    public void resetFirewallCheck()
    {
        // No firewalls in I2P, hence nothing to be done.
    }
    
    // The listening thread.
    public void run()
    {
        if (NLogger.isDebugEnabled( I2PServer.class ))
        {
            NLogger.debug( I2PServer.class,"I2P server listening on: " +
                socketManager.getSession().getMyDestination().toBase64() );
        }
        
        try
        {
            // I2PFIXME:
            // I2PServerSocket does not support an isClosed() method currently.
            // It may however prove possible to implement if needed.
            while ( serverSocket != null )
            {  
                try
                {
                    I2PSocket incoming = serverSocket.accept();
                    // create facade...
                    I2PSocketFacade incomingFacade = new I2PSocketFacade( incoming );
                    handleIncomingSocket( incomingFacade );
                }
                catch ( SocketException exp )
                {
                    NLogger.debug( I2PServer.class, exp );
                }
                catch ( PhexSecurityException exp )
                {
                    NLogger.debug( I2PServer.class, exp );
                }
                catch (IOException exp)
                {
                    NLogger.error( I2PServer.class, exp, exp);
                }
            }
        }
        catch ( Exception exp )
        {
            NLogger.error( I2PServer.class, exp, exp );
        }

        isRunning = false;
        NLogger.debug( I2PServer.class, "I2P listener stopped." );
        
        // I2PFIXME: IMPORTANT:
        // Figure out what the equivalent actions are for I2P.
        // Until then, set the address to null, even if it breaks things.
        DestAddress address = null;
        localAddress.updateLocalAddress( address );
        synchronized(this)
        {
            notifyAll();
        }
    }
    
    private void handleIncomingSocket(SocketFacade clientSocket )
        throws IOException, PhexSecurityException
    {
        // I2PFIXME:
        // It could be a bad idea, to take a value meant for TCP sockets
        // and apply it here to an I2P socket, which performs differently.
        // Then again, leaving it unapplied could be a bad idea too,
        // since I2P sockets could have unexpected default behaviour.
        clientSocket.setSoTimeout( NetworkPrefs.TcpRWTimeout.get().intValue() );

        DestAddress address = clientSocket.getRemoteAddress();
        NetworkHostsContainer netHostsContainer = Servent.getInstance().getHostService()
            .getNetworkHostsContainer();

        // I2PMOD:
        // No equivalent exists in I2P for a site-local address,
        // so cannot check against it.
        
        assert address != null;
        if (!netHostsContainer.isConnectedToHost(address))
        {
            hasConnectedIncomming = true;
            lastInConnectionTime = System.currentTimeMillis();
        }
        
        // I2PMOD:
        // No direct equivalent exists in I2P for configuring TCP delays,
        // trickery with the streaming lib is for much later.

        // Create a Host object for the incoming connection
        // and hand it off to a ReadWorker to handle.
        AccessType access = servent.getSecurityService()
            .controlHostAddressAccess(address);
        switch (access)
        {
            case ACCESS_DENIED:
            case ACCESS_STRONGLY_DENIED:
                throw new PhexSecurityException("Host access denied: " + address );
        }

        NLogger.debug( I2PServer.class, "Accepted incoming connection from: "
                + address.getFullHostName());

        IncomingConnectionDispatcher dispatcher = new IncomingConnectionDispatcher(
            clientSocket, Servent.getInstance() );
        Environment.getInstance().executeOnThreadPool( dispatcher,
            "IncomingConnectionDispatcher-" + Integer.toHexString(hashCode()));
    }

    
    @Override
    protected synchronized void bind( int initialPort ) throws IOException
    {
        // Ignore initialPort, since we cannot bind to ports.

        assert (serverSocket == null);

        // Get an I2PSocketManager from the I2PresentationManager
        // Get an I2PServerSocket from the I2PSocketManager
        socketManager = PresentationManager.getInstance().getSocketManager();
        serverSocket = socketManager.getServerSocket();

        if (serverSocket == null)
        {
            NLogger.debug( I2PServer.class, "Got a null I2PServerSocket, something is wrong!" );
            throw new IOException( "Got a null I2PServerSocket, something is wrong!" );
        }

        // Since I2P has no concept of a port,
        // the process of trying different ports cannot be implemented here.
        
        try {
            // Create a DestAddress object from the local destination key
            // which I2PSocketManager says we're listening on.
            DestAddress address = PresentationManager.getInstance().createHostAddress(
                socketManager.getSession().getMyDestination().toBase64(), INVALID_PORT );
            
            // Update local address.
            localAddress.updateLocalAddress( address );
        }
        catch (MalformedDestAddressException e)
        {
            NLogger.debug( I2PServer.class, "Failed creating local DestAddress!" );
            throw new IOException( "Failed creating local DestAddress!" );
        }
    }
    
    
    @Override
    protected synchronized void closeServer()
    {
        if ( serverSocket != null )
        {
            try
            {
                serverSocket.close();
            }
            catch (Exception exp)
            {
                // Doing close() on an I2PServerSocket should never throw this.
            }
            serverSocket = null;
        }
    }
    
}
