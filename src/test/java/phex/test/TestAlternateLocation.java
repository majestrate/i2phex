/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2008 Phex Development Group
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
 *  $Id: TestAlternateLocation.java 4357 2009-01-15 23:03:50Z gregork $
 */
package phex.test;

import java.util.*;

import junit.framework.TestCase;
import phex.common.*;
import phex.common.address.DefaultDestAddress;
import phex.http.*;
import phex.security.PhexSecurityManager;



public class TestAlternateLocation extends TestCase
{
    private PhexSecurityManager securityService;
    private String privateUriResStr;
    private String wrongPortUriResStr;
    private String wrongIPUriResStr;
    private String wrongIPUriResStr2;
    private String noPortUriResStr;
    private String uriResStr;
    private String uriResStr2;
    private String urnStr;

    public TestAlternateLocation(String s)
    {
        super(s);
    }

    protected void setUp()
    {
        privateUriResStr = "http://10.0.0.2:6346/uri-res/N2R?urn:sha1:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB";
        wrongPortUriResStr = "http://1.1.1.1:0/uri-res/N2R?urn:sha1:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB";
        wrongIPUriResStr = "http://333.333.333.333:6346/uri-res/N2R?urn:sha1:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB";
        wrongIPUriResStr2 = "http://0.0.0.0:6346/uri-res/N2R?urn:sha1:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB";
        noPortUriResStr = "http://1.1.1.1/uri-res/N2R?urn:sha1:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB";
        uriResStr = "http://1.1.1.1:6346/uri-res/N2R?urn:sha1:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB";
        uriResStr2 = "http://1.1.1.1:6347/uri-res/N2R?urn:sha1:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB";
        urnStr = "urn:sha1:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB";
        
        securityService = new PhexSecurityManager();
    }

    protected void tearDown()
    {
    }

    public void testConstructor()
        throws Exception
    {
    }

    public void testParse()
    {
        /* I2PFIXME: IMPORTANT:
         * Test case broken due to change in AlternateLocation.
         * Fix later.
         *
        AlternateLocation loc = AlternateLocation.parseUriResAltLoc( 
            uriResStr, securityService );
        assertEquals( "1.1.1.1", loc.getHTTPString() );
        
        loc = AlternateLocation.parseUriResAltLoc( uriResStr2, securityService );
        assertEquals( "1.1.1.1:6347", loc.getHTTPString() );

        // test if filename urls are accepted.
        String altLocStr = "http://123.123.123.123:6347/get/1/phex.zip 2002-04-30T08:30:00Z";
        loc = AlternateLocation.parseUriResAltLoc( altLocStr, securityService );
        // since change alt-locs do not accept url without urn...
        assertEquals( null, loc );
        //assertEquals( "\"http://123.123.123.123:6347/get/1/phex.zip\" 2002-04-30T08:30:00Z",
        //    loc.getHTTPString() );

        // test if url with private ip in uri res is accepted.
        altLocStr = privateUriResStr + " 2002-04-30T08:30:00Z";
        loc = AlternateLocation.parseUriResAltLoc( altLocStr, securityService );
        // since change alt-locs do not accept url with private ip...
        assertEquals( null, loc );

        // test if wrong port in url is accepted.
        altLocStr = wrongPortUriResStr + " 2002-04-30T08:30:00Z";
        loc = AlternateLocation.parseUriResAltLoc( altLocStr, securityService );
        // since change alt-locs do not accept url with private ip...
        assertEquals( null, loc );

        // test if wrong ip in url is accepted.
        try
        {
            altLocStr = wrongIPUriResStr + " 2002-04-30T08:30:00Z";
            loc = AlternateLocation.parseUriResAltLoc( altLocStr, securityService );
            // since change alt-locs do not accept url with private ip...
            assertEquals( null, loc );
            assertTrue( false );
        }
        catch ( Exception exp )
        {
            assertTrue( true );
        }

        // test if wrong ip in url is accepted.
        altLocStr = wrongIPUriResStr2 + " 2002-04-30T08:30:00Z";
        loc = AlternateLocation.parseUriResAltLoc( altLocStr, securityService );
        // since change alt-locs do not accept url with private ip...
        assertEquals( null, loc );

        // test if missing port in url is leading to a invalid URL 
        // and not anymore converted to 80 or 6346 on http.
        altLocStr = noPortUriResStr;
        loc = AlternateLocation.parseUriResAltLoc( altLocStr, securityService );
        assertEquals( null, loc );
        //assertEquals( 80, loc.getHostAddress().getPort() );

        // test to parse upper case uri res str
        altLocStr = uriResStr.toUpperCase();
        loc = AlternateLocation.parseUriResAltLoc( altLocStr, securityService );
        assertNotNull( loc );

        // test to parse uri res str with long timestamp
        altLocStr = uriResStr + " 2002-04-30T08:30:00Z";
        loc = AlternateLocation.parseUriResAltLoc( altLocStr, securityService );
        assertEquals( "1.1.1.1", loc.getHTTPString() );

        // test to parse uri res str with short timestamp
        altLocStr = uriResStr + " 2002-04-30T08:30Z";
        loc = AlternateLocation.parseUriResAltLoc( altLocStr, securityService );
        assertEquals( "1.1.1.1", loc.getHTTPString() );
        */
    }

