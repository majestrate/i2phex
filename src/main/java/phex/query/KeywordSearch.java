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
 *  --- CVS Information ---
 *  $Id: KeywordSearch.java 4357 2009-01-15 23:03:50Z gregork $
 */
package phex.query;

import java.util.ArrayList;
import java.util.StringTokenizer;

import phex.common.URN;
import phex.common.address.DestAddress;
import phex.download.RemoteFile;
import phex.msg.InvalidMessageException;
import phex.msg.QueryMsg;
import phex.msg.QueryResponseMsg;
import phex.msg.QueryResponseRecord;
import phex.prefs.core.MessagePrefs;
import phex.servent.Servent;
import phex.utils.SearchEngine;

public class KeywordSearch extends Search
{    
    /**
     * The String to search for.
     */
    private String searchString;

    /**
     * The URN to search for.
     */
    private URN searchURN;


    public KeywordSearch( String aSearchString, boolean isFirewalled, Servent servent )
    {
        this( aSearchString, null, isFirewalled, servent );
    }

    public KeywordSearch( String aSearchString, URN aSearchURN, boolean isFirewalled, Servent servent )
    {
        super( servent );
        searchString = aSearchString;
        searchURN = aSearchURN;

        queryMsg = new QueryMsg( MessagePrefs.TTL.get().byteValue(),
            searchString, searchURN, QueryMsg.IS_PHEX_CAPABLE_OF_XML_RESULTS,
            isFirewalled );
    }
    
    public String getSearchString()
    {
        return searchString;
    }
    
    public void setSearchString( String aSearchString, boolean isFirewalled )
    {
        searchString = aSearchString;
        searchURN = null;
        queryMsg = new QueryMsg( MessagePrefs.TTL.get().byteValue(), 
            searchString, searchURN, QueryMsg.IS_PHEX_CAPABLE_OF_XML_RESULTS,
            isFirewalled );
        fireSearchChanged();
    }

    @Override
    public void processResponse( QueryResponseMsg msg )
        throws InvalidMessageException
    {
        //we like to receive results even if the query was stopped already.
        
        // check if it is a response for this query?
        if (!msg.getHeader().getMsgID().equals( queryMsg.getHeader().getMsgID()))
        {
            return;
        }

        // remoteHost.log("Got response to my query.  " + msg);

        QueryHitHost qhHost = QueryHitHost.createFrom( msg );
        RemoteFile rfile;
        QueryResponseRecord[] records = msg.getMsgRecords();
        ArrayList<RemoteFile> newHitList = new ArrayList<RemoteFile>( records.length );
        for (int i = 0; i < records.length; i++)
        {
            // verify record when using a urn query
            // this acts like a filter but there seem to be no need to make this
            // not permanent...
            if ( searchURN != null && records[i].getURN() != null )
            {
                if ( !searchURN.equals( records[i].getURN() ) )
                {
                    continue;
                }
            }
            
            if ( !isResponseRecordValid( records[i] ) )
            {// skip record.
                continue;
            }

            synchronized( this )
            {
                long fileSize = records[i].getFileSize();
                String filename = records[i].getFilename();
                URN urn = records[i].getURN();
                int fileIndex = records[i].getFileIndex();
                String metaData = records[i].getMetaData();
                
                // search string might be null in case whats new search is used
                short score = searchString == null ? 100 : 
                    KeywordSearch.calculateSearchScore( searchString, filename );
                
                // find duplicate from same host...
                RemoteFile availableHit = searchResultHolder.findQueryHit( 
                    qhHost, urn, filename, fileSize, fileIndex );
                
                if ( availableHit != null )
                {
                    // update availableHit
                    availableHit.updateQueryHitHost( qhHost );
                    availableHit.setMetaData( metaData );
                }
                else
                {
                    String pathInfo = records[i].getPathInfo();
                    rfile = new RemoteFile( qhHost, fileIndex, filename, pathInfo,
                        fileSize, urn, metaData, score );
                    searchResultHolder.addQueryHit( rfile );
                    newHitList.add( rfile );
                }
                // handle possible AlternateLocations
                DestAddress[] alternateLocations = records[i].getAlternateLocations();
                if ( urn != null && alternateLocations != null)
                {
                    for ( int j = 0; j < alternateLocations.length; j++ )
                    {
                        // find duplicate from same host...
                        QueryHitHost qhh = new QueryHitHost( null, alternateLocations[j], -1 );
                        
                        availableHit = searchResultHolder.findQueryHit( qhHost,
                            urn, filename, fileSize, fileIndex );
                        if ( availableHit != null )
                        {
                            // update availableHit
                            availableHit.updateQueryHitHost( qhHost );
                            availableHit.setMetaData( metaData );
                        }
                        else
                        {
                            rfile = new RemoteFile( qhh, -1, filename, "", 
                                fileSize, urn, metaData, score );
                            searchResultHolder.addQueryHit( rfile );
                            newHitList.add( rfile );
                        }
                    }
                }
            }
        }
        // if something was added...
        if ( newHitList.size() > 0 )
        {
            if ( queryEngine != null )
            {
                queryEngine.incrementResultCount( msg.getUniqueResultCount() );
            }
            RemoteFile[] newHits = new RemoteFile[ newHitList.size() ];
            newHitList.toArray( newHits );
            fireSearchHitsAdded( newHits );
        }
    }
    
    /**
     * This methods calculates the score of a search result. The return value is
     * between 0 and 100. A value of 100 means all terms of the search string
     * are matched 100% in the result string.
     */
    public static short calculateSearchScore( String searchStr, String resultStr )
    {
        double tokenCount = 0;
        double hitCount = 0;
        StringTokenizer tokens = new StringTokenizer( searchStr );
        SearchEngine searchEngine = new SearchEngine();
        searchEngine.setText(resultStr, false);
        while ( tokens.hasMoreTokens() )
        {
            String token = tokens.nextToken();
            tokenCount ++;
            searchEngine.setPattern( token, false );
            if ( searchEngine.match() )
            {
                hitCount ++;
            }
        }
        double perc = hitCount / tokenCount * 100;
        return (short) perc;
    }
    
    @Override
    public String toString()
    {
        return "[KeywordSearch:" + searchString + "," + super.toString() + "]";
    }
}