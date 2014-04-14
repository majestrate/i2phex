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
 */
package phex.test;

import java.util.BitSet;
import java.util.Iterator;

import junit.framework.TestCase;
import phex.common.QueryRoutingTable;
import phex.msg.InvalidMessageException;
import phex.msg.QRPatchTableMsg;
import phex.msg.QRResetTableMsg;
import phex.msg.QueryMsg;
import phex.msg.RouteTableUpdateMsg;
import phex.utils.AccessUtils;


public class TestQueryRoutingTable extends TestCase
{
    private QueryRoutingTable qrTable;

    public TestQueryRoutingTable(String s)
    {
        super(s);
    }

    protected void setUp()
    {
        
        qrTable = new QueryRoutingTable();
        try
        {
            // words added once with not more then 5 chars...
            AccessUtils.invokeMethod( qrTable, "addWord", "phex" );
            AccessUtils.invokeMethod( qrTable, "addWord", "java" );
            AccessUtils.invokeMethod( qrTable, "add", "test query route table" );
    
            // words added more then once... causes to be (length - 5) but
            // max 6 times.
            AccessUtils.invokeMethod( qrTable, "add", "ExtendedWord" );
            
            AccessUtils.invokeMethod( qrTable, "add", "javafo" );
        }
        catch ( Throwable exp )
        {
            fail();
        }

    }

    protected void tearDown()
    {
    }

    public void testElementCount()
    {
       Integer entryCount = (Integer)AccessUtils.getFieldValue( qrTable, "entryCount" );
       assertEquals( 12, entryCount.intValue() );
    }

    public void testContainsQueryString()
    {
        String[] trueSearchStrings =
        {
            "phex",
            "java",
            "route",
            "test query table",
            "ExtendedWord",
            "Extended",
            "ExtendedWor",
            "ExtendedWo phex"
        };
        QueryMsg query = new QueryMsg( (byte)7, "", null, false, false );
        for ( int i = 0; i < trueSearchStrings.length; i++ )
        {
            AccessUtils.setFieldValue( query, "searchString", trueSearchStrings[i] );
            assertTrue( qrTable.containsQuery( query ) );
        }

        String[] falseSearchStrings =
        {
            "wrong",
            "test query table wrong",
        };
        for ( int i = 0; i < falseSearchStrings.length; i++ )
        {
            AccessUtils.setFieldValue( query, "searchString", falseSearchStrings[i] );
            assertFalse( qrTable.containsQuery( query ) );
        }
    }

    public void testQRTInitMessages()
    {
        Iterator iterator = QueryRoutingTable.buildRouteTableUpdateMsgIterator(
            qrTable, null );

        Object obj = iterator.next();
        assertTrue( obj instanceof QRResetTableMsg );
        obj = iterator.next();
        assertTrue( obj instanceof QRPatchTableMsg );
    }

    public void testBuildAndUpdate()
        throws InvalidMessageException
    {
        Iterator iterator = QueryRoutingTable.buildRouteTableUpdateMsgIterator(
            qrTable, null );

        QueryRoutingTable newTable = new QueryRoutingTable();
        while( iterator.hasNext() )
        {
            RouteTableUpdateMsg msg = (RouteTableUpdateMsg)iterator.next();
            newTable.updateRouteTable( msg );
        }
        BitSet set = (BitSet)AccessUtils.getFieldValue( qrTable, "qrTable" );
        BitSet newSet = (BitSet) AccessUtils.getFieldValue( newTable, "qrTable" );
        assertTrue( "Found:\n" + newSet + "\nExpected:\n" + set, newSet.equals( set ) );
    }
}

