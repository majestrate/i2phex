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
 *  $Id: FWTable.java 4064 2007-11-29 22:57:54Z complication $
 */
package phex.gui.common.table;

//This class contains source from the SwingLabs class
//org.jdesktop.swingx.JXTable

import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;

import phex.common.log.NLogger;
import phex.gui.common.GUIRegistry;
import phex.gui.renderer.DefaultPhexCellRenderers;

public class FWTable extends JTable
{
    /** the factory to use for column creation and configuration. */
    private ColumnFactory columnFactory;
    
    private int visibleRowCount = 18;
    private boolean isColumnResizeToFitEnabled;
    private boolean isColumnSortingEnabled;
    private JPopupMenu headerPopup;

    public FWTable( TableModel dataModel, FWTableColumnModel columnModel )
    {
        super( dataModel, columnModel );
        isColumnResizeToFitEnabled = false;
        isColumnSortingEnabled = false;
        tableHeader.addMouseListener( new MouseHandler() );
        // Set up cell renderers. Just use our standard default renderer to class relationships.
        DefaultPhexCellRenderers.setDefaultPhexCellRenderers( this );
    }
    
    public FWTable( TableModel dataModel  )
    {
        this( dataModel, null );
    }
    
    
    
    // column factory handling
    
    /**
     * Creates, configures and adds default <code>TableColumn</code>s for
     * columns in this table's <code>TableModel</code>. Removes all currently
     * contained <code>TableColumn</code>s. The exact type and configuration
     * of the columns is controlled by the <code>ColumnFactory</code>.
     */
    @Override
    public void createDefaultColumnsFromModel() 
    {
        if (getModel() == null)
        {
            return;
        }
        // Remove any current columns
        removeColumns();
        createAndAddColumns();
    }
    
    /**
     * Creates and adds <code>TableColumn</code>s for each
     * column of the table model. <p>
     */
    private void createAndAddColumns() 
    {
        for (int i = 0; i < getModel().getColumnCount(); i++) 
        {
            // add directly to columnModel - don't go through this.addColumn
            // to guarantee full control of ColumnFactory
            // addColumn has the side-effect to set the header!
            getColumnModel().addColumn(getColumnFactory().createAndConfigureTableColumn(
                    getModel(), i));
        }
    }
    
    /**
     * Remove all columns, make sure to include hidden.
     */
    private void removeColumns()
    {
        List<TableColumn> columns = getColumns( true );
        for (Iterator<TableColumn> iter = columns.iterator(); iter.hasNext();) 
        {
            getColumnModel().removeColumn( iter.next() );
        }
    }
    
    /**
     * Returns the ColumnFactory.
     * 
     * @return the columnFactory to use for column creation and
     *   configuration.
     *   
     * @see #setColumnFactory(ColumnFactory)
     */
    public ColumnFactory getColumnFactory() {
        if ( columnFactory == null ) 
        {
            return ColumnFactory.getInstance();
        }
        return columnFactory;
    }

    /**
     * Sets the <code>ColumnFactory</code> to use for column creation and 
     * configuration. The default value is the shared application
     * ColumnFactory.
     * 
     * @param columnFactory the factory to use, <code>null</code> indicates
     *    to use the shared application factory.
     *    
     * @see #getColumnFactory()
     */
    public void setColumnFactory(ColumnFactory columnFactory) 
    {
        ColumnFactory old = getColumnFactory();
        this.columnFactory = columnFactory;
        firePropertyChange("columnFactory", old, getColumnFactory());
    }
    
    // end column factory handling
    
    
    
    
    public void setVisibleRowCount( int count )
    {
        visibleRowCount = count;
    }
    
    public int getVisibleRowCount()
    {
        return visibleRowCount;
    }
    
