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
 *  --- CVS Information ---
 *  $Id: OpenURLAction.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.gui.actions;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import phex.common.log.NLogger;
import phex.gui.common.BrowserLauncher;
import phex.gui.common.GUIRegistry;
import phex.utils.Localizer;

public class OpenURLAction extends FWAction
{
    private String url;

    public OpenURLAction( String name, String url, Icon smallIcon,
        String toolTipText, Integer mnemonic, KeyStroke keyStroke )
    {
        super( name, smallIcon, toolTipText, mnemonic, keyStroke );
        this.url = url;
    }

    public void actionPerformed( ActionEvent e )
    {
        boolean hasError = false;
        try
        {
            BrowserLauncher.openURL( url );
        }
        catch ( UnsatisfiedLinkError error )
        {
            NLogger.error( OpenURLAction.class, error, error );
            hasError = true;
        }
        catch ( IOException exp )
        {
            NLogger.warn( OpenURLAction.class, exp, exp );
            hasError = true;
        }

        if ( hasError )
        {
            Object[] dialogOptions = new Object[]
            {
                Localizer.getString( "Yes" ),
                Localizer.getString( "No" )
            };

            Component comp = GUIRegistry.getInstance().getMainFrame().getSelectedTab();
            if ( comp == null )
            {
                comp = GUIRegistry.getInstance().getMainFrame().getRootPane();
            }
            int choice = JOptionPane.showOptionDialog( comp,
                Localizer.getString( "FailedToLaunchBrowserURLInClipboard" ),
                Localizer.getString( "FailedToLaunchBrowser" ),
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
                dialogOptions, Localizer.getString( "Yes" ) );
            if ( choice == 0 )
            {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection( url ), null);
            }
        }
    }


    @Override
    public void refreshActionState()
    {// global actions are not refreshed
        //setEnabled( true );
    }
}