/*
 * Id: JXTree.java,v 1.39 2007/10/22 13:49:21 kschaefe Exp
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

package org.jdesktop.swingx;

import java.applet.Applet;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ActionMap;
import javax.swing.CellEditor;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.decorator.ComponentAdapter;

import sun.swing.UIAction;


/**
 * JXTree.
 *
 * PENDING: support filtering/sorting.
 * 
 * @author Ramesh Gupta
 * @author Jeanette Winzenburg
 */
public class JXTree extends JTree {
    private static final Logger LOG = Logger.getLogger(JXTree.class.getName());
    private Method conversionMethod = null;
    private final static Class[] methodSignature = new Class[] {Object.class};
    private final static Object[] methodArgs = new Object[] {null};
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final TreePath[] EMPTY_TREEPATH_ARRAY = new TreePath[0];


    private boolean overwriteIcons;
    
    // hacks around core focus issues around editing.
    /**
     * The propertyChangeListener responsible for terminating
     * edits if focus lost.
     */
    private CellEditorRemover editorRemover;
    /**
     * The CellEditorListener responsible to force the 
     * focus back to the tree after terminating edits.
     */
    private CellEditorListener editorListener;
    
    
    
    /**
     * Constructs a <code>JXTree</code> with a sample model. The default model
     * used by this tree defines a leaf node as any node without children.
     */
    public JXTree() {
    init();
    }

    /**
     * Constructs a <code>JXTree</code> with each element of the specified array
     * as the child of a new root node which is not displayed. By default, this
     * tree defines a leaf node as any node without children.
     *
     * This version of the constructor simply invokes the super class version
     * with the same arguments.
     *
     * @param value an array of objects that are children of the root.
     */
    public JXTree(Object[] value) {
        super(value);
    init();
    }

    /**
     * Constructs a <code>JXTree</code> with each element of the specified
     * Vector as the child of a new root node which is not displayed.
     * By default, this tree defines a leaf node as any node without children.
     *
     * This version of the constructor simply invokes the super class version
     * with the same arguments.
     *
     * @param value an Vector of objects that are children of the root.
     */
    public JXTree(Vector value) {
        super(value);
    init();
    }

    /**
     * Constructs a <code>JXTree</code> created from a Hashtable which does not
     * display with root. Each value-half of the key/value pairs in the HashTable
     * becomes a child of the new root node. By default, the tree defines a leaf
     * node as any node without children.
     *
     * This version of the constructor simply invokes the super class version
     * with the same arguments.
     *
     * @param value a Hashtable containing objects that are children of the root.
     */
    public JXTree(Hashtable value) {
        super(value);
    init();
    }

    /**
     * Constructs a <code>JXTree</code> with the specified TreeNode as its root,
     * which displays the root node. By default, the tree defines a leaf node as
     * any node without children.
     *
     * This version of the constructor simply invokes the super class version
     * with the same arguments.
     *
     * @param root root node of this tree
     */
    public JXTree(TreeNode root) {
        super(root, false);
        init();
    }

    /**
     * Constructs a <code>JXTree</code> with the specified TreeNode as its root,
     * which displays the root node and which decides whether a node is a leaf
     * node in the specified manner.
     *
     * This version of the constructor simply invokes the super class version
     * with the same arguments.
     *
     * @param root root node of this tree
     * @param asksAllowsChildren if true, only nodes that do not allow children
     * are leaf nodes; otherwise, any node without children is a leaf node;
     * @see javax.swing.tree.DefaultTreeModel#asksAllowsChildren
     */
    public JXTree(TreeNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
    init();
    }

    /**
     * Constructs an instance of <code>JXTree</code> which displays the root
     * node -- the tree is created using the specified data model.
     * 
     * This version of the constructor simply invokes the super class version
     * with the same arguments.
     * 
     * @param newModel
     *            the <code>TreeModel</code> to use as the data model
     */
    public JXTree(TreeModel newModel) {
        super(newModel);
        init();
    }

    @Override
    public void setModel(TreeModel newModel) {
        // To support delegation of convertValueToText() to the model...
        // JW: method needs to be set before calling super
        // otherwise there are size caching problems
        conversionMethod = getValueConversionMethod(newModel);
        super.setModel(newModel);
    }

