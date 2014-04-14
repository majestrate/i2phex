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
 *  Created on 29.10.2006
 *  --- CVS Information ---
 *  $Id: NetworkTabPrefs.java 3807 2007-05-19 17:06:46Z gregork $
 */
package phex.gui.prefs;

import java.util.List;

import phex.prefs.api.PreferencesFactory;
import phex.prefs.api.Setting;

public class NetworkTabPrefs extends PhexGuiPrefs
{
    /**
     * The number of milliseconds a error is displayed in the connection table.
     */
    public static final Setting<Integer> HostErrorDisplayTime;    

    /**
     * History of connectTo items.
     */
    public static final Setting<List<String>> ConnectToHistory;

    /**
     * The max size of the connectToHistory list.
     */
    public static final Setting<Integer> MaxConnectToHistorySize;
    
    static
    {
        HostErrorDisplayTime = PreferencesFactory.createIntSetting(  
            "NetworkTab.HostErrorDisplayTime", 2000, instance );
        
        ConnectToHistory = PreferencesFactory.createListSetting(
            "NetworkTab.ConnectToHistory", instance );
        
        MaxConnectToHistorySize = PreferencesFactory.createIntSetting(  
            "NetworkTab.MaxConnectToHistorySize", 10, instance );
    }
}
