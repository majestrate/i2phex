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
 *  $Id: NetworkRowRenderer.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.network;

import java.awt.*;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import phex.gui.common.GUIUtils;
import phex.gui.common.PhexColors;
import phex.gui.common.table.FWTable;
import phex.host.*;

/**
 * 
 */
public class NetworkRowRenderer implements TableCellRenderer
{
    private static final Color FAILED_COLOR = Color.gray;
    //private static final Color CONNECTING_COLOR = new Color( 0x7F, 0x00, 0x00 );
    //private static final Color CONNECTING_COLOR_SELECTED = new Color( 0xFF, 0x7f, 0x7f );
    //private static final Color CONNECTED_COLOR = new Color( 0x00, 0x7F, 0x00 );
    //private static final Color CONNECTED_COLOR_SELECTED = new Color( 0x7f, 0xFF, 0x7f );
    
    private NetworkHostsContainer hostsContainer;
    
    private Color connectingColor;
    private Color selectionConnectingColor;
    private Color connectedColor;
    private Color selectionConnectedColor;
    
    public NetworkRowRenderer( NetworkHostsContainer hostsContainer )
    { 
        this.hostsContainer = hostsContainer;
    }
    
    /** 
     * @Override
     */
    public void updateUI()
    {
        connectingColor = null;
        selectionConnectingColor = null;
        connectedColor = null;
        selectionConnectedColor = null;
    }
    
    private Color getForegroundColorForStatus( HostStatus status, JTable table, boolean isSelected )
    {
        switch ( status )
        {
            case NOT_CONNECTED:
                return null;

            case ERROR:
            case DISCONNECTED:
                return FAILED_COLOR;

            case CONNECTING:
            case ACCEPTING:
                if ( isSelected )
                {
                    selectionConnectingColor = determineColor( selectionConnectingColor, 
                        PhexColors.NETWORK_HOST_CONNECTING_COLORS, table, isSelected );
                    return selectionConnectingColor;
                }
                else
                {
                    connectingColor = determineColor( connectingColor, 
                        PhexColors.NETWORK_HOST_CONNECTING_COLORS, table, isSelected );
                    return connectingColor;
                }

            case CONNECTED:
                if ( isSelected )
                {
                    selectionConnectedColor = determineColor( selectionConnectedColor, 
                        PhexColors.NETWORK_HOST_CONNECTED_COLORS, table, isSelected );
                    return selectionConnectedColor;
                }
                else
                {
                    connectedColor = determineColor( connectedColor, 
                        PhexColors.NETWORK_HOST_CONNECTED_COLORS, table, isSelected );
                    return connectedColor;
                }
        }
        return null;
    }
    
    private Color determineColor( Color cache, Color[] candidates, JTable table, boolean isSelected )
    {
        if ( cache != null )
        {
            return cache;
        }
        Color base = isSelected ? table.getSelectionBackground() : table.getBackground();
        return GUIUtils.getBestColorMatch( base, candidates );
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column)
    {
        if ( !(table instanceof FWTable) )
        {
            throw new IllegalArgumentException( "table not of type FWTable" );
        }
        TableCellRenderer renderer = table.getDefaultRenderer(
            table.getColumnClass( column ) );
        Component comp = renderer.getTableCellRendererComponent( table, value,
            isSelected, hasFocus, row, column );
        FWTable fwTable = (FWTable) table;
        if ( isSelected )
        {
            comp.setForeground( table.getSelectionForeground() );
        }
        else
        {
            comp.setForeground( table.getForeground() );
        }
        

        if (row < hostsContainer.getNetworkHostCount() )
        {
            int modelRow = fwTable.translateRowIndexToModel( row );            
            Host host = hostsContainer.getNetworkHostAt( modelRow );
            if ( host == null )
            {
                return comp;
            }
            Color col = getForegroundColorForStatus( host.getStatus(), fwTable, isSelected );
            if ( col != null )
            {
                comp.setForeground( col );
            }
        }
        return comp;
    }    
}