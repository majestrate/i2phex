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
 *  $Id: SearchTreeCellRenderer.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import phex.common.URN;
import phex.download.RemoteFile;
import phex.download.swarming.SwarmingManager;
import phex.gui.common.GUIRegistry;
import phex.gui.common.IconPack;
import phex.gui.common.LabeledIcon;
import phex.gui.tabs.search.SearchResultElement;
import phex.servent.Servent;
import phex.share.SharedFilesService;

/**
 * 
 */
public class SearchTreeCellRenderer extends DefaultTreeCellRenderer
{
    private static final Color DOWNLOAD_COLOR = new Color( 0x00, 0x7F, 0x00 );
    private static final Color SHARE_COLOR = Color.lightGray;
    
    private SharedFilesService sharedFilesService;
    private SwarmingManager downloadService;
    
    private JTree tree;
    private Icon folderIcon;
    private LabeledIcon folderLabeledIcon;
    
    public SearchTreeCellRenderer( SwarmingManager downloadService )
    {
        GUIRegistry guiRegistry = GUIRegistry.getInstance();
        sharedFilesService = guiRegistry.getServent().getSharedFilesService();
        this.downloadService = downloadService;
        IconPack plafIconPack = guiRegistry.getPlafIconPack();
        folderIcon = plafIconPack.getIcon( "Search.ResultFolder" );
        folderLabeledIcon = new LabeledIcon( folderIcon, null );
    }

    /**
     * Gets the font of this component.
     * @return this component's font; if a font has not been set
     * for this component, the font of its parent is returned
     */
    public Font getFont()
    {
        Font font = super.getFont();
        if (font == null && tree != null)
        {
            // Strive to return a non-null value, otherwise the html support
            // will typically pick up the wrong font in certain situations.
            font = tree.getFont();
        }
        return font;
    }

    /**
      * Configures the renderer based on the passed in components.
      * The value is set from messaging the tree with
      * <code>convertValueToText</code>, which ultimately invokes
      * <code>toString</code> on <code>value</code>.
      * The foreground color is set based on the selection and the icon
      * is set based on on leaf and expanded.
      */
    public Component getTreeCellRendererComponent( JTree tree, Object value,
        boolean sel, boolean expanded, boolean leaf, int row,
        boolean hasFocus )
    {
        if (sel)
            setForeground(getTextSelectionColor());
        else
            setForeground(getTextNonSelectionColor());
            
        String stringValue = convertValueToText(value, sel, expanded, leaf, row, hasFocus);

        this.tree = tree;
        this.hasFocus = hasFocus;
        setText(stringValue);
        
        // There needs to be a way to specify disabled icons.
        if (!tree.isEnabled())
        {
            setEnabled(false);
            if (leaf)
            {
                setDisabledIcon(getLeafIcon());
            }
            else
            {
                if ( value instanceof SearchResultElement )
                {
                    int count = ((SearchResultElement)value).getRemoteFileListCount();
                    folderLabeledIcon.setLabel( String.valueOf( count ) );
                }
                else
                {
                    folderLabeledIcon.setLabel(null);
                }
                setIcon( folderLabeledIcon );
            }
        }
        else
        {
            setEnabled(true);
            if (leaf)
            {
                setIcon(getLeafIcon());
            }
            else
            {
                if ( value instanceof SearchResultElement )
                {
                    int count = ((SearchResultElement)value).getRemoteFileListCount();
                    folderLabeledIcon.setLabel( String.valueOf( count ) );
                }
                else
                {
                    folderLabeledIcon.setLabel(null);
                }
                setIcon( folderLabeledIcon );
            }
        }
        setComponentOrientation(tree.getComponentOrientation());

        selected = sel;

        return this;
    }
    
    /**
     * Called by the renderers to convert the specified value to
     * text. This implementation returns <code>value.toString</code>, ignoring
     * all other arguments. To control the conversion, subclass this 
     * method and use any of the arguments you need.
     * 
     * @param value the <code>Object</code> to convert to text
     * @param selected true if the node is selected
     * @param expanded true if the node is expanded
     * @param leaf  true if the node is a leaf node
     * @param row  an integer specifying the node's display row, where 0 is 
     *             the first row in the display
     * @param hasFocus true if the node has the focus
     * @return the <code>String</code> representation of the node's value
     */
    public String convertValueToText(Object value, boolean selected,
                                     boolean expanded, boolean leaf, int row,
                                     boolean hasFocus)
    {
        RemoteFile remoteFile; 
        if ( value instanceof SearchResultElement )
        {
            remoteFile = ((SearchResultElement)value).getSingleRemoteFile();
        }
        else if ( value instanceof RemoteFile )
        {
            remoteFile = (RemoteFile)value;
        }
        else
        {
            remoteFile = null;
        }
        if(remoteFile == null)
        {
            return "";
        }
        
        // adjust component colors
        URN urn = remoteFile.getURN();
        boolean isShared = sharedFilesService.isURNShared( urn );
        if ( isShared )
        {
            setForeground( SHARE_COLOR );
        }
        else
        {
            boolean isDownloaded = downloadService.isURNDownloaded( urn );
            if ( isDownloaded )
            {
                setForeground( DOWNLOAD_COLOR );
            }
        }
        
        return remoteFile.getDisplayName();
    }
}
