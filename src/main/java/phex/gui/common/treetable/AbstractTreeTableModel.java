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
 *  --- CVS Information ---
 *  $Id: AbstractTreeTableModel.java 3804 2007-05-19 14:52:48Z gregork $
 */
package phex.gui.common.treetable;

import java.util.Comparator;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;


/** An abstract implementation of the TreeTableModel interface, handling the list 
 * of listeners.
 */
public abstract class AbstractTreeTableModel implements TreeTableModel
{
    /**
     * Value returned by getColumnClass.
     */
    public final static Class hierarchicalColumnClass = TreeTableModel.class;
    
    protected String[] tableColumns;
    protected Class[] tableClasses;
    protected Object[] columnIds;

    protected Object root;
    protected EventListenerList listenerList = new EventListenerList();

    public AbstractTreeTableModel( Object root,
        Object[] theColumnIds, String[] theTableColumns, Class[] theTableClasses)
    {
        tableColumns = theTableColumns;
        tableClasses = theTableClasses;
        columnIds = theColumnIds;
        // col 0 must always be hierarchicalColumnClass...
        assert tableClasses[0] == hierarchicalColumnClass;
        
        this.root = root;
    }
    
    public String getColumnName(int column)
    {
        return tableColumns[ column ];
    }

    public int getColumnCount()
    {
        return tableColumns.length;
    }

    public Class getColumnClass( int column )
    {
        Class clazz = tableClasses[ column ];
        if ( clazz == null )
        {
            clazz = String.class;
        }
        return clazz;
    }
    
    /**
     * Returns the most comparator that is used for sorting of the cell values
     * in the column. This is used by the FWSortedTableModel to perform the
     * sorting. If not overwritten the method returns null causing the
     * FWSortedTableModel to use a NaturalComparator. It expects all Objects that
     * are returned from getComparableValueAt() to implement the Comparable interface.
     *
     */
    public Comparator getColumnComparator( int column )
    {
        return null;
    }

    /**
     * Returns an attribute value that is used for comparing on sorting
     * for the cell at row and column. If not overwritten the call is forwarded
     * to getValueAt().
     * The returned Object is compared via the Comparator returned from
     * getColumnComparator(). If no comparator is specified the returned Object
     * must implement the Comparable interface.
     */
    public Object getComparableValueAt( Object node, int column )
    {
        return getValueAt( node, column );
    }

    /**
     * Indicates if a column is hideable.
     */
    public boolean isColumnHideable( int columnIndex )
    {
        return true;
    }
    
    /**
     * Indicates if a column is visible by default.
     */
    public boolean isColumnDefaultVisible( int columnIndex )
    {
        return true;
    }
    
    /**
     * Returns the unique column id for the column model index. This needs to be 
     * done to be able identify columns and there index after changes in Phex releases.
     * The unique column id is not allowed to ever change over Phex releases. It
     * is used when serializing column information. The column id is containd in
     * the identifier field of the TableColumn.
     */
    public Object getColumnId( int columnIndex )
    {
        return columnIds[ columnIndex ];
    }

    //
    // Default implmentations for methods in the TreeModel interface. 
    //   
    public Object getRoot()
    {
        return root;
    }

    public boolean isLeaf(Object node)
    {
        return getChildCount(node) == 0;
    }

    public void valueForPathChanged(TreePath path, Object newValue)
    {
    }

    // This is not called in the JTree's default mode: use a naive implementation. 
    public int getIndexOfChild(Object parent, Object child)
    {
        for (int i = 0; i < getChildCount(parent); i++)
        {
            if (getChild(parent, i).equals(child))
            {
                return i;
            }
        }
        return -1;
    }

    public void addTreeModelListener(TreeModelListener l)
    {
        listenerList.add(TreeModelListener.class, l);
    }

    public void removeTreeModelListener(TreeModelListener l)
    {
        listenerList.remove(TreeModelListener.class, l);
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     * @see EventListenerList
     */
    public void fireTreeNodesChanged(
        Object source,
        Object[] path,
        int[] childIndices,
        Object[] children)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == TreeModelListener.class)
            {
                // Lazily create the event:
                if (e == null)
                {
                    e =
                        new TreeModelEvent(
                            source,
                            path,
                            childIndices,
                            children);
                }
                ((TreeModelListener)listeners[i + 1]).treeNodesChanged(e);
            }
        }
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     * @see EventListenerList
     */
    public void fireTreeNodesInserted(
        Object source,
        Object[] path,
        int[] childIndices,
        Object[] children)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == TreeModelListener.class)
            {
                // Lazily create the event:
                if (e == null)
                    e =
                        new TreeModelEvent(
                            source,
                            path,
                            childIndices,
                            children);
                ((TreeModelListener)listeners[i + 1]).treeNodesInserted(e);
            }
        }
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     * @see EventListenerList
     */
    public void fireTreeNodesRemoved(
        Object source,
        Object[] path,
        int[] childIndices,
        Object[] children)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == TreeModelListener.class)
            {
                // Lazily create the event:
                if (e == null)
                    e =
                        new TreeModelEvent(
                            source,
                            path,
                            childIndices,
                            children);
                ((TreeModelListener)listeners[i + 1]).treeNodesRemoved(e);
            }
        }
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     * @see EventListenerList
     */
    public void fireTreeStructureChanged(
        Object source,
        Object[] path,
        int[] childIndices,
        Object[] children)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == TreeModelListener.class)
            {
                // Lazily create the event:
                if (e == null)
                    e =
                        new TreeModelEvent(
                            source,
                            path,
                            childIndices,
                            children);
                ((TreeModelListener)listeners[i + 1]).treeStructureChanged(e);
            }
        }
    }

    //
    // Default impelmentations for methods in the TreeTableModel interface. 
    //

    /** 
     * 
     */
    public boolean isCellEditable(Object node, int column)
    {
        return false;
    }

    public void setValueAt(Object aValue, Object node, int column)
    {
    }

    // Left to be implemented in the subclass:

    /* 
     *   public Object getChild(Object parent, int index)
     *   public int getChildCount(Object parent) 
     *   public String getColumnName(Object node, int column)  
     *   public Object getValueAt(Object node, int column) 
     */
}
