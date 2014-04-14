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
 *  Created on 29.10.2005
 *  --- CVS Information ---
 *  $Id: PresentationManager.java 4362 2009-01-16 10:27:18Z gregork $
 */
package phex.net.repres;

import java.io.IOException;

import phex.common.address.DestAddressFactory;
import phex.common.address.DestAddress;

import net.i2p.client.streaming.I2PSocketManager;

/**
 * 
 *
 */
public abstract class PresentationManager implements DestAddressFactory
{
    private static final String PRESENTATION_MANAGER_DEFAULT = 
        "phex.net.repres.i2p.I2PPresentationManager";
    private static PresentationManager instance;
    
    public abstract SocketFacade createSocket( DestAddress address, int connectTimeout )
        throws IOException;
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    /// Manager methods
    ////////////////////////////////////////////////////////////////////////////
    
    public static PresentationManager getInstance()
    {
        if ( instance == null )
        {
            instance = createInstance();
        }
        return instance;
    }
    
    private static PresentationManager createInstance()
    {
        String className = PRESENTATION_MANAGER_DEFAULT;
        Class clazz;
        try
        {
            clazz = Class.forName( className );
            if ( !PresentationManager.class.isAssignableFrom( clazz ) )
            {
                throw new RuntimeException( "Can't create PresentationManager from: "
                    + className );
            }
            PresentationManager pMgr = (PresentationManager) clazz.newInstance();
            pMgr.initialize();
            return pMgr;
        }
        catch (ClassNotFoundException exp)
        {
            throw new RuntimeException( "Class not found " + className );
        }
        catch (InstantiationException exp)
        {
            throw new RuntimeException( "Failed to instantiate " + className );
        }
        catch (IllegalAccessException exp)
        {
            throw new RuntimeException( "Illegal access to " + className );
        }
        
    }
    
    /**
     * This method is called in order to initialize the manager. This method
     * includes all tasks that must be done to intialize all the several manager.
     * Like instantiating the singleton instance of the manager. Inside
     * this method you can't rely on the availability of other managers.
     * @return true is initialization was successful, false otherwise.
     */
    public boolean initialize()
    {
        return true;
    }

    
    /**
     * This method is called to return a socket manager for the current network.
     * If the network is ordinary internet, null is returned.
     * If the network is I2P, an I2PSocketManager is returned,
     * using which I2P-specific classes can create sockets,
     * determine the local destination, and perform other work.
     *
     * I2PFIXME:
     * Consider writing an abstract socket manager,
     * to avoid an I2P-specific return type here.
     */
    public I2PSocketManager getSocketManager()
    {
        return null;
    }
}
