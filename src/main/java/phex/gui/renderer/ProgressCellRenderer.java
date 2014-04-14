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
import javax.swing.border.*;
import javax.swing.table.TableCellRenderer;
import javax.accessibility.*;

/**
 * Supports progress bar in table cells.
 */
public class ProgressCellRenderer extends JProgressBar
    implements TableCellRenderer
{
    protected static final Border border = new EmptyBorder(2, 5, 2, 5);

    /**
     * Creates a progress cell renedere for percent. Max is 100, min is 0
     */
    public ProgressCellRenderer()
    {
        super( 0, 100 );
        setOpaque( true );
        //setBorderPainted( false );
        setBorder( border );
        setStringPainted( true );
    }

    /**
     * Paints the progress bar's border.
     *
     * @param g  the <code>Graphics</code> context within which to paint the border
     * @see #paint
     * @see #setBorder
     * @see #isBorderPainted
     * @see #setBorderPainted
     */
    /*protected void paintBorder(Graphics g)
    {
        // implementation of JComponent to force the paiting of the cell
        // renderer border even though isBorderPainted is false and the standard
        // JScrollPane border get not painted
        Border border = getBorder();
        if (border != null) {
            border.paintBorder(this, g, 0, 0, getWidth(), getHeight());
        }
    }*/

    // implements javax.swing.table.TableCellRenderer
    /**
     *
     * Returns the default table cell renderer.
     *
     * @param table  the <code>JTable</code>
     * @param value  the value to assign to the cell at
     *			<code>[row, column]</code>
     * @param isSelected true if cell is selected
     * @param isFocus true if cell has focus
     * @param row  the row of the cell to render
     * @param column the column of the cell to render
     * @return the default table cell renderer
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column)
    {
        if ( isSelected )
        {
            //super.setForeground( table.getSelectionForeground() );
            super.setBackground( table.getSelectionBackground() );
        }
        else
        {
            //super.setForeground( table.getForeground() );
            super.setBackground( table.getBackground() );
        }

        // ---- begin optimization to avoid painting background ----
        Color back = getBackground();
        boolean colorMatch = (back != null) && ( back.equals(table.getBackground()) ) && table.isOpaque();
        setOpaque(!colorMatch);
        // ---- end optimization to aviod painting background ----

        // displayed value handling
        if ( value instanceof Number )
        {
            Number progress = (Number) value;
            if ( progress.intValue() >= 0 )
            {
                setValue( progress.intValue() );
                setString( progress.toString() + " %");
            }
            else
            {
                setValue( 0 );
                setString( "? %");
            }
        }
        else
        {
           setValue( 0 );
           setString( "0 %" );
        }
        return this;
    }

    /*
     * The following methods are overridden as a performance measure to
     * to prune code-paths are often called in the case of renders
     * but which we know are unnecessary.  Great care should be taken
     * when writing your own renderer to weigh the benefits and
     * drawbacks of overriding methods like these.
     */

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void validate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void revalidate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void repaint(long tm, int x, int y, int width, int height) {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void repaint(Rectangle r) { }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        if (propertyName == AccessibleContext.ACCESSIBLE_VALUE_PROPERTY )
        {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { }

    /**
     * A subclass of <code>DefaultTableCellRenderer</code> that
     * implements <code>UIResource</code>.
     * <code>DefaultTableCellRenderer</code> doesn't implement
     * <code>UIResource</code>
     * directly so that applications can safely override the
     * <code>cellRenderer</code> property with
     * <code>DefaultTableCellRenderer</code> subclasses.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    public static class UIResource extends ProgressCellRenderer
        implements javax.swing.plaf.UIResource
    {
    }
}