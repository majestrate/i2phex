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
 *  Created on 08.09.2005
 *  --- CVS Information ---
 *  $Id: ManagedFileException.java 3536 2006-08-05 22:16:44Z gregork $
 */
package phex.common.file;

@SuppressWarnings("serial")
public class ManagedFileException extends Exception
{
    public ManagedFileException( String message )
    {
        super( message );
    }

    public ManagedFileException( String message, Exception cause )
    {
        super( message, cause );
    }
}
