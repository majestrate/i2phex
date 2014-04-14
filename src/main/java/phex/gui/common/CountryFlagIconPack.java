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
 *  Created on 27.04.2006
 *  --- CVS Information ---
 *  $Id: CountryFlagIconPack.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.gui.common;

import java.net.URL;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import phex.common.log.NLogger;

public class CountryFlagIconPack extends IconPack
{
    private Hashtable iconCache;
    
    public CountryFlagIconPack()
    {
        super();
        iconCache = new Hashtable();
    }
    
    /**
     * the method to fetch an icon using the keys defined
     * in Images.properties file.
     */
    public Icon getIcon( String key )
    {
        Icon icon = (Icon) iconCache.get( key );

        if ( icon == null )
        {  // if not in table instanciate it
            String url = "/phex/gui/resources/flags/" + key.toLowerCase() + ".png";
            URL imgURL = CountryFlagIconPack.class.getResource( url );
            if ( imgURL == null )
            {
                NLogger.warn( CountryFlagIconPack.class,
                    "No country flag found for " + key );
                icon = EMPTY_IMAGE_16;
            }
            else
            {
                icon = new ImageIcon( imgURL );
            }
            iconCache.put( key, icon );
        }
        return icon;
    }
}
