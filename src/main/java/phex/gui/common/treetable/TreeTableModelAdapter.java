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
 *  $Id: TreeTableModelAdapter.java 4064 2007-11-29 22:57:54Z complication $
 */
package phex.gui.common.treetable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Comparator;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.*;
import javax.swing.tree.TreePath;

import phex.gui.common.table.FWTableModel;
import phex.gui.models.ISortableModel;

/**
 * This is a wrapper class takes a TreeTableModel and implements 
 * the table model interface. The implementation is trivial, with 
 * all of the event dispatching support provided by the superclass: 
 * the AbstractTableModel. 
 */
public class TreeTableModelAdapter extends FWTableModel implements ISortableModel
{    
    protected final JTree tree; // immutable
    private JTreeTable treeTable = null; // logically immutable
    
    
    private TreeModelListener treeModelListener;
    /**
     * Maintains a TreeTableModel and a JTree as purely implementation details.
     * Developers can plug in any type of custom TreeTableModel through a
     * JXTreeTable constructor or through setTreeTableModel().
     *
     * @param model Underlying data model for the JXTreeTable that will ultimately
     * be bound to this TreeTableModelAdapter
     * @param tree TreeTableCellRenderer instantiated with the same model as
     * specified by the model parameter of this constructor
     * @throws IllegalArgumentException if a null model argument is passed
     * @throws IllegalArgumentException if a null tree argument is passed
     */
    TreeTableModelAdapter(JTree tree) {
        assert tree != null;

        this.tree = tree; // need tree to implement getRowCount()
        tree.getModel().addTreeModelListener(getTreeModelListener());
        tree.addTreeExpansionListener(new TreeExpansionListener() {
            // Don't use fireTableRowsInserted() here; the selection model
            // would get updated twice.
            public void treeExpanded(TreeExpansionEvent event) {
                fireTableDataChanged();
            }

            public void treeCollapsed(TreeExpansionEvent event) {
                fireTableDataChanged();
            }
        });
        tree.addPropertyChangeListener("model", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                TreeTableModel model = (TreeTableModel) evt.getOldValue();
                model.removeTreeModelListener(getTreeModelListener());

                model = (TreeTableModel) evt.getNewValue();
                model.addTreeModelListener(getTreeModelListener());
                
                fireTableStructureChanged();
            }
        });
    }

    /**
     * Returns the JXTreeTable instance to which this TreeTableModelAdapter is
     * permanently and exclusively bound. For use by
     * {@link org.jdesktop.swingx.JXTreeTable#setModel(javax.swing.table.TableModel)}.
     *
     * @return JXTreeTable to which this TreeTableModelAdapter is permanently bound
     */
    protected JTreeTable getTreeTable() {
        return treeTable;
    }

    /**
     * Immutably binds this TreeTableModelAdapter to the specified JXTreeTable.
     *
     * @param treeTable the JXTreeTable instance that this adapter is bound to.
     */
    protected final void bind(JTreeTable treeTable) {
        // Suppress potentially subversive invocation!
        // Prevent clearing out the deck for possible hijack attempt later!
        if (treeTable == null) {
            throw new IllegalArgumentException("null treeTable");
        }

        if (this.treeTable == null) {
            this.treeTable = treeTable;
        }
        else {
            throw new IllegalArgumentException("adapter already bound");
        }
    }

    // Wrappers, implementing TableModel interface.
    // TableModelListener management provided by AbstractTableModel superclass.

    @Override
    public Class<?> getColumnClass(int column) {
            return ((TreeTableModel) tree.getModel()).getColumnClass(column);
    }

    public int getColumnCount() {
            return ((TreeTableModel) tree.getModel()).getColumnCount();
    }

        @Override
    public String getColumnName(int column) {
            return ((TreeTableModel) tree.getModel()).getColumnName(column);
    }

    public int getRowCount() {
        return tree.getRowCount();
    }

    public Object getValueAt(int row, int column) {
        // Issue #270-swingx: guard against invisible row
        Object node = nodeForRow(row);
            return node != null ? ((TreeTableModel) tree.getModel()).getValueAt(node, column) : null;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        // Issue #270-swingx: guard against invisible row
        Object node = nodeForRow(row);
            return node != null ? ((TreeTableModel) tree.getModel()).isCellEditable(node, column) : false;
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        // Issue #270-swingx: guard against invisible row
        Object node = nodeForRow(row);
        if (node != null) {
                ((TreeTableModel) tree.getModel()).setValueAt(value, node, column);
        }
    }

    protected Object nodeForRow(int row) {
        // Issue #270-swingx: guard against invisible row
        TreePath path = tree.getPathForRow(row);
        return path != null ? path.getLastPathComponent() : null;
    }
    
    /**
         * @return <code>TreeModelListener</code>
     */
    private TreeModelListener getTreeModelListener() {
        if (treeModelListener == null) {
            treeModelListener = new TreeModelListener() {

                public void treeNodesChanged(TreeModelEvent e) {
                    delayedFireTableDataUpdated(e);
                }   

                // We use delayedFireTableDataChanged as we can
                // not be guaranteed the tree will have finished processing
                // the event before us.
                public void treeNodesInserted(TreeModelEvent e) {
                    delayedFireTableDataChanged(e, 1);
                }

                public void treeNodesRemoved(TreeModelEvent e) {
                    delayedFireTableDataChanged(e, 2);
                }

                public void treeStructureChanged(TreeModelEvent e) {
                    // ?? should be mapped to structureChanged -- JW
//                    if (isTableStructureChanged(e)) {
//                        delayedFireTableStructureChanged();
//                    } else {
                        delayedFireTableDataChanged();
//                    }
                }
            };
        }
        return treeModelListener;
    }

    /**
     * Decides if the given treeModel structureChanged should 
     * trigger a table structureChanged. Returns true if the 
     * source path is the root or null, false otherwise.<p>
     * 
     * PENDING: need to refine? "Marker" in Event-Object?
     * 
     * @param e the TreeModelEvent received in the treeModelListener's 
     *   treeStructureChanged
     * @return a boolean indicating whether the given TreeModelEvent
     *   should trigger a structureChanged.
     */
    private boolean isTableStructureChanged(TreeModelEvent e) {
        if ((e.getTreePath() == null) ||
                (e.getTreePath().getParentPath() == null)) return true;
        return false;
    }

    /**
     * Invokes fireTableDataChanged after all the pending events have been
     * processed. SwingUtilities.invokeLater is used to handle this.
     */
    private void delayedFireTableStructureChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fireTableStructureChanged();
            }
        });
    }

    /**
     * Invokes fireTableDataChanged after all the pending events have been
     * processed. SwingUtilities.invokeLater is used to handle this.
     */
    private void delayedFireTableDataChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fireTableDataChanged();
            }
        });
    }

    /**
     * Invokes fireTableDataChanged after all the pending events have been
     * processed. SwingUtilities.invokeLater is used to handle this.
         * Allowed event types: 1 for insert, 2 for delete
     */
    private void delayedFireTableDataChanged(final TreeModelEvent tme, final int typeChange) {
        if ((typeChange < 1 ) || (typeChange > 2))
            throw new IllegalArgumentException("Event type must be 1 or 2, was " + typeChange);
        // expansion state before invoke may be different 
        // from expansion state in invoke 
        final boolean expanded = tree.isExpanded(tme.getTreePath());
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int indices[] = tme.getChildIndices();
                TreePath path = tme.getTreePath();
                if (indices != null) { 
                    if (expanded) { // Dont bother to update if the parent 
                        // node is collapsed
                        // indices must in ascending order, as per TreeEvent/Listener doc
                        int min = indices[0];
                        int max = indices[indices.length - 1];
                        int startingRow = tree.getRowForPath(path) + 1;
                        min = startingRow + min;
                        max = startingRow + max;
                        switch (typeChange) {
                            case 1: 
                                fireTableRowsInserted(min, max);
                                break;
                            case 2:
                                fireTableRowsDeleted(min, max);
                                break;
                        }
                    } else { 
                        // not expanded - but change might effect appearance
                        // of parent
                        // Issue #82-swingx
                        int row = tree.getRowForPath(path);
                        // fix Issue #247-swingx: prevent accidental
                        // structureChanged
                        // for collapsed path 
                        // in this case row == -1, which == TableEvent.HEADER_ROW
                        if (row >= 0)
                            fireTableRowsUpdated(row, row);
                    }
                }
                else {  // case where the event is fired to identify root.
                    fireTableDataChanged();
                }
            }
        });
    }
    
    /**
     * This is used for updated only. PENDING: not necessary to delay?
     * Updates are never structural changes which are the critical.
     * 
     * @param e
     */
    protected void delayedFireTableDataUpdated(final TreeModelEvent tme) {
        final boolean expanded = tree.isExpanded(tme.getTreePath());
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int indices[] = tme.getChildIndices();
                TreePath path = tme.getTreePath();
                if (indices != null) {
                    if (expanded) { // Dont bother to update if the parent
                        // node is collapsed
                        Object children[] = tme.getChildren();
                        // can we be sure that children.length > 0?
                        // int min = tree.getRowForPath(path.pathByAddingChild(children[0]));
                        // int max = tree.getRowForPath(path.pathByAddingChild(children[children.length -1]));
                        int min = Integer.MAX_VALUE;
                        int max = Integer.MIN_VALUE;
                        for (int i = 0; i < indices.length; i++) {
                            Object child = children[i];
                            TreePath childPath = path
                                    .pathByAddingChild(child);
                            int index = tree.getRowForPath(childPath);
                            if (index < min) {
                                min = index;
                            }
                            if (index > max) {
                                max = index;
                            }
                        }
//                        LOG.info("Updated: parentPath/min/max" + path + "/" + min + "/" + max);
                        // JW: the index is occasionally - 1 - need further digging 
                        fireTableRowsUpdated(Math.max(0, min), Math.max(0, max));
                    } else {
                        // not expanded - but change might effect appearance
                        // of parent Issue #82-swingx
                        int row = tree.getRowForPath(path);
                        // fix Issue #247-swingx: prevent accidental structureChanged
                        // for collapsed path in this case row == -1, 
                        // which == TableEvent.HEADER_ROW
                        if (row >= 0)
                            fireTableRowsUpdated(row, row);
                    }
                } else { // case where the event is fired to identify
                            // root.
                    fireTableDataChanged();
                }
            }
        });

    }
   

    // Wrappers, implementing TableModel interface. 

    /**
      * Returns the most comparator that is used for sorting of the cell values
      * in the column. This is used by the FWSortedTableModel to perform the
      * sorting. If not overwritten the method returns null causing the
      * FWSortedTableModel to use a NaturalComparator. It expects all Objects that
      * are returned from getComparableValueAt() to implement the Comparable interface.
      *
      */
    public Comparator getColumnComparator(int column)
    {
        return ((TreeTableModel) tree.getModel()).getColumnComparator(column);
    }

    /**
     * Returns an attribute value that is used for comparing on sorting
     * for the cell at row and column. If not overwritten the call is forwarded
     * to getValueAt().
     * The returned Object is compared via the Comparator returned from
     * getColumnComparator(). If no comparator is specified the returned Object
     * must implement the Comparable interface.
     */
    public Object getComparableValueAt(int row, int column)
    {
        return ((TreeTableModel) tree.getModel()).getComparableValueAt(nodeForRow(row), column);
    }

    /**
     * Indicates if a column is hideable.
     */
    @Override
    public boolean isColumnHideable( int columnIndex )
    {
        return ((TreeTableModel) tree.getModel()).isColumnHideable( columnIndex );
    }
    
    /**
     * Indicates if a column is visible by default.
     */
    @Override
    public boolean isColumnDefaultVisible( int columnIndex )
    {
        return ((TreeTableModel) tree.getModel()).isColumnDefaultVisible( columnIndex );
    }
    
    @Override
    public Object getColumnId( int columnIndex )
    {
        return ((TreeTableModel) tree.getModel()).getColumnId( columnIndex );
    }

    @Override
    public void fireTableDataChanged()
    {
        super.fireTableDataChanged();
    }

    /**
     * @see phex.gui.models.ISortableModel#getSortByColumn()
     */
    public int getSortByColumn()
    {
        if ( tree.getModel() instanceof ISortableModel )
        {
            return ((ISortableModel)tree.getModel()).getSortByColumn(); 
        }
        else
        {
            throw new UnsupportedOperationException( "TreeTableModel not an ISortableModel." );
        }
    }

    /**
     * @see phex.gui.models.ISortableModel#isSortedAscending()
     */
    public boolean isSortedAscending()
    {
        if ( tree.getModel() instanceof ISortableModel )
        {
            return ((ISortableModel) tree.getModel()).isSortedAscending(); 
        }
        else
        {
            throw new UnsupportedOperationException( "TreeTableModel not an ISortableModel." );
        }
    }

    /**
     * @see phex.gui.models.ISortableModel#sortByColumn(int, boolean)
     */
    public void sortByColumn(int column, boolean isSortedAscending)
    {
        if ( tree.getModel() instanceof ISortableModel )
        {
            ((ISortableModel)tree.getModel()).sortByColumn( column, isSortedAscending);
        }
        else
        {
            throw new UnsupportedOperationException( "TreeTableModel not an ISortableModel." );
        }
    }
}
