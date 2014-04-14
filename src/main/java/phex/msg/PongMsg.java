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
 *  $Id: PongMsg.java 4365 2009-01-16 11:21:31Z gregork $
 */
package phex.msg;

import java.util.HashSet;
import java.util.Set;

import phex.common.address.DestAddress;
import phex.common.address.MalformedDestAddressException;
import phex.common.log.NLogger;
import phex.io.buffer.ByteBuffer;
import phex.net.repres.PresentationManager;
import phex.security.AccessType;
import phex.security.PhexSecurityManager;
import phex.udp.hostcache.UdpHostCache;
import phex.utils.HexConverter;
import phex.utils.IOUtil;

/**
 * <p>A pong response.</p>
 *
 * <p>This encapsulates a Gnutella message that informs this servent of the
 * vital statistics of a Gnutella host. Pongs should  only ever be received
 * in response to pings.</p>
 *
 * <p>This implementation handles GGEP extension blocks.</p>
 */
public class PongMsg extends Message
{
    protected static final int MIN_PONG_DATA_LEN = 14;
    
    /**
     * <p>The un-parsed body of the message.</p>
     */
    private byte[] body;

    /**
     * The address this pong is carrying.
     */
    private DestAddress pongAddress;

    private long fileCount;
    private long fileSizeInKB;
    
    private String vendor;
    private int vendorVersionMajor = -1;
    private int vendorVersionMinor = -1;
    private int avgDailyUptime;
    private boolean isUltrapeer;
    private GGEPBlock ggepBlock;
    
    /**
     * holds the information that can appear in udp pongs
     */
    private Set<DestAddress> ippDestAddresses;
    private Set<UdpHostCache> udpHostCaches;
    private UdpHostCache udpHostCache;

    public PongMsg( MsgHeader aHeader, byte[] payload, PhexSecurityManager securityService )
    {
        super( aHeader );
        getHeader().setPayloadType( MsgHeader.PONG_PAYLOAD );

        body = payload;
        getHeader().setDataLength( body.length );
        
        avgDailyUptime = -1;

        // parse the body
        parseBody( securityService );
    }

    /**
     * <p>Create a new MsgInitResponse.</p>
     *
     * <p>The header will be modified so that its function property becomes
     * MsgHeader.sInitResponse. The header argument is owned by this object.</p>
     *
     * @param header  the MsgHeader to associate with the new message
     */
    protected PongMsg( MsgHeader header, DestAddress pongAddress, int fileCount,
        int fileSizeInKB, boolean isUltrapeer, GGEPBlock ggepBlock )
    {
        super( header );
        getHeader().setPayloadType( MsgHeader.PONG_PAYLOAD );

        this.pongAddress = pongAddress;

        this.fileCount = fileCount;
        this.isUltrapeer = isUltrapeer;
        this.ggepBlock = ggepBlock;
                
        if ( isUltrapeer )
        {
            this.fileSizeInKB = createUltrapeerMarking( fileSizeInKB );
        }
        else
        {
            this.fileSizeInKB = fileSizeInKB;
        }
        buildBody();
        getHeader().setDataLength( body.length );
    }
    
    /**
     * Get the address this pong is carrying.
     * @return the address of this pong message.
     */
    public DestAddress getPongAddress()
    {
        return pongAddress;
    }
        
    /**
     * Get the number of files served from this servent.
     *
     * @return  a zero or positive integer giving the number of files served
     */
    public long getFileCount()
    {
        return fileCount;
    }

    /**
     * Get the number of bytes served by this servent.
     *
     * @return  the number of bytes served
     */
    public long getFileSizeInKB()
    {
        return fileSizeInKB;
    }
    
    /**
     * @return the vendor
     */
    public String getVendor()
    {
        return vendor;
    }

    /**
     * @return the vendorVersionMajor
     */
    public int getVendorVersionMajor()
    {
        return vendorVersionMajor;
    }

    /**
     * @return the vendorVersionMinor
     */
    public int getVendorVersionMinor()
    {
        return vendorVersionMinor;
    }

    /**
     * @return the avgDailyUptime
     */
    public int getDailyUptime()
    {
        return avgDailyUptime;
    }
    
