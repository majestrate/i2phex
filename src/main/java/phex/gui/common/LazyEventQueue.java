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
 *  $Id: LazyEventQueue.java 4363 2009-01-16 10:37:30Z gregork $
 */
package phex.gui.common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventListener;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.Timer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * The job of this class is to collect events, narrow them and fire them in
 * Intervals. This way we gain performance in the GUI because it don't need to
 * repaint on every event.
 */
public class LazyEventQueue
{
    public static final int MAX_EVENT_COUNT = 20;
    public ArrayList tableModelEventList;
    public ArrayList listDataEventList;

    public LazyEventQueue()
    {
        tableModelEventList = new ArrayList( MAX_EVENT_COUNT );
        listDataEventList = new ArrayList( MAX_EVENT_COUNT );
        
        ActionListener fireLazyEventAction = new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                fireLazyEvents();
            }
        };
        Timer timer = new Timer( 1000, fireLazyEventAction );
        timer.start();
    }

    public synchronized void addTableModelEvent( TableModelEvent event )
    {
        boolean isNew = true;
        // try to narrow event
        Object source = event.getSource();
        int type = event.getType();
        int firstRow = event.getFirstRow();
        int lastRow = event.getLastRow();
        int column = event.getColumn();
        for ( int i = 0; i < tableModelEventList.size(); i++ )
        {
            TableModelEvent inEvent = (TableModelEvent)tableModelEventList.get( i );
            Object inSource = inEvent.getSource();
            if ( source != inSource )
            {
                continue;
            }
            if ( type != inEvent.getType() )
            {
                continue;
            }
            if ( column != inEvent.getColumn() )
            {
                continue;
            }
            // if event is already part of other event
            if ( firstRow >= inEvent.getFirstRow() &&
                 lastRow <= inEvent.getLastRow() )
            {
                isNew = false;
                break;
            }
            // if event is directly above or below other event
            if ( firstRow + 1 == inEvent.getFirstRow() ||
                 lastRow - 1 == inEvent.getLastRow() )
            {
                int newFirstRow = Math.min( firstRow, inEvent.getFirstRow() );
                int newLastRow =  Math.max( lastRow, inEvent.getLastRow() );
                // merge events
                TableModelEvent newEvent = new TableModelEvent( (TableModel)source,
                    newFirstRow, newLastRow, column, type );
                // delete old event
                tableModelEventList.remove( inEvent );
                // and add new event.
                addTableModelEvent( newEvent );
                isNew = false;
                break;
            }
        }
        // if new add event
        if ( isNew )
        {
            tableModelEventList.add( event );
            if ( tableModelEventList.size() > MAX_EVENT_COUNT )
            {
                fireTableModelEvents();
            }
        }
    }

    public synchronized void addListDataEvent( ListDataEvent event )
    {
        boolean isNew = true;
        // try to narrow event
        Object source = event.getSource();
        int type = event.getType();
        int firstRow = event.getIndex0();
        int lastRow = event.getIndex1();
        for ( int i = 0; i < listDataEventList.size(); i++ )
        {
            ListDataEvent inEvent = (ListDataEvent)listDataEventList.get( i );
            Object inSource = inEvent.getSource();
            if ( source != inSource )
            {
                continue;
            }
            if ( type != inEvent.getType() )
            {
                continue;
            }
            // if event is already part of other event
            if ( firstRow >= inEvent.getIndex0() &&
                 lastRow <= inEvent.getIndex1() )
            {
                isNew = false;
                break;
            }
            // if event is directly above or below other event
            if ( firstRow + 1 == inEvent.getIndex0() ||
                 lastRow - 1 == inEvent.getIndex1() )
            {
                int newFirstRow = Math.min( firstRow, inEvent.getIndex0() );
                int newLastRow =  Math.max( lastRow, inEvent.getIndex1() );
                // merge events
                ListDataEvent newEvent = new ListDataEvent( (ListModel)source,
                    type, newFirstRow, newLastRow );
                // delete old event
                listDataEventList.remove( inEvent );
                // and add new event.
                addListDataEvent( newEvent );
                isNew = false;
                break;
            }
        }
        // if new add event
        if ( isNew )
        {
            listDataEventList.add( event );
            if ( listDataEventList.size() > MAX_EVENT_COUNT )
            {
                fireListDataEvents();
            }
        }
    }

    public synchronized void fireLazyEvents( )
    {
        fireTableModelEvents();
        fireListDataEvents();
    }

    private synchronized void fireListDataEvents()
    {
        int size = listDataEventList.size();
        if ( size == 0 )
        {
            return;
        }
        for ( int i = 0; i < size; i++ )
        {
            ListDataEvent event = (ListDataEvent) listDataEventList.get( i );
            AbstractListModel model = (AbstractListModel) event.getSource();
            EventListener[] listeners = model.getListeners( ListDataListener.class );
            for ( int x = listeners.length - 1; x >= 0; x-- )
            {
                ListDataListener listener = (ListDataListener)listeners[ x ];
                if ( event.getType() == ListDataEvent.CONTENTS_CHANGED )
                {
                    listener.contentsChanged( event );
                }
                else if ( event.getType() == ListDataEvent.INTERVAL_ADDED )
                {
                    listener.intervalAdded( event );
                }
                else if ( event.getType() == ListDataEvent.INTERVAL_REMOVED )
                {
                    listener.intervalRemoved( event );
                }
            }
        }
        listDataEventList.clear();
    }

    private synchronized void fireTableModelEvents()
    {
        int size = tableModelEventList.size();
        if ( size == 0 )
        {
            return;
        }
        for ( int i = 0; i < size; i++ )
        {
            TableModelEvent event = (TableModelEvent) tableModelEventList.get( i );
            AbstractTableModel model = (AbstractTableModel) event.getSource();
            //System.out.println( "fire "  + event.getSource() + " " + event.getFirstRow() +
            //    " " + event.getLastRow() + " " + event.getType() );
            model.fireTableChanged( event );
        }
        tableModelEventList.clear();
    }
}