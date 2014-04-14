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
 *  $Id: SWDownloadSet.java 4360 2009-01-16 09:08:57Z gregork $
 */
package phex.download.swarming;

import phex.common.log.NLogger;
import phex.download.DownloadScope;
import phex.servent.Servent;

/**
 * The download set is used to hold everything that is needed for a doing a swarm
 * download. This is the download file, the download segment and the download
 * candidate.
 */
public class SWDownloadSet
{
    private final Servent servent;
    private final SWDownloadFile downloadFile;
    private final SWDownloadCandidate downloadCandidate;
    private DownloadScope downloadScope;
    private SWDownloadSegment downloadSegment;
    

    public SWDownloadSet( Servent servent, SWDownloadFile aDownloadFile,
        SWDownloadCandidate aDownloadCandidate )
    {
        this.servent = servent;
        downloadFile = aDownloadFile;
        downloadCandidate = aDownloadCandidate;
        downloadScope = null;
    }
    
    public Servent getServent()
    {
        return servent;
    }

    public SWDownloadFile getDownloadFile()
    {
        return downloadFile;
    }

    public SWDownloadCandidate getCandidate()
    {
        return downloadCandidate;
    }

    public SWDownloadSegment allocateSegment( )
    {
        NLogger.debug( SWDownloadSet.class, "Allocate segment on set " + this);
        if ( downloadScope == null )
        {
            downloadScope = downloadFile.allocateDownloadScope( 
                downloadCandidate.getAvailableScopeList(), 
                downloadCandidate.getPreferredSegmentSize(),
                downloadCandidate.getSpeed());
            
            if ( downloadScope != null )
            {
                if ( downloadScope.getEnd() == Long.MAX_VALUE )
                {
                    downloadSegment = new SWDownloadSegment( downloadFile,
                        downloadScope.getStart(), SWDownloadConstants.UNKNOWN_FILE_SIZE );
                }
                else
                {
                    downloadSegment = new SWDownloadSegment( downloadFile,
                        downloadScope.getStart(), downloadScope.getLength() );
                }
                downloadCandidate.associateDownloadSegment( downloadSegment );
            }
            // sanity check to make sure!
            NLogger.debug( SWDownloadSet.class, 
                "Allocated segment: " + downloadSegment + " on set " + this);
            downloadCandidate.addToCandidateLog( "Allocated segment: " + downloadSegment + " - " + downloadScope );
        }
        if ( downloadSegment == null )
        {
            return null;
        }
        return downloadSegment;
    }
    
    public SWDownloadSegment getDownloadSegment()
    {
        return downloadSegment;
    }

    /**
     * Releases a allocated download segment.
     */
    public void releaseDownloadSegment( )
    {
        if ( downloadSegment != null )
        {
            NLogger.debug( SWDownloadSet.class, 
                "Release file download segment: " + downloadSegment + " on set " + this);
            downloadFile.releaseDownloadScope( downloadScope, 
                downloadSegment.getTransferredDataSize(), downloadCandidate );
            downloadCandidate.addToCandidateLog( "Release segment: " + downloadSegment 
                + " - " + downloadScope );
            downloadSegment = null;
            downloadScope = null;
        }
        NLogger.debug( SWDownloadSet.class, 
            "Release candidate download segment on set " + this);
        downloadCandidate.releaseDownloadSegment( );
    }

    /**
     * Releases a allocated download set.
     */
    public void releaseDownloadSet( )
    {
        NLogger.debug( SWDownloadSet.class, "Release download set on set " + this );
        releaseDownloadSegment();
        downloadFile.releaseDownloadCandidate( downloadCandidate );
        downloadFile.decrementWorkerCount();
    }

    @Override
    public String toString()
    {
        return "[DownloadSet@" + Integer.toHexString(hashCode()) +": (Segment: " + downloadSegment + " - Candidate: "
            + downloadCandidate + " - File: " + downloadFile + ")]";
    }
}