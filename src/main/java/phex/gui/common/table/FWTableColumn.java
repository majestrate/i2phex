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
 *  $Id: FWTableColumn.java 3803 2007-05-19 14:13:56Z gregork $
 */
package phex.gui.common.table;

//This class contains source from the SwingLabs class
//org.jdesktop.swingx.table.TableColumnExt

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import phex.xml.sax.gui.DTableColumn;


/**
 * There is currently no property change support...
 * Unfortunately TableColumn does not offer the property change support for
 * subclasses. If change support is neccessary it needs to be integrated separatly.
 */
public class FWTableColumn extends TableColumn
{
    /** 
     * Prototype value used to calculate preferred width. 
     */
    protected Object prototypeValue;
    
    /**
     * Indicates if this column is currently shown.
     */
    private boolean isVisible;

    /**
     * Indicates if this column can be hide in the view.
     */
    private boolean isHideable;

    private boolean isSortingAscending;
    
    public FWTableColumn( int modelIndex )
    {
        super( modelIndex );
        isVisible = true;
        isHideable = true;
        isSortingAscending = false;
    }

    public FWTableColumn( int modelIndex, Object headerValue, Integer identifier )
    {
        this( modelIndex );        
        setHeaderValue( headerValue );
        setIdentifier( identifier );
    }
    
    /**
     * Sets the prototypeValue property.  The value should be of a type
     * which corresponds to the column's class as defined by the table model.
     * If non-null, the FWTable instance will use this property to calculate
     * and set the initial preferredWidth of the column.  Note that this
     * initial preferredWidth will be overridden if the user resizes columns
     * directly.
     * 
     * @param value Object containing the value of the prototype to be used
     *         to calculate the initial preferred width of the column
     * @see #getPrototypeValue
     */
    public void setPrototypeValue(Object value) 
    {
        Object oldPrototypeValue = this.prototypeValue;
        this.prototypeValue = value;
        firePropertyChange("prototypeValue",
                           oldPrototypeValue,
                           value);
    }

    /**
     * Returns the prototypeValue property.
     * The default is <code>null</code>.
     * 
     * @return Object containing the value of the prototype to be used
     *         to calculate the initial preferred width of the column
     * @see #setPrototypeValue
     */
    public Object getPrototypeValue() 
    {
        return prototypeValue;
    }

    /**
     * Indicates if this column is currently shown.
     */
    public boolean isVisible()
    {
        return isVisible;
    }

    /**
     * Indicates if this column can be hide in the view.
     */
    public boolean isHideable()
    {
        return isHideable;
    }

    /**
     * Makes the column to be hideable. Dont call this after initializing and using
     * the FWTableColumnModel... since we have no
     * event model for this currently... Unfortunately TableColumn does not offer
     * the property change support for subclasses.
     */
    public void setHideable( boolean state )
    {
        boolean oldHideable = this.isHideable;
        this.isHideable = state;
        firePropertyChange("visible",
                           Boolean.valueOf(oldHideable),
                           Boolean.valueOf(isHideable));
    }

    /**
     * Sets the column as hidden.
     * This should only be called by the FWTableColumnModel since we have no
     * event model for this currently... Unfortunately TableColumn does not offer
     * the property change support for subclasses.
     */
    public void setVisible( boolean state )
    {
        boolean oldVisible = this.isVisible;
        this.isVisible = state;
        firePropertyChange("visible",
                           Boolean.valueOf(oldVisible),
                           Boolean.valueOf(isVisible));
    }

    /**
     * Reverses the sorting order and returns if the new sorting order is ascending.
     */
    public boolean reverseSortingOrder()
    {
        isSortingAscending = !isSortingAscending;
        return isSortingAscending;
    }

    public void sizeWidthToFitData( JTable table, TableModel model )
    {
        TableCellRenderer aCellRenderer = cellRenderer;
        if ( cellRenderer == null )
        {
            aCellRenderer = table.getDefaultRenderer( model.getColumnClass( modelIndex ) );
        }
        int maxWidth = 0;
        Component component;
        int rowCount = model.getRowCount();
        for ( int i = 0; i < rowCount; i++ )
        {
            Object value = model.getValueAt( i, modelIndex );
            component = aCellRenderer.getTableCellRendererComponent( table, value, false,
                false, i, modelIndex );
            maxWidth = Math.max( component.getPreferredSize().width + 4, maxWidth );
        }

        setPreferredWidth( maxWidth );
    }

    public DTableColumn createDGuiTableColumn()
    {
        DTableColumn dColumn = new DTableColumn();
        dColumn.setColumnID( ((Integer)getIdentifier()).intValue() );
        dColumn.setVisible( isVisible );
        dColumn.setWidth( getWidth() );
        return dColumn;
    }
    
    /**
     * Notifies registered <code>PropertyChangeListener</code>s 
     * about property changes. This method must be invoked internally
     * whe any of the enhanced properties changed.
     * <p>
     * Implementation note: needed to replicate super 
     * functionality because super's field <code>propertyChangeSupport</code> 
     * and method <code>fireXX</code> are both private.
     * 
     * @param propertyName  name of changed property
     * @param oldValue old value of changed property
     * @param newValue new value of changed property
     */ 
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if ((oldValue != null && !oldValue.equals(newValue)) ||
              oldValue == null && newValue != null) {
             PropertyChangeListener pcl[] = getPropertyChangeListeners();
             if (pcl != null && pcl.length != 0) {
                 PropertyChangeEvent pce = new PropertyChangeEvent(this,
                     propertyName,
                     oldValue, newValue);

                 for (int i = 0; i < pcl.length; i++) {
                     pcl[i].propertyChange(pce);
                 }
             }
         }
     }
    
    public static int calculateHeaderWidth( FWTable table, FWTableColumn col )
    {
        TableCellRenderer aCellRenderer = col.getHeaderRenderer();
        if ( aCellRenderer == null )
        {
            aCellRenderer = table.getTableHeader().getDefaultRenderer();
        }
        Component component = aCellRenderer.getTableCellRendererComponent( table, 
            col.getHeaderValue(), false, false, -1, col.getModelIndex() );
        int width = Math.max( component.getPreferredSize().width + 4, 75 );
        return width;
    }
}