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
 *  $Id: TestSWDownloadCandidate.java 4362 2009-01-16 10:27:18Z gregork $
 */
package phex.test;

import junit.framework.TestCase;
import phex.common.address.DefaultDestAddress;
import phex.common.log.LogBuffer;
import phex.download.RemoteFile;
import phex.download.swarming.*;
import phex.msg.GUID;
import phex.query.QueryHitHost;
import phex.servent.Servent;

/**
 * 
 */
public class TestSWDownloadCandidate extends TestCase
{
    protected void setUp()
    {
    }

    protected void tearDown()
    {
    }
    
    public void testSetVendor() throws Exception
    {
        RemoteFile remoteFile = new RemoteFile(
            new QueryHitHost(new GUID(), new DefaultDestAddress("1.1.1.1", 1111 ), 0),
            1, "", "",  1, null, "", (short)1);
        SWDownloadFile downloadFile = new SWDownloadFile(
            "", "", 1, null, Servent.getInstance().getDownloadService(), 
            Servent.getInstance().getEventService() );
        SWDownloadCandidate candidate = new SWDownloadCandidate( remoteFile,
            downloadFile, null );
            
        candidate.setVendor( "Phex 0.9.0.44" );
        assertEquals( "Phex 0.9.0.44", candidate.getVendor() );
        candidate.setVendor( "!§$%&/()=?,.-+#" );
        assertEquals( "!§$%&/()=?,.-+#", candidate.getVendor() );
        candidate.setVendor( "" );
        candidate.setVendor( new String( new byte[]{0x00,(byte)0xfffe,(byte)0xdf12}, "UTF-8" ) );
        assertEquals( "", candidate.getVendor() );
        
    }

}
