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
 *  $Id: TestHTTPProcessor.java 4168 2008-04-15 20:44:36Z complication $
 */
package phex.test;

import junit.framework.TestCase;
import phex.common.bandwidth.BandwidthController;
import phex.http.HTTPProcessor;
import phex.http.HTTPRequest;
import phex.net.connection.Connection;
import phex.utils.DummySocketFacade;

public class TestHTTPProcessor extends TestCase
{
    private String fullHTTPRequest;

    public TestHTTPProcessor(String s)
    {
        super(s);
    }

    protected void setUp()
    {
        fullHTTPRequest = "GET / HTTP/1.1\r\n" +
            "Header1: header1 value\r\n" +
            "Header2: header2\r\n" +
            " value\r\n" +
            "Header3: header3\r\n" +
            "\t  \tval\r\n" +
            " ue\r\n" +
            "Header4: header4\r\n" +
            "\t  \tvalue\r\n" +
            "Header5: header5\r\n" +
            "\r\nDATA...DATA...DATA";
    }

    protected void tearDown()
    {
    }

    public void testParseHTTPRequest()
    {        
        DummySocketFacade socketFac = new DummySocketFacade( fullHTTPRequest.getBytes() );
        Connection connection = new Connection( socketFac, 
            new BandwidthController( "JUnitText", Long.MAX_VALUE ) );
        try
        {
            HTTPRequest httpRequestRet = HTTPProcessor.parseHTTPRequest( connection );
            assertEquals( "GET", httpRequestRet.getRequestMethod() );
            assertEquals( "/", httpRequestRet.getRequestURI() );
            assertEquals( "HTTP/1.1", httpRequestRet.getHTTPVersion() );
            assertEquals( "header1 value", httpRequestRet.getHeader("Header1").getValue() );
            assertEquals( "header2 value", httpRequestRet.getHeader("Header2").getValue() );
            assertEquals( "header3 val ue", httpRequestRet.getHeader("Header3").getValue() );
            assertEquals( "header4 value", httpRequestRet.getHeader("Header4").getValue() );
            assertEquals( "header5", httpRequestRet.getHeader("Header5").getValue() );
        }
        catch(Exception e)
        {
            System.err.println("Exception thrown:  "+e);
            fail( e.getMessage() );
        }
    }
}