    @Override
    public Dimension getPreferredScrollableViewportSize()
    {
        Dimension prefSize = super.getPreferredScrollableViewportSize();

        // JTable hardcodes this to 450 X 400, so we'll calculate it
        // based on the preferred widths of the columns and the
        // visibleRowCount property instead...

        if ( prefSize.getWidth() == 450 && prefSize.getHeight() == 400 )
        {
            TableColumnModel columnModel = getColumnModel();
            int columnCount = columnModel.getColumnCount();

            int w = 0;
            for ( int i = 0; i < columnCount; i++ )
            {
                TableColumn column = columnModel.getColumn( i );
                w += column.getPreferredWidth();
            }
            prefSize.width = w;
            JTableHeader header = getTableHeader();

            int rowCount = getVisibleRowCount();
            prefSize.height = rowCount * getRowHeight()
                + (header != null ? header.getPreferredSize().height : 0);

            setPreferredScrollableViewportSize( prefSize );
        }
        return prefSize;
    }

    @Override
    public void updateUI()
    {
        super.updateUI();
        GUIRegistry guiRegistry = GUIRegistry.getInstance();

        boolean showHorizontalLines = guiRegistry.getShowTableHorizontalLines();
        boolean showVerticalLines = guiRegistry.getShowTableVerticalLines();

        setShowHorizontalLines( showHorizontalLines );
        setShowVerticalLines( showVerticalLines );

        int intercellWidth = showVerticalLines ? 1 : 0;
        int intercellHeight = showHorizontalLines ? 1 : 0;
        // when lines are not shown this hides the little gap between cells
        // when lines are shows a gap is created to display the line
        setIntercellSpacing( new Dimension( intercellWidth, intercellHeight ) );
    }
    
    /**
     * Scrolls vertically to make the given row visible.
     * This might not have any effect if the table isn't contained
     * in a JViewport. <p>
     * 
     * Note: this method has no precondition as it internally uses
     * getCellRect which is lenient to off-range coordinates.
     * 
     * @param row the view row index of the cell
     */
    public void scrollRowToVisible(int row) 
    {
        Rectangle cellRect = getCellRect(row, 0, false);
        Rectangle visibleRect = getVisibleRect();
        cellRect.x = visibleRect.x;
        cellRect.width = visibleRect.width;
        scrollRectToVisible(cellRect);
    }

    public static JScrollPane createFWTableScrollPane( JTable table )
    {
        JScrollPane tableScrollPane = new JScrollPane( table );
        updateFWTableScrollPane( tableScrollPane );
        return tableScrollPane;
    }

    public static void updateFWTableScrollPane( JScrollPane tableScrollPane )
    {
        JViewport viewport = tableScrollPane.getViewport();
        if ( viewport == null )
        {
            NLogger.error( FWTable.class, 
                "tableScrollPane.getViewport() returns null" );
            return;
        }
        viewport.setOpaque( true );

        // this is a very strange behavior here. if I set
        // viewport.setBackground( (Color)UIManager.getDefaults().get(
        //      "window" ) );
        // it is not working but this strange code works somehow...
        // so we need to create a new color object out of the returned ColorUIResource
        Color color = (Color)UIManager.getDefaults().get( "window" );
        if ( color != null )
        {
            Color newColor = new Color( color.getRGB() );
            viewport.setBackground( newColor );
        }
        else
        {
            NLogger.error( FWTable.class, 
                "UIManager.getDefaults.get( \"window\" ) returns null" );
        }
    }

    public void activateAllHeaderActions( )
    {
        // activates a right click popup menu for the table header to hide and show
        // columns.
        activateHeaderPopupMenu();
        activateColumnResizeToFit();
        activateColumnSorting();
    }

    public void activateColumnSorting()
    {
        setColumnSelectionAllowed( false );
        isColumnSortingEnabled = true;
        FWSortedTableModel sortedModel = (FWSortedTableModel)dataModel;
        sortedModel.setTableHeader( tableHeader );
    }
    
    
    //  enhanced column support: delegation to TableColumnModel
    /**
     * Returns the <code>TableColumn</code> at view position
     * <code>columnIndex</code>. The return value is not <code>null</code>.
     * 
     * <p>
     * NOTE: This delegate method is added to protect developer's from
     * unexpected exceptions in jdk1.5+. Super does not expose the
     * <code>TableColumn</code> access by index which may lead to unexpected
     * <code>IllegalArgumentException</code>: If client code assumes the
     * delegate method is available, autoboxing will convert the given int to an
     * Integer which will call the getColumn(Object) method.
     * 
     * 
     * @param viewColumnIndex index of the column with the object in question
     * 
     * @return the <code>TableColumn</code> object that matches the column
     *         index
     * @throws ArrayIndexOutOfBoundsException if viewColumnIndex out of allowed
     *         range.
     *         
     * @see #getColumn(Object)
     * @see #getColumnExt(int)
     * @see TableColumnModel#getColumn(int)
     */
    public TableColumn getColumn(int viewColumnIndex) 
    {
        return getColumnModel().getColumn(viewColumnIndex);
    }

