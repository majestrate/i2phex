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
 *  $Id: TestCircularQueue.java 3942 2007-09-29 15:41:44Z gregork $
 */
package phex.test;

import java.util.*;

import phex.utils.AccessUtils;
import phex.utils.CircularQueue;
import junit.framework.TestCase;

/**
 * 
 */
public class TestCircularQueue extends TestCase
{
    protected void setUp()
    {
    }

    protected void tearDown()
    {
    }
    
    /**
     * This test is reported from John jpk129 (a.t) mail (d.o't) usask (d:o't) ca to 
     * demonstrate a bug  
     *
     */
    public void testDropping()
    {
        CircularQueue cq = new CircularQueue( 15 );
        for(int i = 1; i < 101; i++)
        {
            TestObj tobj = (TestObj)cq.addToHead(new TestObj(i));
            if(tobj != null)
            {
                // this element should be dropped... VERIFY it!!
                Object[] elements = (Object[])AccessUtils.getFieldValue( cq,
                    "elements" );
                List list = Arrays.asList( elements );
                assertFalse( list.contains( tobj ) );
            }
        }
        
        cq = new CircularQueue( 15 );
        for(int i = 1; i < 101; i++)
        {
            TestObj tobj = (TestObj)cq.addToTail(new TestObj(i));
            if(tobj != null)
            {
                // this element should be dropped... VERIFY it!!
                Object[] elements = (Object[])AccessUtils.getFieldValue( cq,
                    "elements" );
                List list = Arrays.asList( elements );
                assertFalse( list.contains( tobj ) );
            }
        }
    }
    
    class TestObj
    {
        String id;
        TestObj(int cnt) { this.id = "id:" + cnt;}
        public String toString() { return id;}
    }
}
