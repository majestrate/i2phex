package phex.gui.common.progressbar;

import java.awt.Color;
import java.util.NoSuchElementException;

import phex.download.DownloadScopeList;
import phex.download.swarming.SWDownloadCandidate;
import phex.gui.common.PhexColors;

public class CandidateScopeProvider implements MultiScopeProvider
{
    private SWDownloadCandidate candidate;
    
    public CandidateScopeProvider( )
    {
    }
    
    public void setCandidate( SWDownloadCandidate candidate )
    {
        this.candidate = candidate;
    }
    
    public DownloadScopeList getScopeAt(int idx)
    {
        switch ( idx )
        {
        case 0:
            return candidate.getAvailableScopeList();
        default:
            throw new NoSuchElementException( "Index: " + idx );
        }
    }

    public Color getScopeColor( int idx )
    {
        switch ( idx )
        {
        case 0:
            return PhexColors.getScopeProgressBarForeground();
        default:
            throw new NoSuchElementException( "Index: " + idx );
        }
    }

    public int getScopeCount()
    {
        if ( candidate  == null )
        {
            return 0;
        }
        if ( candidate.getAvailableScopeList() == null )
        {
            return 0;
        }
        return 1;
    }
    
    public long getFileSize()
    {
        return candidate.getDownloadFile().getTotalDataSize();
    }
}
