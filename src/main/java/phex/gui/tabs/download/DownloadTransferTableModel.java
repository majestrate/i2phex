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
 *  $Id: DownloadTransferTableModel.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.download;

import java.awt.EventQueue;
import java.util.Comparator;

import javax.swing.event.TableModelEvent;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import phex.common.format.NumberFormatUtils;
import phex.download.DownloadScopeList;
import phex.download.swarming.SWDownloadCandidate;
import phex.download.swarming.SWDownloadFile;
import phex.download.swarming.SWDownloadInfo;
import phex.download.swarming.SWDownloadSegment;
import phex.download.swarming.SWDownloadCandidate.CandidateStatus;
import phex.event.ContainerEvent;
import phex.event.PhexEventTopics;
import phex.gui.common.GUIRegistry;
import phex.gui.common.table.FWSortableTableModel;
import phex.gui.comparator.DestAddressComparator;
import phex.gui.comparator.ETAComparator;
import phex.gui.renderer.ETACellRenderer;
import phex.gui.renderer.HostAddressCellRenderer;
import phex.gui.renderer.ProgressCellRenderer;
import phex.servent.Servent;
import phex.utils.Localizer;

public class DownloadTransferTableModel extends FWSortableTableModel
{
    public static final int HOST_MODEL_INDEX = 0;
    public static final int VENDOR_MODEL_INDEX = 1;
    public static final int FROM_MODEL_INDEX = 2;
    public static final int TO_MODEL_INDEX = 3;
    public static final int COMPLETED_MODEL_INDEX = 4;
    public static final int SIZE_MODEL_INDEX = 5;
    public static final int PROGRESS_MODEL_INDEX = 6;
    public static final int RATE_MODEL_INDEX = 7;
    public static final int ETA_MODEL_INDEX = 8;
    public static final int STATUS_MODEL_INDEX = 9;
    
    /**
     * The unique column id is not allowed to ever change over Phex releases. It
     * is used when serializing column information. The column id is containd in
     * the identifier field of the TableColumn.
     */
    public static final int HOST_COLUMN_ID = 1001;
    public static final int VENDOR_COLUMN_ID = 1002;
    public static final int FROM_COLUMN_ID = 1003;
    public static final int TO_COLUMN_ID = 1004;
    public static final int COMPLETED_COLUMN_ID = 1005;
    public static final int SIZE_COLUMN_ID = 1006;
    public static final int PROGRESS_COLUMN_ID = 1007;
    public static final int RATE_COLUMN_ID = 1008;
    public static final int ETA_COLUMN_ID = 1009;
    public static final int STATUS_COLUMN_ID = 1010;

    /**
     * Column ids orderd according to its corresponding model index
     */
    private static final Integer[] COLUMN_IDS = new Integer[]
    {
        HOST_COLUMN_ID,
        VENDOR_COLUMN_ID,
        FROM_COLUMN_ID,
        TO_COLUMN_ID,
        COMPLETED_COLUMN_ID,
        SIZE_COLUMN_ID,
        PROGRESS_COLUMN_ID,
        RATE_COLUMN_ID,
        ETA_COLUMN_ID,
        STATUS_COLUMN_ID
    };

    private static String[] tableColumns;
    private static Class[] tableClasses;

    /**
     * Initialize super tableColumns field
     */
    static
    {
        tableColumns = new String[]
        {
            Localizer.getString( "DownloadTransfer_Host" ),
            Localizer.getString( "DownloadTransfer_Vendor" ),
            Localizer.getString( "DownloadTransfer_From" ),
            Localizer.getString( "DownloadTransfer_To" ),
            Localizer.getString( "DownloadTransfer_Completed" ),
            Localizer.getString( "DownloadTransfer_Size" ),
            Localizer.getString( "DownloadTransfer_Progress" ),
            Localizer.getString( "DownloadTransfer_Rate" ),
            Localizer.getString( "DownloadTransfer_ETA" ),
            Localizer.getString( "DownloadTransfer_Status" )
        };

        tableClasses = new Class[]
        {
            HostAddressCellRenderer.class, // host
            String.class, // vendor
            String.class, // from
            String.class, // to
            String.class, // completed
            String.class, // size
            ProgressCellRenderer.class, // progress
            String.class, // rate
            ETACellRenderer.class, // eta
            String.class // status
        };
    }

    /**
     * The currently displayed download file of the model.
     */
    private SWDownloadFile downloadFile;

    /**
     * @param downloadTable The constructor takes the download JTable. This is
     * necessary to get informed of the selection changes of the download table.
     */
    public DownloadTransferTableModel( )
    {
        super( COLUMN_IDS, tableColumns, tableClasses );
        GUIRegistry.getInstance().getServent().getEventService().processAnnotations( this );
    }
    
    public void updateDownloadFile( SWDownloadFile file )
    {
        if ( downloadFile == file )
        {
            return;
        }
        downloadFile = file;
        fireTableDataChanged(  );
    }

    public int getRowCount()
    {
        if ( downloadFile == null )
        {
            return 0;
        }
        return downloadFile.getTransferCandidateCount();
    }

