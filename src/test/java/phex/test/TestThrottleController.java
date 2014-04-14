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
 *  $Id: TestThrottleController.java 4168 2008-04-15 20:44:36Z complication $
 */
package phex.test;

import junit.framework.TestCase;
import phex.common.bandwidth.BandwidthController;

public class TestThrottleController extends TestCase
{
    public TestThrottleController()
    {
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    /*public void testThroughput() throws Exception
    {
        ThrottleController controller = ThrottleController.acquireThrottle( "Test1" );
        controller.setRate( 1000 * 10 );

        long start = System.currentTimeMillis();

        for ( int i = 0; i < 3; i++ )
        {
            for ( int j = 0; j < 1000; j++ )
            {
                controller.controlThrottle( 100 );
                Thread.sleep( 10 );
            }
            System.out.println( controller.toDebugString() );
        }

        long end = System.currentTimeMillis();
        double dataSize = 100 * 1000 * 3;
        System.out.println( "Throughput: " + (dataSize / (end-start)));
        System.out.println( controller.toDebugString() );
    }*/

    public void testThroughput2() throws Exception
    {
        BandwidthController controller =
            new BandwidthController( "Test2", 1000 );

        long start = System.currentTimeMillis();
        long totalData = 10000;
        while( totalData > 0 )
        {
            int toUse = controller.getAvailableByteCount( 1000, true, false );
            totalData -= toUse;
            controller.markBytesUsed(toUse);
            System.out.println( toUse + " - " + totalData + " - " + controller.toDebugString() );
        }
        long end = System.currentTimeMillis();
        System.out.println( "Throughput: " + (end-start)/1000 + "  " + (10000 / (end-start)/1000));
        System.out.println( controller.toDebugString() );
    }
    
    public void testRandThroughput() throws Exception
    {
        BandwidthController controller =
            new BandwidthController( "Test2", 1000 );

        long start = System.currentTimeMillis();
        long totalData = 10000;
        while( totalData > 0 )
        {
            int toUse = controller.getAvailableByteCount( 1000, true, false );
            totalData -= toUse;
            controller.markBytesUsed(toUse);
            System.out.println( toUse + " - " + totalData + " - " + controller.toDebugString() );
            Thread.sleep( (long)(Math.random() * 1000.0) );
        }
        long end = System.currentTimeMillis();
        System.out.println( "Throughput: " + (end-start)/1000 + "  " + (10000 / (end-start)/1000));
        System.out.println( controller.toDebugString() );
    }

}