    /**
     * Returns a <code>List</code> of visible <code>TableColumn</code>s.
     * 
     * @return a <code>List</code> of visible columns.
     * @see #getColumns(boolean)
     */
    public List<TableColumn> getColumns() 
    {
        return Collections.list(getColumnModel().getColumns());
    }

    /**
     * Returns the margin between columns.
     * <p>
     * 
     * Convenience to expose column model properties through
     * <code>JXTable</code> api.
     * 
     * @return the margin between columns
     * 
     * @see #setColumnMargin(int)
     * @see TableColumnModel#getColumnMargin()
     */
    public int getColumnMargin() 
    {
        return getColumnModel().getColumnMargin();
    }

    /**
     * Sets the margin between columns.
     * 
     * Convenience to expose column model properties through
     * <code>JXTable</code> api.
     * 
     * @param value margin between columns; must be greater than or equal to
     *        zero.
     * @see #getColumnMargin()
     * @see TableColumnModel#setColumnMargin(int)
     */
    public void setColumnMargin(int value) 
    {
        getColumnModel().setColumnMargin(value);
    }
    
    // enhanced column support: delegation to TableColumnModelExt
    
    /**
     * Returns the number of contained columns. The count includes or excludes invisible
     * columns, depending on whether the <code>includeHidden</code> is true or
     * false, respectively. If false, this method returns the same count as
     * <code>getColumnCount()</code>. If the columnModel is not of type
     * <code>TableColumnModelExt</code>, the parameter value has no effect.
     * 
     * @param includeHidden a boolean to indicate whether invisible columns
     *        should be included
     * @return the number of contained columns, including or excluding the
     *         invisible as specified.
     * @see #getColumnCount()
     * @see TableColumnModelExt#getColumnCount(boolean)        
     */
    public int getColumnCount(boolean includeHidden) 
    {
        if (getColumnModel() instanceof FWTableColumnModel) 
        {
            return ((FWTableColumnModel) getColumnModel())
                    .getColumnCount(includeHidden);
        }
        return getColumnCount();
    }
    
    /**
     * Returns a <code>List</code> of contained <code>TableColumn</code>s.
     * Includes or excludes invisible columns, depending on whether the
     * <code>includeHidden</code> is true or false, respectively. If false, an
     * <code>Iterator</code> over the List is equivalent to the
     * <code>Enumeration</code> returned by <code>getColumns()</code>. 
     * If the columnModel is not of type
     * <code>TableColumnModelExt</code>, the parameter value has no effect.
     * <p>
     * 
     * NOTE: the order of columns in the List depends on whether or not the
     * invisible columns are included, in the former case it's the insertion
     * order in the latter it's the current order of the visible columns.
     * 
     * @param includeHidden a boolean to indicate whether invisible columns
     *        should be included
     * @return a <code>List</code> of contained columns.
     * 
     * @see #getColumns()
     * @see TableColumnModelExt#getColumns(boolean)
     */
    public List<TableColumn> getColumns(boolean includeHidden) 
    {
        if (getColumnModel() instanceof FWTableColumnModel) 
        {
            return ((FWTableColumnModel) getColumnModel())
                    .getColumns(includeHidden);
        }
        return getColumns();
    }