    public boolean hasFreeLeafSlots()
    {
        if ( ggepBlock == null ||
            !ggepBlock.isExtensionAvailable( GGEPBlock.ULTRAPEER_ID ) )
        {
            return false;
        }
        byte[] data = ggepBlock.getExtensionData( GGEPBlock.ULTRAPEER_ID );
        if ( data != null )
        {
            if( data.length >= 3 )
            {
                if( data[1] > 0 )
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean hasFreeUPSlots()
    {
        if ( ggepBlock == null ||
            !ggepBlock.isExtensionAvailable( GGEPBlock.ULTRAPEER_ID ) )
        {
            return false;
        }
        byte[] data = ggepBlock.getExtensionData( GGEPBlock.ULTRAPEER_ID );
        if ( data != null )
        {
            if( data.length >= 3 )
            {
                if( data[2] > 0 )
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    public Set<DestAddress> getIPPDestAddresses()
    {
        return ippDestAddresses;
    }
    
    public Set<UdpHostCache> getUdpHostCaches()
    {
        return udpHostCaches;
    }
    
    public UdpHostCache getUdpHostCache()
    {
        return udpHostCache;
    }
    
    public byte[] getBody()
    {
        return body;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ByteBuffer createMessageBuffer()
    {
        return ByteBuffer.wrap( body );
    }
    
    public byte[] getbytes()
    {
        byte[] data = new byte[ MsgHeader.DATA_LENGTH + body.length ];
        byte[] hdr = getHeader().getBytes();
        System.arraycopy( hdr, 0, data, 0, MsgHeader.DATA_LENGTH );
        System.arraycopy( body, 0, data, MsgHeader.DATA_LENGTH , body.length );
        return data;
    }

    public String getDebugString()
    {
        return "Pong[ Addr=" + pongAddress +
            ", FileCount=" + fileCount +
            ", FileSize=" + fileSizeInKB +
            ", AvgUptime=" + avgDailyUptime +
            ", HEX=[" + HexConverter.toHexString( body ) +
            "] ]";
    }

    
    private static int I2P_DEST_LENGTH = 516;
    private static int NULL_TERM_LENGTH = 1;
    private static int PORT_LENGTH = 1;
    private static int IP_LENGTH = 4;
    private static int FILECOUNT_LENGTH = 4;
    private static int FILESIZE_LENGTH = 4;
    
    private void buildBody()
    {
        int extensionLength = 0;
        byte[] ggepExtension;
        if ( ggepBlock != null )
        {
            // we only build from the first GGEP block our body
            ggepExtension = ggepBlock.getBytes();
            extensionLength = ggepExtension.length;            
        }
        else
        {
            ggepExtension = null;
            extensionLength = 0;
        }
        
        // I2PMOD:
        // Try to violate Gnutella specs as little as possible.
        body = new byte[ I2P_DEST_LENGTH + NULL_TERM_LENGTH + FILECOUNT_LENGTH + FILESIZE_LENGTH + extensionLength];
        int offset = IOUtil.serializeString(pongAddress.getFullHostName(),body,0);
        body[offset++] = (byte)'\0';        

        offset = IOUtil.serializeIntLE( (int)fileCount, body, offset);
        offset = IOUtil.serializeIntLE( (int)fileSizeInKB, body, offset);
        
        if ( ggepExtension != null )
        {
            System.arraycopy( ggepExtension, 0, body, offset, extensionLength );
        }
    }

    private void parseBody( PhexSecurityManager securityService )
    {
        StringBuffer buffer = new StringBuffer();
        int offset = IOUtil.deserializeString( body, 0 ,buffer);
        
        try
        {
            pongAddress = PresentationManager.getInstance().createHostAddress(buffer.toString(), -1);
        }
        catch (MalformedDestAddressException e)
        {
            // I2P: FIXME: IMPORTANT:
            e.printStackTrace();
            return;
        }
                
        offset=offset+1; //skip the '\0' byte which is not handled in deserializeString
        
        fileCount = IOUtil.unsignedInt2Long( IOUtil.deserializeIntLE( body, offset ) );
        fileSizeInKB = IOUtil.unsignedInt2Long( IOUtil.deserializeIntLE( body, offset+4 ) );

        // parse possible GGEP data
        if ( body.length <= offset + 8 )
        {
            return;
        }
        
        parseGGEPBlocks( securityService );
    }
    
    private void parseGGEPBlocks( PhexSecurityManager securityService )
    {
        NLogger.warn( PongMsg.class, "Parsing Pong message GGEP blocks." );

        ggepBlock = GGEPBlock.mergeGGEPBlocks( GGEPBlock.parseGGEPBlocks( body, 14 ) );
        
        if ( ggepBlock.isExtensionAvailable( GGEPBlock.VENDOR_CODE_ID ) )
        {
            byte[] data = ggepBlock.getExtensionData( GGEPBlock.VENDOR_CODE_ID );
            if ( data.length >= 4 )
            {
                vendor = new String( data, 0, 4 ).intern();
                if ( data.length > 4 )
                {
                    vendorVersionMajor = data[4] >> 4;
                    vendorVersionMinor = data[4] & 0x0F;
                }
            }
        }

        if ( ggepBlock.isExtensionAvailable( GGEPBlock.AVARAGE_DAILY_UPTIME ) )
        {
            byte[] data = ggepBlock.getExtensionData( GGEPBlock.AVARAGE_DAILY_UPTIME );
            if ( data != null )
            {
                if ( data.length >= 1 && data.length <= 4)
                {
                    avgDailyUptime = IOUtil.deserializeIntLE( data, 0, data.length );
                    if ( avgDailyUptime <= 0 )
                    {
                        NLogger.warn( PongMsg.class, "Negative average uptime GGEP extension data: " +
                            avgDailyUptime + " - " + HexConverter.toHexString( data ) );
                        avgDailyUptime = -1;
                    }
                }
                else 
                {
                    NLogger.warn( PongMsg.class, "Invalid average uptime GGEP extension data: " +
                        HexConverter.toHexString( data ) );
                    avgDailyUptime = -1;
                }
            }
        }
        
        // checks for the UP ggep extension
        // if its present and the host has free slots its added to the respective free slots container 
        // in NetworkHostContainer ....as of now we ignore the actual count of the slots available
        
        // I2P:
        // Not sure if this extension ever gets sent currently.
        // But we do want destination addresses for potential peers.

        if ( ggepBlock.isExtensionAvailable( GGEPBlock.UDP_HOST_CACHE_IPP ) )
        {
            byte[] data = ggepBlock.getExtensionData( GGEPBlock.UDP_HOST_CACHE_IPP );
            if ( data != null )
            {        
                ippDestAddresses = unpackIpPortData( data, securityService );
            }
        }
        
        // try to get any packed udp host cache list which may be present in udp pongs
        
        // I2P:
        // Seems to contain UDP host cache contact data.
        // Since we don't support UDP yet, we don't currently want them.
        /*
        if( ggepBlock.isExtensionAvailable( GGEPBlock.UDP_HOST_CACHE_PHC))
        {
            byte[] data = ggepBlock.getExtensionData( GGEPBlock.UDP_HOST_CACHE_PHC );
            if ( data != null )
            {
                String packedData = new String( data );
                udpHostCaches = parsePackedHostCache( packedData, securityService );
            }
        }
        
        //check if the sending host is a udp host cache n add to
        //the functional udp host cache container
        if( ggepBlock.isExtensionAvailable( GGEPBlock.UDP_HOST_CACHE_UDPHC))
        {
            byte[] data = ggepBlock.getExtensionData( GGEPBlock.UDP_HOST_CACHE_UDPHC );
            try
            {
                DestAddress address = null;
                if ( data != null && data.length > 0)
                {
                    try {
                        address = PresentationManager.getInstance().createHostAddress( new String( data ), port );
                    } catch (MalformedDestAddressException e) {
                        // I2PFIXME: IMPORTANT:
                        // If we enable this code, how to handle it?
                    }
                }
                else
                {
                    address = pongAddress;
                }
                //add to the container
                AccessType access = securityService.controlHostAddressAccess(
                    address );
                if ( access == AccessType.ACCESS_GRANTED )
                {
                    udpHostCache = new UdpHostCache( address );
                }
            }
            catch( IllegalArgumentException e)
            {
                NLogger.warn( PongMsg.class, "INVALID Udp Host Cache found " +
                        "and ignored " + e);                
            }
        }
        */
    }

    /**
     * gets the list of udp host cache addresses from the 
     * packed host cache ggep extension
     * @param packedHostCaches
     */
    // I2PMOD:
    // Implements currently unsupported functionality, fix later.
    /*
    private Set<UdpHostCache> parsePackedHostCache( String packedHostCaches, PhexSecurityManager securityMgr )
    {
        PresentationManager netPresMgr = PresentationManager.getInstance();
        String[] hostCaches = packedHostCaches.split( "\n" );
        Set<UdpHostCache> packedUdpHostCaches = new HashSet<UdpHostCache>( hostCaches.length );
        
        for( int i=0; i < hostCaches.length; i++ )
        {
            //find the position of the first key/value pair if any
            int pos = hostCaches[i].indexOf( "&" );
            try
            {
                DestAddress address;
                //no key/value pair found
                if( pos == -1)
                {
                    address = netPresMgr.createHostAddress(hostCaches[i], DefaultDestAddress.DEFAULT_PORT);
                }
                else 
                {
                    //key/value pair found, but just ignore as of now
                    String temp = hostCaches[i].substring( 0, pos );
                    address = netPresMgr.createHostAddress(temp, DefaultDestAddress.DEFAULT_PORT);
                }
                AccessType access = securityMgr.controlHostAddressAccess( address );
                if ( access == AccessType.ACCESS_GRANTED )
                {
                    UdpHostCache cache = new UdpHostCache( address );
                    packedUdpHostCaches.add( cache );
                }
            }
            catch ( MalformedDestAddressException e ) 
            {
                // just ignore and continue with next string
                NLogger.warn( PongMsg.class, " Ignored " +
                        "One Host Cache address in a packed host cache list  "
                        + e );
                continue;
            }
        }
        return packedUdpHostCaches;
    }
    */
   
    private static Set<DestAddress> unpackIpPortData( byte[] data, PhexSecurityManager securityMgr )
    {
        Set<DestAddress> destAddresses = null;
        NLogger.warn( PongMsg.class, "Unpacking Pong message IPP message." );
        try
        {
            final int FIELD_SIZE = I2P_DEST_LENGTH;
            if (data.length % FIELD_SIZE != 0)
            {
                throw new InvalidGGEPBlockException("invalid IPPORT EXTENSION DATA IN PONG");
            }
            int size = data.length/FIELD_SIZE;
            destAddresses = new HashSet<DestAddress>();
            int index;
            for (int i=0; i<size; i++) 
            {
                index = i*FIELD_SIZE;
                byte[] destBytes = new byte[I2P_DEST_LENGTH];
                System.arraycopy(data, index, destBytes, 0, I2P_DEST_LENGTH);
                String destBase64 = destBytes.toString();

                DestAddress current = null;
                try
                {
                    current = PresentationManager.getInstance().createHostAddress(destBase64, -1);
                } catch (MalformedDestAddressException e)
                {
                    // Warn and abort.
                    NLogger.warn( PongMsg.class, "Malformed destination in Pong message IPP extension." );
                }
                
                AccessType access = securityMgr.controlHostAddressAccess( current );
                if ( access == AccessType.ACCESS_GRANTED )
                {
                    destAddresses.add( current );
                }
            }
        }
        catch ( InvalidGGEPBlockException exp )
        {
            //ignore and continue parsing...
            NLogger.warn( PongMsg.class, exp );
        }
        return destAddresses;
    }
    
    /**
     * Returns true if this pong is marking a ultrapeer. This is the case when
     * fileSizeInKB is a power of two but at least 8.
     * @return true if this pong is marking a ultrapeer.
     */
    public boolean isUltrapeerMarked()
    {
        if ( fileSizeInKB < 8 )
        {
            return false;
        }
        return ( fileSizeInKB & (fileSizeInKB - 1 ) ) == 0;
    }
    
    /**
     * Sets the ultrapeer kbytes field for ultrapeers.
     * This is done by returning the nearest power of two of the kbytes field.
     * A kbytes value of 1536 would return 1024.
     *                   1535              512.
     */
    private static int createUltrapeerMarking( int kbytes )
    {
        if ( kbytes < 12 )
        {
            return 8;
        }
        // first get the bit count of the value and substract 1...
        int bitCount = IOUtil.determineBitCount( kbytes );
        // calculate the power of two...
        int power = (int)Math.pow( 2, bitCount );
        // now determine the border value of the exponent...
        int minBorder = power - (power / 4);
        if ( kbytes < minBorder )
        {
            power = (int)Math.pow( 2, bitCount-1 );
        }
        return power;
    }
}