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
package phex.test;

import junit.framework.TestCase;
import phex.download.DownloadScope;
import phex.http.HTTPRangeSet;

public class TestHTTPRangeSet extends TestCase
{
    public TestHTTPRangeSet(String s)
    {
        super( s );
    }

    public void testXAvailableRanges()
    {
        HTTPRangeSet result;
        
        
        // Shareaza
        // X-Available-Ranges: bytes 9728000-87551999,107008000-116735999
        result = HTTPRangeSet.parseHTTPRangeSet( "bytes 900-8000,10000-116735999", false );
        assertNotNull( result );
        assertEquals( 2, result.size() );
        assertEquals( 900, result.getFirstRange().getStartOffset( 20000 ) );
        assertEquals( 8000, result.getFirstRange().getEndOffset( 20000 ) );
        assertEquals( 799, result.getFirstRange().getStartOffset( 800 ) );
        assertEquals( 799, result.getFirstRange().getEndOffset( 800 ) );
        assertEquals( 900, result.getFirstRange().getStartOffset( 2000 ) );
        assertEquals( 1999, result.getFirstRange().getEndOffset( 2000 ) );

        
        // difference X-Available-Ranges header values are used, in 
        // case no ranges are available.
        
        
        // Freebase: (actually freebase reports a wrong empty available range, 
        // it wants to say nothing is available but indicates the first byte is available.)
        // This is not fixed on Phex side...
        // X-Available-Ranges: bytes 0-0
        result = HTTPRangeSet.parseHTTPRangeSet( "bytes 0-0", false );
        assertNotNull( result );
        assertEquals( 1, result.size() );
        assertEquals( 0, result.getFirstRange().getStartOffset( 100 ) );
        assertEquals( 0, result.getFirstRange().getEndOffset( 100 ) );

        // Limewire:
        // X-Available-Ranges: bytes
        result = HTTPRangeSet.parseHTTPRangeSet( "bytes", false );
        assertNotNull( result );
        assertEquals( result.size(), 0 );
        
        // Bearshare: (this is not accepted by phex and ignored).
        // X-Available-Ranges: 
        result = HTTPRangeSet.parseHTTPRangeSet( "", false );
        assertNull( result );
    }
}
