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
 *  $Id: SharedFilesTableModel.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.library;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import phex.event.PhexEventTopics;
import phex.gui.common.GUIRegistry;
import phex.gui.common.table.FWSortableTableModel;
import phex.gui.renderer.FileSizeCellRenderer;
import phex.servent.Servent;
import phex.share.ShareFile;
import phex.share.SharedFilesService;
import phex.thex.ShareFileThexData;
import phex.utils.FilesOnlyFileFilter;
import phex.utils.Localizer;

public class SharedFilesTableModel extends FWSortableTableModel
{
    public static final int FILE_MODEL_INDEX = 0;
    public static final int DIRECTORY_MODEL_INDEX = 1;
    public static final int SIZE_MODEL_INDEX = 2;
    public static final int SEARCH_COUNT_MODEL_INDEX = 3;
    public static final int UPLOAD_COUNT_MODEL_INDEX = 4;
    public static final int ALT_LOC_COUNT_MODEL_INDEX = 5;
    public static final int SHA1_MODEL_INDEX = 6;
    public static final int THEX_MODEL_INDEX = 7;
    
    /**
     * The unique column id is not allowed to ever change over Phex releases. It
     * is used when serializing column information. The column id is contained in
     * the identifier field of the TableColumn.
     */
    public static final int FILE_COLUMN_ID = 1001;
    public static final int DIRECTORY_COLUMN_ID = 1002;
    public static final int SIZE_COLUMN_ID = 1003;
    public static final int SEARCH_COUNT_COLUMN_ID = 1004;
    public static final int UPLOAD_COUNT_COLUMN_ID = 1005;
    public static final int SHA1_COLUMN_ID = 1006;
	public static final int THEX_COLUMN_ID = 1007;
    public static final int ALT_LOC_COUNT_COLUMN_ID = 1008;

    /**
     * Column id's ordered according to its corresponding model index
     */
    private static final Integer[] COLUMN_IDS = new Integer[]
    {
        FILE_COLUMN_ID,
        DIRECTORY_COLUMN_ID,
        SIZE_COLUMN_ID,
        SEARCH_COUNT_COLUMN_ID,
        UPLOAD_COUNT_COLUMN_ID,
        ALT_LOC_COUNT_COLUMN_ID,
        SHA1_COLUMN_ID,
        THEX_COLUMN_ID
    };
        

    private static String[] tableColumns;
    private static Class[] tableClasses;

    static
    {
        tableColumns = new String[]
        {
            Localizer.getString( "File" ),
            Localizer.getString( "Directory" ),
            Localizer.getString( "Size" ),
            Localizer.getString( "SearchCount" ),
            Localizer.getString( "UploadCount" ),
            Localizer.getString( "SharedFilesTable_AltLocCount" ),
            Localizer.getString( "SHA1" ),
			Localizer.getString( "SharedFilesTable_TigerTree" )
        };

        tableClasses = new Class[]
        {
            FileSystemTableCellRenderer.class,
            String.class,
            FileSizeCellRenderer.class,
            Integer.class,
            Integer.class,
            Integer.class,
            String.class,
            String.class
        };
    }
    
    private FileFilter fileFilter = new FilesOnlyFileFilter();
    private File displayDirectory;
    /**
     * Caching buffer of the files in display directory for performance and memory
     * savings.
     */
    private File[] displayDirectryFiles;
    private SharedFilesService sharedFilesService;

    public SharedFilesTableModel()
    {
        super( COLUMN_IDS, tableColumns, tableClasses );
        Servent servent = GUIRegistry.getInstance().getServent();
        sharedFilesService = servent.getSharedFilesService();
        
        servent.getEventService().processAnnotations( this );
    }

    /**
     * @param displayDirectory The displayDirectory to set.
     */
    public void setDisplayDirectory(File displayDirectory)
    {
        this.displayDirectory = displayDirectory;
        if ( displayDirectory != null )
        {
            displayDirectryFiles = displayDirectory.listFiles(fileFilter);
        }
        fireTableDataChanged();
    }
    
