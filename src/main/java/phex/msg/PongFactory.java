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
 *  $Id: PongFactory.java 4365 2009-01-16 11:21:31Z gregork $
 */
package phex.msg;

import java.util.Collection;

import phex.common.address.DefaultDestAddress;
import phex.common.address.DestAddress;
import phex.common.address.IpAddress;
import phex.common.log.NLogger;
import phex.host.CaughtHost;
import phex.host.CaughtHostsContainer;
import phex.host.Host;
import phex.host.NetworkHostsContainer;
import phex.security.PhexSecurityManager;
import phex.servent.Servent;
import phex.udp.hostcache.UdpHostCacheContainer;
import phex.utils.IOUtil;
import phex.utils.VersionUtils;

public class PongFactory
{
    /**
     * Vendor code GGEP extension in GUESS format
     */
    private static final byte[] GGEP_VENDOR_CODE = new byte[5];
    static
    {
        // add vendor code 'PHEX'
        GGEP_VENDOR_CODE[ 0 ] = (byte) 0x50;
        GGEP_VENDOR_CODE[ 1 ] = (byte) 0x48;
        GGEP_VENDOR_CODE[ 2 ] = (byte) 0x45;
        GGEP_VENDOR_CODE[ 3 ] = (byte) 0x58;
        GGEP_VENDOR_CODE[4] = IOUtil.serializeGUESSVersionFormat(
            VersionUtils.getMajorVersionNumber(),
            VersionUtils.getMinorVersionNumber() );
    }
    
    public PongFactory( )
    {
    }
    
    /**
     * creates a udp pong message from a given byte array
     * @throws InvalidMessageException
     * @author Madhu
     *
     */
    public PongMsg createUdpPongMsg( byte[] bytesMsg, Host fromHost,
        PhexSecurityManager securityService ) 
        throws InvalidMessageException
    {
        // I2P:
        // Unsupported functionality, fail.
        if (true) return null;
        
        MsgHeader msgHdr = MsgHeader.createMsgHeader( bytesMsg, 0 );
        return createUdpPongMsg( msgHdr, bytesMsg, MsgHeader.DATA_LENGTH, fromHost,
            securityService );
    }
    
