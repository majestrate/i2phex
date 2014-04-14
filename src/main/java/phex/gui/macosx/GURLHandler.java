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
 *  $Id: GURLHandler.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.macosx;

import java.awt.EventQueue;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import phex.download.swarming.SwarmingManager;
import phex.gui.common.GUIRegistry;
import phex.servent.Servent;

/**
 * JNI based GetURL AppleEvent handler for Mac OS X
 */
public final class GURLHandler
{
    static
    {
        //System.loadLibrary("GURL");
    }

    private static final GURLHandler INSTANCE = new GURLHandler();

    private boolean isRegistered = false;

    private GURLHandler() throws UnsatisfiedLinkError
    {
    }

    public static GURLHandler getInstance()
    {
        return INSTANCE;
    }

    /** Called by the native code */
    private void callback(final String uriStr)
    {
        // currently we only accept magnets through this interface...
        if ( !uriStr.startsWith( "magnet" ) )
        {
            return;
        }
        
        Runnable runner = new Runnable()
        {
            public void run()
            {
                try
                {
                    URI uri = new URI( uriStr, true );
                    // dont add already downloading urns.
                    SwarmingManager swarmingMgr = GUIRegistry.getInstance().getServent().getDownloadService();
                    swarmingMgr.addFileToDownload( uri, true );
                }
                catch ( URIException exp )
                {
                    return ;
                }
            }
        };
        EventQueue.invokeLater(runner);
    }

    /** Registers the GetURL AppleEvent handler. */
    public void register()
    {
        if (!isRegistered)
        {
            if (InstallEventHandler() == 0)
            {
                isRegistered = true;
            }
        }
    }

    /** We're nice guys and remove the GetURL AppleEvent handler although
     this never happens */
    protected void finalize() throws Throwable
    {
        if (isRegistered)
        {
            RemoveEventHandler();
        }
    }

    @SuppressWarnings( value="NM_METHOD_NAMING_CONVENTION", justification="native method")
    private synchronized final native int InstallEventHandler();

    
    private synchronized final native int RemoveEventHandler();
}

