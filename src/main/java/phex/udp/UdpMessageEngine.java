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
 *  $Id: UdpMessageEngine.java 4360 2009-01-16 09:08:57Z gregork $
 */
package phex.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import phex.common.ThreadTracking;
import phex.net.repres.PresentationManager;
import phex.common.address.DestAddress;
import phex.common.address.IpAddress;
import phex.common.log.NLogger;
import phex.host.Host;
import phex.host.HostManager;
import phex.msg.GUID;
import phex.msg.InvalidMessageException;
import phex.msg.Message;
import phex.msg.MsgHeader;
import phex.msg.PingMsg;
import phex.msg.PongFactory;
import phex.msg.PongMsg;
import phex.msghandling.MessageService;
import phex.security.AccessType;
import phex.security.PhexSecurityManager;
import phex.servent.Servent;
import phex.share.SharedFilesService;
import phex.statistic.StatisticProvider;
import phex.statistic.StatisticsManager;
import phex.utils.HexConverter;

/**
 * @author Madhu
 */
public class UdpMessageEngine
{
    /**
     * Max udp packet size we will accept
     * as of now its small as we are dealing only with pings and pongs
     */
    public final static int MAX_PACKET_SIZE = 1024;
    
    /** 
     * HINT TO IMPLEMENTATION ABOUT THE SOCKET RECV BUFFER SIZE
     */ 
    public final static int MAX_RECV_BUFFER_SIZE = 16384;
    
    private DatagramSocket udpSocket;
    
    /** 
     * the send msg queue
     */
    private UdpMsgQueue sendQueue;
    
    /**
     * The guid routing table for pings
     */
    private UdpGuidRoutingTable pingRoutingTable;
    
    private final Servent servent;
    private final HostManager hostService;
    private final PongFactory pongFactory;
    private final PhexSecurityManager securityManager;
    private final SharedFilesService sharedFilesService;
        
    public UdpMessageEngine( Servent servent, HostManager hostService, 
        PongFactory pongFactory, SharedFilesService sharedFilesService )
    {
        this.servent = servent;
        this.hostService = hostService;
        this.pongFactory = pongFactory;
        this.sharedFilesService = sharedFilesService;
        
        sendQueue = new UdpMsgQueue();
        securityManager = servent.getSecurityService();
        
        //create the routing table with a lifetime of the 
        //udp send ping time interval....so a pong will only be accepted if
        //it comes within the period between 0 to 2 * lifetime
        pingRoutingTable = new UdpGuidRoutingTable( 
                MessageService.UDP_PING_PERIOD );
        
        // bind a udp socket to the port on which we operate on
        int port = servent.getLocalAddress().getPort();
        try
        {
            udpSocket = new DatagramSocket( port );
            udpSocket.setReceiveBufferSize( MAX_RECV_BUFFER_SIZE );
        }
        catch( SocketException e )
        {
            NLogger.warn( UdpMessageEngine.class, " Couldnt bind to port "
                    + port, e );
            return;
        }

        // start receiver and sender threads.
        Thread thread = new Thread( ThreadTracking.rootThreadGroup, 
            new Reciever(), "UDP Message Receiver" );
        thread.start();
        
        thread = new Thread( ThreadTracking.rootThreadGroup, 
            new Sender(), "UDP Message Sender" );
        thread.start();        
    }
 
    /**
     * Adds a message to the send queue
     * @param msg the message to add
     * @param destinationAddr the destination for the message.
     */
    public void addMessageToSend( Message msg, DestAddress destinationAddr )
    {
        // I2PFIXME:
        // Until it becomes apparent whether this is needed or not,
        // reliably fail here too.
        if (true) return;
        
        if ( msg == null )
        {
            throw new IllegalArgumentException( "msg is null" );
        }
        if ( destinationAddr == null )
        {
            throw new IllegalArgumentException( "destinationAddr is null" );
        }
        
        sendQueue.addMessage( msg, destinationAddr );
    }
    
