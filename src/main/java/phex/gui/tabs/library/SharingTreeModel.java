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
 *  Created on 13.07.2005
 *  --- CVS Information ---
 *  $Id: SharingTreeModel.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.library;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.bushe.swing.event.annotation.EventTopicSubscriber;

import phex.event.PhexEventTopics;
import phex.gui.actions.GUIActionPerformer;
import phex.gui.common.GUIRegistry;
import phex.servent.Servent;
import phex.share.SharedDirectory;
import phex.share.SharedFilesService;
import phex.utils.DirectoryOnlyFileFilter;
import phex.utils.Localizer;

public class SharingTreeModel implements TreeModel
{    
    private final SharedFilesService fileService;
    private final FileFilter dirFilter = new DirectoryOnlyFileFilter();
    
    /** Listeners. */
    protected final EventListenerList listenerList = new EventListenerList();
    private final RootTreeNode root;
    private final FileSystemView fsv;
    
    public SharingTreeModel()
    {
        Servent servent = GUIRegistry.getInstance().getServent();
        fileService = servent.getSharedFilesService();
        
        root = new RootTreeNode( );
        fsv = FileSystemView.getFileSystemView();
        updateFileSystem();
        
        
        servent.getEventService().processAnnotations( this );
    }
    
    /**
     * 
     */
    public void updateFileSystem()
    {
        root.updateChilds();
        fireTreeStructureChanged();
    }

    /**
     * @see javax.swing.tree.TreeModel#getRoot()
     */
    public Object getRoot()
    {
        return root;
    }
    
    /**
     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
     */
    public int getChildCount( Object node )
    {
        return ((TreeNode)node).getChildCount();
    }

    /**
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    public Object getChild(Object node, int index)
    {
        return ((TreeNode)node).getChildAt(index);
    }

    

    /**
     * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
     */
    public boolean isLeaf(Object node)
    {
        return ((TreeNode)node).isLeaf();
    }

    /**
     * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
     */
    public void valueForPathChanged(TreePath path, Object newValue)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
     */
    public int getIndexOfChild(Object parent, Object child)
    {
        return ((TreeNode)parent).getIndex((TreeNode) child);
    }

    //
    //  Events
    //

    /**
     * Adds a listener for the TreeModelEvent posted after the tree changes.
     *
     * @see     #removeTreeModelListener
     * @param   l       the listener to add
     */
    public void addTreeModelListener(TreeModelListener l)
    {
        listenerList.add(TreeModelListener.class, l);
    }

    /**
     * Removes a listener previously added with <B>addTreeModelListener()</B>.
     *
     * @see     #addTreeModelListener
     * @param   l       the listener to remove
     */
    public void removeTreeModelListener(TreeModelListener l)
    {
        listenerList.remove(TreeModelListener.class, l);
    }
    
