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
 *  $Id: DestAddressComparator.java 3536 2006-08-05 22:16:44Z gregork $
 */
package phex.gui.comparator;

import java.util.Comparator;

import phex.common.address.DestAddress;
import phex.common.address.IpAddress;

/**
 * 
 */
public class DestAddressComparator implements Comparator<DestAddress>
{
    public int compare(DestAddress ha1, DestAddress ha2)
    {
        IpAddress ip1 = ha1.getIpAddress();
        IpAddress ip2 = ha2.getIpAddress();
        if ( ip1 != null && ip2 != null )
        {
            long ip1l = ip1.getLongHostIP();
            long ip2l = ip2.getLongHostIP();
    
            if ( ip1l < ip2l
               || ( ip1l == ip2l && ha1.getPort() < ha2.getPort() ) )
            {
                return -1;
            }
            else
            {
                return 1;
            }
        }
        else
        {
            return ha1.getHostName().compareTo(ha2.getHostName());
        }
    }

}
