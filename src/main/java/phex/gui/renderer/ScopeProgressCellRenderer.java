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

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;

import javax.accessibility.AccessibleContext;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import phex.download.swarming.SWDownloadCandidate;
import phex.gui.common.progressbar.CandidateScopeProvider;
import phex.gui.common.progressbar.MultiScopeProgressBar;

/**
 * Supports progress bar in table cells.
 */
public class ScopeProgressCellRenderer extends MultiScopeProgressBar
    implements TableCellRenderer
{
    protected static final Border border = new EmptyBorder(2, 5, 2, 5);
    
    private CandidateScopeProvider scopeProvider;

    /**
     * Creates a progress cell renedere for percent. Max is 100, min is 0
     */
    public ScopeProgressCellRenderer()
    {
        super( );
        setOpaque( true );
        setBorder( new CompoundBorder( border, getBorder() ) );
        scopeProvider = new CandidateScopeProvider();
    }

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
            super.setBackground( table.getSelectionBackground() );
        }
        else
        {
            super.setBackground( table.getBackground() );
        }

        // ---- begin optimization to avoid painting background ----
        Color back = getBackground();
        boolean colorMatch = (back != null) && ( back.equals(table.getBackground()) ) && table.isOpaque();
        setOpaque(!colorMatch);
        // ---- end optimization to aviod painting background ----

        // displayed value handling
        SWDownloadCandidate candidate = (SWDownloadCandidate) value;
        scopeProvider.setCandidate( candidate );
        setProvider( scopeProvider );
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
    public static class UIResource extends ScopeProgressCellRenderer
        implements javax.swing.plaf.UIResource
    {
    }
}