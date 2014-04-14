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
 *  --- SVN Information ---
 *  $Id$
 */
package phex.common.address;

import phex.servent.Servent;
import phex.common.log.NLogger;

import net.i2p.data.Destination;
import net.i2p.data.DataFormatException;

/**
 * Represents an I2P destination key.
 */
public class I2PDestAddress implements DestAddress
{
    /**
     * I2P destination addresses cannot have ports.
     * They must be constructed with an invalid port.
     * They never accept setting a valid port.
     * They always return an invalid port.
     */
    public static final int INVALID_PORT = -1;

    /** Cache the hash code for the address
     *  A value of 0 means it must be calculated.
     */
    private int hash = 0;

    /**
     * A Destination object, representing an I2P destination key,
     * consisting of a public encryption key, a public signing key,
     * and an optional certificate.
     */
    private Destination destination;

    /**
     * Create an I2PDestAddress from a base64 destination string and port number.
     * This is inefficient (and the port number gets plain discarded),
     * but more compatible with Phex internal logic.
     */
    public I2PDestAddress( String aDestination, int aPort )
    {
        // I2PFIXME:
        // Is throwing null pointer exceptions really the way to go here?
        if ( aDestination == null )
        {
            NLogger.error( I2PDestAddress.class, "Tried creating I2PDestAddress from null destination string" );
            throw new NullPointerException( "Tried creating I2PDestAddress from null destination string" );
        }
        
        if (aPort != INVALID_PORT)
        {
            NLogger.error( I2PDestAddress.class, "Tried creating I2PDestAddress with valid port" );
            throw new NullPointerException( "Tried creating I2PDestAddress with valid port" );
        }
        
        try {
            this.destination = new Destination( aDestination );
        } catch (DataFormatException e) {
            NLogger.error( I2PDestAddress.class, "Tried creating I2PDestAddress from invalid data" );
            throw new NullPointerException( "Tried creating I2PDestAddress from invalid data" );
        }
    }
    
    /**
     * Create an I2PDestAddress from a Destination object.
     * This is more efficient (no translation to base64 and back to binary),
     * but opportunities of using this constructor may be narrow.
     */
    public I2PDestAddress( Destination aDestination )
    {
        if (aDestination == null) {
            NLogger.error( I2PDestAddress.class, "Tried creating I2PDestAddress from null destination" );
            throw new NullPointerException( "Tried creating I2PDestAddress from null destination" );
        }
        this.destination = aDestination;
    }
    
    /**
     * Return the base64 string representation of an I2P destination.
     */
    public String getHostName()
    {
        return destination.toBase64();
    }
    
    /**
     * Return the base64 string representation of an I2P destination.
     */
    public String getFullHostName()
    {
        return destination.toBase64();
    }
    
    /**
     * Always return false, since I2P destinations cannot be parsed as IP addresses.
     */
    public boolean isIpHostName()
    {
        return false;
    }

    /**
     * Always return an invalid port, since I2P destinations don't have ports.
     */
    public int getPort()
    {
        return INVALID_PORT;
    }
    
    /**
     * Never accept a valid port, since I2P destinations don't have ports.
     * @deprecated this is a value type. Changing values should not be permitted.
     */
    public void setPort( int newPort )
    {
        if (newPort != INVALID_PORT)
        {
            NLogger.error( I2PDestAddress.class, "Tried setting a valid port on an I2P destination" );
            // I2PFIXME:
            // Consider throwing an exception here, since this should never run?
        }
        // There is no port, so never actually set anything.
        // Consequently it can't invalidate the hash either.
    }
    
    /**
     * Always return null, since I2P destinations don't have IP addresses.
     */
    public IpAddress getIpAddress()
    {
        return null;
    }
    
    /**
     * Return the I2P destination which this I2P destination address points to.
     */
    public Destination getDestination()
    {
        return destination;
    }

    /**
     * Check if the object is a Phex destination address.
     * Return false if not, otherwise delegate forward.
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof DestAddress )
        {
            return equals( (DestAddress) obj );
        }
        return false;
    }

    /**
     * Check if the Phex destination address is an I2P destination address.
     * Return false if not, otherwise delegate forward.
     */
    public boolean equals( DestAddress address )
    {
        if ( address instanceof I2PDestAddress )
        {
            return equals( (I2PDestAddress) address );
        }
        return false;
    }
    
    /**
     * Compare the destination of this I2P destination address,
     * with the destination of the argument.
     */
    public boolean equals( I2PDestAddress address )
    {
        if ( address != null)
        {
            return destination.equals(address.getDestination());
        }
        return false;
    }
    
    /**
     * Always return false, since an I2P destination address
     * cannot be compared to an IP address and port pair.
     */
    public boolean equals( byte[] testIp, int testPort )
    {
        // I2PFIXME:
        // Consider throwing an exception here, since this should neve run?
        return false;
    }

    /**
     * Return the hash code (only a convenience hash for use in hashtables)
     * of the underlying Destination object.
     */
    public int hashCode()
    {
        if (hash == 0) {
            hash = destination.hashCode();
            return hash;
        }
        return hash;
    }
    
    /**
     * Always return null, since the country of an I2P destination
     * hopefully requires some expensive attacks to find.
     */
    public String getCountryCode()
    {
        return null;
    }

    /**
     * Checks if the DestAddress is the external address of this host.
     * Since I2P has no concept of a loopback address,
     * this is the only check we can make.
     *
     * @return a <code>boolean</code> indicating if the DestAddress represents 
     *         this host.
     */
    public boolean isLocalHost( DestAddress localAddress )
    {
        return localAddress.equals( this );
    }
    
    /**
     * Checks if the DestAddress is a site local address.
     * Always return false, since I2P has no concept of a LAN. 
     *
     * @return a <code>boolean</code> indicating if the DestAddress is 
     * a site local address; or false if address is not a site local address.
     */
    public boolean isSiteLocalAddress()
    {
        return false;
    }
    
    /**
     * Checks if the DestAddress is completly valid.
     * An I2P destination address is valid, if its Destination is not null.
     *
     * @return a <code>boolean</code> indicating if the DestAddress is 
     * valid; or false otherwise.
     */
    public boolean isValidAddress()
    {
        return (destination != null);
    }

    public String toString()
    {
        return getFullHostName();
    }    
}