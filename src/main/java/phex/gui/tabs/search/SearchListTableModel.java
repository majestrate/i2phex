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
 *  $Id: SearchListTableModel.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.search;

import java.awt.EventQueue;
import java.util.Comparator;

import javax.swing.event.TableModelEvent;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import phex.event.ContainerEvent;
import phex.event.PhexEventTopics;
import phex.gui.common.GUIRegistry;
import phex.gui.common.table.FWSortableTableModel;
import phex.gui.renderer.ProgressCellRenderer;
import phex.query.BrowseHostResults;
import phex.query.KeywordSearch;
import phex.query.Search;
import phex.query.SearchContainer;
import phex.query.WhatsNewSearch;
import phex.servent.Servent;
import phex.utils.Localizer;

public class SearchListTableModel extends FWSortableTableModel
{
    public static final int SEARCH_TERM_MODEL_INDEX = 0;
    public static final int RESULT_COUNT_MODEL_INDEX = 1;
    public static final int FILTERED_COUNT_MODEL_INDEX = 2;
    public static final int PROGRESS_MODEL_INDEX = 3;
    public static final int STATUS_MODEL_INDEX = 4;
    
    /**
     * The unique column id is not allowed to ever change over Phex releases. It
     * is used when serializing column information. The column id is containd in
     * the identifier field of the TableColumn.
     */
    public static final int SEARCH_TERM_COLUMN_ID = 1001;
    public static final int RESULT_COUNT_COLUMN_ID = 1002;
    public static final int FILTERED_COUNT_COLUMN_ID = 1003;
    public static final int PROGRESS_COLUMN_ID = 1004;
    public static final int STATUS_COLUMN_ID = 1005;
    
    /**
     * Column ids ordered according to its corresponding model index
     */
    private static final Integer[] COLUMN_IDS = new Integer[]
    {
        SEARCH_TERM_COLUMN_ID,
        RESULT_COUNT_COLUMN_ID,
        FILTERED_COUNT_COLUMN_ID,
        PROGRESS_COLUMN_ID,
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
            Localizer.getString( "SearchTerm" ),
            Localizer.getString( "Results" ),
            Localizer.getString( "Filtered" ),
            Localizer.getString( "Progress" ),
            Localizer.getString( "Status" )
        };

        tableClasses = new Class[]
        {
            String.class,
            Integer.class,
            Integer.class,
            ProgressCellRenderer.class,
            String.class
        };
    }

    private SearchContainer searchContainer;

    public SearchListTableModel( SearchContainer searchContainer )
    {
        super( COLUMN_IDS, tableColumns, tableClasses );
        this.searchContainer = searchContainer;
        GUIRegistry.getInstance().getServent().getEventService().processAnnotations( this );
    }

    public int getRowCount()
    {
        return searchContainer.getSearchCount();
    }

    public Object getValueAt(int row, int col)
    {
        Search search = searchContainer.getSearchAt( row );
        if (search == null)
        {
            return "";
        }
        SearchResultsDataModel dataModel;
        switch (col)
        {
            case SEARCH_TERM_MODEL_INDEX:
                if ( search instanceof KeywordSearch )
                {
                    return ((KeywordSearch)search).getSearchString();
                }
                else if ( search instanceof WhatsNewSearch )
                {
                    return Localizer.getString("SearchTab_WhatsNewSearch");
                }
                else if ( search instanceof BrowseHostResults )
                {
                    return Localizer.getFormatedString("SearchTab_BrowsingHost",
                        new Object[] {((BrowseHostResults)search).getDestAddress().getFullHostName()});
                }
                else
                {
                    throw new RuntimeException("Unknwon search type");
                }
            case RESULT_COUNT_MODEL_INDEX:
                dataModel = SearchResultsDataModel.lookupResultDataModel( search );
                return Integer.valueOf( dataModel.getSearchElementCount() );
            case FILTERED_COUNT_MODEL_INDEX:
                dataModel = SearchResultsDataModel.lookupResultDataModel( search );
                return Integer.valueOf( dataModel.getFilteredElementCount() );
            case PROGRESS_MODEL_INDEX:
                return Integer.valueOf( search.getProgress() );
            case STATUS_MODEL_INDEX:
                if (search.isSearching())
                {
                    return Localizer.getString("Searching");
                }
                else
                {
                    return Localizer.getString("Search_Stopped");
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
    @Override
    public Object getComparableValueAt( int row, int column )
    {
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
    @Override
    public Comparator<?> getColumnComparator( int column )
    {
        switch( column )
        {
            case PROGRESS_MODEL_INDEX:
                return ComparableComparator.getInstance();
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
        if ( columnIndex == SEARCH_TERM_MODEL_INDEX )
        {
            return false;
        }
        return true;
    }
    
    
    ///////////////////////// START event handling ////////////////////////////
    @EventTopicSubscriber(topic=PhexEventTopics.Search_Update)
    public void onSearchUpdateEvent( String topic, final ContainerEvent event )
    {
        if ( searchContainer != event.getContainer() )
        {
            return;
        }
        EventQueue.invokeLater( new Runnable() {
            public void run()
            {
                int position = event.getPosition();
                if ( event.getType() == ContainerEvent.Type.ADDED )
                {
                    fireTableChanged( new TableModelEvent(SearchListTableModel.this,
                        position, position, TableModelEvent.ALL_COLUMNS,
                        TableModelEvent.INSERT ) );
                }
                else if ( event.getType() == ContainerEvent.Type.REMOVED )
                {
                    fireTableChanged( new TableModelEvent(SearchListTableModel.this,
                        position, position, TableModelEvent.ALL_COLUMNS,
                        TableModelEvent.DELETE ) );            
                }
            }
        });
    }
    ///////////////////////// END event handling ////////////////////////////
}
