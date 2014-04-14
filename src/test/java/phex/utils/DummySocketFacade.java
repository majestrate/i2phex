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
package phex.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.ByteChannel;

import phex.common.address.DestAddress;
import phex.io.channels.StreamingByteChannel;
import phex.net.repres.SocketFacade;

public class DummySocketFacade implements SocketFacade
{
    private ByteArrayInputStream inStream;
    private ByteArrayOutputStream outStream;
    private StreamingByteChannel channel;
    
    public DummySocketFacade( byte[] inStreamContent )
    {
        inStream = new ByteArrayInputStream( inStreamContent );
        outStream = new ByteArrayOutputStream( );
    }
    
    public byte[] getOutData()
    {
        return outStream.toByteArray();
    }
    
    public void close() throws IOException
    {
        IOUtil.closeQuietly( inStream );
        IOUtil.closeQuietly( outStream );
    }

    public DestAddress getRemoteAddress()
    {
        return null;
    }

    public void setSoTimeout(int socketRWTimeout) throws SocketException
    {
    }

    public ByteChannel getChannel() throws IOException
    {
        if ( channel == null )
        {
            channel = new StreamingByteChannel( inStream, outStream );
        }
        return channel;
    }      
}