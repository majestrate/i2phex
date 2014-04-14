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
 *  $Id: SecurityTableModel.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.security;

import java.awt.EventQueue;
import java.util.Comparator;

import javax.swing.event.TableModelEvent;

import org.bushe.swing.event.annotation.EventTopicSubscriber;

import phex.event.ContainerEvent;
import phex.event.PhexEventTopics;
import phex.gui.common.GUIRegistry;
import phex.gui.common.table.FWSortableTableModel;
import phex.gui.renderer.DateCellRenderer;
import phex.security.IpSecurityRule;
import phex.security.IpSecurityRuleComparator;
import phex.security.PhexSecurityManager;
import phex.servent.Servent;
import phex.utils.Localizer;

public class SecurityTableModel extends FWSortableTableModel
{
    private static final int ADDRESS_MODEL_INDEX = 0;
    private static final int TYPE_MODEL_INDEX = 1;
    private static final int EXPIRES_MODEL_INDEX = 2;
    private static final int TRIGGER_COUNT_MODEL_INDEX = 3;
    private static final int DESCRIPTION_MODEL_INDEX = 4;
    
    /**
     * The unique column id is not allowed to ever change over Phex releases. It
     * is used when serializing column information. The column id is contained in
     * the identifier field of the TableColumn.
     */
    private static final int ADDRESS_COLUMN_ID = 1001;
    private static final int TYPE_COLUMN_ID = 1002;
    private static final int EXPIRES_COLUMN_ID = 1003;
    private static final int TRIGGER_COUNT_COLUMN_ID = 1004;
    private static final int DESCRIPTION_COLUMN_ID = 1005;

    /**
     * Column ids ordered according to its corresponding model index
     */
    private static final Integer[] COLUMN_IDS = new Integer[]
    {
        ADDRESS_COLUMN_ID,
        TYPE_COLUMN_ID,
        EXPIRES_COLUMN_ID,
        TRIGGER_COUNT_COLUMN_ID,
        DESCRIPTION_COLUMN_ID,
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
            Localizer.getString( "Address" ),
            Localizer.getString( "Type" ),
            Localizer.getString( "Expires" ),
            Localizer.getString( "TriggerCount" ),
            Localizer.getString( "Description" )
        };

        tableClasses = new Class[]
        {
            String.class,
            String.class,
            DateCellRenderer.class,
            Integer.class,
            String.class,
        };
    }

    private PhexSecurityManager securityMgr;

    public SecurityTableModel()
    {
        super( COLUMN_IDS, tableColumns, tableClasses );
        Servent servent = GUIRegistry.getInstance().getServent();
        securityMgr = servent.getSecurityService();
        servent.getEventService().processAnnotations( this );
    }

    public int getRowCount()
    {
        return securityMgr.getIPAccessRuleCount();
    }

    public Object getValueAt( int row, int column )
    {
        IpSecurityRule rule = securityMgr.getIPAccessRule( row );
        if ( rule == null )
        {
            fireTableRowsDeleted( row, row );
            return "";
        }

        switch( column )
        {
            case ADDRESS_MODEL_INDEX:
                return rule.getAddressString();
            case TYPE_MODEL_INDEX:
                if ( rule.isDenyingRule() )
                {
                    return Localizer.getString( "Deny" );
                }
                else
                {
                    return Localizer.getString( "Accept" );
                }
            case EXPIRES_MODEL_INDEX:
                return rule.getExpiryDate();
            case TRIGGER_COUNT_MODEL_INDEX:
                return Integer.valueOf( rule.getTriggerCount() );
            case DESCRIPTION_MODEL_INDEX:
                return rule.getDescription();
            default:
                return "";
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
    public Comparator getColumnComparator( int column )
    {
        switch( column )
        {
            case ADDRESS_MODEL_INDEX:
                return IpSecurityRuleComparator.INSTANCE;
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
            case ADDRESS_MODEL_INDEX:
                IpSecurityRule rule = securityMgr.getIPAccessRule( row );
                return rule;
        }
        return getValueAt( row, column );
    }

    /**
     * Indicates if a column is hideable.
     */
    @Override
    public boolean isColumnHideable( int columnIndex )
    {
        if ( columnIndex == ADDRESS_MODEL_INDEX )
        {
            return false;
        }
        return true;
    }

    ///////////////////////// START event handling ////////////////////////////

    @EventTopicSubscriber(topic=PhexEventTopics.Security_Rule)
    public void onSecurityRuleEvent( String topic, final ContainerEvent event )
    {
        EventQueue.invokeLater( new Runnable() {
            public void run()
            {
                int position = event.getPosition();
                if ( event.getType() == ContainerEvent.Type.ADDED )
                {
                    fireTableChanged(
                        new TableModelEvent( SecurityTableModel.this, position, position,
                        TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT ) );
                }
                else if ( event.getType() == ContainerEvent.Type.REMOVED )
                {
                    fireTableChanged(
                        new TableModelEvent( SecurityTableModel.this, position, position,
                        TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE ) );
                }
            }
        } );
    }
    ///////////////////////// END event handling ////////////////////////////
}