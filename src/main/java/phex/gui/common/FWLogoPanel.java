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
 *  $Id: FWLogoPanel.java 4363 2009-01-16 10:37:30Z gregork $
 */
package phex.gui.common;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class FWLogoPanel extends JPanel
{
    private Image logo;

    public FWLogoPanel( Image aLogo )
    {
        logo = aLogo;
    }

    public void update(Graphics g)
    {
        // Paint directly to avoid clearing the screen.
        paint(g);
    }

    public void paint(Graphics g)
    {
        super.paint( g );
        Dimension dim = getSize();
        int x = dim.width / 2 - logo.getWidth( this ) / 2;
        int y = dim.height / 2 - logo.getHeight( this ) / 2;
        g.drawImage( logo, x, y, null);
    }
}