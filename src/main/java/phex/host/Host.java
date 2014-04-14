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
 *  $Id: Host.java 4417 2009-03-22 22:33:44Z gregork $
 */
package phex.host;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import phex.common.Environment;
import phex.common.QueryRoutingTable;
import phex.common.address.DestAddress;
import phex.common.log.NLogger;
import phex.connection.ConnectionClosedException;
import phex.connection.MessageQueue;
import phex.io.buffer.ByteBuffer;
import phex.msg.GUID;
import phex.msg.Message;
import phex.msg.QueryMsg;
import phex.msg.QueryResponseMsg;
import phex.msg.vendor.CapabilitiesVMsg;
import phex.msg.vendor.MessagesSupportedVMsg;
import phex.net.connection.Connection;
import phex.prefs.core.SecurityPrefs;
import phex.query.DynamicQueryConstants;
import phex.servent.Servent;
import phex.utils.GnutellaInputStream;
import phex.utils.GnutellaOutputStream;

/**
 * <p>A Gnutella host, or servent together with operating statistics and IO.</p>
 *
 * <p>This class seems to be overloaded. Hosts are used in several different
 * distinct modes:
 * <ul>
 * <li>Incoming sTypeIncoming - client half of a Gnutella networm node pair.
 * This is the default state of a Host. A phex.ReadWorker will be assigned to
 * handle the connection.</li>
 * <li>Outgoing sTypeOutgoing - server half of a Gnutella network node pair. A
 * phex.Listener will be assigned to handle the connection, and this will
 * delegate on to a phex.ReadWorker.</li>
 * <li>Download sTypeDownload - attempting to HTTP get data. A
 * phex.download.DownloadWorker will be assigned to handle the connection.</li>
 * <li>Push sTypePush - a Host to send data that has been routed via a Push
 * request. A phex.PushWorker will be assigned to handle the connection.</li>
 * </ul>
 * </p>
 */
public class Host
{
    public enum Type
    {
        OUTGOING,
        INCOMING,
        LOCAL
    }
    
    private static final int MAX_SEND_QUEUE = 400;

    private static final int DROP_PACKAGE_RATIO = 70;

    /**
     * The time after which a query routing table can be updated in milliseconds.
     */
    private long QUERY_ROUTING_UPDATE_TIME = 5 * 60 * 1000; // 5 minutes

    /**
     * The time after which a connection is stable.
     */
    private static final int STABLE_CONNECTION_TIME = 60 * 1000; // 60 seconds

    /**
     * Normal connection type.
     */
    public static final byte CONNECTION_NORMAL = 0;

    /**
     * Connection type as me as a leaf and the host as a ultrapeer.
     */
    public static final byte CONNECTION_LEAF_UP = 1;

    /**
     * Connection type from ultrapeer to ultrapeer.
     */
    public static final byte CONNECTION_UP_UP = 2;

    /**
     * Connection type representing me as a ultrapeer and the host as a leaf.
     */
    public static final byte CONNECTION_UP_LEAF = 3;

    private DestAddress hostAddress;

    private Connection connection;

    private HostStatus status;

    private String lastStatusMsg = "";

    private long statusTime = 0;

    private Type type;

    /**
     * Number of messages received from this host.
     */
    private int receivedMsgCount;

    /**
     * Number of messages send to this host.
     */
    private int sentMsgCount;

    private int droppedMsgCount;

    private long fileCount = -1;

    private long shareSize = -1;

    private String vendor;

    private boolean vendorChecked = false;
    
    /**
     * The servent id we found from {@link QueryResponseMsg}.
     */
    private GUID serventId;