    /**
     * removes a message from the queue and encapsulates it in
     * a datagram. It blocks if no message is available 
     * @return
     * a datagram on success or null on failure
     */
    private DatagramPacket getDatagramToSend()
    {
        UdpMsgQueue.QueueElement element;
        element = sendQueue.removeMessage();
        
        Message msg = element.getMsg();
        DestAddress address = element.getAddress();
        
//        if( !( netContainer.isConnectedToHost( address ) ) )
//        {
//          //dont send
//          return null;  
//        }
        
        byte[] data = null;
        
        if( msg instanceof PingMsg )
        {
            GUID guid = msg.getHeader().getMsgID();
            if( ! (pingRoutingTable.checkAndAddRouting( guid, address ) ) )
            {
                //could not add to routing table
                NLogger.warn( UdpMessageEngine.class,
                        " ping with duplicate guid not sent " + guid 
                        + " for message : " + msg );
                return null;
            }
            PingMsg ping = ( PingMsg )msg;
            data = ping.getBytes();
            
            NLogger.debug( UdpMessageEngine.class, 
                    " guid : " + guid + " successfully added to routing table for " +
                    		" udp ping : \n " + msg );
        }
        
        if( msg instanceof PongMsg )
        {
            PongMsg pong = ( PongMsg )msg;
            data = pong.getbytes();
        }
        
        if ( data == null )
        {
            return null;
        }
        
        try
        {
            InetAddress ipAddr;
            IpAddress ipAddress = address.getIpAddress();
            if ( ipAddress != null )
            {
                ipAddr = InetAddress.getByAddress( ipAddress.getHostIP() );
            }
            else
            {
                ipAddr = InetAddress.getByName( address.getHostName() );
            }
            int port = address.getPort();
            DatagramPacket packet = new DatagramPacket( data, data.length, 
                    ipAddr, port );
            NLogger.debug( UdpMessageEngine.class, " created udp datagram" +
                    " for msg " + msg + " \n to " + ipAddr );
            return packet;
        }
        catch( UnknownHostException e)
        {
            // just report
            NLogger.warn( UdpMessageEngine.class, 
                    " Could not create datagram  from message : " + msg, e );
        }
        // if it has reached here then packet is not created
        return null;
    }
    
    /**
     * reads a packet from the socket
     * @return
     * the packet or null if something went wrong
     */
    public DatagramPacket readMessage()
    {
        byte[] data = new byte[ MAX_PACKET_SIZE ]; 
        DatagramPacket packet = new DatagramPacket( data, data.length );
        
        try
        {
            udpSocket.receive( packet );
            return packet;
        }
        catch ( IOException e )
        {
            NLogger.warn( UdpMessageEngine.class, 
                    " Could not read from udp socket " + udpSocket.getLocalSocketAddress(), e );
            return null;
        }
    }
    
    private void handlePing( MsgHeader header, byte[] body, Host fromHost )
    {
        try
        {
            if ( header.getHopsTaken() > 1 )
            {
                throw new InvalidMessageException( "Udp Ping traveled more then 1 hop." );
            }
            PingMsg udpPing = PingMsg.createUdpPingMsg( header, body, MsgHeader.DATA_LENGTH, fromHost );
            NLogger.debug( UdpMessageEngine.class, " Recieved Udp Ping " +
            		"Msg From " + fromHost + " : " + udpPing );
            respondToPing( udpPing );
        }
        catch ( InvalidMessageException exp )
        {
            dropMessage( header, body, fromHost,
                "Invalid message: " + exp.getMessage() );
            NLogger.warn( UdpMessageEngine.class, exp, exp );
        }
    }
    
    private void respondToPing( PingMsg udpPing )
    {
        StatisticsManager statMgr = servent.getStatisticsService();
        StatisticProvider uptimeProvider = statMgr.getStatisticProvider(
            StatisticsManager.DAILY_UPTIME_PROVIDER );
        int avgDailyUptime = ((Integer)uptimeProvider.getValue()).intValue();
        int shareFileCount = sharedFilesService.getFileCount();
        int shareFileSize = sharedFilesService.getTotalFileSizeInKb();
        
        DestAddress localAddress = servent.getLocalAddress();
        boolean isUdpHostCache = servent.isUdpHostCache();
        
        PongMsg pong = pongFactory.createUdpPongMsg( udpPing, localAddress, isUdpHostCache,
            avgDailyUptime, shareFileCount, shareFileSize, servent.isUltrapeer(), 
            hostService.getCaughtHostsContainer(), hostService.getUhcContainer() );

        
        //add to send queue
        DestAddress address = udpPing.getHeader().getFromHost().getHostAddress(); 
        
        addMessageToSend( pong, address );
        NLogger.info( UdpMessageEngine.class,
                "added to send queue Udp Pong :" + pong + " \n \t to " + address );
    }
    
