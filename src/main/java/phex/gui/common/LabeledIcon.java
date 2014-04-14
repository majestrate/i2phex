/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2005 Phex Development Group
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
 *  Created on 06.04.2006
 *  --- CVS Information ---
 *  $Id: LabeledIcon.java 4363 2009-01-16 10:37:30Z gregork $
 */
package phex.gui.common;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

public class LabeledIcon implements Icon
{
    private Icon delegate;
    private String label;
    
    public LabeledIcon( Icon delegate, String label )
    {
        assert delegate != null : "Delegate icon is null.";
        this.delegate = delegate;
        this.label = label;
    }
    
    public void setLabel( String label )
    {
        this.label = label;
    }

    /**
     * @see javax.swing.Icon#getIconHeight()
     */
    public int getIconHeight()
    {
        return delegate.getIconHeight();
    }

    /**
     * @see javax.swing.Icon#getIconWidth()
     */
    public int getIconWidth()
    {
        return delegate.getIconWidth();
    }

    /**
     * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
     */
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        delegate.paintIcon( c, g, x, y );
        if ( label != null )
        {
            Graphics derivedContext = g.create();
            
            Font font = derivedContext.getFont();
            font = font.deriveFont( Font.BOLD, font.getSize2D() - 4 );
            derivedContext.setFont( font );
            
            if ( derivedContext instanceof Graphics2D )
            {
                Graphics2D g2 = (Graphics2D)derivedContext;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            }

            FontMetrics metrics = derivedContext.getFontMetrics();
            Rectangle2D bounds = metrics.getStringBounds( label, derivedContext );
            
            derivedContext.drawString( label, 
                (int)(x + delegate.getIconWidth() / 2 
                    - bounds.getWidth() / 2 ), 
                (int)(y + delegate.getIconHeight() / 2 
                    + bounds.getHeight() / 2 ) );
        }
    }
}