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
 */
package phex.gui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import phex.common.log.NLogger;

/**
 * The standard class for rendering (displaying) individual cells
 * in a <code>JTable</code>.
 * <p>
 *
 * <strong><a name="override">Implementation Note:</a></strong>
 * This class inherits from <code>JLabel</code>, a standard component class.
 * However <code>JTable</code> employs a unique mechanism for rendering
 * its cells and therefore requires some slightly modified behavior
 * from its cell renderer.
 * The table class defines a single cell renderer and uses it as a
 * as a rubber-stamp for rendering all cells in the table;
 * it renders the first cell,
 * changes the contents of that cell renderer,
 * shifts the origin to the new location, re-draws it, and so on.
 * The standard <code>JLabel</code> component was not
 * designed to be used this way and we want to avoid
 * triggering a <code>revalidate</code> each time the
 * cell is drawn. This would greatly decrease performance because the
 * <code>revalidate</code> message would be
 * passed up the hierarchy of the container to determine whether any other
 * components would be affected.  So this class
 * overrides the <code>validate</code>, <code>revalidate</code>,
 * <code>repaint</code>, and <code>firePropertyChange</code> methods to be
 * no-ops.  If you write your own renderer,
 * please keep this performance consideration in mind.
 * <p>
 * @see JTable
 */
public class FWTableCellRenderer
    extends JLabel
    implements TableCellRenderer
{
    protected static final Border noFocusBorder = new EmptyBorder( 1, 1, 1, 1 );

    // We need a place to store the color the JLabel should be returned
    // to after its foreground and background colors have been set
    // to the selection background color.
    // These ivars will be made protected when their names are finalized.
    private Color defaultUnselectedForeground;
    private Color defaultUnselectedBackground;

    /**
     * Creates a default table cell renderer.
     */
    public FWTableCellRenderer()
    {
        super();
        setOpaque( true );
        setBorder( noFocusBorder );
    }

    /**
     * Overrides <code>JComponent.setForeground</code> to assign
     * the unselected-foreground color to the specified color.
     *
     * @param c set the foreground color to this value
     */
    public void setDefaultForeground( Color c )
    {
        super.setForeground( c );
        defaultUnselectedForeground = c;
    }

    /**
     * Overrides <code>JComponent.setBackground</code> to assign
     * the unselected-background color to the specified color.
     *
     * @param c set the background color to this value
     */
    public void setDefaultBackground( Color c )
    {
        super.setBackground( c );
        defaultUnselectedBackground = c;
    }

    /**
     * Notification from the <code>UIManager</code> that the look and feel
     * [L&F] has changed.
     * Replaces the current UI object with the latest version from the
     * <code>UIManager</code>.
     *
     * @see JComponent#updateUI
     */
    public void updateUI()
    {
        super.updateUI();
        setDefaultForeground( null );
        setDefaultBackground( null );
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
    public Component getTableCellRendererComponent( JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column )
    {
        try
        {
            if ( isSelected )
            {
                super.setForeground( table.getSelectionForeground() );
                super.setBackground( table.getSelectionBackground() );
            }
            else
            {
                super.setForeground( ( defaultUnselectedForeground != null ) ?
                    defaultUnselectedForeground : table.getForeground() );
                super.setBackground( ( defaultUnselectedBackground != null ) ?
                    defaultUnselectedBackground : table.getBackground() );
            }
    
            setFont( table.getFont() );
    
            if ( hasFocus )
            {
                setBorder( UIManager.getBorder( "Table.focusCellHighlightBorder" ) );
                if ( table.isCellEditable( row, column ) )
                {
                    super.setForeground( UIManager.getColor(
                        "Table.focusCellForeground" ) );
                    super.setBackground( UIManager.getColor(
                        "Table.focusCellBackground" ) );
                }
            }
            else
            {
                setBorder( noFocusBorder );
            }
    
            setValue( value );
        }
        catch ( Throwable th )
        {
            NLogger.error( FWTableCellRenderer.class, th, th );
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
    public boolean isOpaque()
    {
        Color back = getBackground();
        Component p = getParent();
        if ( p != null )
        {
            p = p.getParent();
        }
        // p should now be the JTable.
        boolean colorMatch = ( back != null ) && ( p != null ) &&
            back.equals( p.getBackground() ) &&
            p.isOpaque();
        return!colorMatch && super.isOpaque();
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void validate()
    {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void revalidate()
    {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void repaint( long tm, int x, int y, int width, int height )
    {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void repaint( Rectangle r )
    {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    protected void firePropertyChange( String propertyName, Object oldValue,
        Object newValue )
    {
        // Strings get interned...
        if ( propertyName == "text" )
        {
            super.firePropertyChange( propertyName, oldValue, newValue );
        }
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void firePropertyChange( String propertyName, boolean oldValue,
        boolean newValue )
    {}


    /**
     * Sets the <code>String</code> object for the cell being rendered to
     * <code>value</code>.
     *
     * @param value  the string value for this cell; if value is
     *		<code>null</code> it sets the text value to an empty string
     * @see JLabel#setText
     *
     */
    protected void setValue( Object value )
    {
        setText( ( value == null ) ? "" : value.toString() );
    }


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
     * future Swing releases. The current serialization support is
     * appropriate for short term storage or RMI between applications running
     * the same version of Swing.  As of 1.4, support for long term storage
     * of all JavaBeans<sup><font size="-2">TM</font></sup>
     * has been added to the <code>java.beans</code> package.
     * Please see {@link java.beans.XMLEncoder}.
     */
    public static class UIResource
        extends DefaultTableCellRenderer
        implements javax.swing.plaf.UIResource
    {
    }

}
