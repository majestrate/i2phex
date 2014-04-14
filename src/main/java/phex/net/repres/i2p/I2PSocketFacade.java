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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.channels.ByteChannel;

import net.i2p.client.I2PClientFactory;
import net.i2p.client.streaming.I2PSocketManagerFactory;
import net.i2p.client.streaming.I2PSocketManager;
import net.i2p.client.streaming.I2PSocketManagerFull;
import net.i2p.client.streaming.I2PServerSocket;
import net.i2p.client.streaming.I2PSocket;
import net.i2p.data.Destination;
import net.i2p.data.DataFormatException;
import net.i2p.I2PException;

import phex.common.address.I2PDestAddress;
import phex.common.address.DestAddress;
import phex.io.channels.StreamingByteChannel;
import phex.net.repres.SocketFacade;

public class I2PSocketFacade implements SocketFacade
{
    private I2PSocket socket;
    private StreamingByteChannel channel;
    private DestAddress remoteAddress;
    
    public I2PSocketFacade( I2PSocket aSocket )
    {
        this.socket = aSocket;
    }
    
    public void setSoTimeout( int socketRWTimeout ) throws SocketException
    {
        // I2PFIXME:
        // Figure out how to set both a read and write timeout.
        // Until then, set read timeout only.
        socket.setReadTimeout((long) socketRWTimeout);
    }
    
    public ByteChannel getChannel() throws IOException
    {
        if ( channel == null )
        {
            channel = new StreamingByteChannel( socket.getInputStream(),
                socket.getOutputStream() );
        }
        return channel;
    }

    public void close() throws IOException
    {
        socket.close();
    }

    public DestAddress getRemoteAddress()
    {
        if ( remoteAddress == null )
        {
            remoteAddress = new I2PDestAddress(socket.getPeerDestination());
        }
        return remoteAddress;
    }
}
