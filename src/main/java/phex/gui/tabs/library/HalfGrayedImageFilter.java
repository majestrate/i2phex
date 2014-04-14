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
 *  Created on 10.01.2005
 *  --- CVS Information ---
 *  $Id: HalfGrayedImageFilter.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.gui.tabs.library;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;

import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;


class HalfGrayedImageFilter extends GrayFilter
{
    public static Icon createHalfGrayIcon(Icon icon)
    {
        HalfGrayedImageFilter filter = new HalfGrayedImageFilter();
        ImageProducer prod = new FilteredImageSource(((ImageIcon) icon)
            .getImage().getSource(), filter);
        Image grayImage = Toolkit.getDefaultToolkit().createImage(prod);
        return new ImageIcon(grayImage);
    }

    /**
     * @param b
     * @param p
     */
    public HalfGrayedImageFilter()
    {
        super(true, 60);
        canFilterIndexColorModel = false;
    }

    public void filterRGBPixels(int x, int y, int w, int h, int pixels[],
        int off, int scansize)
    {
        for (int cy = 0; cy < h; cy++)
        {
            for (int cx = w-y-1; cx < w; cx++)
            {
                if ( cx == w-y-1)
                {
                    if ( (pixels[cx]>>24) != 0 )
                    {
                        pixels[cx] = 0xff000000;
                    }
                }
                else
                {
                    pixels[cx] = filterRGB(x + cx, y + cy, pixels[cx]);
                }
            }
        }
        consumer.setPixels(x, y, w, h, ColorModel.getRGBdefault(), pixels,
            off, scansize);
    }

}