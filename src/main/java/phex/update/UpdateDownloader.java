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
 *  $Id: UpdateDownloader.java 4362 2009-01-16 10:27:18Z gregork $
 */
package phex.update;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.SystemUtils;

import phex.prefs.core.UpdatePrefs;
import phex.servent.Servent;

 
public class UpdateDownloader
{	
    // TODO provide these urls through the update notification xml
	public static void createUpdateDownload() throws URIException
	{
    	String uriStr; 
    	
    	if ( SystemUtils.IS_OS_MAC_OSX ) 
    	{
    		uriStr = UpdatePrefs.UPDATE_URI_MAC_OSX;
    	}
    	else if ( SystemUtils.IS_OS_WINDOWS ) 
    	{
    		uriStr = UpdatePrefs.UPDATE_URI_WINDOWS;
    	}
    	else 
    	{
    		uriStr = UpdatePrefs.UPDATE_URI_OTHER; 
    	}
		createDownload(uriStr); 
	}
	
	private static void createDownload(String uriStr) throws URIException
    {
    	
        if (uriStr.length() == 0)
        {
            return;
        }
        URI uri = new URI( uriStr, true );
        Servent.getInstance().getDownloadService().addFileToDownload( uri, true );
    }
}