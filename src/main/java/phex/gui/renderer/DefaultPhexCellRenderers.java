/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2006 Phex Development Group
 *  Copyright (C) 2001 Peter Hunnisett (hunnise@users.sourceforge.net)
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
 */
package phex.gui.renderer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;

import phex.gui.tabs.library.FileSystemTableCellRenderer;

/**
 * Static class which contains a mapping for all specially defined Phex renderers.
 * This mapping allows the model and table to be decoupled (as far as renderer goes).
 * The model need only provide a getColumnClass() method which returns the class of
 * the renderer that should be used for renderering the value into the cell.
 *
 *
 * @author  hunnise
 * @version 1.0
 */
public final class DefaultPhexCellRenderers
{
    /**
     * Sets all known default cell renderers for a given table. Once this is
     * called for a table, the model simply has to return the class of
     * the renderer it wishes to use for a given column.
     */
    public static void setDefaultPhexCellRenderers( JTable table )
    {
        final int numberOfRenderers = renderers.length;

        for( int rend = 0; rend < numberOfRenderers; rend++ )
        {
            table.setDefaultRenderer( renderers[ rend ].klass, renderers[ rend ].renderer );
        }
    }

    /**
     * All non default renderers to be used in tables must be included in this array.
     */
    private static final RendererClassCellPair[] renderers =
    {
        new RendererClassCellPair( ProgressCellRenderer.class,
            new ProgressCellRenderer() ),
        new RendererClassCellPair( TransferSizeCellRenderer.class,
            new TransferSizeCellRenderer() ),
        new RendererClassCellPair( DateCellRenderer.class,
            new DateCellRenderer() ),
        new RendererClassCellPair( HostAddressCellRenderer.class,
            new HostAddressCellRenderer() ),
        new RendererClassCellPair( FileSizeCellRenderer.class,
            new FileSizeCellRenderer() ),
        new RendererClassCellPair( FileSystemTableCellRenderer.class,
            new FileSystemTableCellRenderer() ),
        new RendererClassCellPair( ETACellRenderer.class,
            new ETACellRenderer() ),
        new RendererClassCellPair( ScopeProgressCellRenderer.class,
            new ScopeProgressCellRenderer() )
    };

    private final static class RendererClassCellPair
    {
        public RendererClassCellPair( Class c, TableCellRenderer r )
        {
            klass = c;
            renderer = r;
        }

        private final Class klass;
        private final TableCellRenderer renderer;
    }
}