    public PongMsg createUdpPongMsg( MsgHeader msgHdr, byte[] data, int offset, Host fromHost,
        PhexSecurityManager securityService ) 
        throws InvalidMessageException 
    {
        // I2P:
        // Unsupported functionality, fail.
        if (true) return null;
        
        msgHdr.setFromHost( fromHost );
        
        if( msgHdr.getDataLength() < PongMsg.MIN_PONG_DATA_LEN )
        {
             throw new InvalidMessageException( " Could not create Msg Body while trying to" +
                    " create udp pong Msg"
                    );
        }
        
        byte[] body = MessageProcessor.createBody( msgHdr, data, offset );
        if ( body == null )
        {
            throw new InvalidMessageException( " Could not create Msg Body while trying to" +
                    " create udp pong Msg"
                    );
        }
        
        return new PongMsg( msgHdr, body, securityService );  
    }
    
    
    /**
     * Create a pong response message for a given ping
     * @param ping
     * @param sharedFileCount the number of files shared
     * @param sharedFileSize the total size of the shared files in KB
     * @return the new PongMsg
     */
    public PongMsg createUdpPongMsg( PingMsg ping, DestAddress localAddress,
        boolean isUdpHostCache, int avgDailyUptime, int sharedFileCount,
        int sharedFileSize, boolean isUltrapeer, 
        CaughtHostsContainer hostContainer, UdpHostCacheContainer uhcContainer )
    {
        // I2P:
        // Unsupported functionality, fail.
        return null;
        /*
        GGEPBlock ggepBlock = createMyGGEPBlock(avgDailyUptime, isUltrapeer);
        
        byte[] scpByte = ping.getScpByte();
        if ( scpByte != null )
        {
            Collection<CaughtHost> ipPortPairs = null;
            if ( scpByte.length > 0 && (scpByte[0] & PingMsg.UDP_SCP_MASK) == PingMsg.UDP_SCP_ULTRAPEER )
            {
                ipPortPairs = hostContainer.getFreeUltrapeerSlotHosts();
            }
            else
            {
                ipPortPairs = hostContainer.getFreeLeafSlotHosts();
            }
            addUdpPongGGEPExt( localAddress, isUdpHostCache, ipPortPairs, 
                uhcContainer, ggepBlock );
        }
        
        
        IpAddress ipAddress = localAddress.getIpAddress();
        if( ipAddress == null )
        {
            throw new IllegalArgumentException( "Can't accept null ip." );
        }
        
        // Construct pingResponse msg.  Copy the original ping's GUID.
        MsgHeader newHeader = new MsgHeader( ping.getHeader().getMsgID(),
                MsgHeader.PONG_PAYLOAD, (byte)1, (byte)0, 0 );
        PongMsg udpPong = new PongMsg( newHeader, localAddress, 
            sharedFileCount, sharedFileSize, isUltrapeer, ggepBlock );
        NLogger.info( PongMsg.class, "Created udp pong " +
            " in response to ping: " + udpPong );
        return udpPong;
        */
    }
    
    
    /**
     * @param sharedFileCount the number of files shared
     * @param sharedFileSize the total size of the shared files in KB
     * @return the new PongMsg
     */
    public PongMsg createMyOutgoingPong( GUID msgId, DestAddress localAddress,
        byte ttl, int sharedFileCount, int sharedFileSize, boolean isUltrapeer, 
        int avgDailyUptime )
    {
        GGEPBlock ggepBlock = createMyGGEPBlock( avgDailyUptime, isUltrapeer );
        
        // I2PMOD:
        // Cannot implement for I2P, commented out.
        /*
        IpAddress localIp = localAddress.getIpAddress();
        IpAddress pongIp;
        if ( localIp == null )
        {
            pongIp = IpAddress.UNSET_IP;
            // in case we have a unset ip address we need to use the Phex.EXTDEST
            // GGEP extension to specify our pong destination.
            addPhexExtendedDestinationGGEP( localAddress, ggepBlock );
        }
        else
        {
            pongIp = new IpAddress( localIp.getHostIP() );
        }
        */
        MsgHeader header = new MsgHeader( msgId, MsgHeader.PONG_PAYLOAD,
            ttl, (byte)0, 0 );

        // I2P: FIXME:
        // Cannot create pongAddress from pongIP, but localAddress suffices for us.
        DestAddress pongAddress = localAddress;
        PongMsg pong = new PongMsg( header, pongAddress, sharedFileCount, 
            sharedFileSize, isUltrapeer, ggepBlock );
        return pong;
    }
    
    public PongMsg createOtherLeafsOutgoingPong( GUID msgId, byte ttl, 
        byte hops, DestAddress address )
    {
        MsgHeader header = new MsgHeader( msgId, MsgHeader.PONG_PAYLOAD,
            ttl, hops, 0 );
        GGEPBlock ggepBlock = null;
        
        // I2PMOD:
        // Cannot implement for I2P, commented out.
        /*
        IpAddress ip = address.getIpAddress();
        IpAddress pongIp;
        if ( ip == null )
        {
            pongIp = IpAddress.UNSET_IP;
            // in case we have a unset ip address we need to use the Phex.EXTDEST
            // GGEP extension to specify our pong destination.
            ggepBlock = new GGEPBlock( false );
            addPhexExtendedDestinationGGEP( address, ggepBlock );
        }
        else
        {
            pongIp = new IpAddress( ip.getHostIP() );
        }
        */
        PongMsg pong = new PongMsg( header, address, 0, 0, false, ggepBlock );
        return pong;
    }
    
    public PongMsg createFromCachePong( GUID newGuid, byte newTTL, PongMsg pongMsg, 
        PhexSecurityManager securityService )
    {
        MsgHeader header = new MsgHeader( newGuid, MsgHeader.PONG_PAYLOAD,
            newTTL, (byte)0, 0 );
        PongMsg pong = new PongMsg( header, pongMsg.getBody(), securityService );
        return pong;
    }
    
