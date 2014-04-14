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
 *  Created on 13.06.2005
 *  --- CVS Information ---
 *  $Id: GUIUserMessageListener.java 3943 2007-09-29 17:08:27Z gregork $
 */
package phex.gui.common;

import java.awt.EventQueue;

import phex.event.UserMessageListener;
import phex.utils.Localizer;

/**
 *
 */
public class GUIUserMessageListener implements UserMessageListener
{

    private static final long DEFAULT_AUTO_CLOSE_DELAY = 30 * 1000;

    /**
     * @see phex.event.UserMessageListener#displayUserMessage(String, String[])
     */
    public void displayUserMessage( final String userMessageId, final String[] args )
    {
        EventQueue.invokeLater( new Runnable()
            {
                public void run()
                {
                    String title = determineUserMsgTitle(userMessageId);
                    String shortMessage = determineUserMsgShortMessage(userMessageId, args);
                    long autoCloseDelay = determineUserMsgAutoCloseDelay(userMessageId);
                    
                    SlideInWindow window = new SlideInWindow( title, autoCloseDelay );
                    window.setShortMessage(shortMessage, false);
                    window.initializeComponent();
                    window.slideIn( );
                }
            });
    }
    
    private String determineUserMsgTitle( String userMessageId )
    {
        return Localizer.getString( "UserMsg_" + userMessageId + "_Title" ); 
    }
    
    private String determineUserMsgShortMessage( String userMessageId, String ... args )
    {
        if ( args != null && args.length > 0 )
        {
            return Localizer.getFormatedString( 
                "UserMsg_" + userMessageId + "_ShortMessage", (Object[])args );
        }
        else
        {
            return Localizer.getString( "UserMsg_" + userMessageId + "_ShortMessage" );
        }
    }
    
    private long determineUserMsgAutoCloseDelay( String userMessageId )
    {
        try
        {
            return Long.parseLong( Localizer.getString( 
                "UserMsg_" + userMessageId + "_AutoCloseDelay" ) );
        }
        catch ( NumberFormatException exp )
        {
            return DEFAULT_AUTO_CLOSE_DELAY;
        }
    }
}