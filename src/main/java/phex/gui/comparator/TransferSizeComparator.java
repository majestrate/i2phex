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
 */
package phex.gui.comparator;

import java.util.*;
import phex.common.*;

public class TransferSizeComparator implements Comparator<TransferDataProvider>
{
    public int compare( TransferDataProvider p1, TransferDataProvider p2 )
    {
        long p1tds = p1.getTransferredDataSize();
        long p2tds = p2.getTransferredDataSize();

        if ( p1tds < p2tds )
        {
            return -1;
        }
        // only if size and object is equal return 0
        else if ( p1tds == p2tds && p1 == p2 )
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }
}