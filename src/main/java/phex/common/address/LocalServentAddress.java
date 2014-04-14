package phex.common.address;

import phex.event.ChangeEvent;
import phex.event.PhexEventService;
import phex.event.PhexEventTopics;
import phex.net.Server;
import phex.net.repres.PresentationManager;
import phex.prefs.core.ProxyPrefs;

public class LocalServentAddress implements DestAddress
{
    /**
     * The server this local address applies for.
     */
    private final Server server;
    
    /**
     * The event service to use to publish address changes.
     */
    private final PhexEventService eventService;
    
    /**
     * A possibly forced address from the user, 
     * it's null in case no forced address is set.
     */
    private DestAddress forcedAddress;
    
    /**
     * The determined local address of this node, only
     * used if forcedAddress is not null.
     */
    private DestAddress localAddress;
    
    
    public LocalServentAddress( Server server, PhexEventService eventService )
    {
       this.server = server;
       this.eventService = eventService;
       // I2PMOD: IMPORTANT:
       // Cannot initialize to local address.
       // There is no concept of a local address in I2P.
       // Let's hope that nothing breaks if we leave it null
       // until I2PServer.bind() gets an address from its I2PSocketManager.
       localAddress = null;
    }
    
    /**
     * Updates the local address in case there is no forced ip set.
     */
    public void updateLocalAddress( DestAddress updateAddress )
    {
        // I2PMOD: no concept of a forced address, disable to be sure.
        /* if ( forcedAddress != null )
        {
            // we have a forced address the local address has no value.
            return;
        } */
      
        // I2PMOD:
        // We should not actually compare, but simply accept any non-null address,
        // since the only source which may call updateLocalAddress is I2PServer.bind(),
        // and knows our authoritative and final address, which never changes on runtime,
        // so no point in letting anything null it either.

        // we should only compare the IP before updating.. 
        // the port is usually different..
        if (updateAddress != null)
        {
            // I2PFIXME:
            // Recreate or copy reference? Currently copy, much easier.
            localAddress = updateAddress;
            fireNetworkIPChanged( localAddress );
        }
    }

    /**
     * Sets the forced IP in the configuration. This call is not saving the
     * configuration!
     */
    public void setForcedHostIP( IpAddress forcedHostIP )
    {
        // I2PMOD: not supported.
        /*
        PresentationManager presentationMgr = PresentationManager.getInstance();
        if ( forcedHostIP == null )
        {// clear forcedHostIP and init localAddress
            forcedAddress = null;
            ProxyPrefs.ForcedIp.set( "" );
            IpAddress hostIP = server.resolveLocalHostIP();
            int port = server.getListeningLocalPort();
            DestAddress address = presentationMgr.createHostAddress( 
                hostIP, port );
            updateLocalAddress( address );
            return;
        }
        if ( !forcedHostIP.isValidIP() )
        { 
            throw new IllegalArgumentException( 
                "Invalid IP " + forcedHostIP );
        }
        
        ProxyPrefs.ForcedIp.set( forcedHostIP.getFormatedString() );
        
        forcedAddress = presentationMgr.createHostAddress( forcedHostIP, 
            server.getListeningLocalPort() );
        fireNetworkIPChanged( forcedAddress );
        */
    }
    
    private void fireNetworkIPChanged( DestAddress newAddress )
    {
        eventService.publish( PhexEventTopics.Servent_LocalAddress, 
            new ChangeEvent( this, null, newAddress ) );
    }

    
    protected DestAddress getEffectiveAddress()
    {
        return forcedAddress != null ? forcedAddress : localAddress;
    }
    
    //////////////////// Delegate methods ///////////////////////////
    
    public boolean equals(DestAddress address)
    {
        return getEffectiveAddress().equals( address );
    }


    public boolean equals(byte[] ipAddress, int port)
    {
        return getEffectiveAddress().equals( ipAddress, port );
    }

    public String getCountryCode()
    {
        return getEffectiveAddress().getCountryCode();
    }


    public String getFullHostName()
    {
        return getEffectiveAddress().getFullHostName();
    }


    public String getHostName()
    {
        return getEffectiveAddress().getHostName();
    }


    public IpAddress getIpAddress()
    {
        return getEffectiveAddress().getIpAddress();
    }


    public int getPort()
    {
        return getEffectiveAddress().getPort();
    }


    public boolean isIpHostName()
    {
        return getEffectiveAddress().isIpHostName();
    }


    public boolean isLocalHost( DestAddress localAddress )
    {
        return getEffectiveAddress().isLocalHost( localAddress );
    }


    public boolean isSiteLocalAddress()
    {
        return getEffectiveAddress().isSiteLocalAddress();
    }


    public boolean isValidAddress()
    {
        return getEffectiveAddress().isValidAddress();
    }


    
    
    @Deprecated
    public void setPort(int port)
    {
        getEffectiveAddress().setPort( port );
    }
}
