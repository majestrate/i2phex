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
 *  $Id: TestReadWriteLock.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.test;

import junit.framework.TestCase;
import phex.utils.ReadWriteLock;

public class TestReadWriteLock extends TestCase
{

    public TestReadWriteLock( String s )
    {
        super(s);
    }

    protected void setUp()
    {
    }

    protected void tearDown()
    {
    }

    public void testWriteReadLock()
        throws Exception
    {
        final ReadWriteLock lock = new ReadWriteLock();

        Runnable runner = new Runnable()
        {
            public void run()
            {
                try
                {
                    lock.writeLock();
                    lock.readLock();
                    lock.readUnlock();
                    lock.writeUnlock();
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                    fail( exp.getMessage() );
                }
            }
        };
        Thread thread = new Thread( runner );
        thread.start();
        thread.join( 5 * 1000 );
        if ( thread.isAlive() )
        {
            fail( "Thread still in locked situation" );
        }
    }

    public void testReadWriteLock()
        throws Exception
    {
        final ReadWriteLock lock = new ReadWriteLock();

        Runnable runner = new Runnable()
        {
            public void run()
            {
                try
                {
                    lock.readLock();
                    lock.writeLock();

                    lock.writeUnlock();
                    lock.readUnlock();
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                    fail( exp.getMessage() );
                }
            }
        };
        Thread thread = new Thread( runner );
        thread.start();
        thread.join( 5 * 1000 );
        if ( thread.isAlive() )
        {
            fail( "Thread still in locked situation" );
        }
    }

    public void testMultiReadLock()
        throws Exception
    {
        final ReadWriteLock lock = new ReadWriteLock();

        Runnable runner = new Runnable()
        {
            public void run()
            {
                try
                {
                    lock.readLock();
                    lock.readLock();
                    lock.readLock();
                    lock.readUnlock();
                    lock.readUnlock();
                    lock.readUnlock();
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                    fail( exp.getMessage() );
                }
            }
        };
        Thread thread = new Thread( runner );
        thread.start();
        thread.join( 5 * 1000 );
        if ( thread.isAlive() )
        {
            fail( "Thread still in locked situation" );
        }
    }

    /*
    // Takes to long to execute in every test...
    public void testLongWaitingLock()
        throws Exception
    {
        final ReadWriteLock lock = new ReadWriteLock();

        Runnable runner = new Runnable()
        {
            public void run()
            {
                try
                {
                    lock.readLock();
                    Thread.sleep( 10 * 60 * 1000 );
                    lock.readUnlock();
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                    fail( exp.getMessage() );
                }
            }
        };
        Thread thread = new Thread( runner );
        thread.start();
        thread.join( 1000 );
        boolean hasExcepted = false;
        try
        {
            lock.writeLock();
            fail( "Expected Exception" );
        }
        catch ( RuntimeException exp )
        {
            hasExcepted = true;
        }
        assertTrue( hasExcepted );
    }
    */
}