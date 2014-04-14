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
 *  Created on 05.05.2005
 *  --- CVS Information ---
 *  $Id: GradientPanel.java 4363 2009-01-16 10:37:30Z gregork $
 */
package phex.gui.common;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;

import javax.swing.JPanel;

/**
 *
 */
public class GradientPanel extends JPanel 
{
    protected Color fromColor;
    protected Color toColor;
    /**
     * 
     */
    public GradientPanel( Color fromColor, Color toColor )
    {
        super( );
        this.fromColor = fromColor;
        this.toColor = toColor;
    }
    
    /* These rectangles/insets are allocated once for all 
     * paintComponent() calls.  Re-using rectangles rather than 
     * allocating them in each paint call substantially reduced the time
     * it took paint to run.  Obviously, this method can't be re-entered.
     */
    private static Rectangle viewRect = new Rectangle();
    private static Rectangle textRect = new Rectangle();
    private static Rectangle iconRect = new Rectangle();
    
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
        Paint gradient = new GradientPaint(
            0, 0, fromColor,
            viewRect.width, viewRect.height, toColor );
        g2.setPaint( gradient );
        g2.fillRect( viewRect.x, viewRect.y,
            viewRect.width, viewRect.height );
    }
}
