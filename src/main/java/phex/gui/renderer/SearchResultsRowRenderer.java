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
 *  $Id: SearchResultsRowRenderer.java 4359 2009-01-15 23:19:41Z gregork $
 */
package phex.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import phex.common.URN;
import phex.download.RemoteFile;
import phex.download.swarming.SwarmingManager;
import phex.gui.common.treetable.JTreeTable;
import phex.gui.tabs.search.SearchResultElement;
import phex.share.SharedFilesService;

/**
 * 
 */
public class SearchResultsRowRenderer implements TableCellRenderer
{
    private static final Color DOWNLOAD_COLOR = new Color( 0x00, 0x7F, 0x00 );
    private static final Color SHARE_COLOR = Color.lightGray;
    
    private final SharedFilesService sharedFilesService;
    private final SwarmingManager swarmingMgr;
    
    public SearchResultsRowRenderer( SharedFilesService sharedFilesService, 
        SwarmingManager swarmingMgr )
    {
        this.sharedFilesService = sharedFilesService;
        this.swarmingMgr = swarmingMgr;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column)
    {
        JTreeTable treeTable = (JTreeTable) table;
        // because of JTreeTable we need to do special handling for column 0
        // which contains the JTree
        Component comp;
        TableCellRenderer renderer = table.getDefaultRenderer(
            table.getColumnClass( column ) );
        if ( renderer == null )
        {
            throw new NullPointerException( "No default renderer found for: '" + 
                table.getColumnClass( column ) +"', Column: " + column );
        }
        comp = renderer.getTableCellRendererComponent( table, value,
            isSelected, hasFocus, row, column );
        comp.setForeground( table.getForeground() );
        
        Object node = treeTable.getNodeOfRow( row );
        if ( node == null || isSelected )
        {
            comp.setForeground( table.getSelectionForeground() );
            return comp;
        }
        
        // TODO2 to improve scrolling performance store the download and share
        // status inside the remote file and have a background job update the
        // states periodically.
        RemoteFile remoteFile = getRemoteFile( node );
        URN urn = remoteFile.getURN();
        boolean isShared = sharedFilesService.isURNShared( urn );
        if ( isShared )
        {
            comp.setForeground( SHARE_COLOR );
            return comp;
        }
        boolean isDownloaded = remoteFile.isInDownloadQueue() || 
            swarmingMgr.isURNDownloaded( urn );
        if ( isDownloaded )
        {
            comp.setForeground( DOWNLOAD_COLOR );
            return comp;
        }
        return comp;
    }
    
    /**
     * Returns the remote file of the node. In case of SearchResultElement
     * the single remote file is returned.
     * @param node
     * @return
     */
    private RemoteFile getRemoteFile( Object node )
    {
        if ( node instanceof SearchResultElement)
        {
            return ((SearchResultElement)node).getSingleRemoteFile();
        }
        else
        {
            return (RemoteFile)node;
        }
    }
}