    private GGEPBlock createMyGGEPBlock( int avgDailyUptime,
        boolean isUltrapeer )
    {
        GGEPBlock ggepBlock = new GGEPBlock( false );
        
        // I2P:
        // Let's fake something here (for example 6 hours).
        // Avoids both anonymity loss and unforeseen issues.

        // add daily avg. uptime.
        if ( avgDailyUptime > 0 )
        {
            ggepBlock.addExtension( GGEPBlock.AVARAGE_DAILY_UPTIME, 6 );
        }
        
        // add UP GGEP extension.
        if ( isUltrapeer )
        {
            byte[] upExtension = new byte[3];
            upExtension[0] = IOUtil.serializeGUESSVersionFormat(
                VersionUtils.getUltrapeerMajorVersionNumber(),
                VersionUtils.getUltrapeerMinorVersionNumber() ); 
                
            NetworkHostsContainer networkHostsContainer = 
                Servent.getInstance().getHostService().getNetworkHostsContainer();
            upExtension[1] = (byte) networkHostsContainer.getOpenLeafSlotsCount();
            upExtension[2] = (byte) networkHostsContainer.getOpenUltrapeerSlotsCount();
            
            ggepBlock.addExtension( GGEPBlock.ULTRAPEER_ID, upExtension );
        }
        
        // add vendor info
        ggepBlock.addExtension( GGEPBlock.VENDOR_CODE_ID, GGEP_VENDOR_CODE );
        return ggepBlock;
    }
    
    // I2PMOD:
    // Implements currently unsupported functionality, fix later.
    /*
    private static void addUdpPongGGEPExt( DestAddress localAddress, 
        boolean isUdpHostCache, Collection<CaughtHost> ipPortPairs,
        UdpHostCacheContainer uhcContainer, GGEPBlock ggepBlock )
    {
        // add ip port info if asked for
        if( ipPortPairs != null )
        {
            byte[] ipPortData = packIpPortData( ipPortPairs );
            if( ipPortData.length >= 6 )
            {
                ggepBlock.addExtension( GGEPBlock.UDP_HOST_CACHE_IPP, ipPortData );
            }
        }
        
        // if this host is a udp host cache
        if( isUdpHostCache )
        {
            byte[] data;
            
            // check if we have dns name
            if( localAddress.isIpHostName() )
            {
                data = new byte[0];
            }
            else
            {
                data = localAddress.getHostName().getBytes();
            }
            // now add the ggep extension udphc
            ggepBlock.addExtension( GGEPBlock.UDP_HOST_CACHE_UDPHC, data );
            NLogger.debug( PongMsg.class, "UDP HOST CACHE extension added to outgoing pongs");
        }
        
        // providing a uhcContainer is optional
        if ( uhcContainer != null )
        {
            // if we want Packed Host Caches the data should be added in compressed form
            String packedCacheString = uhcContainer.createPackedHostCaches();
            if( packedCacheString.length() > 0 )
            {
                byte[] data = IOUtil.deflate( packedCacheString.getBytes() );
                ggepBlock.addExtension( GGEPBlock.UDP_HOST_CACHE_PHC, data );
                NLogger.debug( PongMsg.class, " PACKED HOST CACHE extension added to outgoing pongs ");
            }
        }
    }
    */
    
    private static void addPhexExtendedDestinationGGEP( DestAddress address,
        GGEPBlock ggepBlock )
    {
        // TODO1 this is totally experimental and needs to be optimized
        // to use correct byte encoding! It can be used to transfer an destination
        // address info in case there is no IP address used for communication.
        // Like the case in I2P
        ggepBlock.addExtension( GGEPBlock.PHEX_EXTENDED_DESTINATION, 
            address.getHostName().getBytes() );
    }
    
    /**
     * packs ip port data into a data array
     * @param ipPortCollection
     * @return ip port byte array 
     */
    
    // I2P: FIXME:
    // Find a better place for this, as duplicate constants are evil.
    private static int I2P_DEST_LENGTH = 516;
    
    private static byte[] packIpPortData( Collection<CaughtHost> caughtHostCollection )
    {
        NLogger.info( PongMsg.class, "Packing Pong message IPP extension." );
        final int FIELD_SIZE = I2P_DEST_LENGTH;
        byte[] data = new byte[caughtHostCollection.size() * FIELD_SIZE];
        int offset = 0;
        
        for( CaughtHost host : caughtHostCollection ) 
        {
            DestAddress address = host.getHostAddress();
            String destBase64 = address.getFullHostName();
            System.arraycopy(destBase64.getBytes(), 0, data, offset, I2P_DEST_LENGTH);
            offset += I2P_DEST_LENGTH;
        }
        return data;    
    }
    
}
