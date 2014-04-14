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
 *  $Id: TestXQueueParameters.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.test;

import junit.framework.TestCase;
import phex.http.XQueueParameters;


public class TestXQueueParameters extends TestCase
{

    public TestXQueueParameters(String s)
    {
        super(s);
    }

    protected void setUp()
    {
    }

    protected void tearDown()
    {
    }

    public void testGetRequestSleepTime()
    {
        XQueueParameters xqueueparameters;

        xqueueparameters = new XQueueParameters( 1, 99, 99, 60 * 1000, 600 * 1000 );
        int intRet = xqueueparameters.getRequestSleepTime();
        assertEquals( 61 * 1000, intRet );

        xqueueparameters = new XQueueParameters( 2, 99, 99, 60 * 1000, 600 * 1000 );
        intRet = xqueueparameters.getRequestSleepTime();
        assertEquals( 121 * 1000, intRet );

        xqueueparameters = new XQueueParameters( 3, 99, 99, 60 * 1000, 600 * 1000 );
        intRet = xqueueparameters.getRequestSleepTime();
        assertEquals( 181 * 1000, intRet );

        xqueueparameters = new XQueueParameters( 4, 99, 99, 60 * 1000, 600 * 1000 );
        intRet = xqueueparameters.getRequestSleepTime();
        assertEquals( 241 * 1000, intRet );

        xqueueparameters = new XQueueParameters( 5, 99, 99, 60 * 1000, 600 * 1000 );
        intRet = xqueueparameters.getRequestSleepTime();
        assertEquals( 301 * 1000, intRet );

        xqueueparameters = new XQueueParameters( 6, 99, 99, 60 * 1000, 600 * 1000 );
        intRet = xqueueparameters.getRequestSleepTime();
        assertEquals( ((60 + 600 ) * 1000) / 2, intRet );

        xqueueparameters = new XQueueParameters( 90, 99, 99, 60 * 1000, 600 * 1000 );
        intRet = xqueueparameters.getRequestSleepTime();
        assertEquals( ((60 + 600 ) * 1000) / 2, intRet );
    }
}
