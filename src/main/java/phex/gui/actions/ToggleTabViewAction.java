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
 *  $Id: ToggleTabViewAction.java 4365 2009-01-16 11:21:31Z gregork $
 */
package phex.gui.actions;

import java.awt.event.ActionEvent;

import phex.gui.common.GUIRegistry;
import phex.gui.tabs.FWTab;
import phex.utils.Localizer;


public class ToggleTabViewAction extends FWToggleAction
{
    private FWTab tab;

    public ToggleTabViewAction( FWTab aTab )
    {
        super( );
        tab = aTab;
        Object[] args = new Object[]
        {
            tab.getName()
        };
        setName( Localizer.getFormatedString( "ToggleTabAction", args ) );
        setSmallIcon( tab.getIcon() );
        putValue( MNEMONIC_KEY, Integer.valueOf( tab.getMnemonic() ) );
        putValue( ACCELERATOR_KEY, tab.getAccelerator() );
        setSelected( tab.getParent() != null );
        
        updateTooltip();
    }

    public void actionPerformed( ActionEvent e )
    {
        if ( tab.isShowing() || !tab.isDisplayable() )
        {
            boolean state = !isSelected();
            setSelected( state );
            GUIRegistry.getInstance().getMainFrame().setTabVisible( tab, state );
            updateTooltip();
        }
        else
        {
            boolean state = !isSelected();
            setSelected( state );
            setSelected( !state );
            GUIRegistry.getInstance().getMainFrame().setSelectedTab( tab );
        }
    }
    
    private void updateTooltip()
    {
        Object[] args = new Object[]
        {
            tab.getName()
        };
        if ( isSelected() )
        {
            setToolTipText( Localizer.getFormatedString( "TTTToggleTabActionHide", args ) );
        }
        else
        {
            setToolTipText( Localizer.getFormatedString( "TTTToggleTabActionShow", args ) );
        }
    }

    public void refreshActionState()
    {// global actions are not refreshed
        //setEnabled( ServiceManager.getNetworkManager().isNetworkJoined() );
    }
}