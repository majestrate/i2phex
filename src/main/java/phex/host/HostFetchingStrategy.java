/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2007 Phex Development Group
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
 *  --- SVN Information ---
 *  $Id: HostFetchingStrategy.java 4097 2007-12-27 01:55:18Z complication $
 */
package phex.host;

/**
 * Interface for providing a strategy how new hosts are fetched 
 * for the host catcher.
 */
public interface HostFetchingStrategy
{
    public enum FetchingReason
    {
        EnsureMinHosts,
        UpdateHosts
    }
    
    // temporary workaround method for post manager initialization
    public void postManagerInitRoutine();
    
    void fetchNewHosts( FetchingReason reason );
}