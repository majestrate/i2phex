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
 *  $Id: ChatFrameManager.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.chat;

import java.awt.EventQueue;
import java.util.*;

import org.bushe.swing.event.annotation.EventTopicSubscriber;


import phex.chat.*;
import phex.event.*;
import phex.gui.common.GUIRegistry;
import phex.servent.Servent;
import phex.utils.*;

public class ChatFrameManager
{
    private static final int MAX_CHAT_WINDOWS = 30;
    private HashMap<ChatEngine, ChatFrame> openChatsMap;

    public ChatFrameManager()
    {
        // give room for 3 chat slots
        openChatsMap = new HashMap<ChatEngine, ChatFrame>( 4 );
        
        GUIRegistry.getInstance().getServent().getEventService().processAnnotations( this );
    }
    
    @EventTopicSubscriber(topic=PhexEventTopics.Chat_Update)
    public void onChatEvent( String topic, final ChatEvent event )
    {
        EventQueue.invokeLater( new Runnable()
        {
            public void run()
            {
                switch ( event.getType() )
                {
                case OPENED:
                    chatConnectionOpened( event.getEngine() );
                    break;
                case FAILED:
                    chatConnectionFailed( event.getEngine() );
                    break;
                case MSG_REC:
                    chatMessageReceived( event.getEngine(), event.getChatMsg() );
                    break;
                }
            }
        } );
    }

    private void chatConnectionOpened( ChatEngine chatEngine )
    {
        if ( openChatsMap.size() > MAX_CHAT_WINDOWS )
        {
            // we don't like to open more chat windows to prevent DoS attacks.
            chatEngine.stopChat();
            return;
        }
        ChatFrame frame = new ChatFrame( chatEngine );
        frame.setVisible( true );
        openChatsMap.put( chatEngine, frame );
    }

    private void chatMessageReceived( ChatEngine chatEngine, String chatMessage )
    {
        ChatFrame frame = openChatsMap.get( chatEngine );
        frame.addChatMessage( chatMessage );
    }

    private void chatConnectionFailed( ChatEngine chatEngine )
    {
        ChatFrame frame = openChatsMap.remove( chatEngine );

        if ( frame != null )
        {
            Object[] args =
            {
                chatEngine.getHostAddress().getFullHostName()
            };
            frame.addInfoMessage( Localizer.getFormatedString( "ChatConnectionClosed",
                args) );
        }
    }
}