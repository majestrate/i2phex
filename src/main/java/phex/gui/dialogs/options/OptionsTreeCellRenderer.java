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
package phex.gui.dialogs.options;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import phex.utils.Localizer;

public class OptionsTreeCellRenderer extends DefaultTreeCellRenderer
{
    public OptionsTreeCellRenderer()
    {
        super();
        // don't have the strange icons displayed
        setClosedIcon( null );
        setLeafIcon( null );
        setOpenIcon( null );
    }

    public Component getTreeCellRendererComponent( JTree tree, Object value,
        boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
    {
        super.getTreeCellRendererComponent( tree, value, selected, expanded,
            leaf, row, hasFocus );

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object obj = node.getUserObject();
        if ( obj instanceof OptionsSettingsPane )
        {
            OptionsSettingsPane pane = (OptionsSettingsPane)obj;
            String str = pane.getOptionTreeRepresentation();
            setText( Localizer.getString( str ) );
        }
        else
        {
            setText( value.toString() );
        }
        return this;
    }
}