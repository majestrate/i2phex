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
 *  --- SVN Information ---
 *  $Id: ColumnFactory.java 3807 2007-05-19 17:06:46Z gregork $
 */
package phex.gui.common.table;

//This is a modified version of the SwingLabs class
//org.jdesktop.swingx.table.ColumnFactory

/*
 * Id: ColumnFactory.java,v 1.13 2007/03/13 15:26:10 kleopatra Exp
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * Creates and configures <code>FWTableColumn</code>s.
 * 
 * <code>FWTable</code> delegates all <code>TableColumn</code> creation and
 * configuration to a <code>ColumnFactory</code>. Enhanced column
 * configuration should be implemented in a custom factory subclass.
 * 
 * Any instance of <code>FWTable</code> can be configured
 * individually with its own <code>ColumnFactory</code>.
 * 
 */
public class ColumnFactory
{
    /** the shared instance. */
    private static ColumnFactory columnFactory;
    /** the default margin to use in pack. */
    private int packMargin = 4;
    
    /**
     * Returns the shared default factory. 
     * 
     * @return the shared instance of <code>ColumnFactory</code>
     * @see #setInstance(ColumnFactory)
     */
    public static synchronized ColumnFactory getInstance() 
    {
        if (columnFactory == null) {
            columnFactory = new ColumnFactory();
        }
        return columnFactory;
    }

    /**
     * Sets the shared default factory. The shared instance is used
     * by <code>FWTable</code> if none has been set individually.
     * 
     * @param factory the default column factory.
     * @see #getInstance()
     */
    public static synchronized void  setInstance(ColumnFactory factory) 
    {
        columnFactory = factory;
    }

    /**
     * Creates and configures a TableColumnExt. <code>JXTable</code> calls
     * this method for each column in the <code>TableModel</code>.
     * 
     * @param model
     *            the TableModel to read configuration properties from
     * @param modelIndex
     *            column index in model coordinates
     * @return a TableColumnExt to use for the modelIndex
     * @throws NPE
     *             if model == null
     * @throws IllegalStateException
     *             if the modelIndex is invalid (in coordinate space of the
     *             tablemodel)
     * 
     * @see #createTableColumn(int)
     * @see #configureTableColumn(TableModel, TableColumnExt)
     * @see org.jdesktop.swingx.JXTable#createDefaultColumnsFromModel()
     */
    public FWTableColumn createAndConfigureTableColumn(TableModel model,
        int modelIndex)
    {
        FWTableColumn column = createTableColumn( modelIndex );
        configureTableColumn( model, column );
        return column;
    }

    /**
     * Creates a table column with modelIndex.
     * <p>
     * The factory's column creation is passed through this method, so
     * subclasses can override to return custom column types.
     * 
     * @param modelIndex
     *            column index in model coordinates
     * @return a TableColumnExt with <code>modelIndex</code>
     * 
     * @see #createAndConfigureTableColumn(TableModel, int)
     * 
     */
    public FWTableColumn createTableColumn( int modelIndex )
    {
        return new FWTableColumn( modelIndex );
    }

    /**
     * Configure column properties from TableModel. This implementation sets the
     * column's <code>headerValue</code> property from the model's
     * <code>columnName</code>.
     * <p>
     * 
     * The factory's initial column configuration is passed through this method,
     * so subclasses can override to customize.
     * <p>
     * 
     * @param model
     *            the TableModel to read configuration properties from
     * @param column
     *            the TableColumnExt to configure.
     * @throws NullPointerException
     *             if model or column == null
     * @throws IllegalStateException
     *             if column does not have valid modelIndex (in coordinate space
     *             of the tablemodel)
     * 
     * @see #createAndConfigureTableColumn(TableModel, int)
     */
    public void configureTableColumn( TableModel model, FWTableColumn column )
    {
        int modelIndex = column.getModelIndex();
        
        if ( (modelIndex < 0) || (modelIndex >= model.getColumnCount()) )
        {
            throw new IllegalStateException( "column must have valid modelIndex" );
        }
        
        column.setHeaderValue( model.getColumnName( modelIndex ) );
        
        FWTableModel fwModel = null;
        if ( model instanceof FWTableModel )
        {
            fwModel = (FWTableModel)model;
           
        }
        else if ( model instanceof FWSortedTableModel )
        {
            fwModel = ((FWSortedTableModel)model).getTableModel();
        }
        if ( fwModel != null )
        {
            Object columnId = fwModel.getColumnId( modelIndex );
            column.setIdentifier( columnId );
            column.setHideable( fwModel.isColumnHideable( modelIndex ) );
            column.setVisible( fwModel.isColumnDefaultVisible( modelIndex ) );
        }
    }

