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
 *  $Id: InterfacePrefs.java 4064 2007-11-29 22:57:54Z complication $
 */
package phex.gui.prefs;

import phex.prefs.api.PreferencesFactory;
import phex.prefs.api.Setting;

public class InterfacePrefs extends PhexGuiPrefs
{
    /**
     * The locale files to use.
     */
    public static final Setting<String> LocaleName;
    
    /**
     * Indicates if tooltips are shown. 
     */
    public static final Setting<Boolean> DisplayTooltip;
    
    /**
     * Indicates whether Phex minimizes to the background on close of the GUI. If set to
     * false it will shutdown. If set to true on windows system it will go into the
     * sys tray, on all other system it will just minimize.
     */
    public static final Setting<Boolean> MinimizeToBackground;

    /**
     * The status if a close options dialog should be displayed or not.
     */
    public static final Setting<Boolean> ShowCloseOptionsDialog;
    
    /**
     * The status if a beta update notification dialog should be displayed or not.
     */
    public static final Setting<Boolean> ShowBetaUpdateNotification;
    
    /**
     * The interval in millis at which the gui gets updated.
     */
    public static final Setting<Integer> GuiUpdateInterval;
    
    
    
    static
    {
        LocaleName = PreferencesFactory.createStringSetting(  
            "Interface.LocaleName", "", instance );
        DisplayTooltip = PreferencesFactory.createBoolSetting(  
            "Interface.DisplayTooltip", true, instance );
        MinimizeToBackground = PreferencesFactory.createBoolSetting(  
            "Interface.MinimizeToBackground", true, instance );
        ShowCloseOptionsDialog = PreferencesFactory.createBoolSetting(  
            "Interface.ShowCloseOptionsDialog", true, instance );
        ShowBetaUpdateNotification = PreferencesFactory.createBoolSetting(  
            "Interface.ShowBetaUpdateNotification", false, instance );
        GuiUpdateInterval = PreferencesFactory.createIntSetting(  
            "Interface.GuiUpdateInterval", 2000, instance );
    }
}
