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
 *  $Id: NetworkTableModel.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.network;

import java.awt.EventQueue;
import java.util.Comparator;

import javax.swing.event.TableModelEvent;

import org.bushe.swing.event.annotation.EventTopicSubscriber;

import phex.common.QueryRoutingTable;
import phex.common.format.NumberFormatUtils;
import phex.common.format.TimeFormatUtils;
import phex.event.ContainerEvent;
import phex.event.PhexEventTopics;
import phex.gui.common.GUIRegistry;
import phex.gui.common.table.FWSortableTableModel;
import phex.gui.comparator.DestAddressComparator;
import phex.gui.renderer.HostAddressCellRenderer;
import phex.host.Host;
import phex.host.HostInfo;
import phex.host.NetworkHostsContainer;
import phex.servent.Servent;
import phex.utils.Localizer;

public class NetworkTableModel extends FWSortableTableModel
{
    public static final int HOST_MODEL_INDEX = 0;
    public static final int VENDOR_MODEL_INDEX = 1;
    public static final int TYPE_MODEL_INDEX = 2;
    public static final int MODE_MODEL_INDEX = 3;
    public static final int RECEIVED_DROPPED_MODEL_INDEX = 4;
    public static final int SENT_QUEUED_MODEL_INDEX = 5;
    public static final int SHARED_MODEL_INDEX = 6;
    public static final int QRT_MODEL_INDEX = 7;
    public static final int UPTIME_MODEL_INDEX = 8;
    public static final int STATUS_MODEL_INDEX = 9;
    
    /**
     * The unique column id is not allowed to ever change over Phex releases. It
     * is used when serializing column information. The column id is contained in
     * the identifier field of the TableColumn.
     */
    private static final Integer HOST_COLUMN_ID = Integer.valueOf( 1001 );
    private static final Integer VENDOR_COLUMN_ID = Integer.valueOf( 1002 );
    private static final Integer TYPE_COLUMN_ID = Integer.valueOf( 1003 );
    private static final Integer RECEIVED_DROPPED_COLUMN_ID = Integer.valueOf( 1004 );
    private static final Integer SENT_QUEUED_COLUMN_ID = Integer.valueOf( 1005 );
    private static final Integer SHARED_COLUMN_ID = Integer.valueOf( 1007 );
    private static final Integer UPTIME_COLUMN_ID = Integer.valueOf( 1008 );
    private static final Integer STATUS_COLUMN_ID = Integer.valueOf( 1009 );
    private static final Integer MODE_COLUMN_ID = Integer.valueOf( 1010 );
    private static final Integer QRT_COLUMN_ID = Integer.valueOf( 1011 );

    /**
     * Column ids ordered according to its corresponding model index
     */
    private static final Integer[] COLUMN_IDS = new Integer[]
    {
        HOST_COLUMN_ID,
        VENDOR_COLUMN_ID,
        TYPE_COLUMN_ID,
        MODE_COLUMN_ID,
        RECEIVED_DROPPED_COLUMN_ID,
        SENT_QUEUED_COLUMN_ID,
        SHARED_COLUMN_ID,
        QRT_COLUMN_ID,
        UPTIME_COLUMN_ID,
        STATUS_COLUMN_ID
    };

    private static final String[] tableColumns;
    private static final Class<?>[] tableClasses;

    static
    {
        tableColumns = new String[]
        {
            Localizer.getString( "RemoteHost" ),
            Localizer.getString( "Vendor" ),
            Localizer.getString( "Type" ),
            Localizer.getString( "Mode" ),
            Localizer.getString( "ReceivedDropped" ),
            Localizer.getString( "SentQueuedDropped" ),
            Localizer.getString( "Shared" ),
            Localizer.getString( "QRT" ),
            Localizer.getString( "Uptime" ),
            Localizer.getString( "Status" )
        };

        tableClasses = new Class[]
        {
             HostAddressCellRenderer.class,
             String.class,
             String.class,
             String.class,
             String.class,
             String.class,
             String.class,
             String.class,
             String.class,
             String.class,
        };
    }

    private NetworkHostsContainer hostsContainer;

    public NetworkTableModel( NetworkHostsContainer hostsContainer )
    {
        super( COLUMN_IDS, tableColumns, tableClasses );
        this.hostsContainer = hostsContainer;
        GUIRegistry.getInstance().getServent().getEventService().processAnnotations( this );
    }

    public int getRowCount()
    {
        return hostsContainer.getNetworkHostCount();
    }

