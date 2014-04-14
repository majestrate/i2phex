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
 *  --- CVS Information ---
 *  $Id: SwitchNetworkAction.java 4365 2009-01-16 11:21:31Z gregork $
 */
package phex.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import phex.gui.common.GUIRegistry;
import phex.gui.dialogs.SelectNetworkDialog;
import phex.utils.Localizer;


public class SwitchNetworkAction extends FWAction
{

    public SwitchNetworkAction( )
    {
        super( Localizer.getString( "SelectNetwork" ),
            GUIRegistry.getInstance().getPlafIconPack().getIcon( "Network.Join" ),
            Localizer.getString( "TTTSelectNetwork" ), Integer.valueOf(
            Localizer.getChar( "SelectNetworkMnemonic") ),
            KeyStroke.getKeyStroke( Localizer.getString( "SelectNetworkAccelerator" ) ) );
    }


    public void actionPerformed( ActionEvent event )
    {
        SelectNetworkDialog dialog = new SelectNetworkDialog();
        dialog.setVisible( true );
    }

    public void refreshActionState()
    {// global actions are not refreshed
        //setEnabled(!ServiceManager.getNetworkManager().isNetworkJoined() );
    }
}


