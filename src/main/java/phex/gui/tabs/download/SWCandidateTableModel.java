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
 *  $Id: SWCandidateTableModel.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.download;

import java.awt.EventQueue;
import java.util.Comparator;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import phex.common.format.NumberFormatUtils;
import phex.download.DownloadScopeList;
import phex.download.swarming.SWDownloadCandidate;
import phex.download.swarming.SWDownloadFile;
import phex.download.swarming.SWDownloadInfo;
import phex.download.swarming.SWDownloadSegment;
import phex.download.swarming.SwarmingManager;
import phex.download.swarming.SWDownloadCandidate.CandidateStatus;
import phex.event.ContainerEvent;
import phex.event.PhexEventTopics;
import phex.gui.common.GUIRegistry;
import phex.gui.common.table.FWSortableTableModel;
import phex.gui.common.table.FWTable;
import phex.gui.comparator.DestAddressComparator;
import phex.gui.renderer.HostAddressCellRenderer;
import phex.gui.renderer.ScopeProgressCellRenderer;
import phex.servent.Servent;
import phex.utils.Localizer;

public class SWCandidateTableModel extends FWSortableTableModel
{
    public static final int HOST_MODEL_INDEX = 0;
    public static final int VENDOR_MODEL_INDEX = 1;
    public static final int PROGRESS_MODEL_INDEX = 2;
    public static final int TOTAL_DOWNLOAD_MODEL_INDEX = 3;
    public static final int RATE_MODEL_INDEX = 4;
    public static final int STATUS_MODEL_INDEX = 5;
    
    /**
     * The unique column id is not allowed to ever change over Phex releases. It
     * is used when serializing column information. The column id is contained in
     * the identifier field of the TableColumn.
     */
    public static final int HOST_COLUMN_ID = 1001;
    public static final int VENDOR_COLUMN_ID = 1002;
    public static final int PROGRESS_COLUMN_ID = 1003;
    public static final int TOTAL_DOWNLOAD_COLUMN_ID = 1004;
    public static final int RATE_COLUMN_ID = 1005;
    public static final int STATUS_COLUMN_ID = 1006;

    /**
     * Column ids ordered according to its corresponding model index
     */
    private static final Integer[] COLUMN_IDS = new Integer[]
    {
        HOST_COLUMN_ID,
        VENDOR_COLUMN_ID,
        PROGRESS_COLUMN_ID,
        TOTAL_DOWNLOAD_COLUMN_ID,
        RATE_COLUMN_ID,
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
            Localizer.getString( "SharingHost" ),
            Localizer.getString( "Vendor" ),
            Localizer.getString( "DownloadTable_Available" ),
            Localizer.getString( "DownloadTable_DownloadTotal" ),
            Localizer.getString( "Rate" ),
            Localizer.getString( "Status" )
        };

        tableClasses = new Class[]
        {
            HostAddressCellRenderer.class,
            String.class,
            ScopeProgressCellRenderer.class,
            String.class,
            String.class,
            String.class
        };
    }

    private final SwarmingManager downloadService;

    /**
     * The currently displayed download file of the model.
     */
    private SWDownloadFile downloadFile;

    private FWTable downloadTable;

    /**
     * @param downloadTable The constructor takes the download JTable. This is
     * necessary to get informed of the selection changes of the download table.
     */
    public SWCandidateTableModel( FWTable aDownloadTable, SwarmingManager downloadService )
    {
        super( COLUMN_IDS, tableColumns, tableClasses );
        if ( downloadService == null )
        {
            throw new NullPointerException( "DownloadService missing" );
        }
        this.downloadService = downloadService;
        downloadTable = aDownloadTable;
        downloadTable.getSelectionModel().addListSelectionListener(
            new DownloadSelectionChangeHandler() );
        GUIRegistry.getInstance().getServent().getEventService().processAnnotations( this );
    }

    /**
     * Returns the download file that is currently displayed.
     */
    public SWDownloadFile getDownloadFile()
    {
        return downloadFile;
    }

    public int getRowCount()
    {
        if ( downloadFile == null )
        {
            return 0;
        }
        return downloadFile.getCandidatesCount();
    }

    public Object getValueAt( int row, int column )
    {
        SWDownloadCandidate candidate = downloadFile.getCandidate( row );
        if ( candidate == null )
        {
            fireTableRowsDeleted( row, row );
            return null;
        }
        
        switch( column )
        {
            case 0:
                return candidate.getHostAddress();
            case 1:
                return candidate.getVendor();
            case PROGRESS_MODEL_INDEX:
                return candidate;
            case TOTAL_DOWNLOAD_MODEL_INDEX:
                return NumberFormatUtils.formatSignificantByteSize( 
                    candidate.getTotalDownloadSize() );
            case RATE_MODEL_INDEX:
            {
                SWDownloadSegment segment = candidate.getDownloadSegment();
                if ( segment == null )
                {
                    return null;
                }
                return NumberFormatUtils.formatSignificantByteSize( 
                    segment.getTransferSpeed() ) + Localizer.getString( "PerSec" );
            }
            case STATUS_MODEL_INDEX:
                return SWDownloadInfo.getDownloadCandidateStatusString(
                    candidate );
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
     *
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
        SWDownloadCandidate candidate = downloadFile.getCandidate( row );
        if ( candidate == null )
        {
            return null;
        }
        
        switch( column )
        {
            case PROGRESS_MODEL_INDEX:
            {
                DownloadScopeList availableScopeList = candidate.getAvailableScopeList();
                if ( availableScopeList == null )
                {
                    return null;
                }
                return Long.valueOf( availableScopeList.getAggregatedLength() );
            }
            case TOTAL_DOWNLOAD_MODEL_INDEX:
                return Long.valueOf( candidate.getTotalDownloadSize() );
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
                    double val;
                    if ( timeLeft == 0 )
                    {
                        val = status.ordinal();
                    }
                    else
                    {// timeLeft is not 0.. checked above..
                        val = status.ordinal() - 1.0 
                            + 1 / (double)timeLeft;
                    }
                    return Double.valueOf( val );
                }
            case RATE_MODEL_INDEX:
            {
                SWDownloadSegment segment = candidate.getDownloadSegment();
                if ( segment == null )
                {
                    return null;
                }
                return Long.valueOf( segment.getTransferSpeed() );
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

    private class DownloadSelectionChangeHandler implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            if ( !e.getValueIsAdjusting() )
            {
                ListSelectionModel model = (ListSelectionModel) e.getSource();
                int viewIdx = model.getMinSelectionIndex();
                int modelIdx = downloadTable.translateRowIndexToModel( viewIdx );
                
                SWDownloadFile tmpDownloadFile = downloadService.getDownloadFile( modelIdx );
                if ( tmpDownloadFile != downloadFile )
                {
                    downloadFile = tmpDownloadFile;
                    fireTableDataChanged();
                }
                //Logger.logMessage( Logger.WARNING, Logger.GLOBAL, "select Index "
                //    + idx + " file " + downloadFile);
            }
        }
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
                    fireTableChanged( new TableModelEvent(SWCandidateTableModel.this,
                        position, position, TableModelEvent.ALL_COLUMNS,
                        TableModelEvent.INSERT ) );
                }
                else if ( event.getType() == ContainerEvent.Type.REMOVED )
                {
                    fireTableChanged( new TableModelEvent(SWCandidateTableModel.this,
                        position, position, TableModelEvent.ALL_COLUMNS,
                        TableModelEvent.DELETE ) );            
                }
            }
        });
    }
}