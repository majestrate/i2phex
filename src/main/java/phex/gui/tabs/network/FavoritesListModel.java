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
 *  $Id: FavoritesListModel.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.network;

import java.awt.EventQueue;

import javax.swing.AbstractListModel;

import org.bushe.swing.event.annotation.EventTopicSubscriber;

import phex.event.ContainerEvent;
import phex.event.PhexEventTopics;
import phex.event.ContainerEvent.Type;
import phex.gui.common.GUIRegistry;
import phex.host.FavoriteHost;
import phex.host.FavoritesContainer;
import phex.servent.Servent;

public class FavoritesListModel extends AbstractListModel
{
    private final FavoritesContainer favoritesContainer;

    public FavoritesListModel( FavoritesContainer favoritesContainer )
    {
        GUIRegistry.getInstance().getServent().getEventService().processAnnotations( this );
        this.favoritesContainer = favoritesContainer;
    }

    public int getSize()
    {
        return favoritesContainer.getBookmarkedHostsCount();
    }

    public Object getElementAt( int row )
    {
        FavoriteHost host = favoritesContainer.getBookmarkedHostAt( row );
        if ( host == null )
        {
            fireIntervalRemoved( this, row, row );
            return "";
        }
        return host;
    }
    
    @EventTopicSubscriber(topic=PhexEventTopics.Net_Favorites)
    public void onBookmarkedHostEventAdded( String topic, final ContainerEvent event )
    {
        EventQueue.invokeLater( new Runnable() {
            public void run()
            {
                if ( event.getType() == Type.ADDED )
                {
                    fireIntervalAdded( this, event.getPosition(), event.getPosition() );
                }
                else if ( event.getType() == Type.REMOVED )
                {
                    fireIntervalRemoved( this, event.getPosition(), event.getPosition() );
                }
            }
        });
    }
}