/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2008 Phex Development Group
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
 *  $Id$
 */
package phex.msg;

import junit.framework.TestCase;
import phex.common.URN;
import phex.common.address.DefaultDestAddress;
import phex.common.bandwidth.BandwidthController;
import phex.io.buffer.ByteBuffer;
import phex.net.connection.Connection;
import phex.prefs.core.LibraryPrefs;
import phex.prefs.core.NetworkPrefs;
import phex.query.QHDConstants;
import phex.security.PhexSecurityManager;
import phex.servent.Servent;
import phex.upload.UploadManager;
import phex.utils.DummySocketFacade;


public class QueryResponseMsgTest extends TestCase
{
    private PhexSecurityManager securityService;

    public QueryResponseMsgTest(String s)
    {
        super(s);
    }

    protected void setUp()
    {
        securityService = new PhexSecurityManager();
    }

    protected void tearDown()
    {
    }

    public void testCreateAndParse()
        throws Exception
    {
        MsgHeader header = new MsgHeader( new GUID(), MsgHeader.QUERY_HIT_PAYLOAD,
            (byte)0x7, (byte)0x0, 0 );
        QueryResponseRecord rec = new QueryResponseRecord( 1, new URN(
            "urn:sha1:LO4DP3SD3I3CZZP6PIKG3VCQHG4KTQD2" ), 1, "file", -1, null );
        QueryResponseRecord[] recArr =
        {
            rec
        };

        QueryResponseMsg respIn = new QueryResponseMsg( header, new GUID(),
            new DefaultDestAddress( "111.111.111.111", 1111 ), 0, recArr, null,
            true, false );
        
        ByteBuffer headerBuf = respIn.createHeaderBuffer();
        ByteBuffer messageBuf = respIn.createMessageBuffer();
        int size = headerBuf.remaining() + messageBuf.remaining();
        ByteBuffer combi = ByteBuffer.allocate( size );
        combi.put( headerBuf ).put( messageBuf );
        byte[] output = combi.array();
        
        //System.out.println( new String(output) + "\n" + HexConverter.toHexString( output ) );

        DummySocketFacade socketFac = new DummySocketFacade( output );
        Connection connection = new Connection( socketFac, 
            new BandwidthController( "JUnitText", Long.MAX_VALUE ) );

        QueryResponseMsg respOut = (QueryResponseMsg)MessageProcessor.parseMessage( connection,
            securityService );

        MsgHeader outHeader = respOut.getHeader();
        
        assertEquals( header.getDataLength(), outHeader.getDataLength() );
        assertEquals( header.getHopsTaken(), outHeader.getHopsTaken() );
        assertEquals( header.getMsgID().toHexString(), outHeader.getMsgID().toHexString() );
        assertEquals( header.getPayload(), outHeader.getPayload() );
        assertEquals( header.getTTL(), outHeader.getTTL() );
        assertEquals( respIn.getDestAddress(), respOut.getDestAddress() );
        assertEquals( respIn.getRecordCount(), respOut.getRecordCount() );
        assertEquals( respIn.getRemoteServentID(), respOut.getRemoteServentID() );
        assertEquals( respIn.getRemoteHostSpeed(), respOut.getRemoteHostSpeed() );
        assertEquals( respIn.getUploadSpeedFlag(), respOut.getUploadSpeedFlag() );
        assertEquals( respIn.getPushNeededFlag(), respOut.getPushNeededFlag() );
        assertEquals( LibraryPrefs.AllowBrowsing.get().booleanValue(), respOut.isBrowseHostSupported() );
        assertEquals( QHDConstants.QHD_UNKNOWN_FLAG, respOut.getHasUploadedFlag() );
        assertEquals( "PHEX", respOut.getVendorCode() );
        assertEquals( true, respOut.isChatSupported() );
        assertEquals( NetworkPrefs.AllowChatConnection.get().booleanValue(), respOut.isChatSupported() );

        assertEquals( Servent.getInstance().isUploadLimitReached() ?
            QHDConstants.QHD_TRUE_FLAG : QHDConstants.QHD_FALSE_FLAG,
            respOut.getServerBusyFlag() );
    }
}