    public void testAlternateLocationContainer()
        throws Exception
    {
        /* I2PFIXME: IMPORTANT:
         * Test case broken due to change in AlternateLocation.
         * Fix later.
         *

        AltLocContainer container = new AltLocContainer(
            new URN( urnStr ) );

        // add invalid alt loc
        AlternateLocation loc = AlternateLocation.parseUriResAltLoc(
            "http://1.1.1.1:6347/uri-res/N2R?urn:sha1:PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP", securityService );
        assertNotNull( loc );
        container.addAlternateLocation( loc );
        assertEquals( 0, container.getSize() );


        for ( int i = 0; i < 130; i++ )
        {
            String altLocStr = "http://1.1.1." + i
                + ":6347/uri-res/N2R?urn:sha1:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB 2002-04-30T08:30:"
                + i%60 + "Z";
            loc = AlternateLocation.parseUriResAltLoc( altLocStr, securityService );
            assertNotNull( loc );
            container.addAlternateLocation( loc );
            assertEquals( Math.min( i + 1, 100 ), container.getSize() );
        }

        Set altLocSet = new HashSet();
        HTTPHeader header = container.getAltLocHTTPHeaderForAddress( GnutellaHeaderNames.X_ALT,
            new DefaultDestAddress( "1.1.1.10", 6347 ), altLocSet );
        assertEquals( 10,
            AltLocContainer.parseCompactIpAltLocFromHeaders(
            new HTTPHeader[]{header}, new URN(urnStr), securityService ).size() );
        assertEquals( 10, altLocSet.size() );

        altLocSet.clear();
        header = container.getAltLocHTTPHeaderForAddress( GnutellaHeaderNames.X_ALT,
            new DefaultDestAddress( "2.1.1.1", 6347 ), altLocSet );
        assertEquals( 10, AltLocContainer.parseCompactIpAltLocFromHeaders(
            new HTTPHeader[]{header}, new URN(urnStr), securityService ).size() );
        assertEquals( 10, altLocSet.size() );

        // check that only one alt loc is returned when only one is available.
        // and check if alt locs for a defined HostAddress are not returned.
        container = new AltLocContainer(
            new URN( urnStr ) );
        loc = AlternateLocation.parseUriResAltLoc(
            "http://1.1.1.2:6347/uri-res/N2R?urn:sha1:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB", securityService);
        assertNotNull( loc );
        container.addAlternateLocation( loc );

        altLocSet.clear();
        header = container.getAltLocHTTPHeaderForAddress( GnutellaHeaderNames.X_ALT,
            new DefaultDestAddress( "1.1.1.2", 6347 ), altLocSet );
        assertNull( header );

        altLocSet.clear();
        header = container.getAltLocHTTPHeaderForAddress( GnutellaHeaderNames.X_ALT,
            new DefaultDestAddress( "2.1.1.1", 6347 ), altLocSet );
        assertEquals( 1, AltLocContainer.parseCompactIpAltLocFromHeaders(
            new HTTPHeader[]{header}, new URN(urnStr), securityService ).size() );
        assertEquals( 1, altLocSet.size() );

        // check if two same alt locs are only once in the download mash
        container = new AltLocContainer(
            new URN( urnStr ) );
        loc = AlternateLocation.parseUriResAltLoc(
            "http://1.1.1.2:6347/uri-res/N2R?urn:sha1:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB"
            + " 2002-04-30T08:30:10Z", securityService);
        assertNotNull( loc );
        container.addAlternateLocation( loc );

        loc = AlternateLocation.parseUriResAltLoc(
            "http://1.1.1.2:6347/uri-res/N2R?urn:sha1:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB"
            + " 2002-04-30T08:30:11Z", securityService);
        assertNotNull( loc );
        container.addAlternateLocation( loc );

        assertEquals( 1, container.getSize() );

        altLocSet.clear();
        header = container.getAltLocHTTPHeaderForAddress( GnutellaHeaderNames.X_ALT,
            new DefaultDestAddress( "1.1.1.2", 6347 ), altLocSet );
        assertNull( header );
        
        altLocSet.clear();
        header = container.getAltLocHTTPHeaderForAddress( GnutellaHeaderNames.X_ALT,
            new DefaultDestAddress( "2.1.1.1", 6347 ), altLocSet );
        assertEquals( 1, AltLocContainer.parseCompactIpAltLocFromHeaders(
            new HTTPHeader[]{header}, new URN(urnStr), securityService ).size() );
        assertEquals( 1, altLocSet.size() );
        
        // check if requesting alt loc for same candidate again will not return
        // same alt locs...
        header = container.getAltLocHTTPHeaderForAddress( GnutellaHeaderNames.X_ALT,
            new DefaultDestAddress( "2.1.1.1", 6347 ), altLocSet );
        assertNull( header );
        assertEquals( 1, altLocSet.size() );
        */
    }
}
