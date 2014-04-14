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
 *  $Id: GuiUpdateTimer.java 4064 2007-11-29 22:57:54Z complication $
 */
package phex.gui.common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import phex.common.log.NLogger;
import phex.prefs.api.Setting;

/**
 * Components that should be updated (redrawn) at a global configured UI update 
 * interval can register here. It important to only perform quick non-blocking
 * operations in, mainly things like updating displayed data and redrawing.
 */
public class GuiUpdateTimer extends Timer
{
    // TODO currently only updated on restart...
    public GuiUpdateTimer( Setting<Integer> guiUpdateInterval )
    {
        super( guiUpdateInterval.get().intValue(), null );
        start();
    }
    
    /**
     * Fires regular update table events for the given table.
     * @param table the table to update.
     */
    public void addTable( JTable table )
    {
        addActionListener( new TableUpdateAction( table ) );
    }
    
    class TableUpdateAction implements ActionListener
    {
        private JTable table;
        
        public TableUpdateAction(JTable table)
        {
            this.table = table;
        }

        public void actionPerformed( ActionEvent e )
        {
            try
            {
                TableModel model = table.getModel();
                if ( model.getRowCount() > 0 )
                {
                    if ( model instanceof AbstractTableModel )
                    {
                        ((AbstractTableModel)model).fireTableRowsUpdated(
                            0, model.getRowCount() );
                    }
                }
            }
            catch ( Throwable th )
            {
                NLogger.error( TableUpdateAction.class, th, th);
            }
        }
    }
}