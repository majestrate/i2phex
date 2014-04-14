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
 *  $Id: UdpHostCacheComparator.java 3682 2007-01-09 15:32:14Z gregork $
 */
package phex.udp.hostcache;

import java.util.Comparator;

/**
 * Implements the comparator interface
 * can be used to sort the udp host caches
 * @author Madhu
 *
 */
public class UdpHostCacheComparator implements Comparator<UdpHostCache>
{
    public int compare( UdpHostCache cacheA, UdpHostCache cacheB )
    {        
        // if host addresses are same return 0
        if( cacheA.equals( cacheB ) )
        {
            return 0;
        }
        // now cache addresses are different
        
        if( cacheA.getFailCount() < cacheB.getFailCount() )
        {
            return -1;
        }
        if ( cacheA.getFailCount() > cacheB.getFailCount() )
        {
            return 1;
        }
        
        // now fail count is equal
        return -1;
    }
}
