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
 *  $Id: IpSecurityRule.java 3817 2007-06-13 08:37:42Z gregork $
 */
package phex.security;

public interface IpSecurityRule extends SecurityRule
{
    /**
     * Returns the int value of the ip of this rule.
     * @return the ip.
     */
    int getIp();
    
    HittingIpCidrPair getIpCidrPair();
    
    /**
     * Returns a string representation of the address
     * for displaying purposes on a GUI.
     * @return a string representation of the address.
     */
    String getAddressString();
}
