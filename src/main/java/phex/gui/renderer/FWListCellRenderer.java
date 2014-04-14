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
package phex.gui.renderer;

import java.awt.*;
import javax.swing.*;

/**
 * This class supports a workaround for the SkinLookAndFeel SkinComboBox UI
 * implementation.
 */
public class FWListCellRenderer extends DefaultListCellRenderer
{
    public Component getListCellRendererComponent( JList list,
        Object value, int index, boolean isSelected, boolean cellHasFocus )
    {
        super.getListCellRendererComponent( list, value, index,
            isSelected, cellHasFocus );
        // if index == -1 then we are painting the selected value (but not a value in the list)
        if (index == -1)
        {
            setOpaque(false);
        }
        else
        {
            setOpaque(true);
        }
        return this;
    }
}