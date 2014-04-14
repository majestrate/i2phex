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
package phex.gui.common;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import phex.gui.actions.FWAction;
import phex.gui.actions.FWToggleAction;

public class FWMenu extends JMenu
{
    public FWMenu( String name )
    {
        super( name );
    }

    public JMenuItem add( Action a )
    {
        throw new UnsupportedOperationException( "Use addAction( )" );
    }

    public JMenuItem addAction( FWAction action )
    {
        return addAction( action, action.isToggleAction() );
    }

    public JMenuItem addAction( Action a, boolean isToogleAction )
    {
        JMenuItem mi = createActionComponent( a, isToogleAction );
        mi.setAction(a);
        add(mi);
        return mi;
    }

    protected JMenuItem createActionComponent( Action action, boolean isToogleAction )
    {
        JMenuItem mi;
        if ( isToogleAction )
        {
            mi = new JCheckBoxMenuItem( ( String )action.getValue( Action.NAME ),
                ( Icon )action.getValue( Action.SMALL_ICON ) )
            {
                protected PropertyChangeListener createActionPropertyChangeListener(
                    Action a )
                {
                    PropertyChangeListener pcl = createActionChangeListener( this );
                    if ( pcl == null )
                    {
                        pcl = super.createActionPropertyChangeListener( a );
                    }
                    return pcl;
                }
            };
            mi.setSelected( ((FWToggleAction)action).isSelected() );
            registerActionChangeListener( mi, action );
            mi.setHorizontalTextPosition( JButton.TRAILING );
            mi.setVerticalTextPosition( JButton.CENTER );
            mi.setEnabled( action.isEnabled() );
        }
        else
        {
            mi = createActionComponent( action );
        }
        return mi;
    }

    private void registerActionChangeListener( JMenuItem mi, Action a )
    {
        PropertyChangeListener actionPropertyChangeListener = new ActionChangedListener(mi);
        a.addPropertyChangeListener(actionPropertyChangeListener);
    }

    private class ActionChangedListener implements PropertyChangeListener
    {
        JMenuItem menuItem;

        ActionChangedListener( JMenuItem mi )
        {
            super();
            menuItem = mi;
        }

        public void propertyChange(PropertyChangeEvent e)
        {
            String propertyName = e.getPropertyName();
            if ( propertyName.equals(Action.NAME))
            {
                String text = (String) e.getNewValue();
                menuItem.setText(text);
                menuItem.repaint();
            }
            else if ( propertyName.equals("enabled"))
            {
                Boolean enabledState = (Boolean) e.getNewValue();
                menuItem.setEnabled(enabledState.booleanValue());
                menuItem.repaint();
            }
            else if ( propertyName.equals( FWAction.MEDIUM_ICON )  )
            {
                Icon icon = (Icon) e.getNewValue();
                menuItem.setIcon(icon);
                menuItem.invalidate();
                menuItem.repaint();
            }
            else if ( propertyName.equals( FWToggleAction.IS_SELECTED )  )
            {
                Boolean state = (Boolean) e.getNewValue();
                menuItem.setSelected( state.booleanValue() );
                menuItem.repaint();
            }
        }
    }

}