    public Object getValueAt(int row, int col)
    {
        Host host = hostsContainer.getNetworkHostAt( row );
        if ( host == null )
        {
            fireTableRowsDeleted( row, row );
            return "";
        }

        switch (col)
        {
            case HOST_MODEL_INDEX:
                return host.getHostAddress();

            case VENDOR_MODEL_INDEX:
                return host.getVendor();

            case TYPE_MODEL_INDEX:
                switch ( host.getType() )
                {
                    case OUTGOING:
                        return Localizer.getString( "HostType_Outgoing" );
                    case INCOMING:
                        return Localizer.getString( "HostType_Incoming" );
                    default:
                        return "";
                }
            case MODE_MODEL_INDEX:
                if ( !host.isConnected() )
                {
                    return "";
                }
                if ( host.isUltrapeer() )
                {
                    String mode = Localizer.getString( "Ultrapeer" );
                    if ( host.getPushProxyAddress() != null )
                    {
                        mode += " (PP)";
                    }
                    return mode;
                }
                else if ( host.isUltrapeerLeafConnection() )
                {
                    return Localizer.getString( "Leaf" );
                }
                else
                {
                    return Localizer.getString( "Peer" );
                }
            case RECEIVED_DROPPED_MODEL_INDEX:
                return String.valueOf(host.getReceivedCount() + " (" + String.valueOf(host.getDropCount()) + ")");

            case SENT_QUEUED_MODEL_INDEX:
                return String.valueOf( host.getSentCount() ) + " / "
                     + String.valueOf( host.getSendQueueLength() ) + " / "
                     + String.valueOf( host.getSendDropCount() );

            case SHARED_MODEL_INDEX:
                if (host.getFileCount() == -1)
                {
                    return "";
                }
                else
                {
                    return host.getFileCount() + "/" + NumberFormatUtils.formatSignificantByteSize(
                        host.getTotalSize() * 1024L );
                }
            case QRT_MODEL_INDEX:
                QueryRoutingTable qrt = host.getLastReceivedRoutingTable();
                if( qrt == null ) 
                {
                    return "";
                }
                else
                {
                    return NumberFormatUtils.formatDecimal( qrt.getFillRatio(), 2 ) + "% / " 
                        + NumberFormatUtils.formatDecimal( qrt.getTableSize()/1024.0, 0 ) + "K";
                }
            case UPTIME_MODEL_INDEX:
                long upSeconds = host.getConnectionUpTime( System.currentTimeMillis() ) / 1000;
                return TimeFormatUtils.formatSignificantElapsedTime( upSeconds );

            case STATUS_MODEL_INDEX:
                return HostInfo.getHostStatusString(host);
        }
        return "";
    }
    
    /**
     * Returns the most comparator that is used for sorting of the cell values
     * in the column. This is used by the FWSortedTableModel to perform the
     * sorting. If not overwritten the method returns null causing the
     * FWSortedTableModel to use a NaturalComparator. It expects all Objects that
     * are returned from getComparableValueAt() to implement the Comparable interface.
     */
    @Override
    public Comparator<?> getColumnComparator( int column )
    {
        switch( column )
        {
            case HOST_MODEL_INDEX:
                return new DestAddressComparator();
            // for all other columns use default comparator
            default:
                return null;
        }
    }

    /**
     * Returns an attribute value that is used for comparing on sorting
     * for the cell at row and column. If not overwritten the call is forwarded
     * to getValueAt().
     * The returned Object is compared via the Comparator returned from
     * getColumnComparator(). If no comparator is specified the returned Object
     * must implement the Comparable interface.
     */
    @Override
    public Object getComparableValueAt( int row, int column )
    {
        switch ( column )
        {
            case UPTIME_MODEL_INDEX:
                Host host = hostsContainer.getNetworkHostAt( row );
                if ( host == null )
                {
                    return Long.valueOf( Long.MIN_VALUE );
                }
                return Long.valueOf( host.getConnectionUpTime( 
                    System.currentTimeMillis() ) );
        }
        return getValueAt( row, column );
    }

    /**
     * Indicates if a column is hideable.
     */
    @Override
    public boolean isColumnHideable( int columnIndex )
    {
        if ( columnIndex == HOST_MODEL_INDEX )
        {
            return false;
        }
        return true;
    }
    
    /**
     * Indicates if a column is visible by default.
     */
    @Override
    public boolean isColumnDefaultVisible( int columnIndex )
    {
        if ( columnIndex == QRT_MODEL_INDEX )
        {
            return false;
        }
        return true;
    }
    
    @EventTopicSubscriber(topic=PhexEventTopics.Net_Hosts)
    public void onNetworkHostsEvent( String topic, final ContainerEvent event )
    {
        EventQueue.invokeLater( new Runnable() {
            public void run()
            {
                int position = event.getPosition();
                if ( event.getType() == ContainerEvent.Type.ADDED )
                {
                    fireTableChanged( new TableModelEvent(NetworkTableModel.this,
                        position, position, TableModelEvent.ALL_COLUMNS,
                        TableModelEvent.INSERT ) );
                }
                else if ( event.getType() == ContainerEvent.Type.REMOVED )
                {
                    fireTableChanged( new TableModelEvent(NetworkTableModel.this,
                        position, position, TableModelEvent.ALL_COLUMNS,
                        TableModelEvent.DELETE ) );            
                }
            }
        });
    }
}