    /**
     * Returns the first <code>TableColumnExt</code> with the given
     * <code>identifier</code>. The return value is null if there is no contained
     * column with <b>identifier</b> or if the column with <code>identifier</code> is not 
     * of type <code>TableColumnExt</code>. The returned column
     * may be visible or hidden.
     * 
     * @param identifier the object used as column identifier
     * @return first <code>TableColumnExt</code> with the given identifier or
     *         null if none is found
     *         
     * @see #getColumnExt(int)
     * @see #getColumn(Object)
     * @see TableColumnModelExt#getColumnExt(Object)        
     */
    public FWTableColumn getFWColumn(Object identifier) 
    {
        if (getColumnModel() instanceof FWTableColumnModel) 
        {
            return ((FWTableColumnModel) getColumnModel())
                    .getFWColumn(identifier);
        } 
        else {
            // PENDING: not tested!
            try {
                TableColumn column = getColumn(identifier);
                if (column instanceof FWTableColumn) 
                {
                    return (FWTableColumn) column;
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return null;
    }

    /**
     * Returns the <code>TableColumnExt</code> at view position
     * <code>columnIndex</code>. The return value is null, if the column at
     * position <code>columnIndex</code> is not of type
     * <code>TableColumnExt</code>. The returned column is visible.
     * 
     * @param viewColumnIndex the index of the column desired
     * @return the <code>TableColumnExt</code> object that matches the column
     *         index
     * @throws ArrayIndexOutOfBoundsException if columnIndex out of allowed
     *         range, that is if
     *         <code> (columnIndex < 0) || (columnIndex >= getColumnCount())</code>.
     * 
     * @see #getColumnExt(Object)
     * @see #getColumn(int)
     * @see TableColumnModelExt#getColumnExt(int)
     */
    public FWTableColumn getFWColumn(int viewColumnIndex) 
    {
        TableColumn column = getColumn(viewColumnIndex);
        if (column instanceof FWTableColumn) 
        {
            return (FWTableColumn) column;
        }
        return null;
    }
    
    /**
     * Convenience method to detect update table event type.
     * 
     * @param e the event to examine. 
     * @return true if the event is of type update and not dataChanged, false else.
     */
    protected boolean isUpdate(TableModelEvent e) {
        if (isStructureChanged(e)) return false;
        return e.getType() == TableModelEvent.UPDATE && 
            e.getLastRow() < Integer.MAX_VALUE;
    }

    /**
     * Convenience method to detect a structureChanged table event type.
     * @param e the event to examine.
     * @return true if the event is of type structureChanged or null, false else.
     */
    protected boolean isStructureChanged(TableModelEvent e) {
        return e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW;
    }

    /**
     * Activates the column resize to fit that occurs when clicking between two
     * columns.
     */
    public void activateColumnResizeToFit( )
    {
        // activate the column resize to fit
        isColumnResizeToFitEnabled = true;
    }

    /**
     * Activates right click popup menu for the table header to hide and show
     * columns.
     */
    public void activateHeaderPopupMenu( )
    {
        headerPopup = new JPopupMenu();
        PopupMenuActionHandler actionHandler = new PopupMenuActionHandler();
        ColumnCheckBoxMenuItem chkItem;
        List<TableColumn> colList = ((FWTableColumnModel)columnModel).getColumns( true );
        for( TableColumn column : colList )
        {
            if ( !(column instanceof FWTableColumn) )
            {
                continue;
            }
            FWTableColumn fwColumn = (FWTableColumn)column;
            chkItem = new ColumnCheckBoxMenuItem( fwColumn );
            chkItem.setEnabled( fwColumn.isHideable() );
            chkItem.addActionListener( actionHandler );
            headerPopup.add( chkItem );
        }
    }

    /**
     * Maps the index of the row in the view at viewRowIndex to the index of the
     * row in the model. Returns the index of the corresponding row in the
     * model. If viewRowIndex is less than zero, returns viewRowIndex.
     *
     * Mapping of the index is necessary if the view might display a different
     * ordering of rows due to sorting or filtering of rows. If there is no
     * different ordering between the view and the model then the view index is
     * returned directly.
     *
     * @params viewRowIndex - the index of the row in the view
     * @returns the index of the corresponding row in the model
     */
    // because of a name conflict with Java 6 this method was renamed from 
    // convertRowIndexToModel() to translateRowIndexToModel()
    public int translateRowIndexToModel( int viewRowIndex )
    {
        if ( isColumnSortingEnabled )
        {
            return ((FWSortedTableModel)dataModel).getModelIndex(
                viewRowIndex );
        }
        else
        {
            return viewRowIndex;
        }
    }

    /**
     * Maps the indices of the rows in the view at viewRowIndices to the indices
     * of the rows in the model. Returns the indices of the corresponding rows
     * in the model. If a view row index is less than zero, it is coppied.
     *
     * Mapping of the indices is necessary if the view might display a different
     * ordering of rows due to sorting or filtering of rows. If there is no
     * different ordering between the view and the model then the viewRowIndices
     * is returned, otherwise a new array with the mapped indices is returned.
     *
     * @params viewRowIndices - the indices of the rows in the view
     * @returns the indices of the corresponding rows in the model
     **/
    public int[] convertRowIndicesToModel( int[] viewRowIndices )
    {
        if ( isColumnSortingEnabled )
        {
            FWSortedTableModel sortedModel = (FWSortedTableModel)dataModel;
            int[] modelRowIndices = new int[ viewRowIndices.length ];
            for ( int i=0; i < viewRowIndices.length; i++ )
            {
                modelRowIndices[ i ] = sortedModel.getModelIndex(
                    viewRowIndices[ i ] );
            }
            return modelRowIndices;
        }
        else
        {
            return viewRowIndices;
        }
    }
    
    /**
     * Maps the index of the row in the model at modelRowIndex to the index of the
     * row in the view. Returns the index of the corresponding row in the
     * view. If modelRowIndex is less than zero, returns modelRowIndex.
     *
     * Mapping of the index is necessary if the view might display a different
     * ordering of rows due to sorting or filtering of rows. If there is no
     * different ordering between the view and the model then the view index is
     * returned directly.
     *
     * @params modelRowIndex - the index of the row in the model
     * @returns the index of the corresponding row in the view
     */
    // because of a name conflict with Java 6 this method was renamed from 
    // convertRowIndexToView() to translateRowIndexToView()
    public int translateRowIndexToView( int modelRowIndex )
    {
        if ( isColumnSortingEnabled )
        {
            return ((FWSortedTableModel)dataModel).getViewIndex(
                modelRowIndex );
        }
        else
        {
            return modelRowIndex;
        }
    }

    /**
     * Returns the index of the visible columns where the given point in the
     * column margin area and shows the resizing cursor.
     */
    public FWTableColumn getResizingColumn( Point p )
    {
        int column = tableHeader.columnAtPoint( p );
        if (column == -1)
        {
            return null;
        }
        Rectangle r = tableHeader.getHeaderRect(column);
        r.grow(-3, 0);
        if (r.contains(p))
        {
            return null;
        }
        int midPoint = r.x + r.width/2;
        int columnIndex;
        if( tableHeader.getComponentOrientation().isLeftToRight() )
        {
            columnIndex = (p.x < midPoint) ? column - 1 : column;
        }
        else
        {
            columnIndex = (p.x < midPoint) ? column : column - 1;
        }
        if (columnIndex == -1)
        {
            return null;
        }

        return (FWTableColumn)tableHeader.getColumnModel().getColumn(columnIndex);
    }
    
    /**
     * {@inheritDoc}
     * Overridden to return a <code>FWTableColumnModel</code>.
     */
    @Override
    protected TableColumnModel createDefaultColumnModel() 
    {
        return new FWTableColumnModel();
    }
    
    /**
     * Invoked when this table's <code>TableModel</code> generates
     * a <code>TableModelEvent</code>.
     * The <code>TableModelEvent</code> should be constructed in the
     * coordinate system of the model; the appropriate mapping to the
     * view coordinate system is performed by this <code>JTable</code>
     * when it receives the event.
     * <p>
     * Application code will not use these methods explicitly, they
     * are used internally by <code>JTable</code>.
     * <p>
     * Note that as of 1.3, this method clears the selection, if any.
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW)
        {
            super.tableChanged(e);
            return;
        }

		// The totalRowHeight calculated below will be incorrect if
		// there are variable height rows. Repaint the visible region,
		// but don't return as a revalidate may be necessary as well.
		//if (rowModel != null) {
		//    repaint();
		//}

        if (e.getType() == TableModelEvent.INSERT || e.getType() == TableModelEvent.DELETE)
        {
            super.tableChanged(e);
            return;
        }
        
        
       super.tableChanged(e);
        
        //System.out.println(e.getColumn() + " " + e.getFirstRow() + " " + e.getLastRow() + " " +
        //    e.getType() + " " + e.getSource());

        int modelColumn = e.getColumn();
        int start = e.getFirstRow();
        int end = e.getLastRow();

        Rectangle dirtyRegion;
        if (modelColumn == TableModelEvent.ALL_COLUMNS)
        {
            // 1 or more rows changed
            dirtyRegion = new Rectangle(0, start * getRowHeight(),
                                        getColumnModel().getTotalColumnWidth(), 0);
        }
        else
        {
            // A cell or column of cells has changed.
            // Unlike the rest of the methods in the JTable, the TableModelEvent
            // uses the coordinate system of the model instead of the view.
            // This is the only place in the JTable where this "reverse mapping"
            // is used.
            int column = convertColumnIndexToView(modelColumn);
            dirtyRegion = getCellRect(start, column, false);
        }

        // Now adjust the height of the dirty region according to the value of "end".
        // Check for Integer.MAX_VALUE as this will cause an overflow.
        if (end != Integer.MAX_VALUE)
        {
            dirtyRegion.height = (end-start+1)*getRowHeight();
            	repaint(dirtyRegion.x, dirtyRegion.y, dirtyRegion.width, dirtyRegion.height);
        }
        // In fact, if the end is Integer.MAX_VALUE we need to revalidate anyway
        // because the scrollbar may need repainting.
        else
        {
            // we remove the clearSelection call here to keep the current selection
            // active, it seems no bug is resulting of this.
            //clearSelection();
            resizeAndRepaint();
            //rowModel = null;
        }
    }

    private class MouseHandler extends MouseInputAdapter
    {
        @Override
        public void mouseClicked( MouseEvent e )
        {
            FWTableColumn column = getResizingColumn( e.getPoint() );
            int clickCount = e.getClickCount();

            
            if ( clickCount >= 2 )
            {
                handleColumnResizeToFit( column );
            }
        }

        @Override
        public void mouseReleased( MouseEvent e )
        {
            if ( e.isPopupTrigger() )
            {
                popupMenu((Component)e.getSource(), e.getX(), e.getY());
            }
        }

        @Override
        public void mousePressed( MouseEvent e )
        {
            if ( e.isPopupTrigger() )
            {
                popupMenu((Component)e.getSource(), e.getX(), e.getY());
            }
        }

        /**
         * Handles double click event on the header for column resizing,
         * when click is between two columns and resize to fit is enabled.
         */
        private void handleColumnResizeToFit( FWTableColumn column )
        {
            if ( !isColumnResizeToFitEnabled )
            {
                return;
            }
            // handles double click event on the header with column resizing...

            if ( column != null )
            {
                column.sizeWidthToFitData( FWTable.this, dataModel );
            }
        }

        /**
         * Shows the popup menu if enabled.
         */
        private void popupMenu(Component source, int x, int y)
        {
            if ( headerPopup != null )
            {
                headerPopup.show(source, x, y);
            }
        }
    }

    private class PopupMenuActionHandler implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            ColumnCheckBoxMenuItem item = (ColumnCheckBoxMenuItem)e.getSource();
            FWTableColumn column = item.getTableColumn( );
            if ( item.getState() )
            {
                column.setVisible( true );
            }
            else
            {
                column.setVisible( false );
            }
        }
    }

    private class ColumnCheckBoxMenuItem extends JCheckBoxMenuItem
    {
        private FWTableColumn column;

        public ColumnCheckBoxMenuItem( FWTableColumn tableColumn )
        {
            super( (String)tableColumn.getHeaderValue(), tableColumn.isVisible() );
            column = tableColumn;
        }

        public FWTableColumn getTableColumn()
        {
            return column;
        }
    }
}