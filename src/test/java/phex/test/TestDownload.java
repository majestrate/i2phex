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
package phex.test;

import junit.framework.TestCase;
import phex.common.AddressCounter;
import phex.common.address.DefaultDestAddress;
import phex.download.RemoteFile;
import phex.download.swarming.*;
import phex.msg.GUID;
import phex.query.QueryHitHost;
import phex.servent.Servent;
import phex.utils.AccessUtils;

public class TestDownload extends TestCase
{

    public TestDownload(String s)
    {
        super(s);
    }

    protected void setUp()
    {
    }

    protected void tearDown()
    {
    }

    public void testEmptyURN()
        throws Throwable
    {
        RemoteFile remoteFile = new RemoteFile( new QueryHitHost( new GUID(),
            new DefaultDestAddress( "127.0.0.1", 6346 ), 1 ), 1, "acw21.exe", "", 1, null, "",
            (short)1 );
        SWDownloadFile downloadFile = new SWDownloadFile( "test",
            "test", remoteFile.getFileSize(), remoteFile.getURN(), 
            Servent.getInstance().getDownloadService(), 
            Servent.getInstance().getEventService() );
        downloadFile.addDownloadCandidate( remoteFile );
        downloadFile.setStatus( SWDownloadConstants.STATUS_FILE_WAITING );

        SWDownloadWorker worker = new SWDownloadWorker( Servent.getInstance().getDownloadService() );

        AddressCounter counter = new AddressCounter( Integer.MAX_VALUE, false );
        SWDownloadCandidate candidate = downloadFile.allocateDownloadCandidate( worker, counter );
        assertNotNull( candidate );
        // init avail range set
        candidate.getAvailableScopeList();
        SWDownloadSet set = new SWDownloadSet( Servent.getInstance(), downloadFile, candidate );
        
        AccessUtils.invokeMethod( worker, "handleDownload", set );
    }
}