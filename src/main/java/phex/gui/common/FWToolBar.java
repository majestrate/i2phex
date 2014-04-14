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

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import phex.gui.actions.FWAction;
import phex.gui.actions.FWToggleAction;

/**
 *
 */
public class FWToolBar extends JToolBar
{
    private boolean isTextShown;

    public FWToolBar( int orientation )
    {
        super( orientation );
        putClientProperty( "JToolBar.isRollover", Boolean.TRUE );
        
//        if ( Environment.getInstance().isJava14orLater() )
//        {
//            try
//            {
//                Method method = getClass().getMethod( "setRollover", new Class[]{boolean.class } );
//                method.invoke( this, new Object[]{Boolean.TRUE} );
//            }
//            catch ( Exception exp )
//            {
//                Logger.logWarning( exp );
//            }
//        }
        isTextShown = true;
    }

    public void setShowText( boolean state )
    {
        isTextShown = state;
    }
    

    /**
     * make the button in the menu bar look like we like to have it
     * and not like the swing implementation wants it
     */
    public AbstractButton addAction( FWAction action  )
    {
        return addAction( (Action) action, action.isToggleAction() );
    }

    public AbstractButton addAction( Action action, boolean isToggleAction )
    {
        AbstractButton btn;
        if ( isToggleAction )
        {
            btn = new JToggleButton();
            btn.setSelected( ((FWToggleAction)action).isSelected() );
        }
        else
        {
            btn = new JButton();
        }
        
        //btn.setAction( action );
        
        Icon icon = (Icon) action.getValue( FWAction.MEDIUM_ICON );
        if (icon == null)
        {
            icon = (Icon) action.getValue( FWAction.SMALL_ICON );
        }
        btn.setIcon(icon);

        btn.setHorizontalTextPosition( JButton.RIGHT );
        btn.setVerticalTextPosition( JButton.BOTTOM );
        btn.setEnabled( action.isEnabled() );
        btn.addActionListener( action );
        registerActionChangeListener( btn, action );

        btn.setToolTipText( (String)action.getValue( FWAction.TOOL_TIP_TEXT ) );
        if ( isTextShown )
        {
            btn.setText( (String)action.getValue( FWAction.NAME ) );
        }
        btn.setRequestFocusEnabled( false );
        btn.setMargin( GUIUtils.EMPTY_INSETS );
        
        add( btn );
        return btn;
    }

    // TODO3 if btn needs to be removed... unregister!
    private void registerActionChangeListener(AbstractButton b, Action a)
    {
        PropertyChangeListener actionPropertyChangeListener = new ActionChangedListener(b);
        a.addPropertyChangeListener(actionPropertyChangeListener);
    }

    private class ActionChangedListener implements PropertyChangeListener
    {
        AbstractButton button;

        ActionChangedListener(AbstractButton b)
        {
            super();
            button = b;
        }

        public void propertyChange(PropertyChangeEvent e)
        {
            String propertyName = e.getPropertyName();
            if ( propertyName.equals(Action.NAME))
            {
                String text = (String) e.getNewValue();
                button.setText(text);
                button.repaint();
            }
            else if ( propertyName.equals("enabled"))
            {
                Boolean enabledState = (Boolean) e.getNewValue();
                button.setEnabled(enabledState.booleanValue());
                button.repaint();
            }
            else if ( propertyName.equals( FWAction.MEDIUM_ICON )  )
            {
                Icon icon = (Icon) e.getNewValue();
                button.setIcon(icon);
                button.invalidate();
                button.repaint();
            }
            else if ( propertyName.equals( FWToggleAction.IS_SELECTED )  )
            {
                Boolean state = (Boolean) e.getNewValue();
                button.setSelected( state.booleanValue() );
                button.repaint();
            }
            else if ( propertyName.equals( FWToggleAction.TOOL_TIP_TEXT )  )
            {
                String state = (String) e.getNewValue();
                button.setToolTipText( state );
                button.repaint();
            }
        }
    }
}