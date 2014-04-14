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
 *  $Id: ToggleStatusbarAction.java 3807 2007-05-19 17:06:46Z gregork $
 */
package phex.gui.actions;

import java.awt.event.ActionEvent;

import phex.gui.common.GUIRegistry;
import phex.utils.Localizer;

/**
 * 
 */
public class ToggleStatusbarAction extends FWToggleAction
{
    /**
     *
     */
    public ToggleStatusbarAction( boolean isStatusbarVisible )
    {
        super( Localizer.getString( "ToggleStatusbarAction" ),
               null, null, null,  null, null );
        setSelected( isStatusbarVisible );
        updateTooltip();
    }
    
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        boolean state = !isSelected();
        setSelected( state );
        GUIRegistry.getInstance().getMainFrame().setStatusbarVisible( state );
        updateTooltip();
    }
    
    private void updateTooltip()
    {
        if ( isSelected() )
        {
            setToolTipText( Localizer.getString( "TTTToggleStatusbarActionHide" ) );
        }
        else
        {
            setToolTipText( Localizer.getString( "TTTToggleStatusbarActionShow" ) );
        }
    }
    
    /**
     * @see phex.gui.actions.FWAction#refreshActionState()
     */
    public void refreshActionState()
    {// global actions are not refreshed
    }
}
