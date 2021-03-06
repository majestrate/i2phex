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
 *  $Id: Res.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex;


import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class Res
{
    private static ResourceBundle	mResources = null;

    static
    {
        try
        {
            mResources = ResourceBundle.getBundle("phex.resources.phex",
                                                 Locale.getDefault());
        }
        catch (MissingResourceException mre)
        {
            System.err.println("phex/resources/phex.properties not found");
            System.exit(1);
        }
    }

    public static String getStr(String nm)
    {
        try
        {
            return mResources.getString(nm);
        }
        catch (MissingResourceException mre)
        {
            return null;
        }
    }
}