/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2008 Phex Development Group
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
 *  $Id: LibraryTreePane.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.library;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.SystemUtils;

import phex.common.Environment;
import phex.common.log.NLogger;
import phex.gui.actions.FWAction;
import phex.gui.actions.GUIActionPerformer;
import phex.gui.common.FWPopupMenu;
import phex.gui.common.FWToolBar;
import phex.gui.common.FileDialogHandler;
import phex.gui.common.GUIRegistry;
import phex.prefs.core.LibraryPrefs;
import phex.servent.Servent;
import phex.share.SharedDirectory;
import phex.share.SharedFilesService;
import phex.utils.DirectoryOnlyFileFilter;
import phex.utils.Localizer;
import phex.utils.SystemShellExecute;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class LibraryTreePane extends JPanel
{
    private Component parent;
    private SharingTreeModel sharingTreeModel;
    private JTree mainTree;
    private FWPopupMenu fileTreePopup;
    
    private AddShareFolderAction addShareFolderAction;
    private RemoveShareFolderAction removeShareFolderAction;
    private ExploreFolderAction exploreFolderAction;
    
    public LibraryTreePane( Component parent )
    {
        this.parent = parent;
        prepareComponent();
    }
    
    public void addTreeSelectionListener( TreeSelectionListener listener )
    {
        mainTree.getSelectionModel().addTreeSelectionListener( listener );
    }
    
    public void appendPopupSeparator()
    {
        fileTreePopup.addSeparator();
    }
    
    public void appendPopupAction( FWAction action )
    {
        fileTreePopup.addAction( action );
    }
    
    public LibraryNode getSelectedTreeComponent()
    {
        TreePath path = mainTree.getSelectionPath();
        if (path == null)
        {
            return null;
        }
        LibraryNode node = (LibraryNode) path.getLastPathComponent();
        return node;
    }
    
    public void updateFileSystem()
    {
        sharingTreeModel.updateFileSystem();
    }
    
    /**
     * 
     */
    private void refreshActions()
    {
        addShareFolderAction.refreshActionState();
        removeShareFolderAction.refreshActionState();
        if ( exploreFolderAction != null )
        {
            exploreFolderAction.refreshActionState();
        }
    }
    
    private void prepareComponent()
    {
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout("fill:d:grow", // columns
            "fill:d:grow, 1dlu, p"); //rows
        PanelBuilder tabBuilder = new PanelBuilder( layout, this );
        
        sharingTreeModel = new SharingTreeModel();
        mainTree = new JTree( sharingTreeModel );
        mainTree.setMinimumSize( new Dimension( 0, 0 ) );
        mainTree.setRowHeight(0);
        mainTree.setCellRenderer(new SharingTreeRenderer());
        mainTree.addMouseListener( new MouseHandler() );
        
        mainTree.getSelectionModel().addTreeSelectionListener(
            new SelectionHandler());
        ToolTipManager.sharedInstance().registerComponent( mainTree );
        
        // open up first level of nodes
        TreeNode root = (TreeNode) sharingTreeModel.getRoot();
        int count = root.getChildCount();
        for ( int i = 0; i < count; i++ )
        {
            mainTree.expandPath( new TreePath( new Object[] {root, root.getChildAt(i)} ) );
        }

        JScrollPane treeScrollPane = new JScrollPane(mainTree);
        tabBuilder.add(treeScrollPane, cc.xywh(1, 1, 1, 1));
        
        FWToolBar shareToolbar = new FWToolBar(FWToolBar.HORIZONTAL);
        shareToolbar.setBorderPainted(false);
        shareToolbar.setFloatable(false);
        tabBuilder.add(shareToolbar, cc.xy(1, 3));
        
        addShareFolderAction = new AddShareFolderAction();
        shareToolbar.addAction( addShareFolderAction );
        
        removeShareFolderAction = new RemoveShareFolderAction();
        shareToolbar.addAction( removeShareFolderAction );
        
        if ( SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_MAC_OSX )
        {
            exploreFolderAction = new ExploreFolderAction();
        }
        
        fileTreePopup = new FWPopupMenu();
        fileTreePopup.addAction( addShareFolderAction );
        fileTreePopup.addAction( removeShareFolderAction );
        
        if ( SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_MAC_OSX )
        {
            fileTreePopup.addAction( exploreFolderAction );
        }
    }
    
    

    /**
     * Starts a download.
     */
    private class AddShareFolderAction extends FWAction
    {
        AddShareFolderAction()
        {
            super( Localizer.getString("LibraryTab_Share"), 
                GUIRegistry.getInstance().getPlafIconPack().getIcon( "Library.ShareFolder"), 
                Localizer.getString("LibraryTab_TTTShare"));
        }

        public void actionPerformed(ActionEvent e)
        {
            try
            {
                File currentDirectory = null;
                TreePath selectionPath = mainTree.getSelectionPath();
                if ( selectionPath != null )
                {
                    Object lastPathComponent = selectionPath.getLastPathComponent();
                    if ( lastPathComponent instanceof LibraryNode )
                    {
                        currentDirectory = ((LibraryNode)lastPathComponent).getSystemFile();
                    }
                }
                
                final File[] files = FileDialogHandler.openMultipleDirectoryChooser( 
                    parent,
                    Localizer.getString( "LibraryTab_SelectDirectoryToShare" ),
                    Localizer.getString( "LibraryTab_Select" ), 
                    Localizer.getChar( "LibraryTab_SelectMnemonic" ),
                    currentDirectory,
                    Localizer.getString( "LibraryTab_CopyrightWarnTitle" ),
                    Localizer.getString( "LibraryTab_CopyrightWarnMessage" ) );
                if ( files == null )
                {
                    return;
                }
                Runnable runner = new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            for ( int i = 0; i < files.length; i++ )
                            {
                                if ( files[i] != null )
                                {
                                    shareDirRecursive(files[i]);
                                }
                            }
                            GUIActionPerformer.rescanSharedFiles();
                        }
                        catch ( Throwable th )
                        {
                            NLogger.error( LibraryTreePane.class, th, th );
                        }
                    }
                };
                Environment.getInstance().executeOnThreadPool(runner, "AddShareFolderAction");
            }
            catch ( Throwable th )
            {
                NLogger.error( LibraryTreePane.class, th, th );
            }
        }

        @Override
        public void refreshActionState()
        {
        }
        
        private void shareDirRecursive(File file)
        {
            if (!file.isDirectory())
            {
                return;
            }
            LibraryPrefs.SharedDirectoriesSet.get().add( file.getAbsolutePath() );
            LibraryPrefs.SharedDirectoriesSet.changed();
            File[] dirs = file.listFiles(new DirectoryOnlyFileFilter());
            if ( dirs == null )
            {// not a valid path or an IO error.
                return;
            }
            for (int i = 0; i < dirs.length; i++)
            {
                shareDirRecursive(dirs[i]);
            }
        }
    }
    
    private class RemoveShareFolderAction extends FWAction
    {
        RemoveShareFolderAction()
        {
            super( Localizer.getString("LibraryTab_StopShare"), 
                GUIRegistry.getInstance().getPlafIconPack().getIcon("Library.ShareFolderClear"), 
                Localizer.getString("LibraryTab_TTTStopShare"));
            refreshActionState();
        }

        public void actionPerformed(ActionEvent e)
        {
            TreePath selectionPath = mainTree.getSelectionPath();
            if ( selectionPath == null )
            {
                return;
            }
            Object lastPathComponent = selectionPath.getLastPathComponent();
            if ( !(lastPathComponent instanceof LibraryNode ))
            {
                return;
                
            }
            final File file = ((LibraryNode)lastPathComponent).getSystemFile();
            if ( file == null )
            {
                return;
            }
            Runnable runner = new Runnable()
            {
                public void run()
                {
                    stopShareDirRecursive(file);
                    GUIActionPerformer.rescanSharedFiles();
                }
            };
            Environment.getInstance().executeOnThreadPool(runner, "RemoveShareFolderAction");
            refreshActionState();
        }
        
        private void stopShareDirRecursive(File file)
        {
            if (!file.isDirectory())
            {
                return;
            }
            LibraryPrefs.SharedDirectoriesSet.get().remove( file.getAbsolutePath() );
            LibraryPrefs.SharedDirectoriesSet.changed();
            SharedFilesService sharedFilesService = GUIRegistry.getInstance().getServent().getSharedFilesService();
            SharedDirectory directory = sharedFilesService.getSharedDirectory(file);
            if (directory == null)
            {
                // in case there is no shared directory here..
                // we can assume there is no shared sub-directory available.
                return;
            }
            
            File[] dirs = file.listFiles(new DirectoryOnlyFileFilter());
            for (int i = 0; i < dirs.length; i++)
            {
                stopShareDirRecursive(dirs[i]);
            }
        }

        @Override
        public void refreshActionState()
        {
            TreePath selectionPath = mainTree.getSelectionPath();
            if ( selectionPath == null )
            {
                setEnabled(false);
                return;
            }
            Object lastPathComponent = selectionPath.getLastPathComponent();
            if ( !(lastPathComponent instanceof LibraryNode ))
            {
                setEnabled(false);
                return;
            }
            File file = ((LibraryNode)lastPathComponent).getSystemFile();
            if ( file == null )
            {
                setEnabled(false);
                return;
            }
            setEnabled(true);
        }
    }
    
    private class ExploreFolderAction extends FWAction
    {
        ExploreFolderAction()
        {
            super(Localizer.getString("LibraryTab_Explore"), 
                GUIRegistry.getInstance().getPlafIconPack().getIcon("Library.Explore"),
                Localizer.getString("LibraryTab_TTTExplore"));
        }
        
        public void actionPerformed(ActionEvent e)
        {
            TreePath selectionPath = mainTree.getSelectionPath();
            if ( selectionPath == null )
            {
                return;
            }
            Object lastPathComponent = selectionPath.getLastPathComponent();
            if ( !(lastPathComponent instanceof LibraryNode ))
            {
                return;
                
            }
            final File dir = ((LibraryNode)lastPathComponent).getSystemFile();
            if ( dir == null )
            {
                return;
            }
            
            try
            {
                SystemShellExecute.exploreFolder(dir);
            }
            catch (IOException exp)
            {// ignore and do nothing..
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void refreshActionState()
        {
            TreePath selectionPath = mainTree.getSelectionPath();
            if ( selectionPath == null )
            {
                setEnabled(false);
                return;
            }
            Object lastPathComponent = selectionPath.getLastPathComponent();
            if ( !(lastPathComponent instanceof LibraryNode ))
            {
                setEnabled(false);
                return;
            }
            File file = ((LibraryNode)lastPathComponent).getSystemFile();
            if ( file == null )
            {
                setEnabled(false);
                return;
            }
            setEnabled(true);
        }
    }
    
    private class SelectionHandler implements TreeSelectionListener
    {
        public void valueChanged( TreeSelectionEvent e )
        {
            final Object treeRoot = sharingTreeModel.getRoot();
            final Object lastPathComponent = e.getPath().getLastPathComponent();
            if ( lastPathComponent == treeRoot )
            {
                EventQueue.invokeLater( new Runnable()
                {
                    public void run()
                    {
                        Object[] path = new Object[]
                        { treeRoot, sharingTreeModel.getChild( treeRoot, 0 ) };
                        mainTree.setSelectionPath( new TreePath( path ) );
                    }
                } );
                return;
            }
            refreshActions();
        }
    }

    private class MouseHandler extends MouseAdapter implements MouseListener
    {
        @Override
        public void mouseClicked( MouseEvent e )
        {
        }

        @Override
        public void mouseReleased( MouseEvent e )
        {
            if ( e.isPopupTrigger() )
            {
                popupMenu( (Component) e.getSource(), e.getX(), e.getY() );
            }
        }

        @Override
        public void mousePressed( MouseEvent e )
        {
            if ( e.isPopupTrigger() )
            {
                popupMenu( (Component) e.getSource(), e.getX(), e.getY() );
            }
        }

        private void popupMenu( Component source, int x, int y )
        {
            refreshActions();
            fileTreePopup.show( source, x, y );
        }
    }
}