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
 *  $Id: FWImagePanel.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.gui.common;

import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class FWImagePanel extends JPanel
{
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;
    
    private int alignment = ALIGN_CENTER;
    
    private ImageIcon image = null;

    private boolean fillEntireArea = false;

    private boolean tileImage = false;

    public FWImagePanel()
    {
        super();
    }

    /**
     * @return Returns the alignment.
     */
    public int getAlignment()
    {
        return alignment;
    }
    /**
     * @param alignment The alignment to set.
     */
    public void setAlignment(int alignment)
    {
        this.alignment = alignment;
    }
    
    public void setFillEntireArea(boolean b)
    {
        fillEntireArea = b;
        repaint();
    }

    public boolean getFillEntireArea()
    {
        return fillEntireArea;
    }

    public void setTileImage(boolean b)
    {
        tileImage = b;
        repaint();
    }

    public boolean getTileImage()
    {
        return tileImage;
    }

    public void setImage(ImageIcon i)
    {
        image = i;
        repaint();
    }

    public ImageIcon getImage()
    {
        return image;
    }

    public void paintComponent(Graphics g)
    {
        if (isOpaque())
        {
            super.paintComponent(g);
            if (image != null)
            {
                int width = getWidth();
                int height = getHeight();

                g.setColor(getBackground());
                g.fillRect(0, 0, width, height);
                if (fillEntireArea)
                {
                    g.drawImage(image.getImage(), 0, 0, width, height, this);
                }
                else
                {
                    if (!tileImage)
                    {
                        int x, y;
                        switch (alignment)
                        {
                        case ALIGN_LEFT:
                            x = 0;
                            y = 0;
                            break;
                        case ALIGN_RIGHT:
                            x = width - image.getIconWidth();
                            y = height - image.getIconHeight();
                            break;
                        case ALIGN_CENTER:
                        default:
                            x = (width - image.getIconWidth()) / 2;
                            y = (height - image.getIconHeight()) / 2;
                            break;
                        }
                        g.drawImage(image.getImage(), x, y, this);
                    }
                    else
                    {
                        int tileW = image.getIconWidth();
                        int tileH = image.getIconHeight();
                        int xpos, ypos, startx, starty;
                        for (ypos = 0; height - ypos > 0; ypos += tileH)
                        {
                            for (xpos = 0; width - xpos > 0; xpos += tileW)
                            {
                                image.paintIcon(null, g, xpos, ypos);
                            }
                        }
                    }
                }
            }
        }
    }
}