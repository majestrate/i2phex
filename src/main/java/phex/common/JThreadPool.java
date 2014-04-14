/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2008 Phex Development Group
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
 *  $Id$
 */
package phex.common;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import phex.common.log.NLogger;

public class JThreadPool
{
    private final ThreadPoolExecutor pool;
    
    public JThreadPool()
    {
        pool = new ThreadPoolExecutor( 1, Integer.MAX_VALUE, 30, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>() );
    }
    
    public void executeNamed( final Runnable runnable, final String name )
    {
        pool.execute( new NamedThreadRunnable( name, runnable ) );
    }
    
    public void shutdown()
    {
        pool.shutdown();
    }
    
    private static final class NamedThreadRunnable implements Runnable
    {
        private final String name;

        private final Runnable runnable;

        private NamedThreadRunnable(String name, Runnable runnable)
        {
            this.name = name;
            this.runnable = runnable;
        }

        public void run()
        {
            Thread currentThread = Thread.currentThread();
            String oldName = currentThread.getName();
            currentThread.setName( name + "-" + oldName );
            try
            {
                runnable.run();
            }
            catch ( Throwable t )
            {
                NLogger.error( JThreadPool.class, t, t);
            }
            finally
            {
                currentThread.setName( oldName );
            }
        }
    }
}