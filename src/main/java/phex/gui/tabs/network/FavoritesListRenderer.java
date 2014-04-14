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
 *  Created on 05.04.2004
 *  --- CVS Information ---
 *  $Id: FavoritesListRenderer.java 4133 2008-03-01 21:38:33Z complication $
 */
package phex.gui.tabs.network;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;

import phex.common.address.DestAddress;
import phex.gui.common.GUIRegistry;
import phex.gui.common.IconPack;
import phex.host.FavoriteHost;

/**
 * 
 */
public class FavoritesListRenderer extends DefaultListCellRenderer
{
    private IconPack iconFactory;
    
    public FavoritesListRenderer()
    {
        iconFactory = GUIRegistry.getInstance().getCountryIconPack();
    }
    
    @Override
    public Component getListCellRendererComponent( JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus)
    {
        super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
        
        if ( value instanceof FavoriteHost )
        {
            FavoriteHost host = (FavoriteHost)value;
            DestAddress hostAddress = host.getHostAddress();
            setText( hostAddress.getFullHostName() );
            String countryCode = hostAddress.getCountryCode();
            Icon icon = null;
            if ( countryCode != null && countryCode.length() > 0 )
            {
                icon = iconFactory.getIcon( countryCode );
            }
            setIcon( icon );
        }
        return this;
    }
}