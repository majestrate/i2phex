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
 *  $Id: IpSecurityRuleComparator.java 3807 2007-05-19 17:06:46Z gregork $
 */
package phex.security;

import phex.utils.*;
import java.util.*;

public class IpSecurityRuleComparator implements Comparator<IpSecurityRule>
{
    public static final IpSecurityRuleComparator INSTANCE = new IpSecurityRuleComparator();
    
    private IpSecurityRuleComparator() {}
    
    public int compare( IpSecurityRule rule1, IpSecurityRule rule2 )
    {
        if ( rule1 == rule2 || rule1.equals(rule2) )
        {
            return 0;
        }
        long ip1l = IOUtil.unsignedInt2Long( rule1.getIp() );
        long ip2l = IOUtil.unsignedInt2Long( rule2.getIp() );

        if ( ip1l < ip2l )
        {
            return -1;
        }
        else
        {
            return 1;
        }
    }
}
