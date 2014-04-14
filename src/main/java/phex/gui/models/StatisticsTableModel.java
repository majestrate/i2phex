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
 *  $Id: StatisticsTableModel.java 4360 2009-01-16 09:08:57Z gregork $
 */
package phex.gui.models;

import phex.gui.common.table.*;
import phex.statistic.*;
import phex.utils.Localizer;

public class StatisticsTableModel extends FWSortableTableModel
    implements StatisticProviderConstants
{
    private static final int NAME_MODEL_INDEX = 0;
    private static final int VALUE_MODEL_INDEX = 1;
    private static final int AVG_MODEL_INDEX = 2;
    private static final int MAX_MODEL_INDEX = 3;
    
    /**
     * The unique column id is not allowed to ever change over Phex releases. It
     * is used when serializing column information. The column id is containd in
     * the identifier field of the TableColumn.
     */
    private static final int NAME_COLUMN_ID = 1001;
    private static final int VALUE_COLUMN_ID = 1002;
    private static final int AVG_COLUMN_ID = 1003;
    private static final int MAX_COLUMN_ID = 1004;
    
    /**
     * Column ids orderd according to its corresponding model index
     */
    private static final Integer[] COLUMN_IDS = new Integer[]
    {
        NAME_COLUMN_ID, VALUE_COLUMN_ID, AVG_COLUMN_ID, MAX_COLUMN_ID,
    };

    private static String[] tableColumns;
    private static Class[] tableClasses;

    private static String[] rowKeys;

    /**
     * Initialize super tableColumns field
     */
    static
    {
        tableColumns = new String[]
        {
            Localizer.getString( "Name" ),
            Localizer.getString( "Value" ),
            Localizer.getString( "Avg." ),
            Localizer.getString( "Max." ),
        };

        tableClasses = new Class[]
        {
            String.class,
            String.class,
            String.class,
            String.class,
        };

        rowKeys = new String[]
        {
            TOTAL_BANDWIDTH_PROVIDER,
            NETWORK_BANDWIDTH_PROVIDER,
            DOWNLOAD_BANDWIDTH_PROVIDER,
            UPLOAD_BANDWIDTH_PROVIDER,
            
            HORIZON_HOST_COUNT_PROVIDER,
            HORIZON_FILE_COUNT_PROVIDER,
            HORIZON_FILE_SIZE_PROVIDER,

            TOTALMSG_IN_PROVIDER,
            PINGMSG_IN_PROVIDER,
            PONGMSG_IN_PROVIDER,
            QUERYMSG_IN_PROVIDER,
            QUERYHITMSG_IN_PROVIDER,
            PUSHMSG_IN_PROVIDER,

            TOTALMSG_OUT_PROVIDER,
            PINGMSG_OUT_PROVIDER,
            PONGMSG_OUT_PROVIDER,
            QUERYMSG_OUT_PROVIDER,
            QUERYHITMSG_OUT_PROVIDER,
            PUSHMSG_OUT_PROVIDER,

            DROPEDMSG_TOTAL_PROVIDER,
            DROPEDMSG_IN_PROVIDER,
            DROPEDMSG_OUT_PROVIDER,
            
            PUSH_DOWNLOAD_ATTEMPTS_PROVIDER,
            PUSH_DOWNLOAD_SUCESS_PROVIDER,
            PUSH_DOWNLOAD_FAILURE_PROVIDER,
            
            PUSH_DLDPUSHPROXY_ATTEMPTS_PROVIDER,
            PUSH_DLDPUSHPROXY_SUCESS_PROVIDER,
            
            PUSH_UPLOAD_ATTEMPTS_PROVIDER,
            PUSH_UPLOAD_SUCESS_PROVIDER,
            PUSH_UPLOAD_FAILURE_PROVIDER,

            UPTIME_PROVIDER,
            DAILY_UPTIME_PROVIDER
        };
    }

    private StatisticsManager statsService;

    public StatisticsTableModel( StatisticsManager statsService )
    {
        super( COLUMN_IDS, tableColumns, tableClasses );
        this.statsService = statsService;
    }

    public int getRowCount()
    {
        return rowKeys.length;
    }

    public Object getValueAt( int row, int column )
    {
        if ( column == 0 )
        {
            return Localizer.getString( rowKeys[ row ] );
        }
        else if ( column == 1 )
        {
            StatisticProvider provider = statsService.getStatisticProvider(
                rowKeys[ row ] );
            if ( provider == null )
            {
                return "";
            }
            Object value = provider.getValue();
            if ( value != null )
            {
                return provider.toStatisticString( value );
            }
        }
        else if ( column == 2 )
        {
            StatisticProvider provider = statsService.getStatisticProvider(
                rowKeys[ row ] );
            if ( provider == null )
            {
                return "";
            }
            // TODO how do we get the unit back in there??
            Object value = provider.getAverageValue();
            if ( value != null )
            {
                return provider.toStatisticString( value );
            }
        }
        else if ( column == 3 )
        {
            StatisticProvider provider = statsService.getStatisticProvider(
                rowKeys[ row ] );
            if ( provider == null )
            {
                return "";
            }
            // TODO how do we get the unit back in there??
            Object value = provider.getMaxValue();
            if ( value != null )
            {
                return provider.toStatisticString( value );
            }
        }

        return "";
    }

    /**
     * Indicates if a column is hideable.
     */
    @Override
    public boolean isColumnHideable( int columnIndex )
    {
        if ( columnIndex == NAME_MODEL_INDEX )
        {
            return false;
        }
        return true;
    }
}