    public Object getValueAt( int row, int column )
    {
        SWDownloadCandidate candidate = downloadFile.getTransferCandidate( row );
        if ( candidate == null )
        {
            return null;
        }

        SWDownloadSegment segment;
        switch( column )
        {
        case HOST_MODEL_INDEX:
            return candidate.getHostAddress();
        case VENDOR_MODEL_INDEX:
            return candidate.getVendor();
        case FROM_MODEL_INDEX:
            segment = candidate.getDownloadSegment();
            if ( segment == null )
            {
                return null;
            }
            return NumberFormatUtils.formatNumber( segment.getStart() );
        case TO_MODEL_INDEX:
            segment = candidate.getDownloadSegment();
            if ( segment == null )
            {
                return null;
            }
            return NumberFormatUtils.formatNumber( segment.getEnd() );
        case COMPLETED_MODEL_INDEX:
            segment = candidate.getDownloadSegment();
            if ( segment == null )
            {
                return null;
            }
            return NumberFormatUtils.formatFullByteSize( segment.getTransferredDataSize() );
        case SIZE_MODEL_INDEX:
            segment = candidate.getDownloadSegment();
            if ( segment == null )
            {
                return null;
            }
            return NumberFormatUtils.formatFullByteSize( segment.getTotalDataSize() );
        case PROGRESS_MODEL_INDEX:
            segment = candidate.getDownloadSegment();
            if ( segment == null )
            {
                return null;
            }
            return segment.getProgress();
        case RATE_MODEL_INDEX:
            segment = candidate.getDownloadSegment();
            if ( segment == null )
            {
                return null;
            }
            return NumberFormatUtils.formatSignificantByteSize( 
                segment.getTransferSpeed() ) + Localizer.getString( "PerSec" );
        case ETA_MODEL_INDEX:
            segment = candidate.getDownloadSegment();
            if ( segment == null )
            {
                return null;
            }
            return segment;
        case STATUS_MODEL_INDEX:
            return SWDownloadInfo.getDownloadCandidateStatusString( candidate );
        default:
            return null;
        }
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
            case PROGRESS_MODEL_INDEX:
                return ComparableComparator.getInstance();
            case ETA_MODEL_INDEX:
                return new ETAComparator();
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
        SWDownloadCandidate candidate = downloadFile.getTransferCandidate( row );
        if ( candidate == null )
        {
            return null;
        }
        
        SWDownloadSegment segment;
        switch( column )
        {
            case FROM_MODEL_INDEX:
                segment = candidate.getDownloadSegment();
                if ( segment == null )
                {
                    return null;
                }
                return Long.valueOf( segment.getStart() );
            case TO_MODEL_INDEX:
                segment = candidate.getDownloadSegment();
                if ( segment == null )
                {
                    return null;
                }
                return Long.valueOf( segment.getEnd() );
            case SIZE_MODEL_INDEX:
                segment = candidate.getDownloadSegment();
                if ( segment == null )
                {
                    return null;
                }
                return Long.valueOf( segment.getTotalDataSize() );
            case PROGRESS_MODEL_INDEX:
            {
                DownloadScopeList availableScopeList = candidate.getAvailableScopeList();
                if ( availableScopeList == null )
                {
                    return null;
                }
                return Long.valueOf( availableScopeList.getAggregatedLength() );
            }
            case STATUS_MODEL_INDEX:
                CandidateStatus status = candidate.getStatus();
                if ( status ==
                    CandidateStatus.REMOTLY_QUEUED )
                {
                    int queuePosition = candidate.getXQueueParameters().getPosition();
                    Double doubObj = Double.valueOf( status.ordinal() + 1.0 -
                        Math.min( (double)queuePosition, (double)10000 ) / 10000.0 );
                    return doubObj;
                }
                else
                {
                    long timeLeft = candidate.getStatusTimeLeft();
                    return Double.valueOf( status.ordinal() +
                        timeLeft / 1000000.0 );
                }
            case RATE_MODEL_INDEX:
            {
                segment = candidate.getDownloadSegment();
                if ( segment == null )
                {
                    return null;
                }
                return Long.valueOf( segment.getTransferSpeed() );
            }
            case ETA_MODEL_INDEX:
            {
                segment = candidate.getDownloadSegment();
                return segment;
            }
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
    
    @EventTopicSubscriber(topic=PhexEventTopics.Download_Candidate)
    public void onDownloadCandidateEvent( String topic, 
        final ContainerEvent event )
    {
        if ( downloadFile != ((SWDownloadCandidate)event.getSource()).getDownloadFile() )
        {
            return;
        }
        EventQueue.invokeLater( new Runnable() {
            public void run()
            {
                int position = event.getPosition();
                if ( event.getType() == ContainerEvent.Type.ADDED )
                {
                    fireTableChanged( new TableModelEvent(DownloadTransferTableModel.this,
                        position, position, TableModelEvent.ALL_COLUMNS,
                        TableModelEvent.INSERT ) );
                }
                else if ( event.getType() == ContainerEvent.Type.REMOVED )
                {
                    fireTableChanged( new TableModelEvent(DownloadTransferTableModel.this,
                        position, position, TableModelEvent.ALL_COLUMNS,
                        TableModelEvent.DELETE ) );            
                }
            }
        });
    }
}