    /**
     * The maxTTL this connection accepts for dynamic queries.
     * It is provide through the handshake header X-Max-TTL and used
     * for the dynamic query proposal.
     * This header indicates that we should not send fresh queries to
     * this connection with TTLs higher than the X-Max-TTL. If we are
     * routing traffic from other Ultrapeers, the X-Max-TTL is irrelevant.
     * The X-Max-TTL MUST NOT exceed 4, as any TTL above 4 indicates a client
     * is allowing too much query traffic on the network. This header is
     * particularly useful for compatibility with future clients that may
     * choose to have higher degrees but that would prefer lower TTL traffic
     * from their neighbors. For example, if future clients connect to
     * 200 Ultrapeers, they could use the X-Max-TTL header to indicate to
     * today's clients that they will not accept TTLs above 2. A typical
     * initial value for X-Max-TTL is 3.
     */
    private byte maxTTL;
    
    /**
     * The max hops value to use for queries coming from a hops flow vendor
     * message.
     */
    private byte hopsFlowLimit;

    /**
     * The intra ultrapeer connection this connection holds.
     * It is provide through the handshake header X-Degree and used
     * for the dynamic query proposal.
     * The X-Degree header simply indicates the number of Ultrapeer
     * connections this nodes attempts to maintain. Clients supporting
     * the dynamic query proposal must have X-Degrees of at least 15, 
     * and higher values are preferable.
     */
    private int ultrapeerDegree;

    /**
     * Marks if a connection is stable. A connection is stable when a
     * host connection last over STABLE_CONNECTION_TIME seconds.
     */
    private boolean isConnectionStable;

    /**
     * Defines if the host supports QRP. This is only important for Ultrapeer
     * connections.
     */
    private boolean isQueryRoutingSupported;

    /**
     * Defines if the host supports Ultrapeer QRP. This is only important for
     * Ultrapeer connections.
     */
    private boolean isUPQueryRoutingSupported;

    /**
     * Defines if the host supports dynamic query.
     */
    private boolean isDynamicQuerySupported;

    /**
     * Marks the last time the local query routing table was sent to this
     * host. Only for leaf-ultrapeers connection a query routing table is send.
     * @see #isQRTableUpdateRequired()
     */
    private long lastQRTableSentTime;

    /**
     * The QR table that was last sent to this host on lastQRTableSentTime. This
     * table is needed to send patch updates to the host.
     */
    private QueryRoutingTable lastSentQRTable;

    /**
     * The QR table that was last received from this host. This
     * table is needed to determine which querys to send to this host.
     */
    private QueryRoutingTable lastReceivedQRTable;

    /**
     * Defines the connection type we have with this host. Possible values are
     * CONNECTION_NORMAL
     * CONNECTION_LEAF_ULTRAPEER
     * PEER_LEAF
     */
    private byte connectionType;

    /**
     * A SACHRIFC message queue implementation.
     */
    private MessageQueue messageQueue;
    private SendEngine sendEngine;
    
    private boolean isVendorMessageSupported;
    
    private MessagesSupportedVMsg supportedVMsgs;
    private CapabilitiesVMsg capabilitiesVMsgs;    
    
    /**
     * The PushProxy host address of this host. It is received from a
     * PushProxyAcknowledgement vendor message.
     */
    private DestAddress pushProxyAddress;

    /**
     * Create a new Host with type OUTGOING.
     */
    private Host()
    {
        connection = null;
        status = HostStatus.NOT_CONNECTED;
        type = Type.OUTGOING;
        isConnectionStable = false;
        connectionType = CONNECTION_NORMAL;
        isQueryRoutingSupported = false;
        isUPQueryRoutingSupported = false;
        isDynamicQuerySupported = false;
        isVendorMessageSupported = false;
        receivedMsgCount = 0;
        sentMsgCount = 0;
        droppedMsgCount = 0;
        maxTTL = DynamicQueryConstants.DEFAULT_MAX_TTL;
        hopsFlowLimit = -1;
    }

    /**
     * <p>Create a new Host for a HostAddress that will default type
     * OUTGOING.</p>
     *
     * @param address  the HostAddress this Host will communicate with
     */
    public Host(DestAddress address)
    {
        this();
        hostAddress = address;
    }

