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
 *  Created on 12.09.2006
 *  --- CVS Information ---
 *  $Id: UpdatePrefs.java 3807 2007-05-19 17:06:46Z gregork $
 */
package phex.gui.prefs;

import phex.prefs.api.PreferencesFactory;
import phex.prefs.api.Setting;

public class UpdatePrefs extends PhexGuiPrefs
{    
    /**
     * This is introduced to maintain the current version of Phex.
     * After a update of Phex the version in the cfg and the Phex version differs.
     * In this case we know that we need to upgrade the cfg or other stuff to the
     * new Phex version
     */
    public static final Setting<String> RunningPhexVersion;
    
    /**
     * This is introduced to maintain the current build number of Phex.
     * After a update of Phex the build number in the cfg and the Phex build number
     * differs. In this case we know that we need to upgrade the cfg and maybe
     * also do some other stuff to reach the new Phex version.
     */
    public static final Setting<String> RunningBuildNumber;
    
    public static final Setting<Boolean> ShowConfigWizard;
    
    static
    {
        RunningPhexVersion = PreferencesFactory.createStringSetting( 
            "Update.RunningPhexVersion", "", instance );
        RunningPhexVersion.setAlwaysSaved( true );
        
        RunningBuildNumber = PreferencesFactory.createStringSetting( 
            "Update.RunningBuildNumber", "", instance );
        RunningBuildNumber.setAlwaysSaved( true );
        
        ShowConfigWizard = PreferencesFactory.createBoolSetting( 
            "Update.ShowConfigWizard", false, instance );
    }
}
