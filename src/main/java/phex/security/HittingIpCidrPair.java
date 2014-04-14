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
 *  $Id: HittingIpCidrPair.java 4064 2007-11-29 22:57:54Z complication $
 */
package phex.security;

public class HittingIpCidrPair extends IpCidrPair
{
    private volatile int hits = 0;
    
    public HittingIpCidrPair( int ip, byte cidr )
    {
        super( ip, cidr );
    }
    
    public int getHits()
    {
        return hits;
    }
    
    public void setHits( int hits )
    {
        this.hits = hits;
    }
    
    public void countHit()
    {
        hits ++;
    }
}
