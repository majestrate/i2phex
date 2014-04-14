package phex.gui.common.progressbar;

import java.awt.Color;

import phex.download.DownloadScopeList;

public interface MultiScopeProvider
{
    /**
     * Returns the number of scopes this provider has to offer.
     * @return the number of scopes this provider has to offer.
     */
    int getScopeCount();
    
    /**
     * Returns the scope at the specified index.
     * @param idx the index to get the scope for.
     * @return the scope at the given idx
     */
    DownloadScopeList getScopeAt( int idx );
    
    /**
     * Returns the drawing color at for the scope at the index.
     * @param idx the index to get the Color for.
     * @return the Color to draw the scope in.
     */
    Color getScopeColor( int idx );
    
    /**
     * Returns the file size the bar is displaying.
     * @return the file size.
     */
    long getFileSize();
    
}