    private void handlePong( MsgHeader header, byte[] data, Host fromHost )
    {
        // first check if we had sent a ping to receive a pong
        GUID guid = header.getMsgID();
        DestAddress address = pingRoutingTable.getAndRemoveRouting( guid ); 
        if( address == null )
        {
            // did not find routing for this pong
            NLogger.warn( UdpMessageEngine.class, " Recieved Udp Pong " +
            		" with Guid not found in the routing table : " + header 
            		+ " \n \t Ignoring pong");
            return;
        }
        
        // thought of comparing the address in the table to the pong packet's address
        // but since its udp the packet can come from any interface of the packet's host
        // so just be happy that u sent a ping with the same guid
        
        PongMsg udpPong = null;
        try
        {
            if ( header.getHopsTaken() > 1 )
            {
                throw new InvalidMessageException( "Udp Ping traveled more then 1 hop." );
            }
            udpPong = pongFactory.createUdpPongMsg( header, data, MsgHeader.DATA_LENGTH, fromHost,
                securityManager );
            NLogger.debug( UdpMessageEngine.class, " Recieved Udp Pong " +
            		"Msg From " + fromHost + " : " + udpPong );
        }
        catch ( InvalidMessageException exp )
        {
            dropMessage( header, data, fromHost,
                "Invalid message: " + exp.getMessage() );
            NLogger.warn( UdpMessageEngine.class, exp, exp );
            return;
        }
        
        AccessType access = securityManager.controlHostAddressAccess( udpPong.getPongAddress() );
        if ( access == AccessType.ACCESS_STRONGLY_DENIED )
        {
            // drop message
            // dropMessage( udpPong, "IP access strongly denied." );
            return;
        }

        // add address to host catcher...
        if ( access == AccessType.ACCESS_GRANTED )
        {
            boolean isNew = hostService.catchHosts( udpPong );
            if ( isNew )
            {
                Servent.getInstance().getMessageService().addPongToCache( udpPong );
            }
        }
    }
    
    private void dropMessage( MsgHeader header, byte[] body, Host fromHost, String reason )
    {
        NLogger.info( UdpMessageEngine.class, 
            "Dropping UDP message: " + reason + " from: " + fromHost );
        if ( NLogger.isDebugEnabled( UdpMessageEngine.class ) )
        {
            NLogger.debug( UdpMessageEngine.class,
                "Header: " + header.toString() + " Body: " +
                " (" + HexConverter.toHexString( body, 0,
                header.getDataLength() ) + ")." );
        }
        // TODO should we count dropping udp? currently we dont
        // fromHost.incDropCount();
        // MessageCountStatistic.dropedMsgInCounter.increment( 1 );
    }
    
    
    /** 
     * Runs as a thread and sends messages that have been queued up.
     * @author Madhu
     */ 
    private class Sender implements Runnable
    {
        public void run()
        {
            while( true )
            {
                DatagramPacket packet = getDatagramToSend();
                
                if( packet == null )
                {
                    continue;
                }
                
                try
                {
                    udpSocket.send( packet );                    
                }
                catch ( IOException e )
                {
                    NLogger.warn( Sender.class, 
                            "Sending udp message " + packet + "failed ", e );
                }
            }
        }
    }
    
    /**
     * runs as a thread and picks up udp messages sent to us
     * @author Madhu
     */
    class Reciever implements Runnable
    {
        public void run()
        {
            DatagramPacket packet;
            while( true )
            {
                packet = readMessage();
                if( packet == null )
                {
                    continue;
                }
                
                byte[] packetData = packet.getData();
                int pktDataLength = packet.getLength();
                byte[] ip = packet.getAddress().getAddress();
                int port = packet.getPort();
                DestAddress address = PresentationManager.getInstance().createHostAddress( ip, port );
                
                //the data size shud be atleast a message's Header length 
                if( pktDataLength < MsgHeader.DATA_LENGTH )
                {
                    continue;
                }
                
//                //we only accept packets from hosts we are still connected to
//                //this is a sort of a crude restriction on the messages we can recieve 
//                if( ! ( netContainer.isConnectedToHost( address ) ) )
//                {
//                    continue;
//                }
                
                // TODO validate if the fromHost is a valid and acceptable source
                // we like to receive udp packages from.
                
                Host fromHost = hostService.getNetworkHostsContainer().getNetworkHost( address );
                if( fromHost == null )
                {
                    fromHost = new Host( address );
                }
                
                MsgHeader header = null;
                try
                {
                    header = MsgHeader.createMsgHeader( packetData, 0 );
                    
                    // check if the header data length is valid 
                    if( header.getDataLength() > ( pktDataLength - MsgHeader.DATA_LENGTH ) )
                    {
                        NLogger.warn( Reciever.class,
                                " Msg Header Data length is invalid " );
                    }
                    
                    header.countHop();
                    
                    // Now check the payload field and take appropriate action
                    switch( header.getPayload() )
                    {
                        case MsgHeader.PING_PAYLOAD :
                            NLogger.debug( Reciever.class, " Recvd Ping from : " + 
                                    address );
                            handlePing( header, packetData, fromHost );
                            break;
                        case MsgHeader.PONG_PAYLOAD :
                            NLogger.debug( Reciever.class, " Recvd Pong from : " + 
                                    address );
                            handlePong( header, packetData, fromHost );
                            break;
                        default:
                            NLogger.debug( Reciever.class, " Recvd unrecognized Msg from : " + 
                                    address );
                            break;
                    }
                }
                catch ( InvalidMessageException exp )
                {
                    NLogger.warn( Reciever.class,
                        "Invalid message: " + exp.getMessage(), exp );
                }
                catch ( Exception e )
                {
                    NLogger.warn( Reciever.class,
                            " Failed to create udp pong from datagram ", e );
                }
            }
        }
    }
}
