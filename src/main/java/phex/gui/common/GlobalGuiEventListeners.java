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
 *  $Id: GlobalGuiEventListeners.java 4064 2007-11-29 22:57:54Z complication $
 */
package phex.gui.common;

import java.awt.EventQueue;

import org.bushe.swing.event.annotation.EventTopicSubscriber;

import phex.event.PhexEventService;
import phex.event.PhexEventTopics;
import phex.gui.dialogs.NewDownloadDialog;

public class GlobalGuiEventListeners
{
    public GlobalGuiEventListeners( PhexEventService eventService )
    {
        eventService.processAnnotations( this );
    }
    
    @EventTopicSubscriber(topic=PhexEventTopics.Incoming_Uri)
    public void incomingUriDownload( String topic, String uri )
    {
        showDownloadDialog( uri, NewDownloadDialog.URI_DOWNLOAD );
    }
    
    @EventTopicSubscriber(topic=PhexEventTopics.Incoming_Magma)
    public void incomingMagmaDownload( String topic, String uri )
    {
        showDownloadDialog( uri, NewDownloadDialog.MAGMA_DOWNLOAD );
    }
    
    @EventTopicSubscriber(topic=PhexEventTopics.Incoming_Rss)
    public void incomingRSSDownload( String topic, String uri )
    {
        showDownloadDialog( uri, NewDownloadDialog.RSS_DOWNLOAD );
    }
    
    private void showDownloadDialog( final String uri, final int type )
    {
        EventQueue.invokeLater( new Runnable()
        {
            public void run()
            {
                NewDownloadDialog dialog = new NewDownloadDialog( uri, type );
                GUIUtils.showMainFrame();
                dialog.setVisible( true );
                dialog.toFront();
            }
        } );
    }
}
