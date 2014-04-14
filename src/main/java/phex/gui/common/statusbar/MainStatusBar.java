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
 *  $Id: MainStatusBar.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.common.statusbar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import phex.common.bandwidth.BandwidthManager;
import phex.common.log.NLogger;
import phex.download.swarming.SwarmingManager;
import phex.gui.common.GUIRegistry;
import phex.servent.Servent;

public class MainStatusBar extends StatusBar
{   
    private final UpdateAction updateAction;

    public MainStatusBar()
    {
        super();
        Servent servent = GUIRegistry.getInstance().getServent();
        BandwidthManager bandwidthService = servent.getBandwidthService();
        SwarmingManager downloadService = servent.getDownloadService();
        updateAction = new UpdateAction();
        addZone("ConnectionsZone", new ConnectionsZone(), "*");
        addZone("DownloadZone", new DownloadZone( downloadService, bandwidthService ), "");
        addZone("UploadZone", new UploadZone( bandwidthService ), "");
    }
    
    @Override
    public void removeNotify()
    {
        super.removeNotify();
        GUIRegistry.getInstance().getGuiUpdateTimer().removeActionListener( 
            updateAction );
    }
    
    @Override
    public void addNotify()
    {
        super.addNotify();
        GUIRegistry.getInstance().getGuiUpdateTimer().addActionListener( 
            updateAction );
    }
    
    private final class UpdateAction implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                ((ConnectionsZone)getZone("ConnectionsZone")).updateZone();
                ((DownloadZone)getZone("DownloadZone")).updateZone();
                ((UploadZone)getZone("UploadZone")).updateZone();
            }
            catch ( Throwable th )
            {
                NLogger.error( UpdateAction.class, th, th);
            }
        }
    }
}