    public Host(DestAddress address, Connection connection)
    {
        this();
        hostAddress = address;
        this.connection = connection;
    }
    
    public void setHostAddress( DestAddress hostAddress )
    {
        this.hostAddress = hostAddress;
    }

    public DestAddress getHostAddress()
    {
        return hostAddress;
    }

    public void setConnection(Connection connection)
    {
        this.connection = connection;
        receivedMsgCount = 0;
        sentMsgCount = 0;
        droppedMsgCount = 0;
    }

    /**
     * @return Returns the connection.
     */
    public Connection getConnection()
    {
        return connection;
    }
    
    /**
     * @deprecated
     */
    @Deprecated
    public GnutellaInputStream getInputStream() throws IOException
    {
        if ( connection == null ) { throw new ConnectionClosedException(
            "Connection already closed"); }
        return connection.getInputStream();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public GnutellaOutputStream getOutputStream() throws IOException
    {
        if ( connection == null ) { throw new ConnectionClosedException(
            "Connection already closed"); }
        return connection.getOutputStream();
    }

    public void activateInputInflation() throws IOException
    {
        getInputStream().activateInputInflation();
    }

    public void activateOutputDeflation() throws IOException
    {
        getOutputStream().activateOutputDeflation();
    }

    public void setVendor(String aVendor)
    {
        vendor = aVendor;
    }

    public String getVendor()
    {
        return vendor;
    }

    /**
     * @return the serventId
     */
    public GUID getServentId()
    {
        return serventId;
    }

    /**
     * @param serventId the serventId to set
     */
    public void setServentId(GUID serventId)
    {
        this.serventId = serventId;
    }

    public HostStatus getStatus()
    {
        return status;
    }
    
    public String getLastStatusMsg()
    {
        return lastStatusMsg;
    }

    public void setStatus( HostStatus status)
    {
        setStatus(status, null, System.currentTimeMillis());
    }

    public void setStatus( HostStatus status, long statusTime)
    {
        setStatus(status, null, statusTime);
    }

    public void setStatus( HostStatus status, String msg)
    {
        setStatus(status, msg, System.currentTimeMillis());
    }

    public void setStatus( HostStatus status, String msg, long statusTime)
    {
        if ( this.status == status && lastStatusMsg != null
            && lastStatusMsg.equals(msg) ) { return; }
        this.status = status;
        lastStatusMsg = msg;
        this.statusTime = statusTime;
    }

    /**
     * Checks if a connection status is stable. A stable connection
     * is a connection to host that last over STABLE_CONNECTION_TIME seconds.
     * The current time is given out of performance reasons when
     * looping over all hosts.
     */
    public void checkForStableConnection(long currentTime)
    {
        if ( isConnectionStable ) { return; }

        // if we have connected status for at least STABLE_CONNECTION_TIME.
        if ( status == HostStatus.CONNECTED
            && getConnectionUpTime(currentTime) > STABLE_CONNECTION_TIME )
        {
            isConnectionStable = true;
        }
    }

    public boolean isConnectionStable()
    {
        return isConnectionStable;
    }

    /**
     * Returns the number of millis the connection is up.
     */
    public long getConnectionUpTime(long currentTime)
    {
        if ( status == HostStatus.CONNECTED )
        {
            return currentTime - statusTime;
        }
        else
        {
            return 0;
        }
    }

    public boolean isErrorStatusExpired( long currentTime, long expiryDelay )
    {
        if ( status == HostStatus.ERROR || status == HostStatus.DISCONNECTED )
        {
            if ( currentTime - statusTime > expiryDelay ) { return true; }
        }
        return false;
    }

    public Type getType()
    {
        return type;
    }

    public void setType( Type aType )
    {
        this.type = aType;
    }

    public boolean isIncomming()
    {
        return type.equals( Type.INCOMING );
    }
    
    public void setVendorMessageSupported( boolean state )
    {
        this.isVendorMessageSupported = state;
    }
    
    public boolean isVendorMessageSupported()
    {
        return isVendorMessageSupported;
    }
    
    public void setCapabilitiesVMsgs( CapabilitiesVMsg capabilitiesVMsgs )
    {
        this.capabilitiesVMsgs = capabilitiesVMsgs;
    }
    
    public boolean isFeatureSearchSupported()
    {
        return capabilitiesVMsgs != null && capabilitiesVMsgs.isFeatureSearchSupported();
    }
    
    public void setSupportedVMsgs( MessagesSupportedVMsg supportedVMsgs )
    {
        this.supportedVMsgs = supportedVMsgs;
    }
    
    public boolean isTCPConnectBackSupported()
    {
        // EEPMOD: the I2P version can't support TCP connect back.
        // return supportedVMsgs != null && supportedVMsgs.isTCPConnectBackSupported();
        return false;
    }
    
    public boolean isTCPConnectBackRedirectSupported()
    {
        // EEPMOD: the I2P version can't support TCP connect back redirect.
        // return supportedVMsgs != null && supportedVMsgs.isTCPConnectBackRedirectSupported();
        return false;
    }
    
    public boolean isPushProxySupported()
    {
        // EEPMOD: the I2P version can't support push proxies.
        // return supportedVMsgs != null && supportedVMsgs.isPushProxySupported();
        return false;
    }
    
    public boolean isHopsFlowSupported()
    {
        // EEPMOD: the I2P version does not currently support hops flow.
        // return supportedVMsgs != null && supportedVMsgs.isHopsFlowSupported();
        return false;
    }
    
    /**
     * Returns the PushProxy host address of this host if received 
     * from a PushProxyAcknowledgement vendor message. Null otherwise.
     */
    public DestAddress getPushProxyAddress()
    {
        return pushProxyAddress;
    }
    
    /**
     * Sets the PushProxy host address of this host. It must be received 
     * from a PushProxyAcknowledgement vendor message.
     */
    public void setPushProxyAddress( DestAddress address )
    {
        pushProxyAddress = address;
    }

    public void incReceivedCount()
    {
        receivedMsgCount++;
    }

    public int getReceivedCount()
    {
        return receivedMsgCount;
    }

    public void incSentCount()
    {
        sentMsgCount++;
    }

    public int getSentCount()
    {
        return sentMsgCount;
    }

    public void incDropCount()
    {
        droppedMsgCount++;
    }

    public int getDropCount()
    {
        return droppedMsgCount;
    }

    public long getFileCount()
    {
        return fileCount;
    }

    public void setFileCount(long fileCount)
    {
        this.fileCount = fileCount;
    }

    /**
     * Returns total size in kBytes.
     */
    public long getTotalSize()
    {
        return shareSize;
    }

    public void setTotalFileSize(long shareSize)
    {
        this.shareSize = shareSize;
    }

    /**
     * Returns the maxTTL this connection accepts.
     * @return the maxTTL this connection accepts.
     * @see #maxTTL
     */
    public byte getMaxTTL()
    {
        return maxTTL;
    }

    /**
     * Sets the maxTTL this connection accepts.
     * @param maxTTL the new maxTTL.
     * @see #maxTTL
     */
    public void setMaxTTL(byte maxTTL)
    {
        this.maxTTL = maxTTL;
    }
    
    /**
     * Returns the max hops value to use for queries comming from a hops flow vendor
     * message, or -1 if not set.
     **/
    public byte getHopsFlowLimit()
    {
        return hopsFlowLimit;
    }
    
    /**
     * Sets the max hops value to use for queries coming from a hops flow vendor
     * message, or -1 to reset.
     * @param hopsFlowLimit
     */
    public void setHopsFlowLimit(byte hopsFlowLimit)
    {
        this.hopsFlowLimit = hopsFlowLimit;
    }
    
    /**
     * Returns the ultrapeer connection degree.
     * @return the ultrapeer connection degree.
     * @see #ultrapeerDegree
     */
    public int getUltrapeerDegree()
    {
        return ultrapeerDegree;
    }

    /**
     * Sets the ultrapeer connection degree of this connection.
     * @param degree the new ultrapeer connection degree.
     * @see #ultrapeerDegree
     */
    public void setUltrapeerDegree(int degree)
    {
        ultrapeerDegree = degree;
    }

    public boolean tooManyDropPackets()
    {
        // don't drop if this is a young connection
        if ( receivedMsgCount < 50
            && getConnectionUpTime(System.currentTimeMillis()) < 1000 * 60 ) { return false; }
        return (droppedMsgCount * 100 / (receivedMsgCount + 1) > DROP_PACKAGE_RATIO);
    }

    public boolean dropPacketsInRed()
    {
        return (droppedMsgCount * 100 / (receivedMsgCount + 1)) > (DROP_PACKAGE_RATIO * 3 / 4);
    }

    public boolean isConnected()
    {
        return connection != null;
    }

    public void disconnect()
    {
        if ( connection != null )
        {
            if ( status != HostStatus.ERROR )
            {
                setStatus( HostStatus.DISCONNECTED );
            }
            connection.disconnect();
            connection = null;
        }

        if ( messageQueue != null )
        {
            // notify messageQueue to cause SendEngine to stop waiting and running..
            synchronized (messageQueue)
            {
                messageQueue.notify();
            }
        }
        Servent.getInstance().getHostService().getNetworkHostsContainer().disconnectHost( this );
    }

    public int getSendQueueLength()
    {
        if ( messageQueue == null )
        {
            return 0;
        }
        else
        {
            return messageQueue.getQueuedMessageCount();
        }
    }

    public int getSendDropCount()
    {
        if ( messageQueue == null )
        {
            return 0;
        }
        else
        {
            return messageQueue.getDropCount();
        }
    }

    public boolean isSendQueueTooLong()
    {
        if ( messageQueue == null ) { return false; }
        return (messageQueue.getQueuedMessageCount() >= MAX_SEND_QUEUE - 1);
    }

    public boolean isSendQueueInRed()
    {
        if ( messageQueue == null ) { return false; }
        return (messageQueue.getQueuedMessageCount() >= MAX_SEND_QUEUE * 3 / 4);
    }

    public boolean isNoVendorDisconnectApplying()
    {
        if ( !SecurityPrefs.DisconnectNoVendorHosts.get().booleanValue() )
        {
            return false;
        }
            
        // Already checked?  Short-circuit out (no need to recalculate len & delta-time)
        // Possible issue if user toggles the config setting while connected to
        // an unwanted host--it will not disconnect because it already passed the test
        if ( vendorChecked ) { return false; }
        // The vendor string might not be there immediately because of
        // handshaking, but will certainly be there when the status is HOST_CONNECTED.
        if ( status != HostStatus.CONNECTED ) { return false; }

        String normalizedVendorString = this.vendor;
        if ( normalizedVendorString == null )
        {
            normalizedVendorString = "";
        }
        else
        {
            normalizedVendorString = normalizedVendorString.trim();
        }

        if ( normalizedVendorString.length() == 0 )
        {
            return true;
        }
        else
        {
            vendorChecked = true;
            return false;
        }
    }

    public boolean isFreeloader(long currentTime)
    {
// freeloaders are no real problem...       
//        // never count a ultrapeer as freeloader...
//        if ( isUltrapeer() ) { return false; }
//        long timeDelta = getConnectionUpTime(currentTime);
//        // We can only really tell after initial handshaing is complete, 10
//        // seconds should be a good delay.
//        if ( timeDelta >= DISCONNECT_POLICY_THRESHOLD )
//        {
//            if ( ServiceManager.sCfg.freeloaderFiles > 0
//                && fileCount < ServiceManager.sCfg.freeloaderFiles ) { return true; }
//            if ( ServiceManager.sCfg.freeloaderShareSize > 0
//                && (shareSize / 1024) < ServiceManager.sCfg.freeloaderShareSize ) { return true; }
//        }
        return false;
    }

    /**
     * Indicates that this is a ultrapeer and I am a leaf. The
     * connection type in this case is CONNECTION_LEAF_UP.
     * @return true if this is a ultrapeer and I am a leaf, false otherwise.
     */
    public boolean isLeafUltrapeerConnection()
    {
        return connectionType == CONNECTION_LEAF_UP;
    }

    /**
     * Indicates that this is a ultrapeer in general without paying attention to
     * my relationship to this ultrapeer. The connection type in this case can
     * be CONNECTION_LEAF_UP or CONNECTION_UP_UP.
     * @return true if this is a ultrapeer, false otherwise.
     */
    public boolean isUltrapeer()
    {
        return connectionType == CONNECTION_LEAF_UP
            || connectionType == CONNECTION_UP_UP;
    }

    /**
     * Indicates that this is a leaf and I am its ultrapeer. The
     * connection type in this case is CONNECTION_UP_LEAF.
     * @return true if this is a leaf and I am its ultrapeer, false otherwise.
     */
    public boolean isUltrapeerLeafConnection()
    {
        return connectionType == CONNECTION_UP_LEAF;
    }

    /**
     * Sets the connection type of the host. The connection can be of type:
     * CONNECTION_NORMAL
     * CONNECTION_LEAF_UP
     * CONNECTION_UP_UP
     * CONNECTION_UP_LEAF
     * @param connectionType the connection type of the host.
     */
    public void setConnectionType(byte connectionType)
    {
        this.connectionType = connectionType;
    }

    @Override
	public String toString()
    {
        return "Host" + "[" + hostAddress.getHostName() + ":"
            + hostAddress.getPort() + "," + vendor + ",State=" + status + "]";
    }

    ////////////////////////START MessageQueue implementation///////////////////

    /**
     * Sends a message over the output stream but is not flushing the output
     * stream. This needs to be done by the caller.
     * @param message the message to send
     * @throws IOException when a send error occurs
     */
    public void sendMessage( Message message ) throws IOException
    {
        if (NLogger.isDebugEnabled( Host.class ) )
            NLogger.debug( Host.class, 
                "Sending message: " + message + " - " + message.getHeader().toString());
        
        
        ByteBuffer headerBuf = message.createHeaderBuffer();
        ByteBuffer messageBuf = message.createMessageBuffer();
        if ( !isConnected() )
        {
            throw new ConnectionClosedException(
                "Connection is already closed");
        }
        connection.write( headerBuf );
        if ( !isConnected() )
        {
            throw new ConnectionClosedException(
                "Connection is already closed");
        }
        connection.write( messageBuf );
        
        if (NLogger.isDebugEnabled( Host.class ) )
            NLogger.debug( Host.class, 
                "Message send: " + message + " - " + message.getHeader().toString());
    }

    public void flushOutputStream() throws IOException
    {
        if ( isConnected() )
        {
            connection.flush();
            //Logger.logMessage( Logger.FINEST, Logger.NETWORK,
            //    "Messages flushed" );
        }
    }

    public void queueMessageToSend(Message message)
    {
        // before queuing a query check hops flow limit...
        if ( hopsFlowLimit > -1 &&
             message instanceof QueryMsg &&
             message.getHeader().getHopsTaken() >= hopsFlowLimit )
        {// don't send query!
            return;
        }
        
        NLogger.debug( Host.class, "Queuing message: " + message );
        initMessageQueue();
        incSentCount();
        synchronized (messageQueue)
        {
            messageQueue.addMessage(message);
            sendEngine.dispatch();
        }
    }
    
    private class SendEngine implements Runnable
    {
        private AtomicBoolean isRunning = new AtomicBoolean(false);
        
        public void dispatch( )
        {
            boolean result = isRunning.compareAndSet( false, true );
            if ( result )
            {
                String jobName = "SendEngine-" + Integer.toHexString(sendEngine.hashCode());
                Environment.getInstance().executeOnThreadPool( sendEngine,
                    jobName);
            }
        }
        
        public void run()
        {
            do
            {
                try
                {
                    messageQueue.sendQueuedMessages();
                }
                catch (IOException exp)
                {
                    setStatus( HostStatus.ERROR, exp.getMessage() );
                    disconnect();
                }
            }
            while( checkForRepeat() );
        }

        private boolean checkForRepeat()
        {
            synchronized (messageQueue)
            {
                if ( isConnected() && messageQueue.getQueuedMessageCount() > 0 )
                {
                    return true;
                }
                else
                {
                    boolean result = isRunning.compareAndSet( true, false );
                    if ( !result )
                    {
                        throw new RuntimeException( "Invalid state." );
                    }
                    return false;
                }
            }
        }
    }

    /**
     * This method is here to make sure the message queue is only generated
     * when it is actually necessary. Generating it in the constructor
     * uses up a big amount of memory for every known Host. Together with the
     * message queue its SendEngine is initialized.
     */
    private void initMessageQueue()
    {
        if ( messageQueue != null ) { return; }
        // Create a queue with max-size, dropping the oldest msg when max reached.
        messageQueue = new MessageQueue(this);
        sendEngine = new SendEngine();
    }

    ////////////////////////END MessageQueue implementation/////////////////////

    ////////////////////////START QRP implementation////////////////////////////

    public boolean isQRTableUpdateRequired()
    {
        return System.currentTimeMillis() > lastQRTableSentTime
            + QUERY_ROUTING_UPDATE_TIME;
    }

    public QueryRoutingTable getLastSentRoutingTable()
    {
        return lastSentQRTable;
    }

    public void setLastSentRoutingTable(QueryRoutingTable routingTable)
    {
        lastSentQRTable = routingTable;
        lastQRTableSentTime = System.currentTimeMillis();
    }

    public QueryRoutingTable getLastReceivedRoutingTable()
    {
        return lastReceivedQRTable;
    }

    public void setLastReceivedRoutingTable(QueryRoutingTable routingTable)
    {
        lastReceivedQRTable = routingTable;
    }

    public boolean isQueryRoutingSupported()
    {
        return isQueryRoutingSupported;
    }

    public void setQueryRoutingSupported(boolean state)
    {
        isQueryRoutingSupported = state;
    }

    public boolean isUPQueryRoutingSupported()
    {
        return isUPQueryRoutingSupported;
    }

    public void setUPQueryRoutingSupported(boolean state)
    {
        isUPQueryRoutingSupported = state;
    }

    /**
     * Returns if the host supports dynamic query.
     * @return true if dynamic query is supported, false otherwise.
     */
    public boolean isDynamicQuerySupported()
    {
        return isDynamicQuerySupported;
    }

    /**
     * Sets if the hosts supports dynamic query.
     * @param state true if dynamic query is supported, false otherwise.
     */
    public void setDynamicQuerySupported(boolean state)
    {
        isDynamicQuerySupported = state;
    }

    //////////////////////////END QRP implementation////////////////////////////

    public static final LocalHost LOCAL_HOST;
    static
    {
        LOCAL_HOST = new LocalHost();
    }

    public static class LocalHost extends Host
    {
        LocalHost()
        {
            // TODO the local address might change...
            super(Servent.getInstance().getLocalAddress());
        }

        @Override
		public boolean isConnected()
        {
            // return true to suite routing table..
            return true;
		}

        @Override
		public Type getType()
        {
            return Type.LOCAL;
        }

    }

}