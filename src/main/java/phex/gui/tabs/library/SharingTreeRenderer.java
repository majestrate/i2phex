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
 *  Created on 12.12.2004
 *  --- CVS Information ---
 *  $Id: SharingTreeRenderer.java 3362 2006-03-30 22:27:26Z gregork $
 */
/* Created on 12.12.2004 */
package phex.gui.tabs.library;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * @author gregor
 */
public class SharingTreeRenderer extends DefaultTreeCellRenderer
{
    private FileSystemView fsv;

    /**
     * 
     */
    public SharingTreeRenderer()
    {
        fsv = FileSystemView.getFileSystemView();
    }

    /**
     * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
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
        
        if ( value instanceof LibraryNode )
        {
            LibraryNode node = (LibraryNode) value;
            setText( node.getDisplayName() );
            setToolTipText( node.getTooltipText() );
            Icon icon = node.getDisplayIcon();
            if ( icon != null )
            {
                setIcon( icon );
            }
        }
        
//        File file = (File) value;
//        String fname = fsv.getSystemDisplayName(file);
//        if (fname == null || fname.length() == 0)
//        {
//            fname = file.getPath();
//            
//        }
//        setText(value.toString());
//
//        SharedDirectory sharedDir = ShareManager.getInstance().getSharedFilesService()
//            .getSharedDirectory( file );
//        if ( sharedDir == null )
//        {
//            Icon icon = fsv.getSystemIcon(file);
//            setIcon( HalfGrayedImageFilter.createGrayIcon(icon));
        
        
        
        
//        }
//        else if ( sharedDir.getType() == SharedDirectory.PARTIALY_SHARED_DIRECTORY )
//        {
//            Icon icon = fsv.getSystemIcon(file);
//            setIcon( HalfGrayedImageFilter.createHalfGrayIcon(icon));
        
        

//        }
//        else if ( sharedDir.getType() == SharedDirectory.SHARED_DIRECTORY )
//        {
//            if (expanded)
//            {
//                setIcon(sharedOpenIcon);
//            }
//            else
//            {
//                setIcon(sharedClosedIcon);
//            }
//        }
        
        //setIcon( fsv.getSystemIcon(file) );
        return this;
    }
}