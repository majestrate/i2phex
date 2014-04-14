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
 */
package phex.gui.common.menubar;


import java.awt.event.ActionEvent;

import phex.gui.actions.FWAction;
import phex.gui.actions.GUIActionPerformer;
import phex.gui.common.GUIRegistry;
import phex.utils.Localizer;

public class RescanSharedFilesAction extends FWAction
{
    public RescanSharedFilesAction( )
    {
        super( Localizer.getString( "RescanSharedFiles" ), 
            GUIRegistry.getInstance().getPlafIconPack().getIcon(
            "MenuBar.Settings.Rescan"),
            Localizer.getString( "TTTRescanSharedFiles" ),
            Integer.valueOf( Localizer.getChar( "RescanSharedFilesMnemonic" ) ),
            null );
    }

    public void actionPerformed( ActionEvent event )
    {
        GUIActionPerformer.rescanSharedFiles();
    }

    public void refreshActionState()
    {// global actions are not refreshed
        //setEnabled( true );
    }
}


