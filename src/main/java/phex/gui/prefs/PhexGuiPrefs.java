/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2005 Phex Development Group
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
 *  Created on 17.08.2006
 *  --- CVS Information ---
 *  $Id: PhexGuiPrefs.java 4360 2009-01-16 09:08:57Z gregork $
 */
package phex.gui.prefs;

import phex.Res;
import phex.common.Environment;
import phex.common.EnvironmentConstants;
import phex.prefs.OldCfg;
import phex.prefs.api.Preferences;
import phex.utils.StringUtils;

public class PhexGuiPrefs extends Preferences
{
    protected static final PhexGuiPrefs instance;
    static
    {
        instance = new PhexGuiPrefs();
    }

    protected PhexGuiPrefs( )
    {
        super( Environment.getInstance().getPhexConfigFile(
            EnvironmentConstants.GUI_PREFERENCES_FILE_NAME ) );
    }
    
    public static void init()
    {
        instance.load();
        instance.updatePreferences();
    }
    
    public static void save( boolean force )
    {
        if ( force )
        {
            instance.saveRequiredNotify();
        }
        instance.save();
    }
    
    
    public void updatePreferences()
    {
        // first find out if this is the first time Phex is running...
        if ( StringUtils.isEmpty( UpdatePrefs.RunningBuildNumber.get() ) ) 
        {
            // this seems to be the first time phex is running...
            // in this case we are not updating... we use default values...
            UpdatePrefs.ShowConfigWizard.set( Boolean.TRUE );
        }
        
        UpdatePrefs.RunningBuildNumber.set( 
            Environment.getInstance().getProperty( "build.number" ) );
        UpdatePrefs.RunningPhexVersion.set( Res.getStr( "Program.Version" ) );
    }

    public static void updatePreV30Config( OldCfg cfg )
    {
        InterfacePrefs.LocaleName.set( cfg.usedLocale != null ? cfg.usedLocale : "" );
        InterfacePrefs.MinimizeToBackground.set( Boolean.valueOf( cfg.minimizeToBackground ) );
        InterfacePrefs.ShowCloseOptionsDialog.set( Boolean.valueOf( cfg.showCloseOptionsDialog ) );
        InterfacePrefs.ShowBetaUpdateNotification.set( Boolean.valueOf( cfg.showBetaUpdateNotification ) );
        
        NetworkTabPrefs.ConnectToHistory.set( cfg.connectToHistory );
        NetworkTabPrefs.MaxConnectToHistorySize.set( Integer.valueOf( cfg.maxConnectToHistorySize ) );
        NetworkTabPrefs.HostErrorDisplayTime.set( Integer.valueOf( cfg.hostErrorDisplayTime ) );
        
        SearchTabPrefs.BrowseHostHistory.set( cfg.browseHostHistory );
        SearchTabPrefs.SearchTermHistory.set( cfg.searchTermHistory );
        SearchTabPrefs.MaxSearchHistorySize.set( Integer.valueOf( cfg.maxSearchTermHistorySize ) );
        
        UpdatePrefs.ShowConfigWizard.set( Boolean.TRUE );
    }
}
