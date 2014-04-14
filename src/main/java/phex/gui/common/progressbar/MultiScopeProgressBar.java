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
 *  $Id: MultiScopeProgressBar.java 4360 2009-01-16 09:08:57Z gregork $
 */
package phex.gui.common.progressbar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.UIManager;

import phex.download.DownloadScope;
import phex.download.DownloadScopeList;
import phex.gui.common.PhexColors;

/**
 *
 */
public class MultiScopeProgressBar extends JPanel 
{
    private MultiScopeProvider provider;
    
    /**
     * 
     */
    public MultiScopeProgressBar( )
    {
        super( );
        setBackground( PhexColors.getScopeProgressBarBackground() );
        setForeground( PhexColors.getScopeProgressBarForeground() );
        setBorder( UIManager.getBorder( "ProgressBar.border" ) );
    }
    
    public void setProvider( MultiScopeProvider provider )
    {
        this.provider = provider;
        repaint();
    }
    
    /* These rectangles/insets are allocated once for all 
     * paintComponent() calls.  Re-using rectangles rather than 
     * allocating them in each paint call substantially reduced the time
     * it took paint to run.  Obviously, this method can't be re-entered.
     */
    private static Rectangle viewRect = new Rectangle();
    
    protected void paintComponent( Graphics g )
    {
        // paint background.
        g.setColor( getBackground() );
        g.fillRect( 0, 0, getWidth(), getHeight() );
        
        Insets i = getInsets();
        viewRect.x = i.left;
        viewRect.y = i.top;
        viewRect.width = getWidth() - (i.right + viewRect.x);
        viewRect.height = getHeight() - (i.bottom + viewRect.y);
        
        // paint gradient
        Graphics2D g2 = (Graphics2D)g;
        
        if ( provider != null )
        {
            int count = provider.getScopeCount();
            for ( int j = 0; j < count; j++ )
            {
                paintScopeList( provider.getScopeAt( j ), 
                    provider.getScopeColor( j ), g2);
            }
        }        
    }
    
    private void paintScopeList( DownloadScopeList scopeList, Color baseColor, Graphics2D g2 )
    {
        double valPerPixel = (provider.getFileSize()-1) / (double)viewRect.width;
        int scopeCount = scopeList.size();
        Color useColor;

        for( int i = 0; i < scopeCount; i++ )
        {
            DownloadScope scope;
            try
            {
                scope = scopeList.getScopeAt(i);
                if ( scope == null )
                {
                    //list changed while painting
                    break;
                }
            }
            catch ( IndexOutOfBoundsException exp )
            {// list changed while painting
                break;
            }
            double startPx = scope.getStart() / valPerPixel;
            double endPx = scope.getEnd() / valPerPixel;
            double width = endPx - startPx;
            int alpha = (int)Math.min( 245*width, 245 )+10;
            if ( alpha < 255 )
            {
                alpha = Math.max( 0, alpha );
                useColor = new Color( baseColor.getRed(), baseColor.getGreen(),
                    baseColor.getBlue(), alpha );
            }
            else
            {
                useColor = baseColor;
            }
            g2.setColor( useColor );
            int rectWidth = (int)Math.max( width, 1 );
            g2.fillRect(viewRect.x + (int)startPx, viewRect.y, rectWidth, viewRect.height);
        }
    }
    
    ////////////// start standard size methods /////////////////////
    public Dimension getPreferredSize() {
        if ( isPreferredSizeSet() )
            return super.getPreferredSize();
        else
            return computePreferredSize();
    }
    public Dimension getMaximumSize() {
        if ( isMaximumSizeSet() )
            return super.getMaximumSize();
        else
            return computeMaximumSize();
    }
    public Dimension getMinimumSize() {
        if ( isMinimumSizeSet() )
            return super.getMinimumSize();
        else
            return computeMinimumSize();
    }
    private boolean isPreferredSizeSet = false;
    private boolean isMaximumSizeSet = false;
    private boolean isMinimumSizeSet = false;
    public boolean isPreferredSizeSet() {
        return isPreferredSizeSet;
    }
    public void setPreferredSize(Dimension sz) {
        isPreferredSizeSet = (sz!=null);
        super.setPreferredSize(sz);
    }
    public boolean isMaximumSizeSet() {
        return isMaximumSizeSet;
    }
    public void setMaximumSize(Dimension sz) {
        isMaximumSizeSet = (sz!=null);
        super.setMaximumSize(sz);
    }
    public boolean isMinimumSizeSet() {
        return isMinimumSizeSet;
    }
    public void setMinimumSize(Dimension sz) {
        isMinimumSizeSet = (sz!=null);
            super.setMinimumSize(sz);
    }
    
    protected Dimension computePreferredSize()
    {
        Dimension dim = super.getPreferredSize();
        dim.height = 25;
        return dim;
    }
    
    //Usually my panels don't have different max/min sizes, hence the code below...
    protected Dimension computeMaximumSize()
    {
        return computePreferredSize();
    }

    protected Dimension computeMinimumSize()
    {
        return computePreferredSize();
    }
    //////////////end standard size methods /////////////////////
}
