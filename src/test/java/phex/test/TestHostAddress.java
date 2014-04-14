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
 *  $Id: TestHostAddress.java 3942 2007-09-29 15:41:44Z gregork $
 */
package phex.test;

import junit.framework.TestCase;
import phex.common.address.AddressUtils;
import phex.common.address.DefaultDestAddress;
import phex.common.address.DestAddress;
import phex.common.address.IpAddress;



public class TestHostAddress extends TestCase
{

    public TestHostAddress(String s)
    {
        super(s);
    }

    protected void setUp()
    {
    }

    protected void tearDown()
    {
    }

    public void testIPClass()
    {
        try
        {
            DestAddress address;
            for ( int i = 1; i < 128; i++ )
            {
                address = new DefaultDestAddress(  i+ ".1.1.1", 80 );
                assertEquals( String.valueOf( i ), IpAddress.IPClass.CLASS_A, 
                    address.getIpAddress().getIPClass() );
            }
            for ( int i = 128; i < 192; i++ )
            {
                address = new DefaultDestAddress(  i+ ".1.1.1",80 );
                assertEquals( String.valueOf( i ), IpAddress.IPClass.CLASS_B,
                    address.getIpAddress().getIPClass() );
            }
            for ( int i = 192; i < 224; i++ )
            {
                address = new DefaultDestAddress(  i+ ".1.1.1",80 );
                assertEquals( String.valueOf( i ), IpAddress.IPClass.CLASS_C, 
                    address.getIpAddress().getIPClass() );
            }
        }
        catch ( Exception exp )
        {
            exp.printStackTrace();
            fail( exp.getMessage() );
        }
    }

    public void testIsIPValid() throws Exception
    {
        DestAddress address;

        address = new DefaultDestAddress( "1.1.1.1", 80 );
        assertTrue( address.getIpAddress().isValidIP() );
        address = new DefaultDestAddress( "130.130.130.130", 80 );
        assertTrue( address.getIpAddress().isValidIP() );
        address = new DefaultDestAddress( "200.200.200.200", 80 );
        assertTrue( address.getIpAddress().isValidIP() );

        address = new DefaultDestAddress( "1.0.0.0", 80 );
        assertFalse( address.getIpAddress().isValidIP() );
        address = new DefaultDestAddress( "130.130.0.0", 80 );
        assertFalse( address.getIpAddress().isValidIP() );
        address = new DefaultDestAddress( "200.200.200.0", 80 );
        assertFalse( address.getIpAddress().isValidIP() );

        address = new DefaultDestAddress( "1.0.0.1", 80 );
        assertTrue( address.getIpAddress().isValidIP() );
        address = new DefaultDestAddress( "1.0.1.0",80 );
        assertTrue( address.getIpAddress().isValidIP() );
        address = new DefaultDestAddress( "1.1.0.0",80 );
        assertTrue( address.getIpAddress().isValidIP() );
        address = new DefaultDestAddress( "130.0.0.1",80 );
        assertTrue( address.getIpAddress().isValidIP() );
        address = new DefaultDestAddress( "130.0.1.0",80 );
        assertTrue( address.getIpAddress().isValidIP() );
        address = new DefaultDestAddress( "200.0.0.1",80 );
        assertTrue( address.getIpAddress().isValidIP() );

        address = new DefaultDestAddress( "0.0.0.0",80 );
        assertFalse( address.getIpAddress().isValidIP() );
    }

    public void testHashEquals()
    {
        try
        {
            DestAddress hostaddress = new DefaultDestAddress( "127.0.0.1", 4444 );
            DestAddress hostaddress2 =  new DefaultDestAddress( "127.0.0.1", 4444 );
            boolean booleanRet = hostaddress.equals(hostaddress2);
            assertEquals( true, booleanRet );

            int hash1 = hostaddress.hashCode();
            int hash2 = hostaddress2.hashCode();
            assertEquals( hash1, hash2 );
        }
        catch ( Exception exp )
        {
            fail( exp.getMessage() );
        }
    }

    public void testPortInRange()
    {
        assertEquals( false, AddressUtils.isPortInRange( -1 ) );
        assertEquals( false, AddressUtils.isPortInRange( 0 ) );
        assertEquals( true, AddressUtils.isPortInRange( 1 ) );
        assertEquals( true, AddressUtils.isPortInRange( 256 ) );
        assertEquals( true, AddressUtils.isPortInRange( 13244 ) );
        assertEquals( true, AddressUtils.isPortInRange( 0xFFFF ) );
    }
    
    public void testIpLookup()
    {
        DestAddress address = new DefaultDestAddress("google.de", 80);
        address.getIpAddress();
        address.getIpAddress();
        address.getIpAddress();
        address.getIpAddress();
        address.getIpAddress();
        address.getIpAddress();
        address.getIpAddress();
    }
}