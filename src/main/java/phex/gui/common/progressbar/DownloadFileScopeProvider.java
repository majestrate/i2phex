package phex.gui.common.progressbar;

import java.awt.Color;
import java.util.NoSuchElementException;

import phex.download.DownloadScopeList;
import phex.download.swarming.SWDownloadFile;
import phex.gui.common.PhexColors;

public class DownloadFileScopeProvider implements MultiScopeProvider
{
    private SWDownloadFile file;
    
    public DownloadFileScopeProvider( SWDownloadFile file )
    {
        this.file = file;
    }
    
    public DownloadScopeList getScopeAt(int idx)
    {
        switch ( idx )
        {
        case 0:
            return file.getMemoryFile().getFinishedScopeList();
        case 1:
            return file.getMemoryFile().getUnverifiedScopeList();
        case 2:
            return file.getMemoryFile().getToBeVerifiedScopeList();
        case 3:
            return file.getMemoryFile().getBlockedScopeList();
        default:
            throw new NoSuchElementException( "Index: " + idx );
        }
    }

    public Color getScopeColor( int idx )
    {
        switch ( idx )
        {
        case 0:
            return PhexColors.getFinishedScopeProgressBarForeground();
        case 1:
            return PhexColors.getUnverifiedScopeProgressBarForeground();
        case 2:
            return PhexColors.getUnverifiedScopeProgressBarForeground();
        case 3:
            return PhexColors.getBlockedScopeProgressBarForeground();
        default:
            throw new NoSuchElementException( "Index: " + idx );
        }
    }

    public int getScopeCount()
    {
        return 4;
    }
    
    public long getFileSize()
    {
        return file.getTotalDataSize();
    }
}