    /**
     * Tries to find and return a method for Object --> to String conversion on the
     * model by reflection. Looks for a signature:
     * 
     * <pre> <code>
     *   String convertValueToText(Object);
     * </code> </pre>
     * 
     * 
     * 
     * PENDING JW: check - does this work with restricted permissions?
     * JW: widened access for testing - do test!
     * 
     * @param model the model to detect the method
     * @return the <code> Method </code> or null if the model has no method with
     *   the expected signature 
     */
    protected Method getValueConversionMethod(TreeModel model) {
        try {
            return model == null ? null : model.getClass().getMethod(
                    "convertValueToText", methodSignature);
        } catch (NoSuchMethodException ex) {
            LOG.finer("ex " + ex);
            LOG.finer("no conversionMethod in " + model.getClass());
        }
        return null;
    }

    
    @Override
    public String convertValueToText(Object value, boolean selected,
            boolean expanded, boolean leaf, int row, boolean hasFocus) {
        // Delegate to model, if possible. Otherwise fall back to superclass...
        if (value != null) {
            if (conversionMethod == null) {
                return value.toString();
            } else {
                try {
                    methodArgs[0] = value;
                    return (String) conversionMethod.invoke(getModel(),
                            methodArgs);
                } catch (Exception ex) {
                    LOG.finer("ex " + ex);
                    LOG.finer("can't invoke " + conversionMethod);
                }
            }
        }
        return "";
    }

    private void init() {
        // To support delegation of convertValueToText() to the model...
        // JW: need to set again (is done in setModel, but at call
        // in super constructor the field is not yet valid)
        conversionMethod = getValueConversionMethod(getModel());

        // Register the actions that this class can handle.
        ActionMap map = getActionMap();
        map.put("expand-all", new Actions("expand-all"));
        map.put("collapse-all", new Actions("collapse-all"));
    }

