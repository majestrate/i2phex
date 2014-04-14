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
 *  Created on 17.03.2006
 *  --- CVS Information ---
 *  $Id: MainMenuBar.java 4365 2009-01-16 11:21:31Z gregork $
 */
package phex.gui.common.menubar;

import java.util.Iterator;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import phex.gui.actions.*;
import phex.gui.common.*;
import phex.gui.tabs.FWTab;
import phex.utils.Localizer;
import phex.xml.sax.gui.DGuiSettings;

public class MainMenuBar extends JMenuBar
{
    private FWMenu viewMenu;
    private ToggleToolbarAction toggleToolbarAction;
    private ToggleStatusbarAction toggleStatusbarAction;
    private JMenuItem phexHomeLink;
    private JMenuItem phexForumLink;

    public MainMenuBar( MainFrame mainFrame, DGuiSettings dSettings )
    {
        super();

        JMenu networkMenu = new JMenu( Localizer.getString( "Network" ) );
        networkMenu.setMnemonic( Localizer.getChar( "NetworkMnemonic" ) );
        
        FWAction action;
        GUIRegistry guiRegistry = GUIRegistry.getInstance();
        FWMenu newMenu = new FWMenu( Localizer.getString( "New" ));
        action = guiRegistry.getGlobalAction(
            GUIRegistryConstants.NEW_DOWNLOAD_ACTION );
        newMenu.addAction( action );
        networkMenu.add( newMenu );
        action = guiRegistry.getGlobalAction( "ConnectNetworkAction" );
        networkMenu.add( action );
        action = guiRegistry.getGlobalAction( "DisconnectNetworkAction" );
        networkMenu.add( action );
        networkMenu.addSeparator();
        action = guiRegistry.getGlobalAction( "SwitchNetworkAction" );
        networkMenu.add( action );
        networkMenu.addSeparator();
        action = guiRegistry.getGlobalAction( "ExitPhexAction" );
        networkMenu.add( action );
        add( networkMenu );

        viewMenu = new FWMenu( Localizer.getString( "View" ) );
        viewMenu.addMenuListener( new ViewMenuListener() );
        viewMenu.setMnemonic( Localizer.getChar( "ViewMnemonic" ) );
        add( viewMenu );
        
        boolean isToolbarVisible = true;
        if ( dSettings != null && dSettings.isSetToolbarVisible() )
        {
            isToolbarVisible = dSettings.isToolbarVisible();
        }
        toggleToolbarAction = new ToggleToolbarAction( isToolbarVisible );
        
        boolean isStatusbarVisible = true;
        if ( dSettings != null && dSettings.isSetStatusbarVisible() )
        {
            isStatusbarVisible = dSettings.isStatusbarVisible();
        }
        toggleStatusbarAction = new ToggleStatusbarAction( isStatusbarVisible );

        FWMenu settingsMenu = new FWMenu( Localizer.getString( "Settings" ) );
        settingsMenu.setMnemonic( Localizer.getChar( "SettingsMnemonic" ) );
        action = new ViewOptionsAction();
        settingsMenu.addAction( action );
        settingsMenu.addSeparator();
        action = new FilteredPortsAction();
        settingsMenu.addAction( action );
        settingsMenu.addSeparator();
        action = new RescanSharedFilesAction();
        settingsMenu.addAction( action );
        add( settingsMenu );

        
        FWMenu helpMenu = new FWMenu( Localizer.getString( "Help" ) );
        helpMenu.setMnemonic( Localizer.getChar( "HelpMnemonic" ) );
        phexHomeLink = helpMenu.addAction( new OpenURLAction(
            Localizer.getString("PhexHomepage" ), 
            "http://www.phex.org", null,
            Localizer.getString( "TTTPhexHomepage" ),
            Integer.valueOf( Localizer.getChar( "PhexHomepageMnemonic" ) ), null ) );
        phexForumLink = helpMenu.addAction( new OpenURLAction( 
            Localizer.getString( "PhexForum" ), 
            "http://www.gnutellaforums.com/forumdisplay.php?s=&forumid=16", null,
            Localizer.getString( "TTTPhexForum" ), 
            Integer.valueOf( Localizer.getChar( "PhexForumMnemonic" ) ), null ) );
        helpMenu.addSeparator();
        helpMenu.addAction( new ViewAboutAction( ) );
        add( helpMenu );
        
        setupIcons();
    }
    
    private void setupIcons()
    {
        IconPack plafIconPack = GUIRegistry.getInstance().getPlafIconPack();
        phexHomeLink.setIcon( plafIconPack.getIcon( "MenuBar.Help.Link" ) );
        phexForumLink.setIcon( plafIconPack.getIcon( "MenuBar.Help.Link" ) );
    }
    
    private final class ViewMenuListener implements MenuListener
    {
        public void menuCanceled( MenuEvent e )
        {
            viewMenu.removeAll();
        }

        public void menuDeselected( MenuEvent e )
        {
            viewMenu.removeAll();
        }

        public void menuSelected( MenuEvent e )
        {
            MainFrame mainFrame = GUIRegistry.getInstance().getMainFrame();
            FWTab tab = mainFrame.getSelectedTab();
            if ( tab != null )
            {
                List list = tab.getViewMenuActions();
                if ( list != null && list.size() > 0 )
                {
                    Iterator iterator = list.iterator();
                    while( iterator.hasNext() )
                    {
                        FWAction action = (FWAction) iterator.next();
                        viewMenu.addAction(action);
                    }
                    viewMenu.addSeparator();
                }
            }
            
            viewMenu.addAction( toggleToolbarAction );
            viewMenu.addAction( toggleStatusbarAction );
            viewMenu.addSeparator();
            
            tab = mainFrame.getTab( MainFrame.NETWORK_TAB_ID );
            viewMenu.addAction( tab.getToggleTabViewAction() );
            tab = mainFrame.getTab( MainFrame.SEARCH_TAB_ID );
            viewMenu.addAction( tab.getToggleTabViewAction() );
            tab = mainFrame.getTab( MainFrame.DOWNLOAD_TAB_ID );
            viewMenu.addAction( tab.getToggleTabViewAction() );
            tab = mainFrame.getTab( MainFrame.UPLOAD_TAB_ID );
            viewMenu.addAction( tab.getToggleTabViewAction() );
            tab = mainFrame.getTab( MainFrame.LIBRARY_TAB_ID );
            viewMenu.addAction( tab.getToggleTabViewAction() );
            tab = mainFrame.getTab( MainFrame.SECURITY_TAB_ID );
            viewMenu.addAction( tab.getToggleTabViewAction() );
            tab = mainFrame.getTab( MainFrame.STATISTICS_TAB_ID );
            viewMenu.addAction( tab.getToggleTabViewAction() );
            tab = mainFrame.getTab( MainFrame.SEARCH_MONITOR_TAB_ID );
            viewMenu.addAction( tab.getToggleTabViewAction() );
            tab = mainFrame.getTab( MainFrame.RESULT_MONITOR_TAB_ID );
            viewMenu.addAction( tab.getToggleTabViewAction() );
        }
    }
}
