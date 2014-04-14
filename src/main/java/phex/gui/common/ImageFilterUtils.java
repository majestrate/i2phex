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
 *  Created on 02.02.2006
 *  --- CVS Information ---
 *  $Id: ImageFilterUtils.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.gui.common;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;

import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ImageFilterUtils
{
    public static Icon createGrayIcon(Icon icon)
    {
        return new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) icon)
            .getImage()));
    }

    public static Icon createGrayIcon(Icon icon, boolean brighter, int percent)
    {
        GrayFilter filter = new GrayFilter( brighter, percent );
        ImageProducer prod = new FilteredImageSource(((ImageIcon) icon)
            .getImage().getSource(), filter);
        Image grayImage = Toolkit.getDefaultToolkit().createImage(prod);
        return new ImageIcon(grayImage);
    }

}