    /**
     * Listens to the model and updates the {@code expandedState} accordingly
     * when nodes are removed, or changed.
     * <p>
     * This class will expand an invisible root when a child has been added to
     * it.
     * 
     * @author Karl George Schaefer
     */
    protected class XTreeModelHandler extends TreeModelHandler {
        /**
         * {@inheritDoc}
         */
        @Override
        public void treeNodesInserted(TreeModelEvent e) {
            TreePath path = e.getTreePath();
            
            //fixes SwingX bug #612
            if (path.getParentPath() == null && !isRootVisible() && isCollapsed(path)) {
                //should this be wrapped in SwingUtilities.invokeLater?
                expandPath(path);
            }
            
            super.treeNodesInserted(e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected TreeModelListener createTreeModelListener() {
        return new XTreeModelHandler();
    }

    /**
     * A small class which dispatches actions.
     * TODO: Is there a way that we can make this static?
     */
    private class Actions extends UIAction {
        Actions(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent evt) {
            if ("expand-all".equals(getName())) {
        expandAll();
            }
            else if ("collapse-all".equals(getName())) {
                collapseAll();
            }
        }
    }
   
 
    
    /**
     * Collapses all nodes in the tree table.
     */
    public void collapseAll() {
        for (int i = getRowCount() - 1; i >= 0 ; i--) {
            collapseRow(i);
        }
    }

    /**
     * Expands all nodes in the tree table.
     */
    public void expandAll() {
        if (getRowCount() == 0) {
            expandRoot();
        }
        for (int i = 0; i < getRowCount(); i++) {
            expandRow(i);
        }
    }

    /**
     * Expands the root path, assuming the current TreeModel has been set.
     */
    private void expandRoot() {
        TreeModel              model = getModel();
        if(model != null && model.getRoot() != null) {
            expandPath(new TreePath(model.getRoot()));
        }
    }

    /**
     * overridden to always return a not-null array 
     * (following SwingX convention).
     */
    @Override
    public int[] getSelectionRows() {
        int[] rows = super.getSelectionRows();
        return rows != null ? rows : EMPTY_INT_ARRAY; 
    }
    
    

    /**
     * overridden to always return a not-null array 
     * (following SwingX convention).
     */
    @Override
    public TreePath[] getSelectionPaths() {
        // TODO Auto-generated method stub
        TreePath[] paths = super.getSelectionPaths();
        return paths != null ? paths : EMPTY_TREEPATH_ARRAY; 
    }    

    /**
     * sets the icon for the handle of an expanded node.
     * 
     * Note: this will only succeed if the current ui delegate is
     * a BasicTreeUI otherwise it will do nothing.
     * 
     * @param expanded
     */
    public void setExpandedIcon(Icon expanded) {
        if (getUI() instanceof BasicTreeUI) {
            ((BasicTreeUI) getUI()).setExpandedIcon(expanded);
        }
    }
    
    /**
     * sets the icon for the handel of a collapsed node.
     * 
     * Note: this will only succeed if the current ui delegate is
     * a BasicTreeUI otherwise it will do nothing.
     *  
     * @param collapsed
     */
    public void setCollapsedIcon(Icon collapsed) {
        if (getUI() instanceof BasicTreeUI) {
            ((BasicTreeUI) getUI()).setCollapsedIcon(collapsed);
        }
    }
        
    /**
     * Property to control whether per-tree icons should be 
     * copied to the renderer on setCellRenderer.
     * 
     * the default is false for backward compatibility.
     * 
     * PENDING: should update the current renderer's icons when 
     * setting to true?
     * 
     * @param overwrite
     */
    public void setOverwriteRendererIcons(boolean overwrite) {
        if (overwriteIcons == overwrite) return;
        boolean old = overwriteIcons;
        this.overwriteIcons = overwrite;
        firePropertyChange("overwriteRendererIcons", old, overwrite);
    }

    public boolean isOverwriteRendererIcons() {
        return overwriteIcons;
    }
    

    
//----------------------- edit
    
    /**
     * {@inheritDoc} <p>
     * Overridden to fix focus issues with editors. 
     * This method installs and updates the internal CellEditorRemover which
     * to terminates ongoing edits if appropriate. Additionally, it
     * registers a CellEditorListener with the cell editor to grab the 
     * focus back to tree, if appropriate.
     * 
     * @see #updateEditorRemover()
     */
    @Override
    public void startEditingAtPath(TreePath path) {
        super.startEditingAtPath(path);
        if (isEditing()) {
            updateEditorListener();
            updateEditorRemover();
        }
    }

    
    /**
     * Hack to grab focus after editing.
     */
    private void updateEditorListener() {
        if (editorListener == null) {
            editorListener = new CellEditorListener() {

                public void editingCanceled(ChangeEvent e) {
                    terminated(e);
                }

                /**
                 * @param e
                 */
                private void terminated(ChangeEvent e) {
                    analyseFocus();
                    ((CellEditor) e.getSource()).removeCellEditorListener(editorListener);
                }

                public void editingStopped(ChangeEvent e) {
                    terminated(e);
                }
                
            };
        }
        getCellEditor().addCellEditorListener(editorListener);

    }

    /**
     * This is called from cell editor listener if edit terminated.
     * Trying to analyse if we should grab the focus back to the
     * tree after. Brittle ... we assume we are the first to 
     * get the event, so we can analyse the hierarchy before the
     * editing component is removed.
     */
    protected void analyseFocus() {
        final boolean isFocusOwnerInTheTable = isFocusOwnerDescending();    
        if (isFocusOwnerInTheTable) {
            requestFocusInWindow();
        }
    }


    /**
     * Returns a boolean to indicate if the current focus owner 
     * is descending from this table. 
     * Returns false if not editing, otherwise walks the focusOwner
     * hierarchy, taking popups into account. <p>
     * 
     * PENDING: copied from JXTable ... should be somewhere in a utility
     * class?
     * 
     * @return a boolean to indicate if the current focus
     *   owner is contained.
     */
    private boolean isFocusOwnerDescending() {
        if (!isEditing()) return false;
        Component focusOwner = 
            KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        // PENDING JW: special casing to not fall through ... really wanted?
        if (focusOwner == null) return false;
        if (isDescending(focusOwner)) return true;
        // same with permanent focus owner
        Component permanent = 
            KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        return isDescending(permanent);
    }

    /**
     * PENDING: copied from JXTable ... should be somewhere in a utility
     * class?
     * 
     * @param focusOwner
     * @return
     */
    private boolean isDescending(Component focusOwner) {
        while (focusOwner !=  null) {
            if (focusOwner instanceof JPopupMenu) {
                focusOwner = ((JPopupMenu) focusOwner).getInvoker();
                if (focusOwner == null) {
                    return false;
                }
            }
            if (focusOwner == this) {
                return true;
            }
            focusOwner = focusOwner.getParent();
        }
        return false;
    }


    /**
     * Overridden to release the CellEditorRemover, if any.
     */
    @Override
    public void removeNotify() {
        if (editorRemover != null) {
            editorRemover.release();
            editorRemover = null;
        }
        super.removeNotify();
    }

    /**
     * Lazily creates and updates the internal CellEditorRemover.
     * 
     *
     */
    private void updateEditorRemover() {
        if (editorRemover == null) {
            editorRemover = new CellEditorRemover();
        }
        editorRemover.updateKeyboardFocusManager();
    }

    /** This class tracks changes in the keyboard focus state. It is used
     * when the JXTree is editing to determine when to terminate the edit.
     * If focus switches to a component outside of the JXTree, but in the
     * same window, this will terminate editing. The exact terminate 
     * behaviour is controlled by the invokeStopEditing property.
     * 
     * @see javax.swing.JTree#setInvokesStopCellEditing(boolean)
     * 
     */
    public class CellEditorRemover implements PropertyChangeListener {
        /** the focusManager this is listening to. */
        KeyboardFocusManager focusManager;

        public CellEditorRemover() {
            updateKeyboardFocusManager();
        }

        /**
         * Updates itself to listen to the current KeyboardFocusManager. 
         *
         */
        public void updateKeyboardFocusManager() {
            KeyboardFocusManager current = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            setKeyboardFocusManager(current);
        }

        /**
         * stops listening.
         *
         */
        public void release() {
            setKeyboardFocusManager(null);
        }
        
        /**
         * Sets the focusManager this is listening to. 
         * Unregisters/registers itself from/to the old/new manager, 
         * respectively. 
         * 
         * @param current the KeyboardFocusManager to listen too.
         */
        private void setKeyboardFocusManager(KeyboardFocusManager current) {
            if (focusManager == current)
                return;
            KeyboardFocusManager old = focusManager;
            if (old != null) {
                old.removePropertyChangeListener("permanentFocusOwner", this);
            }
            focusManager = current;
            if (focusManager != null) {
                focusManager.addPropertyChangeListener("permanentFocusOwner",
                        this);
            }

        }
        public void propertyChange(PropertyChangeEvent ev) {
            if (!isEditing()) {
                return;
            }

            Component c = focusManager.getPermanentFocusOwner();
            JXTree tree = JXTree.this;
            while (c != null) {
                if (c instanceof JPopupMenu) {
                    c = ((JPopupMenu) c).getInvoker();
                } else {

                    if (c == tree) {
                        // focus remains inside the table
                        return;
                    } else if ((c instanceof Window) ||
                            (c instanceof Applet && c.getParent() == null)) {
                        if (c == SwingUtilities.getRoot(tree)) {
                            if (tree.getInvokesStopCellEditing()) {
                                tree.stopEditing();
                            }
                            if (tree.isEditing()) {
                                tree.cancelEditing();
                            }
                        }
                        break;
                    }
                    c = c.getParent();
                }
            }
        }
    }

    
    /**
     * @return the unconfigured ComponentAdapter.
     */
    protected ComponentAdapter getComponentAdapter() {
        if (dataAdapter == null) {
            dataAdapter = new TreeAdapter(this);
        }
        return dataAdapter;
    }

    /**
     * Convenience to access a configured ComponentAdapter.
     * Note: the column index of the configured adapter is always 0.
     * 
     * @param index the row index in view coordinates, must be valid.
     * @return the configured ComponentAdapter.
     */
    protected ComponentAdapter getComponentAdapter(int index) {
        ComponentAdapter adapter = getComponentAdapter();
        adapter.column = 0;
        adapter.row = index;
        return adapter;
    }

    protected ComponentAdapter dataAdapter;

    protected static class TreeAdapter extends ComponentAdapter {
        private final JXTree tree;

        /**
         * Constructs a <code>TableCellRenderContext</code> for the specified
         * target component.
         *
         * @param component the target component
         */
        public TreeAdapter(JXTree component) {
            super(component);
            tree = component;
        }
        public JXTree getTree() {
            return tree;
        }

        public boolean hasFocus() {
            return tree.isFocusOwner() && (tree.getLeadSelectionRow() == row);
        }

        public Object getValueAt(int row, int column) {
            TreePath path = tree.getPathForRow(row);
            return path.getLastPathComponent();
        }

        public Object getFilteredValueAt(int row, int column) {
            /** TODO: Implement filtering */
            return getValueAt(row, column);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEditable() {
            //this is not as robust as JXTable; should it be? -- kgs
            return tree.isPathEditable(tree.getPathForRow(row));
        }
        
        public boolean isSelected() {
            return tree.isRowSelected(row);
        }

        @Override
        public boolean isExpanded() {
            return tree.isExpanded(row);
        }

        @Override
        public int getDepth() {
            return tree.getPathForRow(row).getPathCount() - 1;
        }
        
        @Override
        public boolean isLeaf() {
            return tree.getModel().isLeaf(getValue());
        }

        public boolean isCellEditable(int row, int column) {
            return false;   /** TODO:  */
        }

        public void setValueAt(Object aValue, int row, int column) {
            /** TODO:  */
        }
        
        public String getColumnName(int columnIndex) {
            return "Column_" + columnIndex;
        }
        
        public String getColumnIdentifier(int columnIndex) {
            return null;
        }
    }


}