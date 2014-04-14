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
 *  $Id: GWebCacheComparator.java 3841 2007-06-25 16:45:31Z gregork $
 */
package phex.gwebcache;

import java.util.Comparator;

public class GWebCacheComparator implements Comparator<GWebCache>
{
    public static final GWebCacheComparator INSTANCE = new GWebCacheComparator();
    
    private GWebCacheComparator()
    {
    }
    
    public int compare( GWebCache cache1, GWebCache cache2 )
    {
        if ( cache1.equals(cache2) )
        {
            return 0;
        }
        long diff = cache1.getEarliestReConnectTime() - cache2.getEarliestReConnectTime();
        if ( diff == 0)
        {
            return cache1.hashCode() - cache2.hashCode();
        }
        else if ( diff > 0 )
        {
            return 1;
        }
        else
        {
            return -1;
        }
    }

}