    /**
     * Configures column initial widths properties from <code>JXTable</code>.
     * This bare-bones implementation sets the column's
     * <code>preferredWidth</code> using it's <code>prototype</code>
     * property.
     * <p>
     * 
     * TODO JW - rename method to better convey what's happening, maybe
     * initializeColumnWidths like the old method in JXTable.
     * 
     * @param table
     *            the context the column will live in.
     * @param column
     *            the Tablecolumn to configure.
     * 
     * @see org.jdesktop.swingx.JXTable#getPreferredScrollableViewportSize()
     */
    public void configureColumnWidths( FWTable table, FWTableColumn column )
    {
        Dimension cellSpacing = table.getIntercellSpacing();
        Object prototypeValue = column.getPrototypeValue();
        if ( prototypeValue != null )
        {
            // calculate how much room the prototypeValue requires
            TableCellRenderer renderer = table.getCellRenderer( 0, table
                .convertColumnIndexToView( column.getModelIndex() ) );
            Component comp = renderer.getTableCellRendererComponent( table,
                prototypeValue, false, false, 0, 0 );
            int prefWidth = comp.getPreferredSize().width + cellSpacing.width;

            // now calculate how much room the column header wants
            renderer = column.getHeaderRenderer();
            if ( renderer == null )
            {
                JTableHeader header = table.getTableHeader();
                if ( header != null )
                {
                    renderer = header.getDefaultRenderer();
                }
            }
            if ( renderer != null )
            {
                comp = renderer.getTableCellRendererComponent( table, column
                    .getHeaderValue(), false, false, 0, table
                    .convertColumnIndexToView( column.getModelIndex() ) );

                prefWidth = Math.max( comp.getPreferredSize().width, prefWidth );
            }
            prefWidth += table.getColumnModel().getColumnMargin();
            column.setPreferredWidth( prefWidth );
        }
    }

    /**
     * Configures the column's <code>preferredWidth</code> to fit the content.
     * It respects the table context, a margin to add and a maximum width. This
     * is typically called in response to a user gesture to adjust the column's
     * width to the "widest" cell content of a column.
     * <p>
     * 
     * This implementation loops through all rows of the given column and
     * measures the renderers pref width (it's a potential performance sink).
     * Subclasses can override to implement a different strategy.
     * <p>
     * 
     * Note: though 2 * margin is added as spacing, this does <b>not</b> imply
     * a left/right symmetry - it's up to the table to place the renderer and/or
     * the renderer/highlighter to configure a border.
     * 
     * @param table
     *            the context the column will live in.
     * @param column
     *            the column to configure.
     * @param margin
     *            the extra spacing to add twice, if -1 uses this factories
     *            default
     * @param max
     *            an upper limit to preferredWidth, -1 is interpreted as no
     *            limit
     * 
     * @see #setDefaultPackMargin(int)
     * @see org.jdesktop.swingx.JXTable#packTable(int)
     * @see org.jdesktop.swingx.JXTable#packColumn(int, int)
     * 
     */
    public void packColumn( FWTable table, FWTableColumn column, int margin,
        int max)
    {

        /* Get width of column header */
        TableCellRenderer renderer = column.getHeaderRenderer();
        if ( renderer == null )
            renderer = table.getTableHeader().getDefaultRenderer();

        int viewIdx = table.convertColumnIndexToView( column.getModelIndex() );

        Component comp = renderer.getTableCellRendererComponent( table,
            column.getHeaderValue(), false, false, 0, viewIdx );
        int width = comp.getPreferredSize().width;

        if ( getRowCount( table ) > 0 )
            renderer = table.getCellRenderer( 0, viewIdx );
        for ( int r = 0; r < getRowCount( table ); r++ )
        {
            comp = renderer.getTableCellRendererComponent( table, table
                .getValueAt( r, viewIdx ), false, false, r, viewIdx );
            width = Math.max( width, comp.getPreferredSize().width );
        }
        if ( margin < 0 )
        {
            margin = getDefaultPackMargin();
        }
        width += 2 * margin;

        /* Check if the width exceeds the max */
        if ( max != -1 && width > max )
            width = max;

        column.setPreferredWidth( width );

    }

    /**
     * Returns the number of table view rows accessible during row-related
     * config. All row-related access is bounded by the value returned from this
     * method.
     * 
     * Here: delegates to table.getRowCount().
     * <p>
     * 
     * Subclasses can override to reduce the number (for performance) or support
     * restrictions due to lazy loading, f.i. Implementors must guarantee that
     * view row access with <code>0 <= row < getRowCount(JXTable)</code>
     * succeeds.
     * 
     * @param table
     *            the table to access
     * @return valid rowCount
     */
    protected int getRowCount( FWTable table )
    {
        return table.getRowCount();
    }

    // ------------------------ default state

    /**
     * Returns the default pack margin.
     * 
     * @return the default pack margin to use in packColumn.
     * 
     * @see #setDefaultPackMargin(int)
     */
    public int getDefaultPackMargin()
    {
        return packMargin;
    }

    /**
     * Sets the default pack margin.
     * <p>
     * 
     * Note: this is <b>not</b> really a margin in the sense of symmetrically
     * adding white space to the left/right of a cell's content. It's simply an
     * amount of space which is added twice to the measured widths in
     * packColumn.
     * 
     * @param margin
     *            the default marging to use in packColumn.
     * 
     * @see #getDefaultPackMargin()
     * @see #packColumn(JXTable, TableColumnExt, int, int)
     */
    public void setDefaultPackMargin(int margin)
    {
        this.packMargin = margin;
    }

}