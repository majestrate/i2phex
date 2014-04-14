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
 */
package phex.gui.renderer;

import java.awt.Component;

import javax.swing.JTable;

import phex.common.TransferDataProvider;
import phex.common.format.TimeFormatUtils;
import phex.utils.Localizer;

public class ETACellRenderer extends FWTableCellRenderer
{
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column)
    {
        super.getTableCellRendererComponent(table, value,
            isSelected, hasFocus, row, column);

        if ( value instanceof TransferDataProvider )
        {
            TransferDataProvider provider = (TransferDataProvider) value;

            if ( isSelected )
            {// in case of selection always use default color...
                setForeground( table.getSelectionForeground() );
            }
            else
            {
                CellColorHandler.applyCellColor( provider, this );
            }

            long transferredSize = provider.getTransferredDataSize();
            long totalTransferSize = provider.getTransferDataSize();
            long transferRate = provider.getLongTermTransferRate();
            
            if ( totalTransferSize == -1 || 
                 provider.getDataTransferStatus() != TransferDataProvider.TRANSFER_RUNNING )
            {
                setText( "" );
            }
            else
            {
                long timeRemaining;
                if ( transferRate == 0 )
                {
                    timeRemaining = TransferDataProvider.INFINITY_ETA_INT;
                }
                else
                {
                    timeRemaining = (long)((totalTransferSize - transferredSize) / transferRate);
                    timeRemaining = Math.max( 0, timeRemaining );
                }
        
                // estimated time of arival
                if ( timeRemaining < TransferDataProvider.INFINITY_ETA_INT )
                {
                    setText( TimeFormatUtils.formatSignificantElapsedTime( timeRemaining ) );
                }
                else
                {
                    setText( Localizer.getDecimalFormatSymbols().getInfinity() );
                }
            }
        }
        return this;
    }
}
