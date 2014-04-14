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
 *  $Id: FileSystemTreeRenderer.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.library;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;

import phex.gui.common.GUIRegistry;
import phex.gui.common.IconPack;
import phex.gui.common.ImageFilterUtils;
import phex.servent.Servent;
import phex.share.SharedDirectory;

/**
 * @author gregor
 */
public class FileSystemTreeRenderer extends DefaultTreeCellRenderer
{
    private Icon sharedOpenIcon;

    private Icon sharedClosedIcon;

    private Icon unsharedOpenIcon;

    private Icon unsharedClosedIcon;

    private Icon partlySharedOpenIcon;

    private Icon partlySharedClosedIcon;
    
    private FileSystemView fsv;

    /**
     * 
     */
    public FileSystemTreeRenderer()
    {
        IconPack factory = GUIRegistry.getInstance().getPlafIconPack();
        setOpenIcon(factory.getIcon("Library.FileTreeOpen"));
        setClosedIcon(factory.getIcon("Library.FileTreeClosed"));

        sharedOpenIcon = factory.getIcon("Library.FileTreeClosed");
        sharedClosedIcon = factory.getIcon("Library.FileTreeClosed");
        partlySharedOpenIcon = HalfGrayedImageFilter.createHalfGrayIcon(factory.getIcon("Library.FileTreeClosed"));
        partlySharedClosedIcon = HalfGrayedImageFilter.createHalfGrayIcon(factory.getIcon("Library.FileTreeClosed"));
        unsharedOpenIcon = ImageFilterUtils.createGrayIcon(factory.getIcon("Library.FileTreeClosed"));
        unsharedClosedIcon = ImageFilterUtils.createGrayIcon(factory.getIcon("Library.FileTreeClosed"));
        
        fsv = FileSystemView.getFileSystemView();
    }

    /**
     * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean selected, boolean expanded, boolean leaf, int row,
        boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, selected, expanded,
            leaf, row, hasFocus);
        if (value == tree.getModel().getRoot())
        {
            setIcon(null);
            setText("");
            setMinimumSize(new Dimension(0, 0));
            setPreferredSize(new Dimension(0, 0));
            setMaximumSize(new Dimension(0, 0));
            return this;
        }
        setMinimumSize(null);
        setPreferredSize(null);
        setMaximumSize(null);
        File file = (File) value;
        String fname = fsv.getSystemDisplayName(file);
        if (fname == null || fname.length() == 0)
        {
            fname = file.getPath();
            
        }
        setText(fname);

        SharedDirectory sharedDir = GUIRegistry.getInstance().getServent().getSharedFilesService()
            .getSharedDirectory( file );
        if ( sharedDir == null )
        {
            Icon icon = fsv.getSystemIcon(file);
            setIcon( ImageFilterUtils.createGrayIcon(icon));
//            if (expanded)
//            {
//                setIcon(unsharedOpenIcon);
//            }
//            else
//            {
//                setIcon(unsharedClosedIcon);
//            }
        }
        else if ( sharedDir.getType() == SharedDirectory.UNSHARED_PARENT_DIRECTORY )
        {
            Icon icon = fsv.getSystemIcon(file);
            setIcon( HalfGrayedImageFilter.createHalfGrayIcon(icon));
//            if (expanded)
//            {
//                setIcon(partlySharedOpenIcon);
//            }
//            else
//            {
//                setIcon(partlySharedClosedIcon);
//            }
        }
        else if ( sharedDir.getType() == SharedDirectory.SHARED_DIRECTORY )
        {
            if (expanded)
            {
                setIcon(sharedOpenIcon);
            }
            else
            {
                setIcon(sharedClosedIcon);
            }
        }
        
        //setIcon( fsv.getSystemIcon(file) );
        return this;
    }
}