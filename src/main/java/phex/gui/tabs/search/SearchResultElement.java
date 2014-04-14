/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2006 Phex Development Group
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
 *  $Id: SearchResultElement.java 4364 2009-01-16 10:56:15Z gregork $
 */
package phex.gui.tabs.search;

import java.util.ArrayList;
import java.util.List;

import phex.common.format.HostSpeedFormatUtils;
import phex.download.RemoteFile;
import phex.gui.models.SearchTreeTableModel;
import phex.utils.Localizer;

/**
 * This is a element that is part of the search result data model.
 * It is able to hold a single search results in form of a RemoteFile as well as
 * a list of RemoteFiles that represent the same on different hosts.
 */
public class SearchResultElement
{
    /**
     * The single search result element.
     */
    private RemoteFile remoteFile;
    
    /**
     * The list of search results that this can hold.
     */
    private List<RemoteFile> remoteFileList;
    
    private short bestScore;
    private short bestRating;
    
    /**
     * In kbyte/s
     */
    private int bestSpeed;
    private String cachedBestSpeedFormatted;
    
    public SearchResultElement( RemoteFile aRemoteFile )
    {
        this.remoteFile = aRemoteFile;
        // set best values...
        bestScore = remoteFile.getScore();
        bestRating = remoteFile.getQueryHitHost().getHostRating();
        bestSpeed = remoteFile.getSpeed();
        cachedBestSpeedFormatted = null;
        remoteFileList = new ArrayList<RemoteFile>(2);
    }
    
    public void addRemoteFile( RemoteFile aRemoteFile )
    {
        synchronized( remoteFileList )
        {
            if ( remoteFileList.size() == 0 )
            {
                // first add single search result.
                remoteFileList.add( remoteFile );                
            }
            // now add additional search result...
            remoteFileList.add( aRemoteFile );
            
            // check and update best values
            if ( aRemoteFile.getScore() > bestScore )
            {
                bestScore = aRemoteFile.getScore();
            }
            if ( aRemoteFile.getQueryHitHost().getHostRating() > bestRating )
            {
                bestRating = aRemoteFile.getQueryHitHost().getHostRating();
            }
            if ( aRemoteFile.getSpeed() > bestSpeed )
            {
                bestSpeed = aRemoteFile.getSpeed();
                cachedBestSpeedFormatted = null;
            }
        }
    }
    
    public int getRemoteFileListCount()
    {
        return remoteFileList.size();
    }
    
    public RemoteFile getRemoteFileAt( int index )
    {
        if ( index < 0 || index >= remoteFileList.size() )
        {
            return null;
        }
        return remoteFileList.get( index );
    }
    
    public RemoteFile getSingleRemoteFile()
    {
        return remoteFile;
    }
    
    /**
     * @return
     */
    public RemoteFile[] getRemoteFiles()
    {
        if ( remoteFileList.size() == 0 )
        {
            return new RemoteFile[]{ remoteFile };
        }
        synchronized( remoteFileList )
        {
            RemoteFile[] remoteFiles = new RemoteFile[ remoteFileList.size() ];
            remoteFileList.toArray( remoteFiles );
            return remoteFiles;
        }
    }
    
    /**
     * Returns the displayed value in the table depending of the amount of
     * RemoteFiles collected.
     * This is currently only used for the values SCORE_MODEL_INDEX,
     * HOST_RATING_MODEL_INDEX, HOST_SPEED_MODEL_INDEX, HOST_MODEL_INDEX,
     * HOST_VENDOR_MODEL_INDEX.
     * @param modelIndex
     * @return
     */
    public Object getValue( int modelIndex )
    {
        switch (modelIndex)
        {
            case SearchTreeTableModel.SCORE_MODEL_INDEX:
                return Short.valueOf( bestScore );
            case SearchTreeTableModel.HOST_RATING_MODEL_INDEX:
                return Short.valueOf( bestRating );
            case SearchTreeTableModel.HOST_MODEL_INDEX:
            {
                int listSize = remoteFileList.size();
	            if ( remoteFileList.size() > 0 )
	            {
	                return Localizer.getFormatedString( "NumberOfHosts", 
	                    new Object[]{Integer.valueOf(listSize)} );
	            }
                return remoteFile.getHostAddress();
            }
            case SearchTreeTableModel.HOST_VENDOR_MODEL_INDEX:
            {
                int listSize = remoteFileList.size();
	            if ( remoteFileList.size() > 0 )
	            {
	                return Localizer.getFormatedString( "NumberOfHosts", 
	                    new Object[]{Integer.valueOf(listSize)} );
	            }
                return remoteFile.getQueryHitHost().getVendor();
            }
        }
        return "";
    }
    
    public String getFormattedHostSpeed()
    {
        if ( cachedBestSpeedFormatted == null )
        {
            cachedBestSpeedFormatted = HostSpeedFormatUtils.formatHostSpeed( bestSpeed );
        }
        return cachedBestSpeedFormatted;
    }
}