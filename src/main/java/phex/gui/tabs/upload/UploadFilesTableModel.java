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
 *  $Id: UploadFilesTableModel.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.upload;

import java.awt.EventQueue;
import java.util.Comparator;

import javax.swing.event.TableModelEvent;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import phex.common.format.NumberFormatUtils;
import phex.event.ContainerEvent;
import phex.event.PhexEventTopics;
import phex.gui.common.GUIRegistry;
import phex.gui.common.table.FWSortableTableModel;
import phex.gui.comparator.DestAddressComparator;
import phex.gui.comparator.TransferSizeComparator;
import phex.gui.renderer.ETACellRenderer;
import phex.gui.renderer.HostAddressCellRenderer;
import phex.gui.renderer.ProgressCellRenderer;
import phex.gui.renderer.TransferSizeCellRenderer;
import phex.servent.Servent;
import phex.upload.UploadManager;
import phex.upload.UploadState;
import phex.utils.Localizer;

public class UploadFilesTableModel extends FWSortableTableModel
{
    public static final int HOST_MODEL_INDEX = 0;
    public static final int VENDOR_MODEL_INDEX = 1;
    public static final int FILE_MODEL_INDEX = 2;
    public static final int PROGRESS_MODEL_INDEX = 3;
    public static final int SIZE_MODEL_INDEX = 4;
    public static final int RATE_MODEL_INDEX = 5;
    public static final int ETA_MODEL_INDEX = 6;
    public static final int STATUS_MODEL_INDEX = 7;
    
    /**
     * The unique column id is not allowed to ever change over Phex releases. It
     * is used when serializing column information. The column id is containd in
     * the identifier field of the TableColumn.
     */
    public static final int HOST_COLUMN_ID = 1001;
    public static final int VENDOR_COLUMN_ID = 1002;
    public static final int FILE_COLUMN_ID = 1003;
    public static final int PROGRESS_COLUMN_ID = 1004;
    public static final int SIZE_COLUMN_ID = 1005;
    public static final int RATE_COLUMN_ID = 1006;
    public static final int STATUS_COLUMN_ID = 1007;
    public static final int ETA_COLUMN_ID = 1008;

    /**
     * Column ids orderd according to its corresponding model index
     */
    private static final Integer[] COLUMN_IDS = new Integer[]
    {
        HOST_COLUMN_ID,
        VENDOR_COLUMN_ID,
        FILE_COLUMN_ID,
        PROGRESS_COLUMN_ID,
        SIZE_COLUMN_ID,
        RATE_COLUMN_ID,
        ETA_COLUMN_ID,
        STATUS_COLUMN_ID
    };
    

    private static String[] tableColumns;
    private static Class[] tableClasses;

    static
    {
        tableColumns = new String[]
        {
            Localizer.getString( "Host" ),
            Localizer.getString( "Vendor" ),
            Localizer.getString( "File" ),
            Localizer.getString( "PercentSign" ),
            Localizer.getString( "Size" ),
            Localizer.getString( "Rate" ),
            Localizer.getString( "UploadTable_ETA" ),
            Localizer.getString( "Status" )
        };

        tableClasses = new Class[]
        {
            HostAddressCellRenderer.class,
            String.class,
            String.class,
            ProgressCellRenderer.class,
            TransferSizeCellRenderer.class,
            String.class,
            ETACellRenderer.class,
            String.class
        };
    }

    private UploadManager uploadMgr;

    public UploadFilesTableModel( UploadManager uploadMgr )
    {
        super( COLUMN_IDS, tableColumns, tableClasses );
        this.uploadMgr = uploadMgr;
        GUIRegistry.getInstance().getServent().getEventService().processAnnotations( this );
    }

    public int getRowCount()
    {
        return uploadMgr.getUploadListSize();
    }

    public Object getValueAt( int row, int col )
    {
        UploadState uploadState = uploadMgr.getUploadStateAt( row );
        if ( uploadState == null )
        {
            fireTableRowsDeleted( row, row );
            return "";
        }

        switch ( col )
        {
            case HOST_MODEL_INDEX:
                return uploadState.getHostAddress();

            case VENDOR_MODEL_INDEX:
                String vendor = uploadState.getVendor();
                if ( vendor == null )
                {
                    return "";
                }
                else
                {
                    return vendor;
                }

            case FILE_MODEL_INDEX:
                return uploadState.getFileName();

            case PROGRESS_MODEL_INDEX:
                return Integer.valueOf( uploadState.getProgress() );

            case SIZE_MODEL_INDEX:
                return uploadState;
            case RATE_MODEL_INDEX:
            {
                return NumberFormatUtils.formatSignificantByteSize( 
                    uploadState.getTransferSpeed() ) + Localizer.getString( "PerSec" );
            }
            case ETA_MODEL_INDEX:
                return uploadState;
            case STATUS_MODEL_INDEX:
                return UploadStatusInfo.getUploadStatusString( uploadState.getStatus() );
        }
        return "";
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
        UploadState uploadState = uploadMgr.getUploadStateAt( row );
        if ( uploadState == null )
        {
            return "";
        }
        switch ( column )
        {
            case RATE_MODEL_INDEX:
                return Long.valueOf( uploadState.getTransferSpeed() );
        }
        return getValueAt( row, column );
    }

    /**
     * Returns the most comparator that is used for sorting of the cell values
     * in the column. This is used by the FWSortedTableModel to perform the
     * sorting. If not overwritten the method returns null causing the
     * FWSortedTableModel to use a NaturalComparator. It expects all Objects that
     * are returned from getComparableValueAt() to implement the Comparable interface.
     */
    @Override
    public Comparator getColumnComparator( int column )
    {
        switch( column )
        {
            case HOST_MODEL_INDEX:
                return new DestAddressComparator();
            case PROGRESS_MODEL_INDEX:
                return ComparableComparator.getInstance();
            case SIZE_MODEL_INDEX:
                return new TransferSizeComparator();
            // for all other columns use default comparator
            default:
                return null;
        }
    }
    
    /**
     * Indicates if a column is hideable.
     */
    @Override
    public boolean isColumnHideable( int columnIndex )
    {
        if ( columnIndex == FILE_MODEL_INDEX )
        {
            return false;
        }
        return true;
    }
    
    @EventTopicSubscriber(topic=PhexEventTopics.Upload_State)
    public void onUploadStateEvent( String topic, final ContainerEvent event )
    {
        EventQueue.invokeLater( new Runnable() {
            public void run()
            {
                int position = event.getPosition();
                if ( event.getType() == ContainerEvent.Type.ADDED )
                {
                    fireTableChanged(
                        new TableModelEvent( UploadFilesTableModel.this, position, position,
                        TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT ) );
                }
                else if ( event.getType() == ContainerEvent.Type.REMOVED )
                {
                    fireTableChanged(
                        new TableModelEvent( UploadFilesTableModel.this, position, position,
                        TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE ) );
                }
            }
        } );
    }
        
}