    /**
     * 
     */
    private void fireTreeStructureChanged()
    {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent( this, new TreePath( new Object[] {root,root.childs[0]} ) );
                ((TreeModelListener)listeners[i+1]).treeStructureChanged(e);
            }
        }
    }
    
    @EventTopicSubscriber(topic=PhexEventTopics.Share_Update)
    public void onShareUpdateEvent( String topic, Object event )
    {
        EventQueue.invokeLater( new Runnable()
        {
            public void run()
            {
                updateFileSystem();
            }
        });
    }
    
    private class RootTreeNode implements TreeNode
    {
        private TreeNode[] childs;
        
        public RootTreeNode()
        {
            childs = new TreeNode[] 
            {
              new SharedFilesTreeNode()
            };
            updateChilds();
        }
        
        public void updateChilds()
        {
            ((SharedFilesTreeNode)childs[0]).updateChilds();
        }
        
        public TreeNode getChildAt( int childIndex )
        {
            return childs[childIndex];
        }

        public int getChildCount()
        {
            return childs.length;
        }

        public TreeNode getParent()
        {
            return null;
        }

        public boolean isLeaf()
        {
            return false;
        }
        
        public int getIndex( TreeNode node )
        {
            for ( int i = 0; i < childs.length; i++ )
            {
                if ( childs[i] == node )
                {
                    return i;
                }
            }
            return -1;
        }

        public boolean getAllowsChildren()
        {
            throw new UnsupportedOperationException();
        }
        
        public Enumeration children()
        {
            throw new UnsupportedOperationException();
        }
    }
    
    private class SharedFilesTreeNode implements TreeNode, LibraryNode
    {
        private TreeNode[] childs;
        
        public SharedFilesTreeNode()
        {
            updateChilds();
        }
        
        public String getDisplayName()
        {
            return Localizer.getString( "LibraryTab_SharedFiles" );
        }
        
        public String getTooltipText()
        {
            return Localizer.getString( "LibraryTab_SharedFiles" );
        }
        
        public Icon getDisplayIcon()
        {
            return GUIRegistry.getInstance().getPlafIconPack().getIcon(
                "Library.ShareFolder");
        }
        
        public File getSystemFile()
        {
            return null;
        }
        
        public void updateChilds()
        {
            ArrayList childsList = new ArrayList();
            SharedDirectory[] sharedDirs = fileService.getSharedDirectories(); 
            for ( int i = 0; i < sharedDirs.length; i++ )
            {
                if ( sharedDirs[i].getType() == SharedDirectory.UNSHARED_PARENT_DIRECTORY )
                {
                    continue;
                }
                File parent = sharedDirs[i].getSystemFile().getParentFile();
                SharedDirectory sharedDirectory = null;
                if ( parent != null )
                {
                    sharedDirectory = fileService.getSharedDirectory( parent );
                }
                if (    parent == null
                     || sharedDirectory == null 
                     || sharedDirectory.getType() == SharedDirectory.UNSHARED_PARENT_DIRECTORY )
                {
                    // show directory in case parent is not shared too.
                    childsList.add( new SharedDirectoryTreeNode( sharedDirs[i], this ) );
                }
            }
            childs = (TreeNode[])childsList.toArray(new TreeNode[childsList.size()]);
            
            // TODO2 we can fire a row of events here to optimize redraw.
            // but we still need a way to auto refresh the whole structure on user request!
            
        }
        
        public TreeNode getChildAt( int childIndex )
        {            
            return childs[childIndex];
        }

        public int getChildCount()
        {
            return childs.length;
        }

        public TreeNode getParent()
        {
            return SharingTreeModel.this.root;
        }

        public boolean isLeaf()
        {
            return false;
        }
        
        public int getIndex( TreeNode node )
        {
            for ( int i = 0; i < childs.length; i++ )
            {
                if ( childs[i] == node )
                {
                    return i;
                }
            }
            return -1;
        }

        public boolean getAllowsChildren()
        {
            throw new UnsupportedOperationException();
        }
        
        public Enumeration children()
        {
            throw new UnsupportedOperationException();
        }
    }
    
    private class SharedDirectoryTreeNode implements TreeNode, LibraryNode
    {
        private SharedDirectory sharedDirectory;
        private TreeNode[] childs;
        private TreeNode parent;
        
        public SharedDirectoryTreeNode( SharedDirectory dir, TreeNode parent )
        {
            sharedDirectory = dir;
            this.parent = parent;
        }
        
        public String getDisplayName()
        {
            return fsv.getSystemDisplayName( sharedDirectory.getSystemFile() );
        }
        
        public String getTooltipText()
        {
            return sharedDirectory.getSystemFile().getAbsolutePath();
        }
        
        public Icon getDisplayIcon()
        {
            try
            {            
                return fsv.getSystemIcon( sharedDirectory.getSystemFile() );
            }
            catch ( NullPointerException exp )
            {// A NullPointerException can be thrown from getSystemIcon in
             // in case the system file dosn't exists anymore on the file system.
             // We return null and trigger a rescan to fix this problem.
                GUIActionPerformer.rescanSharedFiles();
                return null;
            }
        }
        
        public File getSystemFile()
        {
            return sharedDirectory.getSystemFile();
        }
        
        public TreeNode getChildAt( int childIndex )
        {
            initChilds();
            return childs[childIndex];
        }

        public int getChildCount()
        {
            initChilds();
            return childs.length;
        }

        public TreeNode getParent()
        {
            return parent;
        }

        public boolean isLeaf()
        {
            initChilds();
            return childs.length == 0;
        }
        
        public int getIndex( TreeNode node )
        {
            for ( int i = 0; i < childs.length; i++ )
            {
                if ( childs[i] == node )
                {
                    return i;
                }
            }
            return -1;
        }

        public boolean getAllowsChildren()
        {
            throw new UnsupportedOperationException();
        }
        
        public Enumeration children()
        {
            throw new UnsupportedOperationException();
        }
        
        private void initChilds()
        {
            if ( childs != null )
            {
                return;
            }
            ArrayList<TreeNode> childsList = new ArrayList<TreeNode>();
            File dir = sharedDirectory.getSystemFile();
            File[] childDirs = dir.listFiles(dirFilter);
            if ( childDirs != null )
            {
                for ( int i = 0; i < childDirs.length; i++ )
                {
                    SharedDirectory sharedDir = fileService.getSharedDirectory(childDirs[i]);
                    if ( sharedDir != null && sharedDir.getType() == SharedDirectory.SHARED_DIRECTORY )
                    {
                        childsList.add( new SharedDirectoryTreeNode( sharedDir, this ) );
                    }
                }
            }
            childs = childsList.toArray(new TreeNode[childsList.size()]);
        }
    }
}
