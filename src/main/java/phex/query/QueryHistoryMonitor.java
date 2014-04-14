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
 *  $Id: QueryHistoryMonitor.java 4364 2009-01-16 10:56:15Z gregork $
 */
package phex.query;

import phex.event.PhexEventTopics;
import phex.host.Host;
import phex.msg.QueryMsg;
import phex.msghandling.MessageSubscriber;
import phex.prefs.core.StatisticPrefs;
import phex.servent.Servent;
import phex.utils.CircularQueue;

public class QueryHistoryMonitor implements MessageSubscriber<QueryMsg>
{
    private boolean isHistoryMonitored;
    private CircularQueue historyQueue;

    public QueryHistoryMonitor()
    {
        isHistoryMonitored = false;
        historyQueue = new CircularQueue( StatisticPrefs.QueryHistoryEntries.get().intValue(),
            StatisticPrefs.QueryHistoryEntries.get().intValue() );
    }
    
    public void setHistoryMonitored( boolean state )
    {
        isHistoryMonitored = state;
    }

    public boolean isHistoryMonitored( )
    {
        return isHistoryMonitored;
    }

    public synchronized void setMaxHistroySize( int size )
    {
        if ( size == historyQueue.getCapacity() )
        {
            return;
        }
        historyQueue = new CircularQueue( size, size );
        StatisticPrefs.QueryHistoryEntries.set( Integer.valueOf( size ) );
    }

    public synchronized int getMaxHistorySize()
    {
        return historyQueue.getCapacity();
    }

    public synchronized int getHistorySize()
    {
        return historyQueue.getSize();
    }

    public synchronized QueryMsg getSearchQueryAt( int index )
    {
        return (QueryMsg) historyQueue.get( index );
    }
    
    public void onMessage(QueryMsg query, Host sourceHost)
    {
        synchronized (this)
        {
            if ( !isHistoryMonitored )
            {
                return;
            }
            String searchString = query.getSearchString();
            if ( searchString.length() > 0 && !searchString.equals( "\\" ) 
                 && !searchString.startsWith( "urn:sha1:" ) )
            {
                historyQueue.addToHead( query );
                fireQueryHistoryChanged( );
            }
        }
    }


    ///////////////////// START event handling methods ////////////////////////
    private void fireQueryHistoryChanged( )
    {
        // empty event..
        Servent.getInstance().getEventService().publish( PhexEventTopics.Query_Monitor,
            "" );
    }
    ///////////////////// END event handling methods ////////////////////////
}