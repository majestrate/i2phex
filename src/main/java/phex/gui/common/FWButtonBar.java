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
 *  Created on 01.02.2006
 *  --- CVS Information ---
 *  $Id: FWButtonBar.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.gui.common;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;

public class FWButtonBar extends JComponent
{
    private static final Dimension rigidDim = new Dimension( 2, 0 );
    private BoxLayout boxLayout;
    protected List<AbstractButton> buttons;

    public FWButtonBar()
    {
        super();
        buttons = new ArrayList<AbstractButton>();
        setDoubleBuffered(true);
        boxLayout = new BoxLayout( this, BoxLayout.LINE_AXIS );
        setLayout(boxLayout);
    }
    
    public void addButton( AbstractButton button )
    {
        buttons.add(button);
        updateLayout();
    }
    
    public void removeButton( AbstractButton button )
    {
        buttons.remove(button);
        updateLayout();
    }
    
    /**
     * 
     */
    private void updateLayout()
    {
        removeAll();
        Iterator<AbstractButton> iterator = buttons.iterator();
        while( iterator.hasNext() )
        {
            AbstractButton btn = iterator.next();
            add( btn );
            if ( iterator.hasNext() )
            {
                add( Box.createRigidArea( rigidDim ) );
            }
        }
        add( Box.createHorizontalGlue() );
        boxLayout.invalidateLayout( this );
        
        doLayout();
        revalidate();
        repaint();
    }
}
