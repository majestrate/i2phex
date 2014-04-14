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
 *  --- CVS Information ---
 *  $Id: TestCatchedHostCache.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.test;

import junit.framework.TestCase;
import phex.common.address.DefaultDestAddress;
import phex.host.CatchedHostCache;
import phex.host.CaughtHost;

/**
 * 
 */
public class TestCatchedHostCache extends TestCase
{
    private CatchedHostCache cache;
    
    public TestCatchedHostCache(String s)
    {
        super(s);
    }
    
    protected void setUp()
    {
        cache = new CatchedHostCache( );        
    }
    
    public void testPutAndGet() 
        throws Exception
    {
        CaughtHost host1 = new CaughtHost( new DefaultDestAddress( "1.1.1.1",1234 ) );
        CaughtHost host2 = new CaughtHost( new DefaultDestAddress( "1.1.1.2",1234 ) );
        CaughtHost host3 = new CaughtHost( new DefaultDestAddress( "1.1.1.3",1234 ) );
        
        cache.add( host1 );
        cache.add( host2 );
        cache.add( host3 );
        
        Object tmp = cache.getCaughHost( host1.getHostAddress() );
        assertEquals( host1, tmp );
        
        tmp = cache.getCaughHost( host2.getHostAddress() );
        assertEquals( host2, tmp );
        
        tmp = cache.getCaughHost( host3.getHostAddress() );
        assertEquals( host3, tmp );
    }    
}
