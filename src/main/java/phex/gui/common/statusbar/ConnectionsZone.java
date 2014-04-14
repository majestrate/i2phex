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
 *  $Id: ConnectionsZone.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.common.statusbar;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import phex.gui.common.GUIRegistry;
import phex.gui.common.IconPack;
import phex.host.HostManager;
import phex.host.NetworkHostsContainer;
import phex.servent.Servent;
import phex.utils.Localizer;

public class ConnectionsZone extends JPanel
{
    private final Servent servent;
    private NetworkHostsContainer hostsContainer;
    
    private JLabel connectionLabel;
    private Icon connectedIcon;
    private Icon firewalledIcon;
    private Icon disconnectedIcon;

    public ConnectionsZone()
    {
        super(  );
        SpringLayout layout = new SpringLayout();
        setLayout( layout );
        
        servent = GUIRegistry.getInstance().getServent();
        HostManager hostMgr = servent.getHostService();
        hostsContainer = hostMgr.getNetworkHostsContainer();
        
        connectionLabel = new JLabel();
        add( connectionLabel );
        
        updateZone();
        
        layout.putConstraint(SpringLayout.NORTH, connectionLabel, 2, SpringLayout.NORTH, this );
        layout.putConstraint(SpringLayout.WEST, connectionLabel, 5, SpringLayout.WEST, this );
        layout.putConstraint(SpringLayout.EAST, this, 5, SpringLayout.EAST, connectionLabel );
        layout.putConstraint(SpringLayout.SOUTH, this, 2, SpringLayout.SOUTH, connectionLabel );
        
        setupIcons();
    }
    
    private void setupIcons()
    {
        IconPack factory = GUIRegistry.getInstance().getPlafIconPack();
        
        connectedIcon = factory.getIcon( "StatusBar.Connected" );
        firewalledIcon = factory.getIcon( "StatusBar.Firewalled" );
        disconnectedIcon = factory.getIcon( "StatusBar.Disconnected" );
        
        connectionLabel.setIcon( disconnectedIcon );
    }

    public void updateZone()
    {
        int hostCount = hostsContainer.getTotalConnectionCount();
        if ( hostCount > 0 )
        {
            if ( servent.isFirewalled() )
            {
                connectionLabel.setIcon( firewalledIcon );
            }
            else
            {
                connectionLabel.setIcon( connectedIcon );
            }
        }
        else
        {
            connectionLabel.setIcon( disconnectedIcon );
        }

        Object[] args = new Object[]
        {
            Integer.valueOf( hostCount )
        };
        String text = Localizer.getFormatedString( "StatusBar_Connections", args );
        connectionLabel.setText( text );
        connectionLabel.setToolTipText( Localizer.getString( "StatusBar_TTTConnections" ) );
        
        validate();
    }
}