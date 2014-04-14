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
 *  --- SVN Information ---
 *  $Id$
 */
package phex.net.repres.i2p;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;

import phex.common.address.*;
import phex.net.repres.PresentationManager;
import phex.net.repres.SocketFacade;
import phex.prefs.core.I2PPrefs;
import phex.common.log.NLogger;
import phex.utils.SystemProperties;

import net.i2p.client.I2PClientFactory;
import net.i2p.client.streaming.I2PSocketManagerFactory;
import net.i2p.client.streaming.I2PSocketManager;
import net.i2p.client.streaming.I2PSocketManagerFull;
import net.i2p.client.streaming.I2PServerSocket;
import net.i2p.client.streaming.I2PSocket;
import net.i2p.data.Destination;
import net.i2p.data.DataFormatException;
import net.i2p.I2PException;


public class I2PPresentationManager extends PresentationManager
{
    // I2P destination addresses cannot have ports.
    // No matter what is suggested by input data, we use an invalid port.
    public static final int INVALID_PORT = -1;

    // I2PFIXME:
    // We should probably ensure better that the keyfile ends up
    // among other configuration files.
    private static String I2P_KEYFILE_NAME = "i2phex.key";
    
    protected I2PSocketManager socketManager;
    
    @Override
    public SocketFacade createSocket( DestAddress address, int connectTimeout )
        throws IOException
    {
        try
        {
            I2PSocket socket = socketManager.connect( new Destination(address.getHostName()) );
            return new I2PSocketFacade( socket );
        }
        catch (DataFormatException e)
        {
            throw new IOException( "Failed creating I2PSocketFacade, DataFormatException." );
        }
        catch (I2PException e)
        {
            throw new IOException( "Failed creating I2PSocketFacade, I2PException." );
        }
    }
    
    @Override
    public boolean initialize() {
        
        File keyFile;
        try
        {
            keyFile = new File( SystemProperties.getPhexConfigRoot(), I2P_KEYFILE_NAME );
            if (!keyFile.exists()) I2PClientFactory.createClient().createDestination(new FileOutputStream(keyFile));
        }
        catch (Exception e)
        {
            NLogger.error( I2PPresentationManager.class, "Failed checking/creating I2P keyfile!" );
            return false;
        }
        
        try {
            // Create a properties object to hold I2P properties
            Properties p = new Properties();

            // Get property values from I2P preferences
            p.setProperty(I2PSocketManagerFactory.PROP_MANAGER, I2PSocketManagerFull.class.getName());
            p.setProperty("inbound.length", I2PPrefs.InboundLength.get().toString());
            p.setProperty("outbound.length", I2PPrefs.OutboundLength.get().toString());
            p.setProperty("inbound.lengthVariance", I2PPrefs.InboundLengthVariance.get().toString());
            p.setProperty("outbound.lengthVariance", I2PPrefs.OutboundLengthVariance.get().toString());
            p.setProperty("inbound.quantity", I2PPrefs.InboundQuantity.get().toString());
            p.setProperty("outbound.quantity", I2PPrefs.OutboundQuantity.get().toString());
            p.setProperty("inbound.backupQuantity", I2PPrefs.InboundBackupQuantity.get().toString());
            p.setProperty("outbound.backupQuantity", I2PPrefs.OutboundBackupQuantity.get().toString());
            p.setProperty("inbound.nickname", I2PPrefs.InboundNickname.get());
            p.setProperty("outbound.nickname", I2PPrefs.OutboundNickname.get());

            // Use socket manager factory to create socket manager from keyfile
            socketManager = I2PSocketManagerFactory.createManager( new FileInputStream( keyFile ),
                I2PPrefs.I2CPHost.get().toString(), I2PPrefs.I2CPPort.get(), p );
            
            if (socketManager == null)
            {
                NLogger.error( I2PPresentationManager.class, "Got a null socket manager, I2P router unreachable!");
                return false;
            }
        }
        catch (FileNotFoundException e)
        {
            NLogger.error( I2PPresentationManager.class, "Failed reading I2P keyfile!");
            return false;
        }
            
        return true;
    }
    
    /**
     * Returns an I2PSocketManager which can be used to create server sockets,
     * determine the local destination, and perform other work.
     */
    @Override
    public I2PSocketManager getSocketManager()
    {
        return socketManager;
    }
    
    /**
     * Creates a DestAddress address object from a given address representation.
     * The address representation we expect is a base64 encoded I2P destination.
     * 
     * @param address a address representation.
     * @return a destination address.
     */
    public DestAddress createHostAddress( String address, int defaultPort ) 
        throws MalformedDestAddressException
    {
        DestAddress destAddress = null;
        // I2PMOD:
        // Let's avoid changing the DestAddress interface currently,
        // and instead isolate callers over here from the NullPointerExceptions
        // which the I2PDestAddress constructor could throw.
        try
        {
            destAddress = new I2PDestAddress( address, INVALID_PORT );
        }
        catch (NullPointerException e)
        {
            throw new MalformedDestAddressException("Malformed destination address.");
        }
        return destAddress;
    }
    
    /**
     * Creates a DestAddress object from a given IpAddress and port.
     * The I2P implementation always returns null.
     */
    public DestAddress createHostAddress( IpAddress ipAddress, int port )
    {
        // I2PFIXME:
        // Determine the side effects of returning null here
        // and possibly log calls to this method, to detect when it happens.
        return null;
    }
    
    /* Creates a DestAddress object from a given byte array and port.
     * The I2P implementation always returns null.
     */
    public DestAddress createHostAddress ( byte[] aHostIP, int aPort )
    {
        // I2PFIXME:
        // Determine the side effects of returning null here
        // and possibly log calls to this method, to detect when it happens.
        return null;
    }
}
