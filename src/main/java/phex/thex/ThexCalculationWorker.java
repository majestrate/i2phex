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
 *  Created on 31.05.2005
 *  --- CVS Information ---
 *  $Id: ThexCalculationWorker.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.thex;

import java.io.IOException;

import phex.common.log.NLogger;
import phex.share.ShareFile;


public class ThexCalculationWorker implements Runnable
{
    private ShareFile shareFile;

    public ThexCalculationWorker(ShareFile shareFile)
    {
        this.shareFile = shareFile;
    }

    public void run()
    {
        try
        {
            TTHashCalcUtils.calculateShareFileThexData( shareFile );
        }
        catch ( IOException exp )
        {
            NLogger.error( ThexCalculationWorker.class, exp, exp );
        }
    }
}