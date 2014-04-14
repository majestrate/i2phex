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
 *  Created on 24.11.2005
 *  --- CVS Information ---
 *  $Id: ConditionCellRenderer.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.gui.dialogs.filter.wizard.condition;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;

import phex.gui.dialogs.filter.ConditionVisualizer;
import phex.rules.condition.Condition;

public class ConditionCellRenderer extends DefaultTableCellRenderer
{
    public ConditionCellRenderer()
    {
        super( );
    }
    
    /**
     * Sets the <code>String</code> object for the cell being rendered to
     * <code>value</code>.
     * 
     * @param value  the string value for this cell; if value is
     *      <code>null</code> it sets the text value to an empty string
     * @see JLabel#setText
     * 
     */
    protected void setValue(Object value)
    {
        setText( (value == null) ? "" : ConditionVisualizer.buildCleanDisplayString( (Condition)value) );
    }    
}
