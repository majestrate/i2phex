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
package phex.msghandling;

import java.net.Socket;

import junit.framework.TestCase;
import phex.common.address.DefaultDestAddress;
import phex.common.bandwidth.BandwidthController;
import phex.host.Host;
import phex.msg.GUID;
import phex.net.connection.Connection;
import phex.net.repres.def.DefaultSocketFacade;
import phex.utils.QueryGUIDRoutingPair;


public class MessageRoutingTest extends TestCase
{
    private MessageRouting messageRouting;

    public MessageRoutingTest(String s)
    {
        super(s);
    }

    @Override
    protected void setUp()
    {
        messageRouting = new MessageRouting( );
    }

    public void testAddToPushRoutingTable()
        throws Exception
    {
        GUID pushClientGUID = new GUID( );
        Host pushHost = new Host( new DefaultDestAddress( "1.1.1.1", 1111 ) );
        // to fake a connection
        pushHost.setConnection( new Connection( new DummySocket(), 
            new BandwidthController( "JUnitText", Long.MAX_VALUE )) );
        messageRouting.addToPushRoutingTable( pushClientGUID, pushHost );

        Host host = messageRouting.getPushRouting( pushClientGUID );
        assertEquals( pushHost, host );
    }

    public void testCheckAndAddToPingRoutingTable()
        throws Exception
    {
        GUID pingGUID = new GUID();
        Host pingHost =  new Host( new DefaultDestAddress( "2.2.2.2", 2222 ) );
        // to fake a connection
        pingHost.setConnection( new Connection( new DummySocket(), 
            new BandwidthController( "JUnitText", Long.MAX_VALUE ) ) );

        boolean pingCheckValue = messageRouting.checkAndAddToPingRoutingTable(
            pingGUID, pingHost );
        assertEquals( true, pingCheckValue );
        pingCheckValue = messageRouting.checkAndAddToPingRoutingTable(
            pingGUID, pingHost );
        assertEquals( false, pingCheckValue );

        Host host = messageRouting.getPingRouting( pingGUID );
        assertEquals( pingHost, host );
    }

    public void testCheckAndAddToQueryRoutingTable()
        throws Exception
    {
        GUID queryGUID = new GUID();
        Host queryHost =  new Host( new DefaultDestAddress( "3.3.3.3", 3333 ) );
        // to fake a connection
        queryHost.setConnection( new Connection( new DummySocket(), 
            new BandwidthController( "JUnitText", Long.MAX_VALUE )) );

        boolean queryCheckValue = messageRouting.checkAndAddToQueryRoutingTable(
            queryGUID, queryHost );
        assertEquals( true, queryCheckValue );
        queryCheckValue = messageRouting.checkAndAddToQueryRoutingTable(
            queryGUID, queryHost );
        assertEquals( false, queryCheckValue );

        QueryGUIDRoutingPair pair = messageRouting.getQueryRouting( queryGUID, 0 );
        assertEquals( queryHost, pair.getHost() );
    }

    private class DummySocket extends DefaultSocketFacade
    {
        DummySocket()
        {
            super( new Socket() );
        }
    }
}