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
 *  $Id: SortedTableHeaderRenderer.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.gui.common.table;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

import phex.gui.models.ISortableModel;


public class SortedTableHeaderRenderer extends DefaultTableCellRenderer
{
    private FWTable table;
    
    public SortedTableHeaderRenderer( FWTable table )
    {
        setHorizontalAlignment( JLabel.CENTER );
        this.table = table;
        setHorizontalTextPosition( JLabel.LEADING );
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column)
    {
        if (table != null)
        {
            JTableHeader header = table.getTableHeader();
            if (header != null)
            {
                setForeground(header.getForeground());
                setBackground(header.getBackground());
                setFont(header.getFont());
                ISortableModel model = (ISortableModel)table.getModel();
                int sortedColumn = table.convertColumnIndexToView(
                    model.getSortByColumn() );
                if ( sortedColumn == column )
                {
                    if ( model.isSortedAscending() )
                    {
                        setIcon( new DirectionIcon( false, getFont().getSize(),
                            0 ) );
                    }
                    else
                    {
                        setIcon( new DirectionIcon( true, getFont().getSize(),
                            0 ) );
                    }
                }
                else
                {
                    setIcon( null );
                }
            }
        }
        setText( (value == null) ? "" : value.toString() );
        setBorder( UIManager.getBorder("TableHeader.cellBorder") );
        return this;
    }
    
    private class DirectionIcon implements Icon
    {
        private boolean descending;
        private int size;
        private int priority;

        public DirectionIcon(boolean descending, int size, int priority) {
            this.descending = descending;
            this.size = size;
            this.priority = priority;
        }

        public void paintIcon(Component c, Graphics g, int x, int y)
        {
            Color color = c == null ? Color.gray : c.getBackground();             
            // In a compound sort, make each succesive triangle 20% 
            // smaller than the previous one. 
            int dx = (int)(size/2*Math.pow(0.8, priority));
            int dy = descending ? dx : -dx;
            // Align icon (roughly) with font baseline. 
            y = y + 5*size/7 + (descending ? -dy : 0);
            int shift = descending ? 1 : -1;
            g.translate(x, y);

            // Right diagonal. 
            g.setColor(color.darker());
            g.drawLine(dx / 2, dy, 0, 0);
            g.drawLine(dx / 2, dy + shift, 0, shift);
            
            // Left diagonal. 
            g.setColor(color.brighter());
            g.drawLine(dx / 2, dy, dx, 0);
            g.drawLine(dx / 2, dy + shift, dx, shift);
            
            // Horizontal line. 
            if (descending) 
            {
                g.setColor(color.darker().darker());
            } 
            else 
            {
                g.setColor(color.brighter().brighter());
            }
            g.drawLine(dx, 0, 0, 0);

            g.setColor(color);
            g.translate(-x, -y);
        }

        public int getIconWidth()
        {
            return size;
        }

        public int getIconHeight()
        {
            return size;
        }
    }
}