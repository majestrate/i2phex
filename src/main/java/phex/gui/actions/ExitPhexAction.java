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
 */
package phex.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.KeyStroke;

import phex.common.log.NLogger;
import phex.gui.common.DesktopIndicator;
import phex.gui.common.GUIRegistry;
import phex.gui.common.MainFrame;
import phex.gui.dialogs.CloseOptionsDialog;
import phex.gui.prefs.InterfacePrefs;
import phex.gui.prefs.PhexGuiPrefs;
import phex.prefs.core.PhexCorePrefs;
import phex.servent.Servent;
import phex.utils.Localizer;

public class ExitPhexAction extends FWAction
{
    public ExitPhexAction()
    {
        super( Localizer.getString( "Exit" ),
            GUIRegistry.getInstance().getPlafIconPack().getIcon( "Phex.Exit" ),
            Localizer.getString( "TTTExitPhex" ), Integer.valueOf(
            Localizer.getChar( "ExitMnemonic") ),
            KeyStroke.getKeyStroke( Localizer.getString( "ExitAccelerator" ) ) );
    }

    public void actionPerformed(ActionEvent e)
    {
        try
        {
            performCloseGUIAction();
        }
        catch ( Throwable th )
        {
            NLogger.error( ExitPhexAction.class, th, th );
        }
    }

    /**
     * Shortcut method for calls when no object reference is available.
     * Not nice but as long as we don't have a new clean global registry it helps out.
     */
    public static void performCloseGUIAction()
    {
        if( InterfacePrefs.ShowCloseOptionsDialog.get().booleanValue() )
        {
            CloseOptionsDialog dialog = new CloseOptionsDialog();
            dialog.setVisible( true );
            if ( !dialog.isOkActivated() )
            {// cancel close operation if ok was not activated.
                return;
            }
        }

        if( InterfacePrefs.MinimizeToBackground.get().booleanValue() )
        {
            minimizeToBackground();
        }
        else
        {
            shutdown();
        }
    }

    private static void minimizeToBackground()
    {
        GUIRegistry registry = GUIRegistry.getInstance();
        DesktopIndicator indicator = registry.getDesktopIndicator();
        MainFrame frame = registry.getMainFrame();

        // minimize...
        if ( frame.getState() != JFrame.ICONIFIED )
        {
            frame.setState( JFrame.ICONIFIED );
        }

        if ( indicator != null )
        {// systray support
            indicator.showIndicator();
            // hide
            frame.setVisible( false );
        }
    }

    /**
     * Shortcut method for calls when no object reference is available.
     */
    public static void shutdown()
    {
            /*//TODO reintegrate this warning message
            DownloadManager dm = ServiceManager.getDownloadManager();
            ShareManager sm = ServiceManager.getShareManager();
            HostManager hm = HostManager.getInstance();

            if ( dm.getDownloadingCount() > 0 ||
                 sm.getUploadFileContainer().getUploadFileCount() > 0 )
            {
                int	option = JOptionPane.showConfirmDialog(
                    mFrame,
                    "There are files being downloaded or uploaded.  Exit anyway?",
                    "Confirmation",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);

                if (option != JOptionPane.YES_OPTION)
                {
                    // Don't proceed.
                    return;
                }
            }*/

        // catch all possible exception to make 100% sure Phex is shutting down
        // even if there are errors.
        try
        {
            GUIRegistry.getInstance().getServent().stop();
            PhexCorePrefs.save( true );
            PhexGuiPrefs.save( true );
        }
        catch ( Exception exp )
        {
            NLogger.error( ExitPhexAction.class, exp, exp );
        }

        GUIRegistry registry = GUIRegistry.getInstance();
        try
        {
            // stop desktop indicator
            DesktopIndicator indicator = registry.getDesktopIndicator();
            if ( indicator != null )
            {
                indicator.hideIndicator();
                indicator.removeIndicator();
            }
        }
        catch ( Exception exp )
        {
            NLogger.error( ExitPhexAction.class, exp, exp );
        }
        try
        {
            registry.saveGUISettings();
        }
        catch ( Exception exp )
        {
            NLogger.error( ExitPhexAction.class, exp, exp );
        }
        registry.getMainFrame().dispose();
        
        System.exit( 0 );
    }

    @Override
    public void refreshActionState()
    {// global actions are not refreshed
    }
}