    public int getRowCount()
    {
        if ( displayDirectory == null )
        {
            return 0;
        }
        if (displayDirectryFiles == null)
        {
            return 0;
        }
        return displayDirectryFiles.length;
    }

    public Object getValueAt(int row, int col)
    {
        if ( displayDirectory == null )
        {
            return "";
        }
        if ( row >= displayDirectryFiles.length )
        {
            fireTableRowsDeleted( row, row );
            return "";
        }
        ShareFile shareFile = sharedFilesService.getShareFileByFile( displayDirectryFiles[row] );
        if ( shareFile == null )
        {
            switch ( col )
            {
                case FILE_MODEL_INDEX:
                    return displayDirectryFiles[row];

                case DIRECTORY_MODEL_INDEX:
                    return displayDirectryFiles[row].getParent();

                case SIZE_MODEL_INDEX:
                    return Long.valueOf( displayDirectryFiles[row].length() );

                case SEARCH_COUNT_MODEL_INDEX:
                case UPLOAD_COUNT_MODEL_INDEX:
                case ALT_LOC_COUNT_MODEL_INDEX:
                    return null;
                case SHA1_MODEL_INDEX:
                case THEX_MODEL_INDEX:
                    return "";
            }
        }
        else
        {
            switch ( col )
            {
                case FILE_MODEL_INDEX:
                    return shareFile;
    
                case DIRECTORY_MODEL_INDEX:
                    return shareFile.getSystemFile().getParent();
    
                case SIZE_MODEL_INDEX:
                    return Long.valueOf( shareFile.getFileSize() );
    
                case SEARCH_COUNT_MODEL_INDEX:
                    return Integer.valueOf( shareFile.getSearchCount() );
                    
                case UPLOAD_COUNT_MODEL_INDEX:
                    return Integer.valueOf( shareFile.getUploadCount() );
                    
                case ALT_LOC_COUNT_MODEL_INDEX:
                    return Integer.valueOf( shareFile.getAltLocCount() );
                    
                case SHA1_MODEL_INDEX:
                    return shareFile.getSha1();
                    
                case THEX_MODEL_INDEX:
                    ShareFileThexData thexData = shareFile.getThexData( null );
                    return thexData != null ? thexData.getRootHash() : "";
            }
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
    public Object getComparableValueAt( int row, int column )
    {
        if ( displayDirectory == null )
        {
            return getValueAt( row, column );
        }
        if ( row >= displayDirectryFiles.length )
        {
            return getValueAt( row, column );
        }
        switch ( column )
        {
            case FILE_MODEL_INDEX:
                ShareFile shareFile = sharedFilesService.getShareFileByFile( 
                    displayDirectryFiles[row] );
                if ( shareFile == null )
                {
                    return displayDirectryFiles[row].getName();
                }
                else
                {
                    return shareFile.getFileName();
                }
        }
        return getValueAt( row, column );
    }
    
    /**
     * Returns the most comparator that is used for sorting of the cell values
     * in the column. This is used by the FWSortedTableModel to perform the
     * sorting. If not overwritten the method returns null causing the
     * FWSortedTableModel to use a NaturalComparator. It expects all Objects that
     * are returned from getComparableValueAt() to implement the Comparable interface.
     *
     */
    public Comparator getColumnComparator( int column )
    {
        switch ( column )
        {
            case SIZE_MODEL_INDEX:
                return ComparableComparator.getInstance();
        }
        return null;
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
    
    /**
     * Indicates if a column is visible by default.
     */
    @Override
    public boolean isColumnDefaultVisible( int columnIndex )
    {
        if ( columnIndex == SHA1_MODEL_INDEX
          || columnIndex == THEX_MODEL_INDEX
          || columnIndex == SEARCH_COUNT_MODEL_INDEX
          || columnIndex == ALT_LOC_COUNT_MODEL_INDEX )
        {
            return false;
        }
        return true;
    }
    
    @EventTopicSubscriber(topic=PhexEventTopics.Share_Update)
    public void onShareUpdateEvent( String topic, Object event )
    {
        EventQueue.invokeLater( new Runnable()
        {
            public void run()
            {
                fireTableDataChanged();
            }
        });
    }
}