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
 *  Created on 30.03.2007
 *  --- SVN Information ---
 *  $Id: DownloadHandlerException.java 3807 2007-05-19 17:06:46Z gregork $
 */
package phex.download.handler;

public class DownloadHandlerException extends Exception
{

    /**
     * 
     */
    public DownloadHandlerException()
    {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public DownloadHandlerException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * @param message
     */
    public DownloadHandlerException( String message )
    {
        super( message );
    }

    /**
     * @param cause
     */
    public DownloadHandlerException( Throwable cause )
    {
        super( cause );
    }
    
}
