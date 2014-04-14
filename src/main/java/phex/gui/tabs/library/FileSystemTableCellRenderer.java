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
 *  $Id: FileSystemTableCellRenderer.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.gui.tabs.library;

import java.io.File;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.collections.map.LRUMap;

import phex.common.log.NLogger;
import phex.gui.common.GUIRegistry;
import phex.gui.common.ImageFilterUtils;
import phex.gui.renderer.FWTableCellRenderer;
import phex.share.ShareFile;

/**
 *
 */
public class FileSystemTableCellRenderer extends FWTableCellRenderer
{
    private Icon defaultIcon;
    private Icon defaultGrayIcon;
    
    private FileSystemView fsv;
    private Map<ShareFile, Icon> shareFileIconMap;
    private Map grayMap;
    
    public FileSystemTableCellRenderer()
    {
        fsv = FileSystemView.getFileSystemView();
        shareFileIconMap = new LRUMap( 100 );
        grayMap = new LRUMap( 100 );
        
        defaultIcon = GUIRegistry.getInstance().getPlafIconPack().getIcon("Library.File");
        defaultGrayIcon = ImageFilterUtils.createGrayIcon( defaultIcon );
    }
    
    /**
     * Sets the string for the cell being rendered to <code>value</code>.
     *
     * @param value  the string value for this cell; if value is
     *      <code>null</code> it sets the text value to an empty string
     * @see JLabel#setText
     *
     */
    @Override
    protected void setValue( Object value )
    {
        try
        {
            if ( value instanceof ShareFile )
            {
                ShareFile shareFile = (ShareFile)value;
                File file = shareFile.getSystemFile();
                if ( file.exists() )
                {
                    Icon icon = shareFileIconMap.get( shareFile );
                    if ( icon == null )
                    {
                        icon = fsv.getSystemIcon( file );
                        shareFileIconMap.put( shareFile, icon );
                    }
                    setIcon( icon );
                }
                else
                {
                    setIcon( defaultIcon );
                }
                setText( shareFile.getFileName() );
                return;
            }
            else if ( value instanceof File )
            {
                File file = (File)value;
                setText( file.getName() );
                if ( file.exists() )
                {
                    Icon icon = fsv.getSystemIcon(file);
                    Icon grayIcon = lookupGrayIcon( icon );
                    if ( grayIcon == null )
                    {
                        grayIcon = ImageFilterUtils.createGrayIcon( icon );
                        bufferGrayIcon( icon, grayIcon );
                    }
                    setIcon( grayIcon );
                }
                else
                {
                    setIcon( defaultGrayIcon );
                }
                return;
            }
            setText( value.toString() );
            setIcon( null );
        }
        catch ( Throwable th )
        {
            NLogger.error( FileSystemTableCellRenderer.class, th, th );
        }
    }
    
    private Icon lookupGrayIcon( Icon icon )
    {
        if ( icon instanceof ImageIcon )
        {
            return (Icon) grayMap.get( ((ImageIcon)icon).getImage() );
        }
        else
        {
            return (Icon) grayMap.get( icon );
        }
    }
    
    private void bufferGrayIcon( Icon icon, Icon grayIcon )
    {
        if ( icon instanceof ImageIcon )
        {
            grayMap.put( ((ImageIcon)icon).getImage(), grayIcon );
        }
        else
        {
            grayMap.put( icon, grayIcon );
